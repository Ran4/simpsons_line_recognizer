import moj.ri.SparseDistributedMemory;

/**
 * A small but functional demo program trying to solve the synonym part
 * (ORD-delen) of the Swedish higher education entrance exam (Högskoleprovet).
 * Only one-to-one word correspondances are supported, so far. Extending this
 * small, and very simple, program with various schemes for handling synonyms
 * with one-to-many word corespondances would probably make for an interesteing
 * course project for the Language Technology course (2D1418 Språkteknologi).
 *
 * @author  Martin Hassel and Magnus Rosell
 * @version 2004
 */


public class OrdHP {
    public static void main(String arg[]) {
	if(arg.length > 0) {

	    long time1 = System.currentTimeMillis();
	    System.out.println("\n\n--------------------- Training\n");
	    String fileOrDirectory = arg[0];
	    System.out.println("Training File or Directory: "+fileOrDirectory);
	    TrainHP trainHP = new TrainHP(fileOrDirectory);
	    SparseDistributedMemory ri = trainHP.train();

	    System.out.println("\n\n--------------------- Testing\n");
	    String facit;
	    if(arg.length > 1)
	    	facit = arg[1];
	    else
	    	facit = "enordhsp.dat";
	    TesterHP testerHP = new TesterHP(facit);
	    ResultHP resultHP = testerHP.test(ri, trainHP);
	    resultHP.writeTestResults();
	    resultHP.writeResultFile();

	    // Time
	    long time2 = System.currentTimeMillis();
	    float timediff = (float)(time2-time1)/60000;
	    System.out.println("\nTotal time: " + timediff + " minutes");

	} else {
	    System.out.println("*** OrdHP ***");
	    System.out.println("Usage: OrdHP <random index> (<facit>)");
	    System.out.println("<random index> : RandomIndex to load");
	    System.out.println("<facit> : data file containg the words to compare for semantic closeness");
	    System.out.println("\nFor example:\njava -Xmx100m -cp bin;lib/xerces.jar OrdHP ordhptest enordhsp.dat");
	}
    }
}
