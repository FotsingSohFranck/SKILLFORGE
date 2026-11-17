-- SF-92a-2 : tentatives de Final Challenge (sessions de simulation) et journal des décisions.
-- Le scénario est figé à une version précise pour garantir l'immuabilité des tentatives passées.

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

CREATE INDEX idx_challenge_attempts_learner ON challenge_attempts(learner_id, node_id);

CREATE TABLE challenge_decisions (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    attempt_id  uuid         NOT NULL REFERENCES challenge_attempts(id) ON DELETE CASCADE,
    seq         int          NOT NULL,
    node_id     varchar(120) NOT NULL,
    choice_id   varchar(120) NOT NULL,
    decided_at  timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (attempt_id, seq)
);

CREATE INDEX idx_challenge_decisions_attempt ON challenge_decisions(attempt_id, seq);
