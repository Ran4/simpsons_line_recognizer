/*
 * RandomIndex.java
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import moj.ri.weighting.MangesWS;
import moj.ri.weighting.WeightingScheme;

/**
 * A Random Index is an index that contextually indexes the texts that are fed
 * to it. Not only is it possible to ask the RandomIndex for the term frequency
 * for a specific index term, but also for its semantically closest relatives
 * (based upon co-occurrence statistics and random labels).
 *
 * @author  Martin Hassel
 * @version 2004-may-09
 */
public class RandomIndex {
    /** The dimensionality of newly created RandomLabels */
    protected int _dimensionality;
    /** Number of non-zeros in newly created RandomLabels */
    protected int _randomDegree;
    /** Seed used in conjunction with the word to generate random labels */
    protected int _seed;
    /** Size of the context window to the left of the focus word */
    protected int _leftWindowSize;
    /** Size of the context window to the right of the focus word */
    protected int _rightWindowSize;
    /** Weighting Scheme applied to the context label when a random label's context is updated */
    protected WeightingScheme _weightingScheme;
    /** The actual RandomIndex */
    protected Map _randomIndex = new HashMap();
    /** The number of <i>word instances</i> indexed so far */
    protected long _wordsIndexed = 0;
    /** The number of <i>documents</i> indexed so far */
    protected long _documentsIndexed = 0;
    /** Should indexed words be all lowercase? */
    protected boolean _allLowerCase = true;

    /**
     * Create a new RandomIndex of RandomLabels with the given dimensionality,
     * degree of initial randomness and window size for contextual updates.
     * @param dimensionality The dimensionality all RandomLabels in the RandomIndex
     *        should have, this can not be altered at a later state.
     * @param randomDegree The number of random values all RandomLabels in the
     *        RandomIndex initially should have.
     * @param seed a seed for each label's random generator. This seed, in combination
     *        with the word, makes it very likely that the created random label is
     *        "unique" yet reproducable.
     * @param leftWindowSize The maximum number of words behind the focus word
     *        to include in the context window when updating a label.
     * @param rightWindowSize The maximum number of words in front of the focus
     *        word to include in the context window when updating a label.
     * @param weightingScheme a visiting object defining the weighting of the context
     *        labels to the left and to the right of the focus word.
     */
    public RandomIndex(int dimensionality, int randomDegree, int seed, 
    		int leftWindowSize, int rightWindowSize, WeightingScheme weightingScheme) {
        _dimensionality  = dimensionality;
        _randomDegree   = randomDegree;
        _seed = seed;
        _leftWindowSize  = leftWindowSize;
        _rightWindowSize = rightWindowSize;
        _weightingScheme  = weightingScheme;
    }

    /**
     * Create a new RandomIndex of RandomLabels with a dimensionality of 1800,
     * a degree of initial randomness of 8 and a window size for contextual
     * updates of 3 (i.e. three words look-behind and look-ahead). The seed
     * for randomization is set to 123.
     */
    public RandomIndex() {
        _dimensionality = 1800;
        _randomDegree  = 8;
        _seed = 123;
        _leftWindowSize  = 3;
        _rightWindowSize = 3;
        _weightingScheme  = new MangesWS(); 
    }

    /**
     * Returns a set view of the keys contained in this <code>RandomIndex</code>. 
     * The set is backed by the <code>RandomIndex</code>, so changes to the 
     * <code>RandomIndex</code> are reflected in the set, and vice-versa. 
     * If the <code>RandomIndex</code> is modified while an iteration over the 
     * set is in progress, the results of the iteration are undefined.
     * @return a set view of the keys contained in this <code>RandomIndex</code>.
     */
    public Set keySet() {
    	return _randomIndex.keySet();
    }

    /**
     * Add an existing RandomLabel to the RandomIndex. Will only succeed if
     * the word (index term) associated with the label does not yet exist in
     * the RandomIndex and the label has the same dimensionality as is set
     * for the RandomIndex.
     * @param label RandomLabel to be added.
     * @return <code>true</code> upon success, else <code>false</code>.
     */
    public boolean addRandomLabel(RandomLabel label) {
        if(label.getDimensionality() != _dimensionality) return false;
        if(_randomIndex.containsKey(label.getWord())) return false;
        _randomIndex.put(label.getWord(),label);
        _wordsIndexed += label.getTermFrequency();
        return true;
    }

