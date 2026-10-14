-- =============================================================================
-- V6 — Contexte Évaluation (quiz, défis de code, examens blancs chronométrés)
-- =============================================================================
-- FK dures INTRA-contexte (quiz -> question -> option, attempt -> answer, etc.).
-- Références SOUPLES (uuid sans FK) vers les autres contextes :
--   node_id / lesson_id      -> catalogue.nodes / contenu.lessons
--   certification_id         -> catalogue.certifications
--   exam_domain_id           -> catalogue.exam_domains
--   user_id                  -> iam.users
-- Les défis de code sont validés par COMPARAISON AUX POINTS CLÉS (pas d'exécution serveur).
-- =============================================================================

-- ---------- Quiz ----------
CREATE TABLE quizzes (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    node_id     uuid,                         -- réf souple -> catalogue.nodes
    lesson_id   uuid,                         -- réf souple -> contenu.lessons (optionnel)
    title       varchar(200) NOT NULL,
    description text,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_quizzes_node   ON quizzes(node_id);
CREATE INDEX idx_quizzes_lesson ON quizzes(lesson_id);

CREATE TABLE questions (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    quiz_id       uuid NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    type          varchar(20) NOT NULL,       -- MCQ / TRUE_FALSE / SCENARIO
    prompt        text        NOT NULL,
    justification text,
    position      int         NOT NULL DEFAULT 0,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (quiz_id, position),
    CONSTRAINT chk_questions_type CHECK (type IN ('MCQ', 'TRUE_FALSE', 'SCENARIO'))
);
CREATE INDEX idx_questions_quiz ON questions(quiz_id);

CREATE TABLE options (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    question_id uuid NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    label       text        NOT NULL,
    is_correct  boolean     NOT NULL DEFAULT false,
    position    int         NOT NULL DEFAULT 0,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    UNIQUE (question_id, position)
);
CREATE INDEX idx_options_question ON options(question_id);

-- ---------- Tentatives de quiz ----------
CREATE TABLE attempts (
    id           uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id      uuid NOT NULL,               -- réf souple -> iam.users
    quiz_id      uuid NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    score        numeric(6,2) NOT NULL DEFAULT 0,
    max_score    numeric(6,2) NOT NULL DEFAULT 0,
    started_at   timestamptz NOT NULL DEFAULT now(),
    submitted_at timestamptz,
    created_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_attempts_user ON attempts(user_id);
CREATE INDEX idx_attempts_quiz ON attempts(quiz_id);

CREATE TABLE attempt_answers (
    id                 uuid PRIMARY KEY DEFAULT uuidv7(),
    attempt_id         uuid NOT NULL REFERENCES attempts(id) ON DELETE CASCADE,
    question_id        uuid NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    selected_option_id uuid REFERENCES options(id) ON DELETE SET NULL,
    is_correct         boolean,
    created_at         timestamptz NOT NULL DEFAULT now(),
    UNIQUE (attempt_id, question_id)
);
CREATE INDEX idx_attempt_answers_attempt ON attempt_answers(attempt_id);

-- ---------- Défis de code ----------
CREATE TABLE code_challenges (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    node_id     uuid,                         -- réf souple -> catalogue.nodes
    lesson_id   uuid,                         -- réf souple -> contenu.lessons (optionnel)
    title       varchar(200) NOT NULL,
    statement   text        NOT NULL,
    skeleton    text,
    solution    text        NOT NULL,
    key_points  jsonb       NOT NULL DEFAULT '[]'::jsonb,  -- points clés attendus pour la validation
    language    varchar(40),
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_code_challenges_node ON code_challenges(node_id);

-- ---------- Examens blancs chronométrés ----------
CREATE TABLE mock_exams (
    id               uuid PRIMARY KEY DEFAULT uuidv7(),
    certification_id uuid NOT NULL,           -- réf souple -> catalogue.certifications
    title            varchar(200) NOT NULL,
    format           varchar(20)  NOT NULL,   -- MCQ / PRACTICAL
    duration_minutes int          NOT NULL,
    passing_score    numeric(5,2),
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_mock_exams_format CHECK (format IN ('MCQ', 'PRACTICAL'))
);
CREATE INDEX idx_mock_exams_cert ON mock_exams(certification_id);

-- Item d'examen : soit une question QCM (réutilise `questions`), soit une TÂCHE pratique.
CREATE TABLE exam_questions (
    id             uuid PRIMARY KEY DEFAULT uuidv7(),
    mock_exam_id   uuid NOT NULL REFERENCES mock_exams(id) ON DELETE CASCADE,
    exam_domain_id uuid NOT NULL,             -- réf souple -> catalogue.exam_domains
    kind           varchar(20) NOT NULL,      -- MCQ / TASK
    question_id    uuid,                       -- réf souple -> questions.id (si kind=MCQ)
    prompt         text,                       -- énoncé de la tâche (si kind=TASK)
    task_solution  text,                       -- solution attendue (si kind=TASK)
    points         numeric(6,2) NOT NULL DEFAULT 1,
    position       int          NOT NULL DEFAULT 0,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    UNIQUE (mock_exam_id, position),
    CONSTRAINT chk_exam_questions_kind CHECK (kind IN ('MCQ', 'TASK'))
);
CREATE INDEX idx_exam_questions_exam   ON exam_questions(mock_exam_id);
CREATE INDEX idx_exam_questions_domain ON exam_questions(exam_domain_id);

CREATE TABLE exam_attempts (
    id              uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id         uuid NOT NULL,            -- réf souple -> iam.users
    mock_exam_id    uuid NOT NULL REFERENCES mock_exams(id) ON DELETE CASCADE,
    status          varchar(20) NOT NULL DEFAULT 'IN_PROGRESS',  -- IN_PROGRESS / SUBMITTED / EXPIRED
    started_at      timestamptz NOT NULL DEFAULT now(),
    deadline_at     timestamptz,              -- started_at + durée (minuteur)
    submitted_at    timestamptz,
    total_score     numeric(6,2) NOT NULL DEFAULT 0,
    max_score       numeric(6,2) NOT NULL DEFAULT 0,
    passed          boolean,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_exam_attempts_status CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'EXPIRED'))
);
CREATE INDEX idx_exam_attempts_user ON exam_attempts(user_id);
CREATE INDEX idx_exam_attempts_exam ON exam_attempts(mock_exam_id);

-- Score PAR DOMAINE d'examen + détection des points faibles.
CREATE TABLE exam_domain_results (
    id              uuid PRIMARY KEY DEFAULT uuidv7(),
    exam_attempt_id uuid NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
    exam_domain_id  uuid NOT NULL,            -- réf souple -> catalogue.exam_domains
    score           numeric(6,2) NOT NULL DEFAULT 0,
    max_score       numeric(6,2) NOT NULL DEFAULT 0,
    is_weak         boolean      NOT NULL DEFAULT false,  -- point faible (pour révision ciblée)
    created_at      timestamptz NOT NULL DEFAULT now(),
    UNIQUE (exam_attempt_id, exam_domain_id)
);
CREATE INDEX idx_exam_domain_results_attempt ON exam_domain_results(exam_attempt_id);
CREATE INDEX idx_exam_domain_results_domain  ON exam_domain_results(exam_domain_id);

-- Triggers updated_at
CREATE TRIGGER trg_quizzes_updated         BEFORE UPDATE ON quizzes         FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_questions_updated       BEFORE UPDATE ON questions       FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_options_updated         BEFORE UPDATE ON options         FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_code_challenges_updated BEFORE UPDATE ON code_challenges FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_mock_exams_updated      BEFORE UPDATE ON mock_exams      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_exam_questions_updated  BEFORE UPDATE ON exam_questions  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_exam_attempts_updated   BEFORE UPDATE ON exam_attempts   FOR EACH ROW EXECUTE FUNCTION set_updated_at();
