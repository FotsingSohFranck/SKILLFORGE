# Lab — fiches d'implémentation par parcours

Ce dossier documente, **parcours par parcours (map par map)**, comment implémenter
l'**évaluation pratique par conteneur** (le « lab exécutable ») : quelle image de base,
quel type d'épreuve, comment la validation automatique fonctionne, et des exemples de
tâches par nœud.

Au moment d'implémenter le lab d'un parcours, **ouvre directement sa fiche** ci-dessous.

- **Cadre global (à lire en premier)** : [`../adr/0008-lab-executable-evaluation-pratique.md`](../adr/0008-lab-executable-evaluation-pratique.md)
- **Suivi / découpage en stories** : épic **SF-91** dans [`../../BACKLOG.md`](../../BACKLOG.md)

## Fiches par parcours

Chaque fiche suit la même structure : *Famille & image · Type d'épreuve · Épreuves par
nœud · Script de validation (modèle) · Spécificités / pièges*.

### Famille code (Phase 1-2 : exécution légère)
- [backend](backend.md) — **pilote** (JVM, JUnit)
- [java-17-21](java-17-21.md) — JVM (nouveautés 17→21)
- [rust](rust.md) — cargo/rustc
- [api-security](api-security.md) — code (API jouet à corriger)
- [prompt-engineering](prompt-engineering.md) — code autour d'un LLM simulé + repli réponse vérifiée
- [backend-performance](backend-performance.md) — SQL (Postgres) + code + repli conceptuel

### Famille infra (Phase 3 : conteneurs / clusters jetables, privilégié, à durcir)
- [docker](docker.md) — docker-in-docker
- [kubernetes](kubernetes.md) — k3d/kind
- [dapr](dapr.md) — Dapr self-hosted + Redis
- [terraform](terraform.md) — terraform offline (providers local/null/random)
- [aws](aws.md) — LocalStack
- [cka](cka.md) — cluster d'admin jetable (kind/k3d)
- [ckad](ckad.md) — cluster développeur jetable (k3d)
- [kcna](kcna.md) — exploration légère (l'évaluation officielle reste le QCM)

### Conception (surtout non exécutable : artefact / réponse vérifiée)
- [software-design](software-design.md) — refactoring exécutable + repli conception
- [software-architect](software-architect.md) — artefacts (ADR, C4) + ArchUnit ponctuel

### Hors périmètre
- [demo](demo.md) — parcours vitrine, pas de lab

> Note transverse : les parcours infra (Phase 3) et le mode privilégié (dind/k3d) feront
> l'objet d'un durcissement de sécurité dédié avant toute implémentation (cf. ADR-0008).
