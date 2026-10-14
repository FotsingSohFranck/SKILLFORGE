# ADR-0003 — UUID v7 générés en base (`DEFAULT uuidv7()`)

**Statut :** Accepté · **Date :** 2026-07-18

## Contexte
Les clés primaires doivent être uniques, non devinables séquentiellement comme un entier
auto-incrémenté, et **index-friendly**. PostgreSQL 18 fournit la fonction native
`uuidv7()` (UUID ordonnés dans le temps).

## Décision
Toutes les PK sont de type `uuid` avec `DEFAULT uuidv7()` — **génération en base**. Si un ID
doit exceptionnellement être connu **avant** insertion, le générer **côté Java avec une lib
v7** (`com.github.f4b6a3:uuid-creator`), **jamais `UUID.randomUUID()`** (qui produit du v4,
non ordonné).

## Conséquences
- **+** IDs ordonnés temporellement → meilleure localité d'index (moins de fragmentation
  qu'un UUID v4 aléatoire).
- **+** Génération centralisée et cohérente côté base.
- **−** L'ID n'est connu qu'**après** insertion (`RETURNING id`) dans le cas nominal.
- **−** L'horodatage de création est **encodé** dans l'ID : ne pas exposer un UUID v7 comme
  identifiant public si cette date est sensible.

## Alternatives écartées
- **UUID v4 applicatif** : perte de l'ordre temporel, index moins efficaces.
- **BIGSERIAL** : séquentiel devinable, couplage fort à la base.
