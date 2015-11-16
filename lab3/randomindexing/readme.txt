RandomIndexing - Java packages for Random Indexing and analyzing texts using the
Granska Server. Please see licence.html regarding use of these packages.

When trying to use these Java packages it is very important to have ones
classpath straight. In order to get TrainRI and TestRI to run properly, take
great care to include both "lib/xerces.jar" and the folder "bin" in the
classpath. For example, standing in the package root folder:


java -Xmx100m -cp bin:lib TrainRI data rodarummet

and then:

java -Xmx100m -cp bin:lib TestRI rodarummet


or:


java -Xmx100m -cp bin:lib TrainRI Frankenstein.txt frankenstein Frankenstein.properties

and then:

java -Xmx100m -cp bin:lib TestRI frankenstein life death love hate monster


The data files, "Röda rummet" by August Strindberg and "Frankenstein" by Mary
Wollstonecraft Shelley, are freely distributable and were downloaded from
Projekt Runeberg (http://www.lysator.liu.se/runeberg/) respectively Project
Gutenberg (http://www.gutenberg.net/). Credits and legal small print were
removed from these files, else these would "taint" the statistics/index.
Unedited versions of the texts can be downloaded from the URL:s given above.

If you want a light weight introdcution to the theoretical side of Random
Indexing, a nice article with pretty pictures can be found at:
http://www.ercim.org/publication/Ercim_News/enw50/sahlgren.html
For articles on scientific use of the technique please visit Magnus Sahlgren's
web page at: http://www.sics.se/~mange/

Thank you for using, or at least reviewing, these packages and tools.
The latest version can be found at: http://www.nada.kth.se/~xmartin/
Feedback is most welcome and accepted suggestions/modifications will be added
with due credit.

Gratitude extended to the following people:
Magnus Sahlgren for eternal patience and feedback during the first implementation
Magnus Rosell for testing and comments on optimization of the RI core

Best regards,
Martin Hassel
KTH NADA Aug 2004
