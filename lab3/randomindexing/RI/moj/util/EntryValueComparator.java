/*
 * EntryValueComparator.java
 * No copyright (generic solution)
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
 */

package moj.util;
import java.util.Comparator;
import java.util.Map;

/**
 * Comparator that compares values. Note that it compares the keys as well as
 * the values. This is necessary to be consistent with Map.Entry.equals() and
 * to ensure that our entrySet behaves like a Set.
 */
public class EntryValueComparator implements Comparator {
    /**
     * Compares values. Note that it compares the keys as well as the values. 
     * This is necessary to be consistent with Map.Entry.equals() and to 
     * ensure that our entrySet behaves like a Set.
     * @param o1 <code>Object</code> that is to be compared to <code>o2</code>.
     * @param o2 <code>Object</code> that is to be compared to <code>o1</code>.
     * @return the difference between <code>o1</code> and <code>o2</code>.
     */
    public int compare(Object o1, Object o2) {
        return compare((Map.Entry)o1, (Map.Entry)o2);
    }

    /**
     * Compares values. Note that it compares the keys as well as the values. 
     * This is necessary to be consistent with Map.Entry.equals() and to 
     * ensure that our entrySet behaves like a Set.
     * @param e1 <code>Map.Entry</code> that is to be compared to <code>e2</code>.
     * @param e2 <code>Map.Entry</code> that is to be compared to <code>e1</code>.
     * @return the difference between <code>e1</code> and <code>e2</code>.
     */
    public int compare(Map.Entry e1, Map.Entry e2) {
        int diff = ((Comparable)e1.getValue()).compareTo(e2.getValue());
        if (diff == 0) {
            diff = ((Comparable)e1.getKey()).compareTo(e2.getKey());
        }
        return diff;
    }
}
