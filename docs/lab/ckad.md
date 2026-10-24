# Lab — CKAD (`ckad`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Phase 3 (bac à sable infra).**

## Famille & image

- **Famille** : infra Kubernetes **orientée développeur** (tâches applicatives sur un cluster).
- **Image de base** : image outillée `kubectl` + **`k3d`** (ou `kind`) + **`helm`** + **`kustomize`**,
  avec un petit registre d'images applicatives préchargé localement (nginx, busybox, une
  image appli jouet) pour éviter tout pull réseau pendant l'épreuve.
- **Phase SF-91** : **3 (bac à sable infra, cluster jetable)**.
- **Cluster** : **jetable par session**, provisionné via **k3d/kind**. Un **seul nœud suffit**
  pour CKAD (épreuves de développeur, pas d'administration multi-nœuds) — nettement plus léger
  qu'un cluster multi-nœuds façon CKA.
- **Réseau** : sortant coupé par défaut ; seul le trafic *interne au cluster* est permis pour
  les scénarios (Service↔Pod, NetworkPolicy). Images tirées d'un cache/registre local.
- **Durcissement dind** : le conteneur de session embarque un cluster k3d/kind reposant sur
  **docker-in-docker privilégié**. C'est le point chaud sécurité de la Phase 3 : à traiter
  **à part**, avec son propre durcissement (voir § Spécificités / pièges).
- **Ressources conseillées** : 2 vCPU, 2–3 Go (un cluster-dans-conteneur pèse lourd), pids
  plafonnés, TTL 20–30 min, FS de travail dédié, **faible concurrence** (file de sessions).

## Type d'épreuve

- **Nature** : l'apprenant réalise des **tâches de développeur Kubernetes** dans un cluster
  jetable, via un **terminal dans le navigateur** (xterm.js ↔ websocket ↔ shell du conteneur)
  et/ou un éditeur de manifestes. Exemples : créer un **pod multi-conteneurs** (init/sidecar),
  un **Deployment** avec **rolling update** puis **rollback**, un **Job/CronJob**, injecter une
  **ConfigMap/Secret**, poser **requests/limits + probes**, exposer via **Service/Ingress**,
  restreindre avec une **NetworkPolicy**, déployer via **Helm/Kustomize**.
- **Mode de validation** : le **script de validation** applique (ou laisse l'apprenant appliquer)
  les manifestes, puis **interroge l'état réel du cluster** : `kubectl get -o jsonpath`,
  `kubectl wait --for=condition=...`, un **endpoint HTTP qui répond** depuis un pod de test, ou
  la **NetworkPolicy qui bloque effectivement** le trafic non autorisé. `exit 0` ⟺ état conforme.
- **Principe anti-triche** : le script et ses sondes sont **hors du workspace éditable**,
  exécutés au moment de la validation ; l'apprenant ne voit que l'énoncé et le squelette de
  manifestes. On vérifie **l'effet observable** (état du cluster), pas le texte du YAML.

## Épreuves pratiques par nœud (pistes)

Le parcours a 15 nœuds. Exemples de tâches (nœud → épreuve → ce que vérifie le script),
mappées aux domaines CKAD. À affiner nœud par nœud lors de SF-91 (Phase 3).

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| `multi-container-pods` — Pods multi-conteneurs *(App Design & Build)* | Créer un pod avec un **init container** + un **sidecar** partageant un volume | `kubectl wait` pod `Ready` ; `jsonpath` sur `initContainerStatuses`/nb de conteneurs ; fichier produit par l'init lu par le sidecar |
| `jobs-cronjobs` — Jobs & CronJobs *(App Design & Build)* | Créer un **Job** (completions/parallelism) ou un **CronJob** programmé | `kubectl wait --for=condition=complete job/...` ; `jsonpath` sur `.spec.schedule` et `.status.succeeded` |
| `rolling-updates` — Rolling updates & rollbacks *(App Deployment)* | Déployer v1, faire un **rolling update** vers v2, puis **rollback** | `jsonpath` sur l'image des pods après `rollout undo` ; `kubectl rollout status` OK ; revision attendue |
| `configmaps-secrets` — ConfigMaps & Secrets *(Env, Config & Security)* | Injecter une **ConfigMap** (env) et un **Secret** (volume) dans un pod | `kubectl exec` lit la variable/le fichier monté ; `jsonpath` sur `envFrom`/`volumes` ; valeur attendue présente |
| `resource-limits` — Ressources & quotas *(Env, Config & Security)* | Poser **requests/limits** conformes et respecter un **ResourceQuota/LimitRange** | `jsonpath` sur `resources.requests/limits` ; pod `Running` sous le quota ; dépassement rejeté |
| `probes-logging` — Sondes & diagnostic *(Observability & Maintenance)* | Ajouter **liveness/readiness/startup** et obtenir un pod `Ready` | `kubectl wait --for=condition=Ready` ; `jsonpath` sur les 3 probes ; endpoint de readiness qui répond |
| `services-ingress` — Services & Ingress *(Services & Networking)* | Exposer un Deployment via **Service** + **Ingress** et router le HTTP | `kubectl run` d'un pod de test → `curl` le Service/Ingress → **200 attendu** ; `jsonpath` sur endpoints non vides |
| `network-policies` — NetworkPolicies *(Services & Networking)* | Poser une **NetworkPolicy** n'autorisant qu'un namespace/label donné | pod autorisé → connexion **OK** ; pod non autorisé → connexion **bloquée (timeout)** |
| `helm-kustomize` — Helm & Kustomize *(App Deployment)* | Déployer via un **chart Helm** paramétré ou un **overlay Kustomize** | `helm status`/`kubectl get` : release déployée, valeurs surchargées appliquées (`jsonpath`) |

> Remarque : rester **offline** (registre local, pas de pull) et **mono-nœud** ; les épreuves
> NetworkPolicy exigent un CNI qui **applique** les policies (k3d/kind : vérifier le CNI ou en
> installer un léger au provisioning). Les nœuds de synthèse (`design-build`, `deployment`,
> `config-security`, `observability`, `networking`) peuvent réutiliser l'épreuve de leur sous-nœud.

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur de session (cluster k3d/kind deja up).
# exit 0 = reussi, != 0 = echec.
set -euo pipefail
export KUBECONFIG=/root/.kube/config
NS=lab

# 1) Appliquer la solution de l'apprenant (manifestes de son workspace)
kubectl apply -n "$NS" -f /workspace/ >/dev/null

# 2) Attendre que l'etat se stabilise
kubectl wait -n "$NS" --for=condition=Available deploy/web --timeout=90s

# 3) Verifier l'etat ATTENDU (exemples, a adapter par tache)
#    a) image apres rollback
IMG=$(kubectl get -n "$NS" deploy/web -o jsonpath='{.spec.template.spec.containers[0].image}')
[ "$IMG" = "nginx:1.25" ] || { echo "PRATIQUE_ECHEC : image attendue nginx:1.25, obtenu $IMG"; exit 1; }

#    b) un endpoint qui repond via le Service
kubectl run probe -n "$NS" --image=busybox:1.36 --restart=Never --rm -i --quiet -- \
  wget -qO- --timeout=5 http://web.$NS.svc.cluster.local | grep -q "OK" \
  || { echo "PRATIQUE_ECHEC : le service ne repond pas"; exit 1; }

#    c) NetworkPolicy : le trafic non autorise doit etre BLOQUE (on attend un echec)
if kubectl run intrus -n "$NS" --image=busybox:1.36 --restart=Never --rm -i --quiet -- \
     wget -qO- --timeout=5 http://web.$NS.svc.cluster.local >/dev/null 2>&1; then
  echo "PRATIQUE_ECHEC : la NetworkPolicy ne bloque pas le trafic non autorise"; exit 1
fi

echo "PRATIQUE_OK"
exit 0
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI
(verdict + messages d'échec filtrés, sans divulguer les sondes cachées).

## Spécificités / pièges

- **Cluster jetable = coût mémoire + démarrage** : provisionner le cluster k3d/kind pèse
  (plusieurs centaines de Mo + quelques secondes à dizaines de secondes). **Préchauffer**
  l'image, **précharger** les images applicatives dans un registre/cache local, viser un
  **cluster mono-nœud** minimal, et **plafonner la concurrence** (file de sessions).
- **Un seul nœud suffit** : CKAD est un examen de *développeur* — pas besoin de workers/control
  plane séparés comme pour CKA. Garder le cluster le plus léger possible.
- **Durcissement dind (point chaud sécurité)** : le dind nécessaire à k3d/kind est **privilégié**.
  À isoler fortement : réseau/segment dédié, jamais le socket Docker de prod ni le réseau Compose
  applicatif, `--no-new-privileges` et seccomp partout où c'est possible, TTL + **reaper**
  obligatoires, quotas CPU/mémoire/pids stricts, destruction garantie du cluster et du conteneur
  en fin de session. Traiter ce durcissement **à part** du pilote Phase 1.
- **Scénarios à provisionner** : beaucoup d'épreuves supposent un **état de départ** (namespace
  `lab`, Deployment v1 existant, pods « client »/« serveur » étiquetés pour les NetworkPolicies,
  release Helm de base). Le provisioning doit poser cet état **avant** d'ouvrir le terminal.
- **NetworkPolicy réellement appliquée** : par défaut certains CNI de k3d/kind n'appliquent pas
  les policies. Vérifier/installer un CNI qui les **enforce**, sinon l'épreuve de blocage est un
  faux positif.
- **Nettoyage** : supprimer le cluster (`k3d cluster delete`) **et** le conteneur dind en fin de
  session ; ne jamais laisser un cluster orphelin consommer la mémoire de la machine locale.
- **Déterminisme** : fixer les versions d'images (tags précis, pas `latest`), utiliser
  `kubectl wait` plutôt que des `sleep`, et normaliser les comparaisons `jsonpath` (trim).
