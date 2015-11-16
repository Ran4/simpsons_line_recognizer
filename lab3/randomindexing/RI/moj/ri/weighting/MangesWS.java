/*
 * MangesWS.java
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
 * Martin Hassel, 2004-may-08
 * http://www.nada.kth.se/~xmartin/
 */

package moj.ri.weighting;

import moj.ri.RandomLabel;

/**
 * Calculates the weighting based upon the distance to the current label in 
 * the following manner: weight=(2^(1-distance to focus word)).
 * However, the RandomLabel at <code>distance</code> only contextually influences
 * the current label if it has <code>TermFrequency</code> of 2 or more.
 * 
 * @author  Martin Hassel
 * @version 2004-aug-17
 */
public class MangesWS extends WeightingScheme {

	/**
	 * Calculates the weight based upon the distance (in the left context) to
	 * the current label in the following manner: weight=(2^(1-distance to focus word)).
	 * However, the RandomLabel at <code>distance</code> only contextually influences
	 * the current label if it has <code>TermFrequency</code> of 2 or more.
	 * @param focusLabel RandomLabel in focus, i.e. the focus word whose context vector 
	 *        should be modified with the weighted RandomLabel in left context.
	 * @param distance leftward distance to the focus word (RandomLabel).
	 * @param leftContextLabel RandomLabel in left context at <code>distance</code> from 
	 *        the focus word, which can be used together with <code>distance</code> to 
	 *        calculate the weight.
	 * @return the context of the focus word modified according to the weighting scheme.
	 */
	public float[] applyLeftWeighting(RandomLabel focusLabel, int distance, RandomLabel leftContextLabel) {
		float[] focusContext = focusLabel.getContext();
		if(leftContextLabel.getTermFrequency() > 1) {
		    float weight = (float)(Math.pow(2.0, (1-distance)));
			// First we update the negative positions (-1:s)
		    int[] negs = leftContextLabel.getNegativePositions();
		    for(int current = 0; current < negs.length; current++)
		    	focusContext[negs[current]] += -weight;
			// Then we update the positive positions (1:s)
		    int[] poss = leftContextLabel.getPositivePositions();
		    for(int current = 0; current < poss.length; current++)
		    	focusContext[poss[current]] += weight;
		}
		return focusContext;
	}

	/**
	 * Calculates the weight based upon the distance (in the right context) to
	 * the current label in the following manner: weight=(2^(1-distance to focus word)).
	 * However, the RandomLabel at <code>distance</code> only contextually influences
	 * the current label if it has <code>TermFrequency</code> of 2 or more.
	 * @param focusLabel RandomLabel in focus, i.e. the focus word whose context vector 
	 *        should be modified with the weighted RandomLabel in right context.
	 * @param distance rightward distance to the focus word (RandomLabel).
	 * @param rightContextLabel RandomLabel in right context at <code>distance</code> from 
	 *        the focus word, which can be used together with <code>distance</code> to 
	 *        calculate the weight.
	 * @return the context of the focus word modified according to the weighting scheme.
	 */
	public float[] applyRightWeighting(RandomLabel focusLabel, int distance, RandomLabel rightContextLabel) {
		float[] focusContext = focusLabel.getContext();
		if(rightContextLabel.getTermFrequency() > 1) {
		    float weight = (float)(Math.pow(2.0, (1-distance)));
			// First we update the negative positions (-1:s)
		    int[] negs = rightContextLabel.getNegativePositions();
		    for(int current = 0; current < negs.length; current++)
		    	focusContext[negs[current]] += -weight;
			// Then we update the positive positions (1:s)
		    int[] poss = rightContextLabel.getPositivePositions();
		    for(int current = 0; current < poss.length; current++)
		    	focusContext[poss[current]] += weight;
		}
		return focusContext;
	}
}
