/*
 * SparseDistributedMemory.java
 * Copyright (c) 2004, KTH NADA.
 *
 * This file is part of SweSum^2 (see http://swesum.nada.kth.se/),
 * and is free software; you can redistribute it and/or
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
 * Martin Hassel, 2004-feb-01
 * http://www.nada.kth.se/~xmartin/
 *
 */

package moj.ri;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import moj.ri.weighting.WeightingScheme;
import moj.util.EntryValueComparator;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * SparseDistributedMemory extends RandomIndex with saving and loading of the
 * RandomIndex. The index is saved as a <b>huge</b> XML-file so saving/loading
 * directly to/from a zip compressed archive is also provided (usually results
 * in a compression rate of about 98% without loss of speed, but be prepared
 * for that converting all those byte and float vectors to strings <i>does</i>
 * take time.)
 * 
 * SparseDistributedMemory also extends RandomIndex with some useful functions
 * for extracting different types of ordered subsets of the RandomIndex.
 *
 * @author  Martin Hassel
 * @version 2004-may-15
 */
public class SparseDistributedMemory extends RandomIndex {

    /**
     * Create a new SparseDistributedMemory of RandomLabels with the given dimensionality,
     * degree of initial randomness and window size for contextual updates.
     * @param dimensionality The dimensionality all RandomLabels in the SparseDistributedMemory
     *        should have, this can not be altered at a later state.
     * @param randomDegree The number of random values all RandomLabels in the
     *        SparseDistributedMemory initially should have.
     * @param seed a seed for each label's random generator. This seed, in combination
     *        with the word, makes it very likely that the created random label is
     *        unique yet reproducable.
     * @param leftWindowSize The maximum number of words behind the focus word
     *        to include in the context window when updating a label.
     * @param rightWindowSize The maximum number of words in front of the focus
     *        word to include in the context window when updating a label.
     * @param weightingScheme a visiting object defining the weighting of the context
     *        labels to the left and to the right of the focus word.
     */
    public SparseDistributedMemory(int dimensionality, int randomDegree, int seed, 
    		int leftWindowSize, int rightWindowSize, WeightingScheme weightingScheme) {
        super(dimensionality, randomDegree, seed, leftWindowSize, rightWindowSize, weightingScheme);
    }

    /**
     * Create a new SparseDistributedMemory of RandomLabels with a dimensionality of 1800,
     * a degree of initial randomness of 8 and a window size for contextual
     * updates of 3 (i.e. three words look-behind and look-ahead).
     */
    public SparseDistributedMemory() {
        super();
    }

