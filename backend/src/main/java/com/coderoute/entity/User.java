package com.coderoute.entity;

import java.util.Locale;
import java.util.UUID;

import com.coderoute.entity.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "app_user")
public class User extends AuditedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String name;

	@NotBlank
	@Email
	@Size(max = 254)
	@Column(nullable = false, length = 254)
	private String email;

	@NotBlank
	@Size(max = 255)
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role = UserRole.USER;

	@Size(max = 255)
	@Column(name = "leetcode_profile_url", length = 255)
	private String leetcodeProfileUrl;

	protected User() {
	}

	public User(String name, String email, String passwordHash, UserRole role) {
		this.name = name;
		this.email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
		this.passwordHash = passwordHash;
		this.role = role == null ? UserRole.USER : role;
	}

	@PrePersist
	@PreUpdate
	private void normalizeEmail() {
		if (email != null) {
			email = email.trim().toLowerCase(Locale.ROOT);
		}
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public String getLeetcodeProfileUrl() {
		return leetcodeProfileUrl;
	}

	public void setLeetcodeProfileUrl(String leetcodeProfileUrl) {
		this.leetcodeProfileUrl = leetcodeProfileUrl;
	}
}