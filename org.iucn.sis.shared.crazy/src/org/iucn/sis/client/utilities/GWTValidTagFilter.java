/*
 * Copyright (C) 2007-2008 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */

package org.iucn.sis.client.utilities;

import com.solertium.util.portable.PortableTagFilter;


/**
 * TagFilter provides a fast callback interface for dealing with ill-formed
 * "real world" HTML documents. This is a version of the GeneralUtility
 * TagFilter that works within GWT limitations; it is taken from an older branch
 * of TagFilter and may not track all the same features as the one in the
 * GeneralUtility project.
 * 
 * Extended to provide a mechanism to disregard invalidly formatted XML tags
 * when a > is found. Using PortableTagFilter, if the buffer contains the text
 * 
 * <pre>
 * ... &lt; 50 km and &gt;250, ...
 * </pre>
 * 
 * PortableTagFilter would treat this as a tag, and most likely filter it out. But
 * since the "tag"
 * 
 * <pre>
 * &lt; 50 km and &gt;
 * </pre>
 * 
 * does not follow the valid XML tag syntax, this class will treat it as plain
 * text, sending it to the output stream normally.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class GWTValidTagFilter extends PortableTagFilter {

	/** For convenience; see TagFilter(Reader r, Writer w) */
	public GWTValidTagFilter(final String r) {
		super(r, null);
	}

	/**
	 * Creates a new instance of TagFilter. This is the principal constructor
	 * and should be used when possible. For performance, be sure to supply a
	 * BufferedReader and BufferedWriter. This filter does not attempt to do its
	 * own buffering. Writer may be null if you do not need to emit output, but
	 * only wish to feed data to Listeners; you may also use the one-argument
	 * constructor below.
	 */
	public GWTValidTagFilter(final String r, final StringBuffer w) {
		super(r, w);
	}

	/**
	 * Causes the input to be parsed and piped to the output. Listeners are
	 * notified of the tags they registered interest in. Listeners may change
	 * the tags before they go to the output.
	 */
	@Override
	public void parse() {
		final char[] cina = r.toCharArray();
		final int in = cina.length;
		cout = new StringBuffer(WRITE_BUFF_SIZE);
		char cin;
		int mode = PASSTHRU;
		StringBuffer ci = new StringBuffer(TAG_BUFF_SIZE);
		for (int ii = 0; ii < in; ii++) {
			if (done)
				return;
			cin = cina[ii];
			switch (cin) {
			case ('<'):
				if (mode == MID_TAG) {
					cout.append(ci);
					ci = new StringBuffer(TAG_BUFF_SIZE);
				}
				mode = BEGIN_TAG;
				break;
			case ('>'):
				mode = END_TAG;
				break;
			case (' '): // If you run into a space, cancel the tag
				if (mode == BEGIN_TAG) {
					cout.append(ci);
					ci = new StringBuffer(TAG_BUFF_SIZE);
					mode = PASSTHRU;
					break;
				}
				// else continue to default case. NO BREAK.
			default:
				if (mode == BEGIN_TAG)
					mode = MID_TAG;
				if (mode == END_TAG)
					mode = PASSTHRU;
				break;
			}
			switch (mode) {
			case (BEGIN_TAG):
				ci.append(cin);
				break;
			case (MID_TAG):
				ci.append(cin);
				if (elideTo != null)
					try {
						final String s = ci.toString();
						if (!s.startsWith(elideTo.substring(0, s.length()))) {
							// System.out.println("tag "+s+" does not start with
							// "+elideTo.substring(0,s.length()));
							mode = PASSTHRU;
							if ((w != null) && (writing))
								cout.append(ci.toString());
							ci = new StringBuffer(TAG_BUFF_SIZE);
						}
					} catch (final Exception mustBeOK) {
						// System.out.println("mustBeOK");
					}
				break;
			case (END_TAG):
				ci.append(cin);
				elideTo = null;

				if (ci.toString().matches(
						"</?[a-zA-Z:]+((\\s+[\\w-]+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>")) { // If
																													// it's
																													// actually
																													// a
																													// tag
					final String co = processTagText(ci.toString());
					if ((w != null) && (writing))
						cout.append(co);
					if (waitToWrite) {
						waitToWrite = false;
						writing = true;
					}
				} else { // Does not follow valid XML/HTML tag constructions
					cout.append(ci.toString());
				}
				ci = new StringBuffer(TAG_BUFF_SIZE);
				mode = PASSTHRU;
				break;
			default:
				if ((w != null) && (writing))
					cout.append(cin);
			}
		}
		if (ci.length() > 0) // If you have leftovers, which will happen if you
								// have a < that
			cout.append(ci); // just has alphanumeric characters and white space
								// following it

		if (w != null)
			if (writing) {
				w.append(cout.toString());
				cout = new StringBuffer(WRITE_BUFF_SIZE);
			}
	}
}
