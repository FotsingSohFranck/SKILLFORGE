# Lab — Software Design (`software-design`)

> Fiche d'implémentation de l'**évaluation pratique par conteneur** pour ce parcours.
> Cadre global : `docs/adr/0008-lab-executable-evaluation-pratique.md` + épic **SF-91**.
> À lire au moment d'implémenter le lab de ce parcours.
> **Attention : ce parcours est majoritairement conceptuel.** Seule une partie des nœuds
> est réellement *exécutable* (via refactoring). Les nœuds de conception pure relèvent
> d'un **repli non exécutable** (réponse vérifiée par critères / artefact à produire) —
> voir §Type d'épreuve et §Spécificités.

## Famille & image

- **Famille** : **mixte conception**. Deux sous-familles coexistent dans le même parcours :
  - **(a) Refactoring exécutable** (JVM) : code qui « sent mauvais » + suite de tests
    verte à préserver → l'apprenant refactorise.
  - **(b) Conception non exécutable** : diagrammes, ADR, choix d'architecture, trade-offs
    → aucun conteneur, validation par points-clés (voir plus bas).
- **Image de base (sous-famille a)** : `eclipse-temurin:21-jdk` + Maven (cache `.m2`
  préchauffé, hors réseau), comme le pilote `backend`. Variante **node** possible si
  l'exemple de code est en TypeScript/JavaScript.
- **Phase SF-91** : **2** (autres parcours de code) pour la sous-famille (a) ; la
  sous-famille (b) peut arriver **sans orchestrateur** (repli texte/artefact, cf. ADR-0008
  « Exercices à réponse vérifiée » et « Correction par points-clés »).
- **Réseau** : `none`. Le dépôt Maven est préchargé dans l'image / cache monté en lecture
  seule.
- **Ressources conseillées** : 1 vCPU, 512–768 Mo, pids ≤ 256, TTL 15 min, FS de travail
  dédié en écriture, racine en lecture seule. Les nœuds (b) ne consomment **aucun**
  conteneur.

## Type d'épreuve

