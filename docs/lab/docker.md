# Lab — Docker (`docker`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Relève de la Phase 3 (infra lourde).**

> ⚠️ **Privilégié — à durcir à part.** Contrairement à la Phase 1 (code verrouillé,
> `--network none`, non-root), ce parcours exige un **démon Docker accessible dans la
> session** : soit **docker-in-docker (`dind`) en conteneur privilégié**, soit un **accès
> contrôlé au démon** via proxy. C'est une surface de sécurité majeure (quasi-root sur
> l'hôte si mal cadré). Ne pas réutiliser le durcissement de la Phase 1 tel quel : la
> Phase 3 fera l'objet de son **propre ADR de durcissement** (dind/k3d). Rien ici ne doit
> toucher au **démon de l'hôte de prod** ni au réseau Compose de prod.

## Famille & image

- **Famille** : infra.
- **Image de base** : `docker:dind` (démon + client Docker + BuildKit) lancée en
  **conteneur privilégié** avec son propre démon isolé ; **alternative** : une image
  cliente légère (`docker:cli` + buildx/BuildKit) qui parle à un démon dédié via un
  **socket proxy en liste blanche** (ex. `tecnativa/docker-socket-proxy`) plutôt que de
  monter le socket de l'hôte brut. Ajouter `hadolint` dans l'image pour le lint de
  Dockerfile.
- **Phase SF-91** : **3 (bac à sable infra, privilégié)**.
- **Réseau** : **pas de `--network none`** possible (un build/pull a besoin du réseau).
  Cadrer par un **registre miroir local** (pull-through cache) sur un **segment réseau
  dédié** ; pas d'accès Internet ouvert par défaut, pas d'accès au réseau Compose de prod.
- **Ressources conseillées** : 1–2 vCPU, **1–2 Go** (le démon + les builds pèsent lourd),
  pids ≤ 512, **TTL 20–30 min**, FS de travail dédié en écriture, et surtout **faible
  concurrence** (file de sessions) car chaque session porte un démon complet.

## Type d'épreuve

- **Nature** : l'apprenant **écrit un `Dockerfile` ou un `compose.yaml`**, **construit**
  une image (`docker build`), puis **lance** un ou plusieurs conteneurs (`docker run` /
  `docker compose up`) dans la session, à partir d'un workspace de départ fourni (sources
  applicatives jouet + énoncé).
- **Mode de validation** : le **script de validation** s'exécute dans la session, pilote le
  démon de la session (`docker build`, `docker run`…), puis **inspecte le résultat** :
  image construite, conteneur **sain** (healthcheck / up), **port qui répond**, **taille
  d'image** sous un seuil, **bonnes pratiques** détectées via `docker image inspect`
  (user non-root, port exposé, entrypoint) et/ou `hadolint` sur le Dockerfile.
- **Principe anti-triche** : l'**oracle** (tags attendus, seuils, requêtes de vérification,
  règles hadolint) vit **hors du workspace éditable** et est copié au moment de la
  validation ; l'apprenant ne voit que l'énoncé et le squelette. Le verdict renvoyé est le
  **code de sortie**, sans divulguer les vérifications internes.

## Épreuves pratiques par nœud (pistes)

Le parcours a 16 nœuds. Exemples de tâches pratiques (nœud → épreuve → ce que vérifie le
script). À affiner nœud par nœud lors de SF-91 (Phase 3).

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| Les bases de Docker (`docker-basics`) | Lancer un conteneur depuis une image fournie et le garder sain | `docker ps` montre le conteneur `running`/`healthy` ; `docker inspect` OK |
| Construire des images (`building-images`) | Écrire un `Dockerfile` pour une petite app et la builder | `docker build` réussit ; `docker run` démarre ; **port qui répond** (curl interne) |
| Construire des images — multi-stage & taille | Convertir en **multi-stage** pour réduire la taille | build OK + **taille d'image < seuil** via `docker image inspect -f '{{.Size}}'` |
| Persistance des données (`data-persistence`) | Monter un **volume nommé** et y persister un fichier | donnée présente après `docker rm` + recréation ; `docker volume inspect` OK |
| Réseau des conteneurs (`container-networking`) | Créer un réseau, y relier 2 conteneurs qui se résolvent par nom | résolution DNS inter-conteneurs OK ; port **publié** répond depuis l'hôte de session |
| Exécuter des conteneurs (`running-containers`) | Écrire un `compose.yaml` (app + base) et le monter | `docker compose up -d` OK ; service applicatif répond ; dépendance liée |
| Sécurité des conteneurs (`container-security`) | Durcir une image (user non-root, pas de secret, base fine) | `docker image inspect` : **USER ≠ root** ; **hadolint** sans violation bloquante |

