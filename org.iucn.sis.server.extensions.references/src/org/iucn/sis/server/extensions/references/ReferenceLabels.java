package org.iucn.sis.server.extensions.references;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.restlet.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;
import com.solertium.util.Replacer;

public class ReferenceLabels {

	public static class LabelMappings {

		public static String normalize(String in) {
			if (in == null)
				return null;
			String out = Replacer.stripWhitespace(in.toLowerCase());
			return out;
		}

		private final Map<String, String> m;
		private final Map<String, String> reverse;
		private final Map<String, String> capitalized;

		private final List<String> o;

		public LabelMappings(final Map<String, String> m, final Map<String, String> reverse,
				final Map<String, String> capitalized, final List<String> o) {
			this.m = m;
			this.reverse = reverse;
			this.capitalized = capitalized;
			this.o = o;
		}

		public String get(final String s) {
			return m.get(normalize(s));
		}

		public String getCapitalized(final String s) {
			return capitalized.get(normalize(s));
		}

		public String getReverse(final String s) {
			return reverse.get(normalize(s));
		}

		public List<String> list() {
			return Collections.unmodifiableList(o);
		}

	}

	private final static ReferenceLabels instance = new ReferenceLabels();

	public final static String LABELS = "org.iucn.sis.server.ref.labels";

	private static final long serialVersionUID = 1L;

	public static ReferenceLabels getInstance() {
		return instance;
	}

	/**
	 * @deprecated use getInstance();
	 */
	@Deprecated
	public static ReferenceLabels loadFrom(final Context context) {
		return getInstance();
	}

	public static String typeNormalize(String in) {
		if (in == null)
			return "generic";
		String out = Replacer.stripWhitespace(in.toLowerCase());
		if ("book chapter".equals(out)) {
			out = "book section";
		}
		if ("internet".equals(out)) {
			out = "electronic source";
		}
		return out;
	}

	private final Map<String, LabelMappings> lm = new TreeMap<String, LabelMappings>();

	public ReferenceLabels() {
		Document structDoc = BaseDocumentUtils.impl.getInputStreamFile(
			ReferenceLabels.class.getResourceAsStream("reflabels.xml")
		);
		ElementCollection types = new ElementCollection(structDoc.getElementsByTagName("type"));
		for (Element tEl : types) {
			final String type = tEl.getAttribute("name");
			ElementCollection fields = new ElementCollection(tEl.getElementsByTagName("field"));
			final List<String> o = new ArrayList<String>();
			final Map<String, String> m = new HashMap<String, String>();
			final Map<String, String> reverse = new HashMap<String, String>();
			final Map<String, String> capitalized = new HashMap<String, String>();
			for (Element fEl : fields) {
				String label = fEl.getAttribute("label");
				String name = fEl.getAttribute("name");
				m.put(LabelMappings.normalize(name), label);
				reverse.put(LabelMappings.normalize(label), name);
				capitalized.put(LabelMappings.normalize(name), label);
				o.add(LabelMappings.normalize(name));
			}
			lm.put(LabelMappings.normalize(type), new LabelMappings(m, reverse, capitalized, o));
		}
	}

	public LabelMappings get(final String type) {
		try {
			return lm.get(LabelMappings.normalize(type));
		} catch (NullPointerException npe) {
			return null;
		}
	}

	public Set<String> listTypes() {
		return lm.keySet();
	}

	/**
	 * @deprecated now a singleton; not necessary to save to context;
	 */
	@Deprecated
	public void saveTo(final Context context) {
	}

}
