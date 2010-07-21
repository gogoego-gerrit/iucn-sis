package org.iucn.sis.server.utils;

public class XMLUtils {
	public static String clean(Object cleanMe) {
		if (cleanMe instanceof String)
			return clean((String) cleanMe);

		return null;
	}

	public static String clean(String cleanMe) {
		if (cleanMe != null) {
			cleanMe = cleanMe.replaceAll("(?!&amp;)(?!&lt;)(?!&gt;)(?!&quot;)&", "&amp;");
			cleanMe = cleanMe.replaceAll("<", "&lt;");
			cleanMe = cleanMe.replaceAll(">", "&gt;");
			cleanMe = cleanMe.replaceAll("\"", "&quot;");

			return cleanMe;
		}

		return "";
	}

	public static String cleanFromXML(String cleanMe) {
		if (cleanMe != null) {
			cleanMe = cleanMe.replaceAll("&amp;", "&");
			cleanMe = cleanMe.replaceAll("&lt;", "<");
			cleanMe = cleanMe.replaceAll("&gt;", ">");
			cleanMe = cleanMe.replaceAll("&quot;", "\"");

			return cleanMe;
		}

		return "";
	}

	public static String cleanNonTagCharacters(String cleanMe) {
		if (cleanMe != null) {
			cleanMe = cleanMe.replaceAll("(?!&amp;)(?!&lt;)(?!&gt;)(?!&quot;)&", "&amp;");
			cleanMe = cleanMe.replaceAll("<", "&lt;");
			cleanMe = cleanMe.replaceAll(">", "&gt;");
			cleanMe = cleanMe.replaceAll("\"", "&quot;");

			return cleanMe;
		}

		return "";
	}
}
