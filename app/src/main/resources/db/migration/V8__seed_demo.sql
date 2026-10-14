-- =============================================================================
-- V8 — Seed : données de référence + démo
-- =============================================================================
-- Deux natures de données :
--   1) RÉFÉRENCE (structurelle, requise par l'app)  : rôles, paliers de niveau, badges.
--   2) DÉMO (explorable dès `docker compose up`)     : 1 admin, 1 apprenant,
--      1 mini-parcours "Backend" (phases, nœuds, arêtes, leçons, sections, quiz)
--      et une progression illustrant la REPRISE.
--
-- Idempotent : `ON CONFLICT DO NOTHING` partout. Les entités de démo utilisent des
-- UUID explicites (stables) pour câbler les références souples inter-contextes.
-- Comptes de démo — mot de passe : « Skillforge123! » (hash BCrypt ci-dessous).
-- =============================================================================

-- ---------- 1) RÉFÉRENCE : rôles ----------
INSERT INTO roles(code, label) VALUES
    ('LEARNER',    'Apprenant'),
    ('INSTRUCTOR', 'Instructeur'),
    ('ADMIN',      'Administrateur')
ON CONFLICT (code) DO NOTHING;

-- ---------- 1) RÉFÉRENCE : paliers de niveau (débutant -> professionnel) ----------
INSERT INTO levels(code, name, min_xp, position) VALUES
    ('BEGINNER',     'Débutant',       0,     1),
    ('APPRENTICE',   'Apprenti',       500,   2),
    ('PRACTITIONER', 'Praticien',      1500,  3),
    ('ADVANCED',     'Confirmé',       3500,  4),
    ('EXPERT',       'Expert',         7000,  5),
    ('PROFESSIONAL', 'Professionnel',  12000, 6)
ON CONFLICT (code) DO NOTHING;

-- ---------- 1) RÉFÉRENCE : badges (règles paramétrables) ----------
INSERT INTO badges(code, name, description, icon, rule_type, rule_config) VALUES
    ('NODE_MASTER',      'Nœud maîtrisé',      'Compléter un nœud entier.',              'target',   'NODE_COMPLETED',    '{}'::jsonb),
    ('ROADMAP_CHAMPION', 'Parcours accompli',  'Compléter un parcours complet.',         'trophy',   'ROADMAP_COMPLETED', '{}'::jsonb),
    ('CERTIFIED',        'Certifié blanc',     'Réussir un examen blanc.',               'medal',    'EXAM_PASSED',       '{}'::jsonb),
    ('STREAK_7',         'Assidu (7 jours)',   'Tenir une série de 7 jours.',            'flame',    'STREAK_DAYS',       '{"days": 7}'::jsonb),
    ('STREAK_30',        'Régulier (30 jours)','Tenir une série de 30 jours.',           'fire',     'STREAK_DAYS',       '{"days": 30}'::jsonb),
    ('XP_1000',          'Millier d''XP',      'Atteindre 1000 XP cumulés.',             'star',     'XP_THRESHOLD',      '{"xp": 1000}'::jsonb)
ON CONFLICT (code) DO NOTHING;

-- ---------- 2) DÉMO : utilisateurs ----------
INSERT INTO users(id, email, password_hash, display_name, theme_pref) VALUES
    ('01920000-0000-7000-8000-0000000000a1', 'admin@skillforge.dev',   '$2y$10$8uFoZE7Nbre6y9Jcwj6E4u2o0AecEiPDYwOo5Rnh3NSyw8fjs9FjO', 'Admin Démo',   'system'),
    ('01920000-0000-7000-8000-0000000000a2', 'learner@skillforge.dev', '$2y$10$8uFoZE7Nbre6y9Jcwj6E4u2o0AecEiPDYwOo5Rnh3NSyw8fjs9FjO', 'Léa Apprenti', 'dark')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles(user_id, role_id)
