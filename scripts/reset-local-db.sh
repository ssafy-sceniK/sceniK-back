#!/usr/bin/env bash
set -euo pipefail

DATABASE="${DATABASE:-scenik}"
DB_USER="${DB_USER:-ssafy}"
DB_PASSWORD="${DB_PASSWORD:-ssafy}"
DB_HOST="${DB_HOST:-localhost}"

BACKEND_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCHEMA="$BACKEND_ROOT/src/main/resources/schema.sql"
DATA="$BACKEND_ROOT/src/main/resources/data.sql"

echo "Dropping and recreating MySQL database '$DATABASE' on $DB_HOST..."
mysql -h "$DB_HOST" -u "$DB_USER" "-p$DB_PASSWORD" -e "DROP DATABASE IF EXISTS $DATABASE; CREATE DATABASE $DATABASE DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;"

echo "Applying schema.sql..."
mysql -h "$DB_HOST" -u "$DB_USER" "-p$DB_PASSWORD" "$DATABASE" < "$SCHEMA"

if [ -f "$DATA" ]; then
  echo "Applying data.sql..."
  mysql -h "$DB_HOST" -u "$DB_USER" "-p$DB_PASSWORD" "$DATABASE" < "$DATA"
fi

echo "Local backend DB reset complete."
echo "Create demo users through POST /api/auth/signup so password hashes use the application code."
