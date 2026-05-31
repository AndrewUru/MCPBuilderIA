#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

cat <<'EOF'
MCP Builder IA - project skills

Installed:
  ui-material
    Library: com.google.android.material:material:1.14.0
    Purpose: MaterialButton, Material themes, future TextInputLayout, ChipGroup, Tabs.

Recommended next:
  ui-appcompat
    Library: androidx.appcompat:appcompat:1.7.1
    Purpose: AppCompatActivity, better compatibility layer for themed native views.

  state-viewmodel
    Library: androidx.lifecycle:lifecycle-viewmodel:2.10.0
    Purpose: move screen state out of MainActivity.

  network-okhttp
    Library: com.squareup.okhttp3:okhttp:5.3.0
    Purpose: replace HttpURLConnection in ConnectionTester.

  secure-storage
    Library: androidx.security:security-crypto
    Purpose: encrypt saved credentials before replacing SharedPreferences usage.

Rule:
  Install a library in Gradle only when the code starts using it.
EOF

echo
echo "Checking Android build..."
if [[ -x "./gradlew" ]]; then
  ./gradlew assembleDebug --no-daemon
else
  gradle assembleDebug
fi
