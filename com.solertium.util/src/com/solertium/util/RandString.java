/*
 * RandString.java - Return a random string suitable for hand transcription
 *
 * Copyright (C) 2004 Cluestream Ventures, LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 *
 * You may download a copy of the GNU General Public License
 * electronically from http://www.cluestream.com/licenses/gpl.html.
 *
 * If the GNU General Public License is not suitable for your
 * purposes (if, for example, you are producing non-free software),
 * please contact the copyright holder to discuss alternative licensing.
 * Please direct your inquiries to: sales@cluestream.com.
 */

package com.solertium.util;

import java.util.Random;

/**
 *
 * @author Rob Heittman (rob@cluestream.com)
 * @version {$REVISION $}
 */
public class RandString {

    // this uses an interesting alphanumeric code set, using the
    // full set of numbers and a subset of English letters.  The set of
    // letters and numbers chosen has no vowels or vowel-appearing numbers.
    // Commonly confused items (5 and S, I and 1) are omitted, and because
    // the vowels are gone the random strings are unlikely to spell any
    // overt dirty words.
    private static final String code =
        "23456789BCDFGHJKLMNPQRTVWY";
    private static Random rand = new Random(System.currentTimeMillis());

    /** Creates new RandString */
    public RandString() {
    }
    
    // main method used only for testing
    public static void main(String[] args){
        String test = RandString.getString(5000);
        System.out.print(test);
    }

    /** Workhorse method.  Gets a random string, suitable for hand
     * transcription, of the specified length.  Note that because this
     * is a base 26 string, if you are using this for IDs or license codes
     * or whatever, you can get by with less characters for the same number
     * of possible unique strings. */
    public static String getString(int nChars){
        StringBuilder sb = new StringBuilder(nChars+1);
        for(int l=1; l<nChars; l++){
            int i = rand.nextInt(25);
            sb.append(code.charAt(i));
        }
        return sb.toString();
    }

}