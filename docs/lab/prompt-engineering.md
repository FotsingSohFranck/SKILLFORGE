# Lab — Prompt Engineering (`prompt-engineering`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours. **Parcours particulier :
> c'est le plus difficile à auto-corriger** (voir § Spécificités).

## Famille & image

- **Famille** : **particulier**. Ce parcours n'enseigne pas un langage ni un outil CLI :
  il enseigne à **écrire de bons prompts**, ce qui n'est pas directement exécutable ni
  déterministe. On documente donc **deux options** (voir § Type d'épreuve), avec une
  image par option.
- **Point dur, assumé** : **aucun LLM n'est garanti en local** (pas de modèle offline
  dans le `docker compose` de SkillForge). On **ne valide donc jamais** une épreuve en
  appelant un vrai modèle. Toute la correction repose soit sur du **code autour d'un
  faux LLM (mock) déterministe**, soit sur une **grille de critères** (regex / présence).
- **Image de base** :
  - Option (a) — **CODE autour du LLM** : `node:22-alpine` **ou** `python:3.12-slim`
    (au choix de la tâche), avec le mock LLM et les tests **préchargés dans l'image**.
  - Option (b) — **réponse vérifiée par critères** : même image légère (node ou python),
    uniquement pour faire tourner le script de grille ; aucun test de code.
- **Phase SF-91** : rattaché à la **Phase 2** (autres parcours de code) pour l'option (a) ;
  l'option (b) est un **repli léger** utilisable dès que l'option (a) n'a pas de sens.
- **Réseau** : `none` (strict). Aucune exception — justement **parce qu'il n'y a pas**
  d'appel modèle à faire. Le mock est 100 % local.
- **Ressources conseillées** : 1 vCPU, 256–512 Mo, pids ≤ 256, TTL 10 min, FS de travail
  dédié en écriture, racine en lecture seule.

## Type d'épreuve

Deux natures d'épreuve, choisies **par nœud** selon qu'il est « technique » ou
« rédactionnel » :

