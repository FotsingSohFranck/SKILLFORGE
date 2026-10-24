# Lab — AWS (`aws`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Phase 3 (infra cloud émulée).**

## Famille & image

- **Famille** : infra **cloud émulée en local** — pas de vrai compte AWS, pas de vraies
  clés, pas d'appel vers `*.amazonaws.com`. Tout tourne contre **LocalStack**, un conteneur
  qui émule les API AWS en local (S3, IAM, Lambda, DynamoDB, SQS/SNS, STS, etc.).
- **Image de base** : image de lab légère avec **AWS CLI v2** + le wrapper **`awslocal`**
  (préconfiguré pour pointer sur l'endpoint LocalStack) + `jq` / `bash` pour le script de
  validation. En option un petit runtime (`python3`/`zip`) pour empaqueter une Lambda.
- **Service compagnon** : un conteneur **LocalStack** (édition communautaire) démarré pour
  la session, joignable par le conteneur de lab via un **réseau interne dédié** (ex.
  `http://localstack:4566`). Les deux conteneurs sont détruits ensemble en fin de session.
- **Phase SF-91** : **3 (bac à sable infra)** — plus lourd qu'un simple conteneur de code,
  à durcir à part (voir ADR-0008, §Sécurité et §Phase 3).
- **Réseau** : **pas d'accès Internet sortant**. Seule exception : le **segment interne
  lab ↔ LocalStack**. Aucune route vers le vrai AWS ni vers le réseau Compose de prod.
- **Ressources conseillées** : LocalStack est gourmand → 1–2 vCPU, **1–1,5 Go** pour le duo
  lab+LocalStack, pids plafonnés, **TTL 15–20 min**, racine en lecture seule + volume de
  travail dédié, `--no-new-privileges`, utilisateur non root, faible concurrence de sessions.

## Type d'épreuve

- **Nature** : l'apprenant **crée et configure des ressources AWS émulées** via `awslocal`
  (ou `aws --endpoint-url=http://localstack:4566 ...`), soit en tapant des commandes dans le
  **terminal navigateur**, soit en complétant un petit **script fourni**.
  Exemples : créer un bucket S3 et activer son chiffrement + le versioning, créer un
  utilisateur/une policy IAM au moindre privilège, déployer une fonction Lambda qui répond,
  créer une table DynamoDB et y insérer un item, créer une file SQS.
- **Mode de validation** : le **script de validation** rejoue des commandes
  `awslocal ... describe/list/get` **en lecture seule** contre LocalStack et compare l'état
  obtenu à l'attendu (ressource présente, option activée, invocation renvoyant la bonne
  valeur). `exit 0` ⟺ l'état cible est atteint.
- **Repli conceptuel / réponse vérifiée** : certains nœuds ne sont **pas émulables** par
  LocalStack communautaire (facturation, Well-Architected, CloudFront/CDN selon support,
  autoscaling réel). Pour ceux-là, on **ne lance pas de conteneur** : l'épreuve est une
  **réponse vérifiée** (valeur/texte normalisé comparé, ou artefact à produire et à vérifier).
  Cf. ADR-0008 : « Exercices à réponse vérifiée… gardé comme repli pour nœuds non exécutables ».
- **Principe anti-triche** : le script attendu (l'oracle) est **hors du workspace éditable**,
  copié au moment de la validation ; l'apprenant ne voit que l'énoncé et un squelette.

## Épreuves pratiques par nœud (pistes)

Le parcours a 18 nœuds. Exemples de tâches (nœud → épreuve → ce que vérifie le script).
À affiner nœud par nœud lors de SF-91 (Phase 3).

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| `cloud-basics` (modèle cloud) | **Repli conceptuel** : classer 4 actions côté client vs côté AWS (responsabilité partagée) | réponse vérifiée (appariement normalisé), pas de conteneur |
| `iam` (identités) | Créer un utilisateur + attacher une policy **moindre privilège** (ex. lecture S3 d'un seul bucket) | `awslocal iam get-user` / `list-attached-user-policies` ; document de policy conforme à l'attendu |
| `cli-sdk` (CLI & SDK) | Configurer le profil et lister les ressources émulées via `awslocal` | `awslocal sts get-caller-identity` répond + sortie `list` non vide |
| `serverless-lambda` (Lambda) | Déployer une fonction (zip fourni) et l'exposer | `awslocal lambda get-function` existe + `invoke` renvoie le payload attendu |
| `storage` / `s3-deep` (S3) | Créer un bucket, **activer chiffrement + versioning**, y déposer un objet | `get-bucket-encryption` activé, `get-bucket-versioning=Enabled`, objet présent |
| `databases` (DynamoDB) | Créer une table (clé de partition) et insérer un item | `describe-table=ACTIVE` + `get-item` renvoie l'item attendu |
| `compute` (messagerie/SQS) | Créer une file SQS et y publier un message | `get-queue-attributes` ok + `receive-message` renvoie le corps attendu |
| `billing-free-tier` / `well-architected` | **Repli conceptuel** : poser un budget / associer un pilier à une décision | réponse vérifiée (valeur/texte normalisé), pas de conteneur |

> Remarque : rester dans le **périmètre LocalStack communautaire** (S3, IAM, Lambda,
> DynamoDB, SQS/SNS, STS…). Les nœuds purement conceptuels ou non émulés
> (`cloud-basics`, `billing-free-tier`, `well-architected`, `caching-cdn`,
> `load-balancing-scaling`, `networking`/VPC avancé) basculent en **réponse vérifiée**.

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur de lab. exit 0 = reussi, != 0 = echec.
# awslocal pointe deja sur http://localstack:4566 (reseau interne, pas de vrai AWS).
set -euo pipefail

BUCKET="demo-skillforge"

# 1) Attendre que LocalStack soit pret (readiness interne, pas d'Internet)
until awslocal s3 ls >/dev/null 2>&1; do sleep 1; done

# 2) Verifications en LECTURE SEULE de l'etat attendu
# a) le bucket existe
awslocal s3api head-bucket --bucket "$BUCKET"

# b) le chiffrement par defaut est active
awslocal s3api get-bucket-encryption --bucket "$BUCKET" \
  | jq -e '.ServerSideEncryptionConfiguration.Rules | length > 0' >/dev/null

# c) le versioning est active
test "$(awslocal s3api get-bucket-versioning --bucket "$BUCKET" \
  | jq -r '.Status // "Off"')" = "Enabled"

echo "PRATIQUE_OK"
exit 0
```

En cas d'écart (ressource absente, option non activée), `set -e` fait sortir en code ≠ 0 et
le script imprime un message d'échec filtré. Pour un nœud en **repli**, le « script » compare
simplement une réponse normalisée à l'attendu, sans LocalStack.

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie à l'UI le **verdict**
et d'éventuels messages d'échec **filtrés** (jamais le contenu de l'oracle).

## Spécificités / pièges

- **Périmètre LocalStack** : seule une partie d'AWS est émulée, et l'édition communautaire
  couvre moins que la Pro. Des fonctionnalités **payantes/absentes** (certains services, du
  comportement avancé réseau, facturation réelle, CloudFront complet) ne doivent pas être
  exigées par une épreuve exécutable → **repli conceptuel / réponse vérifiée**.
- **Pas de vrai cloud ni de vraies clés** : `awslocal` utilise des identifiants factices
  (ex. `test`/`test`). Ne **jamais** injecter de vraies clés AWS ni tolérer un appel vers
  `*.amazonaws.com` ; le seul réseau autorisé est le segment interne lab ↔ LocalStack.
- **Déterminisme** : figer **région** (`AWS_DEFAULT_REGION`), **noms de ressources** et
  **IDs** ; normaliser les sorties JSON via `jq -e` plutôt que du grep fragile ; ne pas
  dépendre d'ARN/horodatages générés aléatoirement.
- **Readiness** : LocalStack met quelques secondes à démarrer ses services. Le script doit
  **attendre la disponibilité** (boucle `until` ci-dessus ou endpoint de santé interne)
  avant de valider, sinon faux négatifs.
- **Coût ressources** : LocalStack pèse plus qu'un conteneur de code → quotas stricts, TTL
  court, **faible concurrence** de sessions (cf. ADR-0008, risques Phase 3).
- **Nettoyage** : conteneurs **lab + LocalStack détruits ensemble** en fin de session ou par
  le **reaper** (TTL) ; état éphémère, aucune ressource persistée entre deux tentatives.
- **Isolation** : conteneur privilégié/lourd → durcissement propre à la Phase 3
  (réseau dédié, `--no-new-privileges`, non root, racine en lecture seule, pas de socket
  Docker exposé à l'apprenant).

---

Fichier écrit : `C:\Users\SOH\Desktop\Projets\DevRoad\docs\lab\aws.md`
