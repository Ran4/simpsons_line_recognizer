/*
 * TrainRI.java
 * Copyright (c) 2004, KTH NADA.
 *
 * This file is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * Martin Hassel, 2004-may-20
 * http://www.nada.kth.se/~xmartin/
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

import moj.granska.GranskaConnection;
import moj.ri.RandomIndex;
import moj.ri.SparseDistributedMemory;
import moj.ri.weighting.WeightingScheme;
import moj.util.TagStripper;

/**
 * A small but functional demo program that indexes a given text, or folder tree 
 * of texts, using RandomIndexing. In case of <code>OutOfMemoryError</code> the
 * index so far is dumped in a well-behaved manner, unfortunately this is not
 * the case (yet) when being prompted to abort (i.e. Ctrl-c).
 * The settings for preprocessing and indexing are located in a separate 
 * properties files (default values are declared in class header). 
 *
 * @author  Martin Hassel
 * @version 2004-aug-04
 */
public class TrainRI {
	// Default properties file
	static String propsfile = "TrainRI.properties";
	// Default values for the properties for RI
	static String _dimensionality = "1800";
	static String _random_degree  = "8";
	static String _seed = "710225";
	static String _left_window_size  = "4";
	static String _right_window_size = "4";
	static String _weighting_scheme = "moj.ri.weighting.MangesWS";
	static String _discourse_marker = "";
	static String _strip_html = "off";
	static String _restrict_extension = "";
	static String _save_compressed = "on";
	static String _granska = "off";
	static String _save_preprocessed_text = "off";
	// Global variables needed for shutdown hook
	static SparseDistributedMemory sdm;
	static String filename;

	public static void main(String arg[]) {
		// Shutdown hook that saves the index in case of OutOfMemoryException
        Thread hook = new Thread() {
    	    public void run() {
    	        System.out.println("Indexing aborted, dumping index so far to compressed file...");
    	        System.out.println("(this behaviour can be altered with the -Xrs JVM option)");
    			sdm.saveCompressed(filename);
    	    }
    	};
        if(arg.length > 1) {
			long time1, time2;
			float timediff;
			filename = arg[1];
			if(arg.length > 2)
				propsfile = arg[2];
			try {
				// Load and set properties for the RandomIndex
		    	Properties props = new Properties();
				props.load(new FileInputStream(propsfile));
				int dimensionality = Integer.parseInt(props.getProperty("dimensionality",_dimensionality));
				int randomDegree  = Integer.parseInt(props.getProperty("random_degree",_random_degree));
				int seed = Integer.parseInt(props.getProperty("seed",_seed));
				int leftWindowSize  = Integer.parseInt(props.getProperty("left_window_size",_left_window_size));
				int rightWindowSize = Integer.parseInt(props.getProperty("right_window_size",_right_window_size));
				String weightingScheme = props.getProperty("weighting_scheme",_weighting_scheme);
				// Print settings for current index, handy when running batch jobs
				System.out.println("Creating RandomIndex '" + arg[1] + "'");
				System.out.println("dimensionality: " + dimensionality);
				System.out.println("random degree: " + randomDegree);
				System.out.println("seed: " + seed);
				System.out.println("left window size: " + leftWindowSize);
				System.out.println("right window size: " + rightWindowSize);
				System.out.println("weighting scheme: " + weightingScheme);
				System.out.println("preprocessing: " + props.getProperty("granska",_granska) + "\n");
                try {
        			time1 = System.currentTimeMillis();
        			// First we need a WeightingScheme that tells the RandomIndex/SDM
        			// how to weight the context window when updating context labels
                    Class cls = Class.forName(weightingScheme);
                    WeightingScheme ws = (WeightingScheme)cls.newInstance();
        			// And then we need the actual RandomIndex (here in the form of SparseDistributedMemory
        			// which is the RandomIndex extended with load/save functionality etc.)
                    sdm = new SparseDistributedMemory(dimensionality, 
        					randomDegree, seed, leftWindowSize, rightWindowSize, ws);
                    Runtime.getRuntime().addShutdownHook(hook);
        			// Now we can recursivly walk through and Random Index the files in the given folder
        			TrainRI.recurseInDirFrom(arg[0], sdm, props);
        			time2 = System.currentTimeMillis();
        			// And print some stats about the index we just built
        			System.out.println("");
        			timediff = (float)(time2-time1)/60000;
        			System.out.println("Indexing file(s) took " + timediff + " minutes");
        			System.out.println(sdm.getWordsIndexed() + " words indexed");
        			System.out.println(sdm.size() + " wordforms/index terms");
        			// Finally we save the compressed index to the given (path+)filename
        			Runtime.getRuntime().removeShutdownHook(hook);
        			time1 = System.currentTimeMillis();
					if(props.getProperty("save_compressed",_save_compressed).compareToIgnoreCase("on")==0)
	        			sdm.saveCompressed(arg[1]);
					else
	        			sdm.save(arg[1]);
        			time2 = System.currentTimeMillis();
        			timediff = (float)(time2-time1)/1000;
        			System.out.println("Saving of RandomIndex took " + timediff + " seconds\n");
                } catch (ClassNotFoundException ex) {
                    System.err.println(ex);
                    ex.printStackTrace();
                }
			} catch (Exception ex) {
				System.err.println("Can't load " + propsfile + ", will exit...");
			}
		} else {
			System.out.println("*** TrainRI ***");
			System.out.println("Usage: TrainRI <data> <file to save to> (<properties file>)");
			System.out.println("<data> : file or folder to recursivly Random Index files in");
			System.out.println("<file to save to> : filename (without extension) to save the RandomIndex to");
			System.out.println("<properties file> : properties file to read settings from (optional)");
			System.out.println("\nFor example:\njava -Xmx100m -cp bin:lib TrainRI data rodarummet");
		}
		Runtime.getRuntime().removeShutdownHook(hook);
	}

