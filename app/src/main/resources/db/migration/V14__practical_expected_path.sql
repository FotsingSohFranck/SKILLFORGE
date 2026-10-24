-- Lab (SF-91g, correctifs UX) : chemin de fichier attendu pour la soumission d'une épreuve.
-- Permet à l'UI de pré-remplir le bon chemin (ex. src/main/java/forge/Solution.java) au lieu
-- d'un défaut trompeur, évitant les échecs dus à un mauvais emplacement de fichier.
ALTER TABLE practical_tasks
    ADD COLUMN expected_path varchar(300);
