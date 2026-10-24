-- Lie l'épreuve pratique à la progression (ADR-0008 / SF-91d).
-- Mémorise, par nœud et par inscription, si l'épreuve pratique a été réussie.
-- Rétrocompatible : défaut false (n'affecte que les nœuds en politique LESSONS_AND_PRACTICAL).

ALTER TABLE node_progress
    ADD COLUMN practical_passed boolean NOT NULL DEFAULT false;
