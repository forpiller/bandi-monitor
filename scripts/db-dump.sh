#!/usr/bin/env bash
set -euo pipefail

CONTAINER="bandi-monitor-db-1"
DB_USER="bandi_monitor"
DB_NAME="bandi_monitor"
OUT_FILE="${1:-bandi_monitor.dump}"

docker exec -t "$CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" -Fc -f "/tmp/$OUT_FILE"
docker cp "$CONTAINER:/tmp/$OUT_FILE" "./$OUT_FILE"

echo "Dump creato: $OUT_FILE"
