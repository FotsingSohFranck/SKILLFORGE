# Schéma JSON pivot — Ingestion d'une roadmap

> Format d'échange consommé par l'endpoint d'ingestion du contexte **catalogue**
> (`POST /api/v1/roadmaps/import`, réservé INSTRUCTOR/ADMIN). Un document = **un parcours
> complet** (roadmap + phases + nœuds + arêtes + métadonnées de certification).
>
> **Idempotent** : l'import fait un *upsert* par clés naturelles en préservant les `id`
> existants (voir §Idempotence). Réimporter le même document est sans effet de bord.

## Structure

```jsonc
{
  "slug": "backend",                 // clé naturelle de la roadmap (unique, requis)
  "title": "Backend Developer",      // requis
  "summary": "Les fondations du développement backend.",
  "kind": "TRACK",                   // TRACK | CERTIFICATION (défaut TRACK)
  "position": 0,                     // ordre d'affichage dans le catalogue

  // Présent uniquement si kind = CERTIFICATION
  "certification": {
    "code": "CKAD",                  // requis (unique)
    "examFormat": "PRACTICAL",       // MCQ | PRACTICAL
    "durationMinutes": 120,
    "passingScore": 66.0,
    "domains": [
      { "name": "Application Design and Build", "weight": 20, "position": 0 },
      { "name": "Application Deployment",        "weight": 20, "position": 1 }
    ]
  },

  "phases": [
    { "title": "Fondamentaux",          "position": 0 },
    { "title": "Données & performance", "position": 1 }
  ],

  "nodes": [
    {
      "slug": "caching",               // clé naturelle du nœud dans la roadmap (requis)
      "title": "Caching",              // requis
      "summary": "Accélérer avec un cache.",
      "phase": "Données & performance",// référence au `title` d'une phase (optionnel)
      "importance": "RECOMMENDED",     // CORE | RECOMMENDED | OPTIONAL (défaut RECOMMENDED)
      "estimatedMinutes": 75,
      "position": 1,
      // Pondération de ce nœud par domaine d'examen (certifications), optionnel :
      "examDomains": [
        { "domain": "Application Deployment", "weight": 10 }
      ]
    }
  ],

  "edges": [
    // Prérequis INDICATIFS (non bloquants) ; référencent les `slug` des nœuds.
    { "source": "sql", "target": "caching", "kind": "PREREQ" }
  ]
}
```

## Règles de validation (au bord + application)
- `slug`, `title` requis et non vides ; `kind` ∈ {TRACK, CERTIFICATION}.
- Les `slug` de nœuds sont **uniques** au sein du document.
- Chaque `edge.source` / `edge.target` référence un `slug` de nœud **présent** dans `nodes`.
- `node.phase`, si fourni, référence le `title` d'une phase **présente** dans `phases`.
- `node.importance` ∈ {CORE, RECOMMENDED, OPTIONAL} ; `edge.kind` ∈ {PREREQ}.
- Si `kind = CERTIFICATION`, le bloc `certification` est requis ; `node.examDomains[].domain`
  référence un `certification.domains[].name`.

## Idempotence (upsert par clés naturelles)
- **roadmap** : mise à jour si `slug` existe, sinon création.
- **phases** : appariées par `title` dans la roadmap.
- **nodes** : appariés par `slug` dans la roadmap — **l'`id` est préservé** (les autres contextes
  y référent en soft-ref).
- **edges** : appariées par `(source_node, target_node, kind)`.
- **node ↔ exam_domains** : la pondération du nœud est remplacée à chaque import.
- **Non géré (dette)** : la suppression des entités retirées du document (pruning). L'import
  met à jour et ajoute ; il ne supprime pas les nœuds/arêtes absents du pivot.

## Exemple minimal (TRACK)

```json
{
  "slug": "demo",
  "title": "Parcours démo",
  "kind": "TRACK",
  "phases": [ { "title": "Base", "position": 0 } ],
  "nodes": [
    { "slug": "intro", "title": "Introduction", "phase": "Base", "importance": "CORE", "position": 0 },
    { "slug": "suite", "title": "Pour aller plus loin", "phase": "Base", "position": 1 }
  ],
  "edges": [ { "source": "intro", "target": "suite", "kind": "PREREQ" } ]
}
```
