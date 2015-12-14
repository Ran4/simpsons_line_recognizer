#!/usr/bin/env python
#encoding: utf-8
import sys

try:
    from termcolor import colored
except: #Couldn't load termcolor, use a regular function instead
    def colored(*args):
        return args[0]
    

def printJulgran(n, kulorElseCones=True, msg=None):
    r"""
        /\
       /\/\      MSG
      /\/\/\
      ^^||^^
      BUT COLORED IN TRUE 256 COLORS
    """
        
    barrColor = "green"
    barkColor = "red"
    
    RESET_COLOR = '\033[0m'
    MSG_COL = "\033[0m"
    bgCol = ""
    for i in range(0, n):
        numSpaces = (n - 1) - i
        numToothPicks = i + 1
        sys.stdout.write(" "*numSpaces + colored("/\\"*numToothPicks, barrColor))
        
        if msg and i == (n-1)/2:
            sys.stdout.write(" "*4 + MSG_COL + msg + RESET_COLOR)
        sys.stdout.write("\n")
    
    
    MANUAL_BARKCOLOR = '\033[38;5;137m'
    barkStr = MANUAL_BARKCOLOR + "||" + RESET_COLOR
    
    if kulorElseCones:
        ornament = "°"
        ORNAMENT_COL = '\033[38;5;160m'
    else:
        ornament = "^"
        ORNAMENT_COL = '\033[38;5;135m'
        
    if n > 1:
        numCones = n - 1
        ornamentStr = ORNAMENT_COL + ornament*numCones + RESET_COLOR
        print ornamentStr + barkStr + ornamentStr
    
