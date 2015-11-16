import java.io.*;
import java.util.Properties;
import java.util.HashSet;

import moj.granska.GranskaConnection;
import moj.ri.RandomIndex;
import moj.ri.SparseDistributedMemory;
import moj.ri.weighting.WeightingScheme;
import moj.util.TagStripper;

public class TrainHPProperties {
    // Default properties file
    private static String _propsfile = "TrainHP.properties";

    // Default values for the properties for RI
    private static String _dimensionality = "1800";
    private static String _random_degree  = "8";
    private static String _seed = "710225";
    private static String _left_window_size  = "4";
    private static String _right_window_size = "4";
    private static String _weighting_scheme = "moj.ri.weighting.MangesWS";

    private static String _stoplist = "False";
    private static String _stoplist_name = "Stoplist.txt";
    private static String _shortest_word = "0";
    private static String _longest_word = "0";
    private static String _minimum_words_per_file = "0";

    private static String _discourse_marker = "";
    private static String _strip_html = "off";
    private static String _restrict_extension = "";
    private static String _save_compressed = "on";
    private static String _granska = "off";
    private static String _save_preprocessed_text = "off";

    // Actual values
    public String propsfile;

    public int dimensionality;
    public int randomDegree;
    public int seed;
    public int leftWindowSize;
    public int rightWindowSize;
    public String weightingScheme;

    public boolean stoplist;
    public String stoplist_name;
    public int shortest_word;
    public int longest_word;
    public int minimum_words_per_file;

    public String restrict_extension;
    public String strip_html;
    public String discourse_marker;
    public String granska;
    public String save_preprocessed_text;
    
    
    public TrainHPProperties(){
	readProperties(_propsfile);
    }

    public TrainHPProperties(String propsfile){
	readProperties(propsfile);
    }

    public TrainHPProperties(int dimensionality,
			     int randomDegree){
	
	readProperties(_propsfile);

	this.dimensionality=dimensionality;
	this.randomDegree=randomDegree;
    }

    public TrainHPProperties(int dimensionality,
			     int randomDegree,
			     int seed,
			     int leftWindowSize,
			     int rightWindowSize,
			     String weightingScheme,
			     boolean stoplist,
			     int shortest_word){
	
	readProperties(_propsfile);

	this.dimensionality=dimensionality;
	this.randomDegree=randomDegree;
	this.seed=seed;
	this.leftWindowSize=leftWindowSize;
	this.rightWindowSize=rightWindowSize;
	this.weightingScheme=weightingScheme;

	this.stoplist=stoplist;
	this.shortest_word=shortest_word;
    }


    public TrainHPProperties(int dimensionality,
			     int randomDegree,
			     int seed,
			     int leftWindowSize,
			     int rightWindowSize,
			     String weightingScheme,
			     boolean stoplist,
			     int shortest_word,
			     int longest_word,
			     int minimum_words_per_file){
	
	readProperties(_propsfile);

	this.dimensionality=dimensionality;
	this.randomDegree=randomDegree;
	this.seed=seed;
	this.leftWindowSize=leftWindowSize;
	this.rightWindowSize=rightWindowSize;
	this.weightingScheme=weightingScheme;

	this.stoplist=stoplist;
	this.shortest_word=shortest_word;
	this.longest_word=longest_word;
	this.minimum_words_per_file=minimum_words_per_file;
    }


    private void readProperties(String propsfile){
	try {
	    // Load and set properties for the RandomIndex
	    Properties props = new Properties();
	    props.load(new FileInputStream(propsfile));
	    dimensionality = Integer.parseInt(props.getProperty("dimensionality",_dimensionality));
	    randomDegree  = Integer.parseInt(props.getProperty("random_degree",_random_degree));
	    seed = Integer.parseInt(props.getProperty("seed",_seed));
	    leftWindowSize  = Integer.parseInt(props.getProperty("left_window_size",_left_window_size));
	    rightWindowSize = Integer.parseInt(props.getProperty("right_window_size",_right_window_size));
	    weightingScheme = props.getProperty("weighting_scheme",_weighting_scheme);

	    stoplist = Boolean.valueOf(props.getProperty("stoplist",_stoplist)).booleanValue();
	    stoplist_name = props.getProperty("stoplist_name",_stoplist_name);
	    shortest_word = Integer.parseInt(props.getProperty("shortest_word",_shortest_word));
	    longest_word = Integer.parseInt(props.getProperty("longest_word",_longest_word));
	    minimum_words_per_file = Integer.parseInt(props.getProperty("minimum_words_per_file", _minimum_words_per_file));


	    restrict_extension = props.getProperty("restrict_extension",_restrict_extension);
	    strip_html = props.getProperty("strip_html",_strip_html);
	    discourse_marker = props.getProperty("discourse_marker",_discourse_marker);
	    granska = props.getProperty("granska",_granska);
	    save_preprocessed_text=props.getProperty("save_preprocessed_text",_save_preprocessed_text);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
    }
	
    public void writeProperties(OutputStreamWriter osw){
	try{
	    osw.write("dimensionality: " + dimensionality+"\n");
	    osw.write("random degree: " + randomDegree+"\n");
	    osw.write("seed: " + seed+"\n");
	    osw.write("left window size: " + leftWindowSize+"\n");
	    osw.write("right window size: " + rightWindowSize+"\n");
	    osw.write("weighting scheme: " + weightingScheme+"\n");
	    osw.write("stoplist: " + stoplist+"\n");
	    osw.write("stoplist_name: " + stoplist_name+"\n");
	    osw.write("shortest_word: " + shortest_word+"\n");
	    osw.write("longest_word: " + longest_word+"\n");
	    osw.write("minimum_words_per_file: " + minimum_words_per_file+"\n");
	    osw.flush();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
    }
}
