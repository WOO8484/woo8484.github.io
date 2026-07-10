#!/usr/bin/env sh
set -eu
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle이 설치되어 있지 않습니다. GitHub Actions에서는 자동 설치됩니다." >&2
exit 1
