#!/usr/bin/env bash
set -euo pipefail

CONTAINER="bandi-monitor-db-1"
DB_USER="bandi_monitor"
DB_NAME="bandi_monitor"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker cp "$SCRIPT_DIR/seed-profili-siad.sql" "$CONTAINER:/tmp/seed-profili-siad.sql"
docker exec -t "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -f /tmp/seed-profili-siad.sql

echo "Profili SIAD verificati/inseriti."
