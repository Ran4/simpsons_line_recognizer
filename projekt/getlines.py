import pprint
with open("simpsonsepisode.txt")as f:
    lines = f.read().split("\n")

name = None
replik = {}
for i, line in enumerate(lines):
    if line.isupper() and not name:
        name = line
    elif name:
        replik[name] = replik.get(name, []) + [line]
        name = None

print pprint.pprint(replik)
