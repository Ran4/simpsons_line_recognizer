import java.io.*;
import java.util.Properties;
import java.util.HashSet;

import moj.granska.GranskaConnection;
import moj.ri.RandomIndex;
import moj.ri.SparseDistributedMemory;
import moj.ri.weighting.WeightingScheme;
import moj.util.TagStripper;

public class TrainHP {
    TrainHPProperties trainProps;
    String fileOrDirectory;

    HashSet stopSet = new HashSet();

    public TrainHP(String fileOrDirectory){
	this.fileOrDirectory = fileOrDirectory;
	// Load and set properties for the RandomIndex
	trainProps = new TrainHPProperties();
	// Print settings for current index, handy when running batch jobs
	trainProps.writeProperties(new OutputStreamWriter(System.out));
    }

    public TrainHP(String fileOrDirectory, TrainHPProperties trainProps){
	this.fileOrDirectory = fileOrDirectory;
	this.trainProps=trainProps;
	// Print settings for current index, handy when running batch jobs
	trainProps.writeProperties(new OutputStreamWriter(System.out));
    }


    public SparseDistributedMemory train(){
	long time1, time2;
	float timediff;
	SparseDistributedMemory sdm=null;
	try {

	    // Read Stoplist
	    if(trainProps.stoplist)
		readStopList();

	    time1 = System.currentTimeMillis();
	    // First we need a WeightingScheme that tells the RandomIndex/SDM
	    // how to weight the context window when updating context labels
	    Class cls = Class.forName(trainProps.weightingScheme);
	    WeightingScheme ws = (WeightingScheme)cls.newInstance();
	    // And then we need the actual RandomIndex (here in the form of SparseDistributedMemory
	    // which is the RandomIndex extended with load/save functionality etc.)
	    sdm = 
		new SparseDistributedMemory(trainProps.dimensionality, 
					    trainProps.randomDegree, 
					    trainProps.seed, 
					    trainProps.leftWindowSize, 
					    trainProps.rightWindowSize, 
					    ws);

	    // Now we can recursivly walk through and Random Index the files in the given folder
	    System.out.print("Indexing: ");
	    recurseInDirFrom(fileOrDirectory, sdm, 0);
	    time2 = System.currentTimeMillis();
	    // And print some stats about the index we just built
	    timediff = (float)(time2-time1)/60000;
	    System.out.println("Indexing file(s) took " + timediff + " minutes");
	    System.out.println(sdm.getWordsIndexed() + " words indexed");
	    System.out.println(sdm.size() + " wordforms/index terms");

	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}

	return sdm;
    }


