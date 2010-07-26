/*
 * Copyright (C) 2000-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package org.gogoego.util.text;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This is a venerable utility class that has been in use since around Java 1.3,
 * when the substring replacement facilities in Java were woefully underpowered.
 * Now, we have String.replace(...) and so forth, and there are probably fewer
 * legitimate reasons to invoke a utility class like this. Still, tests seem to
 * indicate that this Replacer class does simple substitutions much faster than
 * the idiomatic alternatives, and it has some helpful syntactic sugar that is
 * in use by other parts of our libraries, so it remains in maintenance.
 * <p>
 * 
 * This class also can do entity expansion using objects that implement the
 * ELEntity class, which is fairly important to GoGoEgo templating and much
 * slower to do with regular expressions.
 * <p>
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class Replacer {

	/**
	 * Reduces sequential spaces or tabs (but not newlines) to a single space
	 * character.
	 */
	public static String compressSpaces(final String in) {
		// avoid NPE
		if (in == null)
			return null;
		int i;
		int isWhitespace = 0;
		final StringBuilder buff = new StringBuilder(in.length());
		for (i = 0; i < in.length(); i++) {
			final char c = in.charAt(i);
			switch (c) {
			case ' ':
			case '\t':
				switch (isWhitespace) {
				case 1:
					// yes, duplicate whitespace, do nothing
					break;
				case 0:
					// no, turn on whitespace mode, emit a single
					isWhitespace = 1;
					buff.append(' ');
				}
				break;
			default:
				isWhitespace = 0;
				buff.append(c);
			}
		}
		return buff.toString();
	}

	/**
	 * Reduces sequential whitespace of ANY type (spaces, newlines, tabs) to a
	 * single space character.
	 */
	public static String compressWhitespace(final String in) {
		// avoid NPE
		if (in == null)
			return null;
		int i;
		int isWhitespace = 0;
		final StringBuilder buff = new StringBuilder(in.length());
		for (i = 0; i < in.length(); i++) {
			final char c = in.charAt(i);
			switch (c) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				switch (isWhitespace) {
				case 1:
					// yes, duplicate whitespace, do nothing
					break;
				case 0:
					// no, turn on whitespace mode, emit a single
					isWhitespace = 1;
					buff.append(' ');
				}
				break;
			default:
				isWhitespace = 0;
				buff.append(c);
			}
		}
		return buff.toString();
	}

	/**
	 * Accepts ALL UPPER or all lower strings and converts Camel Case strings to
	 * lower case. This will keep things like IT and it, while switching stuff
	 * like It to it. Got it?
	 */
	public static String consistentCase(final String in) {
		// avoid NPE
		if (in == null)
			return null;
		if (in.toUpperCase().equals(in))
			return in;
		return in.toLowerCase();
	}

	/**
	 * True if the supplied strings contains the ASCII Latin letters a-z or A-Z.
	 */
	public static boolean containsLetters(final String in) {
		for (int i = 0; i < in.length(); i++) {
			final char c = in.charAt(i);
			if ((c > 'A') && (c < 'Z'))
				return true;
			if ((c > 'a') && (c < 'z'))
				return true;
		}
		return false;
	}

	/**
	 * True if the supplied strings contains the ASCII digits 0-9.
	 */
	public static boolean containsNumbers(final String in) {
		for (int i = 0; i < in.length(); i++) {
			final char c = in.charAt(i);
			if ((c > '0') && (c < '9'))
				return true;
		}
		return false;
	}

	/**
	 * A reasonably hungry and fast copy ... but uses a 2K buffer instead of
	 * slurping whole files. Which would be bad if you are working with really
	 * big files, or have lots of people working at once.
	 */
	public static void copy(final Reader i, final Writer o) throws IOException {
		final char[] buf = new char[2048];
		int in;
		do {
			in = i.read(buf);
			if (in > -1)
				o.write(buf, 0, in);
		} while (in > -1);
		i.close();
	}

	/**
	 * A specal purpose variant for escaping HTML/XML angle bracket tags where
	 * they would cause trouble
	 */
	public static String escapeTags(String in) {
		in = Replacer.replace(in, "&", "&amp;");
		in = Replacer.replace(in, "<", "&lt;");
		in = Replacer.replace(in, ">", "&gt;");
		in = Replacer.replace(in, "\"", "&quot;");
		in = Replacer.replace(in, "'", "&apos;");
		return in;
	}

	/**
	 * Internal workhorse method; operates on a string to do a single token
	 * replacement.
	 */
	public static String replace(final String in, final String token,
			final String value) {
		// short circuit eliminates NPEs here
		if ((in == null) || (token == null) || (value == null)
				|| "".equals(token) || // token is empty
				(in.indexOf(token) == -1))
			return in;
		int found;
		int last = 0;

		// allocate space for at least 1 replacement
		// a second replacement may require reallocation of string buff size
		final StringBuilder newsb = new StringBuilder(in.length()
				+ value.length() + 4);

		do {
			found = in.indexOf(token, last);
			if (found > -1) {
				newsb.append(in.substring(last, found));
				newsb.append(value);
				last = found + token.length();
			}
		} while (found > -1);
		newsb.append(in.substring(last));

		return newsb.toString();
	}

	/**
	 * Limits HTML tags to only simple (em,strong,p,br,div) by running it
	 * through a TagFilter. Not the most efficient possible solution for this
	 * problem, but beats the heck out of many popular alternatives. Consider
	 * processing streams directly in your own performance-critical code.
	 */
	public static String simplifyTags(final String in) throws IOException {
		if (in == null)
			return null;
		final StringReader sr = new StringReader(in);
		final StringWriter ss = new StringWriter(in.length());
		final SimpleTagListener stl = new SimpleTagListener();
		final TagFilter filter = new TagFilter(sr, ss);
		filter.shortCircuitClosingTags = false;
		filter.registerListener(stl);
		filter.parse();
		String out = Replacer.stripWhitespace(ss.toString());
		int ctr = 0;
		try { // attempt to remove leading/trailing junk generated by WYSIWYG
			while (out.startsWith("<p/>")) {
				out = out.substring(4);
				ctr++;
				if (ctr > 100)
					break; // infinite loop protection
			}
			while (out.endsWith("<p>")) {
				out = out.substring(0, out.length() - 3);
				ctr++;
				if (ctr > 100)
					break; // infinite loop protection
			}
			while (out.endsWith("<p/>")) {
				out = out.substring(0, out.length() - 4);
				ctr++;
				if (ctr > 100)
					break; // infinite loop protection
			}
			while (out.endsWith("<br/>")) {
				out = out.substring(0, out.length() - 5);
				ctr++;
				if (ctr > 100)
					break; // infinite loop protection
			}
			while (out.endsWith("&nbsp;")) {
				out = out.substring(0, out.length() - 6);
				ctr++;
				if (ctr > 100)
					break; // infinite loop protection
			}
			while (out.endsWith("<p></p>")) {
				out = out.substring(0, out.length() - 7);
				ctr++;
				if (ctr > 100)
					break; // infinite loop protection
			}
			while (out.endsWith("<br><br/>")) {
				out = out.substring(0, out.length() - 9);
				ctr++;
				if (ctr > 100)
					break; // infinite loop protection
			}
		} catch (final Exception ignored) {
		}
		out = Replacer.replace(out, "&nbsp;", "&#160;"); // illegal xhtml
		// entity killer
		return out;
	}

	/**
	 * Removes any non-alphanumeric characters (A-Za-z0-9), including spaces
	 */
	public static String stripNonalphanumeric(String in) {
		// avoid NPE
		if (in == null)
			return null;
		StringBuffer buff = new StringBuffer(in.length());
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if ((c >= 'a') && (c <= 'z'))
				buff.append(c);
			else if ((c >= 'A') && (c <= 'Z'))
				buff.append(c);
			else if ((c >= '0') && (c <= '9'))
				buff.append(c);
		}
		return buff.toString();
	}

	public static String stripNonalphanumeric(String in, char[] explictlyAllowed) {
		if (in == null)
			return null;
		StringBuffer buff = new StringBuffer(in.length());
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if ((c >= 'a') && (c <= 'z'))
				buff.append(c);
			else if ((c >= 'A') && (c <= 'Z'))
				buff.append(c);
			else if ((c >= '0') && (c <= '9'))
				buff.append(c);
			else
				for (int k = 0; k < explictlyAllowed.length; k++)
					if (explictlyAllowed[k] == c)
						buff.append(c);
		}
		return buff.toString();
	}

	/**
	 * Removes any non-word characters (A-Za-z), including spaces
	 */
	public static String stripNonword(final String in) {
		// avoid NPE
		if (in == null)
			return null;
		final StringBuilder buff = new StringBuilder(in.length());
		int i;
		for (i = 0; i < in.length(); i++) {
			final char c = in.charAt(i);
			if ((c >= 'a') && (c <= 'z'))
				buff.append(c);
			else if ((c >= 'A') && (c <= 'Z'))
				buff.append(c);
		}
		return buff.toString();
	}
	
	/**
	 * Removes any non-numeric characters.
	 */
	public static String stripNonNumeric(final String in) {
		if (in == null)
			return null;
		final StringBuilder buff = new StringBuilder(in.length());
		for (int i = 0; i < in.length(); i++) {
			final char c = in.charAt(i);
			if (Character.isDigit(c))
				buff.append(c);
		}
		return buff.toString();
	}

	/**
	 * Eliminates HTML tags from a string by running it through a TagFilter. Not
	 * the most efficient possible solution for this problem, but beats the heck
	 * out of many popular alternatives. Consider processing streams instead of
	 * strings in your own performance-critical code.
	 */
	public static String stripTags(final String in) throws IOException {
		if (in == null)
			return null;
		final StringReader sr = new StringReader(in);
		final StringWriter ss = new StringWriter(in.length());
		final StripTagListener stl = new StripTagListener();
		final TagFilter filter = new TagFilter(sr, ss);
		filter.shortCircuitClosingTags = false;
		filter.registerListener(stl);
		filter.parse();
		return ss.getBuffer().toString();
	}

	/**
	 * Removes leading or trailing whitespace of any type (spaces, newlines,
	 * tabs) -- not unlike Perl "chomp."
	 */
	public static String stripWhitespace(final String in) {
		// avoid NPE
		if (in == null)
			return null;
		int i;
		LOOP1: for (i = 0; i < in.length(); i++) {
			final char c = in.charAt(i);
			switch (c) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				break;
			default:
				break LOOP1;
			}
		}
		int j;
		LOOP2: for (j = in.length() - 1; j > i; j--) {
			final char c = in.charAt(j);
			switch (c) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				break;
			default:
				break LOOP2;
			}
		}
		return in.substring(i, j + 1);
	}
}
