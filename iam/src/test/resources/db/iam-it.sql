-- Schéma minimal du contexte IAM + rôles de référence, pour le test d'intégration
-- (exécuté par Testcontainers via JDBC). Sous-ensemble autonome de V2__iam.sql.

CREATE TABLE roles (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    code       varchar(32)  NOT NULL UNIQUE,
    label      varchar(64)  NOT NULL,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    email         varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    display_name  varchar(120) NOT NULL,
    avatar_url    varchar(1024),
    theme_pref    varchar(16)  NOT NULL DEFAULT 'system',
    enabled       boolean      NOT NULL DEFAULT true,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id    uuid NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash varchar(255) NOT NULL UNIQUE,
    expires_at timestamptz  NOT NULL,
    revoked    boolean      NOT NULL DEFAULT false,
    created_at timestamptz  NOT NULL DEFAULT now()
);

-- Rôles de référence requis par l'inscription.
INSERT INTO roles(code, label) VALUES
    ('LEARNER', 'Apprenant'),
    ('INSTRUCTOR', 'Instructeur'),
    ('ADMIN', 'Administrateur');

-- Compte ADMIN de test (mot de passe : Skillforge123! — hash BCrypt).
INSERT INTO users(id, email, password_hash, display_name) VALUES
    ('01940000-0000-7000-8000-0000000000a1', 'admin@it.dev',
     '$2y$10$8uFoZE7Nbre6y9Jcwj6E4u2o0AecEiPDYwOo5Rnh3NSyw8fjs9FjO', 'Admin IT');
INSERT INTO user_roles(user_id, role_id)
    SELECT '01940000-0000-7000-8000-0000000000a1', id FROM roles WHERE code = 'ADMIN';
