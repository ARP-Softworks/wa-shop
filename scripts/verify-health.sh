#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8080}"
BASE_URL="${BASE_URL%/}"

echo "==> GET $BASE_URL/api/health"
curl -fsS "$BASE_URL/api/health" | tee /tmp/wa-shop-health.json
grep -q '"status":"UP"' /tmp/wa-shop-health.json || grep -q '"status": "UP"' /tmp/wa-shop-health.json

echo "==> GET $BASE_URL/api/actuator/health"
curl -fsS "$BASE_URL/api/actuator/health" | tee /tmp/wa-shop-actuator.json
grep -q '"status":"UP"' /tmp/wa-shop-actuator.json || grep -q '"status": "UP"' /tmp/wa-shop-actuator.json

echo "==> Admin API must not be public"
CODE="$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/admin/dashboard" || true)"
if [[ "$CODE" != "401" && "$CODE" != "403" ]]; then
  echo "Unexpected status for admin dashboard: $CODE" >&2
  exit 1
fi
echo "OK /api/admin/dashboard => $CODE"

echo "==> SPA fallback (reload path)"
curl -fsS "$BASE_URL/catalogo/demo-reload" | head -c 400
echo
echo "Health verification passed."
