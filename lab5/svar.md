# Uppgift 1:
Förklara för labbassistenten varför det är lämpligt att logaritmera sannolikheterna, snarare än att representera dem som decimaltal mellan 0 och 1.

* För att undvika avrundningsfel

# Uppgift 2
Förklara för labbassistenten hur feltryckningsmodellen som beskrivits ovan kan
representeras som en HMM. Beskriv speciellt vilka tillstånden är, vilka observationerna är, och hur
man kan definiera sannolikhetsmatriserna A (för tillståndsövergångar) och B (för observationer).

* Tillståndsövergångarna: bi/tri-gram-statistiken
* Observationerna: Sannolikheten för en knapptryckning givet en bokstav

Man kan definiera sannolikhetsmatriserna A (för tillståndsövergångar) och B (för observationer) såhär:

A: Bigramstatistiken, som en 29-29-matrix

B: p(pressed=y|shown=x)

# Uppgift 3
Om vi ser strängen "qööq", vilket svenskt ord har antagligen skrivits in, givet feltrycknings-
modellen och bigramsstatistiken för svenska som finns i filen "bigramstats.txt"? Rita upp grafen
(trellis) för strängen "qööq" och förklara.

* alla
. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
|
a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a 
|
a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a 
|
a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a 
|
--------------------------------------------------------
                                                        |
a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a 
                                                        |
                                                        --
                                                          |

. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
|
----------------------
                      |
a a a a a a a a a a a a a a a a a a a a a a a a a a a w a a 
                      |
a a a a a a a a a a a l a a a p a a a a a a a a a a l l p a 
                      |
----------------------
|
l a a a a a a a a a a a a a a a l a a a a a l a a a a a a a 
|
----------------------------------------------------------
                                                          |
a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a 
                                                          |

# Uppgift 4: Komplettera Java-koden i klassen Decoder.java (leta efter "YOUR CODE HERE"), så att
klassen korrekt implementerar Viterbi-algoritmen. Tips: Implementationen håller sig nära
pseudokoden i boken (s 220). En skillnad är att boken räknar sina tidssteg från 1 och uppåt, medan
Java-programmet räknar från 0. Notera även att det fallit bort en term b s (o t ) i den inre loopen i
rekursionssteget i fig 6.11 på sid 220. Ekvationerna 6.20-6.25 på samma sida är korrekta.
Om din implementation är korrekt så bör resultatet i det nedersta textfältet när du trycker "Decode"

* ???
