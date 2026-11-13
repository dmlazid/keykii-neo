#!/usr/bin/env bash
set -euo pipefail

mkdir -p renderer-audit
rm -rf renderer-audit-pulled

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

set +e
adb shell am instrument -w com.keykii.neo.test/com.keykii.neo.NeoRendererAudit > renderer-audit.log 2>&1
INST_STATUS=$?
set -e

cat renderer-audit.log

adb pull /storage/emulated/0/Android/data/com.keykii.neo/files/theme-v2-audit renderer-audit-pulled \
  || adb pull /sdcard/Android/data/com.keykii.neo/files/theme-v2-audit renderer-audit-pulled \
  || true

if [ -d renderer-audit-pulled ]; then
  cp -r renderer-audit-pulled/. renderer-audit/
  rm -rf renderer-audit-pulled
fi

ls -la renderer-audit
test "$INST_STATUS" -eq 0
grep -q THEME_V2_AUDIT_PASS renderer-audit.log
