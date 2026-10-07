@echo off
setlocal

set BASE=C:\Users\admin\AppData\Local\Programs\TaskManagementSystemTools\mysql-9.7.1-winx64
set DATADIR=%BASE%\data

if not exist "%DATADIR%" mkdir "%DATADIR%"

rem Start MySQL if it is not already running
"%BASE%\bin\mysqladmin.exe" -uroot -proot ping >nul 2>&1
if errorlevel 1 (
    echo Starting MySQL...
    start "MySQL" /b "%BASE%\bin\mysqld.exe" --basedir="%BASE%" --datadir="%DATADIR%"
)

rem Wait for MySQL to accept connections
ping 127.0.0.1 -n 6 >nul

rem Launch the Java desktop app
cd /d "C:\Users\admin\Desktop\TaskManagementSystem"
start "Task Management System" java.exe -jar target\TaskManagementSystem.jar

echo Demo startup complete.
exit /b 0
