import os
import pprint
from collections import Counter

try:
    #pip install fuzzywuzzy
    from fuzzywuzzy import process
    #fuzzywuzzy's levenstein distance is much quicker with this C addon:
    #https://github.com/miohtama/python-Levenshtein
except:
    process = None
    
try:
    from termcolor import colored
except: #Couldn't load termcolor, use a regular function instead
    def colored(*args):
        return args[0]

class replikIdentifier(object):
    def __init__(self, verbose=True):
        
        #self.MIN_REPLIK_OCCURANCE = 25
        self.MIN_REPLIK_OCCURANCE = 100
        self.MINWORDS_1GRAM = 1
        self.ACCEPTED_LETTERS = "abcdefghijklmnopqrstuvwxyz' "
        self.DO_FIND_SIMILAR_CHARACTERS = False
        
        self.printLoadedFiles = False
        self.printMerges = False
        self.printPrunedRepliker = False
        
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
            if self.verbose and self.DO_FIND_SIMILAR_CHARACTERS:
                print "Starting to find similar characters"
                self.findSimilarCharacters(self.replik)
                print "Finished finding similar characters"
        else:
            if self.verbose:
                print "Doesn't have fuzzywuzzy.process," + \
                        "won't find similar characters"
            
        self.pruneRepliker(self.replik)
        if self.verbose:
            print ""
            self.printStatistics(self.replik)
                
        if self.verbose == 3:
            print pprint.pprint(self.replik)
            
        if self.verbose:
            print "\nStarting to calculate N-Gram statistics"
            
        self.calculateNGrams(self.replik)
        
    def calculateNGrams(self, replik):
        #NValues = [1, 3]
        #NValues = [1]
        #NValues = [2]
        #NValues = [3]
        NValues = [1,2,3,4,5]
        for n in NValues:
            print colored("%s-Grams:" % n, "cyan")
            self.calculateNGram(replik, n)
                
    def calculateNGram(self, replik, n):
        def removeItemsUnderCount(counter, n):
            smallCountList = filter(lambda item: counter[item] < n,
                    counter)
            for item in smallCountList:
                del counter[item]
                
        nGrams = {}
        
        if n == 1:
            minCount = 15
        elif n == 2:
            minCount = 6
        elif n == 3:
            minCount = 4
        elif n == 4:
            minCount = 3
        elif n >= 5:
            minCount = 2
        
        for name in replik.keys():
            ngramCounter = Counter()
            if self.verbose:
                print colored("%s, %s lines" % \
                    (name, len(replik[name])), "yellow"),
                    
            for line in replik[name]:
                words = line.split(" ")
                for i in range(len(words) - n + 1):
                    ngram = " ".join(words[i:i+n])
                    #ngram = words[i] + " " + words[i+1]
                    ngramCounter[ngram] += 1
                    
            removeItemsUnderCount(ngramCounter, minCount)
            #print "Most common n-grams: ",
            sortedItems = sorted(ngramCounter.items(),
                    key=lambda x: x[1], reverse=True)
            print " ".join(["%s(%s)" % item for item in sortedItems])
            #pprint.pprint(ngramCounter)
            
            nGrams[name] = ngramCounter
            
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
            if self.verbose and self.printMerges:
                print colored("Merging %s -> %s" % (fr, to), "cyan") + ",",
            if to in replik.keys():
                replik[to].extend(replik[fr])
            else:
                replik[to] = replik[fr]
            del replik[fr]
            if self.verbose and self.printMerges:
                print "%s now has %s lines" % (to, len(replik[to]))
            
        if self.verbose and self.printMerges:
            print "Fixing AND-names:"
        #fix "BART AND LISA" -> "BART", "LISA"
        for oldName in replik.keys():
            if " AND " in oldName or "&" in oldName:
                if " AND " in oldName:
                    names = oldName.split(" AND ")
                else:
                    names = oldName.split("&")
                
                for newName in names:
                    newName = newName.strip()
                    if newName in replik.keys():
                        replik[newName].extend(replik[oldName])
                    else:
                        replik[newName] = replik[oldName]
                    
                    if self.verbose and self.printMerges:
                        print colored("Merging %s -> %s," % (oldName, newName),
                                "cyan"),
                        print "%s now has %s lines" % \
                                (newName, len(replik[newName]))
                            
                del replik[oldName]

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
               
                if self.verbose == 2:
                    print s1 + " " + s2
                #open("similar.log", "a").write("%s %s\n" % (s1, s2))
            else:
                #print ""
                pass
        
    def pruneRepliker(self, replik):
        prunedNames = []
        for name in replik.keys():
            if len(replik[name]) < self.MIN_REPLIK_OCCURANCE:
                del replik[name]
                prunedNames.append(name)
        if self.verbose and self.printPrunedRepliker:
            if self.verbose == 2:
                prunedNamesString = ", ".join(prunedNames)
            else: #only show the first few characters
                prunedNamesString = ", ".join(prunedNames[:10]) + "..."
            
            print "Pruned %s names with less than %s lines: %s" % \
                    (len(prunedNames), self.MIN_REPLIK_OCCURANCE,
                            prunedNamesString)
        
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
                
                line = filter(lambda x: x in self.ACCEPTED_LETTERS, line)
                while "  " in line:
                    line = line.replace("  ", " ")
                
                if self.verbose == 3:
                    print "* %s\n  %s" % (oldLine, line)
                
                replik[name] = replik.get(name, []) + [line]
                name = None
                
    def getFileNames(self):
        fileNames = [os.path.join("episodes", fname)
                for fname in os.listdir("episodes")]
        if self.verbose and self.printLoadedFiles:
            print "Loading these %s files: %s" % \
                    (len(fileNames), str(fileNames))
        return fileNames

if __name__ == "__main__":
    ri = replikIdentifier(verbose=2)
