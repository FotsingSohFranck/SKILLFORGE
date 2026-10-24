# Lab — Backend (`backend`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Pilote de la Phase 1.**

## Famille & image

- **Famille** : code (JVM).
- **Image de base** : `eclipse-temurin:21-jdk` + Maven (ou image maven préchauffée avec un
  cache `.m2` local pour éviter tout accès réseau pendant l'épreuve).
- **Phase SF-91** : **1 (exécution de code)**.
- **Réseau** : `none` (le dépôt Maven est préchargé dans l'image / un cache monté en
  lecture seule). Aucune exception par défaut.
- **Ressources conseillées** : 1 vCPU, 512–768 Mo, pids ≤ 256, TTL 15 min, FS de travail
  dédié en écriture, racine en lecture seule.

## Type d'épreuve

- **Nature** : l'apprenant **écrit / complète du code Java** (une méthode, une petite classe,
  un handler, une requête) dans un workspace de départ fourni.
- **Mode de validation** : le **script de validation** compile puis exécute, et vérifie le
  résultat. Deux variantes selon la tâche :
  1. **Tests fournis** (préféré) : des tests JUnit cachés (non modifiables par l'apprenant)
     s'exécutent via `mvn -q -o test` ; `exit 0` ⟺ tous verts.
  2. **Sortie attendue** : on exécute un `main` et on compare stdout à un attendu normalisé.
- **Principe anti-triche** : les fichiers de test / l'oracle sont **hors du workspace éditable**
  et copiés au moment de la validation ; l'apprenant ne voit que l'énoncé et le squelette.

## Épreuves pratiques par nœud (pistes)

Le parcours a 26 nœuds. Exemples de tâches pratiques (nœud → épreuve → ce que vérifie le
script). À affiner nœud par nœud lors de SF-91g.

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| Fondations langage / collections | Implémenter une méthode (ex. regrouper/filtrer une liste) | tests JUnit cachés verts |
| Programmation réactive (Mono/Flux) | Compléter un pipeline réactif qui transforme un flux | test `StepVerifier` caché |
| REST / contrôleur | Écrire un handler qui renvoie le bon code + payload | test WebTestClient caché |
| Validation / erreurs | Mapper une exception métier vers un code HTTP | test caché sur la réponse d'erreur |
| Persistance (R2DBC) | Écrire une requête / un repository renvoyant le bon résultat | test d'intégration léger (H2/pg embarqué) ou mock |
| SQL / requêtes | Écrire une requête SQL produisant le jeu de lignes attendu | comparaison du résultat à l'attendu |
| Tests | Écrire un test qui échoue sur un bug donné puis le corriger | le test rouge passe au vert après correction |

> Remarque : pour rester léger et offline, privilégier des épreuves **unitaires** (pas de
> vraie base) ; les épreuves de persistance utilisent un double en mémoire ou un Postgres
> embarqué minimal uniquement si nécessaire (sinon Phase 2+).

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur. exit 0 = reussi, != 0 = echec.
set -euo pipefail
cd /workspace

# 1) Copier les tests caches (montes en lecture seule hors du workspace editable)
cp -r /grader/tests/* src/test/java/ 2>/dev/null || true

# 2) Compiler + tester en mode hors-ligne (cache .m2 prechauffe)
if mvn -q -o -Dstyle.color=never test; then
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

## Spécificités / pièges

- **Démarrage à froid Maven** : préchauffer le cache `.m2` dans l'image (build d'un projet
  jouet) pour que `mvn -o test` tourne sans réseau et vite.
- **Temps de compilation** : viser des squelettes **petits** (une classe + ses tests), pas
  un module complet, pour tenir le TTL et la mémoire.
- **Isolation** : jamais le vrai module `app`/`backend` du dépôt dans le conteneur ; un
  workspace jouet dédié par tâche.
- **Déterminisme** : normaliser la sortie (fins de ligne, espaces) avant comparaison ;
  figer locale et fuseau si la tâche manipule des dates.
