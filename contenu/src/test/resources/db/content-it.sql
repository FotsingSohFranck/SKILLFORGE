-- Schéma minimal du contexte Contenu + jeu de données, pour le test d'intégration.
-- Sous-ensemble autonome de V4__contenu.sql.

CREATE TABLE lessons (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    node_id    uuid NOT NULL,
    slug       varchar(120) NOT NULL,
    title      varchar(200) NOT NULL,
    position   int          NOT NULL DEFAULT 0,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (node_id, slug)
);

CREATE TABLE sections (
    id             uuid PRIMARY KEY DEFAULT uuidv7(),
    lesson_id      uuid NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    type           varchar(20) NOT NULL,
    position       int         NOT NULL DEFAULT 0,
    title          varchar(200),
    body           text,
    code_language  varchar(40),
    diagram_format varchar(20),
    solution       text,
    quiz_id        uuid,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    UNIQUE (lesson_id, position)
);

-- Jeu de données : nœud …b1 avec 1 leçon "intro" (…c1) à 3 sections.
INSERT INTO lessons(id, node_id, slug, title, position) VALUES
    ('019b0000-0000-7000-8000-0000000000c1'::uuid, '019b0000-0000-7000-8000-0000000000b1'::uuid, 'intro', 'Introduction', 0);

INSERT INTO sections(id, lesson_id, type, position, title, body, code_language, quiz_id) VALUES
    ('019b0000-0000-7000-8000-000000000011'::uuid, '019b0000-0000-7000-8000-0000000000c1'::uuid, 'CONCEPT', 0, 'Notion', 'Un cache stocke des résultats.', NULL, NULL),
    ('019b0000-0000-7000-8000-000000000012'::uuid, '019b0000-0000-7000-8000-0000000000c1'::uuid, 'CODE', 1, 'Exemple', 'var m = new HashMap();', 'java', NULL),
    ('019b0000-0000-7000-8000-000000000013'::uuid, '019b0000-0000-7000-8000-0000000000c1'::uuid, 'QUIZ', 2, 'Quiz', NULL, NULL, '019b0000-0000-7000-8000-000000000021'::uuid);
