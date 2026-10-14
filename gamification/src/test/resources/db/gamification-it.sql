-- Schéma minimal du contexte Gamification (XP + niveaux) pour les tests d'intégration.

CREATE TABLE levels (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    code       varchar(40)  NOT NULL UNIQUE,
    name       varchar(80)  NOT NULL,
    min_xp     int          NOT NULL,
    position   int          NOT NULL UNIQUE,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE xp_ledger (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id     uuid NOT NULL,
    amount      int  NOT NULL,
    reason      varchar(80) NOT NULL,
    source_type varchar(40),
    source_id   uuid,
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE badges (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    code        varchar(60)  NOT NULL UNIQUE,
    name        varchar(120) NOT NULL,
    description text,
    icon        varchar(80),
    rule_type   varchar(40)  NOT NULL,
    rule_config jsonb        NOT NULL DEFAULT '{}'::jsonb,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE earned_badges (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id    uuid NOT NULL,
    badge_id   uuid NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
    earned_at  timestamptz NOT NULL DEFAULT now(),
    created_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (user_id, badge_id)
);

-- Barème de niveaux (comme le seed V8).
INSERT INTO levels(code, name, min_xp, position) VALUES
    ('BEGINNER',     'Débutant',       0,     1),
    ('APPRENTICE',   'Apprenti',       500,   2),
    ('PRACTITIONER', 'Praticien',      1500,  3),
    ('ADVANCED',     'Confirmé',       3500,  4),
    ('EXPERT',       'Expert',         7000,  5),
    ('PROFESSIONAL', 'Professionnel',  12000, 6);

-- Badges de référence (comme le seed V8).
INSERT INTO badges(code, name, rule_type, rule_config) VALUES
    ('NODE_MASTER', 'Nœud maîtrisé',   'NODE_COMPLETED', '{}'::jsonb),
    ('STREAK_7',    'Assidu (7 jours)','STREAK_DAYS',    '{"days": 7}'::jsonb),
    ('XP_1000',     'Millier d''XP',   'XP_THRESHOLD',   '{"xp": 1000}'::jsonb);
