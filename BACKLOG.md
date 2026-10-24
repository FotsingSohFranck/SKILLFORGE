# BACKLOG — SkillForge

> **Comment lire ce backlog.** On travaille en **flux continu, WIP = 1** (cf. `CLAUDE.md` §4).
> On prend **toujours l'item non commencé le plus haut** dans l'ordre ci-dessous — pas de
> pioche au hasard. Les « sprints » = les **lots** ci-dessous. En fin de lot : courte revue.
>
> **Légende statut :** ✅ fait · ▶️ prochain à prendre · ⬜ à faire · 🧊 hors MVP.
> Chaque story porte un **ID stable** (`SF-xx`). Les critères d'acceptation (CA) définissent
> le « terminé » fonctionnel ; la Definition of Done technique reste celle de `CLAUDE.md` §5.
>
> **Ancre :** `docs/SPECIFICATION.md` (métier) + `docs/ARCHITECTURE.md` (technique) priment.

---

## Résumé des lots

| Lot | Thème | Prompts source | Statut |
|---|---|---|---|
| **Lot 0** | Cadrage & architecture | P0, P1 | ✅ terminé |
| **Lot 1** | Socle backend (données, squelette, IAM) | P2–P4 | ✅ terminé |
| **Lot 2** | Contextes métier | P5–P9 | ✅ terminé |
| **Lot 3** | Frontend React | P10–P13 | ⬜ en cours |
| **Lot 4** | Contenu, déploiement & qualité | P14–P16 | ⬜ à faire |

---

## Lot 0 — Cadrage & architecture ✅

- ✅ **SF-01 — Spécification produit** (`docs/SPECIFICATION.md`)
  - CA : vision, personas, fonctionnalités exhaustives, modèle conceptuel, 7 contextes
    bornés justifiés, user journeys, attributs qualité, périmètre MVP. **Fait.**
- ✅ **SF-02 — Architecture technique + ADR** (`docs/ARCHITECTURE.md`, `docs/adr/0001..0005`)
  - CA : schéma Mermaid des contextes/dépendances, patron hexagonal de référence
    (`apprentissage`), conventions transverses, modules Maven, 5 ADR. **Fait.**
- ✅ **SF-03 — Backlog priorisé** (`BACKLOG.md`)
  - CA : epics → stories ordonnées avec critères d'acceptation. **Fait (ce document).**

**Revue de lot :** les deux documents ancres et le backlog sont posés. Décisions figées :
7 contextes, réactif de bout en bout, Maven multi-modules, UUID v7 en base, JWT stateless,
Flyway. Dette : nom « SkillForge » à confirmer ; choix module `-api` partagé vs ports côté
consommateur laissé ouvert (tranché par défaut = ports côté consommateur).

---

## Lot 1 — Socle backend (P2–P4)

> Objectif : une base démarrable, un squelette multi-module qui compile, l'authentification.

- **SF-10 — Modèle de données & migrations Flyway** (P2) — *découpé en 10a/10b/10c*
  - Cadre commun (tous sous-items) : PK en `uuid DEFAULT uuidv7()` ; audit `created_at`/
    `updated_at` (trigger) ; contraintes d'intégrité + index (FK + colonnes de tri/filtre) ;
    **FK dures intra-contexte, références souples (uuid sans FK) inter-contextes** ;
    migrations versionnées jamais rééditées ; erDiagram Mermaid + notes UUID v7 documentés
    dans `docs/DATA-MODEL.md`.

  - ✅ **SF-10a — Migrations cœur (iam + catalogue + contenu + apprentissage)**
    - CA :
      - [x] `V1` baseline (fonction trigger `set_updated_at`, note uuidv7 natif PG18).
      - [x] `V2` iam : `roles`, `users`, `user_roles`, `refresh_tokens`.
      - [x] `V3` catalogue : `roadmaps`, `certifications`, `exam_domains`, `phases`, `nodes`,
            `edges` (prérequis non bloquants), `node_exam_domains`.
      - [x] `V4` contenu : `lessons`, `sections` (typées CONCEPT/DIAGRAM/CODE/EXERCISE/QUIZ ;
            `quiz_id` en réf souple vers evaluation).
      - [x] `V5` apprentissage : `enrollments` (reprise globale), `node_progress` (reprise
            exacte par nœud), `lesson_progress`, `bookmarks`, `streaks`.
      - [x] Progression modélisée pour navigation non linéaire + reprise (dernière leçon/nœud).
      - [x] `docs/DATA-MODEL.md` : erDiagram du périmètre 10a + notes UUID v7 + convention
            réf. inter-contextes.
    - Vérifié sur PostgreSQL 18.4 (18 tables, scénario de reprise, trigger updated_at, CHECK).

  - ✅ **SF-10b — Migrations évaluation + gamification**
    - CA :
      - [x] `V6` evaluation : `quizzes`, `questions`, `options`, `attempts`, `attempt_answers`,
            `code_challenges`, `mock_exams`, `exam_questions`, `exam_attempts`,
            `exam_domain_results`.
      - [x] `V7` gamification : `xp_ledger`, `levels`, `badges`, `earned_badges`.
      - [x] Scoring par domaine modélisé ; `docs/DATA-MODEL.md` étendu.
    - Vérifié sur PostgreSQL 18.4 (32 tables, scoring par domaine, somme XP, règle badge JSONB).

  - ✅ **SF-10c — Seed de démo**
    - CA :
      - [x] `V8` seed : données de référence (rôles, niveaux, badges) + 1 admin, 1 apprenant,
            1 parcours "Backend" (roadmap + phases + nœuds + arêtes + leçons/sections + quiz).
      - [x] Cohérent avec le schéma 10a/10b ; **idempotent** (`ON CONFLICT DO NOTHING`).
    - Vérifié sur PostgreSQL 18.4 : reprise reconstituée (Backend→Caching→Introduction, 50 %),
      lien section→quiz OK, re-run sans doublon. Comptes démo : `admin@skillforge.dev` /
      `learner@skillforge.dev`, mot de passe `Skillforge123!`.

- **SF-11 — Squelette backend multi-module (WebFlux, R2DBC)** (P3) — *découpé en 11a/11b/11c*

  - ✅ **SF-11a — Socle build & runtime**
    - CA :
      - [x] `mvn clean install` réussit (9 modules : parent, common, 7 contextes, app).
      - [x] `app` démarre en profil local (WebFlux) et **applique Flyway V1→V8 au boot**.
      - [x] `common` : enveloppe `data/meta/error`, erreurs de base, handler global réactif, UUID v7.
      - [x] 7 contextes = modules + `package-info` hexagonal avec TODO (pas de code métier).
      - [x] `application.yml` : R2DBC runtime + Flyway JDBC, profils local/docker, OpenAPI, health.
      - [x] Sécurité `permitAll` temporaire (TODO SF-12).
    - Vérifié : build OK ; app démarrée (PostgreSQL 18.4), 8 migrations `success=true`, 32 tables ;
      `/actuator/health` UP, `/v3/api-docs` (titre "SkillForge API"), `/swagger-ui.html` 302,
      enveloppe d'erreur normalisée sur route inconnue.
    - Notes : en Boot 4 l'auto-config Flyway est modularisée → dépendance `spring-boot-starter-flyway`
      (+ exclusion de `DataSourceAutoConfiguration`, Flyway utilise sa propre datasource JDBC).

  - ✅ **SF-11b — Contexte `catalogue` de bout en bout (lecture)**
    - CA :
      - [x] `domain` (Roadmap, Phase, Node, Edge, RoadmapGraph + VO/identifiants, enums, ports in/out).
      - [x] `application` (ListRoadmaps, SearchRoadmaps, GetRoadmapGraph, GetNode) ; erreurs de domaine.
      - [x] `adapter.out.persistence` (entités R2DBC records, repositories, adapters, mapper).
      - [x] `adapter.in.web` (CatalogueController, DTOs, mapper) : `GET /roadmaps`, `/roadmaps/{slug}`,
            `/nodes/{id}`, `?q=` ; câblage via `config/CatalogueConfig` (services purs).
      - [x] 5 tests unitaires (application/domaine) verts.
    - Vérifié bout-en-bout (PostgreSQL 18 seedé) : liste = "Backend Developer" ; graphe = 2 phases,
      4 nœuds, 2 arêtes ; nœud "caching" par id ; recherche `?q=back` ; 404 normalisé (ROADMAP/NODE_NOT_FOUND).

  - ✅ **SF-11c — Test d'intégration `catalogue` (Testcontainers)**
    - CA :
      - [x] Test d'intégration flux WebFlux → use case → adapter R2DBC contre PostgreSQL 18
            (Testcontainers) : mini-app `CatalogueITApplication` + `WebTestClient` + schéma/seed dédié.
      - [x] `mvn test` démarre le conteneur, 4 tests d'intégration passent (liste, graphe, nœud, 404).
    - Vérifié : `mvn clean install` complet vert (9 tests catalogue : 5 unitaires + 4 intégration).
    - Notes : Boot 4 modularise les autoconfig test (package `AutoConfigureWebTestClient` déplacé) →
      port aléatoire injecté via `local.server.port`. Testcontainers 2.x → artefacts `testcontainers-*`,
      BOM importé dans le POM parent.

  - **Bilan SF-11 :** ✅ socle multi-module démarrable + contexte `catalogue` (référence hexagonale)
    de bout en bout, testé unitairement et en intégration. Patron réutilisable pour les 6 autres contextes.

- **SF-12 — Contexte IAM : utilisateurs, rôles & authentification** (P4) — *découpé en 12a/12b/12c*
  - **Décisions figées :** hachage **BCrypt** (cohérent avec le seed 10c) ; JWT via **Spring
    Security oauth2-jose (Nimbus), HS256** (secret par variable d'env) ; **access 60 min /
    refresh 30 j** avec **rotation** des refresh tokens (stockés hachés en `refresh_tokens`) ;
    access **stateless**. Remplace la sécurité permissive temporaire de SF-11a.

  - ✅ **SF-12a — Fondations IAM (domaine + persistance + hachage)**
    - CA :
      - [x] `domain` : agrégat `User`, VO `Email`/`PasswordHash`/`UserId`, `RoleCode` ; ports in/out.
      - [x] Use cases (sans web/JWT) : `RegisterUser`, `AuthenticateUser`, `GetProfile`.
      - [x] `adapter.out.persistence` R2DBC (`R2dbcEntityTemplate` + `DatabaseClient`, INSERT forcé
            avec id v7 Java ; `users` + `user_roles`) + mapper ; port `PasswordHasher` + adapter **BCrypt**.
      - [x] 6 tests unitaires (register/normalisation/unicité, auth OK/KO 401, email invalide).
      - [x] Exception 401 `UnauthorizedException` ajoutée à `common` + handler.
    - Vérifié : `mvn test` iam vert (6/6) ; build complet SUCCESS.
    - Note : vérification DB réelle du repository reportée au test d'intégration de SF-12b
      (register → login → appel protégé).

  - ✅ **SF-12b — JWT & sécurité réactive + endpoints d'auth**
    - CA :
      - [x] Access JWT HS256 (Nimbus, 60 min) ; refresh **opaque** haché SHA-256 en base (30 j),
            **rotation** à chaque usage, `logout` = révocation.
      - [x] `SecurityWebFilterChain` réactif (Resource Server JWT) remplaçant la config permissive ;
            routes `/api/v1/auth/**` + docs publiques, le reste authentifié ; claim `roles` → `ROLE_*`.
      - [x] Endpoints `register` (201) / `login` / `refresh` / `logout` (204) / `me`.
      - [x] Test d'intégration Testcontainers `register → login → /me → refresh(rotation) → 401` (8 tests iam).
    - Vérifié bout-en-bout (app + PG18 seedée) : le compte seed **BCrypt** `learner@skillforge.dev`
      se connecte ; `/me` OK ; catalogue **401 sans token / 200 avec** ; mauvais mdp → 401 normalisé ;
      register génère un **UUID v7** (LEARNER).
    - Dépend de : SF-12a.

  - ✅ **SF-12c — Administration des utilisateurs (ADMIN)**
    - CA :
      - [x] Endpoints ADMIN (`/api/v1/admin/users`) : `GET` lister, `PUT /{id}/roles` changer de rôle,
            `PATCH /{id}/status` activer/désactiver.
      - [x] Autorisation par rôle effective via `SecurityWebFilterChain` (`/api/v1/admin/**` → `hasRole('ADMIN')`).
      - [x] 4 tests d'intégration (401 sans token, 403 LEARNER, 200 ADMIN, changement rôle + désactivation
            → login refusé) ; 10 tests iam au total.
    - Vérifié bout-en-bout (app + seed V8) : admin liste les 2 comptes ; LEARNER → 403 ; sans token → 401 ;
      promotion learner → [LEARNER, INSTRUCTOR].
    - Dépend de : SF-12b.

  - **Bilan SF-12 :** ✅ IAM complet — inscription, connexion JWT (access/refresh + rotation), profil,
    administration par rôle. La sécurité réactive remplace la config permissive de SF-11a.

**Revue de lot 1 ✅ :** socle Maven multi-module démarrable ; 8 migrations Flyway + seed sur
PostgreSQL 18 ; contexte `catalogue` de référence (hexagonal, lecture) exposé et testé ;
IAM complet (register/login/refresh/logout/me + admin) avec JWT réactif et autorisation par
rôle. Build complet vert (17 tests, dont 2 suites Testcontainers). Dette : voir section dédiée
(port 5432 en dev ; contenu/évaluation/gamification/analytics restent des squelettes à remplir).

---

## Lot 2 — Contextes métier (P5–P9)

- **SF-20 — Catalogue : roadmaps en graphe & ingestion** (P5) — *découpé en 20a/20b*
  - Modèle graphe + endpoints de lecture : déjà faits en SF-11b. Reste l'**ingestion**.
  - **Décision idempotence :** upsert par clés naturelles (`slug`), **id préservés** (soft-refs
    des autres contextes) ; met à jour + ajoute, **ne supprime pas** (pruning = dette).

  - ✅ **SF-20a — Schéma pivot + domaine d'ingestion**
    - CA :
      - [x] Schéma JSON pivot roadmap **documenté** + exemple (`docs/PIVOT-ROADMAP.md`).
      - [x] Commande `RoadmapImport` (neutre), `IngestRoadmapUseCase`, port `RoadmapImporter`.
      - [x] Service `IngestRoadmapService` avec **validation** (slug/titre, kind, unicité slugs nœuds,
            arêtes → nœuds existants, phase référencée, importance, certif requise) → 422 `INVALID_IMPORT`.
      - [x] 7 tests unitaires de validation (16 tests catalogue au total, verts).

  - ✅ **SF-20b — Adapter d'upsert idempotent + endpoint + intégration**
    - CA :
      - [x] `RoadmapImporterAdapter` R2DBC transactionnel : upsert roadmap/certif/phases/nœuds/arêtes/
            domaines via `ON CONFLICT` + appariement (phases par titre, domaines par nom), **id préservés**.
      - [x] Endpoint `POST /api/v1/roadmaps/import` réservé INSTRUCTOR/ADMIN (règle dans la sécurité IAM).
      - [x] 2 tests d'intégration Testcontainers (idempotence + préservation d'id + mise à jour de titre).
    - Vérifié bout-en-bout : 401 sans token, **403 LEARNER, 201 ADMIN** ; ré-import → toujours 2 nœuds
      (idempotent, confirmé en base) ; import invalide → 422 `INVALID_IMPORT`. 18 tests catalogue.
    - Dépend de : SF-20a.

  - **Bilan SF-20 :** ✅ ingestion JSON pivot idempotente opérationnelle — le catalogue peut être
    peuplé/mis à jour par import (base du pipeline de contenu, SF-40).

