@REM Maven Wrapper Script for Windows
@echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set MAVEN_CMD=C:\Users\lenovo\AppData\Local\Temp\maven\apache-maven-3.9.6\bin\mvn.cmd

if exist "%MAVEN_CMD%" (
    "%MAVEN_CMD%" %*
) else (
    mvn %*
)
