/*
 * RandomLabel.java
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

import gate.util.HashMapLong;

import java.util.Random;

import moj.ri.weighting.WeightingScheme;
import moj.util.VectorSpace;

/**
 * A RandomLabel consists of a word (or term), a term frequency count,
 * a document frequency count, a randomly initalized label and a
 * contextually updated (weighted window) context vector.
 * This container is used by RandomIndex to store the index terms and
 * their corresponding frequency and context data.
 *
 * @author  Martin Hassel
 * @version 2004-may-23
 */
public class RandomLabel implements Comparable {
    private final String _word; // The word the 'label' is to be attached to
    private long _termFrequency = 0; // Number of updates done to the label
    private int  _docFrequency = 0;  // Number of documents the word occurs in
    private int _dimensionality;	// The dimensionality of the random label
    private int _negs[];	// The indexes for -1:s in the random label
    private int _poss[]; 	// The indexes for 1:s in the random label
    private float _context[];		// The actual contextually updated label

    /**
     * To construct a new RandomLabel object we need the word that is to be 'labeled',
     * the length of the label (i.e. the label's dimensionality), the randomness of 
     * the label's initial state (i.e. number of non-zero elements) and a seed.
     * @param word the word which the label is to be associated to.
     * @param dimensionality the length of the label to be associated to the word.
     * @param randomDegree the 'degree' of randomness initially applied to the label
     *        given in total number of non-zeros in the inital label (even number).
     *        Should not be greater than dimensionalty and therefore, in this case,
     *        defualts to dimensionality.
     * @param seed a seed for the local random generator. This seed, in combination
     *        with <code>word</code>, makes it very likely that the created random label is
     *        "unique" yet reproducable.
     */
    public RandomLabel(String word, int dimensionality, int randomDegree, int seed) {
        _word = word;
        _dimensionality = dimensionality;
        _context = new float[dimensionality];

        // Nr of initial pos & neg states can never be more than the nr of elements in label
        if(dimensionality < randomDegree)
            randomDegree = dimensionality;

        // We want an equal number of initial +1 and -1 in the label integer array
        int no_initial_pos_and_neg = Math.round(randomDegree/2);
    	_negs = new int[no_initial_pos_and_neg];
    	_poss = new int[no_initial_pos_and_neg];

        // Create reproducable random generator using the word (index term) as seed
        try {
            byte[] wordar = word.getBytes("UTF-8");
            long wseed = 0;
            for(int i = 0; i < wordar.length; i++)
                wseed += wordar[i];
            Random randgen = new Random(wseed+seed);

            /* Slow(er) code that doesn't use HashMapLong from the gate.util package
            Map pos_and_neg_positions = new HashMap();
            // Generate the positions in the label to be initially filled (+1/-1) and fill them
            for(int current = 1; current <= no_initial_pos_and_neg; current++) {

                // Generate unused position and add a +1
                Long position = new Long(randgen.nextInt(dimensionality));
                while(pos_and_neg_positions.get(position) != null) {
                    position = new Long(randgen.nextInt(dimensionality));
                }
                pos_and_neg_positions.put(position, "+");
                _label[position.intValue()] = +1;

                // Generate unused position and add a -1
                position = new Long(randgen.nextInt(dimensionality));
                while(pos_and_neg_positions.get(position) != null) {
                    position = new Long(randgen.nextInt(dimensionality));
                }
                pos_and_neg_positions.put(position, "-");
                _label[position.intValue()] = -1;
            }
            */

            // HashMapLong is a HashMap that takes primitive long keys but only has get(),put() and isEmpty()
            // This is however speedier than the version above (but requires 'import gate.util.HashMapLong;')
            HashMapLong pos_and_neg_positions = new HashMapLong();

            // Generate the positions in the random label to be initially filled
            for(int current = 0; current < no_initial_pos_and_neg; current++) {
                // Generate unused position and add a +1
                long position = randgen.nextInt(dimensionality);
                while(pos_and_neg_positions.get(position) != null) {
                    position = randgen.nextInt(dimensionality);
                }
                pos_and_neg_positions.put(position, "+");
                _poss[current]=(int)position;

                // Generate unused position and add a -1
                position = randgen.nextInt(dimensionality);
                while(pos_and_neg_positions.get(position) != null) {
                    position = randgen.nextInt(dimensionality);
                }
                pos_and_neg_positions.put(position, "-");
                _negs[current]=(int)position;
            }
        } catch (java.io.UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Defaults the dimensionality to 1800 and the randomness to 8, i.e. four
     * +1 and four -1. Seed is set to 123.
     * @param word the word which the label is to be associated to.
     */
    public RandomLabel(String word) {
        this(word, 1800, 8, 123);
    }

    /**
     * Creates an empty RandomLabel with a dimensionality of 1800 and
     * randomness of 0, i.e. all zeros in the label. Usefull for padding.
     */
    public RandomLabel() {
        this("", 1800, 0, 123);
    }

    /**
     * Construct new RandomLabel from existing data.
     * @param word the word which the label is to be associated to.
     * @param termFrequency the term frequency for the associated word.
     * @param docFrequency the document frequency for the associated word.
     * @param positivePositions the positions in the RandomLabel that should hold "1"
     * @param negativePositions the positions in the RandomLabel that should hold "-1"
     * @param context the context vector to be associated to the word
     *        (i.e. an array representing the co-occurrence "coloring").
     */
    public RandomLabel(String word, long termFrequency, int docFrequency, 
    		int positivePositions[], int negativePositions[], float context[]) {
        _word = word;
        _termFrequency = termFrequency;
        _docFrequency = docFrequency;
        _poss = positivePositions;
        _negs = negativePositions;
        _context = context;
    }

    /**
     * Get the word which the RandomLabel is associated to.
     * @return the word which the RandomLabel is associated to.
     */
    public String getWord() {
        return _word;
    }

    /**
     * Get the negative positions in the random label associated to the word.
     * @return the negative part of the randomly generated label associated to the word.
     */
    public int[] getNegativePositions() {
        return _negs;
    }

    /**
     * Get the positive positions in the random label associated to the word.
     * @return the positive part of the randomly generated label associated to the word.
     */
    public int[] getPositivePositions() {
        return _poss;
    }

    /**
     * Get the context vector associated to the word.
     * @return the contextually updated (weighted window) context vector associated to
     * the word.
     */
    public float[] getContext() {
        return _context;
    }

    private float[] setContext(float[] newContext) {
    	float[] tempContext = _context;
    	_context = newContext;
        return tempContext;
    }

    /**
     * Get the random label associated to the word.
     * @return the randomly generated label associated to the word.
     */
    public int getDimensionality() {
        return _dimensionality;
    }

    /**
     * Get the term frequency for the current word.
     * @return the term frequency, i.e. nr of contextual updates (including initialization)
     * the RandomLabel has gone through.
     */
    public long getTermFrequency() {
        return _termFrequency;
    }

    /**
     * Get the document frequency for the current word.
     * @return the document frequency, i.e. nr of documents the word associated with this
     * RandomLabel occurs in.
     */
    public int getDocumentFrequency() {
        return _docFrequency;
    }

    /**
     * Increment the document frequency for the current word.
     * @return the document frequency, i.e. the number of documents the word associated
     * with this RandomLabel occurs in <i>after</i> the increment.
     */
    public int incrementDocumentFrequency() {
        return ++_docFrequency;
    }

    /**
     * Update this RandomLabel's context with the weighted labels of the RandomLabels in left
     * and right context using the supplied weighting scheme (as defined by a visiting object
     * <code>weightingScheme</code>). The RandomLabel of the word nearest to this is the first 
     * element in the context vectors, (context window) the second nearest the second, and so 
     * on (note: this applies to <i>both</i> <code>rightContext</code> <i>and</i> 
     * <code>leftContext</code>).
     * 
     * All RandomLabels in <code>leftContext</code> and <code>rightContext</code> 
     * <i>must</i> have the same dimensionality. However, <code>leftContext</code> and 
     * <code>rightContext</code> themselves (i.e. the context window) do not have to be of 
     * equal length (i.e. you can have an unbalanced context window).
     * 
     * @param  leftContext an array of RandomLables where the first element represents the word
     *         closest to the left of the word who's label is being updated, the second element
     *         represents the word second closest to the left and so on.
     *         No slot may be empty (null) as this will cause a NullPointerException.
     * @param  rightContext same as for <code>leftContext</code> but for the right side.
     * @param  weightingScheme a visiting object that contains the methods for calculating the
     *         weights for the left resp. right contexts based upon distance to the current label.
     * @return returns true upon success, else false. The most common reason for failure is that
     *         not all RandomLabels have the same dimesionality.
     */
    public boolean updateContext(RandomLabel[] leftContext, RandomLabel[] rightContext, WeightingScheme weightingScheme) {
        RandomLabel tempRL = this;

        // Update focusContext with the weighted labels of the left context
        for(int distance = 0; distance < leftContext.length; distance++) {
            // If the labels are not of the same dimensionality as the context we should not add them
            if(leftContext[distance].getDimensionality() != _context.length)
            	return false;
            tempRL.setContext(weightingScheme.applyLeftWeighting(tempRL, distance+1, leftContext[distance]));
        }
        // Update focusContext with the weighted labels of the right context
        for(int distance = 0; distance < rightContext.length; distance++) {
            // If the labels are not of the same dimensionality as the context we should not add them
            if(rightContext[distance].getDimensionality() != _context.length)
            	return false;
            tempRL.setContext(weightingScheme.applyRightWeighting(tempRL, distance+1, rightContext[distance]));
        }

        _context = tempRL.getContext();
        _termFrequency++;
        return true;
    }

    /**
     * Calculate the cosine similarity between this RandomLabel and the given.
     * @param label the RandomLabel that is to be compared with this RandomLabel.
     * @return the 2-norm (Euclidean Distance) between this RandomLabel and
     *         the given.
     **/
    public float cosineSim(RandomLabel label) {
        return VectorSpace.cosineSim(_context, label.getContext());
    }

    /**
     * Compares this RandomLabel with the specified RandomLabel for order on basis of term frequency.
     * Returns a negative integer, zero, or a positive integer as this RandomLabel is less than,
     * equal to, or greater than the specified RandomLabel. This gives descending term frequency
     * when used in a sort.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     * 
     * @param label the RandomLabel to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than,
     *         equal to, or greater than the specified RandomLabel.
     */
    public int compareTo(Object label) throws ClassCastException {
        if(!(label instanceof RandomLabel))
            throw new ClassCastException("A RandomLabel object expected.");

        long termFrequency = ((RandomLabel)label).getTermFrequency();
        long difference = (int)termFrequency - _termFrequency;
        return (difference > 0) ? 1 : (difference < 0) ? -1 : 0;
    }

    /**
     * String representation of the RandomLabel.
     * @return a comma separated string where the first column is the word, the second
     * is the term frequency, the third is the document frequency, the fourth is the
     * dimensionality of the random label vector and the following are the actual vectors,
     * first the random label vector and then the context vector (until end).
     */
    public String toString() {
        StringBuffer retstring = new StringBuffer();
        retstring.append(_word + "," + _termFrequency + "," + _docFrequency + "," + _dimensionality);

    	HashMapLong pos_and_neg_positions = new HashMapLong();
    	for(int current = 0; current < _poss.length; current++)
    	    pos_and_neg_positions.put(_poss[current], "1");
    	for(int current = 0; current < _negs.length; current++)
    	    pos_and_neg_positions.put(_negs[current], "-1");
        for(int current = 0; current < _dimensionality; current++) {
    	    if(pos_and_neg_positions.get(current) == null)
    	    	retstring.append("," + "0");
    	    else 
    	    	retstring.append("," + pos_and_neg_positions.get(current));
        }

        for(int current = 0; current < _context.length; current++) {
            retstring.append("," + _context[current]);
        }
        return retstring.toString();
    }
}
