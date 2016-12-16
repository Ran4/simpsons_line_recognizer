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
