# SkillForge

Plateforme e-learning **locale** (self-hébergée) qui fait progresser un apprenant de
débutant à professionnel sur plusieurs parcours techniques et de certification.

Monolithe **modulaire hexagonal** — Java 21, Spring Boot 4.1 (WebFlux réactif), R2DBC,
PostgreSQL 18 (UUID v7), Flyway. Front **React + Vite (TypeScript)**.

- Spécification : [`docs/SPECIFICATION.md`](docs/SPECIFICATION.md)
- Architecture & ADR : [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md), [`docs/adr/`](docs/adr)
- Modèle de données : [`docs/DATA-MODEL.md`](docs/DATA-MODEL.md)
- Backlog : [`BACKLOG.md`](BACKLOG.md) · Règles de travail : [`CLAUDE.md`](CLAUDE.md)

## Démarrage en une commande (Docker Compose)

Le plus simple : PostgreSQL 18 + backend + frontend en une commande.

```bash
cp .env.example .env          # ajustez ports / secret si besoin
docker compose up --build     # Postgres 18 + backend WebFlux + front (Nginx)
```

- **Front** : http://localhost:8081 (SPA React, proxy `/api` → backend)
- **API** : http://localhost:8080/api/v1/…
- **OpenAPI / Swagger UI** : http://localhost:8080/swagger-ui.html

Flyway applique les migrations et le **seed** au démarrage : la plateforme est immédiatement
explorable (comptes de démo ci-dessous, parcours « Découverte du backend »). Pour charger du
**contenu réel** (parcours `content/…`), voir le pipeline
[`content-pipeline/`](content-pipeline/README.md) (`validate` puis `ingest`).

> Les ports hôte (`BACKEND_PORT`, `FRONTEND_PORT`) sont configurables dans `.env` si `8080`/`8081`
> sont déjà occupés. Arrêt : `docker compose down` (ajouter `-v` pour effacer la base).

## Prérequis
- JDK 21+ (le build cible le bytecode Java 21 ; testé aussi sous JDK 23).
- Maven 3.9+.
- Docker (pour PostgreSQL 18 en local).

## Build
```bash
mvn clean install          # build + tests de tous les modules
mvn -pl app -am -DskipTests install   # build rapide de l'app
```

## Lancer en local (sans Docker Compose)
1. Démarrer PostgreSQL 18 :
   ```bash
   docker run --rm -d --name sf-pg18 \
     -e POSTGRES_DB=skillforge -e POSTGRES_USER=skillforge -e POSTGRES_PASSWORD=skillforge \
     -p 5432:5432 postgres:18
   ```
2. Lancer l'application (Flyway applique `V1..V8` au démarrage) :
   ```bash
   java -jar app/target/skillforge.jar
   # ou : mvn -pl app spring-boot:run
   ```
3. URLs :
   - API : `http://localhost:8080/api/v1/...`
   - Santé : `http://localhost:8080/actuator/health`
   - OpenAPI JSON : `http://localhost:8080/v3/api-docs`
   - Swagger UI : `http://localhost:8080/swagger-ui.html`

### Configuration (variables d'environnement)
| Variable | Défaut | Rôle |
|---|---|---|
| `SF_R2DBC_URL` | `r2dbc:postgresql://localhost:5432/skillforge` | Connexion runtime (R2DBC) |
| `SF_JDBC_URL`  | `jdbc:postgresql://localhost:5432/skillforge`  | Connexion Flyway (JDBC, démarrage) |
| `SF_DB_USER` / `SF_DB_PASSWORD` | `skillforge` / `skillforge` | Identifiants base |
| `SERVER_PORT` | `8080` | Port HTTP |

Le profil `docker` (utilisé par Docker Compose en SF-41) pointe l'hôte de base sur `postgres`.

> ⚠️ **Note machine de dev :** si un PostgreSQL **natif** occupe déjà le port `5432`, lancez
> le conteneur sur un autre port (ex. `-p 55432:5432`) et surchargez `SF_R2DBC_URL` /
> `SF_JDBC_URL` vers `127.0.0.1:55432`. Le clash n'existe pas en Docker Compose (réseau interne).

### Comptes de démonstration (seed)
| Email | Rôle | Mot de passe |
|---|---|---|
| `admin@skillforge.dev` | ADMIN | `Skillforge123!` |
| `learner@skillforge.dev` | LEARNER | `Skillforge123!` |

### Authentification (JWT)
Toutes les routes hors `/api/v1/auth/**` et documentation exigent un **access token** JWT
(`Authorization: Bearer <token>`). Access 60 min, refresh 30 j (rotation).

```bash
# Connexion -> access + refresh tokens
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"learner@skillforge.dev","password":"Skillforge123!"}'

# Profil courant (jeton d'accès)
curl -s http://localhost:8080/api/v1/auth/me -H "Authorization: Bearer <ACCESS>"

# Inscription (rôle LEARNER)
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"nouveau@demo.dev","password":"motdepasse123","displayName":"Nouveau"}'

# Rafraîchir / se déconnecter
curl -s -X POST http://localhost:8080/api/v1/auth/refresh -H 'Content-Type: application/json' -d '{"refreshToken":"<REFRESH>"}'
curl -s -X POST http://localhost:8080/api/v1/auth/logout  -H 'Content-Type: application/json' -d '{"refreshToken":"<REFRESH>"}'
```

> `SF_JWT_SECRET` doit être surchargé hors dev (≥ 32 octets).
