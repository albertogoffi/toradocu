# This script produces precision/recall stats.
# It must be invoked with a parameter with value "current" or "tcomment".
# "current": statistics about the current Toradocu version.
# "tcomment": statistics using @tComment as translation engine.

ERROR_MESSAGE='Script must be invoked with one parameter: either "toradocu" or "toradocu_semantics" or "tcomment"'

# General test suite.
#TESTS='test --tests org.toradocu.accuracy.Accuracy*'
# ISSTA18 Paper test suite (10% classes per project).
TESTS='issta18'

# The following is to run specific tests.
#TESTS_PREFIX="org.toradocu.accuracy.PrecisionRecall"
#TESTS='--tests '$TESTS_PREFIX'CommonsCollections4 --tests '$TESTS_PREFIX'CommonsMath3 --tests '$TESTS_PREFIX'Guava19 --tests '$TESTS_PREFIX'JGraphT --tests '$TESTS_PREFIX'PlumeLib'

# Parse command line argument and set variables
if [ $# -eq 1 ]; then
  if [ "$1" = "toradocu" ]; then
    COMMAND="./gradlew --rerun-tasks -Dorg.toradocu.translator=nosemantics $TESTS"
    STATS_FILE=results_.csv
    STATS_FILE_TO_SAVE=results.csv
  elif [ "$1" = "toradocu_semantics" ]; then
    COMMAND="./gradlew --rerun-tasks $TESTS"
    STATS_FILE=results_semantics_.csv
    STATS_FILE_TO_SAVE=results_semantics.csv
  elif [ "$1" = "tcomment" ]; then
    COMMAND="./gradlew --rerun-tasks -Dorg.toradocu.translator=tcomment $TESTS"
    STATS_FILE=results_tcomment_.csv
    STATS_FILE_TO_SAVE=results_tcomment.csv
  else
	  echo $ERROR_MESSAGE
	  exit 1
	fi
else
  echo $ERROR_MESSAGE
  exit 1
fi

if [ -f $STATS_FILE ]; then
    rm -i $STATS_FILE # Remove old stats file (if user agrees)
fi
if [ -f $STATS_FILE_TO_SAVE ]; then
    rm -i $STATS_FILE_TO_SAVE # Remove old stats file (if user agrees)
fi

if [ ! -f $STATS_FILE ]; then
        echo "METHOD,DISTANCE THRESHOLD,REMOVAL COST,\
CORRECT THROWS CONDITIONS,WRONG THROWS CONDITIONS,UNEXPECTED THROWS CONDITIONS,MISSING THROWS CONDITIONS,\
CORRECT PARAM CONDITIONS,WRONG PARAM CONDITIONS,UNEXPECTED PARAM CONDITIONS,MISSING PARAM CONDITIONS,\
CORRECT RETURN CONDITIONS,WRONG RETURN CONDITIONS,UNEXPECTED RETURN CONDITIONS,MISSING RETURN CONDITIONS" > $STATS_FILE
fi

# Run Toradocu and collect statistics
$COMMAND

echo "TOTAL,,,\
=SUM(D1:INDIRECT(\"D\" & ROW()-1)),=SUM(E1:INDIRECT(\"E\" & ROW()-1)),=SUM(F1:INDIRECT(\"F\" & ROW()-1)),=SUM(G1:INDIRECT(\"G\" & ROW()-1)),\
=SUM(H1:INDIRECT(\"H\" & ROW()-1)),=SUM(I1:INDIRECT(\"I\" & ROW()-1)),=SUM(J1:INDIRECT(\"J\" & ROW()-1)),=SUM(K1:INDIRECT(\"K\" & ROW()-1)),\
=SUM(L1:INDIRECT(\"L\" & ROW()-1)),=SUM(M1:INDIRECT(\"M\" & ROW()-1)),=SUM(N1:INDIRECT(\"N\" & ROW()-1)),=SUM(O1:INDIRECT(\"O\" & ROW()-1))" >> $STATS_FILE
echo "NUMBER OF METHODS,=ROW()-3" >> $STATS_FILE

echo "NUMBER OF THROWS CONDITIONS,=INDIRECT(\"D\" & ROW()-2)+INDIRECT(\"E\" & ROW()-2)+INDIRECT(\"G\" & ROW()-2)" >> $STATS_FILE
echo "THROWS PRECISION,=INDIRECT(\"D\" & ROW()-3)/(INDIRECT(\"D\" & ROW()-3)+INDIRECT(\"E\" & ROW()-3)+INDIRECT(\"F\" & ROW()-3))" >> $STATS_FILE
echo "THROWS RECALL,=INDIRECT(\"D\" & ROW()-4)/(INDIRECT(\"D\" & ROW()-4)+INDIRECT(\"E\" & ROW()-4)+INDIRECT(\"G\" & ROW()-4))" >> $STATS_FILE

echo "NUMBER OF PARAM CONDITIONS,=INDIRECT(\"H\" & ROW()-5)+INDIRECT(\"I\" & ROW()-5)+INDIRECT(\"K\" & ROW()-5)" >> $STATS_FILE
echo "PARAM PRECISION,=INDIRECT(\"H\" & ROW()-6)/(INDIRECT(\"H\" & ROW()-6)+INDIRECT(\"I\" & ROW()-6)+INDIRECT(\"J\" & ROW()-6))" >> $STATS_FILE
echo "PARAM RECALL,=INDIRECT(\"H\" & ROW()-7)/(INDIRECT(\"H\" & ROW()-7)+INDIRECT(\"I\" & ROW()-7)+INDIRECT(\"K\" & ROW()-7))" >> $STATS_FILE

echo "NUMBER OF RETURN CONDITIONS,=INDIRECT(\"L\" & ROW()-8)+INDIRECT(\"M\" & ROW()-8)+INDIRECT(\"O\" & ROW()-8)" >> $STATS_FILE
echo "RETURN PRECISION,=INDIRECT(\"L\" & ROW()-9)/(INDIRECT(\"L\" & ROW()-9)+INDIRECT(\"M\" & ROW()-9)+INDIRECT(\"N\" & ROW()-9))" >> $STATS_FILE
echo "RETURN RECALL,=INDIRECT(\"L\" & ROW()-10)/(INDIRECT(\"L\" & ROW()-10)+INDIRECT(\"M\" & ROW()-10)+INDIRECT(\"O\" & ROW()-10))" >> $STATS_FILE

echo "NUMBER OF CONDITIONS,=INDIRECT(\"B\" & ROW()-9)+INDIRECT(\"B\" & ROW()-6)+INDIRECT(\"B\" & ROW()-3)" >> $STATS_FILE

CORRECT_TRANSLATIONS="(INDIRECT(\"D\" & ROW()-12)+INDIRECT(\"H\" & ROW()-12)+INDIRECT(\"L\" & ROW()-12))"
WRONG_TRANSLATIONS="(INDIRECT(\"E\" & ROW()-12)+INDIRECT(\"I\" & ROW()-12)+INDIRECT(\"M\" & ROW()-12))"
UNEXPECTED_TRANSLATIONS="(INDIRECT(\"F\" & ROW()-12)+INDIRECT(\"J\" & ROW()-12)+INDIRECT(\"N\" & ROW()-12))"
echo "OVERALL PRECISION,=$CORRECT_TRANSLATIONS/($CORRECT_TRANSLATIONS+$WRONG_TRANSLATIONS+$UNEXPECTED_TRANSLATIONS)" >> $STATS_FILE

CORRECT_TRANSLATIONS="(INDIRECT(\"D\" & ROW()-13)+INDIRECT(\"H\" & ROW()-13)+INDIRECT(\"L\" & ROW()-13))"
WRONG_TRANSLATIONS="(INDIRECT(\"E\" & ROW()-13)+INDIRECT(\"I\" & ROW()-13)+INDIRECT(\"M\" & ROW()-13))"
MISSING_TRANSLATIONS="(INDIRECT(\"G\" & ROW()-13)+INDIRECT(\"K\" & ROW()-13)+INDIRECT(\"O\" & ROW()-13))"
echo "OVERALL RECALL,=$CORRECT_TRANSLATIONS/($CORRECT_TRANSLATIONS+$WRONG_TRANSLATIONS+$MISSING_TRANSLATIONS)" >> $STATS_FILE

# Rename file and print final message
mv $STATS_FILE $STATS_FILE_TO_SAVE
echo "Open the result file: $STATS_FILE_TO_SAVE"
