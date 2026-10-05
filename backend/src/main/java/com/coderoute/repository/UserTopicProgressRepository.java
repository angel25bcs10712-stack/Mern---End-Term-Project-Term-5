package com.coderoute.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coderoute.entity.UserTopicProgress;

public interface UserTopicProgressRepository extends JpaRepository<UserTopicProgress, UUID> {
	boolean existsByTopic_Id(UUID topicId);
	Optional<UserTopicProgress> findByUser_IdAndTopic_Id(UUID userId, UUID topicId);
	List<UserTopicProgress> findAllByUser_Id(UUID userId);
}