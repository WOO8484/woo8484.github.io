@echo off
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle이 설치되어 있지 않습니다. GitHub Actions에서는 자동 설치됩니다.
  exit /b 1
)
gradle %*
