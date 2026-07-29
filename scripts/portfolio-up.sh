#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

PORT="${PORT:-8080}"
IMAGE_NAME="${IMAGE_NAME:-springmind-wallet}"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
elif [[ -f .env.example ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env.example
  set +a
fi

SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/springmind}"
SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-springmind}"
SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-springmind}"

if [[ "${USE_DOCKER:-0}" == "1" ]]; then
  docker compose up -d postgres
  docker build -t "$IMAGE_NAME" .
  exec docker run --rm -p "${PORT}:8080" \
    --network host \
    -e SERVER_PORT=8080 \
    -e SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
    -e SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
    -e SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
    "$IMAGE_NAME"
fi

docker compose up -d postgres
./mvnw -q -DskipTests package
JAR="$(ls -1 target/springmind-wallet-*.jar | grep -v '\.original$' | head -n1)"
exec java ${JAVA_OPTS:-} -jar "$JAR" \
  --server.port="$PORT" \
  --spring.datasource.url="$SPRING_DATASOURCE_URL" \
  --spring.datasource.username="$SPRING_DATASOURCE_USERNAME" \
  --spring.datasource.password="$SPRING_DATASOURCE_PASSWORD"
