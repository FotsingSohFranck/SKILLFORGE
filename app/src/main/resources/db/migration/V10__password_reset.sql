-- Jetons de réinitialisation de mot de passe (flux "mot de passe oublié").
-- Le jeton brut (haute entropie) n'est jamais stocké : seule son empreinte SHA-256 l'est.
-- À usage unique (used_at) et à durée limitée (expires_at).
CREATE TABLE password_reset_tokens (
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id    uuid NOT NULL,                 -- réf souple -> iam.users
    token_hash varchar(64) NOT NULL UNIQUE,   -- SHA-256 hex du jeton brut
    expires_at timestamptz NOT NULL,
    used_at    timestamptz,                   -- NULL tant que non consommé
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
