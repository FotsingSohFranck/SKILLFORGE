# Lab — Dapr (`dapr`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Famille infra/runtime — Phase 3.**

## Famille & image

- **Famille** : infra / runtime distribué (Dapr **self-hosted**).
- **Mode retenu** : **self-hosted** — la **CLI `dapr`** + le sidecar **`daprd`** + des
  **composants locaux** (state store, pub/sub, bindings) adossés à un **Redis** en conteneur.
  Plus léger et plus rapide à démarrer que l'alternative cluster.
- **Alternative (non retenue par défaut)** : Dapr sur **k3d** (control plane complet, injection
  du sidecar par annotation). Réservée aux nœuds vraiment liés à Kubernetes
  (`dapr-on-kubernetes`, `deployment-production`), et plus lourde (voir Phase 3 durcie de l'ADR).
- **Image de base** : image dédiée embarquant
  - la **CLI `dapr`** + le binaire **`daprd`** préinstallés (pas de `dapr init` réseau pendant
    l'épreuve : runtime et binaires préchargés, images de composants déjà présentes),
  - un **petit runtime applicatif** (node **ou** python) pour lancer l'app de l'apprenant,
  - un **Redis** accessible (sidecar/conteneur frère sur un réseau interne dédié) servant de
    **state store** et de **broker pub/sub**,
  - `curl`/`jq` pour piloter l'API Dapr depuis le terminal et le script de validation.
- **Phase SF-91** : **3 (bac à sable infra/runtime)**.
- **Réseau** : **segment interne dédié** app ↔ sidecar ↔ Redis uniquement ; **aucun** accès
  sortant par défaut (`--network` restreint au réseau du lab, pas celui de prod).
- **Ressources conseillées** : 1–2 vCPU, 768 Mo–1 Go (plusieurs process : app + daprd + Redis),
  pids ≤ 256, **TTL 15–20 min**, racine en lecture seule + volume de travail dédié, utilisateur
  non root, `--no-new-privileges`.

## Type d'épreuve

- **Nature** : l'apprenant **lance une application avec `dapr run`** (app-id, ports app/HTTP/gRPC,
  dossier de composants) puis **exerce un building block via l'API du sidecar** — invocation de
  service, état, pub/sub, secrets, configuration, bindings — en écrivant/complétant un petit
  composant YAML, un handler d'abonnement ou quelques lignes de code applicatif.
- **Mode de validation** : le **script de validation** interroge l'**API Dapr** par HTTP
  (`curl`) et/ou inspecte le **state store Redis**, puis compare au résultat attendu :
  1. **Aller-retour d'API** (préféré) : POST sur un building block, puis GET/vérification et
     **comparaison à l'attendu** (ex. écrire une clé d'état puis la relire ; publier un message
     puis vérifier qu'il a bien été consommé).
  2. **Effet observable** : vérifier qu'un composant est bien chargé (`/v1.0/metadata`), qu'un
     abonnement est actif, qu'un binding de sortie a produit l'effet attendu.
  3. **Repli (réponse vérifiée)** pour les nœuds très conceptuels non exécutables
     (ex. `building-blocks-overview`, `patterns-ecosystem`) : question à réponse vérifiée
     (texte/regex), sans exécution — comme prévu par l'ADR.
- **Principe anti-triche** : l'oracle (commandes de vérification, valeurs attendues, payloads de
  contrôle) est **hors du workspace éditable**, monté au moment de la validation ; l'apprenant ne
  voit que l'énoncé, le squelette d'app et les composants à compléter.

## Épreuves pratiques par nœud (pistes)

