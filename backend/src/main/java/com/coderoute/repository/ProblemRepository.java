package com.coderoute.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.coderoute.entity.Problem;
import com.coderoute.entity.enums.Difficulty;

public interface ProblemRepository extends JpaRepository<Problem, UUID>, JpaSpecificationExecutor<Problem> {
	boolean existsByTopic_Id(UUID topicId);
	List<Problem> findAllByTopic_Id(UUID topicId);
	List<Problem> findAllByTopic_IdAndDifficulty(UUID topicId, Difficulty difficulty);

	@Query("select problem.topic.id, count(problem.id) from Problem problem group by problem.topic.id")
	List<Object[]> countProblemsByTopic();
}