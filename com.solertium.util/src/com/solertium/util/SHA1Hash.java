/*
 * Dedicated to the public domain by the author, Rob Heittman,
 * Solertium Corporation, July 2002.  Based on examples in the
 * public domain.
 * 
 * http://creativecommons.org/licenses/publicdomain/
 */

package com.solertium.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The real interface to java.security.MessageDigest can be clumsy. This class
 * follows some commonly used patterns to make it easier to work with MD5
 * digests.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class SHA1Hash {

	MessageDigest md;
	byte[] dig;
	
	public SHA1Hash() {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (final NoSuchAlgorithmException unlikely) {
			TrivialExceptionHandler.handle(this, unlikely);
		}
	}

	public SHA1Hash(final String s) {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (final NoSuchAlgorithmException unlikely) {
			TrivialExceptionHandler.handle(this, unlikely);
		}
		update(s);
	}
	
	public SHA1Hash(final byte[] byteString) {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (final NoSuchAlgorithmException unlikely) {
			TrivialExceptionHandler.handle(this, unlikely);
		}
		update(byteString);
	}

	@Override
	public String toString() {
		StringBuffer hexString = new StringBuffer();
		if (dig == null)
			dig = md.digest();
		
		for (int i = 0; i < dig.length; i++)
		{
			 String hex = Integer.toHexString(0xFF & dig[i]);
             if (hex.length() == 1)
             {
                 hexString.append('0');
             }
             hexString.append(hex);
		}
		
		return hexString.toString().toUpperCase();
	}

	public void update(final String s) {
		dig = null;
		md.update(s.getBytes());
	}
	
	public void update(byte[] stringBytes)
	{
		dig = null;
		md.update(stringBytes);
	}

}
