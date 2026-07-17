#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Frontend tests"
cd "$ROOT_DIR/frontend"
npm ci
npm test -- --watch=false --browsers=ChromeHeadless

echo "==> Frontend production build"
npm run build -- --configuration=production

echo "==> Backend tests + package (with frontend assets)"
cd "$ROOT_DIR/backend"
mvn -B test
mvn -B -Pwith-frontend package -DskipTests

echo "==> Done. JAR: backend/target/wa-shop-*.jar"
ls -1 "$ROOT_DIR"/backend/target/wa-shop-*.jar