Le type dépend de la nature du nœud. On **privilégie l'exécutable (a)** partout où c'est
honnêtement possible (patterns, SOLID, refactoring), et on assume le **repli (b)** pour le
conceptuel pur (principes transverses, styles/patterns d'architecture, trade-offs, C4, ADR).

- **(a) Refactoring de code exécutable** *(recommandé quand applicable)* :
  - On fournit un **workspace de départ** : du code qui présente un défaut de conception
    (couplage fort, violation d'un principe SOLID, duplication, dépendance mal orientée)
    **plus une suite de tests JUnit qui est déjà verte**.
  - L'apprenant **refactorise** (extraire une interface, appliquer un patron GoF, inverser
    une dépendance, séparer une responsabilité) **sans changer le comportement**.
  - **Validation = double contrôle** :
    1. les tests fournis **restent verts** (`mvn -o test`) — le comportement est préservé ;
    2. une **assertion structurelle simple** confirme le refactoring attendu : une
       classe/interface attendue existe, une dépendance interdite est absente, un type
       n'est plus référencé, etc. (grep/outil statique léger sur les sources).
- **(b) Conception non exécutable** *(repli explicite — ces nœuds ne sont PAS « exécutables »)* :
  - **Réponse vérifiée par critères** : l'apprenant rédige une réponse ; le script vérifie
    la **présence des éléments-clés attendus** (p. ex. acteurs, frontières, décision +
    conséquences, trade-off nommé). Correction **partielle** par points-clés, pas une
    preuve d'exécution.
  - **Artefact à produire** : un **ADR** (format Statut/Contexte/Décision/Conséquences) ou
    un **diagramme Mermaid** (C4, séquence) validé par points-clés (nœuds présents, relation
    attendue, mots-clés structurants).
  - **À signaler dans l'UI** : ces nœuds affichent clairement « évaluation de conception
    (points-clés) », pas « pratique exécutée ».

> Politique de nœud (ADR-0008) : réserver `LESSONS_AND_PRACTICAL` aux nœuds (a) dont la
> validation est fiable (tests + assertion structurelle). Pour les nœuds (b), rester en
> `LESSONS_ONLY` ou n'utiliser les points-clés qu'à titre **informatif** tant que la
> correction n'est pas jugée assez robuste — on ne verrouille pas un nœud sur une
> correction partielle.

## Épreuves pratiques par nœud

Exemples tirés des vrais nœuds de la roadmap (nœud → épreuve → ce que vérifie le script).
La colonne **Type** indique (a) exécutable ou (b) conception non exécutable.

| Nœud (thème) | Type | Épreuve | Vérification (script) |
|---|---|---|---|
| `solid` — Principes SOLID | (a) | Une classe viole SRP/DIP ; extraire une interface et inverser la dépendance sans casser les tests | `mvn -o test` vert **et** l'interface attendue existe + la classe ne dépend plus du type concret |
| `gof-patterns` — Design patterns (GoF) | (a) | Remplacer un `switch`/cascade de `if` par un patron (Strategy/Factory) ; comportement identique | tests verts **et** présence des types du patron (interface + implémentations), `switch` disparu |
| `clean-code-practices` — Code propre : pratiques | (a) | Décomposer une méthode trop longue, supprimer la duplication, rendre une fonction pure | tests verts **et** assertion structurelle (méthode extraite présente / longueur réduite) |
| `coupling-cohesion` — Couplage & cohésion | (a) | Casser un couplage (injection par constructeur à la place d'un `new` interne) | tests verts **et** dépendance injectée (constructeur) + `new` interdit absent des sources |
| `layered-hexagonal` — Couches & hexagonal | (b) | Produire un diagramme Mermaid domaine/ports/adapters pour un cas donné | points-clés : domaine sans dépendance sortante, port défini, adapter branché |
| `ddd` — Domain-Driven Design | (b) | Décrire un découpage en contextes bornés + langage ubiquitaire pour un énoncé | points-clés : contextes nommés, frontières, termes du domaine présents |
| `architecture-decisions` — Décider en architecte | (b) | Rédiger un **ADR** sur un trade-off (p. ex. monolithe vs microservices) | points-clés : décision explicite, au moins un attribut de qualité, conséquences positives **et** négatives |

> Les nœuds purement conceptuels (`programming-paradigms`, `oop-principles`,
> `dry-yagni-kiss`, `other-design-principles`, `structural-styles`, `messaging-styles`,
> `distributed-styles`, `mvc`, `monolith-vs-microservices`, `event-sourcing-cqrs`,
> `enterprise-patterns`, `posa-component-principles`) relèvent du repli (b) ou restent en
> évaluation informative. Ne pas forcer un conteneur là où il n'apporte rien.

## Script de validation : modèle

Modèle pour un nœud **(a) refactoring** : les tests fournis doivent rester verts **et** une
**assertion structurelle** confirme le refactoring attendu.

```bash
#!/usr/bin/env bash
# S'execute DANS le conteneur. exit 0 = reussi, != 0 = echec.
set -euo pipefail
cd /workspace

# 1) Copier les tests fournis (montes en lecture seule hors du workspace editable).
#    Ces tests DOIVENT rester verts : ils figent le comportement a preserver.
cp -r /grader/tests/* src/test/java/ 2>/dev/null || true

# 2) Comportement preserve : compiler + tester hors-ligne (cache .m2 prechauffe).
if ! mvn -q -o -Dstyle.color=never test; then
  echo "PRATIQUE_ECHEC : le refactoring a casse le comportement (tests rouges)"
  exit 1
fi

# 3) Assertion structurelle : le refactoring attendu a bien ete realise.
#    Exemple : une interface attendue existe ET une dependance interdite a disparu.
if ! grep -rq "interface PaymentGateway" src/main/java/; then
  echo "PRATIQUE_ECHEC : interface PaymentGateway attendue absente"
  exit 1
fi
if grep -rq "new StripeClient(" src/main/java/; then
  echo "PRATIQUE_ECHEC : dependance concrete 'new StripeClient(...)' encore presente"
  exit 1
fi

echo "PRATIQUE_OK"
exit 0
```

Pour un nœud **(b) conception**, pas de conteneur : la réponse (ou l'artefact ADR / le bloc
Mermaid) est vérifiée par **présence de points-clés** (liste d'éléments attendus). On ne
renvoie qu'un **score partiel** et un feedback, jamais un verdict « exécuté ».

Le backend (`lab`) lit le **code de sortie** (0 = `passed`) pour les nœuds (a) et renvoie la
sortie filtrée à l'UI (verdict + messages d'échec, sans divulguer le contenu des tests).

## Spécificités / pièges

- **Parcours majoritairement conceptuel** : l'essentiel des 22 nœuds n'est **pas
  exécutable**. L'exécutable se limite aux nœuds de **refactoring** (SOLID, patterns,
  code propre, couplage). Ne pas chercher à « forcer » un conteneur sur un nœud de
  principes ou de styles d'architecture : le repli (b) y est la bonne réponse.
- **Évaluation de conception partielle** : la correction par points-clés reste
  **imparfaite** (une bonne réponse peut manquer un mot-clé, une mauvaise peut cocher les
  cases). La traiter comme un **signal**, afficher « points-clés » dans l'UI, et éviter de
  verrouiller un nœud dessus tant que la robustesse n'est pas validée.
- **Préserver le comportement, pas le réécrire** : dans (a), les tests fournis sont la
  garde-fou ; l'apprenant ne doit **pas** les modifier (copiés hors du workspace éditable
  au moment de la validation).
- **Assertion structurelle fragile** : un `grep` sur les sources est simple mais sensible
  au nommage/formatage. Préférer des assertions **tolérantes** (motif de signature plutôt
  que ligne exacte) et documenter clairement l'attendu dans l'énoncé pour éviter les
  faux négatifs. Un vrai contrôle statique (ArchUnit) est plus robuste mais plus lourd —
  à n'introduire que si nécessaire.
- **Démarrage à froid Maven** : préchauffer le cache `.m2` dans l'image (build d'un projet
  jouet) pour que `mvn -o test` tourne sans réseau et vite.
- **Squelettes petits** : une ou deux classes + leurs tests, pas un module entier, pour
  tenir le TTL et la mémoire.
- **Isolation** : jamais le vrai module du dépôt dans le conteneur ; un workspace jouet
  dédié par tâche.
- **ADR / Mermaid comme artefact** : si on demande un ADR ou un diagramme, fournir un
  gabarit minimal dans l'énoncé pour que la vérification par points-clés ait des ancres
  stables (sections attendues, nommage des nœuds du diagramme).
