package com.coderoute.entity;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "topic", indexes = {
		@Index(name = "ix_topic_parent", columnList = "parent_topic_id")
}, uniqueConstraints = {
		@UniqueConstraint(name = "uk_topic_name", columnNames = "name")
})
public class Topic extends AuditedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Size(max = 120)
	@Column(nullable = false, length = 120)
	private String name;

	@Size(max = 2000)
	@Column(columnDefinition = "text")
	private String description;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Difficulty difficulty;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_topic_id", foreignKey = @ForeignKey(name = "fk_topic_parent"))
	private Topic parentTopic;

	protected Topic() {
	}

	public Topic(String name, String description, Difficulty difficulty, Topic parentTopic) {
		this.name = name;
		this.description = description;
		this.difficulty = difficulty;
		this.parentTopic = parentTopic;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public Topic getParentTopic() {
		return parentTopic;
	}

	public void update(String name, String description, Difficulty difficulty, Topic parentTopic) {
		this.name = name;
		this.description = description;
		this.difficulty = difficulty;
		this.parentTopic = parentTopic;
	}
}