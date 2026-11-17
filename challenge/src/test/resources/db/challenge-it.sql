-- Schéma autonome du contexte `challenge` pour les tests d'intégration (sous-ensemble de
-- V15__challenge.sql, exécuté par Testcontainers via JDBC).

CREATE TABLE challenge_scenarios (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    scenario_key  varchar(120) NOT NULL UNIQUE,
    node_id       uuid         NOT NULL,
    title         varchar(200) NOT NULL,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE challenge_scenario_versions (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    scenario_id   uuid         NOT NULL REFERENCES challenge_scenarios(id) ON DELETE CASCADE,
    version       int          NOT NULL,
    status        varchar(16)  NOT NULL DEFAULT 'PUBLISHED',
    definition    jsonb        NOT NULL,
    published_at  timestamptz,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (scenario_id, version),
    CONSTRAINT chk_challenge_version_status CHECK (status IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT chk_challenge_version_positive CHECK (version > 0)
);

CREATE TABLE challenge_attempts (
    id                uuid PRIMARY KEY DEFAULT uuidv7(),
    scenario_id       uuid         NOT NULL REFERENCES challenge_scenarios(id),
    scenario_version  int          NOT NULL,
    node_id           uuid         NOT NULL,
    learner_id        uuid         NOT NULL,
    status            varchar(16)  NOT NULL DEFAULT 'IN_PROGRESS',
    current_node_id   varchar(120) NOT NULL,
    state             jsonb        NOT NULL DEFAULT '{}'::jsonb,
    global_score      int,
    passed            boolean,
    result            jsonb,
    started_at        timestamptz  NOT NULL DEFAULT now(),
    completed_at      timestamptz,
    CONSTRAINT chk_challenge_attempt_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED'))
);

CREATE TABLE challenge_decisions (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    attempt_id  uuid         NOT NULL REFERENCES challenge_attempts(id) ON DELETE CASCADE,
    seq         int          NOT NULL,
    node_id     varchar(120) NOT NULL,
    choice_id   varchar(120) NOT NULL,
    decided_at  timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (attempt_id, seq)
);
