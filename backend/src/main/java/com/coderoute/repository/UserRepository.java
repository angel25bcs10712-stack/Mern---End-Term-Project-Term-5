package com.coderoute.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coderoute.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmailIgnoreCase(String email);
}