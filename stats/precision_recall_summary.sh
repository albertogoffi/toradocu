#! bin/bash

# Fail if any command fails
set -e

# Stats file used is the default for Toradocu
STATS_FILE=build/test-results/results.csv

# Remove old stats file (if user agrees)
if [ -e $STATS_FILE ]; then
    rm -i $STATS_FILE
fi

# Initialize a new stats file
if [ ! -e $STATS_FILE ]; then
    echo "METHOD,DISTANCE THRESHOLD,REMOVAL COST,CORRECT CONDITIONS,WRONG CONDITIONS,MISSING CONDITIONS,PRECISION,RECALL" > $STATS_FILE
fi

# Run Toradocu and collect statistics
./gradlew test --tests "org.toradocu.PrecisionRecall*"
echo "TOTAL,,,=SUM(D1:INDIRECT(\"D\" & ROW()-1)),=SUM(E1:INDIRECT(\"E\" & ROW()-1)),=SUM(D1:INDIRECT(\"F\" & ROW()-1)),," >> $STATS_FILE
echo "AVERAGE PRECISION,=INDIRECT(\"D\" & ROW()-1)/(INDIRECT(\"D\" & ROW()-1) + INDIRECT(\"E\" & ROW()-1))" >> $STATS_FILE
echo "AVERAGE RECALL,=INDIRECT(\"D\" & ROW()-2)/(INDIRECT(\"D\" & ROW()-2) + INDIRECT(\"E\" & ROW()-2) + INDIRECT(\"F\" & ROW()-2))" >> $STATS_FILE

echo "Open the result file: $STATS_FILE"
