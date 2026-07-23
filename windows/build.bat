@echo off
chcp 65001 >nul
title Descanso Visual - Build .exe

echo === Descanso Visual - Build .exe ===
echo.

REM Check if ps2exe is installed
powershell.exe -Command "if (-not (Get-Module -ListAvailable -Name ps2exe)) { exit 1 }" >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Instalando modulo ps2exe...
    powershell.exe -Command "Install-Module -Name ps2exe -Force -Scope CurrentUser"
    if %errorlevel% neq 0 (
        echo [ERROR] No se pudo instalar ps2exe.
        echo         Ejecuta PowerShell como administrador y luego:
        echo         Install-Module -Name ps2exe -Force
        pause
        exit /b 1
    )
)

echo [1/2] Compilando descanso.exe...
powershell.exe -Command "ps2exe '%~dp0..\descanso' '%~dp0descanso.exe' -noConsole"
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al compilar descanso.exe
    pause
    exit /b 1
)

echo [2/2] Compilando pantalla-descanso.exe...
powershell.exe -Command "ps2exe '%~dp0..\pantalla-descanso' '%~dp0pantalla-descanso.exe'"
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al compilar pantalla-descanso.exe
    pause
    exit /b 1
)

echo.
echo === Build completado exitosamente ===
echo.
echo Archivos generados:
echo   %~dp0descanso.exe          (scheduler - corre en segundo plano)
echo   %~dp0pantalla-descanso.exe  (ventana de pausa)
echo.
echo Para auto-inicio:
echo   Win+R ^> shell:startup ^> crear acceso directo a descanso.exe
echo.
pause
