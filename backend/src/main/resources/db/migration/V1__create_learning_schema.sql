CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_app_user_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE UNIQUE INDEX uk_app_user_email ON app_user (email);

CREATE TABLE topic (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20) NOT NULL,
    parent_topic_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_topic_name UNIQUE (name),
    CONSTRAINT fk_topic_parent FOREIGN KEY (parent_topic_id) REFERENCES topic (id),
    CONSTRAINT ck_topic_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE TABLE problem (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    topic_id UUID NOT NULL,
    external_url VARCHAR(2048),
    tags TEXT NOT NULL DEFAULT '[]',
    estimated_time_minutes INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_problem_topic FOREIGN KEY (topic_id) REFERENCES topic (id),
    CONSTRAINT ck_problem_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_problem_estimated_time CHECK (estimated_time_minutes > 0)
);

CREATE TABLE problem_attempt (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    problem_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    time_taken_seconds INTEGER NOT NULL,
    attempts INTEGER NOT NULL,
    solved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_attempt_problem FOREIGN KEY (problem_id) REFERENCES problem (id),
    CONSTRAINT ck_attempt_status CHECK (status IN ('ATTEMPTED', 'SOLVED', 'ABANDONED')),
    CONSTRAINT ck_attempt_solved_at CHECK (status <> 'SOLVED' OR solved_at IS NOT NULL),
    CONSTRAINT ck_attempt_time CHECK (time_taken_seconds >= 0),
    CONSTRAINT ck_attempt_count CHECK (attempts > 0)
);

CREATE TABLE user_topic_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    topic_id UUID NOT NULL,
    problems_solved INTEGER NOT NULL DEFAULT 0,
    problems_attempted INTEGER NOT NULL DEFAULT 0,
    accuracy NUMERIC(5, 2) NOT NULL DEFAULT 0,
    average_time_seconds NUMERIC(10, 2),
    current_difficulty VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_progress_user_topic UNIQUE (user_id, topic_id),
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_progress_topic FOREIGN KEY (topic_id) REFERENCES topic (id),
    CONSTRAINT ck_progress_counts CHECK (problems_solved >= 0 AND problems_attempted >= 0),
    CONSTRAINT ck_progress_accuracy CHECK (accuracy BETWEEN 0 AND 100),
    CONSTRAINT ck_progress_average_time CHECK (average_time_seconds IS NULL OR average_time_seconds >= 0),
    CONSTRAINT ck_progress_difficulty CHECK (current_difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE TABLE learning_goal (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    target_problems INTEGER NOT NULL,
    target_date DATE NOT NULL,
    weekly_hours NUMERIC(5, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT ck_goal_target CHECK (target_problems > 0),
    CONSTRAINT ck_goal_weekly_hours CHECK (weekly_hours BETWEEN 0.50 AND 168.00),
    CONSTRAINT ck_goal_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'PAUSED', 'CANCELLED'))
);

CREATE INDEX ix_topic_parent ON topic (parent_topic_id);
CREATE INDEX ix_problem_topic_difficulty ON problem (topic_id, difficulty);
CREATE INDEX ix_attempt_user_created ON problem_attempt (user_id, created_at DESC);
CREATE INDEX ix_attempt_problem_created ON problem_attempt (problem_id, created_at DESC);
CREATE INDEX ix_attempt_user_status ON problem_attempt (user_id, status);
CREATE INDEX ix_progress_user ON user_topic_progress (user_id);
CREATE INDEX ix_goal_user_status_date ON learning_goal (user_id, status, target_date);