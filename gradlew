@echo off
setlocal
set DIR=%~dp0
set APP_BASE_NAME=%~n0
set APP_HOME=%DIR%

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if exist "%JAVA_HOME%\bin\java.exe" (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java
)

%JAVA_EXE% -classpath %CLASSPATH% org.gradle.wrapper.GradleWrapperMain %*
