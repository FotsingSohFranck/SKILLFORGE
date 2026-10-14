-- =============================================================================
-- V7 — Contexte Gamification (XP, niveaux, badges)
-- =============================================================================
-- FK dures INTRA-contexte (earned_badges -> badges). Références SOUPLES vers iam :
--   user_id -> iam.users
-- L'XP totale d'un utilisateur = SUM(xp_ledger.amount) ; le niveau se déduit des
-- seuils de `levels`. Barème PARAMÉTRABLE (données de référence chargées au seed 10c).
-- =============================================================================

-- Paliers de niveau nommés (débutant -> professionnel). Barème paramétrable.
CREATE TABLE levels (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    code       varchar(40)  NOT NULL UNIQUE,   -- ex. BEGINNER, NOVICE, ..., PROFESSIONAL
    name       varchar(80)  NOT NULL,          -- libellé affiché (FR)
    min_xp     int          NOT NULL,          -- seuil d'XP pour atteindre ce palier
    position   int          NOT NULL,          -- ordre du palier
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT chk_levels_min_xp CHECK (min_xp >= 0),
    UNIQUE (position)
);

-- Grand livre d'XP (append-only) : chaque gain est une ligne, traçable à sa source.
CREATE TABLE xp_ledger (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id     uuid NOT NULL,                 -- réf souple -> iam.users
    amount      int  NOT NULL,                 -- XP gagnée (peut être négative pour correction)
    reason      varchar(80) NOT NULL,          -- ex. LESSON_COMPLETED, NODE_COMPLETED, EXAM_PASSED
    source_type varchar(40),                   -- LESSON / NODE / ROADMAP / EXAM / BADGE / OTHER
    source_id   uuid,                          -- réf souple vers l'entité source
    created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_xp_ledger_user ON xp_ledger(user_id);
CREATE INDEX idx_xp_ledger_user_created ON xp_ledger(user_id, created_at DESC);

-- Définitions de badges + règle d'obtention paramétrable.
CREATE TABLE badges (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    code        varchar(60)  NOT NULL UNIQUE,
    name        varchar(120) NOT NULL,
    description text,
    icon        varchar(80),
    rule_type   varchar(40)  NOT NULL,         -- NODE_COMPLETED / ROADMAP_COMPLETED / EXAM_PASSED / STREAK_DAYS / XP_THRESHOLD
    rule_config jsonb        NOT NULL DEFAULT '{}'::jsonb,  -- paramètres (ex. {"days":7} ou {"roadmapSlug":"backend"})
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT chk_badges_rule_type CHECK (
        rule_type IN ('NODE_COMPLETED', 'ROADMAP_COMPLETED', 'EXAM_PASSED', 'STREAK_DAYS', 'XP_THRESHOLD')
    )
);

-- Badges obtenus par un utilisateur (un badge donné une seule fois par utilisateur).
CREATE TABLE earned_badges (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id    uuid NOT NULL,                  -- réf souple -> iam.users
    badge_id   uuid NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
    earned_at  timestamptz NOT NULL DEFAULT now(),
    created_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (user_id, badge_id)
);
CREATE INDEX idx_earned_badges_user  ON earned_badges(user_id);
CREATE INDEX idx_earned_badges_badge ON earned_badges(badge_id);

-- Triggers updated_at
CREATE TRIGGER trg_levels_updated BEFORE UPDATE ON levels FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_badges_updated BEFORE UPDATE ON badges FOR EACH ROW EXECUTE FUNCTION set_updated_at();
