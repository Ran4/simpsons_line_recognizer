#!/usr/bin/env python
#encoding: utf-8
import os
import pprint
from collections import Counter
import copy
import time
import sys
import random
from operator import itemgetter

import ngram
import julgran

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

class replikIdentifier(object): #{{{
    def __init__(self, replik, verbose=VERBOSE, minReplikOccurance=100,
            NValues=[2], scoreFunction=None):
        
        self.minReplikOccurance = minReplikOccurance
        self.MINWORDS_1GRAM = 1
        self.DO_FIND_SIMILAR_CHARACTERS = False
        
        self.printLoadedFiles = False
        self.printPrunedRepliker = False
        
        self.verbose = verbose

        self.replik = replik
        
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

        # self.pruneRepliker(self.replik)
        
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
                globalMinCount=0, NValues=NValues,
                scoreFunction=scoreFunction)
        
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
            """Takes a string of a line s and returns
            how close it is to character >name<
            """
            occurances = 0
            for stringNgram in ngram.generateNGramsForLine(s, n):
                occurances += ngramDict[n][name][stringNgram]
            numNGrams = sum(ngramDict[n][name].values())
            
            if numNGrams:
                ratio = occurances / float(numNGrams)
            else:
                ratio = 0.0
                
            ratio *= 1000
            return ratio
        
        #print "Trying to identify '%s'" % s
        #counter = Counter()
        for n in ngramDict.keys():
            ratios[n] = {}
            for name in ngramDict[n]:
                ratio = getClosenessValue(s, n, name)
                #print ratio
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
            if len(replik[name]) < self.minReplikOccurance:
                del replik[name]
                prunedNames.append(name)
        if self.verbose and self.printPrunedRepliker:
            if self.verbose == 2:
                prunedNamesString = ", ".join(prunedNames)
            else: #only show the first few characters
                prunedNamesString = ", ".join(prunedNames[:10]) + "..."
            
            print "Pruned %s names with less than %s lines: %s" % \
                    (len(prunedNames), self.minReplikOccurance,
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
            
def fixCharacterNames(repl, verbose=VERBOSE, printMerges=PRINT_MERGES):
    replik = copy.copy(repl)
    
    fromToList = [
            ("REPORTER #2","REPORTER"), #{{{
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
            ("FISHERMAN 2","FISHERMAN"), #}}}
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
            
    return replik

                
def loadFiles(fileNames):
    """Takes a list of fileNames and loads repliker (lines) from this"""
    replik = {}
    for fileName in fileNames:
        with open(fileName) as f:
            lines = f.read().split("\n")
            addReplikerToDict(lines, replik)
    return replik
        
                
def getFileNames(folderName, verbose=VERBOSE):
    """Returns a list of filenames in the folder folderName """
    fileNames = []
    for fname in os.listdir(folderName):
        fileNames.append(os.path.join(folderName, fname))
    
    if verbose:
        print "Getting these %s files: %s" % \
                (len(fileNames), str(fileNames))
    return fileNames

"""
def bartControl(): #{{{
    import matplotlib.pyplot as plt
    verbose = 1
    fileNames = getFileNames("episodes", verbose)
    
    asdf = []
    for i, fname in enumerate(fileNames):
        newFileNames = copy.copy(fileNames)
        validationFile = newFileNames.pop(i)
        
        ri = replikIdentifier(newFileNames, verbose=0, minReplikOccurance=100)
        
        #stringToIdentify = "mr burns is here"
        stringToIdentify = "hey dad bet you five bucks you can't eat the whole box"
        ratios = ri.identifyString(stringToIdentify)
        asdf.append(ratios[2]["BART"])
        
    xx = range(len(asdf))
    #plt.title("Identifiering av strangen '%s' for karaktaren 'BART'" % stringToIdentify)
    plt.title("Identifiering av strang for BART over olika 15 avsnitt")
    plt.plot(xx, asdf)
    plt.show() #}}}
"""
#def validateAllLines(): #{{{
#    def getKeyWithBiggestValue(d):
#        keyWithBiggestValue = None
#        for key in d:
#            if keyWithBiggestValue is None or d[key] > d[keyWithBiggestValue]:
#                keyWithBiggestValue = key
#        return keyWithBiggestValue
#
#    def getMostLikelySpeakerPerNGram(ri, name, line):
#        """Takes a ri object and returns a dictionary of n values, with
#        each value holding a tuple being the most likely speaker and
#        a value representing how good the match was.
#
#        E.g. {2: ("HOMER", 0.314)}"""
#        ratios = ri.identifyString(line)
#        bestMatches = {}
#
#        for n in ri.ngramDict.keys():
#            bestNameMatch = getKeyWithBiggestValue(ratios[n])
#            bestMatches[n] = (bestNameMatch, ratios[n][bestNameMatch])
#
#        return bestMatches
#
#    def checkLinesForCharacter(ri, validationRI, name, n):
#        numCorrect = 0
#        for line in validationRI.replik[name]:
#            ngramsForLine = ngram.generateNGramsForLine(line, n)
#
#            bestNameMatchCount = Counter()
#
#            for ngramForLine in ngramsForLine:
#                bestMatches = getMostLikelySpeakerPerNGram(
#                        ri, name, ngramForLine)
#
#                for n in NValues:
#                    bestNameMatch, value = bestMatches[n]
#                    bestNameMatchCount[bestNameMatch] += 1
#
#            if not bestNameMatchCount:
#                print "No guess found for line '%s'" % line
#                continue
#
#            finalGuess, count = bestNameMatchCount.most_common(1)[0]
#            correctRatio = count / float(sum(bestNameMatchCount.values()))
#            isCorrect = finalGuess == name
#            if isCorrect:
#                numCorrect += 1
#
#            print colored("%s: '%s' (%.2f for %s) %s" % \
#                    (name, line, correctRatio, finalGuess,
#                        "" if isCorrect else "FAIL"),
#                    "green" if isCorrect else "red")
#
#    verbose = 1
#    NValues = [2]
#    fileNames = getFileNames("episodes", verbose)
#    for i, fname in enumerate(fileNames):
#        if i != 4: #TODO: later, check for all files
#            continue
#
#        print colored("Checking file %s" % fname, "cyan")
#
#        newFileNames = copy.copy(fileNames)
#        validationFile = newFileNames.pop(i)
#
#        print "Creating ri"
#        ri = replikIdentifier(newFileNames, verbose=0, minReplikOccurance=100,
#                NValues=NValues)
#        print "Creating validationRI"
#        validationRI = replikIdentifier([validationFile], verbose=0,
#                minReplikOccurance=5, NValues=NValues)
#
#        n = 2
#        for name in validationRI.ngramDict[n].keys():
#            print colored("Checking all lines by %s" % name, "cyan")
#            checkLinesForCharacter(ri, validationRI, name, n)
#}}}

def getMainChars(repliker, amount=5):
    nameAndNumLines = [(name, len(lines)) for (name, lines) in repliker.items()]
    
    return map(lambda x: x[0],
               sorted(nameAndNumLines,
                      key=lambda x: x[1],
                      reverse=True))[:amount]
    
def mainCharPruner(repliker, amount=5, mainChars=None, preserveOthers=False):
    mainChars = mainChars or getMainChars(repliker, amount)
    if preserveOthers == True:
        preserveOthers = filter(lambda x: x not in mainChars, repliker.keys())
    elif type(preserveOthers) is int:
        # KLUDGE
        preserveOthers = filter(lambda x: x not in mainChars, getMainChars(repliker, len(mainChars) + preserveOthers))
    elif type(preserveOthers) is list:
        pass
    elif type(preserveOthers) is bool:
        pass
    else:
        raise exception("preserveOthers should be bool int or list")
    
    repl = copy.copy(repliker)
    if preserveOthers:
        others = []
    for key in repl.keys():
        if key not in mainChars:
            if preserveOthers and key in preserveOthers:
                others += repl[key]
            repl.pop(key)
    if preserveOthers:
        repl["OTHER"] = others
    return repl

def dict_add(adict, bdict):
    """adds bdict to adict (where keys match) and stores the result in adict"""
    for key in set(adict.keys() + bdict.keys()):
        adict[key] += bdict[key]
    
def crossValidation(n=2, randomGuess=False, amount=5, verbose=True,
                    preserveOthers=False, scoreFunction=None, use_float_confusion_matrix=False):
    fileNames = getFileNames("episodes")

    repliker = fixCharacterNames(loadFiles(fileNames))
    mainChars = getMainChars(repliker, amount)  # need to determine who is main chars from all the data
    repliker = mainCharPruner(repliker, mainChars=mainChars)

    # if preserveOthers:
    #     preserverOthersCount = preserveOthers if type(preserveOthers) is int else 30
    #     preserveOthersList = getMainChars(repliker, amount+30)[amount:]
    # else:
    #     preserveOthersList = []

    iterMainChars = mainChars if not preserveOthers else mainChars + ["OTHER"]

    confusion_matrix = dict(zip(iterMainChars,
                                [dict(zip(iterMainChars,
                                          [0.0 if use_float_confusion_matrix else 0]*len(iterMainChars)))
                                 for _ in range(len(iterMainChars))]))
    correct_guesses = 0
    incorrect_guesses = 0
    for i, fname in enumerate(fileNames):
        if verbose:
            print "#########################%s, %s#########################" % (i, fname)
            
        
        newFileNames = copy.copy(fileNames)
        validationFile = newFileNames.pop(i)

        trainingSet = mainCharPruner(fixCharacterNames(loadFiles(newFileNames)), mainChars=mainChars, preserveOthers=preserveOthers)
        validationSet = mainCharPruner(fixCharacterNames(loadFiles([validationFile])), mainChars=mainChars, preserveOthers=preserveOthers)
        
        ri = replikIdentifier(trainingSet, NValues=[n],
                scoreFunction=scoreFunction)
        
        # here check how correctly ri can identify the characters' lines in validationSet
        for (name, lines) in validationSet.items():
            if verbose:
                print "---Name: ", name
            for line in lines:
                if not randomGuess:
                    slh = ri.identifyString(line)[n].items()
                    guess = max(slh, key=lambda x: x[1])[0]
                    if use_float_confusion_matrix: dict_add(confusion_matrix_float[name], slh) # modifies in place
                else:
                    guess = random.choice(iterMainChars)
                correct = guess == name
                if not use_float_confusion_matrix: confusion_matrix[name][guess] += 1
                if correct:
                    correct_guesses += 1
                else:
                    incorrect_guesses += 1
                    
                if verbose:
                    print colored("%s: %s" % (guess, line), "green" if correct else "red")

    if randomGuess:
        print colored("(random guessing)", "cyan")
        
    print "Correct guesses: %s (%.1f%%), incorrect guesses: %s" % \
        (colored(str(correct_guesses), "green"),
         100*correct_guesses/float(correct_guesses+incorrect_guesses),
         colored(str(incorrect_guesses), "red"))

    print "Rows: Correct name, Columns: Guessed name"
    nameLen = max(map(len, iterMainChars))
    sys.stdout.write(" "*(nameLen+1))
    for char in iterMainChars:
        sys.stdout.write(("%"+str(nameLen)+"s ") % char)
    sys.stdout.write("\n")
    for ci in iterMainChars:
        sys.stdout.write(("%"+str(nameLen)+"s ") % ci)
        for cj in iterMainChars:
            sys.stdout.write(("%"+str(nameLen)+(".2f" if use_float_confusion_matrix else "d ")) % confusion_matrix[ci][cj])
        sys.stdout.write("\n")

    fullLen = max(nameLen, len("Precision")) + 2
    print
    print " "*(nameLen+3) + "Precision".ljust(fullLen) + \
            "Recall".ljust(fullLen) + \
            "F1Score".ljust(fullLen)
    for precisionItem, recallItem, F1ScoreItem in zip(*map(lambda x: x.items(),
                                                           [calculateRowWisePrecision(confusion_matrix),
                                                            calculateRowWiseRecall(confusion_matrix),
                                                            calculateRowWiseF1Score(confusion_matrix)])):
            prec, rec, f1 = map(lambda x: ("%.3f" % x[1]).ljust(fullLen),
                    [precisionItem, recallItem, F1ScoreItem])
            
            print (precisionItem[0] + ":").ljust(nameLen + 2),
            print "%s%s%s" % (prec, rec, f1)

    return confusion_matrix

def calculateRowWiseF1Score(confusion_matrix): #{{{
    F1scores = {}
    recalls = calculateRowWiseRecall(confusion_matrix)
    precisions = calculateRowWisePrecision(confusion_matrix)
    for char in confusion_matrix.keys():
        F1scores[char] = 2*(precisions[char]*recalls[char])/(precisions[char] + recalls[char])
    return F1scores

def calculateRowWiseSomething(confusion_matrix):
    """Here goes confucian_matrix"""
    asdf = {}
    for (name, row) in confusion_matrix.items():
        correct = row[name]
        incorrect = sum([item[1] for item in row.items() if item[0] != name])
        asdf[name] = float(correct)/float(incorrect)
    return asdf

def calculateRowWisePrecision(confusion_matrix):
    """Here goes confucian_matrix"""
    precisions = {}
    for (name, row) in confusion_matrix.items():
        true_positives  = row[name]
        # print name, [r[name] for (n, r) in confusion_matrix.items() if n != name]
        false_positives = sum([r[name] for (n, r) in confusion_matrix.items() if n != name]) # column-wise sum for column {name} with 0:ed diagonal
        precisions[name] = float(true_positives)/float(true_positives + false_positives)
    return precisions


def calculateRowWiseRecall(confusion_matrix):
    """Here goes confucian_matrix"""
    recalls = {}
    for (name, row) in confusion_matrix.items():
        true_positives  = row[name]
        false_negatives = sum([r for (n, r) in row.items() if n != name]) # row-wise sum with 0:ed diagonal
        recalls[name] = float(true_positives)/float(true_positives + false_negatives)
    return recalls #}}}
        
def repl(): #{{{
    fileNames = getFileNames("episodes")
    amount = 5
    repliker = loadFiles(fileNames)
    
    n = 2
    
    corpus = mainCharPruner(fixCharacterNames(repliker), amount=amount)
    ri = replikIdentifier(corpus, NValues=[n])
    
    def intOrNone(s):
        try:
            return int(s)
        except:
            return None
        
    print "Enter a line. We'll try to figure out what character",
    print "is most likely to say your entered line."
    print "Type a number to change the number",
    print "of characters to search for (default %s)." % amount
    print "Type quit or exit to quit"
    
    while True:
        inLine = raw_input("-> ")
        if not inLine:
            continue
        
        if inLine.lower() in ["q", "quit", ":q", "q!", "exit"]:
            return
        
        newAmount = intOrNone(inLine)
        if newAmount is not None:
            amount = newAmount
            corpus = mainCharPruner(
                    fixCharacterNames(repliker), amount=amount)
            ri = replikIdentifier(corpus, NValues=[n])
            print "Now checking the top %s characters" % newAmount
            continue
     
        ratios = ri.identifyString(inLine.lower())[n]
        
        """ratios == {
            2:{'BART': 0.1231, 'HOMER': 0.93},
            3:{'MARGE': 0.03}
        } """
        sortedRatios = sorted(ratios.items(), key=lambda x: x[1], reverse=True)
        sortedRatios = filter(lambda x: x[1] > 0.0, sortedRatios)
        #import pdb; pdb.set_trace()
        
        if sortedRatios:
            format = lambda item: "%s (%.1f)" % (item[0].title(), item[1])
            print ", ".join(map(format, sortedRatios))
        else:
            msg = "No matches found, but here's a JULGRAN!"
            julgran.printJulgran(amount, random.choice([True, False]), msg)
#}}}
#python replikIdentifier.py -i
    
if __name__ == "__main__":
    if "-i" in sys.argv[1:]:
        repl()
    elif "validate" in sys.argv[1:] or "--validate" in sys.argv[1:]:
        # fileNames = getFileNames("episodes")
        #bartControl()
        # validateAllLines()
        
        for scoreFunction in [None, ngram.rescoreNGrams]:
            print colored("-"*50, "yellow")
            print colored(("with" if scoreFunction else "without") + \
                        " score function!", "yellow")
            print colored("-"*50, "yellow")
        
            crossValidation(n=2, amount=5, randomGuess=False, verbose=False,
                    preserveOthers=False, scoreFunction=scoreFunction)

            ngram.loadNgramStopList("combined_stoplist.txt")
            
            #print "Stoplist with length:", len(ngram.ngramStopList), ngram.ngramStopList
            print colored("\nUsing stoplist with length:", "cyan"),
            print colored(str(len(ngram.ngramStopList)), "cyan")
            #print str(ngram.ngramStopList[:4])[1:-1] + "..."
            crossValidation(n=2, amount=5, randomGuess=False, verbose=False,
                    preserveOthers=False, scoreFunction=scoreFunction)
            
            ngram.noStopList()
    else:
        print "usage: one of"
        print "%s -i" % sys.argv[0]
        print "%s validate" % sys.argv[0]
        julgran.printJulgran(3, random.choice([True, False]), msg="yeah!")
