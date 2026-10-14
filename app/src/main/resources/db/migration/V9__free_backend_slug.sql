-- =============================================================================
-- V9 — Libère le slug « backend » pour le contenu authoré (pipeline SF-40).
-- =============================================================================
-- Le seed de démonstration (V8) crée un parcours placeholder de slug « backend ».
-- Le contenu réel « Développeur Backend » est désormais authoré et ingéré via le
-- pipeline (content/backend). On renomme donc le parcours de démonstration pour
-- qu'il n'entre pas en collision (le slug est unique) tout en restant explorable.
--
-- On ne cible que le placeholder d'origine (id du seed) : si le contenu authoré a
-- déjà été ingéré sous « backend », cette migration est sans effet.
-- =============================================================================

UPDATE roadmaps
   SET slug  = 'decouverte',
       title = 'Découverte du backend'
 WHERE id = '01920000-0000-7000-8000-0000000000b1'
   AND slug = 'backend';
