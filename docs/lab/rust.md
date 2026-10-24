# Lab — Rust (`rust`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Phase 2 (autres parcours de code).**

## Famille & image

- **Famille** : code (Rust).
- **Image de base** : `rust:1-slim` (fournit `cargo` et `rustc`) avec un **registre crates
  préchargé** / un cache `cargo` local, pour que `cargo` tourne **hors-ligne** (`--offline`)
  sans aucun accès réseau pendant l'épreuve.
- **Phase SF-91** : **2 (exécution de code)**.
- **Réseau** : `none` (le cache `cargo` — index + crates nécessaires — est préchargé dans
  l'image / un cache monté en lecture seule). Aucune exception par défaut.
- **Ressources conseillées** : 1 vCPU, 512–768 Mo, pids ≤ 256, TTL 15 min, FS de travail
  dédié en écriture, racine en lecture seule. Prévoir une marge pour la **compilation Rust**
  (gourmande) : squelettes petits, dépendances minimales.

## Type d'épreuve

- **Nature** : l'apprenant **implémente / complète une fonction ou un type Rust** (gestion
  de l'ownership, enums, traits, erreurs, itérateurs, etc.) dans un workspace `cargo` de
  départ fourni.
- **Mode de validation** : le **script de validation** compile puis teste en mode hors-ligne.
  Deux variantes selon la tâche :
  1. **Tests fournis** (préféré) : des tests cachés (non modifiables par l'apprenant)
     s'exécutent via `cargo test --offline` ; `exit 0` ⟺ tous verts.
  2. **Sortie attendue** : on exécute le binaire (`cargo run --offline`) et on compare stdout
     à un attendu normalisé.
- **Principe anti-triche** : les fichiers de test / l'oracle sont **hors du workspace éditable**
  et copiés au moment de la validation ; l'apprenant ne voit que l'énoncé et le squelette.

## Épreuves pratiques par nœud (pistes)

Le parcours a 21 nœuds. Exemples de tâches pratiques (nœud → épreuve → ce que vérifie le
script). À affiner nœud par nœud lors de SF-91.

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| `variables-types` — Variables & types scalaires | Écrire une fonction qui convertit/agrège des scalaires (ex. moyenne d'entiers en flottant) sans dépassement | tests `cargo test` cachés verts |
| `references-emprunts` — Références & emprunts | Compléter une fonction qui modifie un `Vec` via `&mut` sans le consommer | tests cachés verts (le borrow checker doit passer) |
| `enums-pattern` — Enums & pattern matching | Définir un enum et une fonction `match` exhaustive (ex. évaluer une expression / un `Option`) | tests cachés sur chaque variante |
| `error-handling` — Gestion des erreurs | Implémenter une fonction renvoyant `Result<T, E>` et propager avec `?` | tests cachés : cas Ok et cas Err |
| `collections` — Vec, String, HashMap | Implémenter un comptage d'occurrences via `HashMap` | tests cachés sur plusieurs entrées |
| `traits-generics` — Traits & génériques | Définir un trait + une impl générique (ex. `Summarize` sur deux types) | tests cachés appelant la méthode du trait |
| `closures-iterateurs` — Closures & itérateurs | Écrire une chaîne `iter().filter().map().collect()` produisant le résultat attendu | tests cachés sur le vecteur résultat |
| `structs` — Structs & méthodes | Compléter une struct et ses méthodes (constructeur + calcul) | tests cachés sur les méthodes |
| `projet-cli` — Projet guidé CLI | Compléter une commande qui lit des arguments et produit une sortie | exécution `cargo run` + comparaison stdout normalisée |

> Remarque : pour rester léger et offline, privilégier des épreuves **unitaires** (une
> fonction + ses tests dans un seul crate) sans dépendances externes ; réserver un binaire
> complet aux nœuds de synthèse (`projet-cli`).

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur. exit 0 = reussi, != 0 = echec.
set -euo pipefail
cd /workspace

# 1) Copier les tests caches (montes en lecture seule hors du workspace editable)
cp -r /grader/tests/* tests/ 2>/dev/null || true

# 2) Compiler + tester en mode hors-ligne (cache cargo prechauffe, aucun reseau)
if cargo test --offline --quiet --color never; then
  echo "PRATIQUE_OK"
  exit 0
else
  echo "PRATIQUE_ECHEC : des tests ne passent pas"
  exit 1
fi
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI
(sans divulguer le contenu des tests cachés, seulement le verdict et d'éventuels messages
d'échec filtrés).

> Variante « sortie attendue » : remplacer le bloc `cargo test` par un
> `cargo run --offline --quiet > /tmp/out.txt` puis un `diff` normalisé entre `/tmp/out.txt`
> et l'oracle `/grader/expected.txt`.

## Spécificités / pièges

- **Préchauffage du cache `cargo`** : construire une fois le squelette (`cargo build` /
  `cargo fetch`) au build de l'image pour que l'index crates et les crates requis soient
  présents ; sinon `--offline` échoue faute d'index. Aucun accès réseau pendant l'épreuve.
- **Temps de compilation Rust** : la compilation est lente et gourmande ; viser des
  squelettes **petits** (un crate, une fonction + ses tests), **éviter les grosses
  dépendances** (tokio, serde…) hors des nœuds qui les enseignent. Cela tient le TTL et la
  mémoire.
- **Isolation** : jamais un vrai projet du dépôt dans le conteneur ; un workspace `cargo`
  jouet dédié par tâche, le dossier `tests/` (oracle) injecté seulement à la validation.
- **Déterminisme de sortie** : normaliser la sortie (fins de ligne, espaces de fin) avant
  comparaison ; figer locale et fuseau si la tâche manipule des dates ; attention à l'ordre
  non déterministe d'un `HashMap` (trier avant d'afficher, ou tester le contenu, pas l'ordre).
- **Warnings vs erreurs** : ne pas activer `-D warnings` par défaut (un warning ne doit pas
  faire échouer une solution correcte), sauf si l'énoncé l'exige explicitement.
