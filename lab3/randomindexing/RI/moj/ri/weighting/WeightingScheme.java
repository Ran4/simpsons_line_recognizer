/*
 * WeightingScheme.java
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
 * Calculates the weighting based upon the distance to the current label. 
 * By default it doesn't modify the context. Application writers may override 
 * these methods in a subclass to calculate the weights according to specific 
 * algorithms.
 * 
 * @author  Martin Hassel
 * @version 2004-may-23
 */
public class WeightingScheme {

	/**
	 * Calculates the weight based upon the distance (in the left context) to
	 * the current label. By default it doesn't modify the context. Application writers 
	 * may override these methods in a subclass to calculate the weights according to 
	 * specific algorithms.
	 * @param focusLabel RandomLabel in focus, i.e. the focus word whose context vector 
	 *        should be modified with the weighted RandomLabel in left context.
	 * @param distance leftward distance to the focus word (RandomLabel).
	 * @param leftContextLabel RandomLabel in left context at <code>distance</code> from 
	 *        the focus word, which can be used together with <code>distance</code> to 
	 *        calculate the weight.
	 * @return the context of the focus word modified according to the weighting scheme.
	 */
	public float[] applyLeftWeighting(RandomLabel focusLabel, int distance, RandomLabel leftContextLabel) {
		return focusLabel.getContext();
	}

	/**
	 * Calculates the weight based upon the distance (in the right context) to
	 * the current label. By default it doesn't modify the context. Application writers 
	 * may override these methods in a subclass to calculate the weights according to 
	 * specific algorithms.
	 * @param focusLabel RandomLabel in focus, i.e. the focus word whose context vector 
	 *        should be modified with the weighted RandomLabel in right context.
	 * @param distance rightward distance to the focus word (RandomLabel).
	 * @param rightContextLabel RandomLabel in right context at <code>distance</code> from 
	 *        the focus word, which can be used together with <code>distance</code> to 
	 *        calculate the weight.
	 * @return the context of the focus word modified according to the weighting scheme.
	 */
	public float[] applyRightWeighting(RandomLabel focusLabel, int distance, RandomLabel rightContextLabel) {
		return focusLabel.getContext();
	}
}
