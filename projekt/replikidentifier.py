#encoding: utf-8
import os
import pprint
from collections import Counter
import copy

import ngram

import matplotlib.pyplot as plt

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
    
VERBOSE = 0
PRINT_MERGES = False

class replikIdentifier(object):
    def __init__(self, fileNames, verbose=True):
        
        #self.MIN_REPLIK_OCCURANCE = 25
        self.MIN_REPLIK_OCCURANCE = 100
        self.MINWORDS_1GRAM = 1
        self.DO_FIND_SIMILAR_CHARACTERS = False
        
        self.printLoadedFiles = False
        self.printPrunedRepliker = False
        
        self.verbose = verbose
        self.replik = loadFiles(fileNames)
        
        fixCharacterNames(self.replik)
        
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
                        
        if not self.replik:
            print colored("before pruneRepliker, replik is tom!", "red")
            
        self.pruneRepliker(self.replik)
        
        if not self.replik:
            print colored("self.replik is tom!", "red")
        
        if self.verbose:
            print ""
            self.printStatistics(self.replik)
                
        if self.verbose == 3:
            print pprint.pprint(self.replik)
            
        if self.verbose:
            print "\nStarting to calculate N-Gram statistics"
            
        self.ngramDict = ngram.calculateNGrams(self.replik, self.verbose,
                globalMinCount=0)
        
        #print "ngramDict", ngramDict
        
        #self.identifyString("mr burns is here", ngramDict)
        #self.identifyString("hey dad bet you five bucks you can't eat the whole box", ngramDict)
        
        
    def identifyString(self, s):
        """Takes a string s and returns which name is the most likely
        candidate to say this string.
        
        ex.
        ratios == {
            2:{'BART': 0.1231, 'HOMER': 0.93},
            3:{'MARGE': 0.03}
        }
        """
        ratios = {}
        ngramDict = self.ngramDict
        
        def getClosenessValue(s, n, name):
            print "identifying with name", name,
            occurances = 0
            for stringNgram in ngram.generateNGramsForLine(s, n):
                #print "ngram: '%s'. Count in %s: %s" % \
                #        (stringNgram, name, ngramDict[n][name][stringNgram])
                        
                occurances += ngramDict[n][name][stringNgram]
            numNGrams = sum(ngramDict[n][name].values())
            
            if numNGrams:
                ratio = occurances / float(numNGrams)
            else:
                ratio = 0.0
                
            ratio *= 1000
                
            #print "Ratio: %s ----" % ratio
            return ratio
        
        print "Trying to identify '%s'" % s
        #counter = Counter()
        for n in ngramDict.keys():
            ratios[n] = {}
            for name in ngramDict[n]:
                ratio = getClosenessValue(s, n, name)
                print ratio
                ratios[n][name] = ratio
        return ratios
                
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
    #}}}
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

def addReplikerToDict(lines, replik, verbose=VERBOSE):
    """Takes a number of raw lines, parses them and
    adds them to a dictionary replik"""
    ACCEPTED_LETTERS = "abcdefghijklmnopqrstuvwxyz' "
    name = None
    for i, line in enumerate(lines):
        if line.isupper() and not name:
            name = line
        elif name:
            oldLine = line.strip()
            line = oldLine.lower()
            if line.startswith("(") and ")" in line:
                line = line[line.find(")")+1:].strip()
            
            line = filter(lambda x: x in ACCEPTED_LETTERS, line)
            while "  " in line:
                line = line.replace("  ", " ")
                
            if verbose == 3:
                print "* %s\n  %s" % (oldLine, line)
            
            replik[name] = replik.get(name, []) + [line]
            name = None
            
def fixCharacterNames(replik, verbose=VERBOSE, printMerges=PRINT_MERGES):
    """in-place change of replik!"""
    
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
        if verbose and printMerges:
            print colored("Merging %s -> %s" % (fr, to), "cyan") + ",",
        if fr in replik.keys():
            if to in replik.keys():
                replik[to].extend(replik[fr])
            else:
                replik[to] = replik[fr]
            del replik[fr]
            if verbose and printMerges:
                print "%s now has %s lines" % (to, len(replik[to]))
        else:
            #print "Couldn't find %s in fr" % fr
            pass
        
    if verbose and printMerges:
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
                
                if verbose and printMerges:
                    print colored("Merging %s -> %s," % (oldName, newName),
                            "cyan"),
                    print "%s now has %s lines" % \
                            (newName, len(replik[newName]))
                        
            del replik[oldName]

                
def loadFiles(fileNames):
    replik = {}
    for fileName in fileNames:
        with open(fileName) as f:
            lines = f.read().split("\n")
            addReplikerToDict(lines, replik)
    return replik
        
                
def getFileNames(folderName, verbose):
    fileNames = []
    for fname in os.listdir(folderName):
        fileNames.append(os.path.join(folderName, fname))
    
    if verbose:
        print "Getting these %s files: %s" % \
                (len(fileNames), str(fileNames))
    return fileNames

if __name__ == "__main__":
    verbose = 1
    fileNames = getFileNames("episodes", verbose)
    
    asdf = []
    for i, fname in enumerate(fileNames):
        newFileNames = copy.copy(fileNames)
        validationFile = newFileNames.pop(i)
        
        ri = replikIdentifier(newFileNames, verbose=0)
        
        #stringToIdentify = "mr burns is here"
        stringToIdentify = "hey dad bet you five bucks you can't eat the whole box"
        ratios = ri.identifyString(stringToIdentify)
        asdf.append(ratios[2]["BART"])
        
    xx = range(len(asdf))
    #plt.title("Identifiering av strangen '%s' for karaktaren 'BART'" % stringToIdentify)
    plt.title("Identifiering av strang for BART over olika 15 avsnitt")
    plt.plot(xx, asdf)
    plt.show()
