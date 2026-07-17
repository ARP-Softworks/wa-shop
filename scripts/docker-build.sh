#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE="${1:-wa-shop:local}"

echo "==> docker build -t $IMAGE"
cd "$ROOT_DIR"
docker build -t "$IMAGE" .

echo "==> Image built: $IMAGE"
docker image inspect "$IMAGE" --format '{{.Id}} {{.Size}}'
