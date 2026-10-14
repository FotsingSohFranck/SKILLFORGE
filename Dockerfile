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

# --- Étape 2 : image d'exécution légère (JRE seul) ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=build /workspace/app/target/skillforge.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
