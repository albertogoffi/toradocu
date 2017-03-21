#!/bin/bash

# Fail if any command fails
set -e

# Check command line arguments
if [ $# -ne 3 ]; then
    echo "No arguments supplied. This script accepts as arguments:"
    echo "1. File where to store statistics"
    echo "2. Distance threshold lower bound"
    echo "3. Distance threshold upper bound"
    exit 1
fi

# Constants and variables initialization
STATS_FILE=$1
MIN_DISTANCE=$2
MAX_DISTANCE=$3 # Edit distance threshold. Word deletion cost will be MAX_DISTANCE + 2
EXPECTED_OUTPUT_FILE_SUFFIX="_goal.json"

# Create Toradocu jar and target systems sources and binaries
./gradlew shadowJar extractSources extractBinaries

# Remove old stats file (if user agrees)
if [ -f $STATS_FILE ]; then
    rm -i $STATS_FILE
fi

# Initialize a new stats file
if [ ! -f $STATS_FILE ]; then
    echo "METHOD,DISTANCE THRESHOLD,REMOVAL COST,\
CORRECT THROWS CONDITIONS,WRONG THROWS CONDITIONS,MISSING THROWS CONDITIONS,THROWS PRECISION,THROWS RECALL,\
CORRECT PARAM CONDITIONS,WRONG PARAM CONDITIONS,MISSING PARAM CONDITIONS,PARAM PRECISION,PARAM RECALL,\
CORRECT RETURN CONDITIONS,WRONG RETURN CONDITIONS,MISSING RETURN CONDITIONS,RETURN PRECISION,RETURN RECALL,\
CORRECT CONDITIONS,WRONG CONDITIONS,MISSING CONDITIONS,PRECISION,RECALL" > $STATS_FILE
fi

# Collect information about target classes from precision/recall test suite and create target descriprtors
TARGET_DESCRIPTORS=()
for test_suite in `find src/test/java/org/toradocu -maxdepth 1 -name 'PrecisionRecall*.java'`; do
    file_name=stats/`echo $test_suite | rev | cut -d '/' -f -1 | rev | cut -d '.' -f 1`".txt"
    TARGET_DESCRIPTORS+=($file_name)
    grep -o --max-count=3 "\".*\"" $test_suite | cut -d"\"" -f2 > $file_name
    grep -o '".*",' $test_suite | cut -d '"' -f 2 >> $file_name
done

# Run Toradocu for each target class and with each params configuration
for target_descriptor in "${TARGET_DESCRIPTORS[@]}"; do
    SRC=`sed -n '1p' $target_descriptor`
    BIN=`sed -n '2p' $target_descriptor`
    EXPECTED_FILE_DIR=`sed -n '3p' $target_descriptor`

    # Initialize targets and expected arrays. targets array contains the target classes while
    # expected array contains the expected file path for each target class.
    i=0
    for target in `tail -n +4 $target_descriptor`; do
    	targets[$i]=$target
    	expected[$i]="$EXPECTED_FILE_DIR$target$EXPECTED_OUTPUT_FILE_SUFFIX"
    	i=$(( i+1 ))
    done

    # Run Toradocu
    for (( j=0; j<${#targets[@]}; j++ )); do
    	for distance_threshold in `seq $MIN_DISTANCE $MAX_DISTANCE`; do
	    word_removal_bound=$(( distance_threshold + 1 )) # Word deletion cost (always equals to edit distance threshold + 1).
    	    for word_removal_cost in `seq 0 $word_removal_bound`; do
    		echo "Executing Toradocu on ${targets[$j]} with threshold=$distance_threshold and word removal cost=$word_removal_cost"
    		java -jar build/libs/toradocu-1.0-all.jar \
    		     --target-class ${targets[$j]} \
    		     --oracle-generation false \
		     --condition-translator-output /dev/null \
    		     --class-dir $BIN \
    		     --source-dir $SRC \
    		     --expected-output ${expected[$j]} \
    		     --distance-threshold $distance_threshold \
    		     --word-removal-cost $word_removal_cost \
		     --stats-file $STATS_FILE
    	    done
    	done
    done

    targets=()
    expected=()
done
