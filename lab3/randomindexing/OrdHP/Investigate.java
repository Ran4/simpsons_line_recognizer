import java.util.*;
import moj.ri.SparseDistributedMemory;

public class Investigate {

    public static TesterHP testerHP;

    public static TreeSet resultTreeSet = new TreeSet();
    public static String fileOrDirectory;

    //    public static String path = "/var/tmp/rosell/RI/seed40812/";
    //    public static String path = "/var/tmp/rosell/RI/seed710225/";
    public static String path = "/var/tmp/rosell/RI/seed750711/";


    public static ResultHP doOne(TrainHPProperties trainHPProperties){
	long time1 = System.currentTimeMillis();
	System.out.println("\n\n--------------------- Training\n");
	//	System.out.println("Training File or Directory: "+fileOrDirectory);

	TrainHP trainHP = new TrainHP(fileOrDirectory, trainHPProperties);
	SparseDistributedMemory ri = trainHP.train();

	System.out.println("\n--------------------- Testing");
	ResultHP resultHP = testerHP.test(ri, trainHP);
	resultHP.writeTestResultFigures();
	//	resultHP.setResultFilePath("investigation/");
	//	resultHP.setResultFilePath("/var/tmp/rosell/RI/result/");
	resultHP.setResultFilePath(path);
	resultHP.writeResultFile();

	// Time
	long time2 = System.currentTimeMillis();
	float timediff = (float)(time2-time1)/60000;
	System.out.println("\nTotal time: " + timediff + " minutes");

	return resultHP;
    }



    public static void main(String arg[]) {

	if(arg.length > 1) {

	    String facit = arg[1];
	    testerHP = new TesterHP(facit);
	    fileOrDirectory = arg[0];
	    System.out.println("Training File or Directory: "+fileOrDirectory);
	    
	    InvestigateHelper investigateHelper = new InvestigateHelper();
	    Vector trainHPPropertiesVector = investigateHelper.getPropertyVector();
	    for(int i=0; i<trainHPPropertiesVector.size(); i++){
		TrainHPProperties trainHPProperties = (TrainHPProperties)
		    trainHPPropertiesVector.elementAt(i);
		ResultHP resultHP = doOne(trainHPProperties);
		resultTreeSet.add(resultHP);
	    }

	    System.out.println("\n\n--------------------------------");
	    System.out.println("Generar sorterade filer.");
	    int i=1;
	    Iterator it = resultTreeSet.iterator();
	    String fileNameStart = 
		"resultHP."+System.currentTimeMillis()+".sorted.";
	    while(it.hasNext()){
		ResultHP resultHP = (ResultHP) it.next();
		//		resultHP.setResultFilePath("investigation/sorted/");
		//		resultHP.setResultFilePath("/var/tmp/rosell/RI/sorted/");
		resultHP.setResultFilePath(path);
		String number = Integer.toString(i);
		int tagLength=5;
		if(number.length()<tagLength)
		    for(int j=number.length(); j<tagLength; j++)
			number = "0"+number;
		resultHP.writeResultFile(fileNameStart+number+".res");
		i++;
	    }

	} else {
	    System.out.println("*** OrdHPInvestigate ***");
	    System.out.println("Usage: OrdHP <random index> (<facit>)");
	    System.out.println("<random index> : RandomIndex to load");
	    System.out.println("<facit> : data file containg the words to compare for semantic closeness");
	    System.out.println("\nFor example:\njava -Xmx100m -cp bin;lib/xerces.jar OrdHP ordhptest enordhsp.dat");
	}
    }
}
