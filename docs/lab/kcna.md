# Lab — KCNA (`kcna`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Relève de la Phase 3 (infra).**

> ⚠️ **À lire avant tout : KCNA est une certification QCM, pas pratique.**
> L'examen officiel KCNA (Kubernetes and Cloud Native Associate) est un **QCM de 90 min,
> seuil 75%** (cf. `content/kcna/roadmap.json` : `examFormat = MCQ`). L'évaluation
> **officielle et prioritaire** de ce parcours reste donc l'**examen blanc QCM** déjà prévu
> par le contexte `evaluation`, **pas** un lab exécutable.
>
> Le lab décrit ici est **optionnel, léger et facultatif** : un simple bac à sable
> d'**exploration** pour ancrer la pratique et démystifier `kubectl`. Il ne doit **pas**
> devenir la porte de validation principale des nœuds KCNA.
>
> **Priorité de chantier : densifier la banque de questions de l'examen blanc QCM**
> (chantier séparé, contexte `evaluation`). Ce lab est un *bonus* à traiter après, si le
> budget le permet. Ne pas sur-investir.

## Famille & image

- **Famille** : majoritairement **conceptuel** (théorie cloud native). Le lab, lui, relève
  de la famille **infra (Kubernetes)**.
- **Image de base** : `kubectl` + **k3d** (ou kind), cluster **mono-nœud** jetable
  préchargé ; client Kubernetes uniquement, pas d'outillage lourd (ni Helm, ni dind complet).
