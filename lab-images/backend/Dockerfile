# Image de lab du parcours backend (famille code / JVM) — epreuves pratiques SF-91.
# Build hors-ligne a l'execution : le depot Maven est prechauffe ici et monte en lecture
# seule dans le conteneur durci (racine read-only, /workspace en tmpfs, user 1000, reseau none).
FROM maven:3.9-eclipse-temurin-21

ENV MAVEN_REPO=/opt/.m2/repository

# 1) Prechauffage du depot Maven : on compile + teste un projet jouet (JUnit 5) pour
#    telecharger une fois pour toutes JUnit, surefire et le plugin compiler.
WORKDIR /preheat
COPY skeleton/pom.xml ./pom.xml
COPY preheat/ ./
RUN mvn -q -B -ntp -Dmaven.repo.local=${MAVEN_REPO} test \
    && cd / && rm -rf /preheat

# 2) Squelette + grader + tests caches des taches (tout en lecture seule a l'execution).
COPY skeleton/ /grader/skeleton/
COPY tasks/    /grader/tasks/
COPY grade.sh  /grader/grade.sh
RUN chmod 0755 /grader/grade.sh \
    && chmod -R a+rX /opt/.m2 /grader

# Zone de travail (sera un tmpfs inscriptible monte par l'orchestrateur a l'execution).
WORKDIR /workspace
