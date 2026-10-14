# RUNBOOK autonome — Refonte « débutant → expert » du parcours backend

> **But** : terminer, **de façon 100 % autonome et sans intervention humaine**, la refonte de
> **tous les nœuds** du parcours `backend` au standard pédagogique établi avec les nœuds
> `git` et `internet`. Ce fichier est un **prompt exécutable** : le suivre à la lettre, boucler
> jusqu'à ce que tous les nœuds soient traités, validés et ingérés, puis produire un bilan.
>
> **Directive absolue** : ne JAMAIS demander de validation ni de décision à l'humain. Résoudre
> soi-même toute erreur (JSON, schéma, ports, workflow). Continuer jusqu'au critère d'arrêt.

---

## 1. État & inventaire (détection dynamique, ne pas se fier à une liste figée)

Un nœud est considéré **DÉJÀ TRAITÉ** si **chacune de ses leçons possède une section `QUIZ`**.
Sinon il est **À TRAITER**.

Commande de détection (liste les nœuds À TRAITER) :

```bash
cd content/backend && for f in nodes/*.json; do node -e '
const n=require("./"+process.argv[1]);
const todo=n.lessons.some(l=>!l.sections.some(s=>s.type==="QUIZ"));
if(todo) console.log(n.node);
' "$f"; done
```

Référence (au moment de l'écriture) : `git` et `internet` sont faits ; une fournée de 8
(`java-language`, `relational-databases`, `sql-modeling`, `transactions-acid`,
`indexes-performance`, `orms`, `nosql-databases`, `scaling-databases`) était en cours.
Restants ensuite : `rest-apis`, `api-styles`, `authentication`, `web-security`, `caching`,
`testing`, `ci-cd`, `design-principles`, `architectural-patterns`, `message-brokers`,
`containerization`, `web-servers`, `building-for-scale`, `observability`, `real-time`,
`search-engines`.

---

## 2. Standard par nœud (rigueur identique à `git` / `internet`)

Chaque nœud traité par un agent doit, **sans régression** (conserver tous les slugs de leçons
et `quizRef` existants, append/upsert, ne rien supprimer) :

1. **Rewrite débutant** : dans chaque leçon, chaque **terme technique** est **défini à sa
   première apparition**, avec une **analogie** du quotidien (préfixée par `👉 `) et un
   **exemple concret**. Réécrire/allonger les `CONCEPT` si besoin, rester fidèle à roadmap.sh.
2. **Quiz sur chaque leçon** : pour toute leçon sans `QUIZ`, créer
   `content/backend/quizzes/<node>-<court>-check.json` (`ref` = nom de fichier ; champs
   `ref,node,lesson,title,description,questions[{type:MCQ|TRUE_FALSE|SCENARIO,prompt,
   justification,position,options[{label,correct,position}]}]`, ≥ 1 `correct:true`) et
   ajouter une section `QUIZ` (position finale) pointant vers ce `quizRef`. 3–4 questions.
3. **Diagramme** : ajouter **1** section `DIAGRAM` (`diagramFormat:"MERMAID"`) sur la leçon la
   plus abstraite si elle n'en a pas.
4. **Défis** : créer 1–2 `content/backend/challenges/<node>-<nom>.json`
   (`node,lesson,title,statement,skeleton,solution,keyPoints[],language`) sur des leçons existantes.

**Règles de style** : français, tutoiement ; **interdit** le tiret cadratin `—` dans la prose
(virgule/deux-points/parenthèses) ; **interdit** les emojis sauf `👉` ; markdown `**gras**`,
`*italique*`, `` `code` ``, listes `- ` ; **JSON strictement valide** (vérifier chaque fichier).
Positions de sections uniques et consécutives (0..n) par leçon.

---

## 3. Boucle autonome (répéter jusqu'au critère d'arrêt)

```
TANT QUE (il reste des nœuds À TRAITER) :
  1. Si un workflow backend est en cours → attendre sa fin
     (la notification de complétion relance le tour ; sinon ScheduleWakeup ~1200 s en secours).
  2. Prendre le prochain LOT de 6 à 8 nœuds À TRAITER.
  3. Lancer le Workflow (réutiliser le scriptPath existant) avec args = le lot :
     Workflow({ scriptPath: "<...>/backend-nodes-beginner-wf_f3b73428-5a0.js", args: [ ...lot ] })
     (Le script accepte args en tableau OU en chaîne JSON ; il fan-out 1 agent par nœud.)
  4. À la complétion : `node content-pipeline/validate.mjs --only backend`.
     - Si erreurs → ouvrir les fichiers fautifs, corriger le JSON/schéma soi-même, revalider
       jusqu'à « ✓ 1 parcours valide(s) ». (Causes fréquentes : virgule JSON, position
       dupliquée, quizRef ≠ nom de fichier, option sans `correct`.)
  5. `node content-pipeline/ingest.mjs --only backend --api http://localhost:8090`.
     - Port backend = **8090** (vérifier : `curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/api/v1/roadmaps` → 401/200).
  6. Vérifier en base que chaque nœud du lot a bien un quiz par leçon (0 leçon sans quiz).
  7. Recommencer avec le lot suivant.
```

**Ne jamais** lancer deux workflows en parallèle sur `backend` (fichiers/ingestion partagés).

---

## 4. Critère d'arrêt & clôture

**Arrêt** quand la détection (§1) ne renvoie plus aucun nœud À TRAITER **et** que la dernière
ingestion est « ✓ 1/1 parcours ingéré(s) ».

À la clôture (autonome) :
- `mvn clean install -DskipTests` (sanity build backend) si pertinent.
- Vérif finale en base : 26 nœuds, chaque leçon avec quiz ; comptes leçons/quiz/défis.
- Contrôle qualité d'1 nœud dans Chrome (rendu débutant, quiz, diagramme) si l'outil est dispo.
- Mettre à jour `BACKLOG.md` (entrée « SF-56 — Refonte débutant→expert du parcours backend »)
  et `PASSATION.md` (état des parcours).
- Produire un **bilan final** : nœuds traités, quiz/défis/diagrammes ajoutés, 0 régression.

---

## 5. Rappels techniques

- Contenu : `content/backend/{nodes,quizzes,challenges}/`. Pipeline : `content-pipeline/`.
- Types de section : `CONCEPT` / `CODE`(codeLanguage) / `DIAGRAM`(diagramFormat MERMAID) /
  `EXERCISE`(solution) / `QUIZ`(quizRef).
- Non-régression = upsert par `(nœud, slug)` ; ne jamais `--reset`.
- Le front convertit déjà `👉`→icône, `—`→virgule et rend les listes ; le contenu peut donc
  utiliser `👉` (rendu comme icône). Éviter tout autre emoji et le `—` dans la prose.
- Nœuds de référence à imiter : `content/backend/nodes/git.json`, `.../internet.json`.
</content>
