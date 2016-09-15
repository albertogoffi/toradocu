#! bin/bash

# Fail if any command fails
set -e

if [ $# -eq 0 ]; then
    echo "No arguments supplied. This scripts accept a list of files of the following format"
    echo "Line 1: Path to the expected output directory"
    echo "Line 2: Path to the target system binaries"
    echo "Line 3: Path to the target system sources"
    echo "Any following line: Fully qualified name of the target class"
    exit 1
fi

# Create Toradocu jar and target systems sources and binaries
./gradlew shadowJar extractSources downloadBinaries

# Constants and variables initialization
MAX_DISTANCE=5
MAX_WORD_REMOVAL_COST=5
EXPECTED_OUTPUT_FILE_SUFFIX="_expected.json"
STATS_FILE="stats/stats.csv"

# Remove old stats file (if user agrees) and initialize a new one (or append to the old one)
if [ -e $STATS_FILE ]; then
    rm -i $STATS_FILE
fi
echo "METHOD,DISTANCE THRESHOLD,REMOVAL COST,CONDITIONS,PRECISION,RECALL" >> $STATS_FILE

for targets_descriptor in $@; do
    EXPECTED_FILE_DIR=`sed -n '1p' $targets_descriptor`
    BIN=`sed -n '2p' $targets_descriptor`
    SRC=`sed -n '3p' $targets_descriptor`

    i=0
    for target in `tail -n +4 $targets_descriptor`; do
    	targets[$i]=$target
    	expected[$i]="$EXPECTED_FILE_DIR$target$EXPECTED_OUTPUT_FILE_SUFFIX"
    	i=$(( i+1 ))
    done

    for (( j=0; j<${#targets[@]}; j++ )); do
    	for distance_threshold in `seq 0 $MAX_DISTANCE`; do
    	    for word_removal_cost in `seq 0 $MAX_WORD_REMOVAL_COST`; do
    		echo "Executing Toradocu on ${targets[$j]} with threshold=$distance_threshold and word removal cost=$word_removal_cost"
    		java -jar build/libs/toradocu-1.0-devel-all.jar \
    		     --target-class ${targets[$j]} \
    		     --oracle-generation false \
		     --condition-translator-output /dev/null \
    		     --class-dir $BIN \
    		     --source-dir $SRC \
    		     --expected-output ${expected[$j]} \
    		     --distance-threshold $distance_threshold \
    		     --word-removal-cost $word_removal_cost
    	    done
    	done
    done

    targets=()
    expected=()
done