SELECT '01920000-0000-7000-8000-0000000000a1', id FROM roles WHERE code = 'ADMIN'
ON CONFLICT DO NOTHING;
INSERT INTO user_roles(user_id, role_id)
SELECT '01920000-0000-7000-8000-0000000000a2', id FROM roles WHERE code = 'LEARNER'
ON CONFLICT DO NOTHING;

-- ---------- 2) DÉMO : parcours "Backend Developer" ----------
INSERT INTO roadmaps(id, slug, title, summary, kind, position) VALUES
    ('01920000-0000-7000-8000-0000000000b1', 'backend', 'Backend Developer',
     'Parcours de démonstration : les fondations du développement backend.', 'TRACK', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO phases(id, roadmap_id, title, position) VALUES
    ('01920000-0000-7000-8000-0000000000c1', '01920000-0000-7000-8000-0000000000b1', 'Fondamentaux',        0),
    ('01920000-0000-7000-8000-0000000000c2', '01920000-0000-7000-8000-0000000000b1', 'Données & performance', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO nodes(id, roadmap_id, phase_id, slug, title, summary, importance, estimated_minutes, position) VALUES
    ('01920000-0000-7000-8000-0000000000d1', '01920000-0000-7000-8000-0000000000b1', '01920000-0000-7000-8000-0000000000c1', 'internet', 'Comment fonctionne Internet', 'HTTP, DNS, TCP/IP en bref.', 'CORE', 60, 0),
    ('01920000-0000-7000-8000-0000000000d2', '01920000-0000-7000-8000-0000000000b1', '01920000-0000-7000-8000-0000000000c1', 'git',      'Bases de Git',               'Versionner son code.',       'CORE', 90, 1),
    ('01920000-0000-7000-8000-0000000000d3', '01920000-0000-7000-8000-0000000000b1', '01920000-0000-7000-8000-0000000000c2', 'sql',      'SQL & relationnel',          'Modéliser et interroger.',   'CORE', 120, 0),
    ('01920000-0000-7000-8000-0000000000d4', '01920000-0000-7000-8000-0000000000b1', '01920000-0000-7000-8000-0000000000c2', 'caching',  'Caching',                    'Accélérer avec un cache.',   'RECOMMENDED', 75, 1)
ON CONFLICT (id) DO NOTHING;

-- Arêtes = prérequis INDICATIFS (non bloquants)
INSERT INTO edges(id, roadmap_id, source_node_id, target_node_id, kind) VALUES
    ('01920000-0000-7000-8000-0000000000e1', '01920000-0000-7000-8000-0000000000b1', '01920000-0000-7000-8000-0000000000d1', '01920000-0000-7000-8000-0000000000d2', 'PREREQ'),
    ('01920000-0000-7000-8000-0000000000e2', '01920000-0000-7000-8000-0000000000b1', '01920000-0000-7000-8000-0000000000d3', '01920000-0000-7000-8000-0000000000d4', 'PREREQ')
ON CONFLICT (id) DO NOTHING;

-- ---------- 2) DÉMO : contenu du nœud "caching" ----------
INSERT INTO lessons(id, node_id, slug, title, position) VALUES
    ('01920000-0000-7000-8000-0000000000f1', '01920000-0000-7000-8000-0000000000d4', 'intro',      'Introduction au caching', 0),
    ('01920000-0000-7000-8000-0000000000f2', '01920000-0000-7000-8000-0000000000d4', 'strategies', 'Stratégies de cache',     1)
ON CONFLICT (id) DO NOTHING;

-- Quiz référencé par la section QUIZ de la leçon "intro"
INSERT INTO quizzes(id, node_id, lesson_id, title, description) VALUES
    ('01920000-0000-7000-8000-000000000021', '01920000-0000-7000-8000-0000000000d4', '01920000-0000-7000-8000-0000000000f1',
     'Quiz : caching', 'Vérifie les notions de base du caching.')
ON CONFLICT (id) DO NOTHING;

INSERT INTO questions(id, quiz_id, type, prompt, justification, position) VALUES
    ('01920000-0000-7000-8000-000000000031', '01920000-0000-7000-8000-000000000021', 'MCQ',
     'Que signifie « TTL » pour une entrée de cache ?',
     'TTL = Time To Live : durée de validité avant expiration de l''entrée.', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO options(id, question_id, label, is_correct, position) VALUES
    ('01920000-0000-7000-8000-000000000041', '01920000-0000-7000-8000-000000000031', 'Time To Live (durée de vie)', true,  0),
    ('01920000-0000-7000-8000-000000000042', '01920000-0000-7000-8000-000000000031', 'Total Transfer Load',         false, 1)
ON CONFLICT (id) DO NOTHING;

-- Sections typées de la leçon "intro"
INSERT INTO sections(id, lesson_id, type, position, title, body, code_language, quiz_id) VALUES
    ('01920000-0000-7000-8000-000000000011', '01920000-0000-7000-8000-0000000000f1', 'CONCEPT', 0,
     'Qu''est-ce qu''un cache ?',
     'Un **cache** stocke temporairement des résultats coûteux à recalculer afin de servir les requêtes suivantes plus vite. Compromis clé : fraîcheur des données vs performance.',
     NULL, NULL),
    ('01920000-0000-7000-8000-000000000012', '01920000-0000-7000-8000-0000000000f1', 'CODE', 1,
     'Exemple : cache en mémoire (Java 21)',
     'var cache = new java.util.concurrent.ConcurrentHashMap<String, String>();\ncache.computeIfAbsent("key", k -> expensiveLookup(k));',
     'java', NULL),
    ('01920000-0000-7000-8000-000000000013', '01920000-0000-7000-8000-0000000000f1', 'QUIZ', 2,
     'Vérifie tes acquis', NULL, NULL,
     '01920000-0000-7000-8000-000000000021')
ON CONFLICT (id) DO NOTHING;

-- ---------- 2) DÉMO : progression de l'apprenant (illustre la REPRISE) ----------
-- Léa est inscrite à "Backend", active sur le nœud "caching", leçon "intro".
INSERT INTO enrollments(id, user_id, roadmap_id, progress_percent, last_active_node_id, last_active_lesson_id, last_activity_at) VALUES
    ('01920000-0000-7000-8000-000000000051', '01920000-0000-7000-8000-0000000000a2', '01920000-0000-7000-8000-0000000000b1',
     25, '01920000-0000-7000-8000-0000000000d4', '01920000-0000-7000-8000-0000000000f1', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO node_progress(id, enrollment_id, node_id, status, percent, last_lesson_id, last_viewed_at) VALUES
    ('01920000-0000-7000-8000-000000000061', '01920000-0000-7000-8000-000000000051', '01920000-0000-7000-8000-0000000000d4',
     'IN_PROGRESS', 50, '01920000-0000-7000-8000-0000000000f1', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO lesson_progress(id, enrollment_id, node_id, lesson_id, status, viewed_at) VALUES
    ('01920000-0000-7000-8000-000000000071', '01920000-0000-7000-8000-000000000051', '01920000-0000-7000-8000-0000000000d4',
     '01920000-0000-7000-8000-0000000000f1', 'VIEWED', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO streaks(id, user_id, current_length, longest_length, last_activity_date) VALUES
    ('01920000-0000-7000-8000-000000000081', '01920000-0000-7000-8000-0000000000a2', 3, 5, current_date)
ON CONFLICT (id) DO NOTHING;

INSERT INTO xp_ledger(id, user_id, amount, reason, source_type, source_id) VALUES
    ('01920000-0000-7000-8000-000000000091', '01920000-0000-7000-8000-0000000000a2', 50, 'LESSON_VIEWED', 'LESSON', '01920000-0000-7000-8000-0000000000f1')
ON CONFLICT (id) DO NOTHING;
