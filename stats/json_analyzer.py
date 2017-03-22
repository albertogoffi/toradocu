import sys
import json

# This script takes a JSON file in the Toradocu format,
# and prints the number of pre-, post-, and exceptional
# postconditions on the standard output.

# Check command line arguments.
if len(sys.argv) != 2:
    print("This script must be invoked with the JSON file to analyze as parameter.")
    sys.exit(1)

with open(sys.argv[1], 'r') as goal_file:
	data = json.load(goal_file)

# Extract the number of the pre-/post-/exceptional post- conditions.
pre = 0
post = 0
excPost = 0
for method in data:
	if "paramTags" in method:
		for paramTag in method["paramTags"]:
			if paramTag["condition"] != "":
				pre = pre + 1
	if "throwsTags" in method:
		for throwsTag in method["throwsTags"]:
			if throwsTag["condition"] != "":
				excPost = excPost + 1
	if "returnTag" in method:
		if method["returnTag"]["condition"] != "":
			post = post + 1

print "PRE {}".format(pre)
print "POST {}".format(post)
print "EXC {}".format(excPost)
