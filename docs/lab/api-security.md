# Lab — API Security (`api-security`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Phase 2 (autres parcours de code).**

## Famille & image

- **Famille** : code (Node ou Python), exécuté **localement** avec une **petite API jouet**
  volontairement vulnérable. Pas de JVM, pas de cluster.
- **Image de base** : `node:22-slim` **ou** `python:3.12-slim` selon l'implémentation de
  l'API jouet du nœud, enrichie des outils d'attaque/diagnostic : `curl`, `jq`, et un petit
  faux jeu de données en mémoire (SQLite/`better-sqlite3` ou dictionnaire Python).
  Dépendances **préinstallées** dans l'image (`node_modules` / venv figés), zéro `npm
  install`/`pip install` pendant l'épreuve.
- **Phase SF-91** : **2 (autres parcours de code)**.
- **Réseau** : **pas d'accès Internet**. Deux topologies possibles selon la tâche :
  1. **Tout-en-un** (préféré, plus léger) : l'API jouet et le lab tournent dans le **même
     conteneur** ; les requêtes d'attaque visent `http://127.0.0.1:<port>`.
  2. **Deux conteneurs** : un conteneur « **API cible** » + le conteneur de lab sur un
     **réseau interne dédié** (`--internal`), sans route sortante. À réserver aux nœuds qui
     exigent une vraie séparation réseau (ex. CORS cross-origin, mTLS).
- **Ressources conseillées** : 1 vCPU, 512 Mo, pids ≤ 256, TTL 15 min, workspace en écriture,
  racine en lecture seule.

## Type d'épreuve

- **Nature** : l'apprenant **corrige une faille** dans le code source d'une **API jouet
  fournie** (le workspace de départ contient une API qui *fonctionne mais est vulnérable*).
  Exemples : ajouter un contrôle d'autorisation sur une ressource (BOLA/IDOR), **valider
  correctement un JWT** (signature + `exp` + `iss`/`aud`), **paramétrer une requête SQL**
  contre l'injection, poser une **politique CORS** correcte, ajouter du **rate limiting**.
