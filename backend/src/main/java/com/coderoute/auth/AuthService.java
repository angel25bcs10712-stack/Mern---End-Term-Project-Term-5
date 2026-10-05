package com.coderoute.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderoute.dto.auth.AuthResponse;
import com.coderoute.dto.auth.LoginRequest;
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