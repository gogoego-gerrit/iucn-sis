package org.iucn.sis.server.extensions.findreplace;

public class StringToRegex {

	public static String stringToRegex(String string) {
		string = string.replaceAll("\\t", "\\t");
		string = string.replaceAll("\\n", "\\n");
		string = string.replaceAll("\\r", "\\r");
		string = string.replaceAll("\\f", "\\f");
		string = string.replaceAll("\\a", "\\a");
		string = string.replaceAll("\\e", "\\e");
		string = string.replaceAll("\\-", "\\-");
		string = string.replaceAll("\\+", "\\+");
		string = string.replaceAll("\\.", "\\.");
		string = string.replaceAll("\\*", "\\*");
		string = string.replaceAll("\\?", "\\?");
		string = string.replaceAll("\\(", "\\(");
		string = string.replaceAll("\\)", "\\)");
		string = string.replaceAll("\\[", "\\[");
		string = string.replaceAll("\\]", "\\]");
		string = string.replaceAll("\\{", "\\{");
		string = string.replaceAll("\\}", "\\}");
		string = string.replaceAll("\\|", "\\|");
		string = string.replaceAll("\\$", "\\$");
		string = string.replaceAll("\\^", "\\^");
		string = string.replaceAll("\\<", "\\<");
		string = string.replaceAll("\\=", "\\=");
		string = string.replaceAll("\\'", "\\'");
		string = string.replaceAll("\\\\", "\\\\");
		string = string.replaceAll("\"", "\"");
		string = string.replaceAll("\\!", "\\!");
		string = string.replaceAll("\\&", "\\\\&");
		string = string.replaceAll("\\%", "\\%");
		string = string.replaceAll("\\@", "\\@");
		string = string.replaceAll("\\#", "\\#");
		// string = string.replaceAll("\\:", "\\:");
		string = string.replaceAll("\\;", "\\;");
		string = string.replaceAll("\"", "\"");
		string = string.replaceAll("\"", "\"");
		string = string.replaceAll("\"", "\"");
		// string = string.replaceAll("\\s", "\\s");

		return string;

	}
}