- **Mode de validation** : le **script de validation** (re)lance l'API corrigée et lui
  **joue deux familles de requêtes** avec `curl` :
  1. une ou plusieurs **requêtes d'attaque** qui doivent désormais être **bloquées**
     (`401`/`403`/`429`, corps d'erreur normalisé, pas de fuite de données) ;
  2. une ou plusieurs **requêtes légitimes** qui doivent **continuer à marcher** (`200`/`201`
     + payload attendu).
  `exit 0` ⟺ l'attaque est coupée **ET** l'usage légitime est préservé. Cela interdit le
  « correctif » trivial qui casse tout (ex. tout renvoyer en `403`).
- **Principe anti-triche** : le **scénario d'attaque** (requêtes, jetons forgés, attendus) est
  un **oracle hors du workspace éditable** (`/grader/…`), copié au moment de la validation.
  L'apprenant ne voit que l'énoncé et le code source de l'API à corriger, **jamais** les
  assertions du grader.

## Épreuves pratiques par nœud (pistes)

Le parcours a 18 nœuds. Exemples de tâches pratiques (nœud → épreuve → ce que vérifie le
script). À affiner nœud par nœud lors de SF-91g.

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| `authz` — Autorisation / BOLA | Ajouter un contrôle de propriété sur `GET /orders/{id}` | attaque : user A lit l'`order` de B → doit être `403` ; légitime : A lit le sien → `200` |
| `jwt-security` — Sécuriser les JWT | Valider signature + `exp` + `aud` au lieu d'un `decode` naïf | jeton `alg:none` / expiré / mauvais `aud` → `401` ; jeton valide → `200` |
| `injection` — Injections SQL | Remplacer la concaténation par une **requête paramétrée** | payload `' OR '1'='1` → aucune fuite (`400`/vide) ; recherche normale → résultat exact |
| `cors-headers` — CORS & en-têtes | Restreindre `Access-Control-Allow-Origin` à l'origine de confiance | préflight d'une origine non listée → pas d'en-tête permissif ; origine autorisée → `200` + en-têtes de sécurité présents |
| `rate-limiting` — Limitation de débit | Ajouter un limiteur (ex. 5 req / 10 s par IP) sur `/login` | rafale > seuil → `429` + `Retry-After` ; usage sous le seuil → `200` |
| `input-validation` — Validation des entrées | Valider/typer le corps avant traitement | corps malformé / champ hostile → `400` normalisé ; corps valide → `201` |
| `owasp-api-top10` — OWASP API Top 10 | Corriger une faille « mass assignment » (champ `role` injectable) | payload tentant `role:admin` → ignoré/`403` ; création standard → `201` sans élévation |
| `threat-modeling` — Modélisation des menaces (conceptuel) | **Repli non exécutable** : produire un mini modèle de menace STRIDE | **réponse vérifiée** (présence des éléments attendus) / **artefact** déposé, pas d'exécution |

> Remarque : les nœuds **conceptuels** (ex. `threat-modeling`, en partie `security-basics`,
> `oauth2-oidc`) n'ont pas d'API à corriger → repli **réponse vérifiée (texte/regex) /
> artefact** prévu par l'ADR-0008, sans conteneur si possible. Les nœuds **exécutables** sont
> prioritaires pour le lab (`authz`, `jwt-security`, `injection`, `cors-headers`,
> `rate-limiting`, `input-validation`, `owasp-api-top10`, `common-attacks`).

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur. exit 0 = reussi, != 0 = echec.
set -euo pipefail
cd /workspace

# 1) Lancer l'API jouet CORRIGEE par l'apprenant, en tache de fond
#    (code de l'apprenant dans /workspace ; jamais une vraie appli du depot)
node server.js >/tmp/api.log 2>&1 &   # ou: python -m app &
API_PID=$!
trap 'kill "$API_PID" 2>/dev/null || true' EXIT

# 2) Attendre que l'API ecoute (healthcheck, pas de sleep aveugle)
BASE="http://127.0.0.1:3000"
for _ in $(seq 1 30); do
  curl -fsS "$BASE/health" >/dev/null 2>&1 && break
  sleep 0.2
done

# 3) L'oracle d'attaque vient de /grader (hors du workspace editable)
#    Requete d'ATTAQUE : doit etre BLOQUEE
code_attaque=$(curl -s -o /tmp/attaque.json -w '%{http_code}' \
  -H "Authorization: Bearer $(cat /grader/forged.jwt)" \
  "$BASE/orders/0199-victim-order")

#    Requete LEGITIME : doit continuer a MARCHER
code_legit=$(curl -s -o /tmp/legit.json -w '%{http_code}' \
  -H "Authorization: Bearer $(cat /grader/valid.jwt)" \
  "$BASE/orders/0199-own-order")

# 4) Verdict : attaque coupee (403) ET usage legitime preserve (200)
if [ "$code_attaque" = "403" ] && [ "$code_legit" = "200" ] \
   && jq -e '.id == "0199-own-order"' /tmp/legit.json >/dev/null; then
  echo "PRATIQUE_OK"
  exit 0
else
  echo "PRATIQUE_ECHEC : attaque=$code_attaque legit=$code_legit"
  exit 1
fi
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI
(sans divulguer le contenu de l'oracle `/grader`, seulement le verdict et d'éventuels
messages d'échec filtrés : codes HTTP obtenus vs attendus, jamais les jetons forgés).

## Spécificités / pièges

- **API jouet fournie dans l'image** : le workspace de départ contient une API **qui tourne**
  mais est volontairement vulnérable ; l'apprenant corrige, il ne part pas d'une page blanche.
  Garder cette API **minuscule** (un fichier serveur + un faux jeu de données en mémoire) pour
  tenir le TTL et la mémoire.
- **Oracle d'attaque hors du workspace éditable** : scénario de requêtes, jetons forgés et
  attendus montés en **lecture seule** sous `/grader`, copiés/joués seulement à la validation.
  L'apprenant ne peut ni les lire ni les modifier pour « faire passer » le script.
- **Attaque bloquée ET légitime préservée** : toujours tester les **deux côtés**. Un correctif
  qui casse l'usage normal (tout en `403`, ou qui vide la réponse) doit **échouer**.
- **Déterminisme** : pas d'horloge réelle fragile dans les assertions. Pour `jwt-security`,
  figer le temps (jeton avec `exp` passé/futur fixe, ou injecter l'horloge) ; pour
  `rate-limiting`, fenêtre et seuil **déterministes** et healthcheck au lieu d'un `sleep`
  aveugle ; normaliser le JSON (`jq`) avant comparaison.
- **Pas de vrai secret** : clés de signature JWT, mots de passe et jetons sont des **valeurs
  jouets** dédiées à l'épreuve, jamais une vraie clé du dépôt ni un secret de prod.
- **Réseau verrouillé** : topologie tout-en-un par défaut (`127.0.0.1`) ; réseau `--internal`
  seulement si le nœud l'exige (CORS cross-origin, mTLS), **jamais** de route sortante ni le
  réseau Compose de prod.
- **Isolation** : jamais l'image applicative réelle ni un vrai module du dépôt dans le
  conteneur ; une API jouet dédiée par tâche, racine en lecture seule + workspace en écriture.
