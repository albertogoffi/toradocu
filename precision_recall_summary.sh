#!/bin/sh

# Fail if any command fails
set -e

# Stats file used is the default for Toradocu
STATS_FILE=results.csv

# Backup the old result file if present
if [ -f $STATS_FILE ]; then
    mv -f $STATS_FILE $STATS_FILE-$(date -r $STATS_FILE +%Y%m%d)
fi

if [ ! -f $STATS_FILE ]; then
    echo "METHOD,CORRECT CONDITIONS,WRONG CONDITIONS,MISSING CONDITIONS,UNEXPECTED CONDITIONS" > $STATS_FILE
fi

# Run Toradocu and collect statistics
./gradlew --rerun-tasks clean test --tests "org.toradocu.random.AccuracyRandom*"
echo "TOTAL,=SUM(B2:INDIRECT(\"B\" & ROW()-1)),=SUM(C2:INDIRECT(\"C\" & ROW()-1)),=SUM(D2:INDIRECT(\"D\" & ROW()-1)),=SUM(E2:INDIRECT(\"E\" & ROW()-1))" >> $STATS_FILE
echo "NUMBER OF METHODS,=ROW()-3" >> $STATS_FILE
echo "NUMBER OF CONDITIONS,=INDIRECT(\"B\" & ROW()-2)+INDIRECT(\"C\" & ROW()-2)+INDIRECT(\"D\" & ROW()-2)" >> $STATS_FILE
echo "PRECISION,=INDIRECT(\"B\" & ROW()-3)/(INDIRECT(\"B\" & ROW()-3) + INDIRECT(\"C\" & ROW()-3))" >> $STATS_FILE
echo "RECALL,=INDIRECT(\"B\" & ROW()-4)/INDIRECT(\"B\" & ROW()-2)" >> $STATS_FILE

echo "Open the result file: $STATS_FILE"
