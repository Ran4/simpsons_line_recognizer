/*
 *  This class is part of the 'Random Typewriter' assignment in the
 *  Language Technology course at KTH.
 *
 *  First version: Johan Boye, November 2014.
 */

import java.util.*;
import java.io.*;

public class Decoder {

    /** The trellis used for Viterbi decoding. The first index is the
     * time step. */
    double[][] v;

    /** The bigram stats. */ 
    double[][] a = new double[RandomKey.NUMBER_OF_CHARS][RandomKey.NUMBER_OF_CHARS];

    /** The observation matrix. */
    double[][] b = new double[RandomKey.NUMBER_OF_CHARS][RandomKey.NUMBER_OF_CHARS];

    /** Pointers to retrieve the topmost hypothesis. */
    int[][] backptr;

    /** Reads the bigram stats from a file. */
    void init_a( String filename ) {
	try {
	    FileReader in = new FileReader( filename );
	    Scanner scan = new Scanner( in );
	    while ( scan.hasNext() ) {
		int i = scan.nextInt();
		int j = scan.nextInt();
		double d = scan.nextDouble();
		a[i][j] = d;
	    }
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	}
    }

    /** 
     *  Initializes the observation matrix.
     *  Note that the 'neighbour' matrix really encodes
     *  p(shown=x|pressed=y), whereas we are interested 
     *  in p(pressed=y|shown=x). For the random typewriter
     *  application, it happens to be the same thing. But,
     *  as a word of caution, it might not be in other
     *  applications.
     */
    void init_b() {
	for ( int i=0; i<RandomKey.NUMBER_OF_CHARS; i++ ) {
	    char[] cs = RandomKey.neighbour[i];
	    // Initialize all log-probabilities to some small value.
	    for ( int j=0; j<RandomKey.NUMBER_OF_CHARS; j++ ) {
		b[i][j] = Double.NEGATIVE_INFINITY;
	    }
	    // All neighbouring keys are assigned the probability 0.1
	    for ( int j=0; j<cs.length; j++ ) {
		b[i][RandomKey.charToIndex(cs[j])] = Math.log( 0.1 );
	    }
	    // The remainder of the probability mass is given to the correct key.
	    b[i][i] = Math.log( (10-cs.length)/10.0 );
	}
    }

    /**
     *  Performs the Viterbi decoding and returns the most likely
     *  string. 
     */
    String viterbi( String s ) {

	// First turn chars to integers, so that 'a' is represented by 0, 
	// 'b' by 1, and so on. 
	int[] index = new int[s.length()];
	for ( int i=0; i<s.length(); i++ ) {
	    index[i] = RandomKey.charToIndex( s.charAt( i ));
	}

	// The Viterbi matrices
	v = new double[index.length][RandomKey.NUMBER_OF_CHARS];
	backptr = new int[index.length+1][RandomKey.NUMBER_OF_CHARS];

	// Initialization
	for ( int i=0; i<RandomKey.NUMBER_OF_CHARS; i++ ) {
	    v[0][i] = a[RandomKey.START_END][i]+b[index[0]][i];
	    backptr[0][i] = RandomKey.START_END;
	}

	// Induction step
	
	// YOUR CODE HERE

	// Termination step
	double best_prob = Double.NEGATIVE_INFINITY;
	int last_backptr = RandomKey.START_END;
	for ( int j=0; j<RandomKey.NUMBER_OF_CHARS; j++ ) {
	    double m = v[index.length-1][j]+a[j][RandomKey.START_END];
	    if ( m > best_prob ) {
		best_prob = m;
		last_backptr = j;
	    } 
	}
	    
	// Finally return the result
	char[] c = new char[index.length];
	int current = last_backptr;
	for ( int t=index.length-1; t>=0; t-- ) {
	    if ( current >= RandomKey.key.length ) 
		c[t] = ' ';
	    else
		c[t] = RandomKey.key[current];
	    current = backptr[t][current];
	}
	print_trellis( c );
	return new String( c ).trim();  // Trim to remove the trailing space.
    }


    // ------------------------------------------------------
    // 
    // Some debugging methods
    //

    public void print_trellis( char[] c ) {
	for ( int t=0; t<c.length; t++ ) {
	    for ( int i=0; i<RandomKey.NUMBER_OF_CHARS; i++ ) {
		System.err.format( "%c ", RandomKey.indexToChar( backptr[t][i] ));
	    }
	    System.err.println();
	    if ( t<c.length-1 ) {
		print_connector( RandomKey.charToIndex(c[t]), RandomKey.charToIndex(c[t+1]) );
	    }
	    else {
		print_connector( RandomKey.charToIndex(c[t]), RandomKey.START_END );
	    }
	}
    }
    
    // Print the backpointers in an easy-to-read format
    public void print_connector( int start, int end ) {
	int i;
	for ( i=0; i<start; i++ ) {
	    System.err.print( "  " );
	}
	System.err.println( "|" );
	if ( start == end )
	    return;
	for ( i=0; i<Math.min(start,end); i++ ) {
	    System.err.print( "  " );
	}
	for ( int j=i; j<Math.max(start,end); j++ ) {
	    System.err.print( "--" );
	}
	System.err.println();
	for ( i=0; i<end; i++ ) {
	    System.err.print( "  " );
	}
	System.err.println( "|" );
    }

    // ------------------------------------------------------

    
    public Decoder( String filename ) {
	init_a( filename );
	init_b();
    }


    // ------------------------------------------------------
	

    public static void main( String[] args ) {
	Decoder d = new Decoder( args[0] );
	while ( true ) {
	    try {
		// The string we want to decode is input from the keyboard
		BufferedReader in = new BufferedReader( new InputStreamReader( System.in ));
		char[] c = in.readLine().toCharArray();
		// The following recoding is done because of the Windows command window
		// encoding.
		for ( int j=0; j<c.length; j++ ) {
		    int i = (int)c[j];
		    if ( i == 8221 )
			c[j] = 246;
		    if ( i == 8222 )
			c[j] = 228;
		    if ( i == 8224 )
			c[j] = 229;
		}
		String result = d.viterbi( new String( c ) + RandomKey.indexToChar( RandomKey.START_END ));
		System.out.println( result );
	    }
	    catch ( IOException e ) {
		e.printStackTrace();
	    }
	}
    }
}
	