#!/usr/bin/python3

# Parse build/test-results/results.csv, compute summary of test results, and output to standard output.

import os
import sys
import time

RESULTS_CSV_FILE = 'build/test-results/results.csv'

# Check if results.csv exists. Instruct user on how to create it if it doesn't.
if not os.path.isfile(RESULTS_CSV_FILE):
    print('A results.csv file could not be found. Please run the precision recall test suite to '
          'generate this file. For help with running the precision recall test suite, refer to '
          'the developer documentation.')
    sys.exit(1)

# Print modification time of results.csv so user knows how recent tests are.
modification_time = time.ctime(os.path.getmtime(RESULTS_CSV_FILE))
print('==Summary of precision/recall tests run at ' + modification_time + '==')

# Returns the package part of a class name string.
def get_package(class_name):
	for e in enumerate(class_name):
		# Each e is a tuple (index, character).
		if e[1].isupper():
			if (e[0] != 0):
				return class_name[:(e[0] - 1)]
	return class_name

# Takes a list of class names and prints the packages they are in to standard output.
def print_packages(classes):
	print('Packages:')
	seen_packages = set()
	for class_name in classes:
		package = get_package(class_name)
		if package not in seen_packages:
			seen_packages.add(package)
			print(' - ' + package)

# Parse CSV file and output results.
with open(RESULTS_CSV_FILE) as f:
	classes = []
	total_classes = 0
	total_conditions = 0
	avg_precision = 0
	avg_recall = 0
	lines = f.readlines()
	for line in lines:
		(class_name, num_conditions, precision, recall) = line.split(',')
		classes.append(class_name)
		total_classes += 1
		total_conditions += int(num_conditions)
		avg_precision += float(precision)
		avg_recall += float(recall)
	if total_classes == 0:
		avg_precision = 0
		avg_recall = 0
	else:
		avg_precision /= total_classes
		avg_recall /= total_classes
	print_packages(classes)
	print("Total Classes: {0}\nTotal Conditions: {1}\nAverage Precision: {2}\nAverage Recall: {3}"
		  .format(total_classes, total_conditions, avg_precision, avg_recall))