- **SF-21 — Apprentissage : progression, navigation libre & reprise** (P6) — *cœur, découpé en 21a/21b*
  - **Décisions figées :** XP/niveau **hors apprentissage** → calculés en `gamification` (SF-24) via
    des **événements de complétion** émis ici. Compteurs inter-contextes (nb leçons/nœud, nb nœuds/
    parcours) obtenus via un **port** dont l'adapter lit temporairement les tables `lessons`/`nodes`
    (couplage documenté, à re-router vers un service `contenu`/`catalogue` en SF-22). Serveur fait foi.

  - ✅ **SF-21a — Inscription + progression + navigation non linéaire + reprise**
    - CA :
      - [x] Enrollment idempotent (find-or-create, `Mono.defer`).
      - [x] Navigation non linéaire : ouvrir n'importe quel nœud, en sortir, revenir.
      - [x] **Reprise exacte par nœud** (`node_progress.last_lesson`) + **reprise globale** (dernier parcours actif).
      - [x] Progression persistée (upsert R2DBC via `DatabaseClient`) : leçon (vue/complétée), nœud
            (% + statut + dernière leçon), parcours (dernier nœud + %). % via port `LearningCatalogPort`.
      - [x] Use cases : `EnrollLearner`, `MarkLessonViewed`, `CompleteLesson`, `GetRoadmapProgress`, `ResumeLast`.
      - [x] Endpoints `/api/v1/learning/*` (apprenant déduit du principal JWT, sans dépendance sécurité).
      - [x] 4 tests (2 unit + 2 intégration Testcontainers : reprise exacte/globale + calcul des %).
    - Vérifié bout-en-bout (app + seed) : reprise globale → caching/intro ; après « strategies » vue →
      reprise exacte caching/strategies ; complétion 2 leçons → nœud COMPLETED 100 %, parcours 25 % (1/4 nœuds).

  - ✅ **SF-21b — Favoris + streaks + événements de complétion**
    - CA :
      - [x] Favoris/bookmarks (nœud/leçon) — toggle idempotent (`DELETE … RETURNING` sinon `INSERT`) + liste.
      - [x] Streaks : logique pure `Streak.registerActivity` (même jour/jour+1/trou), mise à jour à chaque activité.
      - [x] Événements `LessonCompleted`/`NodeCompleted` via port `LearningEventPublisher` (adapter bus Spring,
            `ApplicationEventPublisher`) — consommés par gamification en SF-24.
      - [x] Endpoints `/api/v1/learning/{bookmarks,streak}` ; 11 tests apprentissage (unit + intégration Testcontainers).
    - Vérifié bout-en-bout : favori ON/OFF + liste ; streak (activité du jour n'incrémente pas — cohérent seed 3/5) ;
      événement NodeCompleted émis à la complétion d'un nœud (capté par un listener de test).
    - Dépend de : SF-21a.

  - **Bilan SF-21 :** ✅ cœur Apprentissage complet — inscription, progression persistée, navigation non
    linéaire, **reprise exacte/globale**, favoris, streaks, événements de complétion. XP/niveau/badges = SF-24.

- **SF-22 — Contenu : leçons structurées & authoring** (P7) — *découpé en 22a/22b/22c*
  - **Décisions figées :** authoring = **ingestion (bulk) + CRUD interactif complet**. Le couplage
    temporaire de `LearningCatalogPort` (apprentissage lit `lessons`/`nodes` en direct) **reste en
    dette** (pas de re-routage en SF-22). Section QUIZ porte un `quiz_id` (réf souple vers evaluation,
    non validée avant SF-23).

  - ✅ **SF-22a — Lecture du contenu**
    - CA :
      - [x] Domaine : `Lesson`, `Section` typée (`SectionType` + `DiagramFormat`), `LessonDetail`.
      - [x] Endpoints lecture : `GET /api/v1/nodes/{nodeId}/lessons`, `GET /api/v1/lessons/{lessonId}` (+ sections).
      - [x] Adapters R2DBC + 5 tests (2 unitaires + 3 intégration Testcontainers).
    - Vérifié bout-en-bout (seed) : nœud caching → 2 leçons ; leçon intro → 3 sections typées
      (CONCEPT, CODE/java, QUIZ) ; leçon inconnue → 404 `LESSON_NOT_FOUND`.

  - ✅ **SF-22b — Ingestion JSON pivot contenu**
    - CA :
      - [x] Schéma pivot « contenu d'un nœud » **documenté** (`docs/PIVOT-CONTENU.md`) + exemple.
      - [x] Upsert **idempotent** transactionnel : leçons par `(node_id, slug)` (**id préservé** →
            protège la progression) ; sections **remplacées** intégralement.
      - [x] Endpoint `POST /api/v1/nodes/{nodeId}/content/import` réservé INSTRUCTOR/ADMIN + validation 422.
      - [x] 6 tests (4 unitaires validation + 2 intégration idempotence/id préservé).
    - Vérifié bout-en-bout : import LEARNER → 403, ADMIN → 201 (leçon lisible ensuite) ; import invalide → 422.
    - Dépend de : SF-22a.

  - ✅ **SF-22c — Authoring CRUD interactif**
    - CA :
      - [x] Leçons : créer/éditer/supprimer/réordonner (`POST /nodes/{id}/lessons`, `PUT`/`DELETE /lessons/{id}`,
            `PUT /nodes/{id}/lessons/order`).
      - [x] Sections : créer/éditer/supprimer/réordonner (`POST /lessons/{id}/sections`, `PUT`/`DELETE /sections/{id}`,
            `PUT /lessons/{id}/sections/order`). Prévisualisation = endpoints de lecture (SF-22a).
      - [x] Autorisation par rôle (write → INSTRUCTOR/ADMIN, lecture → authentifié) ; validation 422.
      - [x] 16 tests contenu (3 unit authoring + CRUD d'intégration Testcontainers).
    - Vérifié bout-en-bout : création/édition LEARNER → 403, ADMIN → 201 ; section ajoutée ; lecture OK.
    - Dépend de : SF-22a.

  - **Bilan SF-22 :** ✅ contenu complet — lecture des leçons/sections typées, ingestion JSON pivot
    idempotente, et authoring CRUD interactif réservé aux créateurs.

- **SF-23 — Évaluation : quiz, défis & examens blancs chronométrés** (P8) — *découpé en 23a/23b/23c*
  - **Décisions figées :** authoring **par ingestion (pivot)** uniquement (CRUD interactif = dette) ;
    tâches pratiques d'examen **auto-évaluées** (QCM auto-corrigés, tâches CKA/CKAD notées par
    l'apprenant avec solution révélée). Ingestion réservée INSTRUCTOR/ADMIN, idempotente par id.

  - ✅ **SF-23a — Quiz : passage & correction immédiate**
    - CA :
      - [x] Domaine quiz (QCM/vrai-faux/scénario, options, justification) ; ingestion pivot **idempotente par id**
            (`POST /quizzes/import`, INSTRUCTOR/ADMIN, questions/options remplacées).
      - [x] `GET /quizzes/{id}` **masque** bonnes réponses/justifications ; `POST /quizzes/{id}/attempts` →
            score + feedback par question (correct, bonne option, justification).
      - [x] Tentative persistée (`attempts` + `attempt_answers`, transactionnel) ; 8 tests (unit + intégration).
    - Vérifié bout-en-bout (quiz du seed) : lecture sans champ `correct` ; tentative correcte → score 1/1 + justification.

  - ✅ **SF-23b — Défis de code**
    - CA :
      - [x] Défi (énoncé, squelette, solution, points clés `jsonb`) ; ingestion idempotente par id (INSTRUCTOR/ADMIN).
      - [x] `GET /challenges/{id}` **masque** solution + points clés ; `POST /challenges/{id}/submit` →
            validation par **comparaison aux points clés** (sous-chaînes, sans exécution) + feedback couverts/manquants + solution révélée.
      - [x] 6 tests évaluation ajoutés (validation + intégration Testcontainers, round-trip jsonb).
    - Vérifié bout-en-bout : import LEARNER→403 / ADMIN→201 ; lecture masquée ; soumission réussie (2/2) et échouée (points manquants).
    - Dépend de : SF-23a.

  - ✅ **SF-23c — Examens blancs chronométrés**
    - CA :
      - [x] Examen par certif (items QCM/TÂCHE), ingestion idempotente (INSTRUCTOR/ADMIN) ; démarrage
            avec **deadline** (minuteur, `deadline_at = start + durée`).
      - [x] Soumission → **score par domaine** (QCM auto-corrigés + tâches auto-évaluées `completed`),
            **points faibles** (<66 %), réussite (seuil certif), `expired` si hors délai ; historique.
      - [x] Mode **révision ciblée** (domaines faibles de la dernière tentative) ; 8 tests évaluation ajoutés.
    - Vérifié bout-en-bout : import 403/201 ; démarrage deadline ; `taskSolution` masquée ; soumission →
      2/3 réussi, domaine D1 faible ; révision → D1 ; historique → 1.
    - Dépend de : SF-23a.

  - **Bilan SF-23 :** ✅ évaluation complète — quiz corrigés, défis de code (points clés), examens blancs
    chronométrés avec scoring par domaine, points faibles, historique et révision ciblée (20 tests).

- **SF-24 — Gamification : niveaux, badges, événements** (P9) — *découpé en 24a/24b*
  - **Décisions figées :** événements d'intégration promus dans **`common`** (`dev.skillforge.common.event`),
    publiés via le bus Spring (apprentissage/évaluation publient, gamification consomme, sans couplage
    compile inter-contextes). Couverture badges **complète** → émission des événements manquants
    (`RoadmapCompleted` depuis apprentissage, `ExamPassed` depuis évaluation, `StreakUpdated`).

  - ✅ **SF-24a — Événements partagés + XP & niveaux**
    - CA :
      - [x] Événements d'intégration promus dans `common.event` ; apprentissage/évaluation refactorés ;
            émission de `LessonCompleted`/`NodeCompleted`/`RoadmapCompleted`/`StreakUpdated` (apprentissage)
            et `ExamPassed` (évaluation), via bus Spring.
      - [x] Contexte **gamification** : `@EventListener` → **attribution d'XP** (`xp_ledger`, barème 50/100/200/500) ;
            calcul du **niveau** (barème `levels` seedé) ; endpoint `GET /api/v1/gamification/me` (niveau, XP, prochain palier).
      - [x] 5 tests gamification (profil, listener, intégration) + apprentissage/évaluation toujours verts.
    - Vérifié bout-en-bout : profil 50 XP → complétion 2 leçons → événements consommés → **250 XP** (Débutant).

  - ✅ **SF-24b — Badges**
    - CA :
      - [x] Règles paramétrables (`NODE_COMPLETED`, `ROADMAP_COMPLETED`, `EXAM_PASSED`, `STREAK_DAYS {days}`,
            `XP_THRESHOLD {xp}`, via `rule_config` jsonb) évaluées sur événements ; `earned_badges` **idempotent** (`ON CONFLICT DO NOTHING`).
      - [x] `GET /api/v1/gamification/badges` ; 9 tests gamification (3 badges + profil + listener + intégration).
    - Vérifié bout-en-bout : complétion d'un nœud → événement `NodeCompleted` → **badge `NODE_MASTER` attribué** (confirmé en base).
    - Dépend de : SF-24a.

  - **Bilan SF-24 :** ✅ gamification complète — XP (événements), niveaux (barème), badges (règles paramétrables),
    profil gamifié, le tout piloté par les événements d'intégration inter-contextes.

**Revue de lot 2 ✅ :** les 5 contextes métier sont livrés et vérifiés (catalogue, apprentissage,
contenu, évaluation, gamification), reliés par un **bus d'événements d'intégration** (`common.event`).
Ingestion pivot idempotente (roadmap, contenu, quiz, défi, examen) prête pour le pipeline de contenu.
Backend fonctionnel de bout en bout (auth JWT, progression/reprise, évaluations, XP/niveaux/badges).

---

## Lot 3 — Frontend React (P10–P13)

- **SF-30 — Design system React (violet/bleu/blanc)** (P10) — *découpé en 30a/30b*
  - **Décisions figées :** **TypeScript** ; **CSS variables + CSS Modules** ; **Zustand** + client fetch typé.
    Projet dans `frontend/` (Vite + React 18).

  - ✅ **SF-30a — Scaffold + tokens + thème + primitives**
    - CA :
      - [x] Scaffold Vite + React + TS ; `npm run build` (tsc + vite) vert.
      - [x] **Design tokens** (couleurs violet/bleu/blanc + variantes sombres, typo, espacements, rayons,
            ombres, focus) via variables CSS ; **thème clair/sombre** (store Zustand, `data-theme`, persistance).
      - [x] Primitives : `Button`, `Card`, `Badge`, `Input` (+ `ThemeToggle`) ; galerie de base.
    - Vérifié : `npm run build` OK (57 modules) ; `vite preview` sert l'app (HTTP 200).

  - ✅ **SF-30b — Composants restants + galerie complète**
    - CA :
      - [x] `Progress`, `Tabs`, `Modal` (portal), `Toast` (store Zustand + viewport), `CodeBlock`
            (react-syntax-highlighter, langages Java/SQL/YAML/bash/Rust enregistrés), `Callout`, `NodeCard` (statuts).
      - [x] Galerie « Storybook maison » complète (tous les composants), bascule clair/sombre.
    - Vérifié : `npm run build` vert (bundle optimisé 248 kB / gzip 79 kB après enregistrement sélectif des langages).
    - Note : capture d'écran non réalisable (extension navigateur non disponible cette session) ; galerie
      consultable via `npm run dev` (frontend/).

  - **Bilan SF-30 :** ✅ design system complet — tokens (variables CSS, clair/sombre, WCAG AA), 11 composants
    de base, galerie de démonstration. Base prête pour le shell applicatif (SF-31).

- **SF-31 — Shell applicatif & authentification (front)** (P11) — *découpé en 31a/31b*
  - **Décisions figées :** access token **en mémoire** + refresh token **en localStorage** ; refresh
    **auto** sur 401 et au démarrage ; `react-router-dom` pour le routing. (Durcissement cookie httpOnly = dette.)

  - ✅ **SF-31a — Client API typé + auth (login/register/refresh)**
    - CA :
      - [x] Client API typé (`apiFetch`/`rawRequest`, enveloppe `data/meta/error`, Bearer auto, **refresh auto** sur 401).
      - [x] Store auth Zustand (login, register, refresh, logout, `loadMe`, `bootstrap` au démarrage ; access en
            mémoire, refresh en localStorage) ; `ApiError` typée.
      - [x] Pages connexion/inscription (`AuthPage` + `LoginForm`/`RegisterForm`) ; erreurs + états de chargement.
    - Vérifié : `npm run build` vert (156 kB) ; **flux d'auth via le proxy Vite → backend** (login/`me`/refresh
      rotation/erreur `INVALID_CREDENTIALS`). Rendu UI visuel non capturé (extension navigateur indisponible).

  - ✅ **SF-31b — Routing + gardes par rôle + layout**
    - CA :
      - [x] Routing pages publiques/privées (`BrowserRouter`) ; **gardes de routes** (`RequireAuth`,
            `RequireRole role="ADMIN"`, `PublicOnly` sur `/login`).
      - [x] Layout : sidebar de navigation (`AppLayout`), header (niveau/XP via `GET /gamification/me`,
            bascule thème, déconnexion) ; pages privées placeholders (accueil, catalogue, roadmap,
            nœud, leçon, profil, admin, 404).
    - Fichiers : `src/auth/guards.tsx`, `src/layout/{AppLayout,Header}.{tsx,module.css}`,
      `src/pages/{Placeholder,index}.tsx` + CSS, `src/App.tsx` (réécrit avec le router).
    - Dette : `@types/node` ajouté en devDep (typage de `vite.config.ts`, usage de `process.env`).
    - Vérifié : `npm run build` vert (tsc strict + vite, 183 kB). Rendu UI visuel non capturé
      (extension navigateur indisponible).
    - Dépend de : SF-31a.
  - Dépend de : SF-12 (endpoints IAM), SF-30.

- **SF-32 — Expérience d'apprentissage (catalogue, roadmap, leçon)** (P12) — *cœur front, découpé en 32a-d*
  - **Décisions figées :** coches leçons via petit endpoint backend `GET /learning/nodes/{nodeId}/lessons/progress`
    (32c) ; quiz **interactif inline** dans la leçon (32d) ; roadmap en **colonnes par phase** (pas de graphe SVG).

  - ✅ **SF-32a — Types & fetchers + Catalogue + bandeau Reprendre + Dashboard**
    - CA :
      - [x] Types TS du domaine (catalogue, contenu, apprentissage) ; fetchers `api/{catalogue,learning}.ts`.
      - [x] Client API : gestion du `204 No Content` (viewed/completed).
      - [x] Page **Catalogue** : liste des parcours, recherche `?q=`, badge type, **inscription** (`Commencer`)
            + `Aperçu` ; hook `useAsync` + `AsyncSection` (chargement/erreur/vide).
      - [x] Bandeau **Reprendre** (`GET /learning/resume` → `/lessons/:id`) réutilisable.
      - [x] **Dashboard** : bandeau Reprendre + série (`GET /learning/streak`) + accès catalogue.
    - Fichiers : `api/{types,client,catalogue,learning}.ts`, `hooks/useAsync.ts`,
      `components/{AsyncSection,ResumeBanner}.tsx`, `pages/{CataloguePage,DashboardPage}.tsx` (+ CSS).
    - Vérifié : `npm run build` vert (188 kB). Rendu visuel/e2e non capturé (extension navigateur indisponible).

  - ✅ **SF-32b — Roadmap interactive**
    - CA :
      - [x] Page `/roadmaps/:slug` : graphe (`GET /roadmaps/{slug}`) rendu en **colonnes par phase**,
            nœuds triés par position, cliquables (→ `/nodes/:id`).
      - [x] Statut + % par nœud via `GET /learning/roadmaps/{id}/progress` (bordure colorée + badge +
            barre) ; **prérequis** listés depuis les arêtes entrantes.
      - [x] Inscription si non inscrit (404 `NOT_ENROLLED` géré → bouton « Commencer ») ; sinon barre de
            progression globale. **Favori nœud** (★) avec bascule optimiste (`POST /learning/bookmarks`).
    - Fichiers : `pages/RoadmapPage.tsx` (+ CSS), `pages/index.tsx`, `App.tsx`.
    - Vérifié : `npm run build` vert (193 kB). Rendu visuel/e2e non capturé (extension navigateur indisponible).
  - ✅ **SF-32c — Endpoint leçons terminées (exception backend) + Vue nœud**
    - CA :
      - [x] *(backend)* `GET /learning/nodes/{nodeId}/lessons/progress` → IDs des leçons **complétées** de
            l'apprenant courant (jointure `lesson_progress`×`enrollments` par `user_id`+`node_id`, statut
            COMPLETED). Port `LessonProgressStore.findCompletedLessonIds`, use case
            `GetNodeLessonProgressUseCase`, DTO `NodeLessonProgressDto`, bean câblé.
      - [x] *(front)* Page `/nodes/:id` : titre, résumé, importance, temps ; **coches par leçon** (✓/○ +
            badge « Terminée »), barre `n/total`, bouton **Commencer/Continuer** (1re leçon non terminée),
            **favori nœud** (★) optimiste.
    - Fichiers back : `domain/port/out/LessonProgressStore`, `adapter/out/persistence/LessonProgressStoreAdapter`,
      `domain/port/in/GetNodeLessonProgressUseCase`, `application/GetNodeLessonProgressService`,
      `adapter/in/web/{LearningController,dto/NodeLessonProgressDto}`, `config/LearningConfig`.
    - Fichiers front : `api/{types,learning}.ts`, `pages/NodePage.tsx` (+ CSS), `pages/index.tsx`, `App.tsx`.
    - Vérifié : `mvn -pl apprentissage test` **vert (12 tests**, dont IT leçons complétées via Testcontainers
      PG18) ; `npm run build` vert (196 kB).
  - ✅ **SF-32d — Vue leçon**
    - CA :
      - [x] Page `/lessons/:id` : sections typées rendues (TEXT paragraphes, **CODE** coloré via `CodeBlock`,
            DIAGRAM `<pre>`, CALLOUT `Callout`, **QUIZ** interactif inline).
      - [x] **Reprise exacte** : `markViewed` à l'ouverture (met à jour le point de reprise serveur).
      - [x] **« Leçon terminée »** persistée (`POST /learning/completed`) + toast ; badge « Terminée ».
      - [x] Navigation **Précédent / Suivant** (leçons sœurs triées) + retour au nœud ; **favori leçon** (★).
      - [x] **Quiz inline** (`QuizRunner`) : `GET /quizzes/{id}`, sélection radio, `POST /quizzes/{id}/attempts`,
            correction immédiate (bonne/mauvaise option, justification, score), recommencer.
    - Fichiers : `api/{types,evaluation}.ts`, `components/QuizRunner.tsx` (+ CSS), `pages/LessonPage.tsx` (+ CSS),
      `pages/index.tsx`, `App.tsx`.
    - Dette (optionnelle) : `LessonPage` non code-splittée → `react-syntax-highlighter` dans le bundle principal
      (293 kB / 94 kB gz). Lazy-load possible plus tard.
    - Vérifié : `npm run build` vert (endpoints quiz déjà couverts côté back en SF-23).
  - Dépend de : SF-20, SF-21, SF-22, SF-31.

  - **Bilan SF-32 :** ✅ expérience d'apprentissage de bout en bout — catalogue → roadmap (colonnes par phase) →
    nœud (coches leçons) → leçon (sections typées, quiz inline, reprise exacte, complétion persistée). Favoris,
    série et progression **côté serveur**. Reste pour le Lot 3 : SF-33 (évaluations avancées & dashboards).

