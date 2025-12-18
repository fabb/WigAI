#!/usr/bin/env bash
set -euo pipefail

BASE_BRANCH="${BASE_BRANCH:-origin/main}"

echo "== WigAI Selective CI (Local) =="
echo "Base branch: ${BASE_BRANCH}"
echo ""

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "ERROR: must be run inside a git repo"
  exit 1
fi

git fetch --quiet origin main || true

CHANGED_FILES="$(git diff --name-only "${BASE_BRANCH}...HEAD" || true)"

if [[ -z "${CHANGED_FILES}" ]]; then
  echo "No changes detected; skipping."
  exit 0
fi

echo "Changed files:"
echo "${CHANGED_FILES}" | sed 's/^/  - /'
echo ""

if command -v rg >/dev/null 2>&1; then
  SHOULD_TEST="$(echo "${CHANGED_FILES}" | rg -q \
    -e '^src/' \
    -e '^bitwig-api-doc-scraper/' \
    -e '^gradle/' \
    -e '^gradlew$' \
    -e '^gradlew\.bat$' \
    -e '^gradle\.properties$' \
    -e '^build\.gradle\.kts$' \
    -e '^settings\.gradle\.kts$' \
    -e '\.kt$' \
    -e '\.kts$' && echo true || echo false)"
else
  SHOULD_TEST="$(echo "${CHANGED_FILES}" | grep -Eq \
    '^(src/|bitwig-api-doc-scraper/|gradle/|gradlew$|gradlew\.bat$|gradle\.properties$|build\.gradle\.kts$|settings\.gradle\.kts$)|(\.kt$)|(\.kts$)' \
    && echo true || echo false)"
fi

if [[ "${SHOULD_TEST}" == "true" ]]; then
  echo "Code/build changes detected -> running tests"
  ./gradlew test --warning-mode=none
  exit 0
fi

echo "No code/build changes detected -> skipping tests"
