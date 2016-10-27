#! bin/bash

# Fail if any command fails
set -e

# Check command line arguments
if [ $# -lt 2 ]; then
    echo "No arguments supplied. This script accepts as arguments:"
    echo "1. File where to store statistics"
    echo "2. A list of files of the following format:"
    echo "Line 1: Path to the expected output directory"
    echo "Line 2: Path to the target system binaries"
    echo "Line 3: Path to the target system sources"
    echo "Any following line: Fully qualified name of the target class."
    exit 1
fi

# Constants and variables initialization
MAX_DISTANCE=2 # Edit distance threshold. Word deletion cost will be MAX_DISTANCE + 1
EXPECTED_OUTPUT_FILE_SUFFIX="_expected.json"
STATS_FILE=$1

# Create Toradocu jar and target systems sources and binaries
./gradlew shadowJar extractSources extractBinaries

# Remove old stats file (if user agrees)
if [ -e $STATS_FILE ]; then
    rm -i $STATS_FILE
fi

# Initialize a new stats file
if [ ! -e $STATS_FILE ]; then
    echo "METHOD,DISTANCE THRESHOLD,REMOVAL COST,CONDITIONS,PRECISION,RECALL" > $STATS_FILE
fi

# Run Toradocu for each target class and with each params configuration
for targets_descriptor in $@; do
    EXPECTED_FILE_DIR=`sed -n '1p' $targets_descriptor`
    BIN=`sed -n '2p' $targets_descriptor`
    SRC=`sed -n '3p' $targets_descriptor`

    # Initialize targets and expected arrays. targets array contains the target classes while
    # expected array contains the expected file path for each target class.
    i=0
    for target in `tail -n +4 $targets_descriptor`; do
    	targets[$i]=$target
    	expected[$i]="$EXPECTED_FILE_DIR$target$EXPECTED_OUTPUT_FILE_SUFFIX"
    	i=$(( i+1 ))
    done

    # Run Toradocu
    for (( j=0; j<${#targets[@]}; j++ )); do
    	for distance_threshold in `seq 0 $MAX_DISTANCE`; do
	    word_removal_bound=$(( distance_threshold + 1 )) # Word deletion cost. Is always edit distance threshold + 1.
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

# Parse statistics
echo ""
python stats/stats.py $STATS_FILE
