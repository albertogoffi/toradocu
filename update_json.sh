#!/bin/sh

if [ "$#" -ne 4 ]; then
    echo "You must invoke this script with exactly 4 arguments:"
    echo "1. Path to the folder where JSON files from which copy condition translations are saved."
    echo "2. Source of the target system (json files describe comments from this system.)"
    echo "3. Binaries of the target system (json files describe comments from this system.)"
    echo "4. Output folder where to save updated Toradocu JSON specs."
    exit 1
fi

# Build Toradocu
TORADOCU_JAR=build/libs/toradocu-1.0-all.jar
./gradlew shadowJar

mkdir -p "$4"

for OLD_SPEC in "$1"/*_goal.json; do
    echo "Updating $OLD_SPEC"
    TARGET_CLASS=$(basename "$OLD_SPEC" _goal.json)
    OUTPUT="$4/$TARGET_CLASS"_goal.json
    OUTPUT_UPDATED="$OUTPUT"_updated
    java -jar $TORADOCU_JAR --target-class $TARGET_CLASS --source-dir "$2" --class-dir "$3" --javadoc-extractor-output "$OUTPUT" --condition-translation false --oracle-generation false
    java -cp $TORADOCU_JAR org.toradocu.util.JsonUpdater "$OLD_SPEC" "$OUTPUT" > "$OUTPUT_UPDATED"
    mv "$OUTPUT_UPDATED" "$OUTPUT"
    echo "Updated specs saved to: $OUTPUT"
done
