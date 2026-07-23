@echo off
if exist "%~dp0descanso.exe" (
    start "" "%~dp0descanso.exe"
) else (
    powershell.exe -WindowStyle Hidden -ExecutionPolicy Bypass -File "%~dp0descanso.ps1"
)