- **Phase SF-91** : **3 (bac à sable infra)**, mais en **usage léger** (exploration, pas
  d'administration de cluster).
- **Réseau** : interne au cluster k3d seulement ; **pas de sortie Internet**
  (`--network none` sur le conteneur hôte, exceptions explicites interdites par défaut).
- **Ressources conseillées** : 1 vCPU, 768 Mo–1 Go (un k3d mono-nœud pèse déjà lourd),
  pids ≤ 256, TTL 15 min, racine en lecture seule + volume de travail dédié.

> Note : comme tout bac à sable de Phase 3, le conteneur est **privilégié** (dind/k3d) et
> doit suivre le durcissement renforcé prévu par l'ADR-0008 (§Sécurité). Raison de plus
> pour n'activer ce lab que là où il apporte vraiment quelque chose, et jamais en masse.

## Type d'épreuve

- **Nature** : **exploration `kubectl`** sur un cluster k3d mono-nœud pré-amorcé (quelques
  objets de démonstration déjà déployés), plus **quelques manipulations simples** de
  manifestes basiques (créer un Pod, un Deployment).
- **Mode de validation** : le **script de validation** lit l'**état du cluster**
  (`kubectl get -o json`, `jsonpath`) et vérifie un fait simple et déterministe :
  - un objet attendu existe (ex. un Pod/Deployment créé par l'apprenant est `Running`) ;
  - ou l'apprenant a **trouvé la bonne réponse** et l'a écrite dans un fichier `reponse.txt`
    du workspace (ex. le nom d'un composant du plan de contrôle, le namespace d'un Pod).
- **Esprit** : **validation souple**, de type « l'apprenant a su observer / lister / créer »,
  sans objectif d'administration avancée. `exit 0` dès que le fait attendu est vrai.
- **Anti-triche** : léger. Le lab étant un bonus d'exploration (et non la validation
  officielle, qui reste le QCM), on ne vise pas le même niveau d'anti-triche que la Phase 1
  code. L'oracle (valeur attendue, manifeste de référence) reste hors du workspace éditable.

## Épreuves pratiques par nœud (pistes)

Le parcours a 15 nœuds, dont la majorité est **purement conceptuelle** (CNCF, patrons cloud
native, observabilité théorique…) et **ne reçoit aucun lab** : ces nœuds restent en
`LESSONS_ONLY` et sont couverts par l'examen blanc QCM. Seuls quelques nœuds à ancrage
Kubernetes se prêtent à une exploration légère. Exemples (à affiner lors de SF-91) :

| Nœud (slug) | Épreuve d'exploration (légère) | Vérification (script) |
|---|---|---|
| `fundamentals` | Lister les objets d'un namespace, écrire le nombre de Pods dans `reponse.txt` | `kubectl get pods -n <ns>` comparé au compte attendu |
| `k8s-architecture` | Identifier un composant du plan de contrôle et noter son nom dans `reponse.txt` | correspondance exacte avec l'oracle (ex. `kube-apiserver`) |
| `pods-workloads` | Créer un Pod `nginx` simple à partir d'un manifeste basique | `kubectl get pod <nom> -o jsonpath=.status.phase` == `Running` |
| `pods-workloads` | Créer un Deployment à 2 réplicas | `.status.readyReplicas` == `2` |
| `services-networking` | Lister les Services d'un namespace, trouver le type d'un Service donné | type (ClusterIP/NodePort) écrit dans `reponse.txt` == oracle |
| `observability` | `kubectl describe` un Pod et relever une info (ex. image du conteneur) | valeur relevée == oracle, ou présence de l'objet observé |

> Les nœuds **sans ancrage cluster** (`orchestration`, `containers-runtime`,
> `cloud-native-arch`, `microservices-patterns`, `cncf-landscape`, `telemetry-signals`,
> `delivery`, `gitops-cicd`, `config-storage`, `scheduling-scaling`) **restent théoriques** :
> pas de lab, évaluation par QCM. Garder la liste des nœuds « labés » **courte** (2–4 nœuds
> max) pour ne pas surcharger une certification qui est, par nature, un examen écrit.

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur du lab (kubectl + k3d mono-noeud).
# exit 0 = reussi, != 0 = echec. Validation SOUPLE : on verifie un fait simple.
set -euo pipefail

NS="demo"

# --- Variante A : l'apprenant a cree un objet dans le cluster ---
# Exemple : un Pod nomme "mon-pod" doit etre en phase Running.
phase="$(kubectl get pod mon-pod -n "$NS" -o jsonpath='{.status.phase}' 2>/dev/null || true)"
if [ "$phase" = "Running" ]; then
  echo "PRATIQUE_OK : le Pod est Running"
  exit 0
fi

# --- Variante B : l'apprenant a ecrit une reponse d'observation ---
# L'oracle est monte hors du workspace editable (ex. /grader/attendu.txt).
if [ -f /workspace/reponse.txt ] && \
   diff -q <(tr -d '[:space:]' < /workspace/reponse.txt) \
           <(tr -d '[:space:]' < /grader/attendu.txt) >/dev/null; then
  echo "PRATIQUE_OK : bonne observation"
  exit 0
fi

echo "PRATIQUE_ECHEC : objet absent / reponse incorrecte"
exit 1
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI
(sans divulguer l'oracle, seulement le verdict et un message d'échec filtré). Rappel : pour
KCNA, un échec de lab **ne doit pas** bloquer la progression de la même façon qu'ailleurs —
privilégier une politique `LESSONS_ONLY` (défaut), le lab restant un bonus.

## Spécificités / pièges

- **KCNA = théorie / QCM.** Ne pas confondre : la vraie évaluation est l'**examen blanc QCM**.
  Le lab est un *supplément* d'exploration. **Investir d'abord dans la banque de questions**,
  pas dans un lab lourd.
- **Éviter la sur-ingénierie.** Un **k3d mono-nœud** en lecture/exploration suffit. Pas de
  multi-nœuds, pas de scénarios d'admin (upgrade, drain, RBAC avancé) : ça relève de CKA/CKAD,
  pas de KCNA.
- **Coût d'un cluster-dans-conteneur.** Même mono-nœud, k3d pèse en mémoire et au démarrage.
  Sur machine locale déjà tendue, limiter la **concurrence** et préchauffer l'image du cluster
  pour un démarrage rapide et **hors réseau**.
- **Validation souple.** On vérifie « a su observer / lister / créer », pas une config
  parfaite. Rester bienveillant : l'objectif est d'ancrer les concepts, pas de piéger.
- **Déterminisme.** Préamorcer le cluster avec des objets fixes (namespace `demo`, noms
  stables) pour que les vérifications `jsonpath`/`diff` soient reproductibles ; normaliser
  les espaces avant comparaison de la réponse texte.
- **Portée des nœuds.** Ne « laber » que les 2–4 nœuds à ancrage Kubernetes
  (`fundamentals`, `k8s-architecture`, `pods-workloads`, `services-networking`) ; laisser le
  reste en théorie/QCM.
