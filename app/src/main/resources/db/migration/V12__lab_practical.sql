-- Lab : évaluation pratique par conteneur (ADR-0008 / SF-91a).
-- Ajoute la politique de validation des nœuds (rétrocompatible) et le schéma du contexte `lab`.

-- 1) Politique de validation d'un nœud. Défaut LESSONS_ONLY = comportement historique (aucune régression).
ALTER TABLE nodes
    ADD COLUMN completion_policy varchar(32) NOT NULL DEFAULT 'LESSONS_ONLY';

ALTER TABLE nodes
    ADD CONSTRAINT chk_nodes_completion_policy
        CHECK (completion_policy IN ('LESSONS_ONLY', 'LESSONS_AND_PRACTICAL'));

-- 2) Définition d'une épreuve pratique par nœud (référence souple vers nodes, comme evaluation).
CREATE TABLE practical_tasks (
    id               uuid PRIMARY KEY DEFAULT uuidv7(),
    node_id          uuid         NOT NULL,
    title            varchar(200) NOT NULL,
    image            varchar(200) NOT NULL,
    instructions     text         NOT NULL,
    workspace_seed   text,
    validation_ref   varchar(200) NOT NULL,
    timeout_seconds  int          NOT NULL DEFAULT 900,
    created_at       timestamptz  NOT NULL DEFAULT now(),
    updated_at       timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (node_id),
    CONSTRAINT chk_practical_tasks_timeout CHECK (timeout_seconds > 0)
);

-- 3) Tentatives d'un apprenant sur une épreuve pratique.
CREATE TABLE practical_attempts (
    id                 uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id            uuid         NOT NULL,
    practical_task_id  uuid         NOT NULL REFERENCES practical_tasks(id) ON DELETE CASCADE,
    node_id            uuid         NOT NULL,
    status             varchar(16)  NOT NULL DEFAULT 'PENDING',
    passed             boolean      NOT NULL DEFAULT false,
    output             text,
    started_at         timestamptz  NOT NULL DEFAULT now(),
    finished_at        timestamptz,
    CONSTRAINT chk_practical_attempts_status
        CHECK (status IN ('PENDING', 'RUNNING', 'PASSED', 'FAILED', 'EXPIRED'))
);

CREATE INDEX idx_practical_attempts_user_node ON practical_attempts(user_id, node_id);
CREATE INDEX idx_practical_tasks_node         ON practical_tasks(node_id);
