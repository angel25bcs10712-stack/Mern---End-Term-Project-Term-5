package com.coderoute.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.coderoute.entity.enums.GoalStatus;
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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "learning_goal", indexes = {
		@Index(name = "ix_goal_user_status_date", columnList = "user_id, status, target_date")
})
public class LearningGoal extends AuditedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_goal_user"))
	private User user;

	@NotNull
	@Positive
	@Column(name = "target_problems", nullable = false)
	private Integer target;

	@NotNull
	@FutureOrPresent
	@Column(name = "target_date", nullable = false)
	private LocalDate targetDate;

	@NotNull
	@DecimalMin("0.50")
	@DecimalMax("168.00")
	@Column(name = "weekly_hours", nullable = false, precision = 5, scale = 2)
	private BigDecimal weeklyHours;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private GoalStatus status = GoalStatus.ACTIVE;

	protected LearningGoal() {
	}

	public LearningGoal(User user, Integer target, LocalDate targetDate, BigDecimal weeklyHours) {
		this.user = user;
		this.target = target;
		this.targetDate = targetDate;
		this.weeklyHours = weeklyHours;
	}

	public void updateStatus(GoalStatus status) {
		this.status = status;
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Integer getTarget() {
		return target;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public BigDecimal getWeeklyHours() {
		return weeklyHours;
	}

	public GoalStatus getStatus() {
		return status;
	}
}