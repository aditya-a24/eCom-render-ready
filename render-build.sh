#!/usr/bin/env bash
# Render build script
set -e

echo "==> Building with Maven..."
mvn clean package -DskipTests

echo "==> Copying webapp resources alongside the JAR..."
mkdir -p target/webapp
cp -r src/main/webapp/. target/webapp/

echo "==> Build complete."
ls -lh target/
