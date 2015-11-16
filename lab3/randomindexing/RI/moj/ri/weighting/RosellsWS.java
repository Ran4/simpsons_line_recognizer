/*
 * RosellsWS.java
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
 * Magnus Rosell, 2004-aug
 * http://www.nada.kth.se/~rosell/
 */

package moj.ri.weighting;

import moj.ri.RandomLabel;
import moj.util.VectorSpace;

/**
 * ((1-c) + c*sim) * 2^(1-d)
 * 
 * @author  Magnus Rosell
 * @version 2004-aug-17
 */
public class RosellsWS extends WeightingScheme {
    public final float simWeight=0.2f;

    public float[] applyLeftWeighting(RandomLabel focusLabel, int distance, RandomLabel leftContextLabel) {
    	float[] focusContext = focusLabel.getContext();
    	if(leftContextLabel.getTermFrequency() > 1) {
    		float sim = VectorSpace.cosineSim(focusLabel.getContext(), leftContextLabel.getContext());
    		float mangeWeight = (float)(Math.pow(2.0, (1-distance)));
    		float weight = ((1.0f-simWeight) + simWeight*sim) * mangeWeight;
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

    public float[] applyRightWeighting(RandomLabel focusLabel, int distance, RandomLabel rightContextLabel) {
    	float[] focusContext = focusLabel.getContext();
    	if(rightContextLabel.getTermFrequency() > 1) {
    		float sim = VectorSpace.cosineSim(focusLabel.getContext(), rightContextLabel.getContext());
    		float mangeWeight = (float)(Math.pow(2.0, (1-distance)));
    		float weight = ((1.0f-simWeight) + simWeight*sim) * mangeWeight;
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
