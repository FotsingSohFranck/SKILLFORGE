# Lab — Kubernetes (`kubernetes`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Phase 3 (infra) — la plus lourde :
> un cluster jetable par session. Coût ressources élevé et durcissement spécifique requis
> (conteneur privilégié). Ne pas traiter comme un lab de code.**

## Famille & image

- **Famille** : infra (cluster Kubernetes jetable).
- **Image de base** : image outillée embarquant `kubectl` + un moteur de cluster-dans-conteneur
  (**k3d** de préférence, `kind` en repli) + `helm`, préchauffée avec les images système du
  cluster (plan de contrôle, CNI, éventuel ingress controller) **pré-tirées** dans l'image pour
  éviter tout `pull` réseau au démarrage.
- **Phase SF-91** : **3 (bac à sable infra, cluster jetable)**.
- **Réseau** : le conteneur de pratique tourne en **docker-in-docker privilégié** et crée un
  cluster k3d/kind **isolé** (réseau interne au conteneur). Pas de sortie Internet par défaut ;
  le registre/les images nécessaires sont préchargés. Aucun accès au réseau Compose de prod.
- **Ressources conseillées** : 2 vCPU, **1,5–2 Go** (un cluster k3d pèse lourd), pids élevés,
  TTL **20–30 min** (le temps de démarrage du cluster s'ajoute à l'épreuve), FS de travail dédié
  en écriture. **Faible concurrence** : file d'attente / limite stricte de sessions simultanées.

## Type d'épreuve

- **Nature** : l'apprenant **écrit / corrige des manifestes YAML** (Pod, Deployment, Service,
  ConfigMap, Secret, Ingress, PVC, HPA, NetworkPolicy, etc.) et les **déploie sur le cluster
  jetable** via `kubectl apply` (ou `helm install`), dans un workspace de départ fourni.
