@echo off
cd /d "%~dp0client"
echo === MySchedule Client ===
echo.
echo Starting at http://localhost:5173
echo Close this window to stop.
echo.
call npm run dev
pause
