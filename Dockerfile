# ============================================================
# Stage 1 — Build the WAR with Maven
# ============================================================
FROM maven:3.9.6-eclipse-temurin-11 AS builder

WORKDIR /build

# Copy POM first so Maven dependency layer is cached separately
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ============================================================
# Stage 2 — Run on official Tomcat 10.1 + JDK 11
# ============================================================
FROM tomcat:10.1-jdk11

# Remove the default Tomcat webapps to keep the image clean
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy our WAR as ROOT so the app is served at /
COPY --from=builder /build/target/eCommerce.war /usr/local/tomcat/webapps/ROOT.war

# Tomcat's server.xml already listens on 8080.
# Render maps its external PORT → container port 8080 via the render.yaml setting.
EXPOSE 8080

# Tomcat start script (default CMD of the base image) is fine.
# We override it only to pass our env vars through to the JVM:
CMD ["catalina.sh", "run"]
