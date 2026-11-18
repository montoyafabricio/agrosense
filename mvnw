#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script for POSIX
# ----------------------------------------------------------------------------

set -e

PROJECT_BASEDIR="$(cd "$(dirname "$0")" && pwd)"
export MAVEN_PROJECTBASEDIR="$PROJECT_BASEDIR"

WRAPPER_JAR="$PROJECT_BASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$PROJECT_BASEDIR/.mvn/wrapper/maven-wrapper.properties"

MVN_VERSION="3.9.9"
DIST_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MVN_VERSION/apache-maven-$MVN_VERSION-bin.zip"
TARGET_DIR="$HOME/.m2/wrapper/dists/apache-maven-$MVN_VERSION"

if [ ! -d "$TARGET_DIR" ]; then
    echo "Downloading Apache Maven $MVN_VERSION..."
    mkdir -p "$TARGET_DIR"
    curl -fsSL "$DIST_URL" -o "/tmp/apache-maven-$MVN_VERSION-bin.zip"
    unzip -q "/tmp/apache-maven-$MVN_VERSION-bin.zip" -d "$TARGET_DIR"
    rm -f "/tmp/apache-maven-$MVN_VERSION-bin.zip"
fi

MVN_BIN="$(find "$TARGET_DIR" -name "mvn" -type f -perm +111 2>/dev/null | head -n 1)"
if [ -z "$MVN_BIN" ]; then
    MVN_BIN="$(find "$TARGET_DIR" -name "mvn" -type f | head -n 1)"
    chmod +x "$MVN_BIN"
fi

exec "$MVN_BIN" "$@"
