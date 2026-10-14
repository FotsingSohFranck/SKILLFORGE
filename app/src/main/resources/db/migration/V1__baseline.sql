-- =============================================================================
-- V1 — Baseline partagé
-- =============================================================================
-- PostgreSQL 18 : la fonction uuidv7() est NATIVE (aucune extension requise).
-- Toutes les clés primaires utilisent `uuid DEFAULT uuidv7()` (ID généré en base).
-- Voir docs/adr/0003-uuid-v7-en-base.md et docs/DATA-MODEL.md.
--
-- Fonction trigger réutilisable pour maintenir automatiquement `updated_at`.
-- Elle est attachée aux tables portant un champ `updated_at` dans les migrations
-- suivantes (BEFORE UPDATE).
-- =============================================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