- **Mode de validation** : le **script de validation** applique les manifestes puis **interroge
  l'état réel du cluster**. Il vérifie que l'objectif est atteint :
  1. `kubectl wait` sur la condition attendue (ex. `--for=condition=Available deployment/...`,
     `--for=condition=Ready pod/...`) ;
  2. `kubectl get -o jsonpath` pour contrôler des champs précis (nombre de répliques prêtes,
     type de Service, ports, labels, variables issues d'une ConfigMap…) ;
  3. une **requête réseau** depuis un pod éphémère pour vérifier qu'un Service résout et répond.
  `exit 0` ⟺ l'état attendu est observé.
- **Principe anti-triche** : l'oracle (scripts et assertions) est **hors du workspace éditable**
  et monté/copié au moment de la validation ; l'apprenant ne voit que l'énoncé et les squelettes
  de manifestes à compléter.

## Épreuves pratiques par nœud (pistes)

Le parcours a 20 nœuds. Exemples de tâches pratiques (nœud → épreuve → ce que vérifie le
script). À affiner nœud par nœud lors de SF-91 (Phase 3).

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| Pods (`pods`) | Écrire un manifeste Pod avec une readiness probe correcte | `kubectl wait --for=condition=Ready pod/...` ; `jsonpath` sur la probe |
| Deployments (`deployments`) | Déployer 3 répliques et déclencher un rolling update | `jsonpath` `.status.readyReplicas == 3` ; rollout `status` à jour |
| Services & découverte (`services`) | Exposer un Deployment via un Service ClusterIP | `jsonpath` sur `.spec.type`/ports ; requête depuis un pod éphémère qui répond |
| Ingress & réseau (`ingress-networking`) | Router un host vers un Service via un Ingress | `kubectl get ingress -o jsonpath` ; `curl` via l'ingress controller préchargé |
| ConfigMaps & Secrets (`config-secrets`) | Injecter une ConfigMap en variables d'env d'un pod | `kubectl exec ... env` / `jsonpath` sur les valeurs attendues |
| Requests/limits & quotas (`resource-management`) | Fixer requests/limits et respecter un ResourceQuota | `jsonpath` sur `resources` ; le pod hors quota est bien rejeté |
| Stockage & volumes (`storage-volumes`) | Créer un PVC et le monter dans un pod avec état | PVC `Bound` ; `jsonpath` sur le `volumeMount` ; écriture persistée |
| Autoscaling (`autoscaling`) | Déclarer un HPA sur un Deployment | `jsonpath` sur cible/min/max ; `kubectl wait` sur le Deployment prêt |
| Ordonnancement (`scheduling`) | Placer un pod via nodeSelector / tolérations | pod `Running` sur le nœud ciblé ; `jsonpath` sur `.spec.nodeName` |

> Remarque : chaque épreuve part d'un **cluster vierge** et doit converger vers un **état
> observable**. Privilégier des assertions sur l'**état réel** (`kubectl get`/`wait`) plutôt que
> sur le texte des manifestes : on valide un résultat, pas une syntaxe.

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur de pratique (dind privilegie). exit 0 = reussi, != 0 = echec.
set -euo pipefail
export KUBECONFIG=/root/.kube/config
cd /workspace

# 0) Le cluster jetable est deja demarre par l'orchestrateur (k3d/kind), images pre-tirees.
kubectl cluster-info >/dev/null

# 1) Appliquer les manifestes de l'apprenant
kubectl apply -f /workspace/manifests/ >/dev/null

# 2) Attendre l'etat attendu (ici : un Deployment disponible)
if ! kubectl wait --for=condition=Available --timeout=90s deployment/web -n app; then
  echo "PRATIQUE_ECHEC : le deploiement n'est pas devenu disponible"
  exit 1
fi

# 3) Verifier des champs precis de l'etat reel du cluster
REPLICAS=$(kubectl get deploy/web -n app -o jsonpath='{.status.readyReplicas}')
SVC_TYPE=$(kubectl get svc/web -n app -o jsonpath='{.spec.type}')

if [ "${REPLICAS:-0}" -eq 3 ] && [ "$SVC_TYPE" = "ClusterIP" ]; then
  # 4) Verifier que le Service repond via un pod ephemere
  if kubectl run probe --rm -i --restart=Never -n app \
       --image=curlimages/curl --command -- curl -sf http://web >/dev/null; then
    echo "PRATIQUE_OK"
    exit 0
  fi
fi

echo "PRATIQUE_ECHEC : etat du cluster non conforme a l'attendu"
exit 1
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI
(sans divulguer le contenu de l'oracle, seulement le verdict et d'éventuels messages
d'échec filtrés).

## Spécificités / pièges

- **Temps de démarrage du cluster** : créer un cluster k3d/kind prend plusieurs secondes à
  plusieurs dizaines de secondes ; prévoir ce coût dans le TTL (20–30 min) et, si possible,
  **démarrer le cluster pendant que l'apprenant lit l'énoncé** plutôt qu'au clic « Valider ».
- **Mémoire** : un cluster-dans-conteneur est gourmand (1,5–2 Go). Sur une machine locale déjà
  tendue, **quotas stricts** et **faible concurrence** ; sérialiser les sessions via une file.
- **Images préchargées** : pré-tirer dans l'image toutes les images système (control plane, CNI,
  ingress) et les images applicatives des épreuves, et les **importer dans le cluster** (`k3d image
  import` / `kind load`) pour éviter tout `pull` réseau pendant l'épreuve (réseau coupé).
- **Conteneur privilégié = surface de sécurité maximale** : le dind privilégié équivaut à un accès
  quasi-root. Durcir à part (voir ADR-0008 §Sécurité, Phase 3 traitée spécifiquement) : réseau
  isolé sans sortie Internet, jamais le socket Docker de prod ni le réseau Compose de prod, segment
  réseau dédié, pas de primitive Docker exposée à l'utilisateur, **reaper** obligatoire.
- **Réseau du cluster isolé** : les vérifications réseau (Service, Ingress) se font **depuis
  l'intérieur** du cluster (pod éphémère `kubectl run ... curl`), pas depuis l'hôte.
- **Nettoyage** : détruire le cluster **et** le conteneur dind en fin de session ou par le reaper
  (TTL) ; vérifier qu'aucun cluster k3d/kind ni volume orphelin ne subsiste entre deux sessions.
- **Déterminisme** : attendre les conditions avec `kubectl wait --timeout` plutôt que des `sleep` ;
  toujours cibler un **namespace dédié** par épreuve pour repartir d'un état propre.
