package org.iucn.sis.server.extensions.recentasms;

import java.util.Map;

import org.hibernate.Session;

import com.solertium.lwxml.shared.utils.RowData;

public abstract class RecentInfo<T> extends RowData {
	
	private static final long serialVersionUID = 1L;
	
	protected final Session session;
	
	public RecentInfo(Session session) {
		this.session = session;
	}
	
	protected abstract void parse(T data) throws ParseException;
	
	public String toXML() {
		if (isEmpty())
			return "";
		
		StringBuilder out = new StringBuilder();
		out.append("<row>");
		for (Map.Entry<String, String> entry : entrySet())
			out.append("<field name=\"" + entry.getKey() + "\"><![CDATA[" + entry.getValue() + "]]></field>");
		out.append("</row>");
		
		return out.toString();
	}
	
	public static class ParseException extends Exception {
		
		private static final long serialVersionUID = 2L;
		
		public ParseException() {
			super();
		}
		
		public ParseException(String message) {
			super(message);
		}
		
		public ParseException(Throwable cause) {
			super(cause);
		}
		
		public ParseException(String message, Throwable cause) {
			super(message, cause);
		}
		
	}

}
