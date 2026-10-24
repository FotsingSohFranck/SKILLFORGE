# Lab — Backend Performance (`backend-performance`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Phase 2** (après le pilote `backend`).

## Famille & image

- **Famille** : **mixte**. Ce parcours n'a pas une seule famille d'épreuve, il en a trois :
  - **(a) SQL / base de données** (phase « Base de données ») : index, plans d'exécution,
    N+1, pagination. L'apprenant écrit un index ou réécrit une requête ; validation par un
    script qui exécute `EXPLAIN (ANALYZE)` et inspecte le plan.
  - **(b) Code (JVM)** (phase « Application & I/O », et le code qui provoque du N+1) :
    micro-optimisations, cache, sérialisation ; validation par tests **déterministes**
    (assertions fonctionnelles, pas des mesures de temps).
  - **(c) Conceptuel** (phase « Mesurer » + nœuds de mise à l'échelle) : latence/percentiles,
    loi de Little, scaling ; repli **réponse vérifiée / calcul attendu** (pas d'exécution).
- **Image de base** : selon la tâche.
  - SQL : `postgres:18` (ou `postgres:18-alpine`) avec `psql` disponible, base **seedée** au
    démarrage (schéma + volume de données suffisant pour que les plans soient réalistes).
  - Code : `eclipse-temurin:21-jdk` + Maven, cache `.m2` préchauffé (comme le lab `backend`).
  - Conceptuel : aucune image lourde ; comparaison de chaîne/nombre côté backend, pas de conteneur
    (ou un conteneur minimal si on veut homogénéiser la chaîne).
- **Phase SF-91** : **2** (code + SQL, pas d'infra). Les nœuds de scaling (`db-scaling`,
  `horizontal-scaling`, `load-resilience`) restent **conceptuels** en Phase 2 — pas de vrai
  cluster ni de test de charge distribué (ce serait Phase 3).
- **Réseau** : `none`. La base Postgres tourne **dans le même conteneur** (ou un conteneur frère
  dédié à la session sur un segment isolé) ; jamais la base de prod ni le réseau Compose de prod.
- **Ressources conseillées** : code → 1 vCPU, 512–768 Mo ; SQL → 1 vCPU, 512 Mo–1 Go (un Postgres
  seedé coûte plus cher). TTL 15 min, FS de travail dédié, racine en lecture seule, pids ≤ 256.

## Type d'épreuve

- **Nature** : l'apprenant **fait** une des trois choses selon le nœud :
  - **SQL** : écrit un `CREATE INDEX ...` ou réécrit une requête dans un fichier `.sql` fourni.
  - **Code** : complète une méthode / un repository / un mapping dans un workspace Java de départ.
  - **Conceptuel** : saisit une valeur ou une courte réponse (p99 attendu, débit via loi de Little…).
- **Mode de validation** : le **script de validation** s'exécute dans le conteneur, `exit 0 = réussi`.
  - **SQL** : exécute la solution de l'apprenant puis un `EXPLAIN (ANALYZE, FORMAT …)` ; vérifie
    une **propriété structurelle du plan** (présence d'un `Index Scan`/`Index Only Scan`, **absence**
    de `Seq Scan` sur la grosse table, usage de l'index nommé, nombre de lignes lues). Et/ou compare
    le **jeu de lignes** retourné à l'attendu (tri, pagination, dédoublonnage corrects).
  - **Code** : `mvn -q -o test` avec des tests JUnit cachés **fonctionnels** (ex. le service émet
    **1** requête au lieu de N+1 via un `DataSource`/repository espion ; le payload sérialisé ne
    contient plus les champs lourds ; le cache sert le 2ᵉ appel sans retaper le dépôt espionné).
  - **Conceptuel** : comparaison de la réponse normalisée à l'oracle (tolérance numérique).
- **Principe anti-triche** : la solution attendue, les tests cachés et les requêtes `EXPLAIN` de
  contrôle sont **hors du workspace éditable**, montés en lecture seule et copiés au moment de la
  validation. L'apprenant ne voit que l'énoncé, le squelette et (SQL) le schéma + données.

## Épreuves pratiques par nœud

Le parcours a 18 nœuds. Exemples de tâches (nœud → épreuve → mode de validation). À affiner
nœud par nœud lors de SF-91. On indique explicitement le mode : **SQL**, **Code**, ou **Conceptuel**.

| Nœud (slug) | Épreuve pratique | Vérification (script) — mode |
|---|---|---|
| `db-indexes` | Ajouter le bon index pour une requête filtrée/triée lente | **SQL** : `EXPLAIN (ANALYZE)` → `Index Scan` présent, **plus de `Seq Scan`** sur la table ciblée |
| `query-optimization` | Réécrire une requête lente (sous-requête → jointure, éviter une fonction sur colonne indexée) | **SQL** : plan sans `Seq Scan`/sans `Sort` coûteux **et** jeu de lignes identique à l'attendu |
| `n-plus-one` | Corriger un service qui fait 1 requête par élément (fetch groupé / `IN (...)`) | **Code** : test compte les requêtes via un repository espion → **1** (ou constant), plus proportionnel à N |
| `pagination-batching` | Implémenter une pagination par curseur (keyset) au lieu d'un gros `OFFSET` | **SQL** : plan borné + lignes de la page attendue ; rejet si `OFFSET` géant |
| `caching-perf` | Ajouter un cache pour que le 2ᵉ appel ne retape pas la source | **Code** : test → 1ᵉʳ appel touche le dépôt espion, 2ᵉ non ; résultat identique |
| `serialization` | Alléger la réponse (DTO projeté, champs exclus, pas de graphe complet) | **Code** : test sur le JSON sérialisé → champs lourds absents, champs utiles présents |
| `memory-gc` | Supprimer une allocation inutile dans une boucle chaude (réutilisation/stream) | **Code** : test fonctionnel (même résultat) + assertion structurelle (pas de collection intermédiaire géante) |
| `latency-throughput` | Calculer un p99 depuis un échantillon / un débit via la loi de Little | **Conceptuel** : réponse numérique comparée à l'oracle (tolérance) |

> Nœuds laissés **conceptuels** en Phase 2 (repli réponse vérifiée, pas d'exécution) :
> `measure-first`, `profiling`, `benchmarking` (phase « Mesurer »), `async-reactive`,
> `compression-http`, `pooling`, `circuit-breaker`, `db-scaling`, `horizontal-scaling`,
> `load-resilience`. Ils dépendent soit de mesures peu déterministes, soit d'infra distribuée
> (Phase 3). On ne force la pratique exécutable que là où elle est **fiable**.

## Script de validation : modèle

Deux variantes selon le mode. Le backend (`lab`) ne lit que le **code de sortie** (0 = `passed`)
et renvoie un verdict filtré à l'UI (sans divulguer les tests cachés ni le plan complet).

### Variante SQL (index / plan d'exécution)

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur postgres. exit 0 = reussi.
set -euo pipefail
PSQL="psql -X -q -v ON_ERROR_STOP=1 -U lab -d labdb"

# 1) Appliquer la solution de l'apprenant (ex. un CREATE INDEX)
$PSQL -f /workspace/solution.sql

# 2) Recuperer le plan de la requete de controle (hors du workspace editable)
PLAN="$($PSQL -A -t -f /grader/explain.sql)"   # explain.sql = EXPLAIN (ANALYZE, BUFFERS) ...

# 3) Assertions STRUCTURELLES sur le plan (pas sur le temps)
if echo "$PLAN" | grep -Eq 'Seq Scan on +grosse_table'; then
  echo "PRATIQUE_ECHEC : Seq Scan encore present sur la table ciblee"
  exit 1
fi
if ! echo "$PLAN" | grep -Eq 'Index (Only )?Scan'; then
  echo "PRATIQUE_ECHEC : aucun Index Scan dans le plan"
  exit 1
fi

# 4) (optionnel) verifier que le resultat est inchange
if ! diff -q <($PSQL -A -t -f /grader/result.sql) /grader/expected.txt >/dev/null; then
  echo "PRATIQUE_ECHEC : le jeu de lignes a change"
  exit 1
fi

echo "PRATIQUE_OK"
exit 0
```

### Variante Code (JVM, tests caches)

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur temurin. exit 0 = reussi.
set -euo pipefail
cd /workspace

# Copier les tests caches (montes en lecture seule hors du workspace editable)
cp -r /grader/tests/* src/test/java/ 2>/dev/null || true

# Compiler + tester hors-ligne (cache .m2 prechauffe). Tests FONCTIONNELS,
# pas de bench de temps : on verifie p.ex. le nombre de requetes, pas la duree.
if mvn -q -o -Dstyle.color=never test; then
  echo "PRATIQUE_OK"
  exit 0
else
  echo "PRATIQUE_ECHEC : des tests ne passent pas"
  exit 1
fi
```

## Spécificités / pièges

- **Ne jamais noter sur le temps mesuré.** Un benchmark de perf est **peu déterministe** en
  conteneur (CPU partagé, JIT, GC, cache système). On valide des **propriétés structurelles** :
  forme du plan d'exécution (`Index Scan` vs `Seq Scan`), **nombre de requêtes** émises, taille/forme
  du payload, nombre d'allocations — jamais un seuil de millisecondes.
- **Plans d'exécution dépendants des données.** Un `EXPLAIN` sur une table quasi vide ne déclenche
  pas d'`Index Scan` (le planificateur préfère le `Seq Scan`). Il faut **seeder assez de lignes** et
  lancer `ANALYZE` à l'initialisation pour que les statistiques rendent le plan réaliste et stable.
- **Figer le planificateur si besoin.** Pour des assertions reproductibles, désactiver le
  parallélisme (`SET max_parallel_workers_per_gather = 0`) et fixer les `*_cost` / `random_page_cost`
  par défaut ; sinon un même index peut produire des plans différents selon la version/charge.
- **Coût d'un Postgres par session.** Un conteneur Postgres seedé est plus lourd qu'un simple JDK :
  préchauffer l'image (schéma + données + `ANALYZE` figés dans un volume ou une couche d'image),
  limiter la mémoire, et **borner la concurrence** de sessions SQL simultanées (voir ADR-0008).
- **Démarrage à froid Maven** (variante Code) : cache `.m2` préchauffé dans l'image pour que
  `mvn -o test` tourne sans réseau et vite, sous le TTL.
- **Normalisation.** Comparer les jeux de lignes avec un **tri déterministe** (`ORDER BY` explicite)
  et une sortie normalisée (`-A -t`, fins de ligne) ; côté conceptuel, prévoir une **tolérance
  numérique** et accepter les formes équivalentes (ex. `0,99` / `0.99`).
- **Isolation.** Jamais la vraie base `catalogue`/`apprentissage` ni le module `app` du dépôt dans
  le conteneur : un schéma jouet et un workspace dédiés par tâche.
- **Honnêteté du périmètre.** Sur ce parcours, seule une partie des nœuds a une épreuve exécutable
  fiable (SQL + quelques nœuds de code). Les nœuds « Mesurer » et « scaling/résilience » restent en
  repli conceptuel tant que la Phase 3 (infra, charge) n'est pas ouverte — ne pas fabriquer de faux
  labs de perf qui mesureraient du bruit.