Le parcours a 20 nœuds. Exemples de tâches pratiques (nœud → épreuve → ce que vérifie le
script). À affiner nœud par nœud lors de SF-91g.

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| `first-app` (première app) | Lancer l'app via `dapr run` avec le bon app-id et les bons ports | `GET /v1.0/healthz` du sidecar OK + app-id présent dans `/v1.0/metadata` |
| `components-configuration` (composants) | Compléter le YAML d'un composant et le scoper à l'app-id | le composant apparaît chargé dans `/v1.0/metadata` (bon type/nom) |
| `service-invocation` (invocation) | Exposer une route et l'appeler via l'API d'invocation | `POST /v1.0/invoke/<app-id>/method/<m>` renvoie le payload attendu |
| `state-management` (état) | Écrire puis relire une clé dans le state store | POST `/v1.0/state/<store>` puis GET compare la valeur (et l'ETag si demandé) |
| `pubsub-messaging` (pub/sub) | Publier sur un topic et le consommer via un abonnement | message publié → apparaît côté consommateur / écrit en état, comparé à l'attendu |
| `bindings` (entrée/sortie) | Déclencher un binding de sortie vers Redis | `POST /v1.0/bindings/<name>` → effet observable (clé créée / valeur attendue) |
| `secrets-management` (secrets) | Lire un secret depuis le secret store local | `GET /v1.0/secrets/<store>/<key>` renvoie la valeur attendue (pas de secret en dur) |
| `configuration-api` (config) | Lire un item de configuration via l'API | `GET /v1.0/configuration/<store>?key=` renvoie la valeur attendue |

> Repli assumé pour les nœuds conceptuels (`dapr-overview`, `sidecar-architecture`,
> `building-blocks-overview`, `patterns-ecosystem`) et pour ceux qui exigeraient un vrai cluster
> (`dapr-on-kubernetes`, `deployment-production`) : réponse vérifiée, ou bascule k3d dédiée hors
> du socle self-hosted. Garder les building blocks exécutables (état, pub/sub, invocation,
> secrets, config, bindings) comme cœur des épreuves pratiques.

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur, sidecar daprd deja demarre (dapr run).
# exit 0 = reussi, != 0 = echec.
set -euo pipefail

DAPR_HTTP="http://127.0.0.1:${DAPR_HTTP_PORT:-3500}/v1.0"
STORE="statestore"
KEY="lab-check"
EXPECTED="pong"

# 0) Attendre que le sidecar soit pret (demarrage a froid : app + daprd + Redis)
for i in $(seq 1 30); do
  if curl -fsS "${DAPR_HTTP%/v1.0}/v1.0/healthz" >/dev/null 2>&1; then break; fi
  sleep 1
done

# 1) Ecrire un etat via l'API Dapr
curl -fsS -X POST "${DAPR_HTTP}/state/${STORE}" \
  -H 'Content-Type: application/json' \
  -d "[{\"key\":\"${KEY}\",\"value\":\"${EXPECTED}\"}]"

# 2) Relire et comparer a l'attendu (sortie normalisee)
GOT="$(curl -fsS "${DAPR_HTTP}/state/${STORE}/${KEY}" | tr -d '"[:space:]')"

if [ "${GOT}" = "${EXPECTED}" ]; then
  echo "PRATIQUE_OK"
  exit 0
else
  echo "PRATIQUE_ECHEC : etat attendu='${EXPECTED}', obtenu='${GOT}'"
  exit 1
fi
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI
(sans divulguer l'oracle ni les payloads de contrôle, seulement le verdict et d'éventuels
messages d'échec filtrés). Pour une épreuve pub/sub, le même modèle publie
(`POST /v1.0/publish/<pubsub>/<topic>`) puis vérifie la consommation (clé d'état écrite par le
handler, ou endpoint de contrôle de l'app) au lieu du simple aller-retour d'état.

## Spécificités / pièges

- **Plusieurs process coordonnés** : une épreuve = app + **sidecar `daprd`** + **Redis**. C'est
  plus lourd qu'un conteneur de code ; prévoir la mémoire et une **faible concurrence** de
  sessions (cf. ADR). Lancer Redis et le sidecar **avant** l'app, et attendre leur disponibilité.
- **Démarrage à froid / temps de prêt** : `dapr run` met quelques secondes à charger les
  composants et établir le sidecar. Le script **doit attendre `/v1.0/healthz`** (boucle de
  retry) avant toute requête, sinon faux échecs. Préchauffer l'image (binaires + images Redis
  présentes) pour éviter tout `dapr init` réseau pendant le TTL.
- **Ports** : ne pas confondre **port applicatif** (`--app-port`), **port HTTP du sidecar**
  (`--dapr-http-port`, 3500 par défaut) et **port gRPC** (50001). Figer et exposer les ports
  attendus au script via variables d'env ; l'app écoute sur `app-port`, le script tape sur le
  port HTTP du sidecar.
- **Réseau interne** : app, sidecar et Redis communiquent sur un **segment dédié** ; **aucun**
  accès au réseau Compose de prod ni sortant. Les composants pointent sur l'hôte/port Redis
  **interne** du lab.
- **TTL, TTL d'état & nettoyage** : TTL du conteneur obligatoire + **reaper**. Attention aussi au
  **TTL d'état Dapr** (clé `ttlInSeconds`) qui peut faire disparaître une valeur avant la
  relecture ; le désactiver dans les épreuves d'état sauf si c'est justement l'objet du test.
- **Déterminisme** : **normaliser** la sortie (guillemets JSON, espaces, fins de ligne) avant
  comparaison ; purger/réinitialiser le state store (`FLUSHALL` Redis ou préfixe de clé par
  session) entre deux tentatives pour éviter qu'un reliquat fasse passer un test à tort.
- **pub/sub asynchrone** : la livraison n'est pas instantanée ; vérifier la consommation avec une
  **courte boucle de polling** bornée, pas un `GET` unique immédiat.
- **Isolation** : jamais l'image applicative de prod ni les composants de prod ; composants et
  app **jouets dédiés** par tâche, secrets de lab factices (pas de secret réel en dur).