> Remarque : rester le plus **léger** possible malgré la famille infra — sources jouet
> minuscules, images de base fines (`alpine`/`distroless`), builds courts — pour tenir le
> TTL et le budget mémoire. Les épreuves orchestrateur (Swarm/Kubernetes du nœud
> `deploying-containers`) relèvent des parcours dédiés (kubernetes), pas de ce lab.

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS la session (demon Docker de la session disponible).
# exit 0 = reussi, != 0 = echec.
set -euo pipefail
cd /workspace

# 0) Lint du Dockerfile (oracle hors workspace editable)
if [ -f Dockerfile ]; then
  hadolint --config /grader/hadolint.yaml Dockerfile
fi

# 1) Construire l'image (BuildKit ; pull depuis le registre miroir local)
docker build -t lab/app:candidate .

# 2) Verifier la taille sous le seuil (ex. 150 Mo)
SIZE=$(docker image inspect lab/app:candidate -f '{{.Size}}')
MAX=$((150 * 1024 * 1024))
if [ "$SIZE" -gt "$MAX" ]; then
  echo "PRATIQUE_ECHEC : image trop grosse ($SIZE octets)"
  exit 1
fi

# 3) Verifier l'utilisateur non-root (bonne pratique)
USER_CFG=$(docker image inspect lab/app:candidate -f '{{.Config.User}}')
if [ -z "$USER_CFG" ] || [ "$USER_CFG" = "root" ] || [ "$USER_CFG" = "0" ]; then
  echo "PRATIQUE_ECHEC : l'image tourne en root"
  exit 1
fi

# 4) Lancer le conteneur et attendre qu'il reponde
docker run -d --name candidate -p 8080:8080 lab/app:candidate
for i in $(seq 1 20); do
  if docker exec candidate wget -qO- http://localhost:8080/health >/dev/null 2>&1; then
    echo "PRATIQUE_OK"
    docker rm -f candidate >/dev/null 2>&1 || true
    exit 0
  fi
  sleep 1
done

echo "PRATIQUE_ECHEC : le service ne repond pas"
docker rm -f candidate >/dev/null 2>&1 || true
exit 1
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI
(sans divulguer l'oracle : seuils, règles hadolint et requêtes de vérification restent
cachés ; seul le verdict et d'éventuels messages d'échec filtrés remontent).

## Spécificités / pièges

- **Privilèges & isolation** : `dind` tourne **privilégié** — c'est le point sensible.
  Ne jamais monter le **socket de l'hôte de prod** ; démon **dédié par session** (dind) ou
  **socket proxy en liste blanche** (interdire `exec` arbitraire, montages, mode privilégié
  côté conteneurs enfants). Ce durcissement est traité dans un **ADR dédié Phase 3**.
- **Coût mémoire** : un démon Docker + un build par apprenant actif, c'est lourd sur une
  machine locale. **Quotas stricts** et **file de sessions** (faible concurrence) ;
  nettoyer agressivement images/conteneurs/volumes de la session (`docker system prune`)
  avant destruction.
- **Réseau à cadrer** : les builds/pull ont besoin du réseau → **registre miroir local**
  (pull-through cache) sur un **segment dédié**, pas d'Internet ouvert, jamais le réseau
  Compose de prod. Prévoir les images de base utilisées par les épreuves dans le miroir.
- **TTL & reaper obligatoires** : conteneur de session **privilégié** = ne jamais le
  laisser traîner. TTL 20–30 min + reaper qui détruit session **et** démon/volumes orphelins.
- **Démarrage à froid** : préchauffer le miroir avec les images de base (`alpine`,
  `node:slim`, `python:slim`…) pour que `docker build`/`pull` soit rapide et offline.
- **Déterminisme** : figer les **tags d'images** attendus (pas de `latest` flottant),
  normaliser les seuils de taille selon l'image de base retenue, et donner un endpoint de
  santé stable (`/health`) pour la sonde de validation.
