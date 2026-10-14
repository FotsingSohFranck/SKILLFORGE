# Pipeline de contenu SkillForge

Outils Node (sans build) pour **valider** puis **ingérer** le contenu pédagogique au format
JSON pivot, un parcours à la fois, via les endpoints d'import du backend.

```bash
cd content-pipeline
npm install            # ajv (validation de schéma)
npm run validate       # valide ../content (tous les parcours)
node validate.mjs ../content --only backend   # un seul parcours
```

## Organisation du contenu (`../content/<slug>/`)

```
content/
  <slug>/
    roadmap.json                # 1 parcours : roadmap + phases + nœuds + arêtes (+ certification)
    nodes/<node-slug>.json      # contenu d'un nœud : leçons + sections typées
    quizzes/<ref>.json          # quiz (référencé par une section QUIZ via quizRef)
    challenges/<name>.json      # défi de code rattaché à un nœud
    exams/<name>.json           # examen blanc (certifications uniquement)
```

Le **format authoré** utilise des **clés naturelles** (slugs, titres de phase, noms de domaine)
et des **références locales** (`quizRef`) plutôt que des UUID. L'ingesteur (SF-40b) résout ces
clés en identifiants réels à l'import (le nœud est créé par la roadmap ; son UUID est relu via
`GET /roadmaps/{slug}` puis injecté dans les imports de contenu/quiz/défis).

## Schémas (`schemas/`)

| Fichier | Document | Endpoint d'import cible |
| --- | --- | --- |
| `roadmap.schema.json` | `roadmap.json` | `POST /api/v1/roadmaps/import` |
| `node-content.schema.json` | `nodes/*.json` | `POST /api/v1/nodes/{id}/content/import` |
| `quiz.schema.json` | `quizzes/*.json` | `POST /api/v1/quizzes/import` |
| `challenge.schema.json` | `challenges/*.json` | `POST /api/v1/challenges/import` |
| `exam.schema.json` | `exams/*.json` | `POST /api/v1/exams/import` |

## Ce que le validateur vérifie

- **Structure** (ajv) : champs requis, types, `additionalProperties: false`, enums
  (`kind`, `importance`, `type` de section, `diagramFormat`, `examFormat`, `kind` d'item…).
- **Cohérence inter-fichiers** :
  - slugs de nœuds / titres de phase / leçons uniques ; `node.phase` référence une phase existante ;
  - arêtes `source`/`target` référencent des nœuds existants (et `source ≠ target`) ;
  - `certification` présente ⇔ `kind = CERTIFICATION` ; `node.examDomains[].domain` ∈ domaines ;
  - sections `QUIZ` → `quizRef` résolu ; sections `CODE` → `codeLanguage` présent ;
  - quiz : `ref` unique, `node` existant, chaque question a ≥ 1 option correcte ;
  - défis/examens : `node`/`domain` existants ; items `MCQ` (`quizRef`+`questionIndex` valides) /
    `TASK` (`prompt`+`taskSolution`) ; positions d'items uniques.

Sortie : liste d'erreurs `fichier: message` et **code de sortie 1** si au moins une erreur.

> `content/demo/` est un petit parcours d'exemple valide, utile pour tester le pipeline.
