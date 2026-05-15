#!/usr/bin/env sh
# ----------------------------------------------------------------------------
# Maven Wrapper
# ----------------------------------------------------------------------------

basedir=$(dirname "$0")
if [ -z "$basedir" ]; then
  basedir="."
fi
basedir=$(cd "$basedir" && pwd)

if [ -z "$JAVA_HOME" ]; then
  JAVACMD=java
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

MAVEN_PROJECTBASEDIR="$basedir"
CLASSPATH="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
exec "$JAVACMD" -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" -cp "$CLASSPATH" org.apache.maven.wrapper.MavenWrapperMain "$@"
