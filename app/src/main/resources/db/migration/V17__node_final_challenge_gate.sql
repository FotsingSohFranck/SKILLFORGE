-- Lie le Final Challenge à la progression (SF-92a-2-2). Indépendant de completion_policy, comme
-- l'épreuve pratique : un nœud peut exiger (ou non) la réussite de son Final Challenge, et on
-- mémorise, par inscription, si ce challenge a été réussi.
-- Rétrocompatible : défauts false (aucun nœud existant n'est affecté).

ALTER TABLE nodes
    ADD COLUMN requires_challenge boolean NOT NULL DEFAULT false;

ALTER TABLE node_progress
    ADD COLUMN challenge_passed boolean NOT NULL DEFAULT false;
