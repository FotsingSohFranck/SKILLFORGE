-- Schéma minimal du contexte Catalogue + jeu de données, pour le test d'intégration
-- (exécuté par Testcontainers via JDBC au démarrage du conteneur PostgreSQL 18).
-- Sous-ensemble autonome de V3__catalogue.sql — valide le mapping R2DBC des adapters.

CREATE TABLE roadmaps (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    slug       varchar(80)  NOT NULL UNIQUE,
    title      varchar(160) NOT NULL,
    summary    text,
    kind       varchar(20)  NOT NULL DEFAULT 'TRACK',
    position   int          NOT NULL DEFAULT 0,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE phases (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    roadmap_id uuid NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    title      varchar(160) NOT NULL,
    position   int          NOT NULL DEFAULT 0,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);

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
    UNIQUE (roadmap_id, slug)
);

CREATE TABLE edges (
    id             uuid PRIMARY KEY DEFAULT uuidv7(),
    roadmap_id     uuid NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    source_node_id uuid NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    target_node_id uuid NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    kind           varchar(20) NOT NULL DEFAULT 'PREREQ',
    created_at     timestamptz NOT NULL DEFAULT now(),
    UNIQUE (source_node_id, target_node_id, kind)
);

CREATE TABLE certifications (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    roadmap_id    uuid NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    code          varchar(20) NOT NULL UNIQUE,
    exam_format   varchar(20) NOT NULL,
    duration_minutes int      NOT NULL,
    passing_score numeric(5,2),
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE exam_domains (
    id               uuid PRIMARY KEY DEFAULT uuidv7(),
    certification_id uuid NOT NULL REFERENCES certifications(id) ON DELETE CASCADE,
    name             varchar(160) NOT NULL,
    weight           numeric(5,2) NOT NULL DEFAULT 0,
    position         int          NOT NULL DEFAULT 0,
    created_at       timestamptz  NOT NULL DEFAULT now(),
    updated_at       timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE node_exam_domains (
    node_id        uuid NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    exam_domain_id uuid NOT NULL REFERENCES exam_domains(id) ON DELETE CASCADE,
    weight         numeric(5,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (node_id, exam_domain_id)
);

-- Jeu de données (UUID fixes pour interroger par identifiant).
INSERT INTO roadmaps(id, slug, title, summary, kind, position) VALUES
    ('01930000-0000-7000-8000-0000000000b1', 'it-backend', 'IT Backend', 'Parcours de test.', 'TRACK', 0);

INSERT INTO phases(id, roadmap_id, title, position) VALUES
    ('01930000-0000-7000-8000-0000000000c1', '01930000-0000-7000-8000-0000000000b1', 'Fondamentaux', 0);

INSERT INTO nodes(id, roadmap_id, phase_id, slug, title, summary, importance, estimated_minutes, position) VALUES
    ('01930000-0000-7000-8000-0000000000d1', '01930000-0000-7000-8000-0000000000b1', '01930000-0000-7000-8000-0000000000c1', 'internet', 'Internet', NULL, 'CORE', 60, 0),
    ('01930000-0000-7000-8000-0000000000d2', '01930000-0000-7000-8000-0000000000b1', '01930000-0000-7000-8000-0000000000c1', 'git',      'Git',      NULL, 'CORE', 90, 1);

INSERT INTO edges(id, roadmap_id, source_node_id, target_node_id, kind) VALUES
    ('01930000-0000-7000-8000-0000000000e1', '01930000-0000-7000-8000-0000000000b1', '01930000-0000-7000-8000-0000000000d1', '01930000-0000-7000-8000-0000000000d2', 'PREREQ');

-- Parcours de certification + certification + domaines pondérés (découverte examens/révision).
INSERT INTO roadmaps(id, slug, title, summary, kind, position) VALUES
    ('01930000-0000-7000-8000-0000000000b2', 'ckad', 'CKAD', 'Certification Kubernetes.', 'CERTIFICATION', 1);

INSERT INTO certifications(id, roadmap_id, code, exam_format, duration_minutes, passing_score) VALUES
    ('01930000-0000-7000-8000-0000000000f1', '01930000-0000-7000-8000-0000000000b2', 'CKAD', 'PRACTICAL', 120, 66.0);

INSERT INTO exam_domains(id, certification_id, name, weight, position) VALUES
    ('01930000-0000-7000-8000-000000000da1', '01930000-0000-7000-8000-0000000000f1', 'Application Design', 20.0, 0),
    ('01930000-0000-7000-8000-000000000da2', '01930000-0000-7000-8000-0000000000f1', 'Application Deployment', 20.0, 1);
