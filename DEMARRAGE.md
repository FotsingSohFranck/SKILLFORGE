# 🚀 Démarrer SkillForge (DevRoad)

Ce guide liste **les prérequis** et **toutes les commandes** pour lancer l'application
(backend + frontend + base de données) en local.

---

## 1. Prérequis

| Outil | Version | Utilité | Vérifier |
|---|---|---|---|
| **JDK** | **21** (LTS) | Backend Java 21 / Spring Boot 4.1 | `java -version` |
| **Maven** | 3.9+ | Build multi-modules du backend | `mvn -version` |
| **Node.js + npm** | Node **20+** | Frontend React/Vite + pipeline de contenu | `node -v` / `npm -v` |
| **Docker + Docker Compose** | récent | PostgreSQL 18 (ou toute la stack) | `docker -v` / `docker compose version` |

> ⚠️ Le backend a **besoin d'un PostgreSQL 18 joignable** : au démarrage, Flyway applique les
> migrations. Sans base, le backend s'arrête avec une erreur. Vérifier les conteneurs : `docker ps`.

**Ports utilisés :**

| Service | Docker (Option A) | Dev (Option B) |
|---|---|---|
| Frontend | `8081` | `5173` (Vite) |
| API backend | `8080` | `8080` |
| PostgreSQL | interne | `5432` |
| Swagger / OpenAPI | `8080/swagger-ui.html` | `8080/swagger-ui.html` |

---

## 2. Option A — Tout en Docker (le plus simple)

Lance PostgreSQL + backend + frontend (Nginx) + migrations + seed de démo, en une commande.

```bash
# 1. Créer le fichier d'environnement (une seule fois)
cp .env.example .env

# 2. Construire et démarrer toute la stack
docker compose up --build
```

Puis ouvrir :
- **Front** : http://localhost:8081
- **API** : http://localhost:8080/api/v1
- **OpenAPI / Swagger** : http://localhost:8080/swagger-ui.html

Arrêter / nettoyer :
```bash
# Ctrl+C pour arrêter, puis :
docker compose down          # arrête et supprime les conteneurs
docker compose down -v       # + supprime le volume de la base (remise à zéro)
```

---

## 3. Option B — Mode développement (hot-reload frontend)

Backend et frontend lancés séparément ; le front se recharge à chaud à chaque modification.

### 3.1 Base de données
Utiliser un PostgreSQL 18. Le plus simple via Compose :
```bash
docker compose up -d postgres      # Postgres seul, exposé sur le port 5432
```
(ou réutiliser un conteneur Postgres déjà lancé.)

### 3.2 Backend (depuis la racine `DevRoad/`)
```bash
mvn -q spring-boot:run -pl app
```
→ API sur **http://localhost:8080** — Flyway migre au démarrage.
Swagger : http://localhost:8080/swagger-ui.html

### 3.3 Frontend (depuis `frontend/`)
```bash
npm install        # une seule fois (installe les dépendances)
npm run dev
```
→ **http://localhost:5173** — Vite proxifie automatiquement `/api` vers `http://localhost:8080`.

> Si le backend tourne sur un autre port (ex. `8090`), lancer le front avec la cible surchargée :
> ```bash
> SF_API_URL=http://localhost:8090 npm run dev
> ```

---

## 4. Contenu pédagogique (parcours, leçons, quiz)

Le contenu vit dans `content/<parcours>/`. Il est **déjà ingéré en base** ; à ne relancer
qu'après modification du contenu. Depuis `content-pipeline/` :

```bash
# Valider le contenu d'un parcours
node validate.mjs --only <parcours>

# Ingérer (append/upsert — préserve la progression existante)
node ingest.mjs --only <parcours> --api http://localhost:8080

# Réinitialiser complètement un parcours (⚠️ efface et recharge)
node ingest.mjs --only <parcours> --reset --api http://localhost:8080
```
Parcours disponibles : `backend`, `docker`, `kubernetes`, `terraform`, `software-design`,
`software-architect`, …

---

## 5. Commandes utiles

```bash
# Backend
mvn clean install -DskipTests     # build complet des 10 modules (sans tests)
mvn -q test                       # lancer les tests backend
mvn -q -pl <module> compile       # compiler un seul module

# Frontend (depuis frontend/)
npm run build                     # build de production (tsc + vite build)
npm run typecheck                 # vérification TypeScript seule
npm run preview                   # prévisualiser le build de prod

# Base de données (inspection rapide)
docker ps                                             # conteneurs en cours
docker exec -it <conteneur-postgres> psql -U skillforge -d skillforge
```

---

## 6. Dépannage rapide

| Symptôme | Cause probable | Solution |
|---|---|---|
| Backend s'arrête au démarrage | PostgreSQL non joignable | `docker ps` → démarrer Postgres (`docker compose up -d postgres`) |
| Front affiche des erreurs `/api` (404/proxy) | Backend éteint ou mauvais port | Vérifier que le backend écoute sur `8080` (ou surcharger `SF_API_URL`) |
| Port déjà utilisé (`8080`/`8081`/`5173`) | Autre process sur le port | Changer `BACKEND_PORT` / `FRONTEND_PORT` dans `.env`, ou libérer le port |
| `mvn` ou `java` introuvable | JDK 21 / Maven absents du PATH | (Ré)installer JDK 21 et Maven, vérifier `java -version` |
| Migrations Flyway en échec | Schéma incohérent avec une base existante | `docker compose down -v` puis relancer (remise à zéro de la base) |

---

## 7. Variables d'environnement (`.env`)

Copiées depuis `.env.example` :

```dotenv
# Base de données PostgreSQL 18
POSTGRES_DB=skillforge
POSTGRES_USER=skillforge
POSTGRES_PASSWORD=skillforge

# Sécurité JWT (HS256) — À CHANGER hors développement, minimum 32 octets
SF_JWT_SECRET=dev-only-change-me-please-32bytes-minimum-secret-0123456789

# Ports exposés sur l'hôte
BACKEND_PORT=8080
FRONTEND_PORT=8081
```

> 🔐 En dehors du développement local, **remplacer `SF_JWT_SECRET`** par un secret d'au moins
> 32 octets et ne jamais le committer.
