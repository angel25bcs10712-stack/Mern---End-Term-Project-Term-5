package com.coderoute.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coderoute.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
	boolean existsByNameIgnoreCase(String name);
	boolean existsByParentTopic_Id(UUID parentTopicId);
	List<Topic> findAllByParentTopic_Id(UUID parentTopicId);
	List<Topic> findAllByParentTopicIsNull();
}