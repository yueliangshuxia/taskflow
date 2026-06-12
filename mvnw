#!/bin/sh
# Maven Wrapper
MAVEN_HOME=".mvn/wrapper"
WRAPPER_JAR="$MAVEN_HOME/maven-wrapper.jar"
MAVEN_OPTS="${MAVEN_OPTS:-}"
PRG_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
fi

exec java $MAVEN_OPTS \
  -Dmaven.multiModuleProjectDirectory="$PRG_DIR" \
  -jar "$WRAPPER_JAR" "$@"
