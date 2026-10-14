-- =============================================================================
-- V3 — Contexte Catalogue (roadmaps en graphe, certifications, domaines d'examen)
-- =============================================================================

-- Un parcours : soit un TRACK technique, soit une CERTIFICATION.
CREATE TABLE roadmaps (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    slug       varchar(80)  NOT NULL UNIQUE,
    title      varchar(160) NOT NULL,
    summary    text,
    kind       varchar(20)  NOT NULL DEFAULT 'TRACK',
    position   int          NOT NULL DEFAULT 0,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT chk_roadmaps_kind CHECK (kind IN ('TRACK', 'CERTIFICATION'))
);

-- Détails de certification rattachés à une roadmap de type CERTIFICATION.
CREATE TABLE certifications (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    roadmap_id    uuid NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    code          varchar(20) NOT NULL UNIQUE,
    exam_format   varchar(20) NOT NULL,
    duration_minutes int      NOT NULL,
    passing_score numeric(5,2),
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_cert_format CHECK (exam_format IN ('MCQ', 'PRACTICAL'))
);
CREATE INDEX idx_certifications_roadmap ON certifications(roadmap_id);

-- Domaines d'examen pondérés d'une certification.
CREATE TABLE exam_domains (
    id               uuid PRIMARY KEY DEFAULT uuidv7(),
    certification_id uuid NOT NULL REFERENCES certifications(id) ON DELETE CASCADE,
    name             varchar(160) NOT NULL,
    weight           numeric(5,2) NOT NULL DEFAULT 0,
    position         int          NOT NULL DEFAULT 0,
    created_at       timestamptz  NOT NULL DEFAULT now(),
    updated_at       timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX idx_exam_domains_cert ON exam_domains(certification_id);

-- Regroupement des nœuds par phase au sein d'une roadmap.
CREATE TABLE phases (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    roadmap_id uuid NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    title      varchar(160) NOT NULL,
    position   int          NOT NULL DEFAULT 0,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (roadmap_id, position)
);
CREATE INDEX idx_phases_roadmap ON phases(roadmap_id);

-- Nœuds du graphe. `slug` unique au sein d'une roadmap.
CREATE TABLE nodes (
    id                uuid PRIMARY KEY DEFAULT uuidv7(),
    roadmap_id        uuid NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    phase_id          uuid REFERENCES phases(id) ON DELETE SET NULL,
    slug              varchar(120) NOT NULL,
    title             varchar(200) NOT NULL,
    summary           text,
    importance        varchar(20)  NOT NULL DEFAULT 'RECOMMENDED',
    estimated_minutes int,
    position          int          NOT NULL DEFAULT 0,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (roadmap_id, slug),
    CONSTRAINT chk_nodes_importance CHECK (importance IN ('CORE', 'RECOMMENDED', 'OPTIONAL'))
);
CREATE INDEX idx_nodes_roadmap ON nodes(roadmap_id);
CREATE INDEX idx_nodes_phase   ON nodes(phase_id);

-- Arêtes = prérequis INDICATIFS et NON bloquants entre nœuds d'une roadmap.
CREATE TABLE edges (
    id             uuid PRIMARY KEY DEFAULT uuidv7(),
    roadmap_id     uuid NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    source_node_id uuid NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    target_node_id uuid NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    kind           varchar(20) NOT NULL DEFAULT 'PREREQ',
    created_at     timestamptz NOT NULL DEFAULT now(),
    UNIQUE (source_node_id, target_node_id, kind),
    CONSTRAINT chk_edges_no_self CHECK (source_node_id <> target_node_id)
);
CREATE INDEX idx_edges_roadmap ON edges(roadmap_id);
CREATE INDEX idx_edges_source  ON edges(source_node_id);
CREATE INDEX idx_edges_target  ON edges(target_node_id);

-- Rattachement pondéré d'un nœud aux domaines d'examen (pour les certifications).
CREATE TABLE node_exam_domains (
    node_id        uuid NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    exam_domain_id uuid NOT NULL REFERENCES exam_domains(id) ON DELETE CASCADE,
    weight         numeric(5,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (node_id, exam_domain_id)
);
CREATE INDEX idx_node_exam_domains_domain ON node_exam_domains(exam_domain_id);

-- Triggers updated_at
CREATE TRIGGER trg_roadmaps_updated      BEFORE UPDATE ON roadmaps      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_certifications_updated BEFORE UPDATE ON certifications FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_exam_domains_updated  BEFORE UPDATE ON exam_domains  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_phases_updated        BEFORE UPDATE ON phases        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_nodes_updated         BEFORE UPDATE ON nodes         FOR EACH ROW EXECUTE FUNCTION set_updated_at();
