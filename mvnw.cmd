@echo off
setlocal
set DIRNAME=%~dp0
set DIRNAME=%DIRNAME:~0,-1%
set MAVEN_PROJECTBASEDIR=%DIRNAME%
set CLASSPATH=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)
"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%CLASSPATH%" org.apache.maven.wrapper.MavenWrapperMain %*
