package com.coderoute.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coderoute.entity.LearningGoal;
import com.coderoute.entity.enums.GoalStatus;

public interface LearningGoalRepository extends JpaRepository<LearningGoal, UUID> {
	List<LearningGoal> findAllByUser_IdOrderByTargetDateAsc(UUID userId);
	List<LearningGoal> findAllByUser_IdAndStatus(UUID userId, GoalStatus status);
	java.util.Optional<LearningGoal> findFirstByUser_IdAndStatusOrderByTargetDateAsc(UUID userId, GoalStatus status);
}