# Lab — Terraform (`terraform`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Famille infra, mais variante
> légère et hors-ligne** (ni cloud réel, ni cluster).

## Famille & image

- **Famille** : infra (IaC), mais **allégée** : aucune ressource cloud, aucun réseau
  sortant. On reste sur les providers **local**, **null**, **random** (et éventuellement
  **docker** pour les nœuds qui s'y prêtent) de façon à ce que `terraform init/plan/apply`
  tourne **entièrement en local, sans réseau**.
- **Image de base** : image avec le binaire `terraform` + un **mirror de providers
  préchargé** (local, null, random, et docker si besoin), monté/configuré pour un
  `init` hors-ligne (`-plugin-dir` ou `provider_installation { filesystem_mirror }`).
  Pas de `registry.terraform.io` au runtime.
- **Phase SF-91** : **2** pour les épreuves local/null/random (pas de démon Docker) ;
  **3** uniquement pour les rares nœuds qui exploitent le provider **docker**
  (docker-in-docker, durcissement à part).
- **Réseau** : `none` par défaut. Le mirror de providers rend tout accès registre inutile.
- **Ressources conseillées** : 1 vCPU, 256–512 Mo, pids ≤ 128, TTL 15 min, FS de travail
  dédié en écriture, racine en lecture seule. (Nettement plus léger qu'un cluster k3d.)

## Type d'épreuve

- **Nature** : l'apprenant **écrit / complète du HCL** (ressources `local_file`,
  `null_resource`, `random_*`, variables, locals, outputs, modules, meta-arguments,
  state) dans un workspace de départ fourni.
- **Boucle d'exécution** : `terraform init -backend=false` (providers depuis le mirror
  local) → `terraform validate` → `terraform plan` → `terraform apply -auto-approve`.
- **Mode de validation** : le **script de validation** enchaîne ces commandes puis vérifie
  le résultat selon la tâche :
  1. **Outputs attendus** (préféré) : `terraform output -json` comparé à un attendu
     normalisé (clé par clé).
  2. **Artefacts produits** : vérifier les fichiers créés par le provider `local`
     (présence, nom, contenu) après `apply`.
  3. **Conformité structurelle** : `terraform validate` et/ou `terraform fmt -check`
     verts, et lecture du state JSON pour compter/typer les ressources attendues.
- **Principe anti-triche** : l'oracle (outputs attendus, fichiers de référence, assertions)
  est **hors du workspace éditable** et appliqué au moment de la validation ; l'apprenant
  ne voit que l'énoncé et le squelette HCL.

## Épreuves pratiques par nœud (pistes)

Le parcours a 20 nœuds. Exemples de tâches pratiques (nœud → épreuve → ce que vérifie le
script). À affiner nœud par nœud lors de SF-91.

| Nœud (thème) | Épreuve pratique | Vérification (script) |
|---|---|---|
| `hcl-basics` — Le langage HCL | Écrire un `local_file` qui écrit un contenu donné dans `out.txt` | `apply` OK + fichier `out.txt` présent avec le contenu attendu |
| `providers` — Providers | Déclarer le provider `random` avec version épinglée et générer un `random_pet` | `init -backend=false` OK (mirror) + `output -json` du nom non vide |
| `resources` — Ressources | Créer 2 `local_file` dépendants (contenu de l'un dérivé de l'autre) | `output -json` des chemins/contenus = attendu |
| `meta-arguments` — count / for_each | Générer N fichiers via `count` ou une map via `for_each` | état contient N ressources, noms = attendu |
| `variables` — Variables d'entrée | Ajouter une variable typée + validation, consommée par une ressource | `plan` échoue sur valeur invalide, `apply` OK sur valeur valide |
| `locals-outputs` — Locals & outputs | Calculer un `local`, l'exposer via un `output` (dont un `sensitive`) | `output -json` = valeur attendue, champ sensible masqué |
| `core-workflow` — Workflow | `fmt` + `validate` + `plan` + `apply` d'une config `null_resource`/`random` | `fmt -check` et `validate` verts, `apply` OK |
| `modules` — Modules | Extraire une ressource dans un module local et l'appeler avec des inputs | `output -json` du module racine = attendu |
| `state-operations` — Opérations sur l'état | Renommer une ressource et utiliser `state mv` sans recréation | `plan` post-`state mv` = « No changes » |

> Remarque : toutes les épreuves ci-dessus sont **100% locales** (local/null/random),
> sans réseau ni cloud. Les nœuds purement conceptuels (`state-management` remote backend,
> `cicd`, `scaling`, `security` policy-as-code, `hcp`, `provisioners` remote) restent en
> **repli QCM / exercice vérifié** : pas de pratique exécutable, ou alors une variante
> simulée (backend `local`, `local-exec` écrivant un marqueur) clairement documentée.

## Script de validation : modèle

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur. exit 0 = reussi, != 0 = echec.
set -euo pipefail
cd /workspace

# 0) Providers depuis le mirror local (aucun acces reseau / registre)
export TF_CLI_CONFIG_FILE=/grader/terraformrc   # filesystem_mirror -> /providers
export TF_IN_AUTOMATION=1

# 1) Init hors-ligne + validation statique
terraform init -backend=false -input=false -no-color >/dev/null
terraform validate -no-color

# 2) Plan + apply deterministes (providers local/null/random, pas de cloud)
terraform apply -auto-approve -input=false -no-color >/dev/null

# 3) Comparer les outputs a l'attendu (oracle hors workspace editable)
terraform output -json > /tmp/out.json
if diff -q <(jq -S . /tmp/out.json) <(jq -S . /grader/expected_output.json) >/dev/null; then
  echo "PRATIQUE_OK"
  # Nettoyage du state pour ne rien laisser trainer
  terraform destroy -auto-approve -input=false -no-color >/dev/null 2>&1 || true
  exit 0
else
  echo "PRATIQUE_ECHEC : les outputs ne correspondent pas a l'attendu"
  exit 1
fi
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI
(sans divulguer l'oracle : seulement le verdict et d'éventuels messages d'échec filtrés —
ne pas renvoyer le `diff` brut qui révélerait les valeurs attendues).

## Spécificités / pièges

- **Mirror de providers (offline)** : le point critique. Pré-remplir un
  `filesystem_mirror` dans l'image (local, null, random, docker) et le pointer via
  `TF_CLI_CONFIG_FILE` / `provider_installation`. Toujours `terraform init -backend=false`
  pour ne pas tenter un backend distant. Vérifier que les **versions épinglées** des
  énoncés existent dans le mirror.
- **Pas de vrai cloud** : les épreuves sont centrées **local / null / random** (+ docker en
  Phase 3). Ne jamais introduire un provider aws/azurerm/google — il exigerait réseau,
  credentials et state réel, hors contrat de ce lab.
- **Déterminisme du plan** : `random_*` est stable une fois en state, mais un `apply`
  propre part d'un state vide → figer les graines (`keepers`, valeurs d'entrée fixes) quand
  l'attendu dépend d'une valeur « aléatoire », sinon comparer la **forme** (type, longueur,
  présence) plutôt que la valeur exacte. Attention aussi aux timestamps (`timestamp()`,
  `plantimestamp()`) : à bannir des outils comparés.
- **Nettoyage du state** : conteneur éphémère, mais faire un `destroy -auto-approve` en fin
  de validation évite des fichiers résiduels (provider `local`) et garantit un re-run propre
  si le workspace est réutilisé. Ne jamais persister `terraform.tfstate` hors du conteneur.
- **Isolation** : workspace jouet dédié par tâche, jamais de config infra réelle du dépôt.
  Racine en lecture seule, seul `/workspace` est inscriptible ; `/grader` (oracle + mirror)
  monté en lecture seule et hors du workspace éditable.
- **Nœud provider `docker`** : seul cas nécessitant la Phase 3 (démon Docker, dind
  privilégié). Le traiter à part avec son durcissement ; par défaut le garder optionnel et
  préférer null/local pour ne pas alourdir le lab.
