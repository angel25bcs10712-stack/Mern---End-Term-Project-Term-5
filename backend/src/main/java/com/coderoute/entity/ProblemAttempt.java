package com.coderoute.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.coderoute.entity.enums.AttemptStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "problem_attempt", indexes = {
		@Index(name = "ix_attempt_user_created", columnList = "user_id, created_at"),
		@Index(name = "ix_attempt_problem_created", columnList = "problem_id, created_at"),
		@Index(name = "ix_attempt_user_status", columnList = "user_id, status")
})
public class ProblemAttempt extends AuditedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attempt_user"))
	private User user;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "problem_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attempt_problem"))
	private Problem problem;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AttemptStatus status;

	@NotNull
	@PositiveOrZero
	@Column(name = "time_taken_seconds", nullable = false)
	private Integer timeTakenSeconds;

	@NotNull
	@Positive
	@Column(nullable = false)
	private Integer attempts;

	@Column(name = "solved_at")
	private Instant solvedAt;

	protected ProblemAttempt() {
	}

	public ProblemAttempt(
			User user,
			Problem problem,
			AttemptStatus status,
			Integer timeTakenSeconds,
			Integer attempts,
			Instant solvedAt) {
		this.user = user;
		this.problem = problem;
		this.status = status;
		this.timeTakenSeconds = timeTakenSeconds;
		this.attempts = attempts;
		this.solvedAt = solvedAt;
	}

	public void markSolved(Instant solvedAt) {
		this.status = AttemptStatus.SOLVED;
		this.solvedAt = Objects.requireNonNull(solvedAt);
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Problem getProblem() {
		return problem;
	}

	public AttemptStatus getStatus() {
		return status;
	}

	public Integer getTimeTakenSeconds() {
		return timeTakenSeconds;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public Instant getSolvedAt() {
		return solvedAt;
	}
}