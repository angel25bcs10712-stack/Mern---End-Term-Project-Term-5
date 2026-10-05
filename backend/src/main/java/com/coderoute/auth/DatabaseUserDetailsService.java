package com.coderoute.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.coderoute.repository.UserRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	public DatabaseUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return userRepository.findByEmailIgnoreCase(email)
				.map(AuthenticatedUser::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}