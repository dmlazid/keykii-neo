#!/usr/bin/env bash
set -euo pipefail
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
java -m jdk.compiler/com.sun.tools.javac.Main -d "$OUT" \
  app/src/main/java/com/keykii/neo/NeoGeometry.java \
  app/src/main/java/com/keykii/neo/NeoKeyboardLanguage.java \
  tests/NeoGeometryAudit.java
java -cp "$OUT" com.keykii.neo.NeoGeometryAudit
