CREATE TABLE topic_prerequisite (
    id UUID PRIMARY KEY,
    dependent_topic_id UUID NOT NULL,
    prerequisite_topic_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_topic_prerequisite UNIQUE (dependent_topic_id, prerequisite_topic_id),
    CONSTRAINT ck_topic_prerequisite_not_self CHECK (dependent_topic_id <> prerequisite_topic_id),
    CONSTRAINT fk_topic_prerequisite_dependent FOREIGN KEY (dependent_topic_id) REFERENCES topic (id),
    CONSTRAINT fk_topic_prerequisite_prerequisite FOREIGN KEY (prerequisite_topic_id) REFERENCES topic (id)
);

CREATE INDEX ix_topic_prerequisite_dependent ON topic_prerequisite (dependent_topic_id);
CREATE INDEX ix_topic_prerequisite_prerequisite ON topic_prerequisite (prerequisite_topic_id);

INSERT INTO topic_prerequisite (id, dependent_topic_id, prerequisite_topic_id, created_at, updated_at)
SELECT gen_random_uuid(), dependent.id, prerequisite.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('Two Pointers', 'Arrays'),
    ('Sliding Window', 'Two Pointers'),
    ('Sliding Window', 'Strings'),
    ('Hashing', 'Arrays'),
    ('Stack', 'Arrays'),
    ('Queue', 'Arrays'),
    ('Linked List', 'Arrays'),
    ('Binary Search', 'Arrays'),
    ('Trees', 'Stack'),
    ('Trees', 'Queue'),
    ('Graphs', 'Trees'),
    ('Graphs', 'Stack'),
    ('Graphs', 'Queue'),
    ('Greedy', 'Arrays'),
    ('Backtracking', 'Trees'),
    ('Dynamic Programming', 'Arrays'),
    ('Dynamic Programming', 'Hashing')
) AS edge(dependent_name, prerequisite_name)
JOIN topic dependent ON dependent.name = edge.dependent_name
JOIN topic prerequisite ON prerequisite.name = edge.prerequisite_name;
