@echo off
title 仿微信即时通讯系统 - 一键启动
echo ============================================
echo   仿微信即时通讯系统
echo ============================================
echo.

:: Set environment
set "JAVA_HOME=%~dp0.tools\jdk8u492-b09"
set "MAVEN_HOME=%~dp0.tools\apache-maven-3.9.16"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

:: Check if port 8080 is already in use
netstat -ano | findstr ":8080.*LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo [警告] 端口 8080 已被占用，后端可能已在运行。
    echo        如需重启，请先关闭占用该端口的进程。
    echo.
) else (
    echo [1/2] 启动后端 (Spring Boot, 端口 8080)...
    start "IM-Backend" /MIN cmd /c "cd /d %~dp0 && set JAVA_HOME=%~dp0.tools\jdk8u492-b09 && set MAVEN_HOME=%~dp0.tools\apache-maven-3.9.16 && set PATH=%%JAVA_HOME%%\bin;%%MAVEN_HOME%%\bin;%%PATH%% && mvn spring-boot:run"
    echo        后端正在启动中，请稍候...
)

:: Wait for backend to be ready
echo        等待后端就绪...
:waitloop
timeout /t 2 /nobreak >nul
netstat -ano | findstr ":8080.*LISTENING" >nul 2>&1
if %errorlevel% neq 0 goto waitloop

echo        后端已就绪！
echo.

:: Check if port 3000 is already in use
netstat -ano | findstr ":3000.*LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo [警告] 端口 3000 已被占用，前端可能已在运行。
) else (
    echo [2/2] 启动前端 (Vue 3, 端口 3000)...
    start "IM-Frontend" /MIN cmd /c "cd /d %~dp0frontend && npm run dev"
)

echo.
echo ============================================
echo   后端: http://localhost:8080
echo   前端: http://localhost:3000
echo.
echo   请打开浏览器访问 http://localhost:3000
echo ============================================
echo.
echo   关闭此窗口不会影响已启动的服务。
pause
