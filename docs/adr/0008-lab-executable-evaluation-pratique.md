# ADR-0008 : Lab exécutable — évaluation pratique par conteneur pour valider un nœud

## Statut

Proposé — 2026-10-24

## Contexte

Aujourd'hui, un nœud est « validé » de façon **purement déclarative** : il passe
`COMPLETED` dès que toutes ses leçons sont marquées terminées par un simple clic
(`ProgressRecalculator` : nœud COMPLETED ⟺ toutes leçons COMPLETED). Aucune réussite
n'est requise :

- les **quiz** sont corrigés mais **informatifs** (pas de seuil, aucun déverrouillage, ils
  n'alimentent que la gamification) ;
- les sections **EXERCISE** sont **statiques** (énoncé + solution révélable), sans
  soumission ni correction ;
- un apprenant peut donc valider un nœud **sans rien réussir**.

Le Product Owner veut combler ce trou : à la fin d'un nœud, une **vraie épreuve pratique**
(pas un QCM) où l'apprenant *fait* (commandes, code, manipulation) dans un **environnement
exécutable**, et dont la **réussite conditionne la validation du nœud**.

Contrainte structurante : la plateforme est **100% locale (Docker Compose)**, self-hébergée.
Pas de dépendance à un service de lab externe (killercoda, Instruqt…).

## Décision

Créer un nouveau contexte borné **`lab`** (hexagonal, comme les autres) qui porte
l'**évaluation pratique d'un nœud**.

### Modèle

- Un nœud peut déclarer une **tâche pratique** (`practical_task`) : une **image de base**
  (par famille de parcours), un **espace de travail de départ** (fichiers), un **énoncé**,
  et un **script de validation** qui s'exécute dans le conteneur et dont le **code de sortie
  0 = réussi**. Le script est la source de vérité de la correction (pas de QCM).
- Une **politique de validation de nœud** devient **configurable** (champ sur le nœud) :
  `LESSONS_ONLY` (défaut, comportement actuel, **aucune régression**) ou
  `LESSONS_AND_PRACTICAL` (nœud validé ⟺ leçons terminées **et** pratique réussie).

### Flux

1. L'apprenant ouvre « Passer la pratique » du nœud.
2. Le backend (`lab`) demande à un **orchestrateur** de démarrer un **conteneur éphémère**
   dédié à la session (image de la tâche), avec un **espace de travail** initialisé.
3. L'apprenant travaille via un **terminal dans le navigateur** (xterm.js côté React ↔
   **websocket** ↔ shell du conteneur) et/ou un éditeur de fichiers.
4. Il clique « Valider » : le backend exécute le **script de validation** dans le conteneur,
   récupère le code de sortie et la sortie.
5. Réussi → émission d'un **`PracticalPassedEvent`** (patron calqué sur `ExamPassedEvent`),
   consommé par **`apprentissage`** qui enregistre la réussite et déclenche la
   recalculation ; le nœud passe `COMPLETED` si la politique l'exige. Échec → feedback.
6. Le conteneur est **détruit** à la fin de session ou par un **reaper** (TTL).

### Orchestration

L'orchestrateur pilote le **démon Docker de l'hôte** (socket monté) pour créer des
conteneurs frères. Abstrait derrière un **port** du domaine (`LabRuntimePort`) pour pouvoir
changer d'implémentation (Docker, plus tard Podman ou un pool de micro-VM) sans toucher au
domaine.

### Images par parcours (pas un conteneur unique)

Un **petit jeu d'images** maintenu dans le dépôt, une par famille :

- **Code** : rust (`cargo`), java/maven, node, python, sql (postgres client).
- **Infra (Phase 3)** : kubectl + **k3d/kind** (cluster jetable), terraform (+ provider
  local/null), docker-in-docker.

### Phasage (du plus rentable au plus lourd)

- **Phase 1 — Pilote (parcours `backend`)** : exécution de code dans un conteneur
  **verrouillé**, validation par script, de bout en bout jusqu'au déverrouillage du nœud.
  Prouve toute la chaîne **sans** le poids d'un cluster.
- **Phase 2** : autres parcours de code (rust, java, python, sql).
- **Phase 3** : bacs à sable **infra** (k3d/dind) pour docker, kubernetes, terraform,
  cka, ckad — terminal complet, clusters jetables.

## Sécurité (cœur du chantier, non négociable)

Donner le socket Docker au backend équivaut à un accès quasi-root sur l'hôte. Le conteneur
de pratique doit être **fortement sandboxé** :

- **Sans réseau sortant** par défaut (`--network none`, exceptions explicites par tâche).
- **Limites** CPU, mémoire, **pids**, taille d'écriture ; **TTL** et **reaper** obligatoires.
- `--no-new-privileges`, profil **seccomp**, capabilities **drop ALL** (+ ajouts minimaux),
  utilisateur **non root**, racine en **lecture seule** + volume de travail dédié.
- **Jamais** l'image applicative ni le réseau Compose de prod ; réseau/segment dédié.
- L'orchestrateur n'expose **aucune** primitive Docker à l'utilisateur : il ne fait que
  créer/détruire/exec selon une **liste blanche** d'opérations.
- La Phase 3 (dind/k3d) est **privilégiée** et sera traitée à part, avec son propre durcissement.

## Conséquences

**Positives**

- Comble le vrai trou pédagogique : on **pratique** et la validation d'un nœud **signifie**
  quelque chose.
- Réutilise le patron événementiel existant (`ExamPassedEvent` → gamification/progression).
- Politique de validation **rétrocompatible** (défaut `LESSONS_ONLY` : zéro régression).

**Négatives / risques**

- **Surface de sécurité** importante (socket Docker). C'est le principal risque, d'où le
  durcissement ci-dessus et l'abstraction par port.
- **Coût ressources** : un conteneur par apprenant actif ; un cluster-dans-conteneur (k3d)
  pèse lourd. Sur une machine locale (déjà tendue en mémoire), **quotas stricts** et **faible
  concurrence**. Prévoir une file/limite de sessions simultanées.
- Maintenance d'un **jeu d'images** et de **scripts de validation** par tâche.

## Alternatives considérées

- **QCM/quiz comme évaluation** : rejeté par le PO (pas une vraie pratique).
- **Exercices à réponse vérifiée (texte/regex), sans exécution** : plus léger mais ne
  « fait » rien ; gardé comme repli éventuel pour des nœuds non exécutables.
- **Service de lab externe** (killercoda, Instruqt) : rejeté (contrainte 100% local).
- **Correction par points-clés** (moteur `code_challenges` existant, sans exécution) :
  insuffisant pour une vraie épreuve pratique ; pourra compléter, pas remplacer.

## Suivi

Épic **SF-91** dans `BACKLOG.md` (découpage en stories, WIP = 1, pilote = `backend`).
