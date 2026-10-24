#!/usr/bin/env bash
# Grader des epreuves pratiques backend. S'execute DANS le conteneur durci, en non-root,
# dans /workspace (tmpfs). exit 0 = reussi, != 0 = echec. Aucun acces reseau.
#
# Usage (validation_ref de la tache) : /grader/grade.sh <task-id>
# La soumission de l'apprenant a deja ete deposee dans /workspace avant l'appel.
set -euo pipefail

TASK="${1:?usage: grade.sh <task-id>}"
TDIR="/grader/tasks/${TASK}"
if [ ! -d "$TDIR" ]; then
  echo "TACHE INCONNUE : ${TASK}"
  exit 2
fi

cd /workspace

# 1) Squelette (pom + arborescence) SANS ecraser la soumission de l'apprenant (-n = no-clobber).
cp -rn /grader/skeleton/. . 2>/dev/null || true

# 2) Tests caches : toujours imposes et non modifiables (on efface d'abord un eventuel
#    src/test soumis par l'apprenant pour empecher toute triche).
rm -rf src/test
mkdir -p src/test/java
cp -r "${TDIR}/tests/." src/test/java/

# 3) Fichiers main eventuels fournis par la tache (fixtures), sans ecraser l'apprenant.
if [ -d "${TDIR}/main" ]; then
  cp -rn "${TDIR}/main/." src/main/java/ 2>/dev/null || true
fi

# 4) Compilation + tests hors-ligne, depot .m2 prechauffe en lecture seule.
export HOME=/workspace
if mvn -q -o -B -ntp -Dstyle.color=never -Duser.home=/workspace \
       -Dmaven.repo.local=/opt/.m2/repository test > /workspace/.mvn.log 2>&1; then
  echo "PRATIQUE_OK"
  exit 0
fi

echo "PRATIQUE_ECHEC : des tests ne passent pas"
# Extrait filtre : resume des tests et messages d'assertion, sans divulguer le code des tests.
grep -E "Tests run|expected:|but was:|AssertionFailedError|BUILD FAILURE|\[ERROR\]|symbol:|cannot find symbol|error:" \
     /workspace/.mvn.log | head -40 || true
exit 1
