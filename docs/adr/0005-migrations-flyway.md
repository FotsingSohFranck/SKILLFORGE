# ADR-0005 — Migrations de schéma via Flyway (JDBC au démarrage)

**Statut :** Accepté · **Date :** 2026-07-18

## Contexte
Le runtime est **R2DBC** (non bloquant), mais l'écosystème de migration de schéma le plus
mûr et versionné (`V1__`, `V2__`, …) repose sur JDBC. Il faut un schéma reproductible et un
seed de démo appliqués automatiquement au démarrage (`docker compose up`).

## Décision
Gérer le schéma avec **Flyway**, exécuté via une datasource **JDBC dédiée au démarrage
uniquement**. Le chemin runtime reste **exclusivement R2DBC**. Toute évolution de schéma =
**nouvelle migration versionnée** ; on n'édite **jamais** une migration déjà appliquée.

## Conséquences
- **+** Migrations versionnées, reproductibles, appliquées automatiquement au boot.
- **+** Seed de démo chargé de façon déterministe.
- **−** Une dépendance JDBC subsiste, strictement cantonnée au démarrage (exception assumée
  à [ADR-0001](0001-reactif-vs-bloquant.md)).

## Alternatives écartées
- **Liquibase** : équivalent fonctionnel ; Flyway retenu pour la simplicité des scripts SQL
  versionnés.
- **Migrations « maison » en R2DBC** : réinvention coûteuse et risquée.
