from collections import Counter

from termcolor import colored

import replikidentifier

global ngramStopList
def loadNgramStopList(fileName):
    ngramStopList = []
    for line in open(fileName).read().split("\n"):
        if line:
            w1, w2 = line.split("\t")
            ngramStopList.append((w1, w2))
            
def calculateNGrams(replik, verbose, globalMinCount=None, NValues=[2], ngramStopList=None):
    """Calculates N-grams for a dict of lines replik
    Returns a dict with key = n, value = an ngrams dict
    {
        3: {"BART": Counter({'hej': 2, 'hopp': 1})},
    }
    """
    #NValues = [1,2,3,4,5,6,7]
    #NValues = [2]
    
    ngramDict = {}
    for n in NValues:
        if verbose:
            print colored("%s-Grams:" % n, "cyan")
        
        if globalMinCount is not None:
            nGrams = calculateNGramsForCharacters(replik, n, verbose,
                    overrideMinCount=globalMinCount)
        else:
            nGrams = calculateNGramsForCharacters(replik, n, verbose)
        if ngramStopList:
            nGrams = filter(lambda x: x not in ngramStopList, nGrams)
   
        ngramDict[n] = nGrams
    
    return ngramDict
            
def calculateNGramsForCharacters(replik, n, verbose, overrideMinCount=None):
    """Calculates NGrams with n=n for all characters in replik
    Filters out uncommon ngrams depending on n, unless overrideMinCount
    is given (where ngrams occuring less than overrideMinCount times
    is filtered).

    Returns a dictionary with the keys being the names and the values being
        a Counter object. Like this:
    {"BART": Counter({'hej': 2, 'hopp': 1})}
    """
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
        if verbose:
            print colored("%s, %s lines" % \
                (name, len(replik[name])), "yellow"),
                
        for line in replik[name]:
            for ngram in generateNGramsForLine(line, n):
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
                print " ".join(["%s(%s)" % item for item in sortedItems[:5]]) + ".."
        #pprint.pprint(ngramCounter)
        
        nGrams[name] = ngramCounter
        
    return nGrams

def generateNGramsForLine(line, n):
    words = line.split(" ")
    for i in range(len(words) - n + 1):
        ngram = " ".join(words[i:i+n])
        yield ngram
        
if __name__ == "__main__":
    calculateNGrams(
            {"TEST":["hej du", "hopp hej med jurtman"]},
            verbose=2, globalMinCount=0, NValues=[2])
