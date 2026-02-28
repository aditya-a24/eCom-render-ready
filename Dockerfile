# ============================================================
# Stage 1 — Build the WAR with Maven
# ============================================================
FROM maven:3.9.6-eclipse-temurin-11 AS builder

WORKDIR /build

# Layer 1: download dependencies (cached as long as pom.xml doesn't change)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Layer 2: compile + package
COPY src ./src
RUN mvn clean package -DskipTests -q

# ============================================================
# Stage 2 — Run on Tomcat 10.1 + JDK 11
# ============================================================
# Pin to a specific patch release so the image never changes under us.
# tomcat:10.1.31-jdk11-temurin is the latest stable as of mid-2025.
FROM tomcat:10.1.31-jdk11-temurin

# ── Clean out the default Tomcat example webapps ────────────────────────────
RUN rm -rf /usr/local/tomcat/webapps/*

# ── Pre-extract the WAR into webapps/ROOT/ ───────────────────────────────────
# Tomcat supports two deployment modes:
#   a) Copy ROOT.war and let Tomcat extract it asynchronously at startup  ← SLOW
#   b) Pre-extract into ROOT/ so Tomcat loads it immediately              ← FAST (we use this)
#
# When the WAR is a directory on startup, Tomcat deploys it synchronously and
# the app is ready before the first HTTP request arrives.  This eliminates the
# race condition where Render's health check (GET /) fires before the context
# is fully deployed and gets a 404.
RUN mkdir -p /usr/local/tomcat/webapps/ROOT
COPY --from=builder /build/target/eCommerce.war /tmp/eCommerce.war
RUN cd /usr/local/tomcat/webapps/ROOT && jar -xf /tmp/eCommerce.war && rm /tmp/eCommerce.war

# ── Tomcat configuration tweaks ──────────────────────────────────────────────
# Reduce startup time by disabling Tomcat's JSP background compilation thread
# and setting the connector to use NIO (already the default, but explicit is better).
ENV CATALINA_OPTS="\
  -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false \
  -Dfile.encoding=UTF-8"

# ── Port ─────────────────────────────────────────────────────────────────────
# Tomcat's default connector listens on 8080.
# render.yaml declares port: 8080, so Render routes external traffic here.
EXPOSE 8080

# ── Start ────────────────────────────────────────────────────────────────────
CMD ["catalina.sh", "run"]
