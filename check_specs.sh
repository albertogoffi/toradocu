#!/bin/bash

# Build Toradocu
TORADOCU_JAR=build/libs/toradocu-1.0-all.jar
./gradlew shadowJar

# Check specs
SPECS_DIR=src/test/resources/goal-output
SPECS[0]="$SPECS_DIR/commons-collections4-4.1"
SPECS[1]="$SPECS_DIR/commons-math3-3.6.1"
SPECS[2]="$SPECS_DIR/guava-19.0"
SPECS[3]="$SPECS_DIR/jgrapht-core-0.9.2"
SPECS[4]="$SPECS_DIR/plume-lib-1.1.0"

for DIR in ${SPECS[@]}; do
    for SPEC in "$DIR"/*_goal.json; do
	java -cp "$TORADOCU_JAR" org.toradocu.util.SpecsChecker "$SPEC"
    done
done
