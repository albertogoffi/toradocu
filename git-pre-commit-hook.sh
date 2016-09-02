#!/bin/sh

# Fail if any command fails
set -e


CHANGED_JAVA_FILES=`git diff --staged --name-only --diff-filter=ACM | grep '\.java$'` || true
if [ ! -z "$CHANGED_JAVA_FILES" ]; then
    ./gradlew test
    ./gradlew verGJF
fi
