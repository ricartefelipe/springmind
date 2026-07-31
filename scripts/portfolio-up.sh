#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

HOST_PORT="${HOST_PORT:-9088}"
HEALTH_URL="${HEALTH_URL:-http://localhost:${HOST_PORT}/actuator/health}"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-120}"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

wait_health() {
  local deadline=$((SECONDS + MAX_WAIT_SECONDS))
  until [[ "$(curl -fsS "$HEALTH_URL" 2>/dev/null || true)" == *'"status":"UP"'* ]]; do
    if (( SECONDS >= deadline )); then
      echo "Timeout aguardando health: $HEALTH_URL" >&2
      docker compose logs --tail=40 app >&2 || true
      return 1
    fi
    sleep 2
  done
}

HOST_PORT="$HOST_PORT" docker compose up --build -d

wait_health
echo "OK springmind $HEALTH_URL"
