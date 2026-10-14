# Schéma JSON pivot — Ingestion du contenu d'un nœud

> Consommé par `POST /api/v1/nodes/{nodeId}/content/import` (contexte **contenu**, réservé
> INSTRUCTOR/ADMIN). Un document = **tout le contenu d'un nœud** (ses leçons et leurs sections).
> Le `nodeId` provient de l'URL ; le corps ne contient que `lessons`.

## Structure

```jsonc
{
  "lessons": [
    {
      "slug": "intro",              // clé naturelle de la leçon dans le nœud (requis)
      "title": "Introduction",      // requis
      "position": 0,
      "sections": [
        { "type": "CONCEPT",  "position": 0, "title": "Notion",  "body": "Texte markdown…" },
        { "type": "DIAGRAM",  "position": 1, "title": "Schéma",  "body": "graph TD; A-->B", "diagramFormat": "MERMAID" },
        { "type": "CODE",     "position": 2, "title": "Exemple", "body": "var m = new HashMap();", "codeLanguage": "java" },
        { "type": "EXERCISE", "position": 3, "title": "Exo",     "body": "Énoncé…", "solution": "Corrigé…" },
        { "type": "QUIZ",     "position": 4, "title": "Quiz",    "quizId": "…uuid…" }
      ]
    }
  ]
}
```

## Règles de validation
- Chaque leçon : `slug` et `title` requis ; les `slug` sont **uniques** dans le document.
- Chaque section : `type` ∈ {CONCEPT, DIAGRAM, CODE, EXERCISE, QUIZ} ; `position` **unique** au sein
  de la leçon ; `diagramFormat` ∈ {SVG, MERMAID} si présent.

## Idempotence
- **Leçons** : upsert par `(node_id, slug)` — l'`id` de la leçon est **préservé** (la progression du
  contexte apprentissage y réfère en soft-ref).
- **Sections** : **remplacées** intégralement à chaque import (DELETE puis INSERT selon le pivot) —
  aucune entité externe ne référence une section ; le contenu reflète donc exactement le document.

## Exemple minimal

```json
{
  "lessons": [
    {
      "slug": "intro",
      "title": "Introduction",
      "position": 0,
      "sections": [
        { "type": "CONCEPT", "position": 0, "title": "Notion", "body": "Un cache accélère les lectures." }
      ]
    }
  ]
}
```
