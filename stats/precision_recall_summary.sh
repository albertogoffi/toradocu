# This script produces precision/recall stats.
# It must be invoked with a parameter with value "current" or "v01" or "tcomment".
# "current": statistics about the current Toradocu version.
# "v01": statistics about the current Toradocu version 0.1.
# "tcomment": statistics using @tComment as translation engine.

# Parse command line argument and set variables
if [ $# -ge 1 ]; then
    if [ "$1" = "current" ]; then
	COMMAND='./gradlew test --tests "org.toradocu.PrecisionRecall*"'
	STATS_FILE=results.csv
	STATS_FILE_TO_SAVE=results_current.csv
    elif [ "$1" = "v01" ]; then
	COMMAND='./gradlew precisionRecallV01 --rerun-tasks'
	STATS_FILE=results.csv
	STATS_FILE_TO_SAVE=results_v01.csv
    elif [ "$1" = "tcomment" ]; then
	COMMAND='./gradlew -Dorg.toradocu.translator=tcomment test --tests "org.toradocu.PrecisionRecall*"'
	STATS_FILE=tcomment_results.csv
	STATS_FILE_TO_SAVE=results_tcomment.csv
    fi
else
    echo 'Script must be invoked with one parameter: either "current" or "v01" or "tcomment"'
    exit 1
fi

if [ -f $STATS_FILE ]; then
    rm -i $STATS_FILE # Remove old stats file (if user agrees)
fi

if [ ! -f $STATS_FILE ]; then
        echo "METHOD,DISTANCE THRESHOLD,REMOVAL COST,\
CORRECT THROWS CONDITIONS,WRONG THROWS CONDITIONS,MISSING THROWS CONDITIONS,THROWS PRECISION,THROWS RECALL,\
CORRECT PARAM CONDITIONS,WRONG PARAM CONDITIONS,MISSING PARAM CONDITIONS,PARAM PRECISION,PARAM RECALL,\
CORRECT RETURN CONDITIONS,WRONG RETURN CONDITIONS,MISSING RETURN CONDITIONS,RETURN PRECISION,RETURN RECALL,\
CORRECT CONDITIONS,WRONG CONDITIONS,MISSING CONDITIONS,PRECISION,RECALL" > $STATS_FILE
fi

# Run Toradocu and collect statistics
eval $COMMAND
echo "TOTAL,,,\
=SUM(D1:INDIRECT(\"D\" & ROW()-1)),=SUM(E1:INDIRECT(\"E\" & ROW()-1)),=SUM(F1:INDIRECT(\"F\" & ROW()-1)),,,\
=SUM(I1:INDIRECT(\"I\" & ROW()-1)),=SUM(J1:INDIRECT(\"J\" & ROW()-1)),=SUM(K1:INDIRECT(\"K\" & ROW()-1)),,,\
=SUM(N1:INDIRECT(\"N\" & ROW()-1)),=SUM(O1:INDIRECT(\"O\" & ROW()-1)),=SUM(P1:INDIRECT(\"P\" & ROW()-1)),,,\
=SUM(S1:INDIRECT(\"S\" & ROW()-1)),=SUM(T1:INDIRECT(\"T\" & ROW()-1)),=SUM(U1:INDIRECT(\"U\" & ROW()-1)),," >> $STATS_FILE
echo "NUMBER OF METHODS,=ROW()-3" >> $STATS_FILE
echo "NUMBER OF THROWS CONDITIONS,=INDIRECT(\"D\" & ROW()-2)+INDIRECT(\"E\" & ROW()-2)+INDIRECT(\"F\" & ROW()-2)" >> $STATS_FILE
echo "THROWS PRECISION,=INDIRECT(\"D\" & ROW()-3)/(INDIRECT(\"D\" & ROW()-3) + INDIRECT(\"E\" & ROW()-3))" >> $STATS_FILE
echo "THROWS RECALL,=INDIRECT(\"D\" & ROW()-4)/INDIRECT(\"B\" & ROW()-2)" >> $STATS_FILE
echo "NUMBER OF PARAM CONDITIONS,=INDIRECT(\"I\" & ROW()-5)+INDIRECT(\"J\" & ROW()-5)+INDIRECT(\"K\" & ROW()-5)" >> $STATS_FILE
echo "PARAM PRECISION,=INDIRECT(\"I\" & ROW()-6)/(INDIRECT(\"I\" & ROW()-6) + INDIRECT(\"J\" & ROW()-6))" >> $STATS_FILE
echo "PARAM RECALL,=INDIRECT(\"I\" & ROW()-7)/INDIRECT(\"B\" & ROW()-2)" >> $STATS_FILE
echo "NUMBER OF RETURN CONDITIONS,=INDIRECT(\"N\" & ROW()-8)+INDIRECT(\"O\" & ROW()-8)+INDIRECT(\"P\" & ROW()-8)" >> $STATS_FILE
echo "RETURN PRECISION,=INDIRECT(\"N\" & ROW()-9)/(INDIRECT(\"N\" & ROW()-9) + INDIRECT(\"O\" & ROW()-9))" >> $STATS_FILE
echo "RETURN RECALL,=INDIRECT(\"N\" & ROW()-10)/INDIRECT(\"B\" & ROW()-2)" >> $STATS_FILE
echo "NUMBER OF CONDITIONS,=INDIRECT(\"S\" & ROW()-11)+INDIRECT(\"T\" & ROW()-11)+INDIRECT(\"U\" & ROW()-11)" >> $STATS_FILE
echo "OVERALL PRECISION,=INDIRECT(\"S\" & ROW()-12)/(INDIRECT(\"S\" & ROW()-12) + INDIRECT(\"T\" & ROW()-12))" >> $STATS_FILE
echo "OVERALL RECALL,=INDIRECT(\"S\" & ROW()-13)/INDIRECT(\"B\" & ROW()-2)" >> $STATS_FILE

# Rename file and print final message
mv $STATS_FILE $STATS_FILE_TO_SAVE
echo "Open the result file: $STATS_FILE_TO_SAVE"
