#!/usr/bin/env bash
set -euo pipefail

echo "== WigAI CI Local Mirror =="
echo ""

echo "-- 1/2: Run unit tests"
./gradlew test --warning-mode=none

echo ""
echo "-- 2/2: Build extension (skip tests)"
./gradlew bwextension -x test --warning-mode=none

echo ""
echo "OK"

