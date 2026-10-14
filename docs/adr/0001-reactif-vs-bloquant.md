# ADR-0001 — Stack réactive de bout en bout (WebFlux + R2DBC)

**Statut :** Accepté · **Date :** 2026-07-18

## Contexte
La plateforme doit rester fluide en local avec des I/O nombreuses (lecture de graphes de
roadmaps, progression, examens). Spring Boot 4.1 (Spring Framework 7) propose une stack
réactive mature ; les branches 3.x sont EOL.

## Décision
Adopter une stack **réactive de bout en bout** : Spring **WebFlux** + Spring Data **R2DBC**
(`r2dbc-postgresql`) + Spring Security réactif. Les API renvoient `Mono<T>` / `Flux<T>`.
**Aucun `.block()`** ni I/O bloquante dans le chemin runtime.

## Conséquences
- **+** Non-bloquant, bonne densité de connexions, cohérent de la couche web à la base.
- **+** Modèle de programmation homogène (Reactor) sur tous les contextes.
- **−** Courbe d'apprentissage Reactor ; débogage de pipelines réactifs plus délicat.
- **−** JDBC/JPA interdits au runtime — seule exception : **Flyway au démarrage** (JDBC),
  voir [ADR-0005](0005-migrations-flyway.md).

## Alternatives écartées
- **Spring MVC bloquant + JPA** : plus simple mais contraire à l'objectif non-bloquant et à
  la stack figée.