    /**
     * Load RanhdomIndex from file.
     * @param source InputSource from (compressed) file, URL etc.
     * @return number of <code>RandomLabel</code>s loaded.
     */
    private int load(InputSource source) {
        try {
            // Create parser+contentHandler and read the XML document
            XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            labelHandler handler = new labelHandler();
            parser.setContentHandler(handler);
            parser.parse(source);
            // Set internal variables from values read into the XML labelHandler
            _randomIndex = handler.randomIndex;
            _dimensionality = handler.dimensionality;
            _randomDegree = handler.randomDegree;
            _seed = handler.randomSeed;
            _leftWindowSize = handler.leftWindowSize;
            _rightWindowSize = handler.rightWindowSize;
            try {
                Class cls = Class.forName(handler.weightingScheme);
                _weightingScheme = (WeightingScheme)cls.newInstance();
            } catch (ClassNotFoundException ex) {
                System.err.println(ex);
                ex.printStackTrace();
            }
            _wordsIndexed = handler.wordsIndexed;
            _documentsIndexed = handler.documentsIndexed;
            _allLowerCase = handler.allLowercase;
            return _randomIndex.size();
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Load RandomIndex from file. If non-compressed compressed file is not 
     * fount it tries to load compressed file at the same location.
     * @param filename the path and filename (without extension, '.xml' will be
     * added) that the RandomIndex is to be saved to.
     * @return number of <code>RandomLabel</code>s loaded.
     */
    public int load(String filename) {
    	File filetest = new File(filename+".xml");
        if(filetest.exists()) {
        	try {
        		BufferedReader reader = new BufferedReader(new FileReader(filename+".xml"));
        		InputSource source = new InputSource(reader);
        		return this.load(source);
        	} catch (Exception ex) {
        		System.err.println(ex);
        		ex.printStackTrace();
        	}
        } else {
        	filetest = new File(filename+".xml.zip");
            if(filetest.exists()) {
            	return this.loadCompressed(filename);
            }
        } 
        System.err.println("Neither '"+filename+".xml' nor '"+filename+".xml.zip' was found.");
        return 0;
    }

    /**
     * Load RandomIndex from compressed file (i.e. zip archive).
     * @param filename the path and filename (without extension, '.xml' will be
     * added) that the RandomIndex is to be saved to.
     * @return number of <code>RandomLabel</code>s loaded.
     */
    public int loadCompressed(String filename) {
        int labels_read = 0;
        try {
            // Create a ZipInputStream to read the zip file
            FileInputStream fis = new FileInputStream(filename+".xml.zip");
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            InputSource source = new InputSource(zis);
            ZipEntry entry;
            if((entry = zis.getNextEntry()) != null) {
                if(!entry.isDirectory()) {
                    labels_read = this.load(source);
                }
            }
            zis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return labels_read;
    }

    private int save(PrintWriter savefile, boolean progressIndicator) {
        Set keys = _randomIndex.keySet();
        if(keys.size() != 0) {
            StringBuffer xml = new StringBuffer();
            xml.append("<?xml version='1.0' encoding='ISO-8859-1' standalone=\"yes\"?>\n\n");
            xml.append("<!--  " + keys.size() + " Random Labels  -->\n\n");
            // DTD given in XML prolog to keep everything in one nice and tidy file
            xml.append("<!DOCTYPE randomlabels [\n");
            xml.append("\t<!ELEMENT randomlabels (rl*)>\n");
            xml.append("\t<!ATTLIST randomlabels labels CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels dimensionality CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels randomness CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels seed CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels leftWindowSize CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels rightWindowSize CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels weightingScheme CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels wordsIndexed CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels documentsIndexed CDATA #REQUIRED>\n");
            xml.append("\t<!ATTLIST randomlabels allLowercase CDATA #REQUIRED>\n");
            xml.append("\t<!ELEMENT rl (wd, tf, df, ll, cv)>\n");
            xml.append("\t<!ELEMENT wd (#PCDATA)>\n");
            xml.append("\t<!ELEMENT tf (#PCDATA)>\n");
            xml.append("\t<!ELEMENT df (#PCDATA)>\n");
            xml.append("\t<!ELEMENT ll (neg, pos)>\n");
            xml.append("\t<!ELEMENT neg (#PCDATA)>\n");
            xml.append("\t<!ELEMENT pos (#PCDATA)>\n");
            xml.append("\t<!ELEMENT cv (#PCDATA)>\n");
            xml.append("]>\n\n");
            // Legend explaining space saving abbreviated tag names
            xml.append("<!--  rl = Random Label  -->\n");
            xml.append("<!--  wd = word  -->\n");
            xml.append("<!--  tf = term frequency  -->\n");
            xml.append("<!--  df = document frequency  -->\n");
            xml.append("<!--  ll = label (i.e. the random label associated with the word)  -->\n");
            xml.append("<!--  neg = negative positions in label (i.e. positions holding -1:s)  -->\n");
            xml.append("<!--  pos = positive positions in label (i.e. positions holding 1:s)  -->\n");
            xml.append("<!--  cv = context vector (i.e. the contextual 'flavour' of the word)  -->\n\n");
            // Write ROOT element
            xml.append("<randomlabels\n");
            xml.append("\tlabels=\"" + keys.size() + "\"\n");
            xml.append("\tdimensionality=\"" + _dimensionality + "\"\n");
            xml.append("\trandomness=\"" + _randomDegree + "\"\n");
            xml.append("\tseed=\"" + _seed + "\"\n");
            xml.append("\tleftWindowSize=\"" + _leftWindowSize + "\"\n");
            xml.append("\trightWindowSize=\"" + _rightWindowSize + "\"\n");
    		xml.append("\tweightingScheme=\"" + _weightingScheme.getClass().getName() + "\"\n");
    		xml.append("\twordsIndexed=\"" + _wordsIndexed + "\"\n");
            xml.append("\tdocumentsIndexed=\"" + _documentsIndexed + "\"\n");
            xml.append("\tallLowercase=\"" + _allLowerCase + "\"\n");
            xml.append("\t>\n\n");
            savefile.print(xml.toString());
            // Write all Random Labels
        	byte rlabel[];
            float context[];
            Iterator kit = keys.iterator();
            while(kit.hasNext()) {
                xml.delete(0, xml.length());
                RandomLabel label = (RandomLabel)_randomIndex.get(kit.next());
                xml.append("\t<rl>\n");
                xml.append("\t\t<wd>" + label.getWord() + "</wd>\n");
                xml.append("\t\t<tf>" + label.getTermFrequency() + "</tf>\n");
                xml.append("\t\t<df>" + label.getDocumentFrequency() + "</df>\n");
                xml.append("\t\t<ll>\n");
                xml.append("\t\t\t<neg>");
        		int[] negs = label.getNegativePositions();
        		for(int current = 0; current < negs.length-1; current++)
        		    xml.append(negs[current]+",");
        		xml.append(negs[negs.length-1]);
        		xml.append("</neg>\n");
        		xml.append("\t\t\t<pos>");
        		int[] poss = label.getPositivePositions();
        		for(int current = 0; current < poss.length-1; current++)
        		    xml.append(poss[current]+",");
        		xml.append(poss[poss.length-1]);
        		xml.append("</pos>\n");
                xml.append("\t\t</ll>\n");
                xml.append("\t\t<cv>");
                context = label.getContext();
                for(int current = 0; current < context.length; current++) {
                    xml.append(context[current]);
                    if(current < context.length-1)
                        xml.append(",");
                }
                xml.append("</cv>\n");
                xml.append("\t</rl>\n\n");
                savefile.print(xml.toString());
                if(progressIndicator)
                    System.out.print(".");
            }
            savefile.print("</randomlabels>\n");
            savefile.flush();
            if(progressIndicator)
                System.out.println("");
        }
        return keys.size();
    }

    /**
     * Saves the <code>RandomIndex</code> to the given <code>filename</code> 
     * with the extension ".xml" added.
     * @param filename file with full path to save the <code>RandomIndex</code> to.
     * @return number of <code>RandomLabel</code>s that were saved.
     */
    public int save(String filename) {
        boolean progressIndicator = false;
        int labels_saved = 0;
        try {
            PrintWriter pr = new PrintWriter(new BufferedWriter(new FileWriter(filename+".xml", false)));
            labels_saved = this.save(pr, progressIndicator);
            pr.close();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        return labels_saved;
    }

    /**
     * Saves the <code>RandomIndex</code> to the given <code>filename</code> as  
     * a zip-file with the extension ".xml.zip" added.
     * @param filename file with full path to save the compressed 
     *        <code>RandomIndex</code> to.
     * @return number of <code>RandomLabel</code>s that were saved.
     */
    public int saveCompressed(String filename) {
        boolean progressIndicator = false;
        int labels_saved = 0;
        try {
            // Create zip compressed output stream, zip file entry and PrintWriter
            ZipOutputStream targetStream = new ZipOutputStream(new FileOutputStream(filename+".xml.zip"));
            String _filename[] = filename.split("/");
            targetStream.putNextEntry(new ZipEntry(_filename[_filename.length-1]+".xml"));
            PrintWriter pr = new PrintWriter(targetStream);
            // Save RandomIndex in zip compressed format (single entry/file)
            labels_saved = this.save(pr, progressIndicator);
            targetStream.setComment(labels_saved + " RandomLabels in archive...");
            // Close compressed output stream and PrintWriter
            targetStream.closeEntry();
            targetStream.close();
            targetStream = null;
            pr.close();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        return labels_saved;
    }

    /**
     * Adds text from a text file to the <code>RandomIndex</code>, i.e. the 
     * text in the text file is read and words, in the order they are 
     * encountered in the text, are added to the <code>RandomIndex</code> 
     * if they aren't already represented in the index and contextually 
     * updated if they are already present.
     * @param filename name, with full path, of the text file which text is to be 
     *        added to the <code>RandomIndex</code> from.
     * @return number of words (index terms) added/updated.
     */
    public int addTextFromFile(String filename) {
        StringBuffer text = new StringBuffer();
        try {
            String line = null;
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        } catch (java.io.FileNotFoundException ex) {
            System.out.println("File '" + filename + "' does not exist.");
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        return this.addText(text.toString());
    }

    /**
     * Generate a set of "semantic relatives" for a given <code>word</code>.
     * The returned String array contains the <code>setSize</code> closest index 
     * terms sorted by <i>cosine</i> together with their respective Euclidean 
     * distance to the given <code>word</code>. However, if the SparseDistributedMemory 
     * is empty, that is <code>isEmpty() == true</code>, a zero sized String array 
     * will be returned.
     * @param word index term we want to generate a set of semantic relatives for.
     * @param setSize number of desired members of the generated set of semantic relatives.
     * @param minTermFrequency minimum <code>TermFrequency</code> required for a semantic
     *        relative to be included in the set. 
     * @param maxTermFrequency maximum <code>TermFrequency</code> allowed for a semantic
     *        relative to be included in the set. 
     * @return a String array of size <code>setSize</code> if <code>setSize <= 
     *         SparseDistributedMemory.size()</code>, otherwise it returns a 
     *         String array of size <code>SparseDistributedMemory.size()</code>.
     */
    public String[] getCorrelations(String word, int setSize, long minTermFrequency, long maxTermFrequency) {
        // If the RandomIndex does not contain the word, return empty array
        if(_allLowerCase) word = word.toLowerCase();
        if(!_randomIndex.containsKey(word)) {
            String[] empty = new String[0];
            return empty;
        }
        String semanticSet[] = new String[setSize];

        Set keys = _randomIndex.keySet();
        if(keys.size() > 0) {
            Float cosine = new Float(0);
            Map indexTerms = new Hashtable();
            RandomLabel wordLabel = (RandomLabel)_randomIndex.get(word);

            Iterator kit = keys.iterator();
            while(kit.hasNext()) {
                RandomLabel keyLabel = (RandomLabel)_randomIndex.get(kit.next());
                cosine = new Float(wordLabel.cosineSim(keyLabel));
                // If the word falls within the supplied term frequency range we add it to the set of candidates
                if(keyLabel.getTermFrequency() > minTermFrequency && keyLabel.getTermFrequency() < maxTermFrequency)
                    indexTerms.put(keyLabel.getWord(), cosine);
            }

            SortedSet sortedIndexTerms = new TreeSet(new EntryValueComparator());
            sortedIndexTerms.addAll(indexTerms.entrySet());

            List sortedTermList = new LinkedList(sortedIndexTerms);
            Collections.reverse(sortedTermList);

            Iterator it = sortedTermList.iterator();
            for(int i = 0; it.hasNext() && i < semanticSet.length; i++) {
                semanticSet[i] = it.next() + "";
            }
        }

        return semanticSet;
    }

    /**
     * Generate a set of "semantic relatives" for a given <code>word</code>.
     * The returned String array contains the <code>setSize</code> closest index 
     * terms sorted by <i>cosine</i> together with their respective Euclidean 
     * distance to the given <code>word</code>. However, if the SparseDistributedMemory 
     * is empty, that is <code>isEmpty() == true</code>, a zero sized String array 
     * will be returned.
     * @param word index term we want to get a set of "semantic relatives" for.
     * @param setSize number of desired members of the generated set of semantic relatives.
     * @return a String array of size <code>setSize</code> if <code>setSize <= 
     *         SparseDistributedMemory.size()</code>, otherwise it returns a 
     *         String array of size <code>SparseDistributedMemory.size()</code>.
     *         The String array contains the <code>setSize</code> closest index terms 
     *         sorted by <code>cosine</code> together with their respective Euclidean 
     *         distance to the given <code>word</code>. However, if the SparseDistributedMemory 
     *         is empty, that is <code>isEmpty() == true</code>, a zero sized String array 
     *         will be returned.
     */
    public String[] getCorrelations(String word, int setSize) {
        return this.getCorrelations(word, setSize, 0, Long.MAX_VALUE);
    }

    /**
     * Generate a set of "semantic relatives" for a given <code>word</code>.
     * The returned String array contains the <code>setSize</code> closest index 
     * terms sorted by <i>cosine</i> together with their respective Euclidean 
     * distance to the given <code>word</code>. However, if the SparseDistributedMemory 
     * is empty, that is <code>isEmpty() == true</code>, a zero sized String array 
     * will be returned.
     * @param word index term we want to get a set of 10 "semantic relatives" for.
     * @return a String array of size 10 if <code>SparseDistributedMemory.size()
     *         >= 10</code>, otherwise it returns a String array of size 
     *         <code>SparseDistributedMemory.size()</code>. The String array 
     *         contains the 10 (or less) closest index terms sorted by 
     *         <code>cosine</code> together with their respective Euclidean 
     *         distance to the given <code>word</code>. However, if the 
     *         SparseDistributedMemory is empty, that is <code>isEmpty() == 
     *         true</code>, a zero sized String array will be returned.
     */
    public String[] getCorrelations(String word) {
        return this.getCorrelations(word, 10, 0, Long.MAX_VALUE);
    }

    /**
     * Generate the <code>setSize</code> best "descriptors" (i.e. the index terms 
     * with the highest information value according to tf*idf).
     * @param setSize number of desired members of the generated set of top ranking
     *        index terms.
     * @return a String array of size <code>setSize</code> if <code>setSize</code> 
     *         <= <code>SparseDistributedMemory.size()</code>, otherwise it returns 
     *         a String array of size <code>SparseDistributedMemory.size()</code>.
     *         The String array contains the top index terms sorted by <code>tf*idf</code> 
     *         together with their <code>tf*idf</code> value ("term=value").
     *         However, if the SparseDistributedMemory is empty, that is 
     *         <code>isEmpty() == true</code>, a zero sized String array will be returned.
     */
    public String[] getTfIDfRank(int setSize) {
        Set keys = _randomIndex.keySet();
        String[] tfidfSet;

        if(keys.size() < setSize)
            tfidfSet = new String[keys.size()];
        else
            tfidfSet = new String[setSize];

        if(keys.size() != 0) {
            Map indexTerms = new Hashtable();
            Iterator kit = keys.iterator();
            while(kit.hasNext()) {
                RandomLabel keyLabel = (RandomLabel)_randomIndex.get(kit.next());
   				// Calculate Inverse Document Frequency (log2 N/n)
                double idf = Math.log(_documentsIndexed/keyLabel.getDocumentFrequency())/Math.log(2);
                Double tfidf = new Double(keyLabel.getTermFrequency()*idf);
                indexTerms.put(keyLabel.getWord(), tfidf);
            }

            SortedSet sortedIndexTerms = new TreeSet(new EntryValueComparator());
            sortedIndexTerms.addAll(indexTerms.entrySet());

            List sortedTermList = new LinkedList(sortedIndexTerms);
            Collections.reverse(sortedTermList);

            Iterator it = sortedTermList.iterator();
            for(int i = 0; it.hasNext() && i < tfidfSet.length; i++) {
                tfidfSet[i] = it.next() + "";
            }
        }
        return tfidfSet;
    }

    /**
     * Get a Document Vector, with the same dimensionality as the <code>RandomLabel</code>s 
     * in the <code>SparseDistributedMemory</code>, representing all <code>word</code>s 
     * in <code>document</code> that are present in the SDM. This vector is weighted 
     * with the supplied <code>weight</code>s in the <<code>word</code>,
     * <code>weight</code>>-pairs in <code>document</code>.
     * @param document a <code>Map</code> containing <<code>word</code>, 
     *        <code>weight</code>>-pairs where the <code>weight</code> should 
     *        be an object of type <code>Number</code>.
     * @param idfWeighting <code>true</code> if weighting should be modified 
     *        with the Inverse Document Frequency <i>(log2 N/n)</i>. In this case 
     *        the <code>weight</code> in the <<code>word</code>,<code>weight</code>>-pairs 
     *        could be used to represent the <code>word</code>'s Term Frequency 
     *        within the document.
     * @return a vector of <code>float</code>s representing the Document Vector.
     */
    public float[] getDocumentVector(Map document, boolean idfWeighting) {
        float[] docContext = new float[_dimensionality];
    	Iterator docIt = document.keySet().iterator();
        while(docIt.hasNext()) {
            String word = (String)docIt.next();
       		float weight = ((Number)document.get(word)).floatValue() ;
            RandomLabel label = this.getRandomLabel(word);
       		float[] tempContext = label.getContext();
       		for(int i = 0; i < tempContext.length; i++) {
       			if(idfWeighting) {
       				// Calculate Inverse Document Frequency (log2 N/n)
       				double idf = Math.log(_documentsIndexed/label.getDocumentFrequency())/Math.log(2);
       				docContext[i] += (tempContext[i] * weight * idf);
       			} else
       				docContext[i] += (tempContext[i] * weight);
        	}
        }
    	return docContext;
    }

    // Nested class to handle XML parsing
    private class labelHandler extends DefaultHandler {
        // These are per RandomIndex, used as 'return values' and therefore public
        public Map randomIndex = new HashMap();
        public int dimensionality = 0;
        public int randomDegree = 0;
        public int randomSeed = 0;
        public int leftWindowSize = 0;
        public int rightWindowSize = 0;
        public String weightingScheme = "";
        public long wordsIndexed = 0;
        public long documentsIndexed = 0;
        public boolean allLowercase = true;
        // These are per RandomLabel read from the XML
        private String _word;        // The word the 'label' is to be attached to
        private long _termfrequency; // Number of updates done to the label
        private int  _docfrequency;  // Number of documents the word occurs in
    	private int _negs[]; // Posititions for -1:s in the random label
    	private int _poss[]; // Posititions for 1:s in the random label
        private float _context[];    // The actual contextually updated label
        // Buffer to read chunk data into
        private StringBuffer buffer = null;
        private boolean progressIndicator = false;
		private int no_pos_and_neg;

        public void startElement(String namespaceURI, String localName,
                String qualifiedName, Attributes atts) throws SAXException {
            buffer = new StringBuffer();

            if(localName.equals("randomlabels")) {
                dimensionality = Integer.parseInt(atts.getValue("dimensionality"));
                randomDegree = Integer.parseInt(atts.getValue("randomness"));
                randomSeed = Integer.parseInt(atts.getValue("seed"));
                leftWindowSize = Integer.parseInt(atts.getValue("leftWindowSize"));
                rightWindowSize = Integer.parseInt(atts.getValue("rightWindowSize"));
                weightingScheme = atts.getValue("weightingScheme");
                documentsIndexed = Long.parseLong(atts.getValue("documentsIndexed"));
                allLowercase = (atts.getValue("allLowercase").compareToIgnoreCase("true")==0)
                    ? true : false;
                // Unused so far but enhances the informativeness of the XML to human eyes
                // Could be used for content validating beyond well-formedness
                // Long.parseLong(atts.getValue("wordsIndexed"));
                // Long.parseLong(atts.getValue("labels"));
                no_pos_and_neg = Math.round(randomDegree/2);
            }

            if(localName.equals("rl")) {
                _word = "";
                _termfrequency = 0;
                _docfrequency = 0;
        		_negs = new int[no_pos_and_neg];
        		_poss = new int[no_pos_and_neg];
                _context = new float[_dimensionality];
            }
        }

        public void endElement(String namespaceURI, String localName,
                String qualifiedName) throws SAXException {
            String chunk = buffer.toString();

            if(localName.equals("wd"))
                _word = chunk;

            if(localName.equals("tf"))
                _termfrequency = Long.parseLong(chunk);

            if(localName.equals("df"))
                _docfrequency  = Integer.parseInt(chunk);

            if(localName.equals("neg")) {
                String data_array[] = chunk.split(",");
                for(int i = 0; i < data_array.length; i++){
                    _negs[i] = Integer.parseInt(data_array[i]);
                }
            }

            if(localName.equals("pos")) {
                String data_array[] = chunk.split(",");
                for(int i = 0; i < data_array.length; i++)
                    _poss[i] = Integer.parseInt(data_array[i]);
            }

            if(localName.equals("cv")) {
                String data_array[] = chunk.split(",");
                for(int i = 0; i < data_array.length; i++)
                    _context[i] = Float.valueOf(data_array[i]).floatValue();
            }

            if(localName.equals("rl")) {
                RandomLabel rlabel = new RandomLabel(_word, _termfrequency, _docfrequency, _negs, _poss, _context);
                randomIndex.put(_word, rlabel);
                wordsIndexed += _termfrequency;
                if(progressIndicator)
                    System.out.print(".");
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if(buffer != null)
                buffer.append(ch, start, length);
        }

        public void endDocument() {
            if(progressIndicator)
                System.out.print("\n");
        }

    } // End nested class
}
