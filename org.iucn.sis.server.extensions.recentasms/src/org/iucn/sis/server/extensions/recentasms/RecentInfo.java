package org.iucn.sis.server.extensions.recentasms;

import java.util.Map;

import com.solertium.lwxml.shared.utils.RowData;

public abstract class RecentInfo<T> extends RowData {
	
	protected abstract void parse(T data);
	
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

}
