#!/bin/sh

# Fail if any command fails
set -e

CHANGED_JAVA_FILES=`git diff --staged --name-only --diff-filter=ACM | grep -v 'src/test/resources/aspects/*' | grep -v 'src/main/resources/AspectTemplate.java' | grep '\.java$'` || true
if [ ! -z "$CHANGED_JAVA_FILES" ]; then
    (cd .run-google-java-format && git pull -q) || git clone -q https://github.com/plume-lib/run-google-java-format.git .run-google-java-format
    ./.run-google-java-format/check-google-java-format.py ${CHANGED_JAVA_FILES}
fi

# This is for non-.java files.
# May need to remove files that are allowed to have trailing whitespace.
CHANGED_STYLE_FILES=`git diff --staged --name-only --diff-filter=ACM` || true
if [ ! -z "$CHANGED_STYLE_FILES" ]; then
    FILES_WITH_TRAILING_SPACES=`grep -l -s --exclude "*\.xlsx" --exclude "*\.jar" '[[:blank:]]$' ${CHANGED_STYLE_FILES} 2>&1` || true
    if [ ! -z "$FILES_WITH_TRAILING_SPACES" ]; then
      echo "Some files have trailing whitespace: ${FILES_WITH_TRAILING_SPACES}" && exit 1
    fi
fi
