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
SELECT CAST(edge.id AS UUID), dependent.id, prerequisite.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('40000000-0000-0000-0000-000000000001', 'Two Pointers', 'Arrays'),
    ('40000000-0000-0000-0000-000000000002', 'Sliding Window', 'Two Pointers'),
    ('40000000-0000-0000-0000-000000000003', 'Sliding Window', 'Strings'),
    ('40000000-0000-0000-0000-000000000004', 'Hashing', 'Arrays'),
    ('40000000-0000-0000-0000-000000000005', 'Stack', 'Arrays'),
    ('40000000-0000-0000-0000-000000000006', 'Queue', 'Arrays'),
    ('40000000-0000-0000-0000-000000000007', 'Linked List', 'Arrays'),
    ('40000000-0000-0000-0000-000000000008', 'Binary Search', 'Arrays'),
    ('40000000-0000-0000-0000-000000000009', 'Trees', 'Stack'),
    ('40000000-0000-0000-0000-00000000000a', 'Trees', 'Queue'),
    ('40000000-0000-0000-0000-00000000000b', 'Graphs', 'Trees'),
    ('40000000-0000-0000-0000-00000000000c', 'Graphs', 'Stack'),
    ('40000000-0000-0000-0000-00000000000d', 'Graphs', 'Queue'),
    ('40000000-0000-0000-0000-00000000000e', 'Greedy', 'Arrays'),
    ('40000000-0000-0000-0000-00000000000f', 'Backtracking', 'Trees'),
    ('40000000-0000-0000-0000-000000000010', 'Dynamic Programming', 'Arrays'),
    ('40000000-0000-0000-0000-000000000011', 'Dynamic Programming', 'Hashing')
) AS edge(id, dependent_name, prerequisite_name)
JOIN topic dependent ON dependent.name = edge.dependent_name
JOIN topic prerequisite ON prerequisite.name = edge.prerequisite_name;