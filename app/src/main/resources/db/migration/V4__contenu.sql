-- =============================================================================
-- V4 — Contexte Contenu (leçons structurées & sections typées)
-- =============================================================================
-- `node_id` référence catalogue.nodes : référence SOUPLE (uuid sans FK) car
-- inter-contexte (cf. docs/DATA-MODEL.md, convention de frontière). Idem
-- `quiz_id` vers evaluation.quizzes (créée en V6).
-- =============================================================================

CREATE TABLE lessons (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    node_id    uuid NOT NULL,                 -- réf souple -> catalogue.nodes
    slug       varchar(120) NOT NULL,
    title      varchar(200) NOT NULL,
    position   int          NOT NULL DEFAULT 0,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (node_id, slug)
);
CREATE INDEX idx_lessons_node ON lessons(node_id);

-- Sections ordonnées et typées d'une leçon.
-- Colonnes spécialisées selon `type` :
--   CONCEPT  -> body (markdown)
--   DIAGRAM  -> body (source), diagram_format (SVG | MERMAID)
--   CODE     -> body (snippet), code_language (java|sql|yaml|bash|rust|...)
--   EXERCISE -> body (énoncé), solution
--   QUIZ     -> quiz_id (réf souple -> evaluation.quizzes)
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
    quiz_id        uuid,                       -- réf souple -> evaluation.quizzes
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    UNIQUE (lesson_id, position),
    CONSTRAINT chk_sections_type
        CHECK (type IN ('CONCEPT', 'DIAGRAM', 'CODE', 'EXERCISE', 'QUIZ')),
    CONSTRAINT chk_sections_diagram_format
        CHECK (diagram_format IS NULL OR diagram_format IN ('SVG', 'MERMAID'))
);
CREATE INDEX idx_sections_lesson ON sections(lesson_id);

-- Triggers updated_at
CREATE TRIGGER trg_lessons_updated  BEFORE UPDATE ON lessons  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_sections_updated BEFORE UPDATE ON sections FOR EACH ROW EXECUTE FUNCTION set_updated_at();
