-- Ajoute le type de section IMAGE (illustrations pédagogiques dans les leçons).
-- On remplace la contrainte CHECK pour inclure 'IMAGE' (V4 ne doit pas être éditée).

ALTER TABLE sections DROP CONSTRAINT chk_sections_type;

ALTER TABLE sections
    ADD CONSTRAINT chk_sections_type
        CHECK (type IN ('CONCEPT', 'DIAGRAM', 'CODE', 'EXERCISE', 'QUIZ', 'IMAGE'));
