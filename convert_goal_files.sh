#!/bin/sh

TORADOCU=build/libs/toradocu-1.0-all.jar
CONVERTER=org.toradocu.util.GoalFileConverter

if [ "$#" -ne 3 ]; then
    echo "You must invoke this script with exactly 3 arguments:"
    echo "1. Folder containing json files to be translated."
    echo "2. Binaries of the target system (json files describe comments from this system.)"
    echo "3. Output folder where to save translated files."
    exit 1
fi

./gradlew shadowJar

mkdir -p "$3"

for JSON in "$1"/*_goal.json; do
    BASENAME=$(basename "$JSON" _goal.json)
    OUTPUT_FILENAME="${BASENAME##*.}"_expected.txt
    echo "Converting $JSON to $3/$OUTPUT_FILENAME"
    java -cp "$TORADOCU" "$CONVERTER" "$JSON" "$3/$OUTPUT_FILENAME" "$2"
done
