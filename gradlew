#!/usr/bin/env bash
set -e
if command -v ./gradle &> /dev/null; then
  ./gradle "$@"
elif command -v gradle &> /dev/null; then
  gradle "$@"
else
  echo "Gradle wrapper not available; using system gradle if present."
  gradle "$@"
fi
