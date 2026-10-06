package com.coderoute.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderoute.dto.auth.AuthResponse;
import com.coderoute.dto.auth.LoginRequest;
import com.coderoute.dto.auth.ProfileUpdateRequest;
import com.coderoute.dto.auth.RegisterRequest;
import com.coderoute.dto.auth.UserResponse;
import com.coderoute.entity.User;
import com.coderoute.entity.enums.UserRole;
import com.coderoute.repository.UserRepository;

import jakarta.persistence.EntityExistsException;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.findByEmailIgnoreCase(request.email().trim()).isPresent()) {
			throw new DuplicateEmailException();
		}
		User user = new User(request.name().trim(), request.email(),
				passwordEncoder.encode(request.password()), UserRole.USER);
		try {
			user = userRepository.saveAndFlush(user);
		} catch (DataIntegrityViolationException exception) {
			if (isEmailConflict(exception)) {
				throw new DuplicateEmailException();
			}
			throw exception;
		}
		return createAuthResponse(user);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		var authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
		AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
		return createAuthResponse(principal.getUser());
	}

	@Transactional
	public UserResponse updateProfile(AuthenticatedUser principal, ProfileUpdateRequest request) {
		User user = userRepository.findById(principal.getId())
				.orElseThrow(() -> new com.coderoute.error.ResourceNotFoundException("User"));
		user.setLeetcodeProfileUrl(normalizeLeetcodeProfileUrl(request.leetcodeProfileUrl()));
		return UserResponse.from(userRepository.save(user));
	}

	/**
	 * Normalizes an optional LeetCode profile link. Blank input clears the value;
	 * otherwise the link must point at leetcode.com so public stats can be resolved.
	 * No LeetCode credentials are ever involved.
	 */
	private String normalizeLeetcodeProfileUrl(String rawUrl) {
		if (rawUrl == null || rawUrl.isBlank()) {
			return null;
		}
		String url = rawUrl.trim();
		if (!url.toLowerCase(java.util.Locale.ROOT).startsWith("http://") && !url.toLowerCase(java.util.Locale.ROOT).startsWith("https://")) {
			url = "https://" + url;
		}
		java.net.URI uri;
		try {
			uri = java.net.URI.create(url);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Enter a valid LeetCode profile link, for example https://leetcode.com/u/username");
		}
		String host = uri.getHost();
		if (host == null || !(host.equalsIgnoreCase("leetcode.com") || host.toLowerCase(java.util.Locale.ROOT).endsWith(".leetcode.com"))) {
			throw new IllegalArgumentException("Enter a valid LeetCode profile link, for example https://leetcode.com/u/username");
		}
		if (url.length() > 255) {
			throw new IllegalArgumentException("LeetCode profile link must be 255 characters or fewer");
		}
		return url;
	}

	private AuthResponse createAuthResponse(User user) {
		return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
	}

	private boolean isEmailConflict(DataIntegrityViolationException exception) {
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			String message = cause.getMessage();
			if (message != null) {
				String lowered = message.toLowerCase(java.util.Locale.ROOT);
				if (lowered.contains("uk_app_user_email")
						|| (lowered.contains("app_user") && lowered.contains("email"))) {
					return true;
				}
			}
		}
		return false;
	}
}