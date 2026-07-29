#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

PORT="${PORT:-8080}"
HEALTH_URL="${HEALTH_URL:-http://localhost:${PORT}/actuator/health}"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-120}"
PID_FILE="${PID_FILE:-/tmp/springmind-portfolio.pid}"
LOG_FILE="${LOG_FILE:-/tmp/springmind-portfolio.log}"

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

wait_health() {
  local deadline=$((SECONDS + MAX_WAIT_SECONDS))
  until curl -fsS "$HEALTH_URL" >/dev/null 2>&1; do
    if (( SECONDS >= deadline )); then
      echo "Timeout aguardando health: $HEALTH_URL" >&2
      tail -n 40 "$LOG_FILE" >&2 || true
      return 1
    fi
    sleep 2
  done
}

docker compose up -d postgres
./mvnw -q -DskipTests package
JAR="$(ls -1 target/springmind-wallet-*.jar | grep -v '\.original$' | head -n1)"

if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "Já rodando PID $(cat "$PID_FILE")"
else
  nohup java ${JAVA_OPTS:-} -jar "$JAR" \
    --server.port="$PORT" \
    --spring.datasource.url="$SPRING_DATASOURCE_URL" \
    --spring.datasource.username="$SPRING_DATASOURCE_USERNAME" \
    --spring.datasource.password="$SPRING_DATASOURCE_PASSWORD" \
    >"$LOG_FILE" 2>&1 &
  echo $! >"$PID_FILE"
fi

wait_health
echo "OK springmind $HEALTH_URL"
echo "PID $(cat "$PID_FILE")  log $LOG_FILE"
