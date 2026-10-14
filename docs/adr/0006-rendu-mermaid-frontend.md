# ADR-0006 : Rendu des diagrammes Mermaid côté frontend

## Statut

Accepté — 2026-07-21

## Contexte

Les leçons contiennent des sections `DIAGRAM` au format **Mermaid** (ex. `graph TD …`).
Jusqu'ici, `LessonPage` les affichait en `<pre>` — c'est-à-dire la **source Mermaid brute**,
pas un diagramme. L'audit UX #2 (constat 03) a relevé que, là où une figure est attendue,
l'apprenant voit un pavé de syntaxe, ce qui nuit à la compréhension.

Deux options étaient sur la table :

1. **Rendre le Mermaid réellement** en ajoutant la dépendance `mermaid` (≈ 500 kb, + deps transitives).
2. **Repli léger sans dépendance** (bloc source stylé + légende), qui reste du texte.

Le projet a une **stratégie de dépendances volontairement minimale** (5 dépendances runtime ;
`CodeBlock` n'enregistre que les langages Prism utilisés pour limiter le bundle). Ajouter `mermaid`
va donc à l'encontre de cet ethos et **méritait une décision explicite** plutôt qu'un ajout silencieux.

## Décision

Option 1 retenue (validée par le Product Owner) : **rendre les diagrammes Mermaid** côté client.

- Nouveau composant `MermaidDiagram` (`components/MermaidDiagram.tsx`).
- `mermaid` est **chargé à la demande** (`await import('mermaid')`) → sorti du bundle initial,
  téléchargé seulement lorsqu'une leçon affiche un diagramme.
- Initialisé en `securityLevel: 'strict'` (SVG assaini par Mermaid).
- **Thème-aware** : re-rendu en thème `dark`/`default` selon le thème applicatif.
- **Repli robuste** : en cas d'erreur de parsing/rendu, on retombe sur la **source** en `<pre>`
  (aucune régression par rapport à l'existant).
- Utilisé dans `LessonPage` uniquement pour `diagramFormat === 'MERMAID'` ; les autres formats
  gardent le rendu `<pre>` + légende.

## Conséquences

- **+** Les diagrammes des leçons sont enfin **visuels** et lisibles.
- **+** Le chargement à la demande évite d'alourdir le bundle initial (impact concentré sur les
  pages de leçon contenant un diagramme).
- **+** Repli sûr : un diagramme mal formé n'affiche jamais une page cassée.
- **−** Dépendance runtime lourde ajoutée (≈ 500 kb + 111 paquets transitifs) — rupture assumée
  de la stratégie « bundle minimal ».
- **−** `npm install mermaid` a signalé **5 vulnérabilités transitives** (4 modérées, 1 haute).
  À suivre : audit régulier (`npm audit`), mise à jour de `mermaid`, sans `--force` (breaking).
- Alternative conservée si le coût devenait problématique : revenir au repli léger (option 2),
  le composant `MermaidDiagram` isolant déjà tout le couplage à la lib.
