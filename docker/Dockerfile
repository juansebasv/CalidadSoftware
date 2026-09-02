# ============================================================================
#  Etapa 1 - Build + pruebas unitarias + gate de cobertura (JaCoCo >= 90%)
# ============================================================================
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /build
COPY horarios-app/pom.xml ./pom.xml
# Descarga de dependencias en capa cacheable
RUN mvn -B -q dependency:go-offline

COPY horarios-app/src ./src
# 'verify' ejecuta surefire (unit tests) + jacoco:check (falla si cobertura < 90%)
RUN mvn -B verify \
 && mvn -B -q org.apache.maven.plugins:maven-dependency-plugin:3.6.1:copy \
        -Dartifact=org.postgresql:postgresql:42.7.4 \
        -DoutputDirectory=/build/libs

# ============================================================================
#  Etapa 2 - Payara Server 5 (full) sobre JDK 11
# ============================================================================
FROM payara/server-full:5.2022.5-jdk11

# Aplicacion en el context-root "/"
COPY --from=build --chown=payara:payara /build/target/ROOT.war /opt/payara/deployments/ROOT.war

# Driver JDBC de PostgreSQL en el dominio
COPY --from=build --chown=payara:payara /build/libs/postgresql-42.7.4.jar \
     /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar

# Pool + recurso JDBC (jdbc/TesisMyDatasource) apuntando al contenedor "db"
COPY --chown=payara:payara docker/post-boot-commands.asadmin \
     /opt/payara/config/post-boot-commands.asadmin

# Directorio de logs escribible
USER root
RUN mkdir -p /opt/payara/appserver/glassfish/domains/domain1/logs/app \
 && chown -R payara:payara /opt/payara/appserver/glassfish/domains/domain1/logs
USER payara
ENV LOG_DIR=/opt/payara/appserver/glassfish/domains/domain1/logs/app

EXPOSE 8080 4848
