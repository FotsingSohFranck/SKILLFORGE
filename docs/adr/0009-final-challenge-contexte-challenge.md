# ADR-0009 : Final Challenge — contexte borné `challenge`, moteur de simulation déterministe

## Statut

Proposé — 2026-11-17

## Contexte

Aujourd'hui l'évaluation d'un nœud/roadmap repose sur des **questions indépendantes** :
quiz informatifs (`evaluation`), examens blancs chronométrés, et — depuis SF-91 — une
**épreuve pratique** exécutable (`lab`). Il manque une évaluation **globale et
contextualisée** qui vérifie que l'apprenant sait **mobiliser** ce qu'il a appris dans une
situation professionnelle, et pas seulement répondre à des items isolés.

Le Product Owner veut un **Final Challenge** (cf. `docs/FINAL-CHALLENGE.md`) : une **mission
simulée** à parcours dynamique en fin de nœud (puis de roadmap). L'apprenant prend des
décisions successives qui modifient un **état de simulation** ; le moteur calcule les
conséquences et la situation suivante ; l'issue produit un **score par compétence**, des
**conditions éliminatoires**, et un **bilan**.

Contraintes structurantes tirées de la spec (§3, §6, §17, §18) : l'évaluation doit rester
**déterministe, versionnée, auditable et contrôlable** malgré la liberté de parcours ; le
**serveur fait foi** ; une tentative est liée à une **version immuable** de scénario ; l'IA
ne doit **pas** décider seule d'une note ou d'une règle.

## Décision

Créer un **nouveau contexte borné `challenge`** (hexagonal, comme les autres), distinct de
`evaluation`. Motif : le modèle (état de simulation, transitions, conséquences, scoring par
compétence, guidage) est **fondamentalement différent** des items indépendants de
`evaluation` ; le mélanger alourdirait un contexte déjà chargé (quiz + défis + examens).

### Priorité : un moteur de règles déterministe, pas une génération IA libre (spec §23)

Le socle est un **moteur de simulation à états** piloté par des **règles configurées** et
**versionnées**. L'immersion graphique, le guidage visuel et l'assistance IA viennent
**renforcer** ce socle ensuite, sans jamais devenir l'autorité qui décide de la justesse ou
de la note.

### Modèle (domaine, calqué sur §6/§16 de la spec)

- `ChallengeScenario` → `ScenarioVersion` (**immuable**, publiée) qui contient des
  `ScenarioNode` (contexte, événement, interaction, `choices`, `evaluation_rules`,
  `effects`, `competencies`, `transition_rules`, `is_terminal`).
- `SimulationSession` (tentative, liée à **une** `ScenarioVersion`) porte un
  `SimulationState` (variables définies **par scénario**), un historique `DecisionRecord`,
  les `TriggeredEvent`, et produit un `ChallengeResult` + `CompetencyResult`.
- Les moteurs de la spec §15 sont des **services applicatifs/domaine** d'un même contexte :
  Scenario / Decision / Simulation-State / Consequence / Scoring / Guidance. Learning
  Analytics s'appuiera sur le contexte `analytics` existant.

### Flux (déterministe, serveur fait foi — spec §6.4)

décision reçue → autorisée ? → évaluée selon les critères du nœud → effets calculés → état
mis à jour → conditions critiques vérifiées → transition sélectionnée → décision enregistrée
→ situation suivante présentée. Pour une **même version + même état + même décision**, le
résultat est **cohérent et reproductible** (audits/tests/comparaison de tentatives).

### Accès (gating)

Le Final Challenge d'un nœud s'ouvre **en fin de nœud** quand les prérequis sont satisfaits.
On **réutilise le patron de gating** déjà en place pour l'épreuve pratique (lien avec
`apprentissage`), plutôt que d'inventer un nouveau mécanisme.

### Authoring versionné

Scénarios définis en **JSON pivot validé** (schéma + import admin), sur le **modèle de
l'import quiz/examens** de `evaluation`. Une `ScenarioVersion` publiée est immuable ; modifier
un scénario crée une nouvelle version et **n'altère pas** les tentatives déjà commencées
(spec §14.5, §17).

### Phasage (du socle au plus lourd — tranches SF-92a…h)

- **SF-92a — Socle moteur (fin de nœud, mode formatif, choix discrets).** Modèle + migration,
  1 scénario écrit à la main, import, cycle complet serveur (briefing → décisions → état →
  transitions → terminal → score/compétences → rapport), tests déterministes. **Pas d'UI
  immersive, pas d'IA, pas de chrono.**
- **SF-92b** guidage (indicateur 3 états sans révéler la réponse) · **SF-92c** UI immersive
  (glassmorphism, journal, ambiance, a11y) · **SF-92d** révision de décision + conséquences
  persistantes + reprise de session · **SF-92e** retour aux cours + modes formatif/certificatif
  · **SF-92f** analytics/pilotage · **SF-92g** challenge fin-de-roadmap · **SF-92h** variantes
  assistées par IA (encadrées, jamais autorité de notation).

## Sécurité (spec §18.3)

- **Règles d'évaluation et scoring exécutés côté serveur** uniquement ; réponses/scores
  **non modifiables par le client**.
- Sessions protégées par rôle ; données d'évaluation accessibles aux seuls rôles autorisés.
- Journaux d'audit des actions importantes ; tentatives terminées **non recalculées**
  silencieusement après modification du scénario.
- L'IA (phase ultérieure) ne peut **pas** inventer une règle de notation ou une condition
  éliminatoire pendant une session (spec §17.12).

## Conséquences

**Positives**

- Évaluation **mobilisatrice** (« ce qu'il sait faire », pas « ce qu'il sait »), différenciante.
- Socle **déterministe/versionné/testable** → auditez, comparez, rejouez les tentatives.
- Réutilise gating (lab), import versionné (evaluation), événements de complétion (gamification).
- Contexte isolé : n'alourdit pas `evaluation`, frontières nettes.

**Négatives / risques**

- **Nouveau contexte** = coût initial (modèle, migrations, câblage) ; mitigé par le phasage.
- **Conception de scénarios** exigeante (équité entre branches §7.7, pas de boucle, compétences
  obligatoires couvertes) → outil d'authoring + validations avant publication (SF-92/§14.4).
- **UI immersive** (tranches ultérieures) : accessibilité à soigner (couleur ≠ seul signal,
  reduced-motion) ; dépend de l'effort de tests front (SF-42).

## Alternatives considérées

- **Étendre `evaluation`** : rejeté — modèle trop différent, alourdit un contexte chargé.
- **Génération IA « libre » du parcours/notation** : rejeté pour le MVP (spec §23.3) — non
  auditable, non équitable ; l'IA n'enrichira que des **variantes encadrées** plus tard.
- **Arbre de décision rigide (réponse → question fixe)** : insuffisant (spec §6.1) — on veut
  des transitions fondées sur l'**état** accumulé, pas une arborescence figée.
- **Commencer par la fin de roadmap** : reporté (SF-92g) — périmètre plus large/risqué ;
  on valide d'abord le moteur sur la **fin de nœud**.

## Suivi

Épic **SF-92** dans `BACKLOG.md` (découpage en stories, WIP = 1, 1re tranche = SF-92a).
Spec de référence : `docs/FINAL-CHALLENGE.md`.
