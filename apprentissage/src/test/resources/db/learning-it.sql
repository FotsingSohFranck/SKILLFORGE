-- Schéma minimal pour les tests d'intégration du contexte Apprentissage.
-- Tables du contexte (enrollments, node_progress, lesson_progress) + tables lues pour les
-- compteurs (nodes, lessons) avec un petit jeu de données.

CREATE TABLE nodes (
    id                uuid PRIMARY KEY DEFAULT uuidv7(),
    roadmap_id        uuid NOT NULL,
    slug              varchar(120) NOT NULL,
    completion_policy varchar(32) NOT NULL DEFAULT 'LESSONS_ONLY'
);

CREATE TABLE lessons (
    id      uuid PRIMARY KEY DEFAULT uuidv7(),
    node_id uuid NOT NULL,
    slug    varchar(120) NOT NULL
);

CREATE TABLE enrollments (
    id                    uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id               uuid NOT NULL,
    roadmap_id            uuid NOT NULL,
    progress_percent      numeric(5,2) NOT NULL DEFAULT 0,
    last_active_node_id   uuid,
    last_active_lesson_id uuid,
    last_activity_at      timestamptz,
    enrolled_at           timestamptz NOT NULL DEFAULT now(),
    created_at            timestamptz NOT NULL DEFAULT now(),
    updated_at            timestamptz NOT NULL DEFAULT now(),
    UNIQUE (user_id, roadmap_id)
);

CREATE TABLE node_progress (
    id             uuid PRIMARY KEY DEFAULT uuidv7(),
    enrollment_id  uuid NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    node_id        uuid NOT NULL,
    status         varchar(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    percent        numeric(5,2) NOT NULL DEFAULT 0,
    last_lesson_id uuid,
    last_viewed_at timestamptz,
    practical_passed boolean NOT NULL DEFAULT false,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    UNIQUE (enrollment_id, node_id)
);

CREATE TABLE lesson_progress (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    enrollment_id uuid NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    node_id       uuid NOT NULL,
    lesson_id     uuid NOT NULL,
    status        varchar(20) NOT NULL DEFAULT 'VIEWED',
    viewed_at     timestamptz,
    completed_at  timestamptz,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (enrollment_id, lesson_id)
);

CREATE TABLE bookmarks (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id     uuid NOT NULL,
    target_type varchar(20) NOT NULL,
    target_id   uuid NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    UNIQUE (user_id, target_type, target_id)
);

CREATE TABLE streaks (
    id                 uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id            uuid NOT NULL UNIQUE,
    current_length     int  NOT NULL DEFAULT 0,
    longest_length     int  NOT NULL DEFAULT 0,
    last_activity_date date,
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now()
);

-- Jeu de données : 1 parcours (…a1), nœud "caching" (…b1, 2 leçons), nœud "sql" (…b2, 1 leçon).
INSERT INTO nodes(id, roadmap_id, slug) VALUES
    ('019a0000-0000-7000-8000-0000000000b1'::uuid, '019a0000-0000-7000-8000-0000000000a1'::uuid, 'caching'),
    ('019a0000-0000-7000-8000-0000000000b2'::uuid, '019a0000-0000-7000-8000-0000000000a1'::uuid, 'sql');

INSERT INTO lessons(id, node_id, slug) VALUES
    ('019a0000-0000-7000-8000-0000000000c1'::uuid, '019a0000-0000-7000-8000-0000000000b1'::uuid, 'intro'),
    ('019a0000-0000-7000-8000-0000000000c2'::uuid, '019a0000-0000-7000-8000-0000000000b1'::uuid, 'strategies'),
    ('019a0000-0000-7000-8000-0000000000c3'::uuid, '019a0000-0000-7000-8000-0000000000b2'::uuid, 'select');
