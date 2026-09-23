#!/usr/bin/env bash
set -euo pipefail

# Run in the original Codespace so the APK keeps its existing debug signing key.
project_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_root"

if [[ "$(git branch --show-current)" != "main" ]]; then
    printf '%s\n' 'Please run this from the main branch of your original Codespace.' >&2
    exit 1
fi

git fetch origin main
if [[ "$(git rev-parse HEAD)" != "$(git rev-parse origin/main)" ]]; then
    printf '%s\n' 'Your branch differs from GitHub. Run git pull --ff-only, then try again.' >&2
    exit 1
fi

bash ./gradlew :app:assembleDebug

# Use Gradle output metadata so the filename always matches the built version.
apk_file="$(python3 - <<'PY'
import json
import pathlib
import re
import shutil

directory = pathlib.Path('app/build/outputs/apk/debug')
metadata = json.loads((directory / 'output-metadata.json').read_text())
if metadata.get('applicationId') != 'com.keykii.neo':
    raise SystemExit('Unexpected APK package; nothing was uploaded.')
elements = metadata.get('elements', [])
if len(elements) != 1:
    raise SystemExit('Expected one APK output; nothing was uploaded.')
element = elements[0]
version = element.get('versionName', '')
if not re.fullmatch(r'[0-9]+(?:\.[0-9]+)*', version):
    raise SystemExit('Unexpected app version; nothing was uploaded.')
source = directory / element['outputFile']
if source.suffix != '.apk' or not source.is_file():
    raise SystemExit('The APK build output is missing.')
destination = pathlib.Path('KeyKii-Neo-' + version + '.apk')
shutil.copyfile(source, destination)
print(destination.name)
PY
)"

# Commit only this APK, preserving unrelated files and staged changes.
git add -- "$apk_file"
if ! git diff --cached --quiet -- "$apk_file"; then
    git commit --only -m "Upload $apk_file from the original Codespace" -- "$apk_file"
fi
git push origin HEAD:main

printf '\nAPK uploaded. Download it here:\nhttps://github.com/dmlazid/keykii-neo/raw/refs/heads/main/%s\n' "$apk_file"
