-- =============================================================================
-- V2 — Contexte IAM (identité, rôles, authentification)
-- =============================================================================

-- Rôles applicatifs : LEARNER / INSTRUCTOR / ADMIN
CREATE TABLE roles (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    code       varchar(32)  NOT NULL UNIQUE,
    label      varchar(64)  NOT NULL,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT chk_roles_code CHECK (code IN ('LEARNER', 'INSTRUCTOR', 'ADMIN'))
);

-- Comptes utilisateurs. `email` est stocké en minuscules par l'application ;
-- l'unicité est garantie ici. Le mot de passe n'est jamais stocké en clair.
CREATE TABLE users (
    id            uuid PRIMARY KEY DEFAULT uuidv7(),
    email         varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    display_name  varchar(120) NOT NULL,
    avatar_url    varchar(1024),
    theme_pref    varchar(16)  NOT NULL DEFAULT 'system',
    enabled       boolean      NOT NULL DEFAULT true,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_theme CHECK (theme_pref IN ('light', 'dark', 'system'))
);

-- Association many-to-many users <-> roles (intra-contexte : FK dures).
CREATE TABLE user_roles (
    user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id    uuid NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- Refresh tokens JWT (rotation). On stocke un HASH du token, jamais le token brut.
CREATE TABLE refresh_tokens (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash varchar(255) NOT NULL UNIQUE,
    expires_at timestamptz  NOT NULL,
    revoked    boolean      NOT NULL DEFAULT false,
    created_at timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- Triggers updated_at
CREATE TRIGGER trg_roles_updated BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
