-- Final Challenge : évaluation finale immersive (ADR-0009 / SF-92a-1).
-- Authoring uniquement : définition de scénarios versionnés (immuables). Le schéma d'exécution
-- (sessions, décisions, résultats) sera ajouté par SF-92a-2. Références souples vers `nodes`
-- (pas de FK inter-contexte, comme `evaluation` / `lab`).

-- 1) Scénario : métadonnées stables, identifié par une clé lisible (slug) et rattaché à un nœud.
CREATE TABLE challenge_scenarios (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    scenario_key  varchar(120) NOT NULL UNIQUE,
    node_id       uuid         NOT NULL,
    title         varchar(200) NOT NULL,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX idx_challenge_scenarios_node ON challenge_scenarios(node_id);

-- 2) Version de scénario : définition complète figée en JSON. Une version publiée est IMMUABLE
--    (on ne met jamais à jour `definition` d'une version existante ; un réimport crée une version
--    suivante). Une tentative sera plus tard liée à une version précise (auditabilité).
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

CREATE INDEX idx_challenge_versions_scenario ON challenge_scenario_versions(scenario_id, version DESC);
