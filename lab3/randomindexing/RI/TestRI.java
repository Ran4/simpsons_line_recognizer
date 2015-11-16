/*
 * TestRI.java
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
 * Martin Hassel, 2004-may-08
 * http://www.nada.kth.se/~xmartin/
 */

import moj.ri.SparseDistributedMemory;

/**
 * A small but functional demo program that loads a given RandomIndex and 
 * generates some semantic sets, either for the given index terms or for the 
 * highest tf*idf ranking index terms in the index.
 *
 * @author  Martin Hassel
 * @version 2004-june-09
 */
public class TestRI {
	public static void main(String arg[]) {
		if(arg.length > 0) {
			long time1, time2;
			float timediff;
			// First we need the actual RandomIndex (here in the form of SparseDistributedMemory
			// which is the RandomIndex extended with load/save functionality etc)
			time1 = System.currentTimeMillis();
			SparseDistributedMemory ri = new SparseDistributedMemory();
			ri.load(arg[0]);
			time2 = System.currentTimeMillis();
			timediff = (float)(time2-time1)/1000;
			System.out.println("Loading of RandomIndex took " + timediff + " seconds");
			System.out.println(ri.getWordsIndexed() + " words indexed");
			System.out.println(ri.size() + " wordforms/index terms\n");

			if(arg.length > 1) {
				String syn[];
				for(int i = 1; i < arg.length; i++) {
					System.out.println(arg[i]);
					syn = ri.getCorrelations(arg[i], 10);
					for(int j = 0; j < syn.length; j++)
						System.out.println("\t" + syn[j]);
					System.out.println("");
				}
			} else {
				// Generate 20 semantic sets of common words
				System.out.println("");
				time1 = System.currentTimeMillis();
				String syn[], tfidf[];
				tfidf = ri.getTfIDfRank(20);
				for(int i = 0; i < tfidf.length; i++) {
					System.out.println(tfidf[i]);
					String tfidf_parts[] = tfidf[i].split("=");
					syn = ri.getCorrelations(tfidf_parts[0], 10);
					for(int j = 0; j < syn.length; j++)
						System.out.println("\t" + syn[j]);
					System.out.println("");
				}
				time2 = System.currentTimeMillis();
				timediff = (float)(time2-time1)/1000;
				System.out.println("Generating sets took " + timediff + " seconds");
			}
		} else {
			System.out.println("*** TestRI ***");
			System.out.println("Usage: TestRI <random index> (<query1> ... <queryn>)");
			System.out.println("<random index> : RandomIndex to load");
			System.out.println("<query1> ... <queryn> : index terms to generate semantic sets for");
			System.out.println("\nFor example:\njava -Xmx100m -cp bin:lib TestRI rodarummet");
		}
	}
}
