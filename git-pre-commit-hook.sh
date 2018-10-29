#!/bin/sh

# Fail if any command fails
set -e

./gradlew verGJF

# This is for non-.java files.
# May need to remove files that are allowed to have trailing whitespace.
CHANGED_STYLE_FILES=`git diff --staged --name-only --diff-filter=ACM` || true
if [ ! -z "$CHANGED_STYLE_FILES" ]; then
    FILES_WITH_TRAILING_SPACES=`grep -l -s --exclude "*\.xlsx" --exclude "*\.jar" '[[:blank:]]$' ${CHANGED_STYLE_FILES} 2>&1` || true
    if [ ! -z "$FILES_WITH_TRAILING_SPACES" ]; then
      echo "Some files have trailing whitespace: ${FILES_WITH_TRAILING_SPACES}" && exit 1
    fi
fi