- **SF-33 — Évaluations, simulateur d'examen & dashboards (front)** (P13) — *découpé en 33a-d*
  - **Décisions figées :** éditeur défis = `<textarea>` monospace (pas de CodeMirror, stack figée) ; petits
    endpoints de **liste** backend ajoutés (exceptions documentées, comme 32c) ; noms de domaines d'examen
    exposés via endpoint catalogue + jointure front.

  - ✅ **SF-33a — Tableau de bord & profil**
    - CA :
      - [x] *(backend)* `GET /learning/enrollments` → inscriptions de l'apprenant courant
            (`EnrollmentStore.findByLearner`, `ListEnrollmentsUseCase`/`Service`, bean câblé).
      - [x] *(front)* **Dashboard** : progression **par parcours** (inscriptions × roadmaps), Continuer →
            dernière leçon active, **recommandations** (parcours non commencés), série ; bandeau Reprendre.
      - [x] *(front)* **Profil** `/profile` : niveau/XP + barre vers le niveau suivant
            (`GET /gamification/me`), **badges** (`GET /gamification/badges`, icône/description/date), série.
    - Fichiers back : `domain/port/out/EnrollmentStore`, `adapter/out/persistence/EnrollmentStoreAdapter`,
      `domain/port/in/ListEnrollmentsUseCase`, `application/ListEnrollmentsService`,
      `adapter/in/web/LearningController`, `config/LearningConfig`.
    - Fichiers front : `api/{types,learning,gamification}.ts`, `pages/{DashboardPage,ProfilePage}.tsx` (+ CSS),
      `pages/index.tsx`, `App.tsx`.
    - Vérifié : `mvn -pl apprentissage test` **vert (13 tests**, dont IT liste inscriptions) ; `npm run build`
      vert (298 kB).

  - ✅ **SF-33b — Défis de code**
    - CA :
      - [x] *(backend)* `GET /nodes/{nodeId}/challenges` → résumés des défis d'un nœud
            (`ChallengeSummary`, `ChallengeRepository.findByNode`, `ListNodeChallengesUseCase`/`Service`,
            `NodeChallengeController`, DTO, bean).
      - [x] *(front)* Défis listés sur la **vue nœud** (⚡ + langage → `/challenges/:id`).
      - [x] *(front)* Page `/challenges/:id` : énoncé, **squelette éditable** (`<textarea>` mono, pré-rempli),
            soumission (`POST /challenges/{id}/submit`) → **points clés atteints (✓) / manquants (✗)** +
            statut réussi/à retravailler, **solution de référence révélable** (`<details>` + `CodeBlock`).
    - Fichiers back : `domain/model/ChallengeSummary`, `domain/port/out/ChallengeRepository`,
      `adapter/out/persistence/ChallengeRepositoryAdapter`, `domain/port/in/ListNodeChallengesUseCase`,
      `application/ListNodeChallengesService`, `adapter/in/web/{NodeChallengeController,dto/ChallengeSummaryDto}`,
      `config/EvaluationConfig`.
    - Fichiers front : `api/{types,evaluation}.ts`, `pages/ChallengePage.tsx` (+ CSS), `pages/NodePage.tsx` (+ CSS),
      `App.tsx`.
    - Vérifié : `mvn -pl evaluation test` **vert (21 tests**, dont IT liste défis par nœud via Testcontainers
      PG18) ; `npm run build` vert (301 kB).
  - **SF-33c — Simulateur d'examen** — *scindé en 33c1/33c2 (slice trop gros : découverte sur 2 contextes + simulateur)*
    - ✅ **SF-33c1 — Découverte examens (backend catalogue + évaluation) + surfaçage**
      - CA :
        - [x] *(catalogue)* `GET /roadmaps/{slug}/certification` → certification + **domaines pondérés**
              (modèles `Certification`/`ExamDomain`, port+adapter `CertificationRepository` DatabaseClient,
              use case/service, DTO, mapper, bean).
        - [x] *(évaluation)* `GET /certifications/{certificationId}/exams` → résumés des examens
              (`ExamSummary`, `ExamRepository.findByCertification`, use case/service, `CertificationExamController`,
              DTO, bean).
        - [x] *(front)* Sur une roadmap de **certification**, section **« Examens blancs »** (⏱️ format/durée →
              `/exams/:id`, page placeholder jusqu'à 33c2).
      - Vérifié : `mvn -pl catalogue test` **vert (20 tests**, dont IT certification + 404) ;
        `mvn -pl evaluation test` **vert (21 tests**, dont IT liste examens par certification) ;
        `npm run build` vert (302 kB). *(Fix doubles de test : `SubmitChallengeServiceTest`/`SubmitExamServiceTest`
        passés en classes anonymes suite aux nouvelles méthodes de port.)*
    - ✅ **SF-33c2 — Simulateur de passage**
      - CA :
        - [x] *(backend)* `certificationId` exposé sur `ExamDto` + `GET /certifications/{id}` (catalogue) pour
              les **libellés de domaines** (port/adapter `findById`, use case `getById`, endpoint).
        - [x] *(front)* Page `/exams/:id` — 3 phases : **intro** (format/durée/nb questions) → **passage**
              (minuteur `deadlineAt` avec auto-soumission à l'expiration, **palette** de navigation entre items,
              MCQ radios / TASK « tâche réalisée », précédent/suivant) → **résultat** (score global, réussi/échoué,
              temps écoulé, **score par domaine** avec **points faibles** et noms de domaines joints).
      - Fichiers back : `evaluation` (`ExamDto`, `EvaluationWebMapper`), `catalogue`
        (`CertificationRepository.findById`, adapter, `GetCertificationUseCase.getById`, `CatalogueController`).
      - Fichiers front : `api/{types,catalogue,evaluation}.ts`, `pages/ExamPage.tsx` (+ CSS), `pages/index.tsx`,
        `App.tsx`.
      - Vérifié : `mvn -pl catalogue,evaluation test` **vert (catalogue 21 / évaluation 21**, dont IT cert par id) ;
        `npm run build` vert (308 kB).
  - ✅ **SF-33d — Historique, révision ciblée & recommandations**
    - CA :
      - [x] **Historique des tentatives** sur la page examen (intro) : date, score, réussi/échoué, temps écoulé
            (`GET /exams/{id}/attempts`).
      - [x] **Révision ciblée** : encart « points faibles » (domaines faibles nommés) avant de repasser
            (`GET /exams/{id}/revision`) ; bouton devient « Repasser l'examen » si tentatives existantes.
      - [x] Après soumission, retour à l'intro rafraîchit historique + révision (`reload`).
      - [x] **Recommandations dashboard** : déjà livrées en SF-33a (parcours non commencés + Reprendre).
    - Fichiers front : `api/{types,evaluation}.ts`, `pages/ExamPage.tsx` (+ CSS).
    - Dette : recommandations dashboard basées sur les **points faibles inter-examens** non faites (nécessiterait
      un endpoint d'agrégation « toutes mes tentatives » ; la révision reste par examen).
    - Vérifié : endpoints historique/révision déjà couverts côté back (SF-23, `ExamIntegrationTest`) ;
      `npm run build` vert (310 kB).
  - Dépend de : SF-23, SF-24, SF-31.

  - **Bilan SF-33 :** ✅ évaluations & dashboards complets — dashboard (parcours/série/reco) + profil (niveau/XP,
    badges), défis de code, simulateur d'examen chronométré (minuteur, navigation, résultat par domaine + points
    faibles), historique & révision ciblée. **Lot 3 (front) terminé.**
  - Dépend de : SF-23, SF-24, SF-31.

**Revue de lot 3 :** parcours utilisateur complet de bout en bout dans le navigateur.

---

## Lot 4 — Contenu, déploiement & qualité (P14–P16)

- **SF-40 — Pipeline de génération de contenu (JSON pivot)** (P14) — *découpé en 40a-d*
  - **Décisions figées :** pipeline = **script Node/TS + fichiers JSON** sous `content/<slug>/…` appelant les
    endpoints d'import REST existants (pas de nouveau module Java) ; périmètre du lot = **1 parcours de référence
    complet (Backend)** prouvant toute la chaîne, le reste = dette d'authoring incrémentale ; import déjà
    idempotent (upsert), on ajoute un **manifeste de reprise** pour les lots.
  - Structure contenu : `content/<slug>/roadmap.json` (pivot roadmap), `content/<slug>/nodes/<node-slug>.json`
    (pivot contenu de nœud), `content/<slug>/quizzes|challenges|exams/*.json`.

  - ✅ **SF-40a — Schémas pivot formalisés + validateur**
    - CA :
      - [x] 5 **JSON Schemas** (`content-pipeline/schemas/`) du format authoré : roadmap, contenu de nœud, quiz,
            défi, examen — alignés sur `docs/PIVOT-*.md` et les commandes d'import (clés naturelles + `quizRef`).
      - [x] Script Node `validate.mjs` (ajv + cohérence inter-fichiers) : structure + slugs uniques, refs
            edges/phases/domaines/quizRef, `certification`⇔`kind`, options correctes, items d'examen ;
            rapport `fichier: message` + **exit 1** si erreur. Options `[contentRoot] [--only <slug>]`.
      - [x] `content/demo/` (parcours d'exemple valide) + `README.md` (organisation, table schémas↔endpoints).
    - Fichiers : `content-pipeline/{package.json,validate.mjs,README.md,.gitignore,schemas/*.json}`,
      `content/demo/{roadmap.json,nodes/intro.json,quizzes/intro-check.json,challenges/hello.json}`.
    - Décision : **format authoré ≠ payload d'import** — clés naturelles + `quizRef` local (l'ingesteur SF-40b
      résout slugs→UUID et injecte `nodeId`/`quizId`). `ajv` ajouté comme dépendance du pipeline.
    - Vérifié : `npm run validate` **vert** sur `content/demo` (exit 0) ; cas cassé → **6 erreurs détectées**
      (enum, slug dupliqué, phase/arête inconnues, option sans correct, quizRef inconnu) + exit 1.
  - ✅ **SF-40b — Ingesteur / pipeline d'import (reprise)**
    - CA :
      - [x] `ingest.mjs` : login admin (seed), pour chaque parcours → `POST /roadmaps/import` → relecture du
            graphe (`GET /roadmaps/{slug}`) pour résoudre les `nodeId` → `POST /nodes/{id}/content/import` par
            nœud (relit les leçons pour les `lessonId`) → `POST /quizzes|challenges|exams/import`.
      - [x] Résolution clés naturelles → UUID : **`quizRef` → `quizId` déterministe** (UUID v5 stable) injecté
            dans les sections QUIZ **et** l'import du quiz (⇒ liaison correcte) ; défis/quiz rattachés au bon nœud.
      - [x] Idempotent + **manifeste de reprise** `.ingest-state.json` (hash par étape ; saute l'inchangé) ;
            options `[contentRoot] [--only <slug>] [--api <url>] [--reset]` ; log étape par étape.
      - [x] Refactor : logique commune (validation + chargement) extraite dans `lib/content.mjs` ; `ingest`
            **valide avant d'importer** (abandon si erreur).
    - Fichiers : `content-pipeline/{ingest.mjs,lib/content.mjs}`, `validate.mjs` (allégé), `package.json`.
    - ~~Dette : items d'examen MCQ non supportés~~ → **levée en SF-40d** : l'ingesteur relit le quiz importé
      (`GET /quizzes/{id}`) pour résoudre `questionIndex` → `questionId` ; MCQ **et** TASK supportés.
    - Vérifié **e2e** contre un backend réel (PG18 Testcontainers-like + jar, port 8090) : `ingest --only demo`
      → roadmap + contenu + quiz + défi importés ; API confirme (leçon `bienvenue`, section QUIZ dont
      `quizId` == id du quiz importé, quiz 3 options, défi `java` découvrable) ; **2ᵉ run = tout « inchangé »**
      (manifeste) ; `--reset` réimporte **sans dupliquer** (upsert).
  - ✅ **SF-40c — Parcours de référence Backend (contenu réel)**
    - CA :
      - [x] `content/backend/` : roadmap **8 nœuds / 4 phases / 7 arêtes** (Fondamentaux → Données → APIs →
            Qualité), leçons riches à **sections typées** (CONCEPT, CODE java/sql, DIAGRAM mermaid, EXERCISE, QUIZ)
            pour les 8 nœuds, **2 quiz** (databases-basics, caching-check) + **2 défis Java** (dedupe, cache
            thread-safe), en français, code Java/Spring.
      - [x] Passé de bout en bout : `validate --only backend` ✓ puis `ingest --only backend` contre un backend
            réel (PG18 + jar) → roadmap + 8 contenus + 2 quiz + 2 défis importés.
      - [x] **Migration V9** `V9__free_backend_slug.sql` : renomme le parcours placeholder du seed
            (slug `backend` → `decouverte`) pour libérer `backend` au contenu authoré (on n'édite pas V8).
    - Vérifié **e2e** : graphe `backend` propre (8 nœuds, aucune pollution du placeholder) ; section QUIZ dont
      `quizId` == quiz importé ; **soumission de la solution du défi caching → `passed=true`** (2 points clés) ;
      2ᵉ ingest = 13 étapes « inchangé » (manifeste). V9 appliqué proprement au démarrage.
    - Dette pipeline : l'import roadmap **ne gère pas** la ré-import sur une structure de phases différente
      (contrainte `phases(roadmap_id, position)` + pas de pruning) — non bloquant pour l'authoring de nouveaux
      parcours ; le renommage du placeholder (V9) contourne la collision avec le seed.
  - 🔄 **SF-40d — Authoring incrémental** : parcours restants + certifications (KCNA/CKA/CKAD).
    - ✅ **Docker** (`content/docker/`) : 7 nœuds / 4 phases, sections typées, 2 quiz + 1 défi (Dockerfile).
    - ✅ **Kubernetes** (`content/kubernetes/`) : 8 nœuds / 4 phases (Fondamentaux → Workloads → Réseau/config →
          Exploitation), sections typées (CODE yaml/bash, DIAGRAM), 2 quiz (pods, services) + 1 défi (Deployment).
    - ✅ **CKAD** (`content/ckad/`, **1ʳᵉ certification**) : `kind=CERTIFICATION`, 5 **domaines pondérés**,
          5 nœuds mappés aux domaines, 1 quiz + **1 examen blanc PRACTICAL** (6 tâches). **Chaîne certification
          prouvée e2e** : cert → liste examens → passage (deadline/minuteur) → soumission → **score 52/52 par
          domaine, réussi**.
    - ✅ **Terraform** (`content/terraform/`) : 8 nœuds / 4 phases (Fondamentaux → Langage → État/modules →
          Workflow), sections typées (CODE bash/hcl), 2 quiz (state, workflow) + 1 défi (ressource).
    - ✅ **Rust** (`content/rust/`) : 8 nœuds / 4 phases (Fondamentaux → Ownership & types → Erreurs &
          abstractions → Concurrence & écosystème), CODE **rust** coloré, 2 quiz (ownership, result) +
          1 défi (`sum_even` idiomatique).
    - ✅ **Software Design & Architecture** (`content/software-design/`) : 8 nœuds / 4 phases (Principes →
          Conception objet → Architecture applicative → Systèmes distribués), CODE java + DIAGRAM,
          2 quiz (SOLID, hexagonal) + 1 défi (définir un port). Ancré sur l'archi du projet.
    - ✅ **API Security** (`content/api-security/`) : 8 nœuds / 4 phases (Fondamentaux → Authn/Authz →
          Menaces → Durcissement), CODE java/yaml, 2 quiz (authn, injection) + 1 défi (vérifier un mot de passe
          BCrypt). Couvre triade CIA, TLS, JWT/OAuth, RBAC/BOLA, injections, XSS/CSRF/SSRF, secrets, CORS.
    - ✅ **Backend Performance** (`content/backend-performance/`) : 8 nœuds / 4 phases (Mesurer → Base de
          données → Application & I/O → Charge & résilience), CODE java/sql, **3 quiz** (mesure, N+1, async) +
          1 défi (éliminer un N+1 en SQL). Couvre percentiles/SLO, index & plans, N+1, cache, réactif, pools,
          backpressure/résilience.
    - ✅ **AWS** (`content/aws/`) : 8 nœuds / 4 phases (Fondamentaux → Calcul & réseau → Stockage & bases →
          Exploitation), CODE bash/json, 2 quiz (responsabilité partagée, IAM) + 1 défi (S3 CLI). Couvre
          régions/AZ, responsabilité partagée, IAM/moindre privilège, EC2/Lambda/conteneurs, VPC, S3/EBS,
          RDS/DynamoDB, CloudWatch/coûts, Well-Architected.
    - ✅ **Prompt Engineering** (`content/prompt-engineering/`) : 8 nœuds / 4 phases (Fondamentaux →
          Techniques → Contexte & données → Fiabilité & production), 3 quiz (LLM, CoT, sûreté) + 1 défi
          (prompt JSON). Couvre tokens/contexte/température, anatomie d'un prompt, few-shot, raisonnement
          pas-à-pas, sorties structurées, RAG, évaluation, hallucinations & injection de prompt.
    - ✅ **CKA** (`content/cka/`, **2ᵉ certification**) : `kind=CERTIFICATION`, 5 domaines pondérés (25/15/20/10/30),
          5 nœuds admin (kubeadm/etcd/RBAC, ordonnancement, réseau, stockage, dépannage), 1 quiz +
          **1 examen blanc PRACTICAL** (6 tâches). Certification + examen listés/ingérés dans le stack live.
    - ✅ **Dette levée — ingestion des examens MCQ** : l'ingesteur résout désormais `quizRef`+`questionIndex`
          → `questionId` en relisant le quiz importé (`GET /quizzes/{id}` expose les `id`+`position`) — **aucun
          changement backend nécessaire**. Vérifié e2e : examen **KCNA** MCQ, score **30/30, réussi, 5 domaines**.
    - ✅ **KCNA** (`content/kcna/`, **3ᵉ certification**) : `kind=CERTIFICATION` **MCQ**, 5 domaines (46/22/16/8/8),
          5 nœuds (fondamentaux, orchestration, archi cloud native, observabilité, livraison), banc de 6 questions +
          **1 examen blanc MCQ** référençant ces questions par index.
    - Avancement : **10/10 tracks** (couverture) + **3/3 certifs ✅** (CKAD, CKA, KCNA). Tous **ingérés
          dans le stack live** (14 parcours avec le seed `decouverte`).
  - Dépend de : SF-20, SF-22 (schémas + endpoints d'import).

- 🔄 **SF-43 — Refonte approfondie du contenu Backend (0 → expert)** (nouveau)
  - Constat : la 1ʳᵉ passe de contenu (SF-40) était trop superficielle. Objectif : **chaque élément** de la
    roadmap officielle (`app/roadmap-contenu/backend.pdf` = roadmap.sh Backend) traité **de 0 à expert**.
  - **Standard de profondeur** par nœud : plusieurs leçons formant une progression (bases → intermédiaire →
    **niveau expert**), sections typées riches (CONCEPT progressifs, CODE, DIAGRAM, EXERCISE avec corrigé),
    un quiz de révision, et un défi quand pertinent.
  - **Roadmap restructurée** (`content/backend/roadmap.json`) fidèle à roadmap.sh : **10 phases, 26 nœuds**
    (Internet → Langage/Git → BDD relationnelles → NoSQL/scaling → APIs → Auth/sécurité → Caching → Tests/CI-CD →
    Design/archi → Scalabilité/infra). Ancien Backend (8 nœuds superficiels) réinitialisé en base puis réingéré.
  - ✅ **1 — Internet & le Web** : 5 leçons (fonctionnement d'Internet/TCP-IP, HTTP, DNS, hébergement,
    cycle complet d'une requête), sections jusqu'au « niveau expert », 2 diagrammes, exercices corrigés,
    quiz 6 questions + défi curl.
  - ✅ **2 — Le langage Java (Java 25)** : 7 leçons (JVM & `void main()` compact, records & sealed, collections/
    génériques/Optional, exceptions, **pattern matching & switch moderne**, Streams/Gatherers, **virtual threads**
    & scoped values), code Java moderne, quiz 6 questions + défi (aire par record patterns).
  - ✅ **3 — Git & gestion de versions** : 6 leçons (VCS distribué & snapshots, flux add/commit/.gitignore,
    branches & fusion/conflits, remotes & pull requests, reset/revert/stash/reflog, **rebase & workflows**
    + conventional commits/tags/bisect), quiz 6 questions + défi (feature branch).
  - ✅ **4 — Bases relationnelles & PostgreSQL** : 5 leçons (modèle relationnel & clés, PostgreSQL client/serveur
    & types riches uuid/jsonb/timestamptz, DDL & migrations Flyway, **contraintes & intégrité référentielle**,
    internes MVCC/VACUUM/pooling), quiz 6 questions + défi (CREATE TABLE avec contraintes).
  - ✅ **5 — SQL & modélisation** : 5 leçons (SELECT/WHERE & pagination keyset, **jointures** INNER/LEFT,
    agrégation GROUP BY/HAVING & COUNT(*), **requêtes avancées** CTE/window functions, **modélisation &
    normalisation** + N-M/dénormalisation), quiz 6 questions + défi (JOIN + GROUP BY).
  - ✅ **6 — Transactions & ACID** : 4 leçons (tout ou rien, isolation & anomalies, verrous pessimiste/optimiste,
    outbox/saga), quiz + défi (UPDATE atomique).
  - ✅ **7 — Index & performance** : 4 leçons (index B-tree, plans EXPLAIN, N+1, profiling/pg_stat_statements),
    quiz + défi (CREATE INDEX composite).
  - ✅ **8 — ORMs & accès aux données** : 3 leçons (mapping, frontières hexagonales, pièges N+1/SQL brut), quiz.
  - ✅ **9 — Bases NoSQL** : 3 leçons (pourquoi/quand, familles document/KV/colonnes/graphe/TS, polyglotte), quiz.
  - ✅ **10 — Scaler les bases** : 3 leçons (réplication & lag, sharding & hotspots, CAP CP/AP), quiz.
  - ✅ **11 — APIs REST** : 4 leçons (ressources/verbes, statuts & erreurs normalisées, conception/idempotence,
    OpenAPI/HATEOAS/Richardson), quiz + défi.
  - ✅ **12 — GraphQL, gRPC & SOAP** : 3 leçons (au-delà de REST, GraphQL over/under-fetching, gRPC/protobuf & SOAP), quiz.
  - ✅ **13 — Authentification** : 4 leçons (authn/authz & hashing BCrypt, sessions vs JWT stateless, OAuth2/OIDC,
    SAML & bonnes pratiques MFA/protection du jeton), quiz.
  - ✅ **14 — Sécurité web** : 4 leçons (HTTPS/TLS, injections & requêtes paramétrées, XSS/CSRF/CORS/CSP,
    OWASP/BOLA & durcissement), quiz + défi (requête paramétrée).
  - ✅ **15 — Caching** : 4 leçons (pourquoi/après optimisation, niveaux client/CDN/serveur & ETag/304,
    Redis & stratégies d'invalidation, patterns cache-aside/stampede), quiz + défi (cache thread-safe).
  - ✅ **16 — Tests** : 4 leçons (pyramide, unitaires AAA/mocks, intégration Testcontainers, TDD), quiz.
  - ✅ **17 — CI / CD** : 3 leçons (CI & barrière qualité, livraison/déploiement continus rolling/blue-green/canary,
    feature flags & migrations rétrocompatibles), quiz.
  - ✅ **18 — Principes de conception** : 4 leçons (patterns GOF, DDD & contextes bornés, CQRS, Event Sourcing), quiz.
  - ✅ **19 — Patterns d'architecture** : 4 leçons (monolithe modulaire, microservices/SOA, serverless/service mesh,
    twelve-factor), quiz.
  - ✅ **20-26 — Infra & scale** : Message brokers (asynchrone/Kafka-RabbitMQ/idempotence), Conteneurs
    (VM vs conteneur, Dockerfile multi-stage, K8s), Serveurs web (reverse proxy, LB, SPA+API), Building for scale
    (vertical/horizontal, circuit breaker/backpressure/retries), Observabilité (3 piliers, percentiles, SLO),
    Temps réel (polling/SSE/WebSockets + backplane), Moteurs de recherche (index inversé, analyse, pertinence).
  - **✅ Backend refondu : 26/26 nœuds, ~97 leçons, 26 quiz + défis de code.** Chaque nœud débutant → expert,
    sections typées (CONCEPT progressifs, CODE, DIAGRAM, EXERCISE corrigé), quiz de révision. Tout ingéré et
    vérifié en ligne (0 nœud vide).
  - Reste (chantier futur) : appliquer le **même approfondissement** aux 9 autres tracks (Docker, Kubernetes,
    Terraform, Rust, Software Design, API Security, Backend Performance, AWS, Prompt Engineering).

- ✅ **SF-44 — Couche « expert » Backend (comble l'audit, sans régression)** — **TERMINÉ (26/26)**
  - **Bilan** : les 26 nœuds du parcours Backend enrichis d'une couche experte (2 leçons de plus en moyenne
    par nœud), **~97 → 151 leçons** (+54), **+26 quiz expert** (52 quiz au total). Chaque nœud comble les manques
    de l'audit senior et vise désormais le niveau « Expert », avec une section « Pour aller plus loin »
    (RFC/docs/livres) par nœud. **0 régression** vérifiée en base à chaque lot (leçons d'origine intactes,
    identifiants préservés, progression apprenants conservée).
  - Suite à l'audit senior (26 nœuds au niveau « Confirmé », cible « Expert »), on **ajoute** à chaque nœud une
    couche expert : nouvelles leçons de profondeur sur les manques relevés, plus de code complet, exercices
    corrigés, défis, quiz expert, et une section « Pour aller plus loin » (RFC/docs/livres).
  - **Sans régression** garanti : on n'édite que par **ajout** de leçons ; l'ingestion fait un upsert par
    `(nœud, slug)` en préservant les identifiants (progression des apprenants conservée) ; les leçons existantes
    restent intactes. Ingestion incrémentale (manifeste).
  - Limite : un **lab exécutable** (sandbox) reste une fonctionnalité de plateforme à construire à part.
  - ✅ **1/26 — Internet & le Web** : +3 leçons expert (TLS 1.3/handshake/certs/mTLS/HSTS/OCSP,
    HTTP/2 & HTTP/3/QUIC, diagnostic réseau curl/dig/traceroute + cache HTTP/CORS mécanique) + quiz expert +
    « Pour aller plus loin » (RFC 9110-9114, 8446, 9000). **5 → 8 leçons, 0 régression** (vérifié en ligne).
  - ✅ **2/26 — Langage Java** : +3 leçons expert (modèle mémoire/happens-before/volatile/locks/CAS-Atomics/
    CompletableFuture ; perf JVM : heap/générations/G1-ZGC/GC logs/JIT/JMH/JFR ; génériques PECS/erasure,
    contrats equals-hashCode, JPMS, outillage jcmd/jstack/jmap) + quiz `java-expert` + « Pour aller plus loin »
    (Effective Java, JCiP, JLS, JEPs). **7 → 10 leçons, 0 régression**.
  - ✅ **3/26 — Bases relationnelles** : +2 leçons expert (PL/pgSQL fonction+trigger, LISTEN/NOTIFY, jsonb+GIN,
    RLS, extensions pg_trgm/pgvector ; réplication/Patroni, pg_dump/PITR/WAL, autovacuum/PgBouncer/tuning) +
    quiz `postgres-expert` + « Pour aller plus loin ». **5 → 7 leçons, 0 régression**.
  - ✅ **4/26 — SQL & modélisation** : +2 leçons expert (SQL avancé : WITH RECURSIVE, UPSERT ON CONFLICT,
    LATERAL/top-N, set ops/anti-joins, grouping sets ; modélisation : audit/temporal/soft-delete/SCD/star schema,
    anti-pattern EAV, requêtes index-aware, EXPLAIN ANALYZE, dénormalisation mesurée) + quiz `sql-expert` +
    « Pour aller plus loin » (Winand, Kleppmann, explain.dalibo). **5 → 7 leçons, 0 régression**.
  - ✅ **5/26 — Transactions & ACID** : +2 leçons expert (MVCC/xmin-xmax/snapshot/VACUUM/bloat/wraparound,
    SSI Serializable ; SELECT FOR UPDATE SKIP LOCKED/NOWAIT file de jobs, advisory locks, savepoints,
    anti-deadlock par ordre de verrouillage) + quiz `transactions-expert` + « Pour aller plus loin ».
    **4 → 6 leçons, 0 régression**.
  - ✅ **6/26 — Index & performance** : +2 leçons expert (types d'index GIN/GiST/BRIN/Hash/partiel/expression/
    couvrant INCLUDE + index-only scan ; planner par statistiques, Bitmap/Index-Only scan, stratégies de
    jointure, CREATE INDEX CONCURRENTLY, ANALYZE) + quiz `indexes-expert` + « Pour aller plus loin ».
    **4 → 6 leçons, 0 régression**.
  - ✅ **7/26 — ORMs** : +2 leçons expert (contexte de persistance/dirty checking/flush JPA vs modèle stateless
    R2DBC, verrou optimiste @Version ; R2DBC en pratique : relations sans graphe, piège isNew insert/update,
    schéma piloté par Flyway) + quiz `orms-expert` + « Pour aller plus loin ». **3 → 5 leçons, 0 régression**.
  - ✅ **8/26 — Git** : +2 leçons expert (modèle objet blob/tree/commit/tag, refs & HEAD, DAG, packfiles,
    cat-file/rev-parse ; worktrees/hooks/rerere, commits signés GPG-SSH, filter-repo pour purger un secret,
    bisect run + blame) + quiz `git-expert` + « Pour aller plus loin » (Pro Git). **6 → 8 leçons, 0 régression**.
  - ✅ **9/26 — Bases NoSQL** : +2 leçons expert (embedding vs referencing, single-table design DynamoDB,
    partition/clustering keys & tombstones Cassandra ; réplication & quorum R+W>N, LWW/vector clocks/CRDT) +
    quiz `nosql-expert` + « Pour aller plus loin » (Kleppmann, papier Dynamo). **3 → 5 leçons, 0 régression**.
  - ✅ **10/26 — Scaler les bases** : +2 leçons expert (partitionnement déclaratif RANGE/HASH, réplication
    sync/async + slots, failover Patroni ; hachage cohérent, scatter-gather, 2PC vs saga, PACELC, Citus/Vitess)
    + quiz `scaling-expert` + « Pour aller plus loin ». **3 → 5 leçons, 0 régression**.
  - ✅ **11/26 — APIs REST** : +2 leçons expert (cache HTTP conditionnel ETag/If-None-Match/304, Cache-Control,
    écritures conditionnelles If-Match/412 ; rate limiting token bucket/429/Retry-After, Problem Details RFC 9457,
    PATCH JSON Merge/Patch, versioning & dépréciation Sunset) + quiz `rest-apis-expert` + « Pour aller plus loin ».
    **4 → 6 leçons, 0 régression**.
  - ✅ **12/26 — Styles d'API** : +2 leçons expert (contract-first, compatibilité protobuf par numéro de champ,
    évolution GraphQL & @deprecated, contract testing Pact, sécurité GraphQL depth/complexity/persisted queries ;
    APIs asynchrones, webhooks signés HMAC + retries + idempotence, AsyncAPI) + quiz `api-styles-expert` +
    « Pour aller plus loin ». **3 → 5 leçons, 0 régression**.
  - ✅ **13/26 — Authentification** : +2 leçons expert (autorisation RBAC/ABAC/ReBAC, rôles vs scopes vs
    permissions, IDOR/BOLA, policy engines OPA/Cedar ; validation JWT iss/aud/exp, attaque confusion
    d'algorithme, rotation JWKS/kid, refresh rotation & reuse detection, passkeys/WebAuthn/TOTP) +
    quiz `authentication-expert` + « Pour aller plus loin » (OWASP, RFC 8725/9700). **4 → 6 leçons, 0 régression**.
  - ✅ **14/26 — Sécurité web** : +2 leçons expert (failles serveur SSRF/désérialisation/XXE/mass assignment/
    path traversal + fil rouge donnée-vs-instruction ; supply chain SCA/SBOM/Log4Shell, ne pas réinventer la
    crypto AES-GCM/SecureRandom/KMS, SAST/DAST/threat modeling STRIDE) + quiz `web-security-expert` +
    « Pour aller plus loin » (OWASP ASVS, SLSA). **4 → 6 leçons, 0 régression**.
  - ✅ **15/26 — Caching** : +2 leçons expert (cohérence & races d'invalidation : ordre écriture base→suppression,
    delayed double delete, clés versionnées/immuables ; pathologies stampede/penetration/avalanche, Redis en prod :
    éviction LRU/persistance RDB-AOF/hot & big keys) + quiz `caching-expert` + « Pour aller plus loin ».
    **3 → 5 leçons, 0 régression**.
  - ✅ **16/26 — Tests** : +2 leçons expert (la couverture ment, mutation testing PIT, property-based jqwik,
    taxonomie des doubles London vs classiciste ; tester le réactif StepVerifier/temps virtuel, test data builders,
    tuer les flaky) + quiz `testing-expert` + « Pour aller plus loin ». **4 → 6 leçons, 0 régression**.
  - ✅ **17/26 — CI/CD** : +2 leçons expert (build-once-deploy-many, cache/parallélisme/fail fast, migrations
    expand/contract, métriques DORA ; sécuriser la chaîne : OIDC vs clés longues, poisoned pipeline, signature
    Sigstore/SLSA, GitOps ArgoCD, séparer build et deploy) + quiz `ci-cd-expert` + « Pour aller plus loin ».
    **2 → 4 leçons, 0 régression**.
  - ✅ **18/26 — Principes de conception** : +2 leçons expert (SOLID + DIP/hexagonal, DRY/KISS/YAGNI, composition,
    Demeter, couplage/cohésion ; code smells god object/feature envy/primitive obsession, anemic domain model,
    connascence, refactorer vers un pattern) + quiz `design-principles-expert` + « Pour aller plus loin ».
    **4 → 6 leçons, 0 régression**.
  - ✅ **19/26 — Patterns d'architecture** : +2 leçons expert (résilience : timeout/retry backoff+jitter/circuit
    breaker/bulkhead/fallback/backpressure, Resilience4j ; communication sync vs async, orchestration vs
    chorégraphie, API gateway/BFF, migration strangler fig) + quiz `architectural-patterns-expert` +
    « Pour aller plus loin » (Newman, Nygard, Richardson). **4 → 6 leçons, 0 régression**.
  - ✅ **20/26 — Message brokers** : +2 leçons expert (Kafka en profondeur : partitions/parallélisme/ordre par
    clé, consumer groups & rebalancing, offsets & moment du commit = sémantique, rétention/compaction/schema
    registry ; messagerie fiable : dual-write → outbox/CDC Debezium, exactly-once vs at-least-once+idempotence,
    consumer lag & poison messages) + quiz `message-brokers-expert` + « Pour aller plus loin ». **3 → 5 leçons, 0 régression**.
  - ✅ **21/26 — Conteneurs & orchestration** : +2 leçons expert (internals namespaces/cgroups/union fs, ordre
    des couches/cache, PID 1 & SIGTERM/arrêt gracieux, distroless/scan Trivy/cosign/non-root ; Kubernetes essentiel
    Pod/Deployment/Service/ConfigMap, liveness vs readiness, requests/limits & OOMKill, HPA) +
    quiz `containerization-expert` + « Pour aller plus loin ». **3 → 5 leçons, 0 régression**.
  - ✅ **22/26 — Serveurs web** : +2 leçons expert (L4 vs L7, algorithmes LB round-robin/least-conn/ip-hash/
    consistent, modèle événementiel vs thread-par-connexion & problème C10k, keep-alive/pool upstream/timeouts ;
    proxy comme couche perf/sécu : TLS terminaison/passthrough/mTLS/OCSP/HTTP2-3, compression, cache edge,
    rate limiting, buffering clients lents, reload gracieux) + quiz `web-servers-expert`. **3 → 5 leçons, 0 régression**.
  - ✅ **23/26 — Building for scale** : +2 leçons expert (ingénierie de la capacité : tail latency P99, loi de
    Little, queue-based load leveling, tests de charge k6/Gatling & performance budgets ; échelle mondiale :
    sans-état, multi-région/CDN/data locality, autoscaling réactif/prédictif/scale-to-zero, chaos engineering &
    architecture cellulaire) + quiz `building-for-scale-expert` + « Pour aller plus loin ». **2 → 4 leçons, 0 régression**.
  - ✅ **24/26 — Observabilité** : +2 leçons expert (tracing distribué : propagation W3C traceparent, head/tail
    sampling, piège réactif MDC→Reactor Context, cardinalité des métriques & exemplars ; SLO en pratique :
    SLI/SLO/SLA, error budget policy, burn-rate multi-fenêtres, postmortems sans blâme) +
    quiz `observability-expert` + « Pour aller plus loin » (Google SRE). **3 → 5 leçons, 0 régression**.
  - ✅ **25/26 — Temps réel** : +2 leçons expert (fiabilité : reprise Last-Event-ID/journal+curseur, ordre &
    idempotence, backpressure vers clients lents, heartbeats ping/pong ; scaler le fan-out : backplane pub/sub,
    fan-out on write/read, présence, config LB WebSocket, WebRTC/MQTT/gRPC streaming) + quiz `real-time-expert` +
    « Pour aller plus loin ». **3 → 5 leçons, 0 régression**.
  - ✅ **26/26 — Moteurs de recherche** : +2 leçons expert (Elasticsearch : shards/replicas, near-real-time/refresh,
    query vs filter context, pagination profonde search_after, reindex par alias ; pertinence avancée : boosting/
    function_score, autocomplete edge n-grams, fuzzy, recherche vectorielle/kNN, hybride BM25+vecteurs, RAG) +
    quiz `search-engines-expert` + « Pour aller plus loin ». **3 → 5 leçons, 0 régression**.
  - Ensuite : appliquer le même approfondissement aux 9 autres tracks.

- ✅ **SF-48 — Couche « expert » Kubernetes (comble l'audit, sans régression)** — **TERMINÉ (20/20)**
  - **Bilan** : les 20 nœuds Kubernetes enrichis d'une couche experte comblant tous les manques de l'audit senior.
    **41 → 67 leçons** (+26), **25 → 51 quiz** (+26 quiz expert). **0 régression** vérifiée en base à chaque lot
    (append de leçons, upsert par `(nœud, slug)` → identifiants + progression apprenants préservés).
    **Certifications cka/ckad/kcna intactes** (5 nœuds chacune).
  - **Manques comblés par nœud** : Borg/CNCF/platform engineering/FinOps (what-is-kubernetes) · **API machinery**
    informers/resourceVersion/server-side apply + finalizers/ownerRefs/**chaîne d'admission** (key-concepts) ·
    kubectl explain-dry-run-diff + **Kustomize** (getting-started) · **arrêt gracieux** preStop/course
    endpoint-SIGTERM + **ephemeral containers**/kubectl debug/sidecars natifs/probes (pods) · Recreate/rollout
    bloqué/replicas↔HPA (deployments) · StatefulSet partition/CronJob concurrencyPolicy (workloads-advanced) ·
    **externalTrafficPolicy/IP source** + headless/EndpointSlices (services) · **Gateway API/cert-manager**/eBPF
    (ingress-networking) · propagation de config/checksum-Reloader (config-secrets) · **throttling CPU CFS** +
    **node allocatable** (resource-management) · can-i/rôles agrégés/**workload identity** (rbac) · **4C**/
    kube-bench/audit logging (cluster-security) · **kube-state-metrics** vs metrics-server/events/OTel
    (observability) · **HPA v2 behavior**/KEDA/Karpenter (autoscaling) · required-vs-preferred/taints auto/
    descheduler (scheduling) · **WaitForFirstConsumer**/reclaimPolicy + expansion/snapshots (storage-volumes) ·
    **progressive delivery** Argo Rollouts/feature flags (deployment-strategies) · Kustomize-vs-Helm/hooks/
    **secrets en GitOps** (helm-gitops) · **admission webhooks** + **construire un operator** (extending-kubernetes) ·
    **Velero/DR** + cordon-drain/spot + **playbook de dépannage** (cluster-operations). Chaque nœud finit par un
    quiz expert dédié.
  - Le parcours Kubernetes est désormais réellement « 0 → expert » sur les 20 nœuds, aligné sur l'audit publié.

- ✅ **SF-57 — Accessibilité débutant du parcours Software Architect (rewrite + diagrammes + défis)** — **TERMINÉ**
  - Les **23 nœuds** rendus accessibles à un vrai débutant sans perdre la profondeur experte : chaque
    terme/acronyme **défini à sa 1re apparition** + analogie `👉` + exemple concret. Réalisé via le
    **workflow multi-agent** (3 fournées parallèles), validation + ingestion append/upsert.
  - **Ajouts** : **24 diagrammes MERMAID** (≥ 1 par nœud sur le concept abstrait) et **46 défis de
    raisonnement** (le parcours n'en avait **aucun** ; dossier `challenges/` créé).
  - Les quiz par leçon existaient déjà (58 leçons / 58 quiz, SF-53/54) : **non dupliqués**.
  - **0 régression** (slugs, quizRef, sections et progression préservés ; autres parcours inchangés).
  - Runbook réutilisé : `docs/RUNBOOK-refonte-backend.md` (adapté par script au parcours archi).

- ✅ **SF-82 — Images pédagogiques DANS les leçons (type section IMAGE) — PARCOURS BACKEND TERMINÉ**
  - **Moteur IMAGE (fait, 4 couches)** : schéma content-pipeline (`node-content.schema.json` enum +IMAGE),
    enum backend `SectionType.IMAGE`, migration `V11__section_image_type.sql` (CHECK +IMAGE), rendu
    frontend (`SectionView` case IMAGE -> `<figure><img><figcaption>`, `SECTION_KIND.IMAGE`, CSS `.figure`).
    `body` = chemin image, `title` = légende. Images exclues du sommaire (TOC). Backend rebuildé (V11 OK).
  - **Placement** : UNE image APRES chaque concept (concept -> image -> concept -> image), pour que
    l'apprenant lise la notion puis voie l'illustration (et PAS groupées avant le quiz). La légende fait
    le lien image <-> notion.
  - **Règle** : UNE image par concept (pas de plafond). Placement : après CHAQUE concept.
  - **Pipeline réutilisable** : `content-pipeline/insert-lesson-images.mjs` (générique : node.json +
    captions.json -> insère 1 image après chaque concept, idempotent). Sourcing via fetch Pexels
    same-origin (navigateur) -> IDs -> download CDN. Données par nœud (requête+légende/concept).
  - **Faits** :
    - kubernetes/pods (5 leçons, 10 images) — vérifié.
    - **backend/internet** (8 leçons, 32 concepts, **32 images, 1/concept**) — re-ingéré, vérifié
      (ordre concept->image, légendes pédagogiques, TOC propre, images servies HTTP 200).
  - **PARCOURS BACKEND 100% FAIT** : 26 nœuds, 153 leçons, **480 concepts -> 480 images** (1/concept),
    0 leçon sans image. Vérifié (internet, pods, java : ordre concept->image, légendes, HTTP 200).
    490 images dans `frontend/public/images/lessons/` (480 backend + 10 pods). Fichiers de données
    (requête+légende par concept) : `scratchpad/images/<node>.json`.
  - **PARCOURS KUBERNETES 100% FAIT** : 20 nœuds, **164 concepts -> 164 images** (1/concept), 0 leçon
    sans image. Sourcing navigateur Pexels (sans clé API) par lots de 3 nœuds, commit par lot, 0 régression.
    Validé + ré-ingéré + frontend rebuildé + vérifié en direct (leçon `le-probleme-a-lechelle` : 2 images
    1280px chargées, légendes, asset HTTP 200).
  - **PARCOURS DOCKER 100% FAIT** : 16 nœuds, **154 concepts -> 154 images** (1/concept), 0 leçon sans image.
    Sourcing bascule sur **`curl`** (extension navigateur trop instable) : `curl` sur `pexels.com/search`
    extrait les IDs photos, download via le CDN `images.pexels.com` — fiable, sans clé API. Lots de 2-3 nœuds,
    commit par lot, 0 régression. Validé + ingéré + frontend rebuildé + assets vérifiés HTTP 200.
    **797 images** au total dans `frontend/public/images/lessons/`.
  - **TOUS LES PARCOURS ILLUSTRÉS** ✅ (via agents parallèles + pipeline curl, consolidation par vagues) :
    backend (480), kubernetes (164), docker (154), dapr (130 + couverture), puis vague 1 (demo, cka, ckad,
    kcna, rust, prompt-engineering, api-security, backend-performance, aws), terraform (113),
    software-architect (175), software-design (259), java-17-21 (369). Règle anti-collision : un slug de
    leçon partagé entre parcours (ex. `les-trois-piliers`, `reseau-du-cluster`, `en-pratique`) réutilise
    l'image existante au lieu de l'écraser. Chaque parcours validé + ingéré + frontend rebuildé + poussé.
  - NB volume/poids : repo devenu lourd (~1900 images, images+historique ~600 Mo) mais sous la limite GitHub.
  - Fichiers moteur : `content-pipeline/schemas/node-content.schema.json`, `SectionType.java`,
    `IngestNodeContentService.java`, `app/.../V11__section_image_type.sql`, `LessonPage.tsx` (+ `.module.css`).

- ✅ **SF-90 — Nouveau parcours Dapr (roadmap + contenu complet)** — **TERMINÉ**
  - **Roadmap** : `content/dapr/roadmap.json` (kind TRACK, position 13) : **20 nœuds, 6 phases**
    (Fondations, Démarrer, Building blocks essentiels, Building blocks avancés, Résilience/sécurité/
    observabilité, Déploiement & production), **24 arêtes** de prérequis (graphe cohérent).
  - **Contenu** : 20 nœuds, **60 leçons**, **60 quiz** (~160 concepts) avec sections CONCEPT/CODE/DIAGRAM
    (Mermaid)/QUIZ. Couvre sidecar, building blocks (invocation, état, pub/sub, bindings, acteurs, secrets,
    config, workflow), résilience, sécurité, observabilité, Kubernetes, prod, patterns.
  - **Méthode (multi-agents)** : 1 gabarit de référence écrit à la main (`dapr-overview`), puis **19 agents
    en parallèle** (un par nœud) ont rédigé le reste en copiant le gabarit. Consolidation : sweep anti-tiret
    cadratin (titres de quiz normalisés en « Quiz : … »), `validate --only dapr` **vert du 1er coup**,
    `ingest --only dapr` OK. Vérifié API : Dapr au catalogue, 20 nœuds / 6 phases / 24 arêtes.
  - **Images** : ✅ **130 concepts illustrés** (une image après chaque concept), via **5 agents parallèles**
    (sourcing curl Pexels + `insert-lesson-images.mjs`), 0 échec. `les-trois-piliers` (observability) réutilise
    les 2 images partagées existantes (même thème). Ré-ingéré + frontend rebuildé + assets vérifiés HTTP 200.
    **925 images** au total dans `frontend/public/images/lessons/`.

- ✅ **Maintenance — Tests `iam` + préparation mise en public (MIT)** — **TERMINÉ**
  - Correctif : `AuthIntegrationTest` (module `iam`) ne chargeait plus le contexte depuis l'ajout du
    « mot de passe oublié » — `JavaMailPasswordResetMailer` exige un bean `JavaMailSender` absent du
    contexte de test. Ajout d'un **hôte mail fictif** dans `iam/src/test/resources/application.yml`
    (aucun email n'est envoyé par ces tests). Build complet re-vérifié **vert** (10 modules, 0 échec).
  - Mise en public : ajout **`LICENSE` (MIT)**, **`.gitattributes`** (`text=auto`, images en binaire),
    section **Licence** + note « identifiants de démo = instance locale uniquement » dans le README.
  - Dépôt propre : aucun secret suivi, `.env` jamais commité, plus gros fichier 964 Ko. **Aucun remote** encore.

- 🔄 **SF-85 — Barre latérale en sections + hubs réels** — *1er item : « Mes parcours »*
  - Constat (validé avec le PO) : sidebar trop maigre (Accueil/Catalogue/Profil) ; des fonctionnalités
    réelles (parcours en cours, examens blancs, certificats) n'avaient **aucune porte d'entrée** dans la nav.
  - Découpage validé : **SF-85** Mes parcours (front), **SF-86** Certificats (front), **SF-87** Examens
    blancs (backend `evaluation` : nouvel endpoint `GET /exams` + front). Ordre choisi pour **zéro lien mort**
    à chaque incrément et isoler le seul item backend en dernier.
  - ✅ **SF-85 — Sidebar en sections + hub « Mes parcours »** (front seul)
    - Nav réorganisée en **groupes séparés par un filet** : pour l'instant `Accueil/Catalogue/Mes parcours`
      · `Profil/(Admin)` (un seul filet — un item seul entre deux filets serait déséquilibré). Le groupe
      « apprentissage » (Mes parcours / Examens / Certificats) sera isolé quand SF-86/87 l'étofferont.
    - Indicateur de sélection glissant refait : on **mesure** la position du lien actif (`--ind-top`/`--ind-h`
      via `useLayoutEffect`) au lieu d'un index × pas fixe -> robuste aux séparateurs et au nombre d'items.
      Suppression du `@property --active-index` (devenu mort).
    - Nouvelle page **`/mes-parcours`** (`MyPathsPage`) : sections **En cours** / **Terminés**, cartes avec
      couverture, anneau de progression, reprise (`Continuer`/`Commencer`) ou `Voir mon certificat`.
      Données réelles via `listEnrollments()` + `listRoadmaps()` (aucun backend ajouté). Route marquée **pair**
      (transitions façon onglets). Nouvelle icône `IconPaths` (outline + variante pleine à l'état actif).
    - Vérifié en direct (compte apprenti de démo) : nav Accueil/Catalogue│Mes parcours│Profil, indicateur qui
      glisse, page « En cours » avec 2 parcours. Fichiers : `AppLayout.tsx` (+ `.module.css`), `MyPathsPage.tsx`
      (+ `.module.css`), `App.tsx`, `icons.tsx`, `global.css`.
  - ⏳ SF-86 (Certificats), SF-87 (Examens blancs) : non commencés.

- ✅ **SF-84 — Refonte de la page de connexion (layout 2 colonnes + image)** — **TERMINÉ**
  - Analyse : page de connexion correcte mais plate / en retrait de l'identité du produit.
  - `AuthPage` : layout **2 colonnes** (formulaire à gauche, **panneau image hero** monochrome + voile
    + accroche à droite ; masqué < 900px), **logo** (hexagone) au-dessus du titre, séparateur +
    bouton **« Créer un compte »** (ghost) distinct du CTA primaire.
  - `LoginForm` : **afficher/masquer le mot de passe**. *(Affiné ensuite : œil **intégré au champ** via le
    composant `Input`, donc actif sur TOUS les champs mot de passe — connexion, inscription, changement, reset.)*
  - Vérifié en direct. Fichiers : `AuthPage.tsx` (+ `.module.css`), `LoginForm.tsx`, `Input.tsx`/`Input.module.css`, `icons.tsx`.

- ✅ **SF-83 — Hero d'accueil (image de bienvenue façon Apple) sur le dashboard** — **TERMINÉ**
  - Recherche : principe hero Apple (grande image forte + titre + accroche, pleine largeur).
  - Image Pexels choisie (MacBook + code, lumière douce, paysage) -> `frontend/public/images/hero-home.jpg`
    (2200px). Traitée en **monochrome** (grayscale) pour l'identité, voile dégradé pour la lisibilité.
  - `DashboardPage` : section `hero` (image + scrim + « Bonjour <nom> » + accroche superposés),
    coins arrondis, bordure, cohérente avec les cartes. Remplace le simple `<h1>`.
  - Vérifié en direct. Fichiers : `DashboardPage.tsx` (+ `.module.css`), `hero-home.jpg`, `credits.json`.

- ✅ **SF-81 — Images dans les parcours (bannières page parcours + page nœud)** — **TERMINÉ**
  - `TrackCover` : variante `tall` (héro 240px) ajoutée.
  - **Page parcours** (`RoadmapPage`) : bannière HÉRO (slot `media` de la Card d'en-tête), slug direct.
  - **Page nœud** (`NodePage`) : bannière (slot `media`) ; slug du parcours récupéré via `listRoadmaps`
    (map `roadmapId` -> slug) ajouté a `loadNode`. Repli propre si pas d'image.
  - Même traitement monochrome + révélation au survol. Vérifié en direct (Kubernetes : héro conteneurs
    sur la page parcours ; bannière sur « Qu'est-ce que Kubernetes »).
  - NB : les **illustrations DANS le contenu des leçons** (une image par notion) ne sont PAS faites —
    ça demande un sourcing par leçon (des centaines) ; a cadrer séparément si souhaité.
  - Fichiers : `TrackCover.tsx` (+ `.module.css`), `RoadmapPage.tsx`, `NodePage.tsx`.

- ✅ **SF-80 — Couvertures de parcours affichées dans les cartes (traitement monochrome)** — **TERMINÉ**
  - Composant `TrackCover` : bandeau pleine largeur `/images/tracks/<slug>.jpg`, `object-fit: cover`,
    `height: 150px`, **filtre `grayscale(1)` par défaut** (cohérent avec l'identité sobre), repli
    propre si l'image manque (`onError` -> rien). Slot `media` ajouté au composant `Card`.
  - Survol de carte : **révélation couleur + léger zoom** (`.sf-lift:hover [data-cover] img` ->
    `grayscale(0.2) scale(1.04)`). Clin d'œil premium.
  - Branché dans le **catalogue** et le **dashboard** (parcours + à découvrir).
  - Ajout de la couverture manquante `decouverte.jpg` (thème backend) -> **17 cartes / 17 couvertures,
    0 cassée** (vérifié en direct). Images servies en HTTP 200 (copiées par Vite depuis `public/`).
  - Fichiers : `TrackCover.tsx` (+ `.module.css`), `Card.tsx`, `global.css`, `DashboardPage.tsx`,
    `CataloguePage.tsx`, `frontend/public/images/tracks/decouverte.jpg`.

- ✅ **SF-79 — Banque d'images (couvertures de parcours, Pexels gratuit)** — **TERMINÉ**
  - Recherche plateformes : Pexels retenu (commercial libre, SANS attribution, download +
    auto-hébergement autorisés ; Unsplash exige hotlinking -> écarté).
  - Outil `content-pipeline/fetch-images.mjs` (API Pexels via `PEXELS_API_KEY`, idempotent, crédits) —
    prêt pour re-générer proprement avec une clé.
  - **16 couvertures téléchargées** (1 par parcours) vers `frontend/public/images/tracks/<slug>.jpg`,
    haute résolution (1880px, ~140–600 Ko, total 5.6 Mo). Sans clé API : IDs récupérés via le site
    (fetch same-origin) puis download depuis le CDN public Pexels. `credits.json` écrit (bonne pratique).
  - ⚠️ Images = **premier résultat** par requête thématique -> à **relire/échanger** au besoin
    (re-pick possible). **Pas encore branchées dans l'UI** (étape suivante, avec traitement
    monochrome/duotone pour ne pas jurer avec l'identité sobre).
  - Fichiers : `content-pipeline/fetch-images.mjs`, `frontend/public/images/tracks/*`.

- ✅ **SF-78 — Colonne de leçon : rail + sommaire fixes sous le header, scroll interne** — **TERMINÉ**
  - Rail de curriculum et sommaire (TOC) étaient `sticky top: var(--space-4)` -> passaient SOUS le
    header fixe. Recalés à `top: calc(var(--header-h) + var(--space-4))`, `max-height` ajustée ;
    `--header-h: 60px` ajouté dans `tokens.css`. `scroll-margin-top` des sections + barre de lecture
    (`.readbar`) également calés sous le header (clic sommaire = section visible, pas masquée).
  - **Bloqueur corrigé** : `.main { overflow-x: hidden }` transformait `.main` en conteneur de scroll
    (overflow-y auto implicite) et **cassait le sticky** du rail/sommaire -> remplacé par
    `overflow-x: clip` (clippe sans créer de conteneur de scroll).
  - Vérifié : après scroll 800px, rail et sommaire collés a `top:76px` (header 60 + 16), scroll interne ;
    l'article défile. Fichiers : `tokens.css`, `AppLayout.module.css`, `LessonPage.module.css`.

- ✅ **SF-77 — App shell : sidebar + header fixes, seul le contenu défile** — **TERMINÉ**
  - Avant : la fenêtre entière défilait -> la barre latérale partait vers le haut avec le contenu.
  - Fix minimal (sans changer le conteneur de scroll = pas d'effet de bord sur la hairline/barre de
    lecture qui lisent `window.scrollY`) : `.sidebar` -> `position: sticky; top: 0; height: 100vh;
    align-self: start; overflow-y: auto`. Le header était déjà `sticky`.
  - Mobile (barre horizontale) : override `height: auto; overflow: visible` (pas de 100vh).
  - Vérifié : après scroll 600px, sidebar et header restent a `top:0`, le document scrolle (contenu
    seul visible en mouvement). Fichier : `AppLayout.module.css`.

- ✅ **SF-76 — Présentation du contenu des cours alignée Apple (traitement éditorial)** — **TERMINÉ**
  - Recherche : Apple optimise la longueur de ligne (~66 car.), hiérarchie typo forte, macro-whitespace
    généreux (plus entre sections qu'à l'intérieur), prose qui COULE (seuls les blocs spéciaux distingués).
  - **Mesure de lecture** : prose (`.paragraph`/`.list`/`.sectionTitle`) cappée a `--reading-width`
    (68ch) -> **~67 caractères/ligne** (avant ~85-90 en colonne 760px). Interligne 1.75 -> **1.7**.
  - **Prose qui coule** : sections « concept » **sans carte/bordure/barre colorée** ; conteneur léger
    réservé aux blocs interactifs (exercice, quiz) ; code/schéma/callout portés par leur composant.
  - **Hiérarchie** : titres de section `text-lg` -> **`text-xl` (1.44rem) / 700** (23px vs 17px corps).
  - **Rythme** : espace entre sections `.content` `space-4` -> **`space-7` (40px)**.
  - **Eyebrows discrets** : pastille colorée par bloc -> petit intertitre muted en MAJUSCULES, et
    **supprimé sur la prose « concept »** (gardé pour Code/Schéma/Exercice/Quiz/À retenir).
  - Vérifié en direct (leçon java) : CPL 67, titres 23px/700, concept non encadré, gap 40px.
  - Fichiers : `LessonPage.module.css`, `LessonPage.tsx`.

- ✅ **SF-75 — Accent couleur aligné Apple (itéré : acier -> Noir Apple #1d1d1f)** — **TERMINÉ**
  - Objectif : donner un accent fonctionnel (boutons, liens, états actifs, barres, focus) aligné Apple.
  - 1ère tentative **acier** (`#45586c`) **rejetée par le PO** (trop coloré).
  - Choix final : **Noir Apple officiel `#1d1d1f`** (RGB 29,29,31), la vraie valeur d'UI d'Apple
    (vérifiée : apple.com/labels). Neutre, sans teinte.
    - Clair : `--color-primary #1d1d1f`, hover `#000`, soft `#ececef`, focus `#1d1d1f`.
    - Sombre : accent quasi-blanc `#f3f4f7` (boutons clairs sur fond sombre, façon Apple), focus idem.
  - Liens (`a`) pointés sur l'accent. Voiles d'ambiance : gris neutres (pas de teinte).
  - Contraste blanc/`#1d1d1f` ~ AAA. Sémantiques et neutres Apple inchangés.
  - NB décision d'identité : accent **neutre noir** (pas un accent coloré vif comme le bleu système
    d'Apple) -> aligné sur la FONDATION et la sobriété Apple, accent chromatique volontairement écarté.
  - Fichiers : `tokens.css`, `global.css`.

- ✅ **SF-74 — Cohérence de la palette (audit Apple + corrections mono)** — **TERMINÉ**
  - Audit vs système de couleurs Apple : **neutres = exactement les valeurs Apple** (`#f5f5f7`,
    `#1d1d1f`, `#6e6e73`, `#d2d2d7`) ✅ ; accent **quasi-noir mono** = choix assumé (Apple utilise un
    accent coloré fonctionnel, bleu — écarté comme « trop IA »).
  - Deux incohérences corrigées pour tenir la direction mono :
    1. **Focus** : `#3b6fd6` (bleu) -> `#1d1d20` clair / `#f3f4f7` sombre (l'accent, mono). Visible via
       l'offset d'outline, respecte `:focus-visible`.
    2. **Voiles d'ambiance** : bleu/violet/orange (`#778eff`/`#b68cff`…, le « AI » caché) -> **gris
       neutres** purs, clair + sombre (gardent la matière du verre sans teinte).
  - Sémantiques (vert/orange/rouge sobres) conservées volontairement (cohérentes avec le mono ;
    plus ternes que les System Colors vives d'Apple, mais c'est voulu).
  - Fichiers : `tokens.css`.

- ✅ **SF-73 — Transitions de navigation « façon Apple » (View Transitions API)** — **TERMINÉ**
  - Choix PO : **contextuel** — fondu enchaîné entre sections pairs de la sidebar, **push latéral**
    (droite→gauche) en descente liste→détail, **push inversé** au retour (bouton navigateur / remontée).
  - `App.tsx` : `AnimatedRoutes` avec `useState(displayed)` + `useLocation`/`useNavigationType` ;
    `document.startViewTransition(() => flushSync(commit))`, sens posé via `data-vt` sur `<html>`
    (`fade`/`forward`/`back`, calculé par profondeur de route + type POP). Effet dépendant de la
    SEULE `location` (refs pour éviter la ré-entrée qui avortait la transition).
  - `.main` porte `view-transition-name: page` : seul le CONTENU glisse/fond, le chrome reste fixe.
  - `global.css` : `::view-transition-old/new(page)` par sens + keyframes ; cascade rendue instantanée
    pendant une transition (`:root[data-vt] .sf-stagger > *`), donc l'écran poussé apparaît complet.
  - Remplace l'ancien `sf-route` (fondu+glissement générique, rejeté). Repli gracieux si API absente
    ou `prefers-reduced-motion` (navigation instantanée).
  - Vérifié : directions correctes (`data-vt` = fade/forward/back selon la nav), `.main` nommé `page`,
    navigation + rendu OK, aucune régression. NB : l'ANIMATION n'est pas observable en automatisation
    (l'onglet piloté est `visibilityState:"hidden"`, et la View Transitions API s'avorte sur document
    caché — « invalid state ») ; elle se joue normalement avec l'onglet au premier plan.
  - Fichiers : `App.tsx`, `AppLayout.tsx` (+ `.module.css`), `global.css`.

- ✅ **SF-72 — Survol des boutons + animations des listes dans le cours** — **TERMINÉ**
  - **Boutons** (Reprendre, Commencer, Continuer, Découvrir, Voir mon certificat... tous via `Button`) :
    ajout d'un **survol** (lévitation `translateY(-1px)`, défini avant `:active` pour que l'appui
    l'emporte). Press (`scale(0.97)`) déjà présent. Règle confirmée dans le CSS compilé.
  - **Listes dans le cours (NodePage)** : lignes de leçon et de défi -> `transition` fluide sur le
    survol (fond/bordure) + retour tactile `sf-pressable` ; liste des défis en cascade `sf-stagger`
    (les leçons l'avaient déjà). Le rail de leçons (LessonPage) était déjà couvert.
  - Fichiers : `Button.module.css`, `NodePage.tsx` (+ `.module.css`).

- ✅ **SF-71 — Interactions header : menu profil au survol (glass) + aperçu de la barre XP** — **TERMINÉ**
  - **Menu profil au survol** : ouverture au `mouseenter` (avec délai de fermeture de 180ms pour
    pouvoir entrer dans le menu), apparition « liquid glass » (verre déjà en place + keyframe `menuIn`
    scale + fondu ressort, ancrée en haut a droite). Le clic reste disponible (tactile).
  - **Aperçu de la barre XP au survol** : le remplissage avance jusqu'a 100% (niveau suivant) puis
    revient a sa valeur (`@keyframes xpPreview` sur `.pill:hover .fill`, piloté par `--sf-p = ${percent}%`).
  - Vérifié en direct (menu ouvert au survol, largeur de barre 20% -> montée -> retour). Neutralisé
    par `prefers-reduced-motion`. Fichiers : `Header.tsx`, `Header.module.css`.

- ✅ **SF-70 — Enrichissement du mouvement (entrée de page, cascade ressort, retour tactile étendu)** — **TERMINÉ**
  - Cascade des listes passée en ressort (`sf-rise` : montée + échelle, `--ease-spring`, `--dur-slow`),
    étendue à Profil (tuiles + badges), Roadmap (nœuds) et Node (leçons).
  - Retour tactile `sf-pressable` (`:active` → `scale(0.97)`) étendu à la nav latérale et au rail de leçons ;
    appui bouton passé de décalage à enfoncement.
  - Entrée de page conservée (fondu `sf-route` à chaque navigation). **MAJ (SF-72+) : `sf-route`
    passé de fondu seul a `sf-page` = fondu + glissement vers le haut (12px), `backwards` pour relâcher
    le transform a la fin -> transition de REDIRECTION nette sur toutes les pages (y compris celles sans
    liste). Résultat final vérifié visuellement (page affichée correctement, opacité 1).
  - **Couverture étendue (survol + clic partout)** : utilitaire de survol `.sf-lift` (lévitation
    -2px + ombre `--shadow-md`) sur les cartes de parcours (dashboard + catalogue), les certificats
    (profil) et les nœuds de parcours ; `.sf-pressable` ajouté au déclencheur avatar (header), au
    bouton de recherche (sidebar), aux lignes de la palette de commandes et aux options de quiz.
  - Correctif : `.sf-stagger > *` passé de `both` a **`backwards`** (sinon le transform figé en fin
    d'animation d'entrée écrasait la lévitation au survol).
  - **Indicateur de sélection GLISSANT dans la sidebar : REMIS et fonctionnel.** Un `<span>` absolu
    positionné par `top: calc(var(--active-index) * 39px)`, `--active-index` posé DIRECTEMENT sur
    l'élément animé (clé du correctif) et enregistré via `@property { syntax: '<number>' }` pour être
    animable ; la transition porte sur la variable (`--ease-spring`). L'item actif a un fond
    transparent (c'est l'indicateur qui fournit le fond) ; sur mobile (barre horizontale) l'indicateur
    est masqué et l'actif reprend un fond statique. Vérifié : positionnement correct sur chaque page
    (Accueil/Catalogue/Profil) et mise à jour au clic.
    Note : la mesure de la transition via automatisation est peu fiable (getComputedStyle pendant une
    transition de custom property), mais le positionnement final est juste.
  - Fichiers : `global.css`, `AppLayout.tsx` (+ `.module.css`), `ProfilePage.tsx`, `RoadmapPage.tsx`,
    `NodePage.tsx`, `LessonPage.tsx`, `Button.module.css`.

- ✅ **SF-69 — Système de mouvement cohérent « tout répond » (façon Apple)** — **TERMINÉ**
  - Principe Apple : ce n'est pas le NOMBRE d'animations, c'est que CHAQUE action répond avec une
    seule et même signature (les tokens de SF-67), brièvement et sans gratuité.
  - **Entrée de page** à chaque navigation : `<Outlet>` enveloppé d'un `<div key={pathname}
    class="sf-route">` (fondu `sf-fade`) dans `AppLayout`.
  - **Apparition en cascade** des listes (`sf-stagger` : enfants en `sf-rise` fondu+montée, délais
    échelonnés par `nth-child`) sur les tuiles + grilles du dashboard et les grilles du catalogue.
  - **Retour tactile universel** (`sf-pressable` : `:active` → `scale(0.97)`) sur boutons (enfoncement
    au lieu du décalage), chips de filtre, cartes de nœud, liens de certificat, liens de nav et items du rail.
  - Utilitaires globaux dans `global.css` ; tout neutralisé par `prefers-reduced-motion` (déjà en place).
  - Vérifié : tsc + Vite OK, navigation + états actifs (GIF `skillforge-motion.gif`).
  - Fichiers : `global.css`, `AppLayout.tsx`, `Button.module.css`, `NodeCard.tsx`, `DashboardPage.tsx`,
    `CataloguePage.tsx`, `ProfilePage.tsx`, `LessonPage.tsx`.

- ✅ **SF-68 — Hairline d'en-tête révélée au scroll (toolbar façon macOS)** — **TERMINÉ**
  - En haut de page, l'en-tête est **sans bordure** ; le filet fin + une ombre discrète
    **apparaissent quand le contenu défile dessous** (comportement toolbar macOS/Safari).
  - `Header.tsx` : état `scrolled` via un listener `scroll` passif (`window.scrollY > 2`), classe
    conditionnelle sur le `<header>`. `Header.module.css` : `border-bottom` transparent par défaut,
    `.scrolled` révèle `--glass-border` + `--shadow-sm`, transition `--dur-base`/`--ease-standard`.
  - Vérifié en DOM : haut de page → filet transparent, aucune ombre ; après scroll (611px) →
    header épinglé `top:0`, classe `scrolled`, filet + ombre révélés. tsc + Vite OK.
  - Clôt le volet « détail Apple » (SF-64 → SF-68).

- ✅ **SF-67 — Icônes & animations façon Apple (SF Symbols + Motion)** — **TERMINÉ**
  - **Tokens de mouvement** (`tokens.css`) : `--dur-fast` 0.15s, `--dur-base` 0.25s, `--dur-slow` 0.4s,
    `--ease-standard` (ease-in-out calibré) et `--ease-spring` (léger overshoot). Branchés partout
    (fin du `ease` nu et des durées éparpillées) : boutons, cartes, liens, inputs, chips, barres, thème.
  - **Entrées animées façon Apple** (scale + fondu avec ressort) : modale (`dialogIn`), onboarding
    (`tourIn`), toast (ressort). La barre de progression de lecture reste en `linear` (suit le scroll).
  - **Icônes** (`icons.tsx`) : trait allégé 2 → **1.75** (rendu plus fin, façon SF) ; helper `solid`
    + prop `filled` ; **variantes pleines à l'état actif** pour Accueil/Catalogue/Profil/Admin
    (câblées via le render-prop `isActive` de `NavLink` dans `AppLayout.tsx`) ; `IconMedal` épurée.
  - Respecte toujours `prefers-reduced-motion` (neutralisation globale déjà en place).
  - Vérifié : tsc + Vite OK, bascule outline↔filled sur la nav, zéro warning React en console.
  - Fichiers : `tokens.css`, `icons.tsx`, `AppLayout.tsx` (+ `.module.css`), `Modal`, `OnboardingTour`,
    `ToastViewport`, `Button`, `NodeCard`, `Input`, `Progress`, `Header`, `SidebarProgress`,
    `CataloguePage`, `NodePage`, `CodeBlock`, `RoadmapPage`, `LessonPage`, `global.css`.

- ✅ **SF-66 — Audit & harmonisation des deux barres latérales (métriques Apple)** — **TERMINÉ**
  - Audit détaillé (mesures live + source) des deux barres : nav principale (`AppLayout`) et rail de
    curriculum (`LessonPage`), croisé avec la page HIG « Sidebars » (lignes 24/28/32pt, 0 vertical).
  - **Densité unifiée** : lignes ~35px des deux côtés (nav passée de 42px → 35px via `text-sm` + line-height 1.35).
  - **Sélection tendue** : rayon des lignes `radius-md` (12px) → `radius-sm` (8px), nav + rail.
  - **Grille propre** : gap entre lignes unifié à 4px (`space-1`) ; gap icône/label 12px → 8px (`space-2`) ;
    suppression du `gap: 2px` off-grid du rail ; padding horizontal du rail 8px → 12px (plus proche des 17pt).
  - Recette commune finale : rayon 8px, padding 8/12, gap icône 8px, gap liste 4px, texte 14px.
  - Vérifié en direct (nav sur l'accueil, rail sur une leçon). Fichiers : `AppLayout.module.css`,
    `LessonPage.module.css`.

- ✅ **SF-65 — Barres & cadres façon Apple : verre réservé au chrome, contenu opaque** — **TERMINÉ**
  - Principe HIG Materials appliqué : **le verre (Liquid Glass) = chrome flottant** (barre latérale,
    barre du haut, menu, palette, modale) ; **le contenu = surfaces opaques**. Fin du verre-sur-verre.
  - Nouveau token `--card-shadow` (clair + sombre) : ombre ambiante douce pour les boîtes de contenu.
  - Passés en **opaque** (`--color-surface` + `--color-border` + `--card-shadow`, sans `backdrop-filter`) :
    `Card`, tuiles stats (Dashboard + Profil), bannière « Reprendre » (`ResumeBanner`).
  - Gardés en **verre** (chrome/flottant, correct) : sidebar + header (`AppLayout`, `Header`),
    menu utilisateur, `CommandPalette`, `OnboardingTour`, `Modal`.
  - Vérifié en clair ET sombre. Fichiers : `tokens.css`, `Card.module.css`, `DashboardPage.module.css`,
    `ProfilePage.module.css`, `ResumeBanner.module.css`.

- ✅ **SF-64 — Alignement visuel sur Apple (couleurs, formes, boutons)** — **TERMINÉ**
  - Comparaison menée (HIG Materials/Buttons + apple.com) sur couleurs / formes / boutons.
  - **Boutons en capsule pleine** (`border-radius: var(--radius-full)`) + padding horizontal plus
    généreux (md: `space-5`, sm: `space-4`), façon Apple (rayon = demi-hauteur).
  - **Neutres calés sur Apple** (thème clair) : fond `#f5f5f7`, surface-2 `#efeff2`,
    texte `#1d1d1f`, texte secondaire `#6e6e73`, bordure `#d2d2d7` (plus lisible qu'avant).
  - Accent **quasi-noir conservé** (le bleu « System Blue » d'Apple rejeté comme « trop IA »).
  - Coins continus (squircle) écartés : support navigateur encore trop limité.
  - Fichiers : `styles/tokens.css`, `components/Button.module.css`. Build Vite OK, vérifié front.

- ✅ **SF-63 — 9 améliorations UX « niveau plateforme » (Udemy/GitLab Learn/roadmap.sh)** — **TERMINÉ**
  1. **Éditeur de code interactif** (CodeMirror, hors-ligne) sur la page Défi (remplace le textarea).
  2. **Palette de recherche globale Cmd/Ctrl+K** (`CommandPalette`) : saut rapide vers parcours/nœuds + bouton
     « Rechercher » dans la barre latérale.
  3. **Tiroir « Leçons » sur mobile** (le rail de curriculum devient un tiroir sous 880px).
  4. **Bloc-notes par leçon** (`LessonNotes`, stockage local, autosave) ; version serveur = suivi.
  5. **Temps restant estimé** sur la page Nœud et sur la page Parcours (somme pondérée des `estimatedMinutes`).
  6. **Certificat de fin de parcours** (`/certificate/:slug`, généré côté client à 100 %, imprimable/PDF),
     accessible depuis le parcours et le tableau de bord.
  7. **Identité visuelle par parcours** (`TrackBadge` : pastille colorée + initiales) sur catalogue, parcours, dashboard.
  8. **Reprise au bon endroit dans la leçon** (position de défilement mémorisée par leçon).
  9. **Tour d'onboarding** à la 1re connexion (`OnboardingTour`, 4 étapes, affiché une fois).
  Frontend uniquement (aucune régression backend). Nouvelle dép : CodeMirror. Build tsc + Vite OK.

- ✅ **SF-62 — Correctif : blocs de code affichant « [object Object] »** — **TERMINÉ**
  - Bug global : `react-syntax-highlighter` (15.6.x puis 15.5.0) rendait les jetons comme objets
    (`[object Object]`) au lieu du code, dans toutes les leçons. Downgrade sans effet.
  - Remplacé par **Prism.js** en direct (rendu HTML déterministe) : `CodeBlock.tsx` réécrit, couleurs
    de jetons définies globalement (`.sf-prism .token.*`) via variables de thème clair/sombre (contraste
    WCAG AA), déclaration de types `prismjs.d.ts`. Vérifié visuellement (Chrome) sur leçons git et Java :
    code coloré correct, 0 `[object Object]`. (react-syntax-highlighter devenu inutilisé, à retirer du
    manifeste lors d'un prochain `npm install`.)

- ✅ **SF-61 — Gestion du mot de passe (changement + réinitialisation « mot de passe oublié »)** — **TERMINÉ**
  - **Brique 1** : changer son mot de passe connecté. `PATCH /api/v1/auth/password` (vérifie l'actuel),
    section « Sécurité » sur la page Profil.
  - **Brique 2** : « mot de passe oublié » self-service par e-mail. `POST /auth/password/forgot` (réponse
    neutre, jeton à usage unique haché SHA-256, expirant), `POST /auth/password/reset`. Lien « Mot de passe
    oublié » sur le login, pages `/forgot-password` et `/reset-password`. Conteneur **Mailpit** ajouté au
    docker-compose (SMTP 1025, interface web 8025) pour capter les e-mails en local.
  - Backend hexagonal (iam) : use cases `ChangePassword` / `RequestPasswordReset` / `ResetPassword`, ports
    `PasswordResetTokenRepository` + `PasswordResetMailer`, store R2DBC, adaptateur JavaMailSender
    (envoi non bloquant), migration **V10** (`password_reset_tokens`). Routes de reset publiques en sécurité.
    Tests unitaires des 3 services (changement, demande neutre, reset + jeton à usage unique). Build + tests OK.

- ✅ **SF-60 — Nouveau parcours « Java moderne : de Java 17 à Java 21 »** — **TERMINÉ**
  - Parcours créé de zéro, calqué sur la roadmap backend : **7 phases, 26 nœuds, 31 prérequis** (de l'installation du JDK
    jusqu'à l'atelier fil rouge). Couvre records, classes scellées, text blocks, switch expressions, pattern matching
    (instanceof, switch, record patterns), Sequenced Collections, Virtual Threads, concurrence structurée, Scoped Values,
    streams modernes, outils (JShell, build, tests, jlink/jpackage), GC/ZGC et la migration 17 vers 21.
  - **Bilan (base) : 26 nœuds / 134 leçons / 134 quiz (1 par leçon, 26/26) / 57 diagrammes MERMAID / 53 défis.**
  - Nœud `records` écrit à la main comme gabarit de référence, puis **25 nœuds générés via workflow multi-agent** (1 agent
    par nœud, 4 fournées), au standard débutant : chaque terme défini à sa 1re apparition + analogie `👉` + exemple concret.
    Validé (`15/15` puis track complet), ingéré via `/api/v1/roadmaps/import` + imports de contenu. Aucun tiret cadratin,
    aucun emoji hors 👉 (audit : 214 fichiers, 0 tiret, 0 emoji parasite). 0 régression sur les autres parcours.

- ✅ **SF-59 — Nettoyage des tirets cadratins (`—`/`–`) à la source sur TOUS les parcours** — **TERMINÉ**
  - Remplacement des `—`/`–` par virgule/deux-points (logique identique au `deEmDash` du front, style `" - "` dans
    le code) **directement dans les fichiers de contenu** des 15 parcours, plus ré-ingestion (upsert, sans `--reset`).
  - **2221 tirets** nettoyés (nodes 1611 · quizzes 597 · challenges 9 · roadmap 4) → **0 restant**, en fichiers **et** en base.
  - Script réutilisable `scratchpad/clean_dashes.py` (ne touche que les champs chaîne existants, n'ajoute aucune clé).
    Validation `15/15 parcours valides`, ingestion `15/15`, **0 régression** (slugs, quizRef, quizId, positions préservés).

- ✅ **SF-58 — Refonte « débutant → expert » de Docker, Kubernetes, Terraform, Software Design** — **TERMINÉ**
  - Les 4 parcours rendus accessibles à un vrai débutant au standard `git` : rewrite débutant (chaque terme
    **défini à sa 1re apparition** + analogie `👉` + exemple concret), **quiz sur chaque leçon** (créés là où
    ils manquaient), **≥ 1 diagramme MERMAID** par nœud, **défis** par nœud. Réalisé via un **workflow multi-agent
    généralisé** (paramétré par parcours, `docs/RUNBOOK` + script réutilisable), fournées parallèles, validation +
    ingestion append/upsert, **0 régression**.
  - **Bilan (base)** : docker 16/60/60q · kubernetes 20/67/67q · terraform 19/55/55q · software-design 22/85/85q
    (nœuds/leçons/quiz, **1 quiz par leçon partout**). Diagrammes : 20 + 21 + 19 + 34. **Défis créés : 32 + 40 + 38 + 45**.
  - Avec SF-56 (backend) et SF-57 (software-architect), les **6 grands parcours** sont désormais « 0 → expert »
    débutant : **126 nœuds, 478 leçons, 478 quiz, 147 diagrammes, 263 défis**, sans régression.

- ✅ **SF-56 — Refonte « débutant → expert » de TOUT le parcours Backend + UX front** — **TERMINÉ**
  - **Contenu (26 nœuds)** : chaque nœud retravaillé au standard pédagogique établi sur `git` :
    rewrite débutant (chaque terme technique **défini à sa 1re apparition** + analogie `👉` + exemple
    concret), **un quiz sur CHAQUE leçon**, **≥ 1 diagramme MERMAID** par nœud sur le sujet abstrait,
    **1–2 défis** par nœud. **Bilan base : 26 nœuds / 153 leçons / 153 quiz (0 leçon sans quiz), 62 défis.**
    Réalisé via un **workflow multi-agent** (1 agent par nœud, 4 fournées parallèles), puis
    validation + ingestion append/upsert. **0 régression** (slugs, quizRef et progression préservés ;
    autres parcours inchangés). `git` a reçu en plus une leçon 0 « Préparer sa machine » et un atelier fil rouge.
  - **Front / UX** (vérifié dans Chrome, Lighthouse) : rendu des listes markdown, `👉`→icône SVG,
    suppression des emojis (jeu d'icônes maison) et du tiret cadratin `—` au rendu, **contraste du code
    en mode clair corrigé (WCAG AA)**, **gzip nginx** (JS 316→101 KB), `aria-label` thème, SEO
    (meta description + robots.txt). **Lighthouse : Accessibilité 96→100, SEO 82→100, Best-Practices 100.**
  - Runbook autonome de la refonte : `docs/RUNBOOK-refonte-backend.md`.

- ✅ **SF-55 — Renforcement « expert » Software Design (2e passe d'audit, comble les gaps, sans régression)** — **TERMINÉ**
  - **Bilan** : 2e audit 0→expert des 22 nœuds (4 auditeurs parallèles), gaps réconciliés inter-nœuds (doublons écartés).
    **50 → 85 leçons** (+35), **28 → 63 leçons expertes** (+35), **50 → 85 quiz** (+35). **0 régression** (append-only,
    upsert par `(nœud, slug)` ; backend 26/151, docker 16/60, k8s 20/67, terraform 19/55, software-architect 23/58 —
    inchangés). Le nœud `architecture-decisions` (qui n'avait **aucune** leçon experte) reçoit **3 leçons** → plus aucun
    nœud sans couche experte.
  - **Gaps comblés (par nœud)** : complexité cognitive/quality gates (clean-code) · TDD/F.I.R.S.T./doublures
    (clean-code-practices) · Result-Either/railway + composition/currying (programming-paradigms) · modèle anémique vs
    riche (oop-principles) · variance/PECS + record patterns/DOP (oop-constructs) · LSP/Design-by-Contract + ISP vs SRP
    (solid) · 4 règles du Simple Design (dry-yagni-kiss) · loi de Déméter portée réelle (other-design-principles) ·
    Visitor/double dispatch + Flyweight/Bridge (gof-patterns) · Broker/Blackboard/Microkernel/Pipes (posa) · taxonomies
    cohésion/stamp coupling/Weirich (coupling-cohesion) · hexagonal vs onion vs clean + frontières partielles/ACL
    (boundaries) · couches strictes/relâchées/sinkhole (structural-styles) · backpressure + broker/EIP/schéma
    (messaging-styles) · sharding/quorum + horloges Lamport/vector clocks (distributed-styles) · composition root +
    vertical slice (layered-hexagonal) · MVP/MVVM/MVI + SSR/SPA/hypermédia (mvc) · domain services/factories/specs +
    sous-domaines vs BC (ddd) · API composition/BFF/service mesh (monolith-vs-microservices) · RGPD/crypto-shredding +
    concurrence optimiste/causation (event-sourcing-cqrs) · specification/assembler + mapping héritage/N+1
    (enterprise-patterns) · QAS/utility-tree/ATAM + choisir un style + MADR/tech-radar/fitness functions
    (architecture-decisions).
  - Chaque leçon experte se termine par son quiz dédié. Le parcours Software Design est désormais réellement « 0 → expert »
    sur les 22 nœuds.

- ✅ **SF-54 — Couche « expert » Software Architect (comble l'audit 4-auditeurs, sans régression)** — **TERMINÉ**
  - **Bilan** : les 23 nœuds enrichis d'une couche experte issue de l'audit 0→expert (4 auditeurs parallèles).
    **23 → 58 leçons** (+35), **23 → 58 quiz** (+35). **12 nœuds « haute » = 2 leçons expertes**, **11 « moyenne » =
    1 leçon**. **0 régression** (append-only, upsert par `(nœud, slug)` ; backend 26/151, docker 16/60, k8s 20/67,
    terraform 19/55, software-design 22/50 — inchangés).
  - **Manques comblés (extraits)** : Conway/ISO 25010/Big Ball of Mud (what-is-architecture) · 8 attentes Richards&Ford,
    largeur/profondeur, spécialités (what-is-architect) · Team Topologies/influence sans autorité (collaborate-coach) ·
    ATAM/matrice pondérée/biais/gouvernance (decision-making) · BASE/PACELC/Saga/Outbox/connascence (patterns-principles) ·
    EDA/hexagonal/orchestration-chorégraphie/8 fallacies/BFF-mesh (architectural-styles) · versionnement/RFC 7807/async/
    GraphQL N+1 (apis-integrations) · HTTP2-3/TLS-mTLS/DNS/latency numbers/zero-trust (networks) · Kafka/exactly-once/
    4 patterns d'événements/schema registry (messaging) · SRE-SLO/observabilité-OTel/canary-GitOps/DR/chaos (operations-devops) ·
    STRIDE/RBAC-ABAC/OAuth-PKCE/supply chain (security) · CAP-PACELC/isolation/sharding/CDC/Lakehouse (working-with-data) ·
    TOGAF-ADM/Zachman · ASR/utility-tree · arc42/42010/Structurizr · Backstage/policy-as-code · estimation outillée ·
    concurrence/runtime · Core Web Vitals/Module Federation · EventStorming/Tech Radar · DORA · EIP/6R's/MACH.
  - Chaque leçon experte finit par un quiz dédié. Le parcours Software Architect est désormais réellement « 0 → expert ».

- ✅ **SF-53 — Nouveau parcours Software Architect (fidèle roadmap.sh)** — **TERMINÉ (23/23)**
  - **Bilan** : nouveau TRACK **`software-architect`** (le RÔLE d'architecte), distinct de `software-design` (les
    techniques). Bâti depuis `app/roadmap-contenu/software-architect.pdf` : **23 nœuds / 9 phases / 23 leçons /
    23 quiz** (vérifié en base). Chaque nœud cadre les sujets **du point de vue de l'architecte (décisions/arbitrages)**
    et renvoie à `software-design` pour le détail technique, afin d'éviter la redite. Sections CONCEPT/DIAGRAM/EXERCISE
    corrigé/QUIZ, en français. **0 régression** (backend 26/151, docker 16/60, k8s 20/67, terraform 19/55,
    software-design 22/50 — inchangés).
  - **Structure (miroir des groupes roadmap.sh)** : Comprendre les bases (what-is-architecture, what-is-architect,
    levels-of-architecture) · Responsabilités (tech-decisions, requirements-elicitation, documentation,
    enforcing-standards, collaborate-coach) · Compétences clés (decision-making, communication-estimation) ·
    Socle technique (programming-languages, patterns-principles) · Styles d'architecture (architectural-styles,
    frontend-architecture) · Intégration &amp; réseau (apis-integrations, networks, messaging) · Opérations &amp;
    sécurité (operations-devops, security) · Données (working-with-data) · Processus, outils &amp; entreprise (tools,
    management-methodologies, enterprise-software). Edges PREREQ tracent la progression.
  - Ensuite (optionnel) : audit senior + couche « expert » par nœud (façon SF-46/48/50/52) si souhaité.

- ✅ **SF-52 — Couche « expert » Software Design & Architecture (comble l'audit, sans régression)** — **TERMINÉ**
  - **Bilan** : les 21 nœuds enrichis d'une couche experte + **1 nouveau nœud transverse** `architecture-decisions`.
    **21 → 50 leçons** (+29), **21 → 50 quiz** (+29), **21 → 22 nœuds**, **9 → 10 phases** (nouvelle phase « Décider
    en architecte »). **0 régression** vérifiée (append de leçons, upsert par `(nœud, slug)` → identifiants +
    progression préservés ; autres parcours/certifs intacts : backend 26/151, docker 16/60, k8s 20/67, terraform 19/55).
  - **7 nœuds priorité haute (2 leçons expertes chacun)** : solid (DIP/DI/IoC + SRP par acteur/role interfaces) ·
    ddd (DDD stratégique/context mapping + agrégats Vernon/domain events) · layered-hexagonal (interactor/presenter/ACL
    + stratégie de test par couche) · messaging-styles (4 patterns d'événements Fowler + Saga/Outbox/garanties) ·
    distributed-styles (patterns de résilience + PACELC/cohérence/consensus) · monolith-vs-microservices (Strangler
    Fig/branch by abstraction + hors-code microservices/Conway) · enterprise-patterns (Unit of Work/Service
    Layer/verrous + impedance/Active Record vs Data Mapper/ACL).
  - **11 nœuds moyens + 3 bas (1 leçon)** : smells/refactoring/erreurs · immutabilité/boundaries/package-by-feature ·
    réactif & FP concret · types de polymorphisme/fragile base class · records/sealed · AHA/complexité essentielle ·
    GRASP · anti-patterns & patterns absorbés par le langage · métriques de composants/Main Sequence · connascence ·
    cercles Clean Architecture/franchissement · autres styles/modularité imposée (ArchUnit) · projections/snapshots/
    versioning · flux unidirectionnel (Flux/Redux).
  - **Nouveau nœud transverse `architecture-decisions`** : attributs de qualité (-ilités), trade-offs, ADR, modèle C4,
    architecture évolutive/fitness functions. Chaque leçon experte finit par un quiz dédié.
  - Le parcours SD&A est désormais réellement « 0 → architecte », aligné sur l'audit publié.

- ✅ **SF-51 — Refonte du parcours Software Design & Architecture (fidèle roadmap.sh)** — **TERMINÉ (21/21)**
  - **Bilan** : ancien TRACK superficiel (8 nœuds, 8 leçons, 2 quiz — placeholder) **remplacé** par une structure
    fidèle à la **roadmap.sh Software Design & Architecture** (`app/roadmap-contenu/software-design-architecture.pdf`) :
    **21 nœuds / 9 phases / 21 leçons / 21 quiz + 1 challenge** (vérifié en base). Chaque nœud va du débutant au
    professionnel (sections CONCEPT/CODE/DIAGRAM/EXERCISE corrigé/QUIZ), en français. Ingestion `--reset` après purge
    propre du parcours (nœuds obsolètes design-patterns/scalability supprimés). **0 régression** sur les autres
    parcours/certifs (backend 26/151, docker 16/60, kubernetes 20/67, terraform 19/55, cka/ckad/kcna 5/5 — intacts).
  - **Structure (miroir des 9 groupes roadmap.sh)** : Code propre (clean-code, clean-code-practices) · Paradigmes
    (programming-paradigms) · POO (oop-principles, oop-constructs) · Principes de conception (solid, dry-yagni-kiss,
    other-design-principles) · Design patterns (gof-patterns, posa-component-principles) · Principes d'architecture
    (coupling-cohesion, boundaries-policy-detail) · Styles d'architecture (structural-styles, messaging-styles,
    distributed-styles) · Patterns d'architecture (layered-hexagonal, ddd, mvc, monolith-vs-microservices,
    event-sourcing-cqrs) · Patterns d'entreprise (enterprise-patterns). Edges PREREQ tracent la progression.
  - Challenge `define-a-port` (layered-hexagonal) conservé ; quizzes solid-check & hexagonal-check enrichis.
  - Ensuite (optionnel) : audit senior + couche « expert » par nœud (façon SF-46/48/50) si souhaité.

- ✅ **SF-50 — Couche « expert » Terraform (comble l'audit, sans régression)** — **TERMINÉ (19/19)**
  - **Bilan** : les 19 nœuds Terraform enrichis d'une couche experte comblant tous les manques de l'audit senior.
    **31 → 55 leçons** (+24), **20 → 44 quiz** (+24 quiz expert). **0 régression** vérifiée en base à chaque lot
    (append de leçons, upsert par `(nœud, slug)` → identifiants + progression apprenants préservés).
  - **Manques comblés par nœud** : OpenTofu/paysage IaC (what-is-terraform) · **for/splat/dynamic blocks** +
    fonctions (try/coalesce/cidrsubnet)/**terraform console** (hcl-basics) · configuration_aliases/providers→module +
    lock file multi-plateforme/mirror (providers) · **moved/removed blocks** + postcondition/replace_triggered_by/
    timeouts (resources) · for_each sur modules + migration count→for_each via moved (meta-arguments) · types
    optional()/nullable (variables) · for expressions dans locals + **valeurs ephemeral** vs sensitive
    (locals-outputs) · **détection de dérive** -refresh-only/-target/**-detailed-exitcode** + console/upgrade
    provider (core-workflow) · migration de backend/**partial config** (state-management) · **import blocks** en
    masse/-generate-config-out + state replace-provider/force-unlock (state-operations) · versionnage/publication/
    composition (modules) · timing de lecture data + **check blocks** (data-sources) · **terraform_data**/
    null_resource (provisioners) · **CLI vs HCP workspaces** (workspaces) · **Atlantis/TACOS** + dérive planifiée
    (cicd) · **mock_provider/overrides** (testing) · **HCP Stacks**/provider-defined functions (scaling) ·
    **valeurs éphémères/IAM moindre privilège** + supply chain/**dérive comme enjeu de sécurité** (security) ·
    registre privé/**no-code modules**/agents/run triggers (hcp). Chaque nœud finit par un quiz expert dédié.
  - Le parcours Terraform est désormais réellement « 0 → expert » sur les 19 nœuds, aligné sur l'audit publié.

- ✅ **SF-49 — Refonte du parcours Terraform (fidèle roadmap.sh, 0 → expert)** — **TERMINÉ (19/19)**
  - **Bilan** : ancien TRACK Terraform superficiel (8 nœuds, 8 leçons, 2 quiz) **remplacé** par une structure
    fidèle à la **roadmap.sh Terraform** (`app/roadmap-contenu/terraform.pdf`) : **19 nœuds / 12 phases /
    31 leçons / 20 quiz** (vérifié en base). Chaque nœud va du débutant à l'expert (sections CONCEPT/CODE/EXERCISE
    corrigé/QUIZ), en français. Pas de certification Terraform → rebuild complet sûr.
  - **Structure** : Introduction (what-is-terraform) · HCL & providers (hcl-basics, providers) · Ressources
    (resources, meta-arguments) · Variables & sorties (variables, locals-outputs) · Workflow (core-workflow) ·
    État (state-management, state-operations) · Modules (modules) · Données & provisioners (data-sources,
    provisioners) · Environnements & CI/CD (workspaces, cicd, testing) · Échelle (scaling) · Sécurité (security) ·
    HCP (hcp).
  - **Contenu clé** : clickops/drift & IaC déclaratif, IaC vs CaC ; HCL/blocs/références-dépendances implicites/
    init & lock file ; providers/Registry/credentials hors code/contraintes de version/alias ; ressources
    arguments-vs-attributs/actions create-update-replace-destroy/drift ; count vs for_each/depends_on/lifecycle
    (prevent_destroy) ; variables/validation/tfvars/TF_VAR_ ; locals vs variables/outputs/sensitive≠chiffré/
    preconditions ; plan-apply-destroy/plan sauvegardé/fmt-validate-TFLint ; state/données sensibles/backend
    distant/state locking ; import/state mv-rm/découpage d'état (blast radius) ; modules = fonction/publiés-vs-
    locaux/agnostique du provider ; data sources/templatefile ; provisioners « dernier recours »/immuabilité ;
    workspaces vs répertoires séparés ; CI/CD plan-sur-PR/OIDC ; terraform test/Terratest ; parallélisme/
    Terragrunt/Infracost ; secrets/scanners Checkov-tfsec/policy as code Sentinel-OPA ; HCP managé vs auto-géré.
    Chaque nœud finit par un quiz.
  - **Bascule** : authoring complet fichiers (ancien TRACK resté live pendant l'écriture), puis reset DB du TRACK
    `terraform` (purge lessons/quizzes/code_challenges par nœud + delete roadmap → cascade) + `ingest --reset`.
  - Ensuite (optionnel) : audit senior + couche « expert » par nœud (façon SF-46/SF-48) si souhaité.

- ✅ **SF-47 — Refonte du parcours Kubernetes (fidèle roadmap.sh, 0 → expert)** — **TERMINÉ (20/20)**
  - **Bilan** : ancien TRACK Kubernetes superficiel (8 nœuds, 8 leçons, 2 quiz) **remplacé** par une structure
    fidèle à la **roadmap.sh Kubernetes** (`app/roadmap-contenu/kubernetes.pdf`) : **20 nœuds / 12 phases /
    41 leçons / 25 quiz** (vérifié en base). Chaque nœud va du débutant à l'expert (sections CONCEPT/CODE/DIAGRAM/
    EXERCISE corrigé/QUIZ), en français.
  - **Structure** : Introduction (what-is-kubernetes, key-concepts) · Mise en place (getting-started) ·
    Charges de travail (pods, deployments, workloads-advanced) · Services & réseau (services, ingress-networking) ·
    Configuration (config-secrets) · Ressources (resource-management) · Sécurité (rbac, cluster-security) ·
    Observabilité (observability) · Autoscaling & ordonnancement (autoscaling, scheduling) · Stockage
    (storage-volumes) · Déploiement (deployment-strategies, helm-gitops) · Extensions & opérations
    (extending-kubernetes, cluster-operations).
  - **Contenu clé** : orchestration & modèle déclaratif/réconciliation ; plan de contrôle (apiserver/etcd/scheduler/
    controller-manager) & nœuds (kubelet/kube-proxy/runtime) ; Pods/probes liveness-readiness-startup/init & sidecar ;
    ReplicaSet/Deployment/rolling-update/rollback ; StatefulSet/DaemonSet/Job/CronJob ; Services ClusterIP-NodePort-
    LoadBalancer/DNS ; Ingress & CNI/kube-proxy/modèle une-IP-par-pod ; ConfigMaps/Secrets (base64≠chiffré) ;
    requests-limits/QoS/quotas ; RBAC/ServiceAccounts moindre privilège ; NetworkPolicies/Pod Security/admission
    Kyverno-OPA/chaîne d'appro/Falco ; logs-métriques-traces/percentiles-SLO ; HPA-VPA-Cluster Autoscaler ;
    scheduler/taints-tolerations/affinités/topology spread/PDB ; PV-PVC-StorageClass-CSI/StatefulSet ; rolling/
    blue-green/canary Argo Rollouts ; Helm/GitOps ArgoCD-Flux ; CRDs/opérateurs ; managé vs kubeadm/upgrades-skew/
    sauvegarde etcd/multi-cluster. Chaque nœud finit par un quiz.
  - **Bascule** : authoring complet fichiers (ancien TRACK resté live pendant l'écriture), puis reset DB du **seul**
    TRACK `kubernetes` (purge lessons/quizzes/code_challenges par nœud + delete roadmap → cascade phases/nodes/edges)
    + `ingest --only kubernetes --reset`. **Certifications cka/ckad/kcna intactes** (vérifié : 5 nœuds chacune).
  - Ensuite (optionnel) : audit senior + couche « expert » par nœud (façon SF-46 Docker) si souhaité.

- ✅ **SF-45 — Refonte du parcours Docker (fidèle roadmap.sh, 0 → expert)** — **TERMINÉ (16/16)**
  - **Bilan** : ancien parcours Docker superficiel (7 nœuds, 7 leçons, 2 quiz) **remplacé** par une structure
    fidèle à la **roadmap.sh Docker** : **16 nœuds / 9 phases / 40 leçons / 16 quiz** (vérifié en base). Chaque
    nœud va du débutant à l'expert (sections CONCEPT/CODE/DIAGRAM/EXERCISE corrigé/QUIZ + « Pour aller plus loin »),
    en français.
  - **Structure** : Introduction (what-are-containers) · Technologies sous-jacentes (linux-fundamentals,
    namespaces-cgroups-union, docker-and-oci) · Installation (installation-setup) · Les bases (docker-basics) ·
    Images & registres (third-party-images, building-images, container-registries) · Exécution & données
    (running-containers, data-persistence) · CLI & réseau (container-networking, docker-cli) · Sécurité
    (container-security) · Dév & déploiement (developer-experience, deploying-containers).
  - **Contenu clé** : problème résolu par les conteneurs & immuabilité ; namespaces/cgroups/union FS &
    copy-on-write ; OCI/runc/containerd/CRI & runtimes isolants ; Desktop vs Engine, groupe docker/rootless ;
    run/cycle de vie/logs/exec ; images tierces & versions épinglées ; Dockerfile/cache de couches/multi-stage/
    taille & sécurité/build secrets ; registres, tag mutable vs digest immuable, signature/SBOM ; run avancé/
    Compose/depends_on+healthcheck/durcissement runtime ; volumes vs bind mounts/permissions/sauvegarde ;
    réseaux/DNS par nom/EXPOSE vs -p/segmentation/overlay ; CLI objets+verbes/format/prune ; sécurité image+
    runtime/moindre privilège/socket Docker/privileged/défense en profondeur ; hot reload/debug/Testcontainers ;
    CI-CD build-once-deploy-many/PaaS vs orchestrateurs/Swarm-Nomad-K8s.
  - **Bascule réalisée** : authoring complet fichiers (ancien parcours resté live pendant l'écriture), puis reset
    DB (purge lessons/quizzes/code_challenges par nœud + delete roadmap docker → cascade phases/nodes/edges) +
    `ingest --only docker --reset`. Aucun état cassé pendant les tests front.
  - Ensuite (optionnel) : couche « expert » supplémentaire par nœud (façon SF-44 Backend) si souhaité, et même
    refonte pour les 8 autres tracks restants.

- ✅ **SF-46 — Couche « expert » Docker (comble l'audit, sans régression)** — **TERMINÉ (16/16)**
  - **Bilan** : les 16 nœuds Docker enrichis d'une couche experte comblant tous les manques de l'audit senior.
    **40 → 60 leçons** (+20), **16 → 36 quiz** (+20 quiz expert). **0 régression** vérifiée en base à chaque lot
    (append de leçons, upsert par `(nœud, slug)` → identifiants + progression apprenants préservés).
  - **Manques comblés par nœud** : histoire/Windows/WASM/frontière sécurité (what-are-containers) ·
    FD/signaux/proc + réseau Linux/capabilities (linux-fundamentals) · cgroups v2/OverlayFS copy-up + conteneur
    à la main unshare/nsenter (namespaces-cgroups-union) · multi-arch/manifest list/containerd/BuildKit
    (docker-and-oci) · docker context/miroirs-rate limits/WSL2 (installation-setup) · codes de sortie 137/143 +
    cp/diff/commit/attach-vs-exec (docker-basics) · scripts d'init/variantes de tags (third-party-images) ·
    **BuildKit cache mounts/build secrets/buildx multi-arch** + ARG-vs-ENV/HEALTHCHECK/LABEL OCI/reproductibilité
    (building-images) · self-hosting registre/GC/pull-through cache (container-registries) · **Compose avancé**
    profiles/override/watch + **init-tini/PID 1/zombies** (running-containers) · drivers de volume/backup/perf
    bind mount/piège VOLUME (data-persistence) · **veth/docker0/iptables NAT + piège UFW** + DNS embarqué
    (container-networking) · plugins CLI/context/events/jq (docker-cli) · **capabilities/seccomp/LSM/userns-remap**
    + **chaîne d'appro cosign-SBOM-SLSA/admission control Kyverno/secrets/Falco/CIS** (container-security) ·
    **Dev Containers**/compose watch/docker init-debug (developer-experience) · Swarm concret/**GitOps**/
    observabilité conteneurs (deploying-containers). Chaque nœud finit par un quiz expert dédié.
  - Le parcours Docker est désormais réellement « 0 → expert » sur les 16 nœuds, aligné sur l'audit publié.

- ✅ **SF-41 — Déploiement local (Docker Compose)** (P15)
  - CA :
    - [x] `docker-compose.yml` : **PostgreSQL 18** (healthcheck, volume `pgdata`), **backend WebFlux**
          (profil `docker`, `depends_on` postgres healthy), **frontend Nginx** (SPA + proxy `/api`),
          réseau interne `skillforge`.
    - [x] Flyway (V1–V9) + **seed** au démarrage → plateforme explorable (parcours `decouverte`, comptes démo) ;
          **`.env.example`** documenté ; ports hôte configurables (`BACKEND_PORT`/`FRONTEND_PORT`).
    - [x] **Dockerfiles** back (multi-stage Maven → JRE 21) + front (multi-stage Node → Nginx 1.27) ;
          `.dockerignore` (racine + front) ; `nginx.conf` (fallback SPA + proxy `/api`, `/v3/api-docs`, `/swagger-ui`).
    - [x] **Une commande** : `docker compose up --build`. URLs front/API/OpenAPI documentées (README).
  - Fichiers : `Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf`, `docker-compose.yml`, `.env.example`,
    `.dockerignore`, `frontend/.dockerignore`, `README.md`, `.gitignore`.
  - Corrections : retrait de la directive `# syntax=docker/dockerfile:1` (crash du helper d'identifiants Docker
    sous Windows) ; montage volume sur `/var/lib/postgresql` (nouvelle convention `postgres:18`).
  - Vérifié **e2e** : `docker compose up --build` → 3 conteneurs, postgres healthy ; **backend `/actuator/health`
    UP** ; **front servi** (200) + **SPA fallback** (`/profile` → index.html) ; **proxy `/api`** (login 200) ;
    catalogue via le front → seed `decouverte` chargé (Flyway V1–V9 appliqué). Stack démonté proprement.
  - Dépend de : SF-11 (config), SF-30/SF-31 (front buildable).

- ⬜ **SF-42 — Tests & qualité (CI)** (P16)
  - CA :
    - [ ] Back : unitaires domaine, intégration Testcontainers (R2DBC + WebFlux), tests
          sécurité (accès autorisé/refusé par rôle).
    - [ ] Front : tests composants clés + e2e « se connecter → ouvrir un nœud → terminer une
          leçon → voir la progression ».
    - [ ] Lint/format configurés ; CI GitHub Actions build + tests.

**Revue de lot 4 :** plateforme démarrable en une commande, testée, avec du contenu réel.

---

## Lot 5 — Lab exécutable : évaluation pratique par conteneur

> Comble le trou identifié par le PO : à la fin d'un nœud, une **vraie épreuve pratique**
> (pas un QCM) dans un **environnement exécutable**, dont la **réussite valide le nœud**.
> Conception figée dans **`docs/adr/0008-lab-executable-evaluation-pratique.md`**.
> Contrainte : 100% local (Docker Compose). **Sécurité = cœur du chantier** (socket Docker).
> WIP = 1, **pilote = parcours `backend`**. Chaque story respecte la DoD de `CLAUDE.md` §5.

- ⬜ **SF-91 — Épic : évaluation pratique par conteneur pour valider un nœud** (ADR-0008)
  - **Objectif** : nouveau contexte `lab` ; un nœud peut porter une **tâche pratique**
    (image, workspace, énoncé, **script de validation** code-sortie 0 = réussi) ; politique
    de validation de nœud **configurable** (`LESSONS_ONLY` défaut = aucune régression,
    `LESSONS_AND_PRACTICAL`) ; la réussite émet `PracticalPassedEvent` consommé par
    `apprentissage`.
  - **Découpage proposé (à prendre dans l'ordre, WIP = 1) :**
    - ✅ **SF-91a — Modèle & migration `lab`** — **TERMINÉ**
      - Module `lab` créé (hexagonal, câblé dans le pom parent + dépendance `app`). Domaine pur :
        `PracticalTask`, `PracticalAttempt` (transitions immuables + garde terminal), enums
        `AttemptStatus` / `CompletionPolicy`, value objects `PracticalTaskId` / `LearnerId` ;
        ports sortants `PracticalTaskStore` / `PracticalAttemptStore`. Pas d'orchestration (SF-91b+).
      - Migration **V12** : colonne `nodes.completion_policy` (CHECK, défaut **`LESSONS_ONLY`** =
        aucune régression), tables `practical_tasks` (une par nœud) et `practical_attempts`.
      - Vérifs : `mvn -pl lab test` **5/5 verts** ; `mvn -pl app -am compile` OK ; V12 jouée en
        transaction ROLLBACK sur Postgres (syntaxe valide, état Flyway intact).
    - ✅ **SF-91b — Port d'exécution + orchestrateur Docker durci** — **TERMINÉ**
      - Port `LabRuntimePort` (start/exec/stop/reapExpired) + types `LabRuntimeSpec`
        (quotas/TTL, défaut pilote backend), `RunningLab`, `ExecResult`.
      - **`SandboxPolicy`** (classe pure, cœur sécurité) construit les arguments `docker` en
        imposant TOUS les garde-fous : `--network none`, `--cpus`/`--memory`(+swap off)/
        `--pids-limit`, `--security-opt no-new-privileges`, `--cap-drop ALL`, `--user 1000:1000`,
        `--read-only` + tmpfs workspace, label de reaping, `sleep TTL` (auto-extinction).
      - Adapter `DockerCliLabRuntime` (ProcessBuilder, réactif boundedElastic) + reaper des
        conteneurs `exited` du label.
      - **Tests d'isolation 5/5** : tous les drapeaux de durcissement présents, et AUCUN drapeau
        dangereux émis (`--privileged`, `network host`, montage `docker.sock`/`/var/run`,
        `--cap-add`, `--pid host`). Total module `lab` : **10/10 verts**.
      - Choix de sécurité : **le socket Docker n'est PAS monté dans le compose** (capacité
        quasi-root non activée tant que la fonctionnalité n'est pas complète et relue) ;
        l'adapter n'est pas encore câblé en bean. Activation = étape explicite (SF-91c+).
    - ✅ **SF-91c — Soumission & correction** — **TERMINÉ**
      - Use case `SubmitPracticalUseCase` + service `SubmitPracticalService` : démarre le
        conteneur durci (`Mono.usingWhen` → arrêt garanti), y dépose les fichiers soumis
        (`putFile`), exécute le script de validation, en déduit `passed` + sortie, persiste la
        tentative, et publie **`PracticalPassedEvent`** (dans `common.event`) si réussi.
      - Port `putFile` ajouté (seed via `docker exec -i`), adapters R2DBC `PracticalTaskStore`/
        `PracticalAttemptStore` (DatabaseClient, upsert), `SpringLabEventPublisher`, `LabConfig`
        (orchestrateur inerte sans démon Docker), contrôleur `POST /api/v1/lab/nodes/{id}/submit`
        (protégé par la chaîne de sécurité existante, garde anti-traversée sur les chemins).
      - Vérifs : module `lab` **13/13** (service réussite/échec/not-found avec doubles, isolation,
        domaine) ; backend **rebuild OK**, Flyway **V12 appliquée**, 315 nœuds `LESSONS_ONLY`
        (0 régression), endpoint câblé (401 sans jeton). Capacité Docker non activée (voir SF-91b).
    - ✅ **SF-91d — Lien progression** — **TERMINÉ**
      - `ProgressRecalculator` intègre la politique : un nœud `LESSONS_AND_PRACTICAL` n'est
        COMPLETED que si leçons terminées **ET** pratique réussie (lecture `completion_policy` +
        `practical_passed` via les ports catalogue / node_progress).
      - Listener `PracticalProgressListener` (@EventListener) consomme `PracticalPassedEvent` →
        service `MarkNodePracticalPassedService` : résout l'inscription (via `roadmapOfNode`),
        mémorise la réussite (`markPracticalPassed`), recalcule statut + % parcours (sans déplacer
        la reprise), émet `NodeCompletedEvent`/`RoadmapCompletedEvent` **à la transition** (pas de
        double comptage). Migration **V13** (`node_progress.practical_passed`, défaut false).
      - Vérifs : `apprentissage` **18/18** (dont `PracticalGatingTest` 5 et IT Testcontainers 7),
        schéma d'IT complété, backend **rebuild OK** + Flyway **V13** appliquée. Défaut
        `LESSONS_ONLY` partout → **0 régression** (les nœuds existants se valident comme avant).
    - ✅ **SF-91e — UI pratique (front)** — **TERMINÉ**
      - Backend : endpoint **GET `/api/v1/lab/nodes/{id}/practical`** (use case `GetPracticalUseCase`
        + `GetPracticalService` + `LabCatalogPort`/adapter lisant `completion_policy`) renvoyant
        existence, caractère requis, consigne et statut de la dernière tentative.
      - Front : `api/lab.ts` (getPractical/submitPractical), composant **`PracticalPanel`**
        (consigne + éditeur de code + « Valider la pratique » + verdict réussi/échoué + sortie),
        intégré dans `NodePage` (affiché seulement si le nœud a une épreuve), badge « Requise pour
        valider » / « Réussie », erreur propre si l'environnement est indisponible.
      - Vérifs : module `lab` compile + tests ; **`npm run build` (tsc + vite) OK**. Pour
        l'essayer visuellement de bout en bout, il faut SF-91g (tâche seedée) + activation Docker.
    - ⬜ **SF-91f — Terminal navigateur (optionnel pilote)** : xterm.js ↔ websocket ↔ shell
      du conteneur, pour une vraie manipulation interactive. CA : terminal fonctionnel et borné.
    - ⬜ **SF-91g — Contenu pilote** : tâches pratiques + scripts de validation pour quelques
      nœuds `backend` ; support dans `content-pipeline` (nouveau fichier de tâche + schéma).
      CA : au moins 3 nœuds backend validables par la pratique, seedés.
    - 🧊 **SF-91h — Phase 3 (hors pilote)** : bacs à sable infra (k3d/dind) pour docker,
      kubernetes, terraform, cka, ckad (terminal complet, clusters jetables). ADR dédié au
      durcissement du mode privilégié avant de commencer.
  - **Risques connus** (cf. ADR) : surface de sécurité (socket Docker) ; coût mémoire sur
    machine locale déjà tendue → **quotas stricts + faible concurrence** ; maintenance des
    images et scripts de validation.

**Revue de lot 5 (à faire en fin de lot) :** un apprenant valide un nœud backend par une
épreuve pratique réellement exécutée, sans régression des parcours existants.

---

## Dette & items découverts en cours de route

> À alimenter au fil de l'eau (cf. `CLAUDE.md` §4/§5 : tout reste non fait est noté ici).

- [ ] Confirmer/renommer le nom de produit « SkillForge ».
- [ ] Trancher : module `-api` partagé par contexte vs ports côté consommateur (défaut retenu :
      ports côté consommateur, câblage dans `app`).
- [ ] Définir le barème XP/niveaux paramétrable (valeurs concrètes) — à préciser en SF-21/SF-24.
- [x] Lib JWT et hachage : **Nimbus HS256** (via Spring Security oauth2-jose) + **BCrypt**
      (acté en SF-12 ; TTL access 60 min / refresh 30 j avec rotation).
- [ ] Env de dev : un PostgreSQL natif occupe le port 5432 → pour les runs locaux, utiliser un
      port dédié (ex. 55432) via `SF_R2DBC_URL`/`SF_JDBC_URL` (documenté dans `README.md`).
      Sans impact en Docker Compose (SF-41, réseau interne, hôte `postgres`).