    public long recurseInDirFrom(String dirItem, RandomIndex ri, long fileNumber) {
	File file;
	String list[];	
	file = new File(dirItem);
	// Recurse through folders in folder
	if(file.isDirectory()) {
	    list = file.list();
	    for(int i = 0; i < list.length; i++){
		fileNumber = recurseInDirFrom(dirItem + File.separatorChar + list[i], ri, fileNumber);
	    }
	    // Carriage return after finished directory
	    System.out.println();
	}

	// Random Index files (with correct extension) in folder
	File fileItem = new File(dirItem);
	if(fileItem.isFile() && 
	   dirItem.endsWith(trainProps.restrict_extension)) {
	    try {
		// Write a point for each hundred files
		if(fileNumber % 100 == 1)
		    System.out.print(".");
		fileNumber++;
		//		System.out.print(dirItem + " ... ");
		String line = null;
		// Read current file into a StringBuffer
		StringBuffer doc = new StringBuffer();
		BufferedReader txtbr = new BufferedReader(new FileReader(dirItem));
		// Append lines to form one "single" line (with line breaks)
		while((line = txtbr.readLine()) != null) {
		    doc.append(line + " \n");
		}
		txtbr.close();
		String text = doc.toString();
		if(trainProps.strip_html.compareToIgnoreCase("on")==0) {
		    // Remove any HTML _tags_ from the file (not comments or code, yet)
		    TagStripper ts = new TagStripper();
		    text = ts.stripTags(text);
		}
		// Each discourse segment is treated as a separate document
		String[] discourseSegments;
		String discourseMarker = trainProps.discourse_marker;
		if(discourseMarker.compareToIgnoreCase("")!=0) {
		    System.out.println("indexing discourse segments (discourse_marker="+discourseMarker+")");
		    discourseSegments = text.split(discourseMarker);
		} else {
		    discourseSegments = new String[1];
		    discourseSegments[0] = text;
		}
		for(int i=0 ; i<discourseSegments.length ; i++) {
		    text = discourseSegments[i];
		    // Do we wish to use the Granska Server for SWEDISH preprocessing?
		    String cgranska = trainProps.granska;
		    if(cgranska.compareToIgnoreCase("tokenize")==0) {
			GranskaConnection granska = new GranskaConnection();
			text = granska.tokenize(text);
		    } else if(cgranska.compareToIgnoreCase("lemmatize")==0) {
			GranskaConnection granska = new GranskaConnection();
			text = granska.lemmatize(text);
		    } else if(cgranska.compareToIgnoreCase("lemmatag")==0) {
			GranskaConnection granska = new GranskaConnection();
			text = granska.lemmaTag(text);
		    } else if(cgranska.compareToIgnoreCase("simpletag")==0) {
			GranskaConnection granska = new GranskaConnection();
			text = granska.simpleTag(text);
		    } else {
			// Tokenize text by removing all punctuation
			text = text.replaceAll("[\\s-.,:;!?()\"&]"," ").replaceAll("\\s\\s"," ");
		    }

		    text=removeWords(text);
		    // Add preprocessed text to Random Index
		    ri.addText(text);

		    /*
		    if(trainProps.save_preprocessed_text.compareToIgnoreCase("on")==0) {
			// Save preprocessed text to file for later use
			PrintWriter pr = new PrintWriter(new BufferedWriter(new FileWriter(dirItem + "." + cgranska + i + ".txt", false)));
			pr.print(text);
			pr.flush();
			pr.close();
		    }
		    */
		}
	    } catch(Exception ex) {
		ex.printStackTrace();
		System.exit(0);
	    }
	}
	return fileNumber;
    }


    public TrainHPProperties getTrainHPProperties(){
	return trainProps;
    }


    private String removeWords(String text) throws IOException{
	
	if(trainProps.stoplist || 
	   trainProps.shortest_word!=0 || 
	   trainProps.shortest_word!=0 || 
	   trainProps.minimum_words_per_file!=0){
	    String newText="";
	    String[] words=text.split(" ");
	    //	    System.out.println(words.length);
	    int numOfWords=0;
	    for(int i=0; i<words.length; i++){
		boolean save=true;
		if(trainProps.stoplist && 
		   stopSet.contains(words[i]))
		    save=false;
		if(trainProps.shortest_word != 0 
		   && words[i].length() < trainProps.shortest_word)
		    save=false;
		if(trainProps.longest_word != 0 && 
		   words[i].length() >= trainProps.longest_word)
		    save=false;
		if(save){
		    newText=newText+words[i]+" ";
		    numOfWords++;
		}
	    }
	    if(numOfWords > trainProps.minimum_words_per_file)
		text=newText;
	    //	    System.out.println(numOfWords);
	}
	return text;
    }

    private void readStopList(){
	try{
	    stopSet=new HashSet();
	    BufferedReader fr = 
		new BufferedReader(new FileReader(trainProps.stoplist_name));
	    String word = fr.readLine();
	    while(word!=null){
		stopSet.add(word);
		word = fr.readLine();
	    }
	    fr.close();
	} catch(IOException ioe){
	    ioe.printStackTrace();
	    System.exit(0);
	}
    }
}
