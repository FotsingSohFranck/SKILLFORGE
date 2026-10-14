# ADR-0002 — Monolithe modulaire (pas de microservices)

**Statut :** Accepté · **Date :** 2026-07-18

## Contexte
La plateforme est **self-hébergée en local** (Docker Compose), pour un nombre modeste
d'utilisateurs. Elle comporte plusieurs sous-domaines (`iam`, `catalogue`, `contenu`,
`apprentissage`, `evaluation`, `gamification`, `analytics`).

## Décision
Construire un **monolithe modulaire** : un **seul déployable**, découpé en **contextes
bornés** aux frontières nettes, chacun en **architecture hexagonale**. Build **Maven
multi-modules** (un module par contexte + `app` + `common`). Les contextes communiquent via
**ports exposés** (synchrone) ou **événements** (découplé), jamais par accès direct aux
tables d'un autre.

## Conséquences
- **+** Déploiement local trivial (un artefact) ; pas d'orchestration réseau/distribuée.
- **+** Frontières explicites qui préservent l'option d'extraction future en services.
- **+** Transactions et cohérence simplifiées (une seule base).
- **−** Discipline nécessaire pour ne pas franchir les frontières (revue, conventions).

## Alternatives écartées
- **Microservices** : surdimensionné pour un usage local, complexité opérationnelle
  injustifiée.
