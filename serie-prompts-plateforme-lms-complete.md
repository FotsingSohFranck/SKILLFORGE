# Série de prompts — Grande plateforme e-learning locale « SkillForge » (Java 21 / WebFlux / Hexagonal / PostgreSQL 18 / React)

**Objectif :** construire, via une série de prompts enchaînés, UNE plateforme LMS complète, self-hébergée en local, qui :
- couvre TOUTES les roadmaps fournies (Backend, Software Design & Architecture, Docker, Kubernetes, Terraform, Rust, Prompt Engineering, AWS, Backend Performance, API Security) ;
- fournit tout ce qu'une vraie plateforme e-learning propose : comptes utilisateurs, base de données, progression persistante, quiz, examens blancs, tableaux de bord, gamification ;
- reprend et unifie les capacités des 3 mini-plateformes précédentes (apprendre l'architecture ; préparer les certifications Docker/Kubernetes ; former un développeur backend Java) ;
- fait passer l'utilisateur de **débutant à professionnel** ;
- **Backend :** Java 21, architecture **hexagonale**, Spring Boot **4.1** (Spring Framework 7), **WebFlux** (réactif), **R2DBC**, **PostgreSQL 18** avec **UUID v7** en clés primaires ;
- **Frontend :** React, **design monochrome sobre « inspiration Apple »** — neutres gris + accent quasi-noir, surfaces en verre (glassmorphism), mode clair/sombre, WCAG AA.

> **Note (document à jour).** Cette série de prompts a servi à **construire** la plateforme, aujourd'hui réalisée. Deux écarts par rapport au plan initial, décidés en cours de route :
> - **Design** : la palette « violet / bleu / blanc » a été abandonnée (jugée « trop IA ») au profit d'un **monochrome sobre inspiration Apple** — neutres gris (valeurs Apple) + accent **quasi-noir `#1d1d1f`**, surfaces en **verre (glassmorphism)** pour le chrome, cartes de contenu **opaques**, mode clair/sombre, WCAG AA.
> - **Coloration syntaxique** : `react-syntax-highlighter` a été remplacé par **Prism.js** (intégration directe).
> Le reste (stack, architecture hexagonale, contenu des parcours, gamification, images pédagogiques) est conforme au plan.

---

## MÉTHODE — comment mener cette série (à lire avant de commencer)

Construire une grande application via une IA suit les mêmes bonnes pratiques de prompting que celles de la roadmap Prompt Engineering :

- **Spec-first.** Le Prompt 0 produit un DOCUMENT DE SPÉCIFICATION. C'est l'ancre : tu le **réinjectes en tête de chaque prompt suivant** (ou tu demandes à Claude de s'y référer). Aucune décision d'archi ne se prend deux fois.
- **Délimite les sections** avec des balises XML ou des triples backticks (comme dans ces prompts).
- **Sorties structurées** (JSON / arborescence de fichiers) quand ça aide la machine à consommer le résultat.
- **Placeholders `[[ ]]`** pour tout ce qui varie (contexte à recoller, module ciblé, etc.).
- **Un module à la fois.** Ne demande jamais « génère tout le backend ». Va contexte borné par contexte borné, et **vérifie que ça compile / tourne** avant de continuer.
- **Conventions figées tôt** (nommage, structure de package, style d'API) et rappelées dans chaque prompt pour la cohérence.
- **Itère** : chaque prompt se termine en te laissant relire, tester, puis enchaîner.

**Ordre recommandé :** Track 0 → Track 1 → Track 2 → Track 3 → Track 4. Les Tracks 2 (backend métier) et 3 (frontend) peuvent partiellement se paralléliser une fois le socle (Track 1) en place.

**Décisions de stack déjà tranchées (issues de recherche, juillet 2026) :**
- Spring Boot **4.1** sur **Spring Framework 7** (les branches 3.x sont EOL). Java 21 reste compatible.
- **PostgreSQL 18** : `DEFAULT uuidv7()` natif pour les clés primaires (ordonnées dans le temps, index-friendly). Alternative : génération côté Java via `com.github.f4b6a3:uuid-creator` ou `com.fasterxml.uuid:java-uuid-generator`.
- Migrations compatibles R2DBC : **Flyway** (exécuté via JDBC au démarrage) ou **Liquibase**.
- Sécurité réactive : **Spring Security WebFlux** + JWT.

---

# TRACK 0 — Cadrage & architecture

## PROMPT 0 — Spécification produit (document ancre)

```
Tu es lead product + architecte logiciel. Tu réponds en FRANÇAIS.

Je veux construire une plateforme e-learning LOCALE nommée [[SkillForge]] qui fait
monter un apprenant de DÉBUTANT à PROFESSIONNEL sur plusieurs parcours techniques.

ROADMAPS À COUVRIR (chacune = un parcours de la plateforme) :
Backend Developer, Software Design & Architecture, Docker, Kubernetes, Terraform,
Rust, Prompt Engineering, AWS (bonnes pratiques), Backend Performance, API Security.
La plateforme doit aussi proposer des PARCOURS DE CERTIFICATION (KCNA, CKA, CKAD)
avec examens blancs, et un parcours transverse "Développeur Backend Java".

CONTRAINTES TECHNIQUES (à respecter dans toute la spec) :
- Backend : Java 21, architecture hexagonale, Spring Boot 4.1 (Spring Framework 7),
  WebFlux réactif, R2DBC, PostgreSQL 18 (UUID v7 en clés primaires).
- Frontend : React, design monochrome sobre (inspiration Apple) : neutres gris + accent quasi-noir, verre (glassmorphism), mode clair/sombre.
- 100% exécutable en local (Docker Compose).

TÂCHE : produis un DOCUMENT DE SPÉCIFICATION structuré et complet, servant
d'ancre à tout le projet, contenant :
1. Vision & proposition de valeur.
2. Personas (apprenant débutant, apprenant confirmé, admin/instructeur).
3. Liste EXHAUSTIVE des fonctionnalités, groupées :
   - Comptes & auth (inscription, connexion, profil, rôles LEARNER / INSTRUCTOR / ADMIN).
   - Catalogue de parcours (roadmaps) et navigation.
   - Apprentissage : inscription à un parcours, NAVIGATION NON LINÉAIRE (aller à
     n'importe quel nœud, en sortir, revenir exactement où on s'était arrêté),
     progression persistée en base, favoris, reprise globale.
   - Contenu : leçons structurées (Concept / Schéma / Exemple de code / Exercice /
     Quiz).
   - Évaluation : quiz, défis de code, examens blancs CHRONOMÉTRÉS (certifs),
     scoring par domaine, analyse des points faibles.
   - Gamification : XP, niveaux (débutant → pro), badges, séries (streaks).
   - Tableaux de bord & analytics (progression par parcours, couverture des
     domaines, recommandations de révision).
   - Espace admin : gestion des parcours, du contenu, des utilisateurs.
   - Recherche dans le contenu.
4. Modèle conceptuel de haut niveau (les grands objets métier et leurs relations).
5. Découpage en CONTEXTES BORNÉS (bounded contexts) proposé, avec justification.
6. Parcours utilisateur clés (user journeys) en quelques étapes chacun.
7. Attributs de qualité visés (perf, sécurité, maintenabilité) et hypothèses.
8. Périmètre explicite : ce qui est DANS le MVP vs plus tard.

Format : Markdown clair et hiérarchisé. Ce document sera réutilisé tel quel comme
contexte dans les prompts suivants — sois précis et sans ambiguïté.
```

## PROMPT 1 — Architecture technique & découpage hexagonal

```
En te basant sur la SPÉCIFICATION ci-dessous, conçois l'ARCHITECTURE TECHNIQUE de
la plateforme. Réponds en FRANÇAIS.

SPÉCIFICATION :
[[ colle le document du Prompt 0 ]]

EXIGENCES
- Monolithe MODULAIRE (un seul déployable local) découpé en contextes bornés, chacun
  en architecture HEXAGONALE (ports & adapters) : domaine (entités, value objects,
  ports), application (use cases / services applicatifs), adaptateurs entrants
  (controllers WebFlux), adaptateurs sortants (repositories R2DBC, etc.).
- Stack : Java 21, Spring Boot 4.1, WebFlux, Spring Security réactif + JWT, R2DBC,
  PostgreSQL 18 (UUID v7), Flyway (ou Liquibase) pour les migrations.
- Contextes bornés attendus (ajuste si besoin, justifie) : IAM (utilisateurs/auth),
  Catalogue (roadmaps/nœuds/arêtes), Contenu (leçons/ressources), Apprentissage
  (inscriptions/progression/favoris/streaks/XP), Évaluation (quiz/examens/tentatives),
  Gamification (badges/niveaux), Analytics.

LIVRABLE
1. Schéma d'architecture (Mermaid) : contextes, dépendances autorisées, flux.
2. Pour UN contexte de référence (ex. Apprentissage), l'arborescence de packages
   hexagonale détaillée qui servira de patron à tous les autres.
3. Conventions transverses : nommage, structure Maven multi-modules (ou Gradle),
   gestion des UUID v7, mapping domaine↔persistance, gestion des erreurs réactive,
   format des réponses API (enveloppe, pagination, codes).
4. Choix build (Maven multi-module recommandé) + liste des modules.
5. 3 à 5 ADR (Architecture Decision Records) courts sur les décisions structurantes
   (réactif vs bloquant, monolithe modulaire vs microservices, UUID v7 en base vs
   applicatif, JWT stateless, migrations via Flyway).

Ce document d'architecture sera, avec la spécification, réinjecté dans les prompts
suivants.
```

---

# TRACK 1 — Socle backend

## PROMPT 2 — Modèle de domaine & schéma PostgreSQL 18

```
En te basant sur la SPÉCIFICATION et l'ARCHITECTURE ci-dessous, conçois le MODÈLE
DE DONNÉES complet et les MIGRATIONS PostgreSQL 18. Réponds en FRANÇAIS.

CONTEXTE :
[[ colle spécification (Prompt 0) + architecture (Prompt 1) ]]

EXIGENCES
- Toutes les clés primaires en UUID (type uuid), valeur par défaut uuidv7()
  (fonction native PostgreSQL 18). Explique où l'ID est généré (base vs applicatif)
  et reste cohérent.
- Couvre tous les contextes : utilisateurs & rôles ; roadmaps, nœuds, arêtes
  (graphe), parcours de certification et domaines d'examen ; leçons et leurs
  sections (concept, schéma, code, exercice, quiz) ; inscriptions, progression par
  nœud et par leçon, favoris, streaks, XP/niveaux ; quiz, questions, options,
  tentatives, examens blancs, résultats par domaine ; badges obtenus.
- Contraintes d'intégrité, index pertinents (dont index sur les FK et les colonnes
  de tri/filtre), et champs d'audit (created_at, updated_at).
- Modélise la PROGRESSION de façon à supporter la navigation non linéaire + reprise :
  dernière leçon vue par nœud, dernier nœud actif par parcours, % par nœud.

LIVRABLE
1. Diagramme entité-relation (Mermaid erDiagram).
2. Scripts de migration SQL versionnés (Flyway : V1__..., V2__..., etc.), prêts à
   l'emploi sur PostgreSQL 18, utilisant uuidv7().
3. Un jeu de données de SEED minimal (un admin, un apprenant, un petit parcours de
   démonstration) en SQL.
4. Notes sur l'usage d'UUID v7 (avantages d'index, précaution : la date de création
   est encodée dans l'ID).
```

## PROMPT 3 — Squelette backend (multi-module hexagonal, WebFlux, R2DBC)

```
Génère le SQUELETTE du backend, prêt à compiler, en respectant la SPÉCIFICATION,
l'ARCHITECTURE et le MODÈLE DE DONNÉES ci-dessous. Réponds en FRANÇAIS pour les
explications ; le code et les commentaires de code peuvent être en anglais.

CONTEXTE :
[[ colle spécification + architecture + (au besoin) modèle de données ]]

EXIGENCES
- Projet Maven multi-modules, Java 21, Spring Boot 4.1, WebFlux, Spring Data R2DBC,
  driver r2dbc-postgresql, Spring Security réactif, Flyway, validation, MapStruct
  (ou mapping manuel) domaine↔entités R2DBC.
- Implémente INTÉGRALEMENT et proprement, en hexagonal, UN contexte de référence
  choisi : [[Catalogue]] (roadmaps, nœuds, arêtes) — de bout en bout : domaine,
  ports, use cases, adaptateur R2DBC, controller WebFlux, DTOs, mapping, gestion
  d'erreurs, tests unitaires du domaine.
- Pour les AUTRES contextes : crée les modules et packages vides/à-remplir suivant
  le même patron, avec des TODO clairs (je les remplirai via les prompts suivants).
- Configuration : application.yml (datasource R2DBC + Flyway JDBC), profils
  local/docker, génération UUID v7 cohérente, enveloppe de réponse standard,
  handler d'erreurs global réactif.

LIVRABLE
- Arborescence complète des fichiers du projet.
- pom.xml parent + pom.xml des modules.
- Tout le code du contexte de référence.
- Instructions pour builder et lancer en local (mvn, variables d'env, DB requise).
Ne génère pas encore les autres contextes en détail : garde des placeholders.
```

## PROMPT 4 — Contexte IAM : utilisateurs, rôles & authentification

```
Implémente le contexte borné IAM en hexagonal, dans le squelette existant, en
respectant les conventions déjà définies. Réponds en FRANÇAIS pour les explications.

CONTEXTE :
[[ colle architecture + conventions + modèle de données (partie utilisateurs) ]]

EXIGENCES
- Inscription, connexion, profil ; rôles LEARNER / INSTRUCTOR / ADMIN.
- Sécurité RÉACTIVE : Spring Security WebFlux, mots de passe hachés (BCrypt/Argon2),
  JWT (access + refresh), filtre d'authentification réactif, autorisation par rôle.
- Endpoints : register, login, refresh, logout, me (profil), et administration des
  utilisateurs (réservée ADMIN).
- Domaine : User (agrégat), Role, Credentials en value objects ; ports in/out ;
  use cases (RegisterUser, AuthenticateUser, RefreshToken, GetProfile...).
- Adaptateurs : controllers WebFlux, repository R2DBC, encodeur de mot de passe,
  fournisseur JWT.

LIVRABLE
- Tout le code du contexte IAM (domaine, application, adaptateurs).
- Configuration de sécurité réactive (SecurityWebFilterChain).
- Tests : use cases + un test d'intégration du flux register→login→appel protégé.
- Exemples d'appels (curl) pour tester chaque endpoint.
```

---

# TRACK 2 — Contextes métier

## PROMPT 5 — Contexte Catalogue : roadmaps, nœuds & ingestion

```
Complète/enrichis le contexte Catalogue (déjà amorcé) pour gérer les roadmaps comme
des GRAPHES de nœuds, et prévoir l'INGESTION des roadmaps fournies. Explications en
FRANÇAIS.

CONTEXTE :
[[ colle spécification + architecture + modèle de données (catalogue) ]]

EXIGENCES
- Modèle : Roadmap → Nœuds → Arêtes (prérequis indicatifs, non bloquants) ;
  regroupement par phases ; métadonnées par nœud (importance, durée estimée,
  certification associée, domaines d'examen + pondération quand pertinent).
- Endpoints : lister les parcours, obtenir un parcours avec son graphe, obtenir un
  nœud, rechercher.
- Fonction d'INGESTION : un format JSON pivot (fourni par le pipeline de contenu du
  Track 4) que la plateforme importe pour créer/mettre à jour un parcours complet
  (roadmap + nœuds + arêtes + métadonnées). Prévoir l'idempotence (ré-import).
- Les 10 roadmaps cibles (Backend, Software Design & Architecture, Docker,
  Kubernetes, Terraform, Rust, Prompt Engineering, AWS, Backend Performance,
  API Security) doivent toutes entrer dans ce modèle sans exception.

LIVRABLE
- Code complet du contexte (domaine, use cases, adaptateurs, DTOs).
- Le SCHÉMA du JSON pivot d'ingestion (documenté), + un exemple minimal.
- Endpoint(s) d'import (réservé INSTRUCTOR/ADMIN) + tests.
```

## PROMPT 6 — Contexte Apprentissage : progression, navigation libre & reprise

```
Implémente le contexte borné Apprentissage en hexagonal. C'est le cœur de
l'expérience. Explications en FRANÇAIS.

CONTEXTE :
[[ colle architecture + modèle de données (progression) ]]

EXIGENCES FONCTIONNELLES (essentielles)
- Inscription d'un apprenant à un parcours.
- NAVIGATION NON LINÉAIRE : l'apprenant ouvre n'importe quel nœud à tout moment,
  en sort, va ailleurs, revient — et retrouve EXACTEMENT la dernière leçon consultée
  dans ce nœud.
- Progression persistée EN BASE (pas seulement côté client) : par leçon (vue /
  complétée), par nœud (% + dernière leçon), par parcours (dernier nœud actif, %).
- REPRISE globale : "reprendre là où j'en étais", tous parcours confondus.
- Favoris / marque-pages ; séries d'apprentissage (streaks) ; attribution d'XP à la
  complétion et calcul du NIVEAU (débutant → pro) selon un barème paramétrable.

LIVRABLE
- Domaine (Enrollment, Progress, Bookmark, Streak, Xp/Level comme value objects),
  ports, use cases (Enroll, MarkLessonViewed, CompleteLesson, ResumeLast,
  ToggleBookmark, GetProgress...).
- Adaptateurs R2DBC + controllers WebFlux + DTOs + mapping.
- Tests couvrant précisément le scénario quitter-un-nœud / y-revenir / reprise
  globale, et le calcul d'XP/niveau.
```

## PROMPT 7 — Contexte Contenu : leçons structurées & authoring

```
Implémente le contexte Contenu en hexagonal. Explications en FRANÇAIS.

CONTEXTE :
[[ colle architecture + modèle de données (contenu) ]]

EXIGENCES
- Une leçon appartient à un nœud et se compose de SECTIONS ordonnées typées :
  Concept, Schéma (SVG/Mermaid), Exemple de code (langage + snippet), Exercice
  (énoncé + solution), Quiz (référence vers le contexte Évaluation).
- Endpoints de lecture (apprenant) et d'AUTHORING (INSTRUCTOR/ADMIN) : créer /
  éditer / réordonner leçons et sections, prévisualiser.
- Ingestion : accepter le JSON pivot produit par le pipeline de contenu (Track 4)
  pour peupler massivement les leçons d'un nœud.
- Rendu prêt pour le front (le front s'occupe de l'affichage ; ici on structure).

LIVRABLE
- Code complet du contexte + DTOs + tests.
- Schéma du JSON pivot "contenu d'un nœud" (leçons + sections), documenté + exemple.
```

## PROMPT 8 — Contexte Évaluation : quiz, défis & examens blancs chronométrés

```
Implémente le contexte Évaluation en hexagonal. Explications en FRANÇAIS.

CONTEXTE :
[[ colle architecture + modèle de données (évaluation) + parcours de certification ]]

EXIGENCES
- Quiz par leçon/nœud : questions (QCM, vrai/faux, scénario), options, bonne
  réponse, justification ; tentative + score.
- Défis de code : énoncé, squelette, solution attendue ; validation par comparaison
  aux points clés (pas d'exécution serveur requise) + feedback.
- EXAMENS BLANCS CHRONOMÉTRÉS par certification (KCNA en QCM ~90 min ; CKA/CKAD en
  tâches pratiques ~2 h) : minuteur, ensemble de questions/tâches, score PAR DOMAINE
  d'examen, rapport des points faibles, historique des tentatives.
- Mode RÉVISION CIBLÉE : ressert en priorité les domaines où l'apprenant est faible.

LIVRABLE
- Domaine (Quiz, Question, Attempt, MockExam, ExamResult...), ports, use cases,
  adaptateurs R2DBC, controllers WebFlux, DTOs, mapping.
- Logique de scoring + agrégation par domaine + détection des points faibles.
- Tests des scénarios de notation et d'examen chronométré.
```

## PROMPT 9 — Gamification & progression débutant → professionnel

```
Implémente le contexte Gamification (ou intègre-le proprement à Apprentissage selon
l'architecture décidée). Explications en FRANÇAIS.

CONTEXTE :
[[ colle architecture + règles XP/niveaux du Prompt 6 ]]

EXIGENCES
- Système de NIVEAUX explicite débutant → professionnel (ex. paliers nommés) basé
  sur l'XP cumulée et la couverture des parcours.
- BADGES : règles d'obtention (compléter un nœud, un parcours, réussir un examen
  blanc, tenir une série de N jours, etc.).
- Événements : à la complétion d'une leçon / d'un examen, évaluer et attribuer XP,
  niveaux, badges (approche orientée événements interne, découplée).
- Endpoints : profil de progression gamifiée (niveau, XP, badges, prochain palier).

LIVRABLE
- Code du contexte + règles paramétrables + tests des règles d'attribution.
```

---

# TRACK 3 — Frontend React

## PROMPT 10 — Design system React (monochrome sobre, inspiration Apple)

```
Crée le DESIGN SYSTEM d'une application React pour la plateforme. UI en FRANÇAIS.

CONTRAINTES
- Palette : MONOCHROME sobre, inspiration Apple (définis les tokens) : neutres gris
  (valeurs Apple : fond #f5f5f7, texte #1d1d1f, gris #6e6e73, bordure #d2d2d7),
  accent quasi-noir (#1d1d20 clair / quasi-blanc en sombre), + variantes sombres.
  PAS de violet/bleu vif (« trop IA »). Contrastes accessibles (WCAG AA).
- Surfaces en VERRE (glassmorphism) pour le chrome (barre latérale, en-tête, popovers),
  cartes de contenu OPAQUES (jamais de verre sur verre).
- Style sobre, moderne, orienté lecture longue (belle typographie, largeur de texte
  maîtrisée ~66 caractères), mode clair/sombre.
- Stack front : React + Vite + React Router + un client HTTP (fetch/axios) + gestion
  d'état légère (Context/Zustand). Coloration syntaxique (Java, SQL, YAML, bash,
  Rust) via Prism.js (intégration directe).

LIVRABLE
- Tokens de design (CSS variables / thème) : couleurs, typographie, espacements,
  rayons, ombres, états focus.
- Bibliothèque de composants de base : Button, Card, Badge, Progress, Tabs, Modal,
  Toast, Input, CodeBlock, Callout, NodeCard (statut non commencé/en cours/terminé).
- Une page "Storybook maison" (galerie) démontrant tous les composants en clair et
  sombre.
- Arborescence du projet Vite + instructions de lancement local.
```

## PROMPT 11 — Shell applicatif & authentification (front)

```
Construis la coquille de l'app React et le parcours d'authentification, sur la base
du design system existant. UI en FRANÇAIS.

CONTEXTE :
[[ colle les endpoints IAM du Prompt 4 + tokens du design system ]]

EXIGENCES
- Routing : pages publiques (accueil, connexion, inscription) et privées (catalogue,
  parcours, nœud, leçon, tableau de bord, profil, admin).
- Auth : formulaires inscription/connexion, stockage sécurisé du token, rafraîchis-
  sement automatique, garde de routes selon le rôle, déconnexion.
- Layout : barre latérale de navigation, en-tête avec état de progression/niveau,
  bascule clair/sombre.

LIVRABLE
- Code du shell (layout, routing, contexte d'auth, client API typé) + pages auth.
- Gestion des erreurs et des états de chargement.
```

## PROMPT 12 — Expérience d'apprentissage (catalogue, roadmap interactive, leçon)

```
Construis l'expérience d'apprentissage côté front. C'est le cœur. UI en FRANÇAIS.

CONTEXTE :
[[ colle endpoints Catalogue (P5), Apprentissage (P6), Contenu (P7) + design system ]]

EXIGENCES
- Page CATALOGUE : les 10 parcours + parcours de certification, avec progression.
- VUE ROADMAP INTERACTIVE (façon roadmap.sh) : tous les nœuds d'un parcours,
  regroupés par phase, chacun cliquable À TOUT MOMENT, avec statut visuel (non
  commencé / en cours / terminé) et %.
- NAVIGATION LIBRE + REPRISE : entrer dans un nœud, en sortir, aller ailleurs,
  revenir → ré-ouverture exacte à la dernière leçon ; bandeau "Reprendre" global.
- VUE NŒUD : liste des leçons (cases cochées), progression du nœud.
- VUE LEÇON : sections rendues distinctement — Concept / Schéma (SVG/Mermaid) /
  Exemple de code (coloré) / Exercice (solution repliable) / Quiz — avec boutons
  précédent/suivant et marquage "leçon terminée" (persisté via l'API).
- Favoris, streak et niveau visibles.

LIVRABLE
- Toutes les pages/composants correspondants, câblés sur l'API réelle.
- Persistance de la progression via le backend (pas de localStorage comme source de
  vérité ; le serveur fait foi).
```

## PROMPT 13 — Évaluations, simulateur d'examen & tableaux de bord (front)

```
Construis les écrans d'évaluation et de pilotage. UI en FRANÇAIS.

CONTEXTE :
[[ colle endpoints Évaluation (P8) + Gamification (P9) + design system ]]

EXIGENCES
- Quiz interactifs : correction immédiate, feedback par option, score persisté.
- Défis de code : éditeur (CodeMirror/Monaco), énoncé, solution à révéler,
  validation via l'API.
- SIMULATEUR D'EXAMEN chronométré (KCNA/CKA/CKAD) : minuteur, navigation entre
  tâches, écran de résultat avec score PAR DOMAINE et points faibles ; historique.
- Mode révision ciblée.
- TABLEAU DE BORD : progression par parcours, couverture des domaines, niveau/XP,
  badges, séries, recommandations de la prochaine étape.

LIVRABLE
- Toutes les pages/composants, câblés sur l'API.
```

---

# TRACK 4 — Contenu, déploiement & qualité

## PROMPT 14 — Pipeline de génération de contenu (réutilise les 3 plateformes)

```
Tu es architecte pédagogique + ingénieur backend Java senior. Tu réponds en
FRANÇAIS. Objectif : produire, parcours par parcours, le CONTENU pédagogique complet
au FORMAT JSON PIVOT que la plateforme ingère (voir schémas des Prompts 5 et 7).

CONTEXTE :
[[ colle les schémas JSON pivot "roadmap" (P5) et "contenu d'un nœud" (P7) ]]

PARCOURS À TRAITER MAINTENANT : [[Backend Developer]]
(un seul parcours par exécution ; on répétera pour chacun des 10 + certifications)

EXIGENCES DE CONTENU
- Couvre TOUS les nœuds du parcours, sans exception, du niveau débutant au niveau
  professionnel.
- Pour chaque notion générique, ancre l'illustration côté Java/Spring quand c'est
  pertinent (ex. ORM → JPA/Hibernate ; REST → Spring Web ; broker → Spring Kafka).
- Chaque nœud → plusieurs leçons ; chaque leçon → sections : Concept (rigoureux,
  style GeeksforGeeks) / Schéma (SVG ou Mermaid, style Refactoring Guru, avant-après
  si utile) / Exemples de code NOMBREUX et réalistes (Java 21) / Exercice pratique
  (learn-by-doing, style Educative, avec solution) / Quiz (5-8 questions justifiées).
- Pour les parcours de certification (Docker/K8s → KCNA/CKA/CKAD) : ajoute des tâches
  pratiques mappées aux domaines d'examen et leur pondération.

FORMAT DE SORTIE
- Uniquement du JSON valide conforme au schéma pivot fourni (roadmap + contenu),
  prêt à être importé via les endpoints d'ingestion. Aucun texte autour.
- Traite le parcours en LOTS de nœuds si nécessaire pour préserver la profondeur ;
  indique clairement où reprendre au lot suivant.
```

## PROMPT 15 — Déploiement local (Docker Compose)

```
Rends la plateforme exécutable en local en une commande. Explications en FRANÇAIS.

CONTEXTE :
[[ colle architecture + config backend (P3) + front (P10/11) ]]

EXIGENCES
- docker-compose.yml : PostgreSQL 18 (avec uuidv7()), backend Spring Boot 4.1
  (WebFlux), frontend React (build servi par Nginx ou vite preview), réseau interne.
- Migrations Flyway appliquées au démarrage ; seed de démo chargé.
- Variables d'environnement documentées (.env.example) ; profils local/docker.
- Un script unique (make up / docker compose up) qui démarre tout, plus les URLs
  d'accès (front, API, docs OpenAPI).

LIVRABLE
- Tous les fichiers (compose, Dockerfiles backend & front, .env.example, README de
  démarrage pas à pas).
```

## PROMPT 16 — Tests & qualité

```
Mets en place la stratégie de tests. Explications en FRANÇAIS.

CONTEXTE :
[[ colle architecture + un ou plusieurs contextes déjà implémentés ]]

EXIGENCES
- Backend : tests unitaires du domaine (purs, rapides), tests d'intégration avec
  Testcontainers (PostgreSQL 18) pour les adaptateurs R2DBC et les flux WebFlux,
  tests de sécurité (accès protégé/refusé selon rôle).
- Front : tests de composants clés + un test e2e du parcours "se connecter → ouvrir
  un nœud → terminer une leçon → voir la progression".
- Qualité : configuration lint/format, et un pipeline CI (GitHub Actions) build +
  tests.

LIVRABLE
- Exemples de tests représentatifs pour chaque catégorie + config CI.
```

---

## Récapitulatif de l'enchaînement

1. **P0–P1** : spécification + architecture (les deux documents ancres).
2. **P2–P4** : base de données, squelette backend, IAM/auth.
3. **P5–P9** : contextes métier (catalogue, apprentissage/reprise, contenu,
   évaluation/examens, gamification).
4. **P10–P13** : design system puis frontend (auth, apprentissage, évaluations,
   dashboards).
5. **P14** : génération du contenu réel, parcours par parcours (répété 10×
   + certifications) et import.
6. **P15–P16** : déploiement local + tests/CI.

**Rappels** : réinjecte toujours spécification + architecture en tête des prompts ;
avance un contexte à la fois et vérifie que ça compile/tourne ; garde des
conventions figées ; traite le contenu (P14) parcours par parcours, en lots.
```
