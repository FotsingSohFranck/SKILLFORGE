-- =============================================================================
-- V5 — Contexte Apprentissage (progression, navigation non linéaire, reprise)
-- =============================================================================
-- Cœur du produit. Le SERVEUR fait foi pour la progression.
-- Références inter-contextes (user_id, roadmap_id, node_id, lesson_id) = SOUPLES
-- (uuid sans FK). Les FK dures ne concernent que l'intra-contexte (enrollment_id).
-- =============================================================================

-- Inscription d'un apprenant à un parcours + état de REPRISE GLOBALE.
CREATE TABLE enrollments (
    id                    uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id               uuid NOT NULL,      -- réf souple -> iam.users
    roadmap_id            uuid NOT NULL,      -- réf souple -> catalogue.roadmaps
    progress_percent      numeric(5,2) NOT NULL DEFAULT 0,
    last_active_node_id   uuid,               -- reprise : dernier nœud actif du parcours
    last_active_lesson_id uuid,               -- reprise : dernière leçon consultée
    last_activity_at      timestamptz,
    enrolled_at           timestamptz NOT NULL DEFAULT now(),
    created_at            timestamptz NOT NULL DEFAULT now(),
    updated_at            timestamptz NOT NULL DEFAULT now(),
    UNIQUE (user_id, roadmap_id)
);
CREATE INDEX idx_enrollments_user ON enrollments(user_id);
-- Support de la REPRISE GLOBALE : dernier parcours actif d'un utilisateur.
CREATE INDEX idx_enrollments_last_activity ON enrollments(user_id, last_activity_at DESC);

-- Progression par NŒUD + REPRISE EXACTE (dernière leçon consultée dans ce nœud).
CREATE TABLE node_progress (
    id             uuid PRIMARY KEY DEFAULT uuidv7(),
    enrollment_id  uuid NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    node_id        uuid NOT NULL,             -- réf souple -> catalogue.nodes
    status         varchar(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    percent        numeric(5,2) NOT NULL DEFAULT 0,
    last_lesson_id uuid,                       -- reprise exacte par nœud -> contenu.lessons
    last_viewed_at timestamptz,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    UNIQUE (enrollment_id, node_id),
    CONSTRAINT chk_node_progress_status
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);
CREATE INDEX idx_node_progress_enrollment ON node_progress(enrollment_id);

-- Progression par LEÇON (vue / complétée). `node_id` dénormalisé pour agréger par nœud.
CREATE TABLE lesson_progress (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    enrollment_id uuid NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    node_id       uuid NOT NULL,              -- réf souple (dénormalisée)
    lesson_id     uuid NOT NULL,              -- réf souple -> contenu.lessons
    status        varchar(20) NOT NULL DEFAULT 'VIEWED',
    viewed_at     timestamptz,
    completed_at  timestamptz,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (enrollment_id, lesson_id),
    CONSTRAINT chk_lesson_progress_status
        CHECK (status IN ('VIEWED', 'COMPLETED'))
);
CREATE INDEX idx_lesson_progress_enrollment ON lesson_progress(enrollment_id);
CREATE INDEX idx_lesson_progress_node       ON lesson_progress(enrollment_id, node_id);

-- Favoris / marque-pages sur un nœud ou une leçon.
CREATE TABLE bookmarks (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id     uuid NOT NULL,                -- réf souple -> iam.users
    target_type varchar(20) NOT NULL,
    target_id   uuid NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    UNIQUE (user_id, target_type, target_id),
    CONSTRAINT chk_bookmarks_target CHECK (target_type IN ('NODE', 'LESSON'))
);
CREATE INDEX idx_bookmarks_user ON bookmarks(user_id);

-- Séries d'apprentissage (streaks) : un enregistrement par utilisateur.
CREATE TABLE streaks (
    id                 uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id            uuid NOT NULL UNIQUE,  -- réf souple -> iam.users
    current_length     int  NOT NULL DEFAULT 0,
    longest_length     int  NOT NULL DEFAULT 0,
    last_activity_date date,
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now()
);

-- Triggers updated_at
CREATE TRIGGER trg_enrollments_updated    BEFORE UPDATE ON enrollments    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_node_progress_updated  BEFORE UPDATE ON node_progress  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_lesson_progress_updated BEFORE UPDATE ON lesson_progress FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_streaks_updated        BEFORE UPDATE ON streaks        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
