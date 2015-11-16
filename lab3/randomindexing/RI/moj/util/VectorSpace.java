/*
 * VectorSpace.java
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
 * Martin Hassel, 2004-may-19
 * http://www.nada.kth.se/~xmartin/
 *
 */

package moj.util;

/**
 * Utilities for manipulating Vector Space data.
 * 
 * @author  Martin Hassel
 * @version 2004-may-19
 */
public class VectorSpace {

	/**
     * Calculate the Cosine similarity between two vectors of floats.
     * If the two vectors differ in length, a value of 0 is returned since
     * they per definition are not equal. If both vectors are of equal length
     * and both are all zeroes, a value of -1 is returned to differentiate
     * this state from any other case where they are equal.
     * @param vector1 the vector to be compared to <code>vector2</code>.
     * @param vector2 the vector to be compared to <code>vector1</code>.
     * @param weight1 weight to apply to <code>vector1</code>.
     * @param weight2 weight to apply to <code>vector2</code>.
     * @return the 2-norm (Euclidean Distance) between <code>vector1</code>
     *         and <code>vector2</code> weighted with <code>weight1</code>
     *         and <code>weight2</code>.
     **/
	public static float cosineSim(float[] vector1, float[] vector2, float weight1, float weight2) {
        float numerator=0, sum1=0, sum2=0;

        // If the labels are not of the same dimensionality we should
        // not try to compare them as this may go beyond array boundaries
        // Instead we return maximum distance
        if(vector1.length != vector2.length) return 0;

        for(int current = 0; current < vector1.length; current++) {
            sum1 += (vector1[current] * vector1[current] * weight1);
            sum2 += (vector2[current] * vector2[current] * weight2);
            numerator += (vector1[current] * vector2[current]);
        }

        // A value of -1 is returned when both vectors are all zeroes in order to
        // differentiate this state from any other where the vectors are identical
        if(sum1==0 && sum2==0) return -1;
        if(sum1==0 || sum2==0) return 0;
        return numerator/(float)Math.sqrt(sum1*sum2);
      }

	/**
     * Calculate the Cosine similarity between two vectors of floats.
     * If the two vectors differ in length, a value of 0 is returned since
     * they per definition are not equal. If both vectors are of equal length
     * and both are all zeroes, a value of -1 is returned to differentiate
     * this state from any other case where they are equal.
     * @param vector1 the vector to be compared to <code>vector2</code>.
     * @param vector2 the vector to be compared to <code>vector1</code>.
     * @return the 2-norm (Euclidean Distance) between <code>vector1</code>
     *         and <code>vector2</code>.
     **/
	public static float cosineSim(float[] vector1, float[] vector2) {
        return VectorSpace.cosineSim(vector1, vector2, 1, 1);
      }

	/**
     * Calculate the Cosine similarity between two vectors of bytes.
     * If the two vectors differ in length, a value of 0 is returned since
     * they per definition are not equal. If both vectors are of equal length
     * and both are all zeroes, a value of -1 is returned to differentiate
     * this state from any other case where they are equal.
     * @param vector1 the vector to be compared to <code>vector2</code>.
     * @param vector2 the vector to be compared to <code>vector1</code>.
     * @return the 2-norm (Euclidean Distance) between <code>vector1</code>
     *         and <code>vector2</code>.
     **/
	public static float cosineSim(byte[] vector1, byte[] vector2) {
        if(vector1.length != vector2.length) return 0;
		float[] fvector1= new float[vector1.length];
		float[] fvector2= new float[vector2.length];
        for(int i=0;i<vector1.length;i++) {
            fvector1[i] = (float)vector1[i];
            fvector2[i] = (float)vector2[i];
        }
        return VectorSpace.cosineSim(fvector1, fvector2, 1, 1);
      }
}
