/*
 *  This class is part of the 'Random Typewriter' assignment in the
 *  Language Technology course at KTH.
 *
 *  First version: Johan Boye, November 2014.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class TypeWriter extends JFrame implements KeyListener {

    /** The actual typewriter. */
    private RandomKey rand = new RandomKey();
    
    /** The file containing the bigram statistics. */
    public static final String STATSFILE = "bigramstats.txt";

    /* GUI resources */
    JTextArea inputWindow = new JTextArea( "", 2, 28 );    
    JTextArea outputWindow = new JTextArea( "", 2, 28 );
    JTextArea decodeWindow = new JTextArea( "", 2, 28 );
    JScrollPane inputPane = new JScrollPane( inputWindow );    
    JScrollPane outputPane = new JScrollPane( outputWindow );
    JScrollPane decodePane = new JScrollPane( decodeWindow );
    JButton clearBut = new JButton( "Clear all" );
    JButton decodeBut = new JButton( "Decode" );
    Font coorFont = new Font( "Courier", Font.BOLD, 18 );

    /* Set up the GUI */
    public TypeWriter() {
	setSize( 600, 350 );
	inputWindow.setFont( coorFont );
	inputWindow.addKeyListener( this );
	outputWindow.setFont( coorFont );
	decodeWindow.setFont( coorFont );
	JPanel p = new JPanel();
	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	p.add( inputPane );
	p.add( outputPane );
	p.add( decodePane );
	JPanel b = new JPanel();
	b.setLayout(new BoxLayout(b, BoxLayout.X_AXIS));
	b.add( clearBut );
	b.add( decodeBut );
	p.add( b );
	getContentPane().add(p, BorderLayout.CENTER);
	setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	setVisible( true );
	setTitle( "Random Typewriter" );

	/** Create the decoder. */
	final Decoder d = new Decoder( STATSFILE );

	/** Handle a click on the "Clear All" button. */
	Action clear = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    inputWindow.setText( "" );
		    outputWindow.setText( "" );
		    decodeWindow.setText( "" );
		}
	    };
	clearBut.addActionListener( clear );

	/** Handle a click on the "Decode" button. */
	Action decode = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    String original = inputWindow.getText();
		    String scrambled = outputWindow.getText();
		    // First add a end-of-string symbol at the end
		    String s = scrambled + RandomKey.indexToChar( RandomKey.START_END );
		    // Then decode the scrambled string
		    String decoded = d.viterbi( s );
		    // Compute the quality of the result 
		    int baseline = compare( original, scrambled );
		    int result = compare( original, decoded );
		    outputWindow.append( " (" + baseline + "%)" );
		    decodeWindow.setText( decoded + " (" + result + "%)" );
		}
	    };
	decodeBut.addActionListener( decode );
    }

    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
	if ( e.getKeyChar() == 8 ) {
	    // Backspace
	    String s = outputWindow.getText();
	    if ( s.length() > 0 ) {
		outputWindow.setText( s.substring( 0, s.length()-1 ));
	    }
	} 
	else {
	    outputWindow.append( rand.keyPress( e.getKeyChar() ) + "" );
	}
    }
     
    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
    }
     
    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {
    }

    //---------------------------

    int compare( String s1, String s2 ) {
	int match = 0;
	int non_space = 0;
	for ( int i=0; i<s1.length() && i<s2.length(); i++ ) {
	    if ( s1.charAt(i) != ' ' ) {
		non_space++;
		if ( s1.charAt(i) == s2.charAt(i) ) {
		    match++;
		}
	    }
	}
	if ( non_space == 0 )
	    return 0;
	else
	    return 100*match/non_space;
    }


    public static void main( String[] args ) {
	new TypeWriter();
    }
}