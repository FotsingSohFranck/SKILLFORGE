# Lab — CKA (`cka`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Relève de la Phase 3 (infra),
> la plus lourde** : cluster Kubernetes jetable par session, à traiter à part avec son
> propre durcissement. L'examen CKA étant **100% pratique**, le lab n'est pas un
> complément : il **est** l'évaluation idéale de ce parcours.

## Famille & image

- **Famille** : infra — administration Kubernetes (pas du code applicatif).
- **Image de base** : image d'admin outillée contenant `kubectl`, `kind` **et/ou** `k3d`,
  `etcdctl`, `crictl`, `jq`, `yq`, plus `kubeadm` **si et quand c'est simulable** (voir
  pièges). Le cluster d'épreuve est un **cluster jetable** monté par session via
  **kind/k3d** à l'intérieur d'un conteneur **docker-in-docker (dind) privilégié**.
- **Cluster** : viser un **kind multi-nœuds** (un control-plane + un ou deux workers) pour
  rendre crédibles les tâches d'admin (nœud NotReady, drain/cordon, placement).
- **Phase SF-91** : **3 (bac à sable infra)**.
- **Réseau** : interne au cluster uniquement. **Aucun egress** vers Internet par défaut
  (`--network none` au niveau du conteneur hôte dind, sauf exception explicite). Les images
  nécessaires (noeuds kind, CoreDNS, CNI, add-ons) sont **préchargées** dans l'image pour
  éviter tout pull réseau pendant l'épreuve.
- **Ressources conseillées** : conteneur **lourd** — viser 2 vCPU, 2–4 Go de mémoire, pids
  généreux (le cluster lance de nombreux process), TTL **étendu** (p. ex. 30–45 min pour
  tenir compte du temps de démarrage du cluster + la tâche), FS de travail dédié.
  **Faible concurrence** imposée (file de sessions, quelques labs CKA simultanés au plus).

## Type d'épreuve

- **Nature** : l'apprenant **administre un vrai cluster** via un **terminal navigateur**
  (xterm.js ↔ websocket ↔ shell du conteneur). Il exécute des commandes `kubectl`/`etcdctl`,
  édite des manifestes, répare des composants — exactement comme à l'examen CKA.
- **Provisionnement « cassé »** : pour les tâches de dépannage, le cluster est démarré puis
  **volontairement dégradé** par un script d'amorçage (kubelet arrêté, manifeste de pod
  statique erroné, RBAC manquant, Service sans endpoints…) **avant** que l'apprenant ne
  commence. L'énoncé décrit le symptôme, pas la cause.
- **Mode de validation** : le **script de validation** inspecte l'**état réel du cluster**
  après action (pas de QCM, pas de diff de fichiers). Exemples d'assertions :
  `kubectl get ... -o jsonpath`, `kubectl auth can-i`, présence/intégrité d'un fichier de
  snapshot (`etcdctl snapshot status`), un pod/nœud qui redevient `Ready`/`Running`, une
  StorageClass/PVC `Bound`. `exit 0` ⟺ l'état attendu est atteint.
- **Principe anti-triche** : le script de validation et les oracles sont **hors du shell de
  l'apprenant** et exécutés côté backend ; l'apprenant ne voit que l'énoncé et le cluster.

## Épreuves pratiques par nœud (pistes)

Le parcours a 15 nœuds mappés aux 5 domaines CKA (Install 25%, Workloads 15%, Networking
20%, Storage 10%, Troubleshooting 30%). Exemples de tâches (nœud → épreuve → ce que vérifie
le script). À affiner nœud par nœud lors de SF-91.

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| `etcd-backup` (Install) | Prendre un `etcdctl snapshot save` puis restaurer depuis un snapshot fourni | `etcdctl snapshot status` du fichier OK + une clé/objet témoin présent après restauration |
| `cluster-upgrade` (Install) | Monter le control-plane puis les nœuds d'une version à la cible | `kubectl get nodes -o jsonpath` : toutes les versions kubelet == version cible |
| `rbac-security` (Install) | Créer Role/RoleBinding donnant un droit précis à un ServiceAccount | `kubectl auth can-i <verbe> <ressource> --as=system:serviceaccount:...` == `yes`, et `no` ailleurs |
| `node-cluster-troubleshooting` (Troubleshooting) | Nœud `NotReady` (kubelet cassé / plan de contrôle : manifeste de pod statique erroné) à réparer | le nœud repasse `Ready` et le composant visé redevient `Running` (jsonpath sur `.status`) |
| `scheduling-advanced` (Workloads) | Poser taints/tolerations + affinité pour forcer un pod sur un nœud donné | `kubectl get pod -o jsonpath` : `.spec.nodeName` == nœud attendu, pod `Running` |
| `storage-classes` (Storage) | Créer une StorageClass + un PVC qui se lie par provisionnement dynamique | le PVC passe `Bound` et référence la bonne StorageClass (jsonpath) |
| `cni-dns` / `services-networking` (Networking) | Diagnostiquer un Service « muet » (sélecteur erroné / endpoints vides) et le réparer | `kubectl get endpoints <svc>` non vide + résolution/accès au Service OK |
| `app-logs-troubleshooting` (Troubleshooting) | Pod en `CrashLoopBackOff` : lire logs/events, corriger la cause | le pod atteint `Running`/`Ready` (jsonpath sur `.status.phase`/readiness) |

> Remarque : chaque tâche de dépannage s'appuie sur un **amorçage de cassure** déterministe
> (mêmes symptômes à chaque session) pour que le script de validation reste fiable.

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute cote backend contre le cluster jetable de la session.
# exit 0 = reussi, != 0 = echec. N'affiche jamais l'oracle complet a l'apprenant.
set -euo pipefail
export KUBECONFIG=/grader/kubeconfig   # kubeconfig admin, hors du shell apprenant

fail() { echo "PRATIQUE_ECHEC : $1"; exit 1; }

# Exemple : noeud NotReady repare (node-cluster-troubleshooting)
TARGET_NODE="kind-worker"

# 1) Le noeud cible doit etre Ready
READY=$(kubectl get node "$TARGET_NODE" \
  -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')
[ "$READY" = "True" ] || fail "le noeud $TARGET_NODE n'est pas Ready"

# 2) Les composants du plan de controle doivent etre Running
NOT_RUNNING=$(kubectl -n kube-system get pods \
  -l tier=control-plane \
  -o jsonpath='{.items[?(@.status.phase!="Running")].metadata.name}')
[ -z "$NOT_RUNNING" ] || fail "composants non Running : $NOT_RUNNING"

echo "PRATIQUE_OK"
exit 0
```

Variantes selon le nœud : `etcdctl snapshot status <fichier>` (backup), `kubectl auth
can-i ... --as=...` (RBAC), `kubectl get pvc -o jsonpath=...` == `Bound` (storage),
`kubectl get endpoints` non vide (networking). Le backend (`lab`) lit le **code de sortie**
(0 = `passed`) et renvoie à l'UI le **verdict** + d'éventuels messages d'échec filtrés,
sans divulguer les assertions exactes de l'oracle.

## Spécificités / pièges

- **dind privilégié à durcir** : un conteneur dind privilégié qui héberge kind/k3d est le
  point sensible de la Phase 3. Appliquer le durcissement ADR-0008 **adapté** : segment
  réseau dédié et **sans egress**, `--no-new-privileges` partout où possible, capabilities
  minimales, TTL + reaper obligatoires, **jamais** le réseau/volumes Compose de prod, jamais
  le socket Docker de l'hôte exposé à l'apprenant. Le shell apprenant ne pilote **que** le
  cluster, pas le démon Docker.
- **Cluster jetable coûteux** : démarrage lent (plusieurs dizaines de secondes à minutes) et
  mémoire élevée. Prévoir TTL généreux mais plafonné, **file d'attente** et **faible
  concurrence**. Envisager un **pool pré-chauffé** de clusters si le temps de démarrage nuit
  à l'expérience.
- **Préchargement des images** : embarquer toutes les images (nœuds kind, CNI, CoreDNS,
  add-ons) dans l'image du lab pour éviter tout pull réseau pendant l'épreuve (réseau coupé).
- **Scénarios « cassés » à provisionner avant l'épreuve** : l'amorçage de cassure doit être
  **déterministe et idempotent** pour que le symptôme soit identique à chaque session et que
  l'oracle reste fiable. Attention aux **courses** : attendre que le cluster soit stable
  (`kubectl wait`) avant d'injecter la panne, et avant de valider.
- **`kubeadm` difficile à simuler** : un vrai `kubeadm init/join`/upgrade suppose un hôte
  dédié. Dans kind/k3d, privilégier des **équivalents simulés** (démos de la procédure,
  tâches de upgrade sur les nœuds kind) plutôt qu'un kubeadm complet ; documenter ce qui est
  réellement exécutable vs. illustratif.
- **Nettoyage** : destruction systématique du cluster et du conteneur dind en fin de session
  ou par reaper ; vérifier qu'aucun volume/anonymous volume ne fuit (coût disque).
- **Déterminisme de validation** : toujours valider sur l'**état du cluster** via jsonpath,
  pas sur des fichiers éditables ; normaliser et attendre la convergence (readiness) avant
  de conclure pour éviter les faux échecs.

---

Fichier écrit : `C:\Users\SOH\Desktop\Projets\DevRoad\docs\lab\cka.md`