    /**
     * Add an existing RandomLabel to the RandomIndex. Will only succeed if
     * the word (index term) associated with the label does not yet exist in
     * the RandomIndex and the label has the same dimensionality as is set
     * for the RandomIndex.
     * @param data A comma separated String containing the word in the first column,
     *        the frequency in the second and the actual label in the following. This 
     *        is the same format that is returned by <code>RandomLabel.toString();</code>
     * @return <code>true</code> upon success, else <code>false</code>.
     */
/*    public boolean addRandomLabel(String data) {
        String data_array[] = data.split(",");
        String word = data_array[0];
        long termfrequency = Long.parseLong(data_array[1]);
        int  docfrequency  = Integer.parseInt(data_array[2]);
        int dimensionality = Integer.parseInt(data_array[3]);

        byte label[] = new byte[dimensionality];
        for(int i = 4; i < 4+dimensionality; i++)
            label[i-4] = Byte.valueOf(data_array[i]).byteValue();

        float context[] = new float[data_array.length-(4+dimensionality)];
        for(int i = 4+dimensionality; i < data_array.length; i++)
            context[i-4] = Float.valueOf(data_array[i]).floatValue();

        RandomLabel rlabel = new RandomLabel(word, termfrequency, docfrequency, label, context);
        return this.addRandomLabel(rlabel);
    } */

    /**
     * Sets the RandomIndex to henceforth automagically convert all words
     * (index terms) to all lower case and returns the previous state. Index terms
     * already in the index will <i>not</i> be changed.
     * @param allLowercase boolean value denoting the wish to have all index terms henceforth
     *        converted to lower case of not.
     * @return the previous state, i.e. either <code>true</code> or <code>false</code>.
     */
    public boolean setAllLowerCase(boolean allLowercase) {
        boolean retallLowercase = _allLowerCase;
        _allLowerCase = allLowercase;
        return retallLowercase;
    }

    /**
     * Gets the state of the RandomIndex denoting the wish to henceforth
     * automagically convert all words (index terms) to all lower case.
     * @return the current state, i.e. either true or false.
     */
    public boolean getAllLowerCase() {
        return _allLowerCase;
    }

    /**
     * Gets the dimensionality of RandomLabels in the RandomIndex.
     * @return the dimensionality of RandomLabels in the RandomIndex.
     */
    public int getDimensionality() {
        return _dimensionality;
    }

	/**
	 * Gets the random degree of RandomLabels in the RandomIndex.
	 * @return the random degree of RandomLabels in the RandomIndex.
	 */
    public int getRandomDegree() {
		return _randomDegree;
	}

    /**
	 * Gets the seed used to create RandomLabels in the RandomIndex.
	 * @return the seed used to create RandomLabels in the RandomIndex.
	 */
	public int getSeed() {
		return _seed;
	}

	/**
	 * Gets the size of the context window to the left used when updating 
	 * context labels in RandonLabels in the RandomIndex.
	 * @return the size of the context window to the left.
	 */
	public int getLeftWindowSize() {
		return _leftWindowSize;
	}

	/**
	 * Gets the size of the context window to the right used when updating 
	 * context labels in RandonLabels in the RandomIndex.
	 * @return the size of the context window to the right.
	 */
	public int getRightWindowSize() {
		return _rightWindowSize;
	}

	/**
	 * Gets the name (class) of the WeightingScheme used by the RandomIndex 
	 * when updating context labels in RandonLabels in the RandomIndex.
	 * @return the name (class) of the WeightingScheme used by the RandomIndex.
	 */
	public WeightingScheme getWeightingScheme() {
		return _weightingScheme;
	}

	/**
     * Gets the number of words (or index terms) indexed by the Random Index.
     * @return the number of words (or index terms) indexed by the Random Index.
     */
    public long getWordsIndexed() {
        return _wordsIndexed;
    }

    /**
     * Gets the number of documents indexed by the Random Index.
     * @return the number of documents indexed by the Random Index.
     */
    public long getDocumentsIndexed() {
        return _documentsIndexed;
    }

    /**
     * Add text to the Random Index and contextually update all words in, and
     * added to, the index. The words updated/added are contextually "coloured"
     * according to the set window size and weighting scheme.
     * @param text a text tokenized on word level where each element in the
     *        list <code>text</code> contains one token.
     * @return number of words (or index terms) added/updated.
     */
    public int addText(String[] text) {
        Set uniqueWords = new HashSet();
        // Traverse all words in array text and add/update them to/in the RandomIndex
        for(int current = 0; current < text.length; current++) {
            String word = text[current];

            // If all words are to be lowercased, then lowercase
            if(_allLowerCase) word = word.toLowerCase();
            uniqueWords.add(word);

            // Construct empty RandomLabel to ensure all zeros in context
            RandomLabel label = new RandomLabel("",_dimensionality,0,0);

            // Create left and right context window (arrays of RandomLabels)
            RandomLabel[] left = new RandomLabel[_leftWindowSize];
            for(int distance = 0; distance < _leftWindowSize; distance++) {
                // Do not go beyond left array boundary
                if((current-distance-1) < 0) {
                    left[distance] = label;
                } else {
                	if(_randomIndex.containsKey(text[current-distance-1])) {
                		 left[distance] = (RandomLabel)_randomIndex.get(text[current-distance-1]);
                	} else {
                		 // A word to the left is not yet in the index???
                		 left[distance] = label;
                	}
                }
            }
            RandomLabel[] right = new RandomLabel[_rightWindowSize];
            for(int distance = 0; distance < _rightWindowSize; distance++) {
                // Do not go beyond right array boundary
                if((current+distance+1) > text.length-1) {
                    right[distance] = label;
                } else {
                    if(_randomIndex.containsKey(text[current+distance+1])) {
                        right[distance] = (RandomLabel)_randomIndex.get(text[current+distance+1]);
                    } else {
                        // A word to the right is not yet in the index
                        right[distance] = label;
                    }
                }
            }

            // If the word already exists in the index, then it should be updated with its context
            if(_randomIndex.containsKey(word)) {
                label = (RandomLabel)_randomIndex.get(word);
            } else {
                // String[] word_wc = word.split("_");
           		label = new RandomLabel(word, _dimensionality, _randomDegree, _seed);
            }
            label.updateContext(left,right,_weightingScheme);
            _randomIndex.put(word,label);
            _wordsIndexed++;
        }

        Iterator uwit = uniqueWords.iterator();
        while(uwit.hasNext()) {
            String word = (String)uwit.next();
            RandomLabel label = (RandomLabel)_randomIndex.get(word);
            label.incrementDocumentFrequency();
            _randomIndex.put(word,label);
        }
        _documentsIndexed++;
        return text.length;
    }

