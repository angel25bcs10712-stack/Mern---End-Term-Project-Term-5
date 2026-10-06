package com.coderoute.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "dsa_todo", indexes = {
		@Index(name = "ix_dsa_todo_user", columnList = "user_id, created_at")
})
public class DsaTodo extends AuditedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dsa_todo_user"))
	private User user;

	@NotBlank
	@Size(max = 200)
	@Column(nullable = false, length = 200)
	private String title;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "problem_id", foreignKey = @ForeignKey(name = "fk_dsa_todo_problem"))
	private Problem problem;

	@NotNull
	@Column(nullable = false)
	private Boolean completed = Boolean.FALSE;

	protected DsaTodo() {
	}

	public DsaTodo(User user, String title, Problem problem) {
		this.user = user;
		this.title = title;
		this.problem = problem;
		this.completed = Boolean.FALSE;
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Problem getProblem() {
		return problem;
	}

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
}
