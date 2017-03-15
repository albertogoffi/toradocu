import csv
import sys

# Check command line arguments
if len(sys.argv) != 2:
    print("This script must be invoked with the CSV file to analyze as parameter.")
    sys.exit(1)

AGGREGATE_OVER = [
    'DISTANCE THRESHOLD',
    'REMOVAL COST',
]

AGGREGATE = [
    'CORRECT PARAM CONDITIONS',
    'WRONG PARAM CONDITIONS',
    'MISSING PARAM CONDITIONS',
    'CORRECT THROWS CONDITIONS',
    'WRONG THROWS CONDITIONS',
    'MISSING THROWS CONDITIONS',
    'CORRECT RETURN CONDITIONS',
    'WRONG RETURN CONDITIONS',
    'MISSING RETURN CONDITIONS',
]

def extract(keys, d):
    return [d[key] for key in keys]

with open(sys.argv[1], 'r') as stats_file:
    data = list(csv.DictReader(stats_file))

    results = dict()
    for row in data:
    	keys = tuple(extract(AGGREGATE_OVER, row))
    	values = map(float, extract(AGGREGATE, row))
    	counters = results.setdefault(keys, [0] * len(values))
    	results[keys] = map(sum, zip(results[keys], values))

    print('"DISTANCE THRESHOLD","WORD REMOVAL COST","PARAM PRECISION","PARAM RECALL","THROWS PRECISION","THROWS RECALL","RETURN PRECISION","RETURN RECALL","OVERALL PRECISION","OVERALL RECALL"')

    for key in sorted(results.keys()):
        correct_param = results[key][AGGREGATE.index('CORRECT PARAM CONDITIONS')]
        wrong_param = results[key][AGGREGATE.index('WRONG PARAM CONDITIONS')]
        missing_param = results[key][AGGREGATE.index('MISSING PARAM CONDITIONS')]
        correct_throws = results[key][AGGREGATE.index('CORRECT THROWS CONDITIONS')]
        wrong_throws = results[key][AGGREGATE.index('WRONG THROWS CONDITIONS')]
        missing_throws = results[key][AGGREGATE.index('MISSING THROWS CONDITIONS')]
        correct_return = results[key][AGGREGATE.index('CORRECT RETURN CONDITIONS')]
        wrong_return = results[key][AGGREGATE.index('WRONG RETURN CONDITIONS')]
        missing_return = results[key][AGGREGATE.index('MISSING RETURN CONDITIONS')]
        overall_correct = correct_param + correct_throws + correct_return
        overall_wrong = wrong_param + wrong_throws + wrong_return
        overall_missing = missing_param + missing_throws + missing_return

        ident = ",".join(key)
        param_precision = 0 if correct_param == 0 else float(correct_param) / (correct_param + wrong_param)
        param_recall = 0 if correct_param == 0 else float(correct_param) / (correct_param + wrong_param + missing_param)
        throws_precision = 0 if correct_throws == 0 else float(correct_throws) / (correct_throws + wrong_throws)
        throws_recall = 0 if correct_throws == 0 else float(correct_throws) / (correct_throws + wrong_throws + missing_throws)
        return_precision = 0 if correct_return == 0 else float(correct_return) / (correct_return + wrong_return)
        return_recall = 0 if correct_return == 0 else float(correct_return) / (correct_return + wrong_return + missing_return)
        overall_precision = 0 if overall_correct == 0 else float(overall_correct) / (overall_correct + overall_wrong)
        overall_recall = 0 if overall_correct == 0 else float(overall_correct) / (overall_correct + overall_wrong + overall_missing)

	print "{},{},{},{},{},{},{},{},{}".format(ident, param_precision, param_recall, throws_precision, throws_recall, return_precision, return_recall, overall_precision, overall_recall)
