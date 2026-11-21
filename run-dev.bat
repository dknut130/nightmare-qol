@echo off
setlocal EnableDelayedExpansion
title Nightmare QOL - dev client
set "REPO_URL=https://github.com/dknut130/nightmare-qol"
set "REPO_DIR=C:\GitHub\nightmare-qol"
rem If this file is already inside the repo, use that copy of the repo.
if exist "%~dp0gradlew.bat" set "REPO_DIR=%~dp0."

echo.
echo === Nightmare QOL: setup and launch ===
echo.

rem ---- Git ----
where git >nul 2>&1
if errorlevel 1 (
  if exist "C:\Program Files\Git\cmd\git.exe" (
    set "PATH=C:\Program Files\Git\cmd;!PATH!"
  ) else (
    echo Installing Git...
    winget install --id Git.Git -e --silent --accept-source-agreements --accept-package-agreements
    set "PATH=C:\Program Files\Git\cmd;!PATH!"
  )
)

rem ---- Java (Temurin 17) ----
call :findjava
if not defined JAVA_HOME (
  echo Installing Java 17. Approve the Windows prompt if one appears...
  winget install --id EclipseAdoptium.Temurin.17.JDK -e --silent --accept-source-agreements --accept-package-agreements
  call :findjava
)
if not defined JAVA_HOME (
  echo.
  echo Could not find or install Java.
  goto :fail
)
set "PATH=!JAVA_HOME!\bin;!PATH!"
echo Using Java at !JAVA_HOME!

rem ---- Plugin code ----
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
echo.
echo RuneLite closed.
pause
exit /b 0

:findjava
set "JAVA_HOME="
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
if defined JAVA_HOME exit /b 0
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
exit /b 0

:fail
echo.
echo Something failed. Screenshot this window and send it to Claude.
pause
exit /b 1
