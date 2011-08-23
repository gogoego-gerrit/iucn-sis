/*
 * Dedicated to the public domain by the author, Rob Heittman,
 * Solertium Corporation, December 2007
 * 
 * http://creativecommons.org/licenses/publicdomain/
 */

package org.iucn.sis.shared.api.utils;

import com.solertium.util.portable.PortableAlphanumericComparator;

/**
 * Optimized public-domain implementation of a Java alphanumeric sort.
 * <p>
 * 
 * This implementation uses a single comparison pass over the characters in a
 * CharSequence, and returns as soon as a differing character is found, unless
 * the difference occurs in a series of numeric characters, in which case that
 * series is followed to its end. Numeric series of equal length are compared
 * numerically, that is, according to the most significant (leftmost) differing
 * digit. Series of unequal length are compared by their length.
 * <p>
 * 
 * This implementation appears to be 2-5 times faster than alphanumeric
 * comparators based based on substring analysis, with a lighter memory
 * footprint.
 * <p>
 * 
 * This alphanumeric comparator has approximately 20%-50% the performance of the
 * lexical String.compareTo() operation. Character sequences without numeric
 * data are compared more quickly.
 * <p>
 * 
 * @author rasanka.jayawardana
 */
public class CaseInsensitiveAlphanumericComparator extends PortableAlphanumericComparator {
	private static final long serialVersionUID = 1L;

	public int compare(final Object ol, final Object or) {
		CharSequence l; 
		CharSequence r;
		
		// Convert to Uppercase
		if (ol instanceof CharSequence)
			l = (CharSequence) ol.toString().toUpperCase();
		else
			l = ol.toString().toUpperCase();
		
		if (or instanceof CharSequence)
			r = (CharSequence) or.toString().toUpperCase();
		else
			r = or.toString().toUpperCase();
		
		return super.compare(l, r);
	}

}