    /**
     * Add text to the Random Index and contextually update all words in, and
     * added to, the index, tokens are separated according to the supplied
     * pattern. The words updated/added are contextually "coloured"
     * according to the set window size and weighting scheme.
     * @param text a text that is to be tokenized according to the supplied
     *        <code>pattern</code>.
     * @param pattern a pattern according to which the text string is to be 
     *        split into tokens.
     * @return number of words (index terms) added/updated.
     */
    public int addText(String text, String pattern) {
        // If all words are to be lowercased, then lowercase
        if(_allLowerCase) { text = text.toLowerCase(); }
        return this.addText(text.split(pattern));
    }

    /**
     * Add text to the Random Index and contextually update all words in, and
     * added to, the index, tokens are separated by white-space. The words
     * updated/added are contextually "coloured" according to the set window
     * size and weighting scheme.
     * @param text a text that is to be tokenized according to the default
     *        pattern which is <code>"\\s"</code>.
     * @return number of words (index terms) added/updated.
     */
    public int addText(String text) {
        return this.addText(text, "\\s");
    }

    /**
     * Get the <code>RandomLabel</code> for the given <code>word</code> if it 
     * exists in the <code>RandomIndex</code>. If the <code>RandomIndex</code> 
     * does not contain a corrsponding <code>RandomLabel</code>, return a 
     * zero sized <code>RandomLabel</code>.
     * @param word the word in the <code>RandomIndex</code> that we want the 
     *        corresponding <code>RandomLabel</code> for.
     * @return the <code>RandomLabel<code> corresponding to the given 
     *         <code>word</code> if it exists in the <code>RandomIndex</code>, 
     *         if not a zero sized <code>RandomLabel</code> is returned.
     */
    public RandomLabel getRandomLabel(String word) {
        if(!_randomIndex.containsKey(word)) return new RandomLabel("",0,0,0);
        return (RandomLabel)_randomIndex.get(word);
    }

    /**
     * String representation of RandomIndex.
     * @return a string containing string representations of RandomLabels
     *         where each label is separated by a newline (the string also
     *         ends in newline).
     */
    public String toString() {
        StringBuffer retstring = new StringBuffer();
        Set keys = _randomIndex.keySet();
        if(keys.size() != 0) {
            Iterator kit = keys.iterator();
            while(kit.hasNext()) {
                retstring.append(_randomIndex.get(kit.next())+"\n");
            }
        }
        return retstring.toString();
    }

    /**
     * Returns <code>true</code> if this RandomIndex contains no elements.
     * @return <code>true</code> if this RandomIndex contains no elements, 
     *         otherwise false.
     */
    public boolean isEmpty() {
        return _randomIndex.isEmpty();
    }

    /**
     * Returns the number of Random Labes in the Random Index.
     * @return the number of Random Labes in the Random Index.
     */
    public int size() {
        return _randomIndex.size();
    }

    /**
     * Returns <code>true</code> if this RandomIndex contains the given word (index term).
     * @param word The word which existance in the RandomIndex is to be determined.
     * @return <code>true</code> if this set contains the word, otherwise false.
     */
    public boolean contains(String word) {
        return _randomIndex.containsKey(word);
    }

    /**
     * Returns <code>true</code> if this RandomIndex contains the given RandomLabel.
     * @param label The RandomLabel which existance in the RandomIndex is to be determined.
     * @return <code>true</code> if this set contains the RandomIndex, otherwise false.
     */
    public boolean contains(RandomLabel label) {
        return this.contains(label.getWord());
    }
}
