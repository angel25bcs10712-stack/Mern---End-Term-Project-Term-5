package com.coderoute.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.coderoute.entity.enums.Difficulty;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "user_topic_progress", indexes = {
		@Index(name = "ix_progress_user", columnList = "user_id")
}, uniqueConstraints = {
		@UniqueConstraint(name = "uk_progress_user_topic", columnNames = { "user_id", "topic_id" })
})
public class UserTopicProgress extends AuditedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_progress_user"))
	private User user;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "topic_id", nullable = false, foreignKey = @ForeignKey(name = "fk_progress_topic"))
	private Topic topic;

	@NotNull
	@PositiveOrZero
	@Column(name = "problems_solved", nullable = false)
	private Integer problemsSolved = 0;

	@NotNull
	@PositiveOrZero
	@Column(name = "problems_attempted", nullable = false)
	private Integer problemsAttempted = 0;

	@NotNull
	@DecimalMin("0.00")
	@DecimalMax("100.00")
	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal accuracy = BigDecimal.ZERO;

	@PositiveOrZero
	@Column(name = "average_time_seconds", precision = 10, scale = 2)
	private BigDecimal averageTimeSeconds;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "current_difficulty", nullable = false, length = 20)
	private Difficulty currentDifficulty = Difficulty.BEGINNER;

	protected UserTopicProgress() {
	}

	public UserTopicProgress(User user, Topic topic) {
		this.user = user;
		this.topic = topic;
	}

	public void updateSummary(
			Integer problemsSolved,
			Integer problemsAttempted,
			BigDecimal accuracy,
			BigDecimal averageTimeSeconds,
			Difficulty currentDifficulty) {
		this.problemsSolved = problemsSolved;
		this.problemsAttempted = problemsAttempted;
		this.accuracy = accuracy;
		this.averageTimeSeconds = averageTimeSeconds;
		this.currentDifficulty = currentDifficulty;
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Topic getTopic() {
		return topic;
	}

	public Integer getProblemsSolved() {
		return problemsSolved;
	}

	public Integer getProblemsAttempted() {
		return problemsAttempted;
	}

	public BigDecimal getAccuracy() {
		return accuracy;
	}

	public BigDecimal getAverageTimeSeconds() {
		return averageTimeSeconds;
	}

	public Difficulty getCurrentDifficulty() {
		return currentDifficulty;
	}
}