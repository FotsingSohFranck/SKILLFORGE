# --- Étape 1 : build Maven multi-modules (produit app/target/skillforge.jar) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# Copie des descripteurs d'abord pour tirer parti du cache de couches sur les dépendances.
COPY pom.xml ./
COPY common/pom.xml common/
COPY iam/pom.xml iam/
COPY catalogue/pom.xml catalogue/
COPY contenu/pom.xml contenu/
COPY apprentissage/pom.xml apprentissage/
COPY evaluation/pom.xml evaluation/
COPY gamification/pom.xml gamification/
COPY analytics/pom.xml analytics/
COPY app/pom.xml app/
RUN mvn -q -pl app -am -DskipTests dependency:go-offline || true
# Sources puis build du seul module applicatif (+ ses dépendances internes).
COPY . .
RUN mvn -q -pl app -am -DskipTests clean package

# --- Étape 2 : image d'exécution légère (JRE seul) — cible par défaut ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=build /workspace/app/target/skillforge.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# --- Étape 2 bis : variante avec client Docker, pour l'évaluation pratique (lab, SF-91) ---
# Utilisée UNIQUEMENT quand le lab est activé (docker-compose.lab.yml, cible runtime-lab).
# L'image par défaut (runtime) reste sans client Docker et sans accès réseau au build.
FROM runtime AS runtime-lab
USER root
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates curl gnupg; \
    install -m 0755 -d /etc/apt/keyrings; \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc; \
    chmod a+r /etc/apt/keyrings/docker.asc; \
    codename="$(. /etc/os-release && echo "$VERSION_CODENAME")"; \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${codename} stable" > /etc/apt/sources.list.d/docker.list; \
    apt-get update; \
    apt-get install -y --no-install-recommends docker-ce-cli; \
    apt-get purge -y gnupg; apt-get autoremove -y; \
    rm -rf /var/lib/apt/lists/*