	public static void recurseInDirFrom(String dirItem, RandomIndex ri, Properties props) {
		File file;
		String list[];	
		file = new File(dirItem);
		// Recurse through folders in folder
		if(file.isDirectory()) {
			list = file.list();
			for(int i = 0; i < list.length; i++)
				recurseInDirFrom(dirItem + File.separatorChar + list[i], ri, props);
		}

		// Random Index files (with correct extension) in folder
		File fileItem = new File(dirItem);
		if(fileItem.isFile() && 
				dirItem.endsWith(props.getProperty("restrict_extension",_restrict_extension))) {
			try {
				System.out.print(dirItem + " ... ");
				String line = null;
				// Read current file into a StringBuffer
				StringBuffer doc = new StringBuffer();
				BufferedReader txtbr = new BufferedReader(new FileReader(dirItem));
				// Append lines to form one "single" line (with line breaks)
				while((line = txtbr.readLine()) != null) {
					doc.append(line + " \n");
				}
				txtbr.close();
				try {
					String text = doc.toString();
					if(props.getProperty("strip_html",_strip_html).compareToIgnoreCase("on")==0) {
						// Remove any HTML _tags_ from the file (not comments or code, yet)
						TagStripper ts = new TagStripper();
						text = ts.stripTags(text);
					}
					// Each discourse segment is treated as a separate document
					String[] discourseSegments;
					String discourseMarker = props.getProperty("discourse_marker",_discourse_marker);
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
						String cgranska = props.getProperty("granska",_granska);
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
						// Add preprocessed text to Random Index
						System.out.println(ri.addText(text) + " words added");
						if(props.getProperty("save_preprocessed_text",_save_preprocessed_text).compareToIgnoreCase("on")==0) {
							// Save preprocessed text to file for later use
					        try {
					            PrintWriter pr = new PrintWriter(new BufferedWriter(new FileWriter(
					            		dirItem + "." + cgranska + i + ".txt", false)));
					            pr.print(text);
					            pr.flush();
					            pr.close();
					        } catch(java.io.IOException ex) {
								System.err.println(ex);
					            ex.printStackTrace();
					        }
						}
					}
				} catch(Exception ex) {
					System.err.println(ex);
		            ex.printStackTrace();
				}
			} catch(Exception ex) {
				System.err.println(ex);
	            ex.printStackTrace();
			}
		}
	}
}
