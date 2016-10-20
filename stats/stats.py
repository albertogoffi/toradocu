#!/usr/bin/python

import csv
import sys

# Check command line arguments
if len(sys.argv) != 2:
    print("This script must be invoked with the CSV file to analyze as parameter.")
    sys.exit(1)

with open(sys.argv[1], 'r') as stats_file:
    data = list(csv.DictReader(stats_file))

    # Find max distance threshold in the stat file
    MAX_DISTANCE_THRESHOLD = 0
    for row in data:
        distance_threshold = int(row['DISTANCE THRESHOLD'])
        if distance_threshold > MAX_DISTANCE_THRESHOLD:
            MAX_DISTANCE_THRESHOLD = distance_threshold

    # Initialize constants and data structures
    MAX_WORD_REMOVAL_COST = MAX_DISTANCE_THRESHOLD + 1
    count = [[0] * (MAX_WORD_REMOVAL_COST + 1) for x in range(0, MAX_DISTANCE_THRESHOLD + 1)]
    averagePrecision = [[0] * (MAX_WORD_REMOVAL_COST + 1) for x in range(0, MAX_DISTANCE_THRESHOLD + 1)]
    averageRecall = [[0] * (MAX_WORD_REMOVAL_COST + 1) for x in range(0, MAX_DISTANCE_THRESHOLD + 1)]
    totalPrecision = [[0] * (MAX_WORD_REMOVAL_COST + 1) for x in range(0, MAX_DISTANCE_THRESHOLD + 1)]
    totalRecall = [[0] * (MAX_WORD_REMOVAL_COST + 1) for x in range(0, MAX_DISTANCE_THRESHOLD + 1)]

    # Collect data from CSV file created by Toradocu
    for row in data:
        distanceThreshold = int(row['DISTANCE THRESHOLD'])
        removalCost = int(row['REMOVAL COST'])
        totalPrecision[distanceThreshold][removalCost] = totalPrecision[distanceThreshold][removalCost] + float(row['PRECISION'])
        totalRecall[distanceThreshold][removalCost] = totalRecall[distanceThreshold][removalCost] + float(row['RECALL'])
        count[distanceThreshold][removalCost] = count[distanceThreshold][removalCost] + 1

    # Compute average precision and recall for each param configuration
    for i in range(0, MAX_DISTANCE_THRESHOLD + 1):
        for j in range(0, MAX_WORD_REMOVAL_COST + 1):
            if count[i][j] != 0:
                averagePrecision[i][j] = totalPrecision[i][j] / count[i][j]
                averageRecall[i][j] = totalRecall[i][j] / count[i][j]

    print("Average precision (rows are distance threshold values, columns are word removal cost values)")
    for row in averagePrecision:
        print(str(row)[1:-1]) # Print without square brackets
    print("\nAverage recall (rows are distance threshold values, columns are word removal cost values)")
    for row in averageRecall:
        print(str(row)[1:-1]) # Print without square brackets
