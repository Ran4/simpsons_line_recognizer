import random
lines = open("test1.dat").read().split("\n")[:-1]
nl = []
for i in range(50):
    n = 0
    for line in lines:
        vals = line.split("|")[1:]
        x = random.randint(0,3)
        if x == int(vals[-1]):
            print vals[x], "CORRECT"
            n += 1
        else:
            print vals[x], "INCORRECT"
        
    print "numCorrect %s/%s" % (n, len(lines)-1)
    nl.append(n)
    
avg = sum(nl) / float(len(nl))
print "avg:", avg , (len(lines) - 1.0)
print "avg:", avg / (len(lines) - 1.0)
