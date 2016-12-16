TUI application that tries to identify a sentence with a The Simpsons character based on N-Grams.

A report that explains the algoritm used (in Swedish) is available in [report](report).

## Installation

Clone the repository

`git clone https://github.com/Ran4/simpsons_line_recognizer`

All libraries are included in this repository.

## Usage

Run the very nicely named `replikidentifier.py`, preferably using the -i interactive switch:

```
python2 replikidentifier.py -i
-> hey dad
Bart (2.513), Lisa (0.03043), Homer (0.005907), Marge (0.001147)
```

The numbers are relative scores.

By default, only the five most common Simpsons characters (of the scripts that were used) are taken into account.

Enter a number to change the number of Simpsons characters used (e.g. 'please come again' won't match the Apu character unless you first enter 7, since Apu is the 7th most popular character in the scripts used).

A fancy-looking evaluation can be generated with `python2 replikidentifier.py validate`.