- **(a) CODE autour du LLM — exécutable, recommandé** : l'apprenant écrit le **code** qui
  entoure le modèle (construction de prompt, parsing de sortie, garde-fous, boucle
  d'agent, moteur de templates). Un **faux LLM (mock) déterministe** est fourni : il
  renvoie des réponses figées / scriptées selon l'entrée. Le script valide par **tests
  cachés** : le code produit-il le bon prompt ? gère-t-il un refus ? respecte-t-il le
  format JSON ? résiste-t-il à une injection simulée ? C'est **exécutable et
  déterministe**, donc fiable. **Privilégier (a)** pour les nœuds techniques :
  `structured-output`, `tool-use-agents`, `guardrails-injection`, `prompt-templates`,
  `context-management`.
- **(b) Qualité du prompt lui-même — non déterministe, repli** : l'apprenant **rédige
  un prompt** (texte). On ne peut pas le juger par un vrai modèle (pas d'LLM offline).
  Repli : **réponse vérifiée par critères** via une **grille / regex** — présence des
  éléments attendus (rôle, contexte, format de sortie attendu, exemples few-shot,
  consigne de raisonnement, etc.). On documente **honnêtement** que cette évaluation est
  **partielle** (elle vérifie la *structure*, pas la *finesse*) et que **juger la
  qualité réelle par un modèle local est hors périmètre**. Utiliser (b) pour les nœuds
  de rédaction : `anatomy`, `few-shot`, `chain-of-thought`.

- **Principe anti-triche (option a)** : mock LLM + tests cachés **hors du workspace
  éditable**, copiés au moment de la validation ; l'apprenant ne voit que l'énoncé et le
  squelette. `exit 0` ⟺ tous les tests verts.
- **Principe de transparence (option b)** : la grille est volontairement **non secrète**
  dans son intention pédagogique (on attend un rôle, un format…), mais les regex exactes
  restent côté grader. Le verdict reste binaire (seuil de critères atteint ou non).

## Épreuves pratiques par nœud

Le parcours a 18 nœuds. Exemples (nœud → épreuve → ce que vérifie le script → option).
À affiner nœud par nœud lors de SF-91g.

| Nœud (slug) | Épreuve pratique | Vérification (script) | Option |
|---|---|---|---|
| `anatomy` | Rédiger un prompt complet pour une tâche donnée | grille : présence instruction + contexte + exemple + format attendu | (b) |
| `few-shot` | Rédiger un prompt few-shot (2–3 exemples) pour une classification | grille : ≥ 2 paires exemple→réponse + consigne de format | (b) |
| `chain-of-thought` | Rédiger un prompt qui force le raisonnement étape par étape | grille : marqueur « étape par étape » / « raisonne avant » + ordre réponse en dernier | (b) |
| `structured-output` | Écrire le code qui **parse** le JSON renvoyé par le mock et gère un JSON invalide | tests cachés : objet correct + repli propre sur sortie malformée | (a) |
| `prompt-templates` | Écrire un moteur de template paramétré (substitution de variables, échappement) | tests cachés : rendu attendu + variable manquante → erreur explicite | (a) |
| `context-management` | Écrire la fonction qui tronque / résume le contexte sous un budget de tokens | tests cachés : respect du budget + conservation des messages prioritaires | (a) |
| `tool-use-agents` | Écrire la **boucle d'agent** (appel outil → observation → relance) autour du mock | tests cachés : outil appelé avec les bons arguments + arrêt à la bonne étape | (a) |
| `guardrails-injection` | Écrire le garde-fou qui détecte/neutralise une **injection de prompt** simulée | tests cachés : l'instruction injectée est ignorée + réponse cadrée | (a) |

> Les autres nœuds (ex. `how-llms-work`, `tokens-context`, `roles-system`,
> `role-persona`, `decomposition`, `self-consistency`, `rag`, `evaluation`, `safety`,
> `cost-latency`) relèvent surtout de (b) ou restent en `LESSONS_ONLY` quand aucune
> épreuve exécutable honnête n'est possible (ex. `rag` complet nécessiterait un vrai
> pipeline/modèle — à reporter en Phase 3).

## Script de validation : modèle

Deux modèles selon l'option. Le backend (`lab`) lit le **code de sortie** (0 = `passed`).

**Option (a) — CODE + mock LLM déterministe + tests (exemple node) :**

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur. exit 0 = reussi, != 0 = echec.
set -euo pipefail
cd /workspace

# 1) Copier le mock LLM deterministe + les tests caches (montes hors du workspace editable)
cp -r /grader/mock-llm/*   ./            2>/dev/null || true
cp -r /grader/tests/*      ./tests/      2>/dev/null || true

# 2) Lancer les tests hors-ligne (dependances prechargees dans l'image, reseau none)
if node --test ./tests/ ; then   # ou: npm test --offline / pytest -q selon l'image
  echo "PRATIQUE_OK"
  exit 0
else
  echo "PRATIQUE_ECHEC : des tests ne passent pas"
  exit 1
fi
```

Le **mock LLM** (`/grader/mock-llm/`) est une fonction/objet qui, pour une entrée donnée,
renvoie une sortie **figée** (table de correspondance ou règles simples). Il simule aussi
les cas à tester : refus, sortie malformée, tentative d'injection. **Aucun appel réseau.**

**Option (b) — réponse vérifiée par critères (grille / regex) :**

```bash
#!/usr/bin/env bash
set -euo pipefail
cd /workspace

# Le prompt redige par l'apprenant est dans prompt.txt
# La grille (regex + seuil) est cote grader, hors du workspace editable.
if node /grader/check-rubric.js prompt.txt /grader/rubric.json ; then
  echo "PRATIQUE_OK"      # seuil de criteres atteint
  exit 0
else
  echo "PRATIQUE_ECHEC : criteres manquants (voir feedback)"
  exit 1
fi
```

`check-rubric.js` applique une liste de critères (chacun = regex + poids), calcule un
score et compare à un **seuil**. Il renvoie un feedback **filtré** (quels éléments
manquent : « pas de format de sortie explicite », « aucun exemple ») sans divulguer les
regex exactes.

Dans les deux cas, la sortie renvoyée à l'UI est **le verdict + un feedback filtré**,
jamais le contenu des tests cachés ni les regex.

## Spécificités / pièges

- **Pas d'LLM réel offline — c'est la contrainte dure.** On ne juge **jamais** une
  épreuve en appelant un modèle : d'où le **mock déterministe** (option a) ou la **grille
  de critères** (option b). Ne pas introduire d'appel réseau vers une API modèle (le
  réseau est `none`, et ce serait de toute façon non reproductible et payant).
- **Déterminisme** : le mock doit être **totalement déterministe** (mêmes entrées → mêmes
  sorties) ; interdire tout aléa (température, `random`, horloge). Figer locale et seed si
  le code manipule des listes/échantillons.
- **L'évaluation de style reste partielle** : l'option (b) vérifie la **présence et la
  structure** d'éléments (rôle, contexte, format, exemples), **pas la qualité réelle** du
  prompt. Le dire clairement dans l'énoncé pour ne pas survendre la correction. Un prompt
  « structurellement complet mais médiocre » peut passer : c'est un compromis assumé en
  l'absence de modèle local.
- **Risque de triche sur (b)** : un apprenant peut « cocher les cases » de la grille sans
  écrire un bon prompt. Mitigations : varier les énoncés, exiger des éléments **liés au
  cas** (pas juste des mots-clés), garder (b) pour les nœuds où (a) n'est pas possible.
- **Préférer (a) dès que possible** : un nœud rédactionnel peut souvent être reformulé en
  tâche de **code** (ex. « écris la fonction qui construit le prompt à partir de ces
  variables ») — on gagne un verdict déterministe et honnête.
- **Isolation** : workspace jouet dédié par tâche ; le mock et les tests/grille vivent
  dans `/grader` (lecture seule, hors workspace éditable), copiés seulement à la
  validation.
- **Démarrage à froid** : précharger les dépendances (node_modules / wheels) dans l'image
  pour tenir le TTL sans réseau.
