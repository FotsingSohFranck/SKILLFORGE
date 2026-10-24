# Lab — Démo (`demo`)

> Fiche d'implémentation de l'évaluation pratique (ADR-0008 + SF-91).
> Parcours de **démonstration** du format de contenu, pas un vrai parcours d'apprentissage.

## Famille & image

- **Famille** : sans objet (parcours vitrine).
- **Phase SF-91** : aucune. Le parcours `demo` existe pour montrer les types de sections
  (CONCEPT, IMAGE, CODE, DIAGRAM, EXERCISE, QUIZ), pas pour évaluer une compétence.

## Décision

**Pas de lab pour `demo`.** Si l'on veut tout de même illustrer la pratique, on peut y
greffer une tâche jouet triviale (famille code, image `eclipse-temurin:21-jdk`, « afficher
Bonjour SkillForge ») validée par comparaison de sortie, uniquement à titre de vitrine du
mécanisme. Sinon, laisser `demo` en politique `LESSONS_ONLY`.
