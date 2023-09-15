CREATE SEQUENCE project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE project (
    project_id BIGINT NOT NULL DEFAULT nextval('project_id_seq'),
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    PRIMARY KEY (project_id));

CREATE SEQUENCE task_state_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE task_state (
    task_state_id BIGINT NOT NULL DEFAULT nextval('task_state_id_seq'),
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at timestamp with time zone NOT NULL,
    left_task_state_id BIGINT,
    right_task_state_id BIGINT,
    project_id BIGINT NOT NULL,
    PRIMARY KEY (task_state_id));

CREATE SEQUENCE task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE task (
    task_id BIGINT NOT NULL DEFAULT nextval('task_id_seq'),
    name VARCHAR(255) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    description text,
    task_state_id BIGINT NOT NULL,
    PRIMARY KEY (task_id));

ALTER TABLE if EXISTS task_state
    ADD CONSTRAINT project_id_fk
    FOREIGN KEY (project_id) REFERENCES project;

ALTER TABLE if EXISTS task
    ADD CONSTRAINT task_state_id_fk
    FOREIGN KEY (task_state_id) REFERENCES task_state;

ALTER TABLE if EXISTS task_state
    ADD CONSTRAINT left_task_state_id_fk
    FOREIGN KEY (left_task_state_id) REFERENCES task_state;

ALTER TABLE if EXISTS task_state
    ADD CONSTRAINT right_task_state_id_fk
    FOREIGN KEY (right_task_state_id) REFERENCES task_state;