#!/bin/sh

# Fail if any command fails
set -e

CHANGED_JAVA_FILES=`git diff --staged --name-only --diff-filter=ACM | grep '\.java$'` || true
if [ ! -z "$CHANGED_JAVA_FILES" ]; then
    wget -N https://raw.githubusercontent.com/mernst/plume-lib/master/bin/check-google-java-format.py
    python check-google-java-format.py ${CHANGED_JAVA_FILES}
fi

# This is for non-.java files.
# May need to remove files that are allowed to have trailing whitespace.
CHANGED_STYLE_FILES=`git diff --staged --name-only --diff-filter=ACM` || true
if [ ! -z "$CHANGED_STYLE_FILES" ]; then
    FILES_WITH_TRAILING_SPACES=`grep -l -s '[[:blank:]]$' ${CHANGED_STYLE_FILES} 2>&1` || true
    if [ ! -z "$FILES_WITH_TRAILING_SPACES" ]; then
	echo "Some files have trailing whitespace: ${FILES_WITH_TRAILING_SPACES}" && exit 1
    fi
fi

# # This is for non-.java files.
# # May need to remove files that are allowed to have trailing whitespace.
# CHANGED_STYLE_FILES=`git diff --staged --name-only --diff-filter=ACM` || true
# if [ ! -z "$CHANGED_STYLE_FILES" ]; then
#     # echo "CHANGED_STYLE_FILES: ${CHANGED_STYLE_FILES}"
#     grep -q '[[:blank:]]$' ${CHANGED_STYLE_FILES} 2>&1 && (echo "Some files have trailing whitespace:" && grep -l '[[:blank:]]$' ${CHANGED_STYLE_FILES} && exit 1)
# fi
