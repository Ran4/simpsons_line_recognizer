/*
 *  This class is part of the 'Random Typewriter' assignment in the
 *  Language Technology course at KTH.
 *
 *  First version: Johan Boye, November 2014.
 */

import java.io.*;
import java.util.Random;

public class RandomKey {
    
    /* We are using the ISO-8859-1 encoding, representing 'ö' by 246, 'ä' by 228, and 'å' by 229. */ 
    static final char key[] =    { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 246, 228, 229 };

    static final int NUMBER_OF_CHARS = key.length + 1;
    // The START_END character is used to represent end of word/end of sentence.
    static final int START_END = NUMBER_OF_CHARS - 1;

    public static char[][] neighbour = {
	{'q','w','s','z'}, // a
	{'v','g','h','n'}, // b
	{'x','d','f','v'}, // c
	{'x','s','e','r','f','c'}, // d
	{'w','s','d','r'}, // e
	{'d','r','t','g','v','c'}, // f
	{'f','t','y','h','b','v'}, // g
	{'g','y','u','j','n','b'}, // h
	{'u','j','k','o'}, // i
	{'h','u','i','k','m','n'}, // j
	{'m','j','i','o','l'}, // k
	{'k','o','p',229}, // l
	{'n','j','k'}, // m
	{'b','h','j','m'}, // n
	{'i','k','l','p'}, // o
	{'o','l',229,246,}, // p
	{'w','a'}, // q
	{'e','d','f','t'}, // r
	{'a','w','e','d','x','z'}, // s
	{'r','f','g','y'}, // t
	{'y','h','j','i'}, // u
	{'c','f','g','b'}, // v
	{'q','a','s','e'}, // w
	{'z','s','d','c'}, // x
	{'t','g','h','u'}, // y
	{'x','s','a'}, // z
	{'p','l',229,228}, // ö
	{246,229}, // ä
	{'p',228,246}, // å
	{}};    // whitespace, represented by the START_END symbol
	
    Random random = new Random();

    void readEvalPrint() {
	while ( true ) {
	    try {
		BufferedReader in = new BufferedReader( new InputStreamReader( System.in ));
		char[] chars = (in.readLine()).toCharArray();
		char[] result = new char[chars.length];
		for ( int i=0; i<chars.length; i++ ) {
		    result[i] = keyPress( chars[i] );
		}
		System.out.println( new String( result ));
	    }
	    catch ( IOException e ) {
		e.printStackTrace();
	    }
	}
    }

    static int charToIndex( char c ) {
	if ( c>='a' && c<='z' )
	    return c-'a';
	else if ( c == 246 ) 
	    return 26;
	else if ( c == 228 ) 
	    return 27;
	else if ( c == 229 )
	    return 28;
	else
	    return START_END;
    }


    static char indexToChar( int i ) {
	if ( i < key.length )
	    return key[i];
	else 
	    return '.';
    }

    char keyPress( char c ) {
	int index = charToIndex( c );
	if ( index == START_END ) 
	    return c;
	else {
	    int r = random.nextInt( 10 );
	    if ( r<neighbour[index].length )
		return neighbour[index][r];
	    else
		return c;
	}
    }
 
    public static void main( String[] args ) {
	RandomKey r = new RandomKey();
	r.readEvalPrint();
    }
}