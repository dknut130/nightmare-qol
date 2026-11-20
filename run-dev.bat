@echo off
setlocal
title Nightmare QOL - dev client
set "REPO_URL=https://github.com/dknut130/nightmare-qol"
set "REPO_DIR=C:\GitHub\nightmare-qol"
rem If this file is already inside the repo, use that copy of the repo.
if exist "%~dp0gradlew.bat" set "REPO_DIR=%~dp0."

echo.
echo === Nightmare QOL: setup and launch ===
echo.

set "NEED_RESTART="

where git >nul 2>&1
if errorlevel 1 (
  echo Installing Git...
  winget install --id Git.Git -e --silent --accept-source-agreements --accept-package-agreements
  set "NEED_RESTART=1"
)

where java >nul 2>&1
if errorlevel 1 (
  echo Installing Java 17 ^(Temurin^)...
  winget install --id EclipseAdoptium.Temurin.17.JDK -e --silent --accept-source-agreements --accept-package-agreements
  set "NEED_RESTART=1"
)

if defined NEED_RESTART (
  echo.
  echo Installs finished. Windows needs a fresh window to see them.
  echo Close this window and double-click this file again.
  echo.
  echo Also, do the one-time Jagex account login setup for the dev client:
  start "" "https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts"
  pause
  exit /b 0
)


if exist "%REPO_DIR%\.git" (
  echo Getting the latest plugin code...
  git -C "%REPO_DIR%" pull --ff-only
) else (
  echo Downloading the plugin code...
  if not exist "C:\GitHub" mkdir "C:\GitHub"
  git clone "%REPO_URL%" "%REPO_DIR%"
  if errorlevel 1 goto :fail
)

echo.
echo Building and launching the RuneLite dev client.
echo First run downloads a lot and can take several minutes.
echo.
cd /d "%REPO_DIR%"
call gradlew.bat run
if errorlevel 1 goto :fail
exit /b 0

:fail
echo.
echo Something failed. Screenshot or copy the red text above and send it to Claude.
pause
exit /b 1
