package com.coderoute.entity;

import com.coderoute.entity.enums.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "problem", indexes = {
		@Index(name = "ix_problem_topic_difficulty", columnList = "topic_id, difficulty")
})
public class Problem extends AuditedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Size(max = 200)
	@Column(nullable = false, length = 200)
	private String title;

	@NotBlank
	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Difficulty difficulty;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "topic_id", nullable = false, foreignKey = @ForeignKey(name = "fk_problem_topic"))
	private Topic topic;

	@Size(max = 2048)
	@Column(name = "external_url", length = 2048)
	private String externalUrl;

	@Convert(converter = StringArrayJsonConverter.class)
	@Column(nullable = false, columnDefinition = "text")
	private String[] tags = new String[0];

	@NotNull
	@Positive
	@Column(name = "estimated_time_minutes", nullable = false)
	private Integer estimatedTimeMinutes;

	protected Problem() {
	}

	public Problem(
			String title,
			String description,
			Difficulty difficulty,
			Topic topic,
			String externalUrl,
			String[] tags,
			Integer estimatedTimeMinutes) {
		this.title = title;
		this.description = description;
		this.difficulty = difficulty;
		this.topic = topic;
		this.externalUrl = externalUrl;
		this.tags = tags == null ? new String[0] : tags.clone();
		this.estimatedTimeMinutes = estimatedTimeMinutes;
	}

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public Topic getTopic() {
		return topic;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public String[] getTags() {
		return tags.clone();
	}

	public Integer getEstimatedTimeMinutes() {
		return estimatedTimeMinutes;
	}

	public void update(String title, String description, Difficulty difficulty, Topic topic, String externalUrl,
			String[] tags, Integer estimatedTimeMinutes) {
		this.title = title;
		this.description = description;
		this.difficulty = difficulty;
		this.topic = topic;
		this.externalUrl = externalUrl;
		this.tags = tags == null ? new String[0] : tags.clone();
		this.estimatedTimeMinutes = estimatedTimeMinutes;
	}
}

