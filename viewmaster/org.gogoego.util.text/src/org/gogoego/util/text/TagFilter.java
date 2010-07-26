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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * TagFilter provides a fast callback interface for dealing with ill-formed
 * "real world" HTML documents. It is instantiated as a filter between a Reader
 * and a Writer, and modulates the output that is written, based on the action
 * of plug-in TagListeners.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class TagFilter {

	public static class InteriorIDListener extends BaseTagListener {

		int ctr = 0;
		String id;
		List<String> l = new ArrayList<String>();
		String matchtag = null;

		public InteriorIDListener(final String id) {
			this.id = id;
			l.add("*");
		}

		public List<String> interestingTagNames() {
			return l;
		}

		public void process(final TagFilter.Tag t) throws IOException {
			if (t.name.startsWith("/")) {
				if ((matchtag != null) && matchtag.equals(t.name.substring(1))) {
					ctr--;
					if (ctr < 1) {
						matchtag = null;
						parent.stopWritingBeforeTag();
					}
				}
			} else if (id.equals(t.getAttribute("id"))) {
				matchtag = t.name;
				ctr = 0;
				parent.startWritingAfterTag();
			} else if ((matchtag != null) && matchtag.equals(t.name))
				ctr++;
			return;
		}

	}

	/**
	 * Extracts the interior of a certain tag (or set of tags) within an HTML
	 * file.
	 */
	public static class InteriorListener extends BaseTagListener {

		List<String> l = new ArrayList<String>();

		public InteriorListener(final String tag) {
			l.add(tag);
			l.add("/" + tag);
		}

		public List<String> interestingTagNames() {
			return l;
		}

		public void process(final TagFilter.Tag t) throws IOException {
			if (t.name.startsWith("/"))
				parent.stopWritingBeforeTag();
			else
				parent.startWritingAfterTag();
			return;
		}

	}

	/**
	 * Inner interface for representing Listener objects. A Listener is an
	 * object which registers an interest in a list of tag names, and is
	 * equipped to process them (perhaps even making modifications.)
	 */
	public interface Listener {
		public List<String> interestingTagNames();

		public void process(TagFilter.Tag t) throws IOException;

		public void setTagFilter(TagFilter tf);
	}

	/**
	 * Inner class for representing Tag objects. This is a lightweight class
	 * whose member variables are accessed directly for efficiency inside the
	 * TagFilter.
	 */
	public static class Tag {
		public Map<String, String> attr = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String put(String key, String value) {
				return super.put(key.toLowerCase(), value);
			}
			public String get(Object key) {
				return super.get(key.toString().toLowerCase());
			}
		};
		public String name = null;
		public String newTagText = null;
		public String originalTagText = null;

		/**
		 * This utility method abstracts the common operation of resetting a
		 * particular tag's attribute to a different value. It loses the quotes,
		 * if any, originally used in the tag. If you need to preserve the
		 * original quotation style (single, double, or no) then you must
		 * manipulate the attr Map directly.
		 * 
		 * Returns null if there is no attribute by that name.
		 */
		public String getAttribute(final String key) {
			String found = attr.get(key);
			if (found != null)
				if ((found.startsWith("'")) || (found.startsWith("\"")))
					found = found.substring(1, found.length() - 1);
			return found;
		}

		/**
		 * If changes have been made, this rewrites the tag's internal structure
		 * in (x)html format. The original tag syntax is preserved as closely as
		 * possible, except that double quotes will be added to attributes which
		 * formerly had none (because this does no harm, and may do much good)
		 */
		public void rewrite() {
			final StringBuilder nsb = new StringBuilder(
					originalTagText.length() + 32);
			nsb.append('<');
			nsb.append(name);
			for (final String k : attr.keySet()) {
				nsb.append(' ');
				nsb.append(k);
				final String v = attr.get(k);
				if (v != null) {
					nsb.append('=');
					nsb.append(v);
				}
			}
			if (originalTagText.endsWith("/>"))
				nsb.append("/>");
			else
				nsb.append('>');
			newTagText = nsb.toString();
		}

		/**
		 * This utility method abstracts the common operation of resetting a
		 * particular tag's attribute to a different value.
		 */
		public void setAttribute(final String k, final String v) {
			final String ov = attr.get(k);
			if (ov != null) {
				final char qchar = ov.charAt(0);
				attr.put(k, qchar + v + qchar);
			} else
				attr.put(k, "\"" + v + "\"");
		}
	}

	final static int BEGIN_TAG = 1;
	final static boolean d = false;
	final static int END_TAG = 2;

	final static Logger log = Logger.getLogger(TagFilter.class.getName());
	final static int MID_TAG = 3;
	final static int PARSE_BUFF_SIZE = 512;
	final static int PASSTHRU = 0;

	final static int T_ATTNAME = 3;
	final static int T_ATTVALUE = 4;
	final static int T_ESCAPE = 1;
	final static int T_TAGNAME = 2;

	final static int TAG_BUFF_SIZE = 128;
	final static int TAGPART_BUFF_SIZE = 64;
	final static int WRITE_BUFF_SIZE = PARSE_BUFF_SIZE + 256;

	StringBuilder cout;

	private boolean done = false;
	private String elideTo = null;
	private final Map<String, String> fullyElided = new HashMap<String, String>();
	private final Map<String, List<TagFilter.Listener>> listenersByTagName = new HashMap<String, List<TagFilter.Listener>>();

	private Writer orig;

	private final Reader r;

	// flag to decide whether closing tags are exposed
	public boolean shortCircuitClosingTags = true;

	private Writer w;

	private boolean waitToWrite = false;

	private boolean writing = true;

	/** For convenience; see TagFilter(Reader r, Writer w) */
	public TagFilter(final Reader r) {
		this.r = r;
		w = null;
	}

	/**
	 * Creates a new instance of TagFilter. This is the principal constructor
	 * and should be used when possible. For performance, be sure to supply a
	 * BufferedReader and BufferedWriter. This filter does not attempt to do its
	 * own buffering. Writer may be null if you do not need to emit output, but
	 * only wish to feed data to Listeners; you may also use the one-argument
	 * constructor below.
	 */
	public TagFilter(final Reader r, final Writer w) {
		this.r = r;
		this.w = w;
	}

	public void divert(final Writer diverted) {
		try {
			w.write(cout.toString());
			cout = new StringBuilder(WRITE_BUFF_SIZE);
		} catch (final Exception unreported) {
		}
		orig = w;
		w = diverted;
	}

	public void extractInteriorOf(final String tagname) throws IOException {
		shortCircuitClosingTags = false;
		stopWritingBeforeTag();
		registerListener(new TagFilter.InteriorListener(tagname));
		parse();
	}

	public void extractInteriorOfID(final String id) throws IOException {
		shortCircuitClosingTags = false;
		stopWritingBeforeTag();
		registerListener(new TagFilter.InteriorIDListener(id));
		parse();
	}

	/**
	 * No tags should be triggered in the body of the element named here. Thus,
	 * by specifying fullyElide("script"), all tags or taglike entities between
	 * the initial &lt;script&gt; tag and its corresponding &lt;/script&gt; tag
	 * will be ignored.
	 * 
	 * @param toElide
	 */
	public void fullyElide(final String toElide) {
		fullyElided.put(toElide, "");
	}

	/**
	 * We do not need to parse all the attributes of tags that no listeners care
	 * about ... they are written verbatim to the output. As soon as we know the
	 * tag name, therefore, we call this method to see whether to continue
	 * processing.
	 */
	public boolean noListenersCare(final String tagname) {
		// there is a global listener installed, so definitely false
		if (listenersByTagName.get("*") != null)
			return false;
		if (listenersByTagName.get(tagname.toLowerCase()) == null)
			return true;
		return false;
	}

	/**
	 * Called when a tag parse is completed, to notify all listeners interested
	 * in that tag.
	 */
	private void notifyListeners(final TagFilter.Tag t) throws IOException {
		if (d)
			log.fine("notifyListeners(\"" + t.name + "\");");
		List<TagFilter.Listener> list = listenersByTagName.get("*");
		if (list != null)
			for (final TagFilter.Listener hearMe : list)
				hearMe.process(t);
		list = listenersByTagName.get(t.name.toLowerCase());
		if (list != null)
			for (final TagFilter.Listener hearMe : list)
				hearMe.process(t);
	}

	/**
	 * Causes the input to be parsed and piped to the output. Listeners are
	 * notified of the tags they registered interest in. Listeners may change
	 * the tags before they go to the output.
	 */
	public void parse() throws IOException {
		final char[] cina = new char[PARSE_BUFF_SIZE];
		cout = new StringBuilder(WRITE_BUFF_SIZE);
		char cin;
		int in;
		int mode = PASSTHRU;
		StringBuilder ci = new StringBuilder(TAG_BUFF_SIZE);
		do {
			in = r.read(cina);
			if (in > -1)
				for (int ii = 0; ii < in; ii++) {
					if (done)
						return;
					cin = cina[ii];
					switch (cin) {
					case ('<'):
						if(mode == MID_TAG) {
							cout.append(ci);
							ci = new StringBuilder(TAG_BUFF_SIZE);
						}
						mode = BEGIN_TAG;
						break;
					case ('>'):
						mode = END_TAG;
						break;
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
						if (elideTo != null) {
							final String s = ci.toString();
							if (elideTo.length() >= s.length())
								if (!s.startsWith(elideTo.substring(0, s
										.length()))) {
									mode = PASSTHRU;
									if ((w != null) && (writing))
										cout.append(ci.toString());
									ci = new StringBuilder(TAG_BUFF_SIZE);
								}
						}
						break;
					case (END_TAG):
						ci.append(cin);
						elideTo = null;
						final String co = processTagText(ci.toString());
						if ((w != null) && (writing))
							cout.append(co);
						if (waitToWrite) {
							waitToWrite = false;
							writing = true;
						}
						ci = new StringBuilder(TAG_BUFF_SIZE);
						break;
					default:
						if ((w != null) && (writing))
							cout.append(cin);
					}
				}
			if (w != null)
				if (writing) {
					w.write(cout.toString());
					cout = new StringBuilder(WRITE_BUFF_SIZE);
				}
		} while (in > -1);
		r.close();
	}

	/**
	 * This method reflects the parse tree for the tags. It follows the typical
	 * state machine pattern, but does a little extra conditional processing to
	 * deal with sloppy vagaries of human written HTML. This method should be
	 * considered "no user serviceable parts inside" unless you really intend to
	 * change the tag parser's behavior in major ways. If you do, the comments
	 * in the source should help clarify intent.
	 */
	private String processTagText(final String s) throws IOException {
		if (d)
			log.fine("processTagText(\"" + s + "\");");
		if (shortCircuitClosingTags)
			if (s.indexOf('/') == 1)
				return s;
		// load up new tag
		final TagFilter.Tag t = new TagFilter.Tag();
		t.originalTagText = s;
		// setup defaults for parsing operation
		StringBuilder cbuff = new StringBuilder(TAGPART_BUFF_SIZE);
		String cattr = null;
		boolean valueIsDone = false;
		char valueQuote = '"';
		boolean valueIsQuoted = false;
		int mode = T_TAGNAME;
		try {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				switch (mode) {
				case T_TAGNAME:
					switch (c) {
					case (' '):
					case ('\n'):
					case ('\r'):
					case ('\t'):
						// whitespace, signals end of tagname
					case ('>'):
						// as does ending bracket
						t.name = cbuff.toString();
						// now ... we can short circuit right here
						// if we can establish that no listeners care
						// about this tag name ... why parse the rest
						// of the tag?
						if (noListenersCare(t.name))
							return s;
						if (fullyElided.containsKey(t.name))
							elideTo = "</" + t.name;
						cbuff = new StringBuilder(TAGPART_BUFF_SIZE);
						mode = T_ATTNAME;
						break;
					case ('<'):
						// opening tag not relevant
					case ('\\'):
						// escape char illegal here
					case ('\''):
					case ('"'):
						// quotes, illegal here
						break;
					default:
						// other characters
						cbuff.append(c);
						break;
					}
					break;
				case T_ATTNAME:
					switch (c) {
					case ('>'):
					case ('='):
						// signals end of attname
						if (cbuff.length() > 0) {
							cattr = cbuff.toString();
							t.attr.put(cattr, null);
						}
						cbuff = new StringBuilder(TAGPART_BUFF_SIZE);
						mode = T_ATTVALUE;
						break;
					case (' '):
					case ('\n'):
					case ('\r'):
					case ('\t'):
						// whitespace, ignored
					case ('\\'):
						// escape char illegal here
					case ('\''):
					case ('"'):
						// quotes, illegal here
					case ('/'):
						// self-enclosure slash, ignored
						break;
					default:
						// other characters
						cbuff.append(c);
						break;
					}
					break;
				case T_ATTVALUE:
					switch (c) {
					case ('\''):
					case ('"'):
						// quotes, may signal begin/end of attvalue
						if (!valueIsQuoted) {
							// we don't have a quote setting yet. This is it!
							valueIsQuoted = true;
							valueQuote = c;
						} else if (valueQuote == c) {
							// same as our quote setting, signals end
							valueIsDone = true;
							break;
						} else {
							// valid character data
							cbuff.append(c);
							break;
						}
						break;
					case ('>'):
						// signals end of value (sloppy, often in HTML though)
						valueIsDone = true;
						break;
					case ('/'):
						// a self-enclosing slash follows the whitespace rules
					case (' '):
					case ('\n'):
					case ('\r'):
					case ('\t'):
						// whitespace is significant -- inside quotes only
						if (valueIsQuoted)
							cbuff.append(c);
						else
							// unquoted, signals end of value (HTML style)
							valueIsDone = true;
						break;
					case ('\\'):
						// escape chars are kept,
						cbuff.append(c);
						// and respected,
						// so advance immediately to next char.
						i++;
						c = s.charAt(i);
						cbuff.append(c);
						break;
					default:
						// other characters are appended
						cbuff.append(c);
						break;
					}
					if (valueIsDone) {
						t.attr.put(cattr, valueQuote + cbuff.toString()
								+ valueQuote);
						// reset all the value parsing stuff
						cattr = null;
						cbuff = new StringBuilder(TAGPART_BUFF_SIZE);
						valueIsDone = false;
						valueIsQuoted = false;
						valueQuote = '"';
						mode = T_ATTNAME;
					}
					break;
				}
			}
			notifyListeners(t);
		} catch (final RuntimeException e) {
			// It is reasonably difficult to get here.
			// Warn about this tag, but do not fail the parse
			log.warning("TagFilter failed on tag: " + t.originalTagText);
		}
		if (t.newTagText == null)
			return s;
		else
			return t.newTagText;
	}

	/** Register a listener to receive (and possibly modify) tags. */
	public void registerListener(final TagFilter.Listener l) {
		l.setTagFilter(this);
		for (String name : l.interestingTagNames()) {
			name = name.toLowerCase();
			if (listenersByTagName.get(name) == null)
				listenersByTagName.put(name,
						new ArrayList<TagFilter.Listener>());
			listenersByTagName.get(name).add(l);
		}
	}

	public void startWritingAfterTag() {
		cout = new StringBuilder(WRITE_BUFF_SIZE);
		waitToWrite = true;
	}

	public void stopDiverting() throws IOException {
		w.write(cout.toString());
		cout = new StringBuilder(WRITE_BUFF_SIZE);
		w = orig;
	}

	public void stopParsing() {
		done = true;
	}

	public void stopWritingBeforeTag() throws IOException {
		if(cout!=null) w.write(cout.toString());
		cout = new StringBuilder(WRITE_BUFF_SIZE);
		writing = false;
	}

	/**
	 * Arbitrarily write data on the output writer, if there is one. Ignores the
	 * "writing" setting.
	 */
	public void write(final String s) throws IOException {
		if (w != null) {
			w.write(cout.toString());
			cout = new StringBuilder(WRITE_BUFF_SIZE);
			w.write(s);
		}
	}

	/**
	 * Arbitrarily write data on the output writer, if there is one. Respects
	 * the "writing" setting; will not write if output writing has been turned
	 * off elsewhere.
	 */
	public void writeIfWriting(final String s) throws IOException {
		if (writing)
			write(s);
	}
	
	public static void main(String[] args) throws IOException {
		Writer w = new PrintWriter(System.out);
		TagFilter tf = new TagFilter(new StringReader("aaa<script>for(i=1;i<3;i++)</script>bbb"),w);
		tf.extractInteriorOf("script");
		w.flush();
	}

}
