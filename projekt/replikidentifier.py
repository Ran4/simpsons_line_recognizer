import os
import pprint

try:
    #pip install fuzzywuzzy
    from fuzzywuzzy import process
except:
    process = None
    
try:
    from termcolor import colored
except: #Couldn't load termcolor, use a regular function instead
    def colored(*args):
        return args[0]

class replikIdentifier(object):
    def __init__(self, verbose=True):
        
        self.MIN_REPLIK_OCCURANCE = 25
        self.ACCEPTED_LETTERS = "abcdefghijklmnopqrstuvwxyz' "
        
        self.verbose = verbose
        self.replik = {}
        
        fileNames = self.getFileNames()
        for fileName in fileNames:
            #try:
            with open(fileName) as f:
                lines = f.read().split("\n")
                self.addReplikerToDict(lines, self.replik)
            #except:
            #    raise Exception("Couldn't load file %s" % fileName)
            
        self.fixCharacterNames(self.replik)
            
        if process: #helper function, use this manually then change
                    #fixCharacterNames
            self.findSimilarCharacters(self.replik)
            pass
            
        self.pruneRepliker(self.replik)
        if self.verbose:
            print ""
            self.printStatistics(self.replik)
                
        if self.verbose == 2:
            print pprint.pprint(self.replik)
            
    def fixCharacterNames(self, replik):
        fromToList = [
                ("REPORTER #2","REPORTER"),
                ("REPORTER #1","REPORTER"),
                ("GUARDS","GUARD"),
                ("DR NICK","DR. NICK"),
                ("MRS. KREBAPPEL","EDNA KRABAPPEL"),
                ("MRS. KRABAPPEL","EDNA KRABAPPEL"),
                ("EDNA KREBAPPEL","EDNA KRABAPPEL"),
                ("HIBBERT","DR. HIBBERT"),
                ("DR HIBBERT","DR. HIBBERT"),
                ("MS HOOVER","MS. HOOVER"),
                ("MEYER","MEYERS"),
                ("MAN #1","MAN"),
                ("MAN #2","MAN"),
                ("MARTIN PRINCE, SR.","MARTIN PRINCE"),
                ("ADVISOR 3","ADVISOR"),
                ("ADVISOR 2","ADVISOR"),
                ("EMPLOYEES","EMPLOYEE"),
                ("TV ANNOUNCER","ANNOUNCER"),
                ("FISHERMAN 1","FISHERMAN"),
                ("FISHERMAN 2","FISHERMAN"),
                ]
        for fr, to in fromToList:
            print colored("Merging %s -> %s" % (fr, to), "cyan") + ",",
            if to in replik.keys():
                replik[to].extend(replik[fr])
            else:
                replik[to] = replik[fr]
            del replik[fr]
            print "%s now has %s lines" % (to, len(replik[to]))

    def findSimilarCharacters(self, replik):
        #->[('New York Jets', 100), ('New York Giants', 78)]
        
        #open("similar.log", "w").write("")
        for name in replik.keys():
            choices = filter(lambda x: x != name, replik.keys())
            possibleNames = process.extract(name, choices, limit=1)
            ALTERNATIVE, VAL = 0, 1
            if possibleNames and possibleNames[0][VAL] > 90:
                s1 =  "%s: %s" % (name, str(possibleNames))
                s2 = colored("Might be wrong?", "red")
               
                print s1 + " " + s2
                #open("similar.log", "a").write("%s %s\n" % (s1, s2))
            else:
                #print ""
                pass
            
        exit()
        
    def pruneRepliker(self, replik):
        prunedNames = []
        for name in replik.keys():
            if len(replik[name]) < self.MIN_REPLIK_OCCURANCE:
                del replik[name]
                prunedNames.append(name)
        if self.verbose:
            print "Pruned %s names: %s" % \
                    (len(prunedNames), ", ".join(prunedNames))
        
    def printStatistics(self, replik):
        numCharacters = len(replik.keys())
        replikNumbers = map(len, replik.values())
        print "%s unique characters with a total of\n%s repliker" % \
                (numCharacters, sum(replikNumbers)),
        
        avgNumRepliker = sum(replikNumbers) / float(numCharacters)
        
        print "with an average of\n%.1f lines per character loaded" % avgNumRepliker
        
        sortedKeys = sorted(self.replik.keys(),
                key=lambda name: len(self.replik[name]),
                reverse=True)
        
        print "Number of lines per character:"
        print ", ".join(
                "%s %s" % (name, len(self.replik[name])) for name in sortedKeys)

    def addReplikerToDict(self, lines, replik):
        """Takes a number of lines"""
        name = None
        for i, line in enumerate(lines):
            if line.isupper() and not name:
                name = line
            elif name:
                oldLine = line.strip()
                line = oldLine.lower()
                if line.startswith("(") and ")" in line:
                    line = line[line.find(")")+1:].strip()
                
                line = filter(lambda x: x in self.ACCEPTED_LETTERS,line)
                
                if self.verbose == 2:
                    print "* %s\n  %s" % (oldLine, line)
                
                replik[name] = replik.get(name, []) + [line]
                name = None
                
    def getFileNames(self):
        fileNames = [os.path.join("episodes", fname)
                for fname in os.listdir("episodes")]
        print "Loading these %s files: %s" % (len(fileNames), str(fileNames))
        return fileNames

if __name__ == "__main__":
    ri = replikIdentifier(verbose=1)
