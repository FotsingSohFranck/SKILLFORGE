# ADR-0004 — Authentification JWT stateless (access + refresh)

**Statut :** Accepté · **Date :** 2026-07-18

## Contexte
La plateforme expose des API réactives protégées par rôle (`LEARNER` / `INSTRUCTOR` /
`ADMIN`) et un front SPA React. Il faut une authentification qui s'intègre à Spring Security
**réactif** sans état serveur de session.

## Décision
Authentification **JWT stateless** : un **access token** court + un **refresh token** plus
long. Filtre d'authentification **réactif** (`SecurityWebFilterChain`), autorisation par
rôle. Mots de passe hachés (BCrypt/Argon2). La progression, elle, reste **serveur = source
de vérité** (le stateless concerne l'auth, pas les données métier).

## Conséquences
- **+** Pas de session serveur à répliquer ; scalabilité et simplicité en local.
- **+** Intégration naturelle avec WebFlux security.
- **−** Révocation non triviale d'un access token avant expiration (mitigée par durée courte
  + rotation du refresh).
- **−** Gestion soigneuse du stockage du token côté front (rafraîchissement automatique).

## Alternatives écartées
- **Sessions serveur (cookies + store)** : état côté serveur, moins adapté au SPA réactif
  visé et à l'objectif stateless.
