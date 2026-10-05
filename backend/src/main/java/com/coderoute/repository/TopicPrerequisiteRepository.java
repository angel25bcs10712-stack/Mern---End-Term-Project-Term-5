package com.coderoute.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coderoute.entity.TopicPrerequisite;

public interface TopicPrerequisiteRepository extends JpaRepository<TopicPrerequisite, UUID> {
	List<TopicPrerequisite> findAllByOrderByCreatedAtAsc();
	List<TopicPrerequisite> findAllByDependentTopic_Id(UUID topicId);
	boolean existsByDependentTopic_Id(UUID topicId);
	boolean existsByPrerequisiteTopic_Id(UUID topicId);
}