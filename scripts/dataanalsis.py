import csv

path = "/home/huang/Desktop/Project/Fuzz/jdriver/tests/graph.csv"
reader = csv.reader(open(path, "r"))
d = {}
mydata = dict((rows[0],(rows[1:])) for rows in reader)

print mydata.keys()