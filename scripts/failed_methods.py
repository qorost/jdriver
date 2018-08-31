# This is to analyze jdriver_methods.log
import sys

def read_log(filename):
    fp = open(filename, "r")
    c = set()
    d = dict()
    for line in fp:
        if "failure:" in line:
            lines = line.split("failure:")
            classname = lines[1][:-2]
            c.add(classname)
            if classname in d:
                d[classname] += 1
            else:
                d[classname] = 1

    fp.close()
    for key in d:
        print key, "", d[key]

    print str(len(c)) + " items in the class set"





if __name__ == "__main__":
    if(len(sys.argv) > 1) :
        filename = sys.argv[1]
        read_log(filename)

