@echo off
cd /d "%~dp0client"
echo === MySchedule Client (Electron) ===
echo.
call npm run electron:dev
pause
