package com.coderoute.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.coderoute.entity.DsaTodo;

public interface DsaTodoRepository extends JpaRepository<DsaTodo, UUID> {
	List<DsaTodo> findByUser_IdOrderByCreatedAtDesc(UUID userId);

	Optional<DsaTodo> findByIdAndUser_Id(UUID id, UUID userId);

	/**
	 * Marks a user's unfinished to-do items complete when they solve the matching
	 * problem: either the linked problem or a task titled exactly like the problem.
	 */
	@Transactional
	@Modifying
	@Query("update DsaTodo t set t.completed = true, t.updatedAt = :now "
			+ "where t.user.id = :userId and t.completed = false "
			+ "and (t.problem.id = :problemId or lower(trim(t.title)) = lower(:title))")
	int completeMatchedForSolve(@Param("userId") UUID userId, @Param("problemId") UUID problemId,
			@Param("title") String title, @Param("now") Instant now);
}
