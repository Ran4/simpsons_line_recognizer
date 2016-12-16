from collections import Counter
import time

from termcolor import colored

import replikidentifier

global ngramStopList
ngramStopList = {}
def loadNgramStopList(fileName, n=2):
    global ngramStopList
    
    ngramStopList[n] = [" ".join(line.split()) for line in open(fileName).read().split("\n") if line]
    #print ngramStopList
            
def noStopList(n=2):
    global ngramStopList
    
    ngramStopList[n] = []
            
def calculateNGrams(replik, verbose, globalMinCount=None, NValues=[2], scoreFunction=None):
    """Calculates N-grams for a dict of lines replik
    Returns a dict with key = n, value = an ngrams dict
    {
        3: {"BART": Counter({'hej': 2, 'hopp': 1})},
    }
    """
    ngramDict = {}
    for n in NValues:
        if verbose:
            print colored("%s-Grams:" % n, "cyan")
        
        if globalMinCount is not None:
            nGrams = calculateNGramsForCharacters(replik, n, verbose,
                    overrideMinCount=globalMinCount)
        else:
            nGrams = calculateNGramsForCharacters(replik, n, verbose)
        
        if scoreFunction is not None:
            scoreFunction(nGrams)
   
        ngramDict[n] = nGrams
    
    return ngramDict
    
def rescoreNGrams(nGrams):
    """rescoreNGrams: Takes a dict of ngrams of form
    {"BART": Counter({'hej': 2, 'hopp': 1})}
    and in-place multiplies the value of every key with 1/occurance
    where occurance is the number of occurances of a specific ngram
    """
    
    allNGrams = Counter()
    map(allNGrams.update, nGrams.values())
    
    #print "allNGrams:", allNGrams
    
    for name in nGrams.keys():
        for ngramStr in nGrams[name]:
            nGrams[name][ngramStr] *= 1.0 / allNGrams[ngramStr]
            
def rescoreNGramsByMoreUniqueMethod(nGrams):
    """Like rescoreNGrams but use all nGrams except those specific
    to a certain character
    """
    def getAllNGramsExceptByCharacter(nGrams, name):
        allNGrams = Counter()
        map(allNGrams.update,
                [item[1] for item in nGrams.items()
                    if item[0] != name])
        return allNGrams
    
    UNIQVALUE = 0.05
    #MUL = 1e9
    for name in nGrams.keys():
        allNGrams = getAllNGramsExceptByCharacter(nGrams, name)
        for ngramStr in nGrams[name]:
            nGrams[name][ngramStr] *= 1.0 / max(UNIQVALUE, allNGrams[ngramStr])
            
def calculateNGramsForCharacters(replik, n, verbose, overrideMinCount=None):
    """Calculates NGrams with n=n for all characters in replik
    Filters out uncommon ngrams depending on n, unless overrideMinCount
    is given (where ngrams occuring less than overrideMinCount times
    is filtered).

    Returns a dictionary with the keys being the names and the values being
        a Counter object. Like this:
    {"BART": Counter({'hej': 2, 'hopp': 1})}
    """
    global ngramStopList
    
    def removeItemsUnderCount(counter, n):
        smallCountList = filter(lambda item: counter[item] < n,
                counter)
        for item in smallCountList:
            del counter[item]
            
    nGrams = {}
    
    #minCount is used to filter out overly common ngrams
    if overrideMinCount is not None:
        minCount = overrideMinCount
    else:
        if n == 1:   minCount = 15
        elif n == 2: minCount = 6
        elif n == 3: minCount = 4
        elif n == 4: minCount = 3
        elif n >= 5: minCount = 2
    
    for name in replik.keys():
        ngramCounter = Counter()
        if verbose:
            print colored("%s, %s lines" % (name, len(replik[name])), "yellow"),
                
        for line in replik[name]:
            for ngram in generateNGramsForLine(line, n):
                if ngram not in ngramStopList.get(n, []):
                    ngramCounter[ngram] += 1
                
        if minCount:
            removeItemsUnderCount(ngramCounter, minCount)
        sortedItems = sorted(ngramCounter.items(),
                key=lambda x: x[1], reverse=True)
        
        if verbose:
            print "Most common n-grams: ",
            if verbose == 2:
                print " ".join(["%s(%s)" % item for item in sortedItems])
            else:
                print " ".join(["%s(%s)" % item
                    for item in sortedItems[:5]]) + ".."
        
        nGrams[name] = ngramCounter
        
    return nGrams

# def generateNGramsForLine(line, n):
#     words = line.split(" ")
#     for i in range(len(words) - n + 1):
#         ngram = " ".join(words[i:i+n])
#         yield ngram

# def generateNGramsForLine(line, n):
#     words = line.split(" ")
#     yield "$ %s" % words[0]
#     for i in range(len(words) - n + 1):
#         ngram = " ".join(words[i:i+n])
#         yield ngram
#     yield "%s $" % words[-1]

def generateNGramsForLine(line, n):
    words = line.split(" ")
    if n > 1:
        yield "$ %s" % " ".join(words[0:(n-1)])
    for i in range(len(words) - n + 1):
        ngram = " ".join(words[i:i+n])
        yield ngram
    if n > 1:
        yield "%s $" % " ".join(words[len(words)-(n-1):])

        
if __name__ == "__main__":
    calculateNGrams(
            {"TEST":["hej du", "hopp hej med jurtman"]},
            verbose=2, globalMinCount=0, NValues=[2])
