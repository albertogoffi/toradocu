import csv
import sys

# Check command line arguments.
if len(sys.argv) != 3:
    print("""\
This script must be invoked with the following arguments:
1. CSV file to parse;
2. Total number of considered conditions (pre, post, and exceptional post).
Ouptut will be printed on the standard output.
""")
    sys.exit(1)

TOTAL_CONDITIONS = int(sys.argv[2])

COLUMNS = [
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

# Load information from the input CSV file.
with open(sys.argv[1], 'r') as stats_file:
    data = list(csv.DictReader(stats_file))

    results = dict()
    for column in COLUMNS:
        results[column] = 0

    for row in data:
        for column in COLUMNS:
            results[column] += int(row.get(column, 0))

correct_param = results.get('CORRECT PARAM CONDITIONS', 0)
wrong_param = results.get('WRONG PARAM CONDITIONS', 0)
missing_param = results.get('MISSING PARAM CONDITIONS', 0)
correct_throws = results.get('CORRECT THROWS CONDITIONS', 0)
wrong_throws = results.get('WRONG THROWS CONDITIONS', 0)
missing_throws = results.get('MISSING THROWS CONDITIONS', 0)
correct_return = results.get('CORRECT RETURN CONDITIONS', 0)
wrong_return = results.get('WRONG RETURN CONDITIONS', 0)
missing_return = results.get('MISSING RETURN CONDITIONS', 0)

params = (correct_param + wrong_param + missing_param)
returns = (correct_return + wrong_return + missing_return)
throws = (correct_throws + wrong_throws + missing_throws)

param_precision =  0 if correct_param == 0 else float(correct_param) / (correct_param + wrong_param)
param_recall =  0 if correct_param == 0 else float(correct_param) / params
return_precision =  0 if correct_return == 0 else float(correct_return) / (correct_return + wrong_return)
return_recall =  0 if correct_return == 0 else float(correct_return) / returns
throws_precision = 0 if correct_throws == 0 else float(correct_throws) / (correct_throws + wrong_throws)
throws_recall = 0 if correct_throws == 0 else float(correct_throws) / throws

overall_correct = correct_param + correct_return + correct_throws
overall_missing = missing_param + missing_return + missing_throws
overall_wrong = wrong_param + wrong_return + wrong_throws
overall_precision = 0 if overall_correct == 0 else float(overall_correct) / (overall_correct + overall_wrong)
overall_recall = 0 if overall_correct == 0 else float(overall_correct) / TOTAL_CONDITIONS
fmeasure = (2 * overall_precision * overall_recall) / (overall_precision + overall_recall)

output = "{:.2f}".format(param_precision) if (correct_param + wrong_param) > 0 else "n.a." # param_precision
output += " & {:.2f} && ".format(param_recall) # param_recall
output += "{:.2f}".format(return_precision) if (correct_return + wrong_return) > 0 else "n.a." # return_precision
output += " & {:.2f} && ".format(return_recall) # return_recall
output += "{:.2f}".format(throws_precision) if (correct_throws + wrong_throws) > 0 else "n.a." # throws_precision
output += " & {:.2f} && {:.2f} & {:.2f} & {:.2f} \\\\".format(throws_recall, overall_precision, overall_recall, fmeasure) # throws_recall,  overall precision, recall, and fmeasure
print output
