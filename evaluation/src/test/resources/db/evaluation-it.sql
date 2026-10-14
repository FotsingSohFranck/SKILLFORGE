-- Schéma minimal du contexte Évaluation (quiz) pour les tests d'intégration.
-- Sous-ensemble autonome de V6__evaluation.sql (partie quiz).

CREATE TABLE quizzes (
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    node_id     uuid,
    lesson_id   uuid,
    title       varchar(200) NOT NULL,
    description text,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE questions (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    quiz_id       uuid NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    type          varchar(20) NOT NULL,
    prompt        text        NOT NULL,
    justification text,
    position      int         NOT NULL DEFAULT 0,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (quiz_id, position)
);

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

CREATE TABLE attempts (
    id           uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id      uuid NOT NULL,
    quiz_id      uuid NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    score        numeric(6,2) NOT NULL DEFAULT 0,
    max_score    numeric(6,2) NOT NULL DEFAULT 0,
    started_at   timestamptz NOT NULL DEFAULT now(),
    submitted_at timestamptz,
    created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE attempt_answers (
    id                 uuid PRIMARY KEY DEFAULT uuidv7(),
    attempt_id         uuid NOT NULL REFERENCES attempts(id) ON DELETE CASCADE,
    question_id        uuid NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    selected_option_id uuid REFERENCES options(id) ON DELETE SET NULL,
    is_correct         boolean,
    created_at         timestamptz NOT NULL DEFAULT now(),
    UNIQUE (attempt_id, question_id)
);

CREATE TABLE code_challenges (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    node_id    uuid,
    lesson_id  uuid,
    title      varchar(200) NOT NULL,
    statement  text        NOT NULL,
    skeleton   text,
    solution   text        NOT NULL,
    key_points jsonb       NOT NULL DEFAULT '[]'::jsonb,
    language   varchar(40),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE mock_exams (
    id               uuid PRIMARY KEY DEFAULT uuidv7(),
    certification_id uuid NOT NULL,
    title            varchar(200) NOT NULL,
    format           varchar(20)  NOT NULL,
    duration_minutes int          NOT NULL,
    passing_score    numeric(5,2),
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE exam_questions (
    id             uuid PRIMARY KEY DEFAULT uuidv7(),
    mock_exam_id   uuid NOT NULL REFERENCES mock_exams(id) ON DELETE CASCADE,
    exam_domain_id uuid NOT NULL,
    kind           varchar(20) NOT NULL,
    question_id    uuid,
    prompt         text,
    task_solution  text,
    points         numeric(6,2) NOT NULL DEFAULT 1,
    position       int          NOT NULL DEFAULT 0,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    UNIQUE (mock_exam_id, position)
);

CREATE TABLE exam_attempts (
    id           uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id      uuid NOT NULL,
    mock_exam_id uuid NOT NULL REFERENCES mock_exams(id) ON DELETE CASCADE,
    status       varchar(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at   timestamptz NOT NULL DEFAULT now(),
    deadline_at  timestamptz,
    submitted_at timestamptz,
    total_score  numeric(6,2) NOT NULL DEFAULT 0,
    max_score    numeric(6,2) NOT NULL DEFAULT 0,
    passed       boolean,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE exam_domain_results (
    id              uuid PRIMARY KEY DEFAULT uuidv7(),
    exam_attempt_id uuid NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
    exam_domain_id  uuid NOT NULL,
    score           numeric(6,2) NOT NULL DEFAULT 0,
    max_score       numeric(6,2) NOT NULL DEFAULT 0,
    is_weak         boolean NOT NULL DEFAULT false,
    created_at      timestamptz NOT NULL DEFAULT now(),
    UNIQUE (exam_attempt_id, exam_domain_id)
);
