# SkillForge --- Final Challenge

## Spécification fonctionnelle détaillée

**Version :** 1.0\
**Statut :** Spécification de référence — épic **SF-92** (contexte borné `challenge`), cf. **ADR-0009**\
**Produit :** SkillForge\
**Module :** Évaluation finale immersive (fin de nœud, puis fin de roadmap)\
**Langue :** Français

> Spec fonctionnelle importée (origine : proposition de l'auteur). Les artefacts de conversion
> Markdown « noeud et noeud et roadmap » ont été nettoyés en « nœud / roadmap ». Le découpage
> technique et l'état d'avancement vivent dans `BACKLOG.md` (épic SF-92) ; la décision de placement
> dans `docs/adr/0009-final-challenge-contexte-challenge.md`.

------------------------------------------------------------------------

# 1. Résumé exécutif

Le **Final Challenge** est un système d'évaluation immersive qui
intervient à la fin d'une nœud / roadmap SkillForge. Il place l'apprenant dans
une mission professionnelle simulée et lui demande d'atteindre un
objectif en prenant des décisions successives.

Contrairement à un examen traditionnel composé de questions
indépendantes, le Final Challenge fonctionne comme une simulation à
parcours dynamiques :

-   l'apprenant reçoit un contexte, une mission et un objectif ;
-   il analyse une situation et choisit une action ;
-   sa décision modifie l'état de la simulation ;
-   le moteur détermine la situation suivante à partir de l'état obtenu
    ;
-   de nouveaux problèmes, contraintes ou événements peuvent apparaître
    ;
-   l'apprenant poursuit jusqu'à atteindre l'objectif, échouer ou
    épuiser les conditions de la simulation.

Le parcours n'est pas une simple liste de questions. Il est construit
progressivement en fonction des décisions de l'apprenant. Plusieurs
apprenants peuvent donc vivre des expériences différentes tout en étant
évalués sur un référentiel de compétences commun.

## 1.1. Orientations retenues

La présente spécification prend en compte les orientations suivantes :

  -----------------------------------------------------------------------
  Dimension                           Orientation retenue
  ----------------------------------- -----------------------------------
  Construction des scénarios          **Hybride** : scénarios et
                                      objectifs conçus et validés par des
                                      experts ; situations, événements et
                                      variantes sélectionnés ou générés
                                      dynamiquement dans des limites
                                      contrôlées

  Expérience visuelle                 **Immersive** : interface de
                                      simulation, ambiance graphique
                                      évolutive, effets de profondeur et
                                      indicateurs de progression

  Structure des parcours              **Dynamique** : les situations
                                      suivantes dépendent de l'état de la
                                      simulation et des décisions prises
  -----------------------------------------------------------------------

Une règle fondamentale doit être respectée : la liberté de parcours ne
doit jamais rendre l'évaluation arbitraire. Les décisions peuvent
varier, mais les compétences à mesurer, les critères de notation et les
conditions de réussite doivent rester définis et contrôlables.

------------------------------------------------------------------------

# 2. Objectifs du système

## 2.1. Objectif pédagogique

Vérifier que l'apprenant sait appliquer les connaissances acquises
pendant sa nœud / roadmap dans une situation globale, contextualisée et
évolutive.

Le système doit mesurer notamment :

1.  la compréhension des concepts ;
2.  la capacité d'analyse ;
3.  le raisonnement et la qualité des décisions ;
4.  la maîtrise technique ou méthodologique ;
5.  l'anticipation des risques ;
6.  la résolution des incidents ;
7.  la capacité à corriger une décision ;
8.  l'atteinte de l'objectif final.

## 2.2. Objectif produit

Faire du Final Challenge une expérience différenciante de SkillForge :
l'apprenant ne répond pas simplement à un questionnaire, il accomplit
une mission.

Le système doit permettre de créer plusieurs types de simulations sans
développer un moteur spécifique pour chaque nœud / roadmap.

## 2.3. Objectifs mesurables

Le système doit pouvoir produire, pour chaque tentative :

-   un résultat global sur 100 ;
-   un statut de réussite ou d'échec ;
-   un score par compétence ;
-   un historique des décisions ;
-   les conséquences déclenchées ;
-   les erreurs critiques éventuelles ;
-   les ressources de cours consultées ;
-   les changements de décision ;
-   un bilan personnalisé des compétences maîtrisées et à renforcer.

------------------------------------------------------------------------

# 3. Principes de conception

Le Final Challenge doit respecter les principes suivants.

### 3.1. Une mission, pas une succession de QCM

Les questions doivent s'inscrire dans une situation cohérente. Une
décision doit avoir un lien logique avec les décisions précédentes et
avec l'objectif de la mission.

### 3.2. Plusieurs chemins possibles

Il ne doit pas exister systématiquement une seule séquence de réponses.
Plusieurs stratégies peuvent être acceptables si elles respectent les
contraintes du scénario.

### 3.3. Une évaluation fondée sur le contexte

Une réponse ne doit pas être jugée uniquement comme vraie ou fausse. Sa
pertinence dépend du contexte, des objectifs, des contraintes et des
risques.

### 3.4. Des conséquences crédibles

Une décision peut entraîner un avantage, un coût, un risque, un retard,
un incident ou une nouvelle contrainte. Les conséquences doivent être
cohérentes avec le domaine évalué.

### 3.5. Pas de correction immédiate révélant la réponse

Pendant la simulation, le système ne doit pas afficher systématiquement
« bonne réponse » ou « mauvaise réponse ». L'apprenant doit observer les
conséquences et interpréter les signaux de l'environnement.

Le bilan détaillé et les explications pédagogiques sont présentés à la
fin, ou à un moment prévu par le concepteur du scénario.

### 3.6. Équité entre les parcours

Les parcours peuvent être différents, mais la difficulté, les
compétences évaluées et les critères de réussite doivent être
comparables. Le système doit éviter qu'un chemin plus court ou plus
facile permette d'obtenir artificiellement un meilleur résultat.

------------------------------------------------------------------------

# 4. Expérience apprenant

## 4.1. Accès au Final Challenge

À la fin d'une nœud / roadmap, lorsque les conditions de progression sont
satisfaites, l'apprenant voit apparaître une étape dédiée :

**Final Challenge --- Épreuve de validation**

La présentation doit préciser :

-   le titre de la mission ;
-   le domaine et les compétences concernées ;
-   l'objectif général ;
-   la durée indicative, si elle est définie ;
-   les règles de la simulation ;
-   les conditions de validation ;
-   les possibilités de consulter les cours ;
-   les conséquences éventuelles d'une nouvelle tentative.

Le lancement d'une tentative doit créer une session distincte afin de
conserver l'historique.

## 4.2. Briefing de mission

Avant le début, l'apprenant reçoit un briefing qui décrit :

-   son rôle dans la situation ;
-   l'organisation ou le projet concerné ;
-   le contexte technique ou professionnel ;
-   les ressources disponibles ;
-   les contraintes ;
-   les objectifs attendus ;
-   les critères généraux de réussite.

Le briefing doit donner suffisamment d'informations pour permettre un
raisonnement sérieux, sans révéler les décisions attendues.

## 4.3. Déroulement d'une étape

Chaque étape de simulation présente les éléments suivants :

1.  **Contexte courant** : état du projet ou de l'environnement.
2.  **Événement ou problème** : ce qui nécessite une décision.
3.  **Informations disponibles** : données, contraintes, journaux,
    demandes ou observations.
4.  **Action attendue** : décision, choix d'une stratégie, diagnostic ou
    réponse.
5.  **Conséquences** : effets de la décision sur l'état de la
    simulation.
6.  **Suite du parcours** : nouvelle situation calculée par le moteur.

Selon la nature de la compétence, une étape peut prendre plusieurs
formes :

-   choix entre plusieurs stratégies ;
-   sélection de plusieurs actions ;
-   classement d'étapes dans le bon ordre ;
-   diagnostic à partir d'informations ;
-   saisie d'une commande ou d'une configuration ;
-   analyse d'un extrait de code ou d'un journal ;
-   réponse argumentée ;
-   arbitrage entre plusieurs contraintes.

Les formats doivent être choisis selon la compétence évaluée, et non
pour varier artificiellement l'interface.

## 4.4. Progression et fin de mission

La simulation continue tant que l'objectif n'est pas atteint et que les
conditions de poursuite sont réunies.

Elle peut se terminer dans l'un des cas suivants :

-   objectif atteint ;
-   échec d'une condition critique ;
-   ressources ou temps épuisés, si le scénario utilise ces contraintes
    ;
-   impossibilité logique de poursuivre ;
-   abandon volontaire ;
-   fin prévue par le scénario.

Une fin de mission doit toujours produire un bilan, même en cas d'échec
ou d'abandon.

------------------------------------------------------------------------

# 5. Exemple de scénario : déployer SkillForge

Cet exemple illustre le fonctionnement du système. Il ne constitue pas
une règle universelle de déploiement : la pertinence d'une solution
dépend toujours du contexte.

## 5.1. Briefing

**Mission :** mettre en production une nouvelle version de SkillForge.

**Objectif :** rendre la plateforme accessible aux utilisateurs, de
manière sécurisée, stable et maintenable.

**Contraintes possibles :**

-   l'application doit être accessible depuis Internet ;
-   les données des utilisateurs doivent être protégées ;
-   le service doit rester disponible ;
-   les sauvegardes doivent être exploitables ;
-   le déploiement doit pouvoir être maintenu par l'équipe ;
-   le budget et les ressources peuvent être limités.

## 5.2. Première décision : choisir une stratégie d'hébergement

Le système peut proposer plusieurs approches, par exemple :

-   hébergement local ou sur une machine de développement ;
-   serveur VPS administré par l'équipe ;
-   plateforme Cloud ou service managé.

Il ne faut pas considérer ces choix comme intrinsèquement bons ou
mauvais. Leur pertinence doit être évaluée par rapport à la mission.

Exemple : un environnement WAMP peut convenir à un usage local de
développement ou de test. En revanche, si la mission exige une
plateforme publique de production, accessible et maintenue de manière
fiable, ce choix peut ne pas satisfaire les objectifs.

## 5.3. Branches et situations suivantes

Si l'apprenant choisit un VPS, le système peut ensuite l'interroger sur
:

-   le système d'exploitation ;
-   la configuration réseau ;
-   le pare-feu ;
-   le serveur Web ou reverse proxy ;
-   le certificat TLS ;
-   la gestion des secrets ;
-   le déploiement de l'application ;
-   la base de données ;
-   les sauvegardes ;
-   la supervision.

S'il choisit une plateforme Cloud, les situations peuvent porter sur :

-   le choix des services ;
-   les permissions ;
-   les variables et secrets ;
-   le réseau ;
-   les coûts ;
-   la haute disponibilité ;
-   les sauvegardes ;
-   la supervision.

Si son choix ne satisfait pas une contrainte fondamentale, la simulation
ne doit pas nécessairement s'arrêter immédiatement. Elle peut faire
apparaître une conséquence réaliste, puis demander à l'apprenant de
réagir.

## 5.4. Événements après le déploiement

Une fois l'application accessible, la mission continue. Le moteur peut
déclencher des événements tels que :

-   hausse du trafic ;
-   ralentissement de la base de données ;
-   certificat bientôt expiré ;
-   tentative d'accès non autorisé ;
-   panne d'un service externe ;
-   saturation du stockage ;
-   erreur après une mise à jour ;
-   restauration nécessaire après une perte de données.

L'apprenant doit démontrer qu'il sait exploiter et maintenir sa
solution, et pas uniquement la mettre en ligne.

## 5.5. Exemple de logique de conséquence

  -----------------------------------------------------------------------
  Décision ou situation               Conséquence possible
  ----------------------------------- -----------------------------------
  Ouvrir tous les ports sans          Augmentation de l'exposition et
  justification                       apparition d'un risque de sécurité

  Ne pas prévoir de sauvegarde        Difficulté ou impossibilité de
                                      restaurer les données

  Configurer une supervision adaptée  Détection plus rapide des incidents

  Déployer sans stratégie de retour   Récupération plus risquée après une
  arrière                             mise à jour défectueuse

  Vérifier la configuration TLS       Réduction des risques liés aux
                                      communications
  -----------------------------------------------------------------------

Les conséquences doivent être définies et validées par un expert du
domaine.

------------------------------------------------------------------------

# 6. Moteur de parcours dynamique

## 6.1. Principe

Le parcours est construit à partir d'un état de simulation. Après chaque
décision, le moteur met à jour cet état, calcule les conséquences et
détermine la prochaine situation.

Le système ne doit pas se limiter à une arborescence rigide dans
laquelle chaque réponse renvoie toujours vers une question fixe. Il doit
pouvoir tenir compte de plusieurs éléments :

-   les décisions précédentes ;
-   les compétences déjà évaluées ;
-   les risques accumulés ;
-   les ressources restantes ;
-   les événements déclenchés ;
-   l'état actuel de l'objectif ;
-   les contraintes du scénario.

## 6.2. Nœud de simulation

Un nœud représente une étape de la mission. Il doit contenir au minimum
:

  Champ                     Description
  ------------------------- ---------------------------------------------
  `id`                      Identifiant unique du nœud
  `title`                   Titre de l'étape
  `context`                 Contexte présenté à l'apprenant
  `event`                   Problème ou événement à traiter
  `available_data`          Informations disponibles
  `interaction_type`        Type d'interaction attendu
  `choices`                 Actions ou réponses possibles
  `evaluation_rules`        Critères d'évaluation
  `effects`                 Modifications de l'état de simulation
  `competencies`            Compétences évaluées
  `next_transition_rules`   Conditions de sélection de la suite
  `is_terminal`             Indique si le nœud peut terminer la mission

## 6.3. État de simulation

L'état de simulation représente la situation actuelle de l'apprenant. Il
peut inclure :

-   la progression vers l'objectif ;
-   le niveau de sécurité ;
-   la disponibilité du service ;
-   le niveau de risque ;
-   le budget restant ;
-   le temps restant, si applicable ;
-   les incidents actifs ;
-   les ressources disponibles ;
-   les décisions déjà prises ;
-   les compétences déjà observées.

Les variables doivent être définies par scénario. Il ne faut pas imposer
les mêmes variables à toutes les nœuds / roadmaps.

## 6.4. Sélection de la prochaine situation

Après une décision, le moteur suit un processus déterministe et traçable
:

1.  recevoir la décision ;
2.  vérifier qu'elle est autorisée ;
3.  l'évaluer selon les critères du nœud ;
4.  calculer ses effets ;
5.  mettre à jour l'état de simulation ;
6.  vérifier les conditions critiques ;
7.  sélectionner la prochaine transition applicable ;
8.  enregistrer la décision et le résultat ;
9.  présenter la situation suivante.

Pour une même version de scénario, un même état et une même décision
doivent produire un résultat cohérent. Cela facilite les audits, les
tests et la comparaison des tentatives.

## 6.5. Génération hybride

L'approche hybride est recommandée :

**Partie contrôlée par les concepteurs**

-   objectifs ;
-   compétences à mesurer ;
-   contraintes métier ;
-   règles de notation ;
-   conséquences critiques ;
-   conditions d'échec ;
-   règles de transition ;
-   situations de référence.

**Partie adaptative**

-   sélection de variantes de situations ;
-   ordre de certains événements ;
-   données contextuelles ;
-   valeurs de certains paramètres ;
-   formulations adaptées au niveau de l'apprenant ;
-   événements supplémentaires compatibles avec l'état courant.

Une IA peut assister la génération de variantes ou de contenus, mais
elle ne doit pas modifier librement les règles de notation ou les
conditions de validation pendant une tentative. Toute variante utilisée
dans une évaluation doit être validée ou encadrée par des règles
strictes.

------------------------------------------------------------------------

# 7. Système de notation

## 7.1. Principes

La notation doit mesurer la qualité du raisonnement et des actions, et
pas uniquement le nombre de réponses correspondant à une réponse modèle.

Le système doit prendre en compte :

-   la pertinence par rapport au contexte ;
-   la qualité technique ;
-   les risques introduits ;
-   les conséquences ;
-   la capacité de correction ;
-   l'atteinte de l'objectif.

Une décision acceptable mais moins optimale ne doit pas être notée comme
une erreur totale si elle reste défendable dans le contexte.

## 7.2. Proposition de pondération

La grille ci-dessous constitue une base configurable pour un scénario
technique.

  Dimension                                   Pondération
  -------------------------------------- ----------------
  Compréhension du contexte                     15 points
  Pertinence des décisions                      25 points
  Maîtrise technique                            25 points
  Gestion des incidents et des risques          20 points
  Atteinte de l'objectif                        15 points
  **Total**                                **100 points**

Les pondérations doivent pouvoir être adaptées selon la nœud / roadmap. Une
simulation de gestion de projet n'aura pas nécessairement les mêmes
dimensions qu'une simulation de cybersécurité.

## 7.3. Score par décision

Chaque décision peut être évaluée selon plusieurs dimensions :

-   adéquation au contexte ;
-   exactitude technique ;
-   sécurité ;
-   coût ou efficacité ;
-   capacité à préserver l'objectif ;
-   justification, si elle est demandée.

Le score doit être associé à des critères explicites et versionnés.

## 7.4. Score global

Le score final doit être calculé à partir des critères du scénario, et
non par une simple moyenne du nombre de questions répondues.

Le moteur doit éviter les biais liés à la longueur des parcours. Une
branche contenant davantage de nœuds ne doit pas automatiquement donner
plus de points ou pénaliser l'apprenant.

Une approche possible consiste à calculer le résultat par compétence,
puis à agréger les scores avec des pondérations définies à l'avance.

## 7.5. Seuil de validation

Proposition initiale :

-   **80 à 100 :** simulation validée ;
-   **60 à 79 :** compétences à renforcer, validation non obtenue ;
-   **0 à 59 :** simulation non validée.

Le seuil est configurable par nœud / roadmap ou par scénario.

## 7.6. Conditions critiques

Certaines erreurs peuvent être éliminatoires, même si le score global
dépasse 80.

Exemples possibles selon le scénario :

-   exposer des données sensibles de manière grave ;
-   supprimer volontairement des données sans sauvegarde ni autorisation
    ;
-   ignorer une condition de sécurité explicitement obligatoire ;
-   déclarer la mission terminée alors qu'une exigence fondamentale
    n'est pas satisfaite.

Une condition éliminatoire doit être :

1.  explicitement définie dans le scénario ;
2.  directement liée à une compétence ou à une exigence fondamentale ;
3.  validée par un expert ;
4.  expliquée dans le bilan final.

Le système doit distinguer le score brut, le score ajusté et le statut
final. Il doit également préciser quelle condition a empêché la
validation.

## 7.7. Équité entre les parcours

Les concepteurs doivent définir une matrice reliant chaque branche aux
compétences évaluées. Le moteur doit vérifier qu'un parcours valide
évalue suffisamment les compétences obligatoires.

Si une branche ne permet pas d'évaluer une compétence essentielle, le
système doit prévoir une situation complémentaire ou un événement
permettant de l'évaluer.

------------------------------------------------------------------------

# 8. Guidance Engine : orientation sans révélation de la réponse

## 8.1. Objectif

Fournir à l'apprenant des indications sur l'évolution de sa situation
sans lui afficher directement la correction.

L'indicateur doit lui permettre de ressentir qu'une décision améliore ou
détériore la situation globale.

## 8.2. Indicateur directionnel

Le système peut présenter trois états :

  -----------------------------------------------------------------------
  État                    Couleur                 Signification
  ----------------------- ----------------------- -----------------------
  Favorable               Vert                    Les décisions
                                                  rapprochent globalement
                                                  l'apprenant des
                                                  objectifs

  À surveiller            Orange                  Des risques ou
                                                  incohérences
                                                  apparaissent

  Défavorable             Rouge                   La situation se dégrade
                                                  ou l'objectif est
                                                  compromis
  -----------------------------------------------------------------------

Les couleurs doivent représenter une tendance, pas certifier qu'une
réponse est bonne ou mauvaise.

## 8.3. Calcul de l'indicateur

L'indicateur peut s'appuyer sur plusieurs variables :

-   progression vers l'objectif ;
-   risques actifs ;
-   exigences critiques satisfaites ;
-   stabilité de la solution ;
-   ressources consommées ;
-   cohérence des décisions.

Il ne doit pas être calculé uniquement à partir du score obtenu. Une
décision peut être techniquement correcte tout en introduisant un risque
secondaire.

Le concepteur doit définir les règles qui associent l'état de simulation
à chaque niveau visuel.

## 8.4. Ne pas transformer l'indicateur en aide à la devinette

L'indicateur ne doit pas révéler la réponse attendue. Par exemple, si
chaque bonne réponse produit systématiquement du vert et chaque mauvaise
réponse du rouge, l'apprenant risque de sélectionner les options selon
la couleur plutôt que de raisonner.

Il est préférable de faire évoluer l'ambiance en fonction de l'état
global et des conséquences cumulées, avec des transitions progressives
et parfois différées.

------------------------------------------------------------------------

# 9. Interface immersive

## 9.1. Direction artistique

L'interface doit donner à l'apprenant l'impression d'être dans un
environnement professionnel simulé.

La direction artistique proposée repose sur :

-   une interface sombre ou adaptable au thème de SkillForge ;
-   des panneaux translucides ;
-   des effets de verre dépoli (*glassmorphism*) ;
-   des arrière-plans floutés ;
-   des animations de transition ;
-   des changements progressifs de teinte ;
-   une hiérarchie visuelle claire ;
-   une typographie lisible.

L'immersion ne doit jamais nuire à la lisibilité, à l'accessibilité ou à
la compréhension des informations.

## 9.2. Éléments de l'écran de simulation

L'écran principal peut contenir :

1.  **En-tête de mission** : nom, objectif et état de la mission.
2.  **Panneau de contexte** : environnement actuel et informations
    utiles.
3.  **Zone d'événement** : problème ou situation à résoudre.
4.  **Zone de décision** : choix, formulaire, éditeur ou interaction.
5.  **Indicateur directionnel** : tendance globale de la situation.
6.  **Indicateurs de ressources** : budget, temps ou autres variables
    pertinentes.
7.  **Journal de mission** : historique des événements et des décisions.
8.  **Accès aux cours** : retour vers les contenus pédagogiques
    autorisés.
9.  **Aide contextuelle** : règles d'utilisation et informations
    disponibles.

Tous ces éléments ne sont pas obligatoires dans chaque scénario.
L'interface doit afficher uniquement les informations pertinentes.

## 9.3. Évolution de l'ambiance

Les changements visuels doivent être progressifs :

-   ambiance favorable : tonalités plus rassurantes ;
-   situation incertaine : tonalités intermédiaires ;
-   situation critique : tonalités d'alerte.

Les couleurs ne doivent pas être le seul moyen de communiquer l'état. Il
faut aussi utiliser des libellés, des icônes et des descriptions
compatibles avec les personnes ayant des difficultés à distinguer
certaines couleurs.

## 9.4. Animations

Les animations peuvent signaler :

-   l'enregistrement d'une décision ;
-   l'apparition d'un événement ;
-   une évolution du niveau de risque ;
-   une modification de l'état de la mission ;
-   une transition vers un nouveau contexte.

Elles doivent être courtes, non bloquantes et désactivables ou réduites
selon les préférences d'accessibilité.

------------------------------------------------------------------------

# 10. Retour aux cours et révision

## 10.1. Accès aux contenus pédagogiques

L'apprenant doit pouvoir consulter les cours de la nœud / roadmap lorsqu'il est
bloqué, dans les limites définies par le mode d'évaluation.

L'accès peut se faire depuis un panneau latéral ou une fenêtre
secondaire afin de conserver le contexte de la simulation.

## 10.2. Reprise de la simulation

Lorsqu'il revient à la mission :

-   son état doit être conservé ;
-   ses décisions précédentes doivent rester enregistrées ;
-   le nœud courant doit être restauré ;
-   aucune décision ne doit être validée sans action explicite de sa
    part.

## 10.3. Impact sur l'évaluation

La consultation des cours peut être autorisée sans pénalité dans un mode
formatif. Dans un mode certificatif, elle peut être limitée ou prise en
compte dans l'évaluation si cela est justifié pédagogiquement.

Il faut donc distinguer les modes :

-   **Mode formatif :** consultation libre des cours et possibilité
    d'expérimenter.
-   **Mode certificatif :** règles de consultation définies avant le
    lancement, appliquées de manière identique à tous.

Le simple fait de consulter un cours ne doit pas être automatiquement
considéré comme une faute.

------------------------------------------------------------------------

# 11. Retour sur une décision et correction du parcours

L'apprenant doit pouvoir, lorsque le scénario l'autorise, revenir sur
une décision antérieure et choisir une autre stratégie.

Deux mécanismes sont à distinguer.

## 11.1. Révision sans réécriture de l'historique

L'apprenant peut revenir à une étape et proposer une autre décision. Le
système conserve les deux versions dans l'historique :

-   décision initiale ;
-   conséquences déjà observées ;
-   décision de remplacement ;
-   conséquences du nouveau choix.

## 11.2. Conséquences persistantes

Le retour en arrière ne doit pas effacer artificiellement les
conséquences déjà survenues. Certaines conséquences peuvent être
irréversibles, comme dans la réalité professionnelle.

Le concepteur doit préciser :

-   les étapes révisables ;
-   les étapes irréversibles ;
-   les coûts éventuels d'une correction ;
-   les conséquences qui persistent ;
-   les cas où un redémarrage de mission est nécessaire.

## 11.3. Mesure de la capacité de correction

Le système peut mesurer :

-   la capacité à reconnaître une incohérence ;
-   le délai avant correction ;
-   le nombre de corrections ;
-   la qualité de la solution de remplacement ;
-   la capacité à limiter les conséquences.

La correction d'une erreur peut être valorisée. L'objectif n'est pas de
punir toute erreur, mais de vérifier la capacité à raisonner et à
s'adapter.

------------------------------------------------------------------------

# 12. Bilan final et rapport d'évaluation

À la fin de la simulation, SkillForge doit présenter un rapport
compréhensible et exploitable.

## 12.1. Résumé

Le rapport présente :

-   le statut final ;
-   le score global ;
-   l'objectif atteint ou non ;
-   les conditions critiques éventuelles ;
-   le nombre de décisions prises ;
-   les événements rencontrés ;
-   la durée de la tentative, si elle est mesurée.

## 12.2. Analyse par compétence

Pour chaque compétence :

-   score obtenu ;
-   niveau de maîtrise estimé ;
-   décisions ayant servi de preuve ;
-   erreurs ou lacunes observées ;
-   recommandations de révision.

Les niveaux de maîtrise doivent être définis et documentés. Ils ne
doivent pas être présentés comme des diagnostics absolus.

## 12.3. Historique du parcours

L'apprenant doit pouvoir revoir :

-   les situations rencontrées ;
-   ses décisions ;
-   les conséquences ;
-   les changements de stratégie ;
-   les événements déclenchés.

Le rapport peut présenter une carte récapitulative du parcours
réellement suivi.

## 12.4. Recommandations pédagogiques

Le système peut proposer des cours ou exercices ciblés en fonction des
compétences insuffisamment maîtrisées.

Exemple :

> La mission a été menée à bien, mais la gestion des sauvegardes et la
> stratégie de restauration doivent être renforcées. Nous vous
> recommandons de revoir le module consacré à la sauvegarde et à la
> reprise après incident.

------------------------------------------------------------------------

# 13. Rôles et responsabilités

## 13.1. Apprenant

L'apprenant peut :

-   consulter le briefing ;
-   lancer une tentative ;
-   prendre des décisions ;
-   consulter les informations autorisées ;
-   accéder aux cours selon le mode ;
-   revenir sur certaines décisions ;
-   consulter son bilan ;
-   recommencer une tentative selon les règles définies.

## 13.2. Concepteur pédagogique

Le concepteur pédagogique peut :

-   créer un scénario ;
-   définir la mission et les objectifs ;
-   associer des compétences ;
-   rédiger les situations ;
-   définir les interactions ;
-   configurer les règles de notation ;
-   définir les transitions ;
-   définir les événements et conséquences ;
-   prévisualiser et tester le scénario.

## 13.3. Expert métier ou technique

L'expert valide :

-   la justesse des situations ;
-   la pertinence des solutions acceptables ;
-   les conséquences ;
-   les risques ;
-   les critères d'évaluation ;
-   les conditions critiques ;
-   le niveau de difficulté.

## 13.4. Administrateur

L'administrateur peut :

-   gérer les versions publiées ;
-   activer ou désactiver un scénario ;
-   consulter les statistiques globales ;
-   gérer les droits ;
-   suivre les incidents et les anomalies.

------------------------------------------------------------------------

# 14. Espace de conception des scénarios

Le moteur doit être accompagné d'un outil permettant de créer et de
maintenir les scénarios sans modifier le code de l'application pour
chaque nouvelle mission.

## 14.1. Création d'un scénario

Le concepteur renseigne :

-   titre et description ;
-   nœud / roadmap associée ;
-   niveau de difficulté ;
-   objectifs ;
-   compétences ;
-   briefing ;
-   règles de validation ;
-   seuil de score ;
-   conditions critiques ;
-   paramètres de simulation.

## 14.2. Création des situations

Chaque situation doit être créée comme un nœud comportant un contexte,
un événement, une interaction, des critères d'évaluation et des effets.

## 14.3. Définition des transitions

Le concepteur définit les conditions qui permettent de sélectionner la
prochaine situation.

Les règles doivent être compréhensibles, testables et traçables. L'outil
peut proposer une représentation visuelle du graphe de parcours, mais le
moteur doit pouvoir fonctionner à partir de règles structurées.

## 14.4. Validation avant publication

Un scénario ne doit pas être publiable sans vérifications :

-   chaque branche atteignable possède une suite valide ou une fin
    explicite ;
-   aucune boucle involontaire ne bloque la simulation ;
-   les conditions de transition ne sont pas contradictoires ;
-   les compétences obligatoires sont évaluables ;
-   les critères de notation sont renseignés ;
-   les conditions critiques sont justifiées ;
-   les variantes générées restent dans les limites prévues ;
-   les parcours ont été testés.

## 14.5. Versionnement

Une tentative doit être liée à une version précise du scénario.

Une modification du scénario publié ne doit pas changer rétroactivement
les règles d'une tentative déjà commencée. Les résultats doivent rester
auditables.

------------------------------------------------------------------------

# 15. Architecture fonctionnelle du système

Le Final Challenge peut être décomposé en composants fonctionnels
indépendants.

## 15.1. Scenario Engine

Responsabilités :

-   charger la version du scénario ;
-   initialiser la mission ;
-   vérifier les préconditions ;
-   fournir le briefing et les situations.

## 15.2. Decision Engine

Responsabilités :

-   recevoir les actions de l'apprenant ;
-   vérifier leur validité ;
-   appliquer les règles d'évaluation ;
-   calculer les transitions applicables.

## 15.3. Simulation State Engine

Responsabilités :

-   maintenir l'état courant ;
-   appliquer les effets ;
-   gérer les ressources ;
-   enregistrer les incidents actifs ;
-   vérifier les contraintes.

## 15.4. Consequence Engine

Responsabilités :

-   appliquer les conséquences définies ;
-   déclencher des événements ;
-   mettre à jour les variables ;
-   garantir la cohérence des effets.

## 15.5. Scoring Engine

Responsabilités :

-   calculer les scores par décision ;
-   agréger les scores par compétence ;
-   calculer le score global ;
-   appliquer les conditions critiques ;
-   produire le statut final.

## 15.6. Guidance Engine

Responsabilités :

-   interpréter l'état global ;
-   calculer les indicateurs de tendance ;
-   fournir les signaux visuels à l'interface ;
-   éviter de révéler directement les réponses.

## 15.7. Learning Analytics

Responsabilités :

-   enregistrer le parcours ;
-   analyser les tentatives ;
-   identifier les compétences à renforcer ;
-   produire les rapports ;
-   fournir des statistiques aux responsables pédagogiques.

## 15.8. Vue d'ensemble des interactions

``` mermaid
flowchart TD
    A[Fin de nœud / roadmap] --> B[Initialisation du Final Challenge]
    B --> C[Briefing de mission]
    C --> D[Présentation de la situation]
    D --> E[Décision de l'apprenant]
    E --> F[Évaluation de la décision]
    F --> G[Mise à jour de l'état]
    G --> H[Calcul des conséquences]
    H --> I{Objectif ou condition de fin ?}
    I -->|Non| J[Sélection de la prochaine situation]
    J --> D
    I -->|Oui| K[Calcul du score final]
    K --> L[Rapport et recommandations]
```

------------------------------------------------------------------------

# 16. Modèle de données conceptuel

Le modèle ci-dessous est indicatif. Il doit être adapté à l'architecture
technique et au système de données de SkillForge.

  -----------------------------------------------------------------------
  Entité                              Rôle
  ----------------------------------- -----------------------------------
  `nœud / roadmap`                           Parcours d'apprentissage auquel le
                                      challenge est associé

  `Competency`                        Compétence évaluée

  `ChallengeScenario`                 Définition générale d'une
                                      simulation

  `ScenarioVersion`                   Version immuable publiée du
                                      scénario

  `ScenarioNode`                      Situation ou étape de la mission

  `ScenarioChoice`                    Action ou réponse proposée

  `EvaluationCriterion`               Critère utilisé pour évaluer une
                                      décision

  `TransitionRule`                    Règle de sélection du prochain nœud

  `ConsequenceRule`                   Effet d'une décision sur la
                                      simulation

  `SimulationSession`                 Tentative d'un apprenant

  `SimulationState`                   État courant de la tentative

  `DecisionRecord`                    Historique d'une décision

  `TriggeredEvent`                    Événement déclenché

  `CompetencyResult`                  Résultat obtenu pour une compétence

  `ChallengeResult`                   Résultat final

  `CourseConsultation`                Consultation d'un contenu
                                      pédagogique pendant la mission
  -----------------------------------------------------------------------

## 16.1. Relations principales

-   Une nœud / roadmap peut être associée à un ou plusieurs scénarios.
-   Un scénario possède plusieurs versions.
-   Une version contient plusieurs nœuds.
-   Un nœud peut proposer plusieurs choix.
-   Un choix peut déclencher plusieurs effets.
-   Un nœud peut avoir plusieurs règles de transition.
-   Un apprenant peut réaliser plusieurs sessions.
-   Une session est liée à une version précise du scénario.
-   Une session contient plusieurs décisions et événements.
-   Une session produit un résultat global et des résultats par
    compétence.

## 16.2. Exemple de structure JSON simplifiée

``` json
{
  "scenarioId": "skillforge-deployment",
  "version": 1,
  "title": "Déployer SkillForge en production",
  "objective": "Mettre en production une plateforme accessible, sécurisée et maintenable",
  "competencies": [
    "deployment",
    "networking",
    "security",
    "monitoring",
    "incident-management"
  ],
  "initialNodeId": "choose-hosting",
  "nodes": [
    {
      "id": "choose-hosting",
      "type": "decision",
      "title": "Choisir une stratégie d'hébergement",
      "context": "La plateforme doit être accessible aux utilisateurs.",
      "choices": [
        {
          "id": "vps",
          "label": "Utiliser un VPS",
          "effects": [
            {
              "variable": "operational_responsibility",
              "operation": "increase",
              "value": 1
            }
          ]
        },
        {
          "id": "cloud",
          "label": "Utiliser une plateforme Cloud",
          "effects": [
            {
              "variable": "managed_services",
              "operation": "increase",
              "value": 1
            }
          ]
        }
      ]
    }
  ]
}
```

Ce JSON illustre uniquement la structure générale. Une implémentation
complète devra définir les types, les validations, les opérateurs
autorisés, les règles de transition et les contrôles de sécurité.

------------------------------------------------------------------------

# 17. Règles métier essentielles

1.  Une tentative est toujours associée à une version précise d'un
    scénario.
2.  Le moteur ne doit jamais modifier silencieusement les critères de
    notation pendant une tentative.
3.  Toute décision doit être enregistrée avec son contexte et son
    horodatage.
4.  Une transition doit être justifiée par une règle identifiable.
5.  Les conséquences doivent être déterministes pour une même version,
    un même état et une même décision, sauf variabilité explicitement
    contrôlée.
6.  Une branche ne doit pas permettre de contourner une compétence
    obligatoire.
7.  Le score final doit tenir compte des compétences et des critères du
    scénario, pas uniquement du nombre de réponses.
8.  Les conditions critiques doivent être explicites et auditées.
9.  L'accès aux cours dépend du mode d'évaluation défini avant le
    lancement.
10. Une correction de décision doit conserver l'historique.
11. Le rapport final doit expliquer le statut de validation.
12. Une IA ne peut pas inventer une règle de notation ou une condition
    éliminatoire pendant une session.
13. Le système doit pouvoir reprendre une session interrompue lorsque le
    scénario l'autorise.
14. Les événements et conséquences doivent rester cohérents avec l'état
    de simulation.
15. Les données d'évaluation doivent être protégées et accessibles
    uniquement aux rôles autorisés.

------------------------------------------------------------------------

# 18. Exigences non fonctionnelles

## 18.1. Fiabilité

-   Les décisions enregistrées ne doivent pas être perdues lors d'un
    rafraîchissement ou d'une interruption.
-   Les mises à jour de l'état et l'enregistrement de la décision
    doivent être cohérents.
-   Les tentatives terminées ne doivent pas être recalculées
    silencieusement après une modification du scénario.

## 18.2. Performance

-   Le passage d'une situation à la suivante doit être suffisamment
    rapide pour préserver l'immersion.
-   Le chargement des contenus doit être optimisé.
-   Les animations ne doivent pas bloquer les interactions.
-   Les calculs de notation doivent pouvoir être exécutés et vérifiés de
    manière fiable.

## 18.3. Sécurité

-   Les sessions doivent être protégées contre les accès non autorisés.
-   Les réponses et scores ne doivent pas être modifiables directement
    par le client.
-   Les règles d'évaluation doivent être exécutées côté serveur.
-   Les contenus et données sensibles doivent respecter les permissions.
-   Les journaux doivent permettre d'auditer les actions importantes.

## 18.4. Accessibilité

-   Les couleurs ne doivent pas être le seul moyen d'indiquer un état.
-   Les textes doivent rester lisibles sur les fonds translucides.
-   Les animations doivent respecter les préférences de réduction des
    mouvements.
-   Les interactions doivent être utilisables au clavier lorsque cela
    est pertinent.
-   L'interface doit s'adapter aux écrans mobiles et aux ordinateurs.

## 18.5. Maintenabilité et évolutivité

-   Ajouter un scénario ne doit pas nécessiter de modifier le moteur
    central.
-   Les règles doivent être configurables et versionnées.
-   Les composants doivent être testables indépendamment.
-   Les formats d'interaction doivent pouvoir évoluer sans casser les
    scénarios existants.

------------------------------------------------------------------------

# 19. Modes d'évaluation

Il est recommandé de distinguer deux modes.

## 19.1. Mode formatif

Objectif : apprendre en expérimentant.

Caractéristiques possibles :

-   accès libre aux cours ;
-   possibilité de recommencer ;
-   retours pédagogiques plus fréquents ;
-   essais et corrections encouragés ;
-   conséquences parfois réinitialisables ;
-   score utilisé principalement comme indicateur de progression.

## 19.2. Mode certificatif

Objectif : vérifier le niveau atteint.

Caractéristiques possibles :

-   règles annoncées avant le lancement ;
-   critères de notation stables ;
-   accès aux cours limité selon le règlement ;
-   historique complet ;
-   conditions de validation strictes ;
-   variantes contrôlées pour garantir l'équité ;
-   rapport final détaillé.

Le mode choisi doit être visible avant le démarrage. L'apprenant ne doit
pas découvrir après coup que les règles de consultation ou de correction
étaient différentes de ce qu'il pouvait raisonnablement attendre.

------------------------------------------------------------------------

# 20. Critères d'acceptation fonctionnels

Le module peut être considéré comme fonctionnellement prêt pour une
première mise en production lorsque les scénarios de test suivants sont
satisfaits.

  -----------------------------------------------------------------------
  ID                                  Critère d'acceptation
  ----------------------------------- -----------------------------------
  AC-01                               Un apprenant ayant terminé les
                                      prérequis de la nœud / roadmap peut
                                      accéder au Final Challenge.

  AC-02                               Le briefing et les règles sont
                                      présentés avant le lancement.

  AC-03                               Une tentative est créée et liée à
                                      une version du scénario.

  AC-04                               Une décision valide met à jour
                                      l'état de simulation.

  AC-05                               Le moteur sélectionne la situation
                                      suivante selon les règles
                                      configurées.

  AC-06                               Deux décisions différentes peuvent
                                      conduire à des parcours différents.

  AC-07                               Les conséquences sont enregistrées
                                      et consultables dans l'historique.

  AC-08                               L'indicateur visuel évolue en
                                      fonction de l'état de la
                                      simulation.

  AC-09                               L'indicateur ne révèle pas
                                      directement la réponse attendue.

  AC-10                               L'apprenant peut consulter les
                                      cours lorsque le mode l'autorise.

  AC-11                               Une session interrompue peut être
                                      reprise selon les règles du
                                      scénario.

  AC-12                               Une décision révisée ne supprime
                                      pas l'historique initial.

  AC-13                               Le moteur calcule un score global
                                      et des scores par compétence.

  AC-14                               Une condition critique peut
                                      empêcher la validation malgré un
                                      score élevé.

  AC-15                               Le rapport final explique le score
                                      et le statut obtenu.

  AC-16                               Le scénario peut être versionné et
                                      testé avant publication.

  AC-17                               Une modification d'un scénario
                                      publié n'altère pas les tentatives
                                      déjà commencées.

  AC-18                               Le parcours ne peut pas contourner
                                      une compétence obligatoire sans
                                      évaluation complémentaire.
  -----------------------------------------------------------------------

------------------------------------------------------------------------

# 21. Stratégie de tests

Les tests doivent couvrir à la fois le moteur, les scénarios et
l'expérience utilisateur.

## 21.1. Tests du moteur

-   calcul des conséquences ;
-   mise à jour de l'état ;
-   sélection des transitions ;
-   détection des fins de mission ;
-   calcul des scores ;
-   application des conditions critiques ;
-   reprise d'une session.

## 21.2. Tests de scénarios

-   parcours nominal ;
-   parcours alternatif ;
-   décisions incohérentes ;
-   erreurs critiques ;
-   correction d'une décision ;
-   événements déclenchés ;
-   branches sans suite ;
-   boucles involontaires ;
-   couverture des compétences obligatoires.

## 21.3. Tests d'équité

Faire exécuter plusieurs parcours à des profils de test différents et
vérifier que :

-   les compétences obligatoires sont évaluées ;
-   les critères restent cohérents ;
-   les chemins ne donnent pas d'avantage injustifié ;
-   la difficulté reste comparable ;
-   le score reflète les performances et non la longueur du parcours.

## 21.4. Tests d'interface

-   affichage sur ordinateur et mobile ;
-   lisibilité des panneaux translucides ;
-   contraste des textes ;
-   navigation clavier ;
-   réduction des animations ;
-   évolution des couleurs ;
-   conservation de la session.

------------------------------------------------------------------------

# 22. Indicateurs de pilotage

Les responsables de SkillForge pourront suivre :

-   taux de lancement du Final Challenge ;
-   taux de réussite ;
-   score moyen par nœud / roadmap ;
-   score moyen par compétence ;
-   taux d'abandon ;
-   durée moyenne des tentatives ;
-   nombre moyen de décisions ;
-   branches les plus fréquemment empruntées ;
-   erreurs critiques les plus fréquentes ;
-   cours les plus consultés pendant la simulation ;
-   taux de correction des décisions ;
-   nombre de tentatives avant validation.

Ces indicateurs doivent être interprétés avec prudence. Par exemple, une
durée élevée ne signifie pas nécessairement une mauvaise maîtrise : elle
peut refléter une analyse approfondie.

------------------------------------------------------------------------

# 23. Périmètre recommandé pour une première version (MVP)

Le concept est ambitieux. Il est préférable de construire
progressivement le moteur plutôt que de commencer par une génération IA
entièrement libre.

## 23.1. Inclus dans le MVP

-   lancement d'une simulation à la fin d'une nœud / roadmap ;
-   briefing de mission ;
-   scénarios structurés par nœuds ;
-   parcours dynamiques fondés sur des règles prédéfinies ;
-   plusieurs types de décisions ;
-   état de simulation ;
-   conséquences configurées ;
-   notation par compétence ;
-   score global et conditions critiques ;
-   indicateur visuel à trois états ;
-   historique des décisions ;
-   rapport final ;
-   accès aux cours selon le mode ;
-   outil interne simple de création et de test des scénarios.

## 23.2. À prévoir dans les versions suivantes

-   génération assistée par IA de variantes de situations ;
-   événements plus complexes ;
-   éditeur visuel avancé de graphes ;
-   simulations collaboratives ;
-   simulations avec environnements techniques exécutables ;
-   tableaux de bord analytiques avancés ;
-   recommandations pédagogiques personnalisées ;
-   scénarios adaptatifs fondés sur les résultats de plusieurs
    tentatives.

## 23.3. Recommandation importante

Pour le MVP, les scénarios doivent être définis et validés par des
experts. Le caractère dynamique doit venir d'abord du moteur de règles
et de l'état de simulation.

L'IA pourra ensuite enrichir les variantes, les formulations et certains
événements, sans devenir l'autorité qui décide seule de la justesse
technique ou de la note.

------------------------------------------------------------------------

# 24. Exemple de parcours complet simplifié

``` mermaid
flowchart TD
    A[Mission : mettre SkillForge en production] --> B[Choisir une stratégie d'hébergement]
    B --> C{Stratégie choisie}
    C --> D[Environnement local]
    C --> E[VPS]
    C --> F[Cloud]
    D --> G[Évaluer l'accessibilité et les contraintes de production]
    E --> H[Configurer réseau et serveur]
    F --> I[Configurer services et permissions]
    G --> J[Analyser les conséquences]
    H --> K[Déployer l'application]
    I --> K
    J --> K
    K --> L[Tester disponibilité et sécurité]
    L --> M{Incident ou objectif atteint ?}
    M -->|Incident| N[Diagnostiquer et corriger]
    N --> L
    M -->|Objectif atteint| O[Valider la mission]
    O --> P[Calcul du score et rapport final]
```

Ce schéma est volontairement simplifié. Dans le système réel, les
transitions dépendront des variables, des conséquences et des règles du
scénario. Certaines branches pourront se rejoindre, d'autres déclencher
des événements supplémentaires ou mener à un échec.

------------------------------------------------------------------------

# 25. Décisions de conception à confirmer

Les points suivants nécessitent une décision produit avant la conception
technique détaillée :

1.  **Tentatives :** combien de tentatives sont autorisées par scénario
    et comment gérer les reprises ?
2.  **Durée :** les missions sont-elles chronométrées ou libres ?
3.  **Révision :** quelles décisions peuvent être modifiées après
    validation ?
4.  **Cours :** l'accès aux cours est-il libre, limité ou configurable
    selon le mode ?
5.  **IA :** l'IA intervient-elle uniquement pour créer des variantes ou
    également pour analyser certaines réponses libres ?
6.  **Réponses libres :** quelles interactions nécessitent une
    correction automatique, une grille experte ou une validation humaine
    ?
7.  **Certification :** le résultat produit-il une attestation ou un
    badge ?
8.  **Difficulté :** le niveau est-il fixe par nœud / roadmap ou peut-il
    évoluer pendant la mission ?

Ces décisions doivent être documentées avant de figer les règles métier.

------------------------------------------------------------------------

# 26. Conclusion

Le Final Challenge doit devenir une composante majeure de l'expérience
pédagogique de SkillForge.

Sa valeur ne réside pas seulement dans son interface immersive ou dans
la variété des questions. Elle repose sur sa capacité à reproduire les
contraintes d'une situation professionnelle, à faire évoluer cette
situation en fonction des décisions de l'apprenant et à mesurer les
compétences réellement mobilisées.

Le modèle retenu repose sur trois piliers :

1.  **Scénarios hybrides :** un cadre pédagogique maîtrisé, enrichi par
    des variantes adaptatives.
2.  **Parcours dynamiques :** chaque décision contribue à déterminer la
    suite de la mission.
3.  **Immersion visuelle :** une interface qui rend visibles les
    conséquences et l'évolution de la situation sans révéler les
    réponses.

La priorité de réalisation doit être un moteur de simulation
déterministe, configurable, versionné et testable. L'immersion graphique
et l'assistance par IA viendront renforcer ce socle, sans compromettre
la fiabilité de l'évaluation.

**Principe directeur : SkillForge ne doit pas seulement vérifier ce que
l'apprenant sait. Le Final Challenge doit démontrer ce qu'il est capable
de faire avec ce qu'il a appris.**
