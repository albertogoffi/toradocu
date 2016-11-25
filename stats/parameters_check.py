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
	'CORRECT CONDITIONS',
	'WRONG CONDITIONS',
	'MISSING CONDITIONS',
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

    print "DISTANCE THRESHOLD,WORD REMOVAL COST,PRECISION,RECALL"
    for key in sorted(results.keys()):
    	correct = results[key][AGGREGATE.index('CORRECT CONDITIONS')]
    	wrong = results[key][AGGREGATE.index('WRONG CONDITIONS')]
    	missing = results[key][AGGREGATE.index('MISSING CONDITIONS')]

    	ident = ",".join(key)
    	precision = float(correct) / (correct + wrong)
    	recall = float(correct) / (correct + wrong + missing)

    	print "{},{},{}".format(ident, precision, recall)
