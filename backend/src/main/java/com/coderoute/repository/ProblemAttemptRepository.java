package com.coderoute.repository;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coderoute.dto.analytics.AnalyticsAttemptData;
import com.coderoute.entity.ProblemAttempt;

public interface ProblemAttemptRepository extends JpaRepository<ProblemAttempt, UUID> {
	boolean existsByProblem_Id(UUID problemId);
	long countByStatus(com.coderoute.entity.enums.AttemptStatus status);
	List<ProblemAttempt> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);
	List<ProblemAttempt> findTop100ByUser_IdOrderByCreatedAtDesc(UUID userId);

	@Query("select new com.coderoute.dto.analytics.AnalyticsAttemptData(p.topic.name, p.difficulty, a.status, a.attempts, a.timeTakenSeconds, a.createdAt) "
			+ "from ProblemAttempt a join a.problem p where a.user.id = :userId order by a.createdAt desc")
	List<AnalyticsAttemptData> findAnalyticsAttemptsByUserId(@Param("userId") UUID userId, Pageable pageable);

	List<ProblemAttempt> findAllByUser_IdAndProblem_IdOrderByCreatedAtDesc(UUID userId, UUID problemId);
	boolean existsByUser_IdAndProblem_IdAndStatus(UUID userId, UUID problemId,
			com.coderoute.entity.enums.AttemptStatus status);

	@Query("select distinct a.problem.id from ProblemAttempt a where a.user.id = :userId and a.status = com.coderoute.entity.enums.AttemptStatus.SOLVED and a.problem.id in :problemIds")
	List<UUID> findSolvedProblemIds(@Param("userId") UUID userId, @Param("problemIds") Collection<UUID> problemIds);

	@Query("select count(distinct a.problem.id) from ProblemAttempt a where a.user.id = :userId and a.problem.topic.id = :topicId")
	long countDistinctProblemsByUserAndTopic(@Param("userId") UUID userId, @Param("topicId") UUID topicId);

	@Query("select count(distinct a.problem.id) from ProblemAttempt a where a.user.id = :userId and a.problem.topic.id = :topicId and a.status = com.coderoute.entity.enums.AttemptStatus.SOLVED")
	long countDistinctSolvedByUserAndTopic(@Param("userId") UUID userId, @Param("topicId") UUID topicId);

	@Query("select avg(a.timeTakenSeconds) from ProblemAttempt a where a.user.id = :userId and a.problem.topic.id = :topicId")
	Double averageTimeByUserAndTopic(@Param("userId") UUID userId, @Param("topicId") UUID topicId);

	@Query("select count(distinct a.problem.id) from ProblemAttempt a where a.user.id = :userId")
	long countDistinctProblemsByUser(@Param("userId") UUID userId);

	@Query("select count(distinct a.problem.id) from ProblemAttempt a where a.user.id = :userId and a.status = com.coderoute.entity.enums.AttemptStatus.SOLVED")
	long countDistinctSolvedByUser(@Param("userId") UUID userId);
}