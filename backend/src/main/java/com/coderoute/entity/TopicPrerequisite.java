package com.coderoute.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "topic_prerequisite", uniqueConstraints = @UniqueConstraint(
		name = "uk_topic_prerequisite", columnNames = { "dependent_topic_id", "prerequisite_topic_id" }))
public class TopicPrerequisite extends AuditedEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dependent_topic_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_topic_prerequisite_dependent"))
	private Topic dependentTopic;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "prerequisite_topic_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_topic_prerequisite_prerequisite"))
	private Topic prerequisiteTopic;

	protected TopicPrerequisite() {
	}

	public TopicPrerequisite(Topic dependentTopic, Topic prerequisiteTopic) {
		this.dependentTopic = dependentTopic;
		this.prerequisiteTopic = prerequisiteTopic;
	}

	public UUID getId() {
		return id;
	}

	public Topic getDependentTopic() {
		return dependentTopic;
	}

	public Topic getPrerequisiteTopic() {
		return prerequisiteTopic;
	}
}