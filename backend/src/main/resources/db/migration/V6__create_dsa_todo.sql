CREATE TABLE dsa_todo (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    problem_id UUID,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_dsa_todo_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_dsa_todo_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);

CREATE INDEX ix_dsa_todo_user ON dsa_todo (user_id, created_at DESC);
