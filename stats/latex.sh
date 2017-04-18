#!/bin/bash

# This script takes no input and produces latex tables reporting subjects and precision/recall values.
# Generated tables are saved in the path indicated by variable $SUBJECTS_TABLE.

OUTPUT_DIR=latex
SUBJECTS_TABLE="$OUTPUT_DIR"/subject-classes-table.tex
RESULTS_TABLE="$OUTPUT_DIR"/accuracy-table.tex
MACROS="$OUTPUT_DIR"/macros.tex

numberOfClasses() {
    echo $(find "$1" -name "*.java" -type f | wc -l | tr -d " ")
}

numberOfAnalyzedClasses() {
    echo $(fgrep -c "@Test" "$1")
}

numberOfMethods() {
    # 1st arg is the path of the test suite from which derive the target class.
    # 2nd arg is the jar containing the target class.
    local count=0
    for class in `fgrep "test(\"" $1 | cut -d '"' -f 2`; do
	count=$((count + $(java -cp "$2":build/classes/main org.toradocu.util.ExecutableMembers $class)))
    done
    echo $count
}

numberOfAnalyzedMethods() {
    echo $(egrep -c "^\"$1" results_current.csv)
}

numberOfAnalyzedComments() {
    # 1st arg is either "PRE" or "POST" or "EXC_POST".
    # 2nd arg is the path to the folder containing the goal files.
    local count=0
    for goalFile in "$2"/*.json; do
	count=$((count + $(python "stats/json_analyzer.py" "$goalFile" | fgrep $1 | cut -d ' ' -f 2)))
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

# Create output dir
mkdir -p "$OUTPUT_DIR"

echo "Creating subjects table..."

# Collect info for Commons Collections
CLASSES[0]=$(numberOfClasses src/test/resources/src/commons-collections4-4.1-src/src/main/java)
SELECTED_CLASSES[0]=$(numberOfAnalyzedClasses src/test/java/org/toradocu/PrecisionRecallCommonsCollections4.java)
METHODS[0]=$(numberOfMethods src/test/java/org/toradocu/PrecisionRecallCommonsCollections4.java src/test/resources/bin/commons-collections4-4.1.jar)
DOCUMENTED_METHODS[0]=$(numberOfAnalyzedMethods org.apache.commons.collections4)
PRE[0]=$(numberOfAnalyzedComments PRE src/test/resources/goal-output/commons-collections4-4.1)
POST[0]=$(numberOfAnalyzedComments POST src/test/resources/goal-output/commons-collections4-4.1)
EXC_POST[0]=$(numberOfAnalyzedComments EXC src/test/resources/goal-output/commons-collections4-4.1)

# Collect info for Commons Math
CLASSES[1]=$(numberOfClasses src/test/resources/src/commons-math3-3.6.1-src/src/main/java)
SELECTED_CLASSES[1]=$(numberOfAnalyzedClasses src/test/java/org/toradocu/PrecisionRecallCommonsMath3.java)
METHODS[1]=$(numberOfMethods src/test/java/org/toradocu/PrecisionRecallCommonsMath3.java src/test/resources/bin/commons-math3-3.6.1.jar)
DOCUMENTED_METHODS[1]=$(numberOfAnalyzedMethods org.apache.commons.math3)
PRE[1]=$(numberOfAnalyzedComments PRE src/test/resources/goal-output/commons-math3-3.6.1)
POST[1]=$(numberOfAnalyzedComments POST src/test/resources/goal-output/commons-math3-3.6.1)
EXC_POST[1]=$(numberOfAnalyzedComments EXC src/test/resources/goal-output/commons-math3-3.6.1)

# Collect info for FreeCol
# CLASSES[2]=$(numberOfClasses src/test/resources/src/freecol-0.11.6/src/)
# SELECTED_CLASSES[2]=$(numberOfAnalyzedClasses src/test/java/org/toradocu/PrecisionRecallFreeCol.java)
# METHODS[2]=$(numberOfAnalyzedMethods net.sf.freecol)
# PRE[2]=$(numberOfAnalyzedComments PRE src/test/resources/goal-output/freecol-0.11.6)
# POST[2]=$(numberOfAnalyzedComments POST src/test/resources/goal-output/freecol-0.11.6)
# EXC_POST[2]=$(numberOfAnalyzedComments EXC src/test/resources/goal-output/freecol-0.11.6)

# Collect info for Guava
CLASSES[3]=$(numberOfClasses src/test/resources/src/guava-19.0-sources)
SELECTED_CLASSES[3]=$(numberOfAnalyzedClasses src/test/java/org/toradocu/PrecisionRecallGuava19.java)
METHODS[3]=$(numberOfMethods src/test/java/org/toradocu/PrecisionRecallGuava19.java src/test/resources/bin/guava-19.0.jar)
DOCUMENTED_METHODS[3]=$(numberOfAnalyzedMethods com.google.common)
PRE[3]=$(numberOfAnalyzedComments PRE src/test/resources/goal-output/guava-19.0)
POST[3]=$(numberOfAnalyzedComments POST src/test/resources/goal-output/guava-19.0)
EXC_POST[3]=$(numberOfAnalyzedComments EXC src/test/resources/goal-output/guava-19.0)

# Collect info for JGraphT
CLASSES[4]=$(numberOfClasses src/test/resources/src/jgrapht-core-0.9.2-sources)
SELECTED_CLASSES[4]=$(numberOfAnalyzedClasses src/test/java/org/toradocu/PrecisionRecallJGraphT.java)
METHODS[4]=$(numberOfMethods src/test/java/org/toradocu/PrecisionRecallJGraphT.java src/test/resources/bin/jgrapht-core-0.9.2.jar)
DOCUMENTED_METHODS[4]=$(numberOfAnalyzedMethods org.jgrapht)
PRE[4]=$(numberOfAnalyzedComments PRE src/test/resources/goal-output/jgrapht-core-0.9.2)
POST[4]=$(numberOfAnalyzedComments POST src/test/resources/goal-output/jgrapht-core-0.9.2)
EXC_POST[4]=$(numberOfAnalyzedComments EXC src/test/resources/goal-output/jgrapht-core-0.9.2)

# Collect info for Plume-lib
CLASSES[5]=$(numberOfClasses src/test/resources/src/plume-lib-1.1.0/java/src)
SELECTED_CLASSES[5]=$(numberOfAnalyzedClasses src/test/java/org/toradocu/PrecisionRecallPlumeLib.java)
METHODS[5]=$(numberOfMethods src/test/java/org/toradocu//PrecisionRecallPlumeLib.java src/test/resources/bin/plume-lib-1.1.0.jar)
DOCUMENTED_METHODS[5]=$(numberOfAnalyzedMethods plume.)
PRE[5]=$(numberOfAnalyzedComments PRE src/test/resources/goal-output/plume-lib-1.1.0)
POST[5]=$(numberOfAnalyzedComments POST src/test/resources/goal-output/plume-lib-1.1.0)
EXC_POST[5]=$(numberOfAnalyzedComments EXC src/test/resources/goal-output/plume-lib-1.1.0)

# Collect info for GraphStream
# CLASSES[6]=$(numberOfClasses src/test/resources/src/gs-core-1.3-sources)
# SELECTED_CLASSES[6]=$(numberOfAnalyzedClasses src/test/java/org/toradocu/PrecisionRecallGraphStream.java)
# METHODS[6]=$(numberOfAnalyzedMethods org.graphstream)
# PRE[6]=$(numberOfAnalyzedComments PRE src/test/resources/goal-output/gs-core-1.3)
# POST[6]=$(numberOfAnalyzedComments POST src/test/resources/goal-output/gs-core-1.3)
# EXC_POST[6]=$(numberOfAnalyzedComments EXC src/test/resources/goal-output/gs-core-1.3)

# Compute totals
TOTAL[0]=$(arraySum CLASSES)
TOTAL[1]=$(arraySum SELECTED_CLASSES)
TOTAL[2]=$(arraySum METHODS)
TOTAL[3]=$(arraySum DOCUMENTED_METHODS)
TOTAL[4]=$(arraySum PRE)
TOTAL[5]=$(arraySum POST)
TOTAL[6]=$(arraySum EXC_POST)

# Create the table
echo 'Commons Collections 4.1 \newline\footnotesize\url{https://commons.apache.org/collections}' \
     '& '${CLASSES[0]}' & '${SELECTED_CLASSES[0]}' & '${METHODS[0]}' & '${DOCUMENTED_METHODS[0]}' & '${PRE[0]}' & '${POST[0]}' & '${EXC_POST[0]}' \\' > "$SUBJECTS_TABLE"
echo 'Commons Math 3.6.1 \newline\footnotesize\url{https://commons.apache.org/math}' \
     '& '${CLASSES[1]}' & '${SELECTED_CLASSES[1]}' & '${METHODS[1]}' & '${DOCUMENTED_METHODS[1]}' &'${PRE[1]}' & '${POST[1]}' & '${EXC_POST[1]}' \\' >> "$SUBJECTS_TABLE"
echo 'Guava 19 \newline\footnotesize\url{http://github.com/google/guava}' \
     '& '${CLASSES[3]}' & '${SELECTED_CLASSES[3]}' & '${METHODS[3]}' & '${DOCUMENTED_METHODS[3]}' & '${PRE[3]}' & '${POST[3]}' & '${EXC_POST[3]}' \\' >> "$SUBJECTS_TABLE"
echo 'JGraphT 0.9.2 \newline\footnotesize\url{http://jgrapht.org}' \
     '& '${CLASSES[4]}' & '${SELECTED_CLASSES[4]}' & '${METHODS[4]}' & '${DOCUMENTED_METHODS[4]}' & '${PRE[4]}' & '${POST[4]}' & '${EXC_POST[4]}' \\' >> "$SUBJECTS_TABLE"
echo 'Plume-lib 1.1 \newline\footnotesize\url{http://mernst.github.io/plume-lib}' \
     '& '${CLASSES[5]}' & '${SELECTED_CLASSES[5]}' & '${METHODS[5]}' & '${DOCUMENTED_METHODS[5]}' & '${PRE[5]}' & '${POST[5]}' & '${EXC_POST[5]}' \\' >> "$SUBJECTS_TABLE"
echo '\midrule' >> "$SUBJECTS_TABLE"
echo 'Total & '${TOTAL[0]}' & '${TOTAL[1]}' & '${TOTAL[2]}' & '${TOTAL[3]}' & '${TOTAL[4]}' & '${TOTAL[5]}' & '${TOTAL[6]}' \\' >> "$SUBJECTS_TABLE"

echo "Created table: $SUBJECTS_TABLE"

# Create results table
echo "Creating results table..."

TAC="tac"
if [ `uname` == "Darwin" ]; then
    TAC="tail -r"
fi

cat results_tcomment.csv | $TAC | tail -n +15 | $TAC > results_tcomment_truncated.csv
echo '@tComment     & '`python stats/results_table.py results_tcomment_truncated.csv` > "$RESULTS_TABLE"
rm results_tcomment_truncated.csv

cat results_toradocu-0.1.csv | $TAC | tail -n +6 | $TAC | tail -n +2 > results_toradocu_truncated.csv
echo '"METHOD","CORRECT THROWS CONDITIONS","WRONG THROWS CONDITIONS","MISSING THROWS CONDITIONS"' > results_toradocu_truncated2.csv
cat results_toradocu_truncated.csv >> results_toradocu_truncated2.csv
echo '\OldToradocu  & '`python stats/results_table.py results_toradocu_truncated2.csv` >> "$RESULTS_TABLE"
rm results_toradocu_truncated.csv results_toradocu_truncated2.csv

cat results_current.csv | $TAC | tail -n +15 | $TAC > results_current_truncated.csv
echo '\ToradocuPlus & '`python stats/results_table.py results_current_truncated.csv` >> "$RESULTS_TABLE"
rm results_current_truncated.csv

echo "Created table: $RESULTS_TABLE"

# Create macros
echo '\newcommand{\tCommentPrecision}{\tdq\xspace}' > "$MACROS"
echo '\newcommand{\tCommentRecall}{\tdq\xspace}' >> "$MACROS"
echo '\newcommand{\tCommentFMeasure}{\tdq\xspace}' >> "$MACROS"
echo '\newcommand{\OldToradocuPrecision}{\tdq\xspace}' >> "$MACROS"
echo '\newcommand{\OldToradocuRecall}{\tdq\xspace}' >> "$MACROS"
echo '\newcommand{\OldToradocuFMeasure}{\tdq\xspace}' >> "$MACROS"
echo '\newcommand{\ToradocuPlusPrecision}{92\%\xspace}' >> "$MACROS"
echo '\newcommand{\ToradocuPlusRecall}{64\%\xspace}' >> "$MACROS"
echo '\newcommand{\ToradocuPlusFMeasure}{75\%\xspace}' >> "$MACROS"
