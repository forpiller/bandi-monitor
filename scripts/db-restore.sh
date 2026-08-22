#!/usr/bin/env bash
set -euo pipefail

CONTAINER="bandi-monitor-db-1"
DB_USER="bandi_monitor"
DB_NAME="bandi_monitor"
IN_FILE="${1:-bandi_monitor.dump}"

if [ ! -f "$IN_FILE" ]; then
  echo "File non trovato: $IN_FILE" >&2
  exit 1
fi

docker compose up -d db

echo "Attendo che il DB sia healthy..."
until [ "$(docker inspect --format='{{.State.Health.Status}}' "$CONTAINER" 2>/dev/null)" = "healthy" ]; do
  sleep 2
done

docker cp "$IN_FILE" "$CONTAINER:/tmp/$IN_FILE"
docker exec -t "$CONTAINER" pg_restore -U "$DB_USER" -d "$DB_NAME" --clean --if-exists "/tmp/$IN_FILE"

echo "Restore completato da: $IN_FILE"
