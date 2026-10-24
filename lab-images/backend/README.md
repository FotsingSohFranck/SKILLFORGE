# Image de lab — parcours backend (famille code / JVM)

Image des épreuves pratiques exécutables du parcours **backend** (SF-91 / ADR-0008).
L'apprenant écrit du Java ; un grader compile et exécute des tests JUnit **cachés** ;
le code de sortie fait foi (`0` = réussi).

## Construire l'image

```bash
docker build -t skillforge-lab-backend:pilot lab-images/backend
```

Le dépôt Maven est **préchauffé** au build (projet jouet) pour que la correction tourne
ensuite **hors-ligne** et vite. À l'exécution, l'orchestrateur lance le conteneur durci :
réseau `none`, racine en lecture seule, `/workspace` en tmpfs, utilisateur non-root,
`cap-drop ALL`, `no-new-privileges`, quotas CPU/mémoire/pids.

## Anatomie

```
skeleton/          pom.xml (JUnit 5) + arborescence src/main/java/forge (vide)
preheat/           projet jouet, uniquement pour préchauffer le dépôt .m2 au build
grade.sh           grader générique : /grader/grade.sh <task-id>
tasks/<task-id>/
  tests/           tests JUnit cachés imposés au moment de la correction
  main/            (optionnel) fixtures à ajouter dans src/main/java
```

Flux de correction (`grade.sh`) :
1. la soumission de l'apprenant est déjà déposée dans `/workspace` ;
2. le squelette est copié **sans écraser** la soumission (`cp -n`) ;
3. `src/test` est **réinitialisé** puis rempli avec les tests cachés de la tâche
   (l'apprenant ne peut donc pas affaiblir les tests) ;
4. `mvn -o test` hors-ligne ; `exit 0` si tout passe, sinon extrait filtré des échecs.

## Tâches disponibles (pilote)

| task-id | Nœud (`content/backend`) | Fichier attendu | Sujet |
|---|---|---|---|
| `collections-grouping` | `java-language` | `src/main/java/forge/Solution.java` | regrouper une liste par parité |
| `caching-lru` | `caching` | `src/main/java/forge/LruCache.java` | cache LRU borné |
| `design-strategy` | `design-principles` | `src/main/java/forge/Checkout.java` | règles de remise par type de client |

Chaque tâche est rattachée à un nœud via un document `content/backend/practical/<node>.json`
(champ `validationRef` = `/grader/grade.sh <task-id>`), ingéré par le content-pipeline.

## Ajouter une tâche

1. créer `tasks/<task-id>/tests/*.java` (tests JUnit cachés) ;
2. reconstruire l'image ;
3. créer `content/<parcours>/practical/<node>.json` pointant `validationRef` vers
   `/grader/grade.sh <task-id>`, puis ré-ingérer le contenu.
