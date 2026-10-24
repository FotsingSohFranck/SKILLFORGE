# Lab — Software Architect (`software-architect`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours.
>
> **Avertissement de réalité** : ce parcours est **très majoritairement conceptuel**.
> L'essentiel n'est **pas exécutable** au sens « on compile et on lance du code ». On
> documente donc deux modes complémentaires : **(a) artefact vérifiable** (le cas
> général, recommandé) et **(b) exécutable ponctuel** (rare, là où une règle
> d'architecture est automatisable). L'auto-correction d'une conception reste
> **limitée et partielle** (voir « Spécificités / pièges »).

## Famille & image

- **Famille** : conception / architecture (pas une famille « code » classique).
- **Mode (a) — Artefact vérifiable (par défaut)** : pas besoin d'exécution. L'apprenant
  produit un **texte / diagramme** (ADR, diagramme C4 en Mermaid, tableau de trade-offs,
  choix justifié). La validation se fait par un **vérificateur de points-clés** et/ou un
  **contrôle de validité syntaxique Mermaid**. Image minimale suffisante :
  `node:22-alpine` + `@mermaid-js/mermaid-cli` (rendu Mermaid hors-ligne) ; ou une simple
  image outillée d'un linter de texte. Pas de JVM nécessaire.
- **Mode (b) — Exécutable ponctuel (rare)** : uniquement pour les nœuds où une règle
  d'architecture est **automatisable** (ex. fitness functions via **ArchUnit**, ou
  vérification d'une **structure de dossiers hexagonale**). Image JVM :
  `eclipse-temurin:21-jdk` + Maven, avec cache `.m2` préchauffé (offline).
- **Phase SF-91** : rattaché à la **Phase « artefacts / repli non exécutable »** (voir
  ADR, alternative « exercices à réponse vérifiée » promue ici en mode principal) ; le
  volet ArchUnit relève de la **Phase 1/2 (exécution de code JVM)**.
- **Réseau** : `none`. Le rendu Mermaid et le build Maven sont hors-ligne (CLI et cache
  embarqués dans l'image). Aucune exception par défaut.
- **Ressources conseillées** : 1 vCPU, 512 Mo (mode a) à 768 Mo (mode b, JVM),
  pids ≤ 256, TTL 10–15 min, FS de travail dédié en écriture, racine en lecture seule.

## Type d'épreuve

- **Majoritairement (a) — artefact vérifiable** : l'apprenant *conçoit* et *justifie*.
  Il rend un artefact structuré :
  - un **ADR** (contexte, décision, conséquences, alternatives) ;
  - un **diagramme C4 / Mermaid** (contexte, conteneurs, composants) ;
  - un **tableau de trade-offs** (option, avantages, coûts, attribut de qualité visé) ;
  - un **choix justifié** (style d'architecture, protocole d'API, modèle de données).
  - **Mode de validation** : un script cherche la présence des **éléments attendus**
    (points-clés : contexte, décision, conséquences, attributs de qualité, frontières,
    au moins N alternatives…) et, pour un diagramme, valide qu'il **rend sans erreur**
    via `mmdc` (syntaxe Mermaid correcte).
- **Ponctuellement (b) — exécutable** là où c'est automatisable : un **test ArchUnit**
  vérifie une règle (dépendances pointant vers l'intérieur, pas de fuite d'entités de
  persistance dans le domaine, nommage des use cases), ou un contrôle vérifie une
  **arborescence hexagonale** (`domain/ application/ adapter/in/web adapter/out/persistence`).
- **Principe anti-triche** : les points-clés attendus, la solution de référence et les
  tests ArchUnit sont **hors du workspace éditable**, copiés au moment de la validation.
  L'apprenant ne voit que l'énoncé et un squelette (gabarit d'ADR vide, projet jouet).

## Épreuves pratiques par nœud (pistes)

Le parcours a 23 nœuds. Exemples de tâches (nœud → épreuve → ce que vérifie le script).
À affiner nœud par nœud lors de SF-91g. **(a)** = artefact vérifiable, **(b)** = exécutable.

| Nœud (thème) | Épreuve pratique | Vérification (script) | Mode |
|---|---|---|---|
| `tech-decisions` — Décisions & ADR | Rédiger un **ADR** complet sur un choix imposé (ex. REST vs gRPC) | Points-clés présents : contexte, décision, conséquences, ≥ 2 alternatives, attribut de qualité visé | (a) |
| `requirements-elicitation` — Exigences | Lister exigences fonctionnelles **et** attributs de qualité pour un cas | Points-clés : ≥ 3 attributs de qualité nommés (perf, dispo, sécurité…), distinction fonctionnel / non-fonctionnel | (a) |
| `documentation` — C4 / vues | Produire un **diagramme C4 niveau conteneurs** en Mermaid | Rendu Mermaid **sans erreur** (`mmdc`) + points-clés : ≥ N conteneurs, frontières, relations étiquetées | (a) |
| `patterns-principles` — SOLID / hexagonal | Organiser un **projet jouet** en arborescence hexagonale et respecter la direction des dépendances | **Test ArchUnit** vert : domaine sans dépendance Spring/web, dépendances vers l'intérieur | (b) |
| `enforcing-standards` — Fitness functions | Écrire une **fitness function** ArchUnit (ex. nommage des use cases `*UseCase`) | Le test ArchUnit rédigé détecte la violation injectée puis passe au vert sur le code conforme | (b) |
| `architectural-styles` — Styles | Choisir un style pour un cas et remplir un **tableau de trade-offs** | Points-clés : ≥ 2 options comparées, coûts **et** bénéfices, lien explicite à un attribut de qualité | (a) |
| `apis-integrations` — APIs | Justifier REST vs gRPC vs GraphQL pour un besoin donné (**choix justifié**) | Points-clés : contexte d'usage, critère décisif (latence, contrat, streaming…), conséquences du choix | (a) |
| `working-with-data` — Données | Décider SQL vs NoSQL et esquisser le modèle (texte + mini-diagramme) | Points-clés : propriétés visées (ACID/CAP), motif d'accès, justification ; diagramme rend sans erreur | (a) |

> Remarque : le mode **(a)** domine (conception). Le mode **(b)** est réservé aux nœuds
> où une **règle est mécaniquement vérifiable** (`patterns-principles`,
> `enforcing-standards`, et éventuellement `operations-devops` pour une structure IaC).
> Partout ailleurs, n'exécutez rien : la correction par points-clés est explicitement
> partielle (voir pièges).

## Script de validation : modèle

### Modèle (a) — vérification de points-clés d'un ADR produit

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur. exit 0 = reussi, != 0 = echec.
set -euo pipefail
cd /workspace

ADR="docs/adr.md"            # artefact rendu par l'apprenant
[ -f "$ADR" ] || { echo "PRATIQUE_ECHEC : artefact introuvable ($ADR)"; exit 1; }

# Points-cles attendus (montes en lecture seule hors du workspace editable).
# Chaque ligne = un motif (regex, insensible a la casse) a trouver dans l'artefact.
#   ex. /grader/keys.txt :
#     contexte|context
#     d(e|é)cision
#     cons(e|é)quence
#     (performance|disponibilit|s(e|é)curit|scalab)   # >= 1 attribut de qualite
#     alternative
manquants=0
while IFS= read -r motif; do
  [ -z "$motif" ] && continue
  if ! grep -Eiq "$motif" "$ADR"; then
    echo "MANQUE : $motif"
    manquants=$((manquants+1))
  fi
done < /grader/keys.txt

# Option diagramme : si un .mmd est rendu, il doit compiler (syntaxe Mermaid valide).
if [ -f diagram.mmd ]; then
  mmdc -i diagram.mmd -o /tmp/out.svg >/dev/null 2>&1 \
    || { echo "PRATIQUE_ECHEC : diagramme Mermaid invalide"; exit 1; }
fi

if [ "$manquants" -eq 0 ]; then
  echo "PRATIQUE_OK"
  exit 0
else
  echo "PRATIQUE_ECHEC : $manquants point(s)-cle(s) manquant(s)"
  exit 1
fi
```

### Option (b) — règle d'architecture automatisable (ArchUnit)

Pour les nœuds où une règle est vérifiable, on bascule sur le modèle JVM : copier un
**test ArchUnit caché** dans le projet jouet, puis lancer la vérification hors-ligne.

```bash
#!/usr/bin/env bash
set -euo pipefail
cd /workspace
cp -r /grader/tests/* src/test/java/ 2>/dev/null || true   # regles ArchUnit cachees
if mvn -q -o -Dstyle.color=never test; then
  echo "PRATIQUE_OK"; exit 0
else
  echo "PRATIQUE_ECHEC : regle(s) d'architecture non respectee(s)"; exit 1
fi
```

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) et renvoie la sortie à l'UI.
Pour le mode (a), on peut remonter la **liste des points-clés manquants** comme feedback
pédagogique (c'est un indice, pas une note), sans divulguer la solution de référence.
Pour le mode (b), on remonte le verdict et les violations ArchUnit filtrées.

## Spécificités / pièges

- **Auto-correction de conception = limitée et partielle.** Vérifier des **points-clés**
  prouve qu'un artefact *mentionne* les bons éléments (contexte, décision, conséquences,
  attributs de qualité, frontières), **pas** que le raisonnement est bon. Deux ADR
  opposés peuvent tous deux « passer ». Il faut l'assumer explicitement dans l'UI :
  l'épreuve valide la **forme et la complétude**, pas la pertinence du choix. La relecture
  humaine / l'auto-évaluation restent recommandées pour la qualité du fond.
- **L'exécutable se limite aux règles automatisables.** Seuls `patterns-principles`,
  `enforcing-standards` (et à la marge une structure IaC) justifient un conteneur JVM +
  ArchUnit. Ne pas forcer un volet exécutable sur un nœud purement conceptuel : ce serait
  artificiel.
- **Robustesse du matching de points-clés.** Utiliser des regex tolérantes (accents,
  synonymes français/anglais, casse), accepter plusieurs formulations, et éviter les
  motifs trop stricts qui recalent de bonnes réponses. Mieux vaut quelques points-clés
  solides que beaucoup de motifs fragiles (faux négatifs décourageants).
- **Validité Mermaid ≠ exactitude du diagramme.** `mmdc` garantit que le diagramme *rend*
  (syntaxe correcte), pas qu'il modélise le bon système. Compléter par des points-clés sur
  le contenu (nombre de conteneurs attendus, présence des frontières/relations).
- **Rendu Mermaid hors-ligne.** `@mermaid-js/mermaid-cli` embarque un navigateur headless
  (Puppeteer/Chromium) : préinstaller Chromium dans l'image et figer la config pour tourner
  en `--network none` et sans téléchargement. Sinon, se rabattre sur un parseur Mermaid
  léger qui valide la syntaxe sans rendu graphique.
- **Démarrage à froid Maven (mode b uniquement).** Préchauffer le cache `.m2` (ArchUnit +
  JUnit) dans l'image pour que `mvn -o test` tourne sans réseau et vite.
- **Isolation.** Jamais le vrai dépôt ni le module `app`/`backend` dans le conteneur : un
  workspace jouet dédié par tâche (gabarit d'ADR vide, mini-projet pour ArchUnit).
- **Déterminisme.** Normaliser l'artefact avant matching (fins de ligne, espaces, casse) ;
  pour ArchUnit, figer locale et fuseau si une règle les touche.
