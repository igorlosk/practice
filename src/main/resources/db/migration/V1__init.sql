-- V1__init.sql
-- Первоначальная схема TaskFlow

-- ===========================================================
-- USERS
-- ===========================================================
CREATE TABLE users (
                       id           BIGSERIAL     PRIMARY KEY,
                       username     VARCHAR(50)   NOT NULL,
                       email        VARCHAR(255)  NOT NULL,
                       display_name VARCHAR(100),
                       created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                       updated_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

ALTER TABLE users ADD CONSTRAINT uq_users_username UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT uq_users_email    UNIQUE (email);

-- ===========================================================
-- PROJECTS
-- ===========================================================
CREATE TABLE projects (
                          id          BIGSERIAL    PRIMARY KEY,
                          name        VARCHAR(255) NOT NULL,
                          description TEXT,
                          owner_id    BIGINT       NOT NULL,
                          created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                          updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
            ON DELETE RESTRICT;

CREATE INDEX idx_projects_owner_id ON projects (owner_id);

-- ===========================================================
-- TASKS
-- ===========================================================
CREATE TABLE tasks (
                       id           BIGSERIAL    PRIMARY KEY,
                       title        VARCHAR(255) NOT NULL,
                       description  TEXT,
                       status       VARCHAR(20)  NOT NULL DEFAULT 'TODO',
                       priority     VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
                       project_id   BIGINT       NOT NULL,
                       creator_id   BIGINT       NOT NULL,
                       assignee_id  BIGINT,
                       due_date     TIMESTAMPTZ,
                       created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

ALTER TABLE tasks
    ADD CONSTRAINT chk_tasks_status
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED'));

ALTER TABLE tasks
    ADD CONSTRAINT chk_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'));

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
            ON DELETE CASCADE;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_creator
        FOREIGN KEY (creator_id) REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_assignee
        FOREIGN KEY (assignee_id) REFERENCES users (id)
            ON DELETE SET NULL;

CREATE INDEX idx_tasks_project_id     ON tasks (project_id);
CREATE INDEX idx_tasks_assignee_id    ON tasks (assignee_id);
CREATE INDEX idx_tasks_creator_id     ON tasks (creator_id);
CREATE INDEX idx_tasks_project_status ON tasks (project_id, status);
CREATE INDEX idx_tasks_created_at     ON tasks (created_at DESC);
CREATE INDEX idx_tasks_due_date       ON tasks (due_date) WHERE due_date IS NOT NULL;