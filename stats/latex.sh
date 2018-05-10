#!/bin/bash

# This script takes no input and produces latex tables reporting subjects and precision/recall values.
# Generated tables are saved in the path indicated by variable $SUBJECTS_TABLE.

# Check command line arguments
if [ $# -ne 1 ]; then
    echo "No arguments supplied. This script must be invoked with the following argument:"
    echo "- Target Test Suite [paper|regression]"
    echo '"paper" computes values with subject classes of ISSTA 2018 paper (subset of regression test suite)'
    echo '"regression" computes values with extended test suite'
    exit 1
fi

OUTPUT_DIR=latex
SUBJECTS_TABLE="$OUTPUT_DIR"/subject-classes-table.tex
RESULTS_TABLE="$OUTPUT_DIR"/accuracy-table.tex
MACROS="$OUTPUT_DIR"/macros.tex

if [ "$1" = "paper" ]; then
    ACCURACY_TS=src/test/java/org/toradocu/accuracy/paper
    GOAL_FILES=src/test/resources/goal-output/issta_2018
else
    ACCURACY_TS=src/test/java/org/toradocu/accuracy
    GOAL_FILES=src/test/resources/goal-output
fi

JDOCTORPLUS="\ToradocuPlusSem" # Be aware of string matching!
JDOCTOR="\ToradocuPlus"
TORADOCU="\OldToradocu"

RESULTS_TCOMMENT="results_tcomment.csv"
RESULTS_SEMANTICS="results_semantics.csv"
# this should be OldToradocu csv name according to the script in branch 0.1
RESULTS="results_toradocu-0.1.csv"

numberOfClasses() {
    #echo $(find "$1" -name "*.java" -type f | wc -l | tr -d " ")
    echo $(java -cp build/libs/toradocu-1.0-all.jar org.toradocu.util.RandomTestSelection "$1")
}

numberOfAnalyzedClasses() {
    echo $(fgrep -c "@Test" "$1")
}

numberOfMethods() {
    # 1st arg is the path of the test suite from which derive the target class.
    # 2nd arg is the jar containing the target class.
    local count=0
    for class in `fgrep "test(\"" $1 | cut -d '"' -f 2`; do
        count=$((count + $(java -cp "$2":build/libs/toradocu-1.0-all.jar org.toradocu.util.ExecutableMembers $class)))
    done
    echo $count
}

numberOfAnalyzedMethods() {
    echo $(egrep -c "^\"$1" "$RESULTS")
}

numberOfAnalyzedComments() {
    # 1st arg is either "PRE" or "POST" or "EXC".
    # 2nd arg is the path to the folder containing the goal files.
    # 3rd arg is the jar containing the target class.
    local count=0
    for goalFile in "$2"/*.json; do
        count=$((count + $(java -cp "$3":build/libs/toradocu-1.0-all.jar org.toradocu.util.SpecsCount "$goalFile" | fgrep $1 | cut -d ' ' -f 2)))
    done
    echo $count
}

arraySum() {
    local arrayName=$1[@]
    local array=("${!arrayName}")
    local count=0
    for val in "${array[@]}"; do
	      count=$((count + val))
    done
    echo $count
}

# Create Toradocu Jar with dependencies
FATJAR=build/libs/toradocu-1.0-all.jar
if [ -f "$FATJAR" ]; then
  echo -n "Do you want to overwrite existing Toradocu fat jar? [y/n]: "
  read ANSWER
  if [ "$ANSWER" = "y" ]; then
    ./gradlew shadowJar
  fi
else
  ./gradlew shadowJar
fi

# Create output dir
mkdir -p "$OUTPUT_DIR"

echo "Creating subjects table..."

# Collect info for Commons Collections
TS=$ACCURACY_TS/AccuracyCommonsCollections4.java
CLASSES[0]=$(numberOfClasses src/test/resources/src/commons-collections4-4.1-src/src/main/java)
SELECTED_CLASSES[0]=$(numberOfAnalyzedClasses $TS)
METHODS[0]=$(numberOfMethods $TS src/test/resources/bin/commons-collections4-4.1.jar)
DOCUMENTED_METHODS[0]=$(numberOfAnalyzedMethods org.apache.commons.collections4)
PRE[0]=$(numberOfAnalyzedComments PRE $GOAL_FILES/commons-collections4-4.1 src/test/resources/bin/commons-collections4-4.1.jar)
POST[0]=$(numberOfAnalyzedComments POST $GOAL_FILES/commons-collections4-4.1 src/test/resources/bin/commons-collections4-4.1.jar)
EXC_POST[0]=$(numberOfAnalyzedComments EXC $GOAL_FILES/commons-collections4-4.1 src/test/resources/bin/commons-collections4-4.1.jar)

# Collect info for Commons Math
TS=$ACCURACY_TS/AccuracyCommonsMath3.java
CLASSES[1]=$(numberOfClasses src/test/resources/src/commons-math3-3.6.1-src/src/main/java)
SELECTED_CLASSES[1]=$(numberOfAnalyzedClasses $TS)
METHODS[1]=$(numberOfMethods $TS src/test/resources/bin/commons-math3-3.6.1.jar)
DOCUMENTED_METHODS[1]=$(numberOfAnalyzedMethods org.apache.commons.math3)
PRE[1]=$(numberOfAnalyzedComments PRE $GOAL_FILES/commons-math3-3.6.1 src/test/resources/bin/commons-math3-3.6.1.jar)
POST[1]=$(numberOfAnalyzedComments POST $GOAL_FILES/commons-math3-3.6.1 src/test/resources/bin/commons-math3-3.6.1.jar)
EXC_POST[1]=$(numberOfAnalyzedComments EXC $GOAL_FILES/commons-math3-3.6.1 src/test/resources/bin/commons-math3-3.6.1.jar)

# Collect info for Guava
TS=$ACCURACY_TS/AccuracyGuava19.java
CLASSES[2]=$(numberOfClasses src/test/resources/src/guava-19.0-sources)
SELECTED_CLASSES[2]=$(numberOfAnalyzedClasses $TS)
METHODS[2]=$(numberOfMethods $TS src/test/resources/bin/guava-19.0.jar)
DOCUMENTED_METHODS[2]=$(numberOfAnalyzedMethods com.google.common)
PRE[2]=$(numberOfAnalyzedComments PRE $GOAL_FILES/guava-19.0 src/test/resources/bin/guava-19.0.jar)
POST[2]=$(numberOfAnalyzedComments POST $GOAL_FILES/guava-19.0 src/test/resources/bin/guava-19.0.jar)
EXC_POST[2]=$(numberOfAnalyzedComments EXC $GOAL_FILES/guava-19.0 src/test/resources/bin/guava-19.0.jar)

# Collect info for JGraphT
TS=$ACCURACY_TS/AccuracyJGraphT.java
CLASSES[3]=$(numberOfClasses src/test/resources/src/jgrapht-core-0.9.2-sources)
SELECTED_CLASSES[3]=$(numberOfAnalyzedClasses $TS)
METHODS[3]=$(numberOfMethods $TS src/test/resources/bin/jgrapht-core-0.9.2.jar)
DOCUMENTED_METHODS[3]=$(numberOfAnalyzedMethods org.jgrapht)
PRE[3]=$(numberOfAnalyzedComments PRE $GOAL_FILES/jgrapht-core-0.9.2 src/test/resources/bin/jgrapht-core-0.9.2.jar)
POST[3]=$(numberOfAnalyzedComments POST $GOAL_FILES/jgrapht-core-0.9.2 src/test/resources/bin/jgrapht-core-0.9.2.jar)
EXC_POST[3]=$(numberOfAnalyzedComments EXC $GOAL_FILES/jgrapht-core-0.9.2 src/test/resources/bin/jgrapht-core-0.9.2.jar)

# Collect info for Plume-lib
TS=$ACCURACY_TS/AccuracyPlumeLib.java
CLASSES[4]=$(numberOfClasses src/test/resources/src/plume-lib-1.1.0/java/src)
SELECTED_CLASSES[4]=$(numberOfAnalyzedClasses $TS)
METHODS[4]=$(numberOfMethods $TS src/test/resources/bin/plume-lib-1.1.0.jar)
DOCUMENTED_METHODS[4]=$(numberOfAnalyzedMethods plume.)
PRE[4]=$(numberOfAnalyzedComments PRE $GOAL_FILES/plume-lib-1.1.0 src/test/resources/bin/plume-lib-1.1.0.jar)
POST[4]=$(numberOfAnalyzedComments POST $GOAL_FILES/plume-lib-1.1.0 src/test/resources/bin/plume-lib-1.1.0.jar)
EXC_POST[4]=$(numberOfAnalyzedComments EXC $GOAL_FILES/plume-lib-1.1.0 src/test/resources/bin/plume-lib-1.1.0.jar)

# Collect info for GraphStream
TS=$ACCURACY_TS/AccuracyGraphStream.java
CLASSES[5]=$(numberOfClasses src/test/resources/src/gs-core-1.3-sources)
SELECTED_CLASSES[5]=$(numberOfAnalyzedClasses $TS)
METHODS[5]=$(numberOfMethods $TS src/test/resources/bin/gs-core-1.3.jar)
DOCUMENTED_METHODS[5]=$(numberOfAnalyzedMethods org.graphstream)
PRE[5]=$(numberOfAnalyzedComments PRE $GOAL_FILES/gs-core-1.3 src/test/resources/bin/gs-core-1.3.jar)
POST[5]=$(numberOfAnalyzedComments POST $GOAL_FILES/gs-core-1.3 src/test/resources/bin/gs-core-1.3.jar)
EXC_POST[5]=$(numberOfAnalyzedComments EXC $GOAL_FILES/gs-core-1.3 src/test/resources/bin/gs-core-1.3.jar)

# Compute totals
TOTAL[0]=$(arraySum CLASSES)
TOTAL[1]=$(arraySum SELECTED_CLASSES)
TOTAL[2]=$(arraySum METHODS)
TOTAL[3]=$(arraySum DOCUMENTED_METHODS)
TOTAL[4]=$(arraySum PRE)
TOTAL[5]=$(arraySum POST)
TOTAL[6]=$(arraySum EXC_POST)

CONDITIONS=0
CONDITIONS=$((CONDITIONS+${TOTAL[4]}))
CONDITIONS=$((CONDITIONS+${TOTAL[5]}))
CONDITIONS=$((CONDITIONS+${TOTAL[6]}))

# Create subject table
echo 'Commons Collections 4.1' \
     '& '${CLASSES[0]}' & '${SELECTED_CLASSES[0]}' & '${METHODS[0]}' & '${DOCUMENTED_METHODS[0]}' & '${PRE[0]}' & '${POST[0]}' & '${EXC_POST[0]}' \\' > "$SUBJECTS_TABLE"
echo 'Commons Math 3.6.1' \
     '& '${CLASSES[1]}' & '${SELECTED_CLASSES[1]}' & '${METHODS[1]}' & '${DOCUMENTED_METHODS[1]}' & '${PRE[1]}' & '${POST[1]}' & '${EXC_POST[1]}' \\' >> "$SUBJECTS_TABLE"
echo 'GraphStream 1.3' \
     '& '${CLASSES[5]}' & '${SELECTED_CLASSES[5]}' & '${METHODS[5]}' & '${DOCUMENTED_METHODS[5]}' & '${PRE[5]}' & '${POST[5]}' & '${EXC_POST[5]}' \\' >> "$SUBJECTS_TABLE"
echo 'Guava 19' \
     '& '${CLASSES[2]}' & '${SELECTED_CLASSES[2]}' & '${METHODS[2]}' & '${DOCUMENTED_METHODS[2]}' & '${PRE[2]}' & '${POST[2]}' & '${EXC_POST[2]}' \\' >> "$SUBJECTS_TABLE"
echo 'JGraphT 0.9.2' \
     '& '${CLASSES[3]}' & '${SELECTED_CLASSES[3]}' & '${METHODS[3]}' & '${DOCUMENTED_METHODS[3]}' & '${PRE[3]}' & '${POST[3]}' & '${EXC_POST[3]}' \\' >> "$SUBJECTS_TABLE"
echo 'Plume-lib 1.1' \
     '& '${CLASSES[4]}' & '${SELECTED_CLASSES[4]}' & '${METHODS[4]}' & '${DOCUMENTED_METHODS[4]}' & '${PRE[4]}' & '${POST[4]}' & '${EXC_POST[4]}' \\' >> "$SUBJECTS_TABLE"
echo '\midrule' >> "$SUBJECTS_TABLE"
echo 'Total & '${TOTAL[0]}' & '${TOTAL[1]}' & '${TOTAL[2]}' & '${TOTAL[3]}' & '${TOTAL[4]}' & '${TOTAL[5]}' & '${TOTAL[6]}' \\' >> "$SUBJECTS_TABLE"

echo "Created table: $SUBJECTS_TABLE"

# Create results table
echo "Creating results table..."

TAC="tac"
if [ `uname` == "Darwin" ]; then
    TAC="tail -r"
fi

MISSING=${TOTAL[5]}
cat "$RESULTS_TCOMMENT" | $TAC | tail -n +15 | $TAC > results_tcomment_truncated.csv
echo '@tComment     & '`python stats/results_table.py results_tcomment_truncated.csv $MISSING` > "$RESULTS_TABLE"
rm results_tcomment_truncated.csv

MISSING=$((${TOTAL[4]}+${TOTAL[5]}))
cat results_toradocu-0.1.csv | $TAC | tail -n +6 | $TAC | tail -n +2 > results_toradocu_truncated.csv
echo '"METHOD","CORRECT THROWS CONDITIONS","WRONG THROWS CONDITIONS","MISSING THROWS CONDITIONS","UNEXPECTED THROWS CONDITIONS"' > results_toradocu_truncated2.csv
cat results_toradocu_truncated.csv >> results_toradocu_truncated2.csv
echo "$TORADOCU & "`python stats/results_table.py results_toradocu_truncated2.csv $MISSING` >> "$RESULTS_TABLE"
rm results_toradocu_truncated.csv results_toradocu_truncated2.csv

# Following lines are not used in later computations. Commenting them for now
#cat "$RESULTS" | $TAC | tail -n +15 | $TAC > results_jdoctor_truncated.csv
#echo "$JDOCTOR & "`python stats/results_table.py results_jdoctor_truncated.csv` >> "$RESULTS_TABLE"
#rm results_jdoctor_truncated.csv

cat "$RESULTS_SEMANTICS" | $TAC | tail -n +15 | $TAC > results_jdoctor_semantics_truncated.csv
echo "$JDOCTORPLUS & "`python stats/results_table.py results_jdoctor_semantics_truncated.csv` >> "$RESULTS_TABLE"
rm results_jdoctor_semantics_truncated.csv

echo "Created table: $RESULTS_TABLE"

# Create macros
echo "Creating macros..."

echo '\newcommand{\tCommentPrecision}{'`fgrep @tComment "$RESULTS_TABLE" | cut -d "&" -f 11 | xargs | cut -d "." -f 2`'\%\xspace}' > "$MACROS"
echo '\newcommand{\tCommentRecall}{'`fgrep @tComment "$RESULTS_TABLE" | cut -d "&" -f 12 | xargs | cut -d "." -f 2`'\%\xspace}' >> "$MACROS"
echo '\newcommand{\tCommentFMeasure}{'`fgrep @tComment "$RESULTS_TABLE" | cut -d "&" -f 13 | xargs | cut -d ' ' -f 1 | cut -d "." -f 2`'\%\xspace}' >> "$MACROS"

echo '\newcommand{\OldToradocuPrecision}{'`fgrep $TORADOCU "$RESULTS_TABLE" | cut -d "&" -f 11 | xargs | cut -d "." -f 2`'\%\xspace}' >> "$MACROS"
echo '\newcommand{\OldToradocuRecall}{'`fgrep $TORADOCU "$RESULTS_TABLE" | cut -d "&" -f 12 | xargs | cut -d "." -f 2`'\%\xspace}' >> "$MACROS"
echo '\newcommand{\OldToradocuFMeasure}{'`fgrep $TORADOCU "$RESULTS_TABLE" | cut -d "&" -f 13 | xargs | cut -d ' ' -f 1 | cut -d "." -f 2`'\%\xspace}' >> "$MACROS"

echo '\newcommand{\ToradocuPlusPrecision}{'`fgrep "$JDOCTOR " "$RESULTS_TABLE" | cut -d "&" -f 11 | xargs | cut -d "." -f 2`'\%\xspace}' >> "$MACROS"
echo '\newcommand{\ToradocuPlusRecall}{'`fgrep "$JDOCTOR " "$RESULTS_TABLE" | cut -d "&" -f 12 | xargs | cut -d "." -f 2`'\%\xspace}' >> "$MACROS"
echo '\newcommand{\ToradocuPlusFMeasure}{'`fgrep "$JDOCTOR " "$RESULTS_TABLE" | cut -d "&" -f 13 | xargs | cut -d ' ' -f 1 | cut -d "." -f 2`'\%\xspace}' >> "$MACROS"

echo '\newcommand{\totalConditions}{'$CONDITIONS'\xspace}' >> "$MACROS"
echo '\newcommand{\totalClasses}{'${TOTAL[1]}'\xspace}' >> "$MACROS"

# Jdoctor precision/recall values
JDOCTOR_PRECISION_PRE=`fgrep "$JDOCTOR " "$RESULTS_TABLE" | cut -d '&' -f 2 | xargs`
JDOCTOR_RECALL_PRE=`fgrep "$JDOCTOR " "$RESULTS_TABLE" | cut -d '&' -f 3 | xargs`
JDOCTOR_PRECISION_EXC=`fgrep "$JDOCTOR " "$RESULTS_TABLE" | cut -d '&' -f 8 | xargs`
JDOCTOR_RECALL_EXC=`fgrep "$JDOCTOR " "$RESULTS_TABLE" | cut -d '&' -f 9 | xargs`

# Improvement over @tComment
TCOMMENT_PRECISION_PRE=`fgrep @tComment "$RESULTS_TABLE" | cut -d '&' -f 2 | xargs`
TCOMMENT_RECALL_PRE=`fgrep @tComment "$RESULTS_TABLE" | cut -d '&' -f 3 | xargs`
TCOMMENT_PRECISION_EXC=`fgrep @tComment "$RESULTS_TABLE" | cut -d '&' -f 8 | xargs`
TCOMMENT_RECALL_EXC=`fgrep @tComment "$RESULTS_TABLE" | cut -d '&' -f 9 | xargs`
PRECISION_PRE_IMPROVEMENT_TCOMMENT=`bc -l <<< "scale=0; ($JDOCTOR_PRECISION_PRE-$TCOMMENT_PRECISION_PRE)*100 / 1"`
RECALL_PRE_IMPROVEMENT_TCOMMENT=`bc -l <<< "scale=0; ($JDOCTOR_RECALL_PRE-$TCOMMENT_RECALL_PRE)*100 / 1"`
PRECISION_EXC_IMPROVEMENT_TCOMMENT=`bc -l <<< "scale=0; ($JDOCTOR_PRECISION_EXC-$TCOMMENT_PRECISION_EXC)*100 / 1"`
RECALL_EXC_IMPROVEMENT_TCOMMENT=`bc -l <<< "scale=0; ($JDOCTOR_RECALL_EXC-$TCOMMENT_RECALL_EXC)*100 / 1"`

# Improvement over Toradocu
TORADOCU_PRECISION_EXC=`fgrep $TORADOCU "$RESULTS_TABLE" | cut -d '&' -f 8 | xargs`
TORADOCU_RECALL_EXC=`fgrep $TORADOCU "$RESULTS_TABLE" | cut -d '&' -f 9 | xargs`
PRECISION_EXC_IMPROVEMENT_TORADOCU=`bc -l <<< "scale=0; ($JDOCTOR_PRECISION_EXC-$TORADOCU_PRECISION_EXC)*100 / 1"`
RECALL_EXC_IMPROVEMENT_TORADOCU=`bc -l <<< "scale=0; ($JDOCTOR_RECALL_EXC-$TORADOCU_RECALL_EXC)*100 / 1"`
echo '\newcommand{\precisionImprovementExcToradocu}{'$PRECISION_EXC_IMPROVEMENT_TORADOCU'\%\xspace}' >> "$MACROS"
echo '\newcommand{\recallImprovementExcToradocu}{'$RECALL_EXC_IMPROVEMENT_TORADOCU'\%\xspace}' >> "$MACROS"

echo "Created macros: $MACROS"
