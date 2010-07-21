package org.iucn.sis.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SISHashIndexer implements SearchIndexType {

	HashMap<String, String> index;
	HashMap<String, Long> lastModified;

	public SISHashIndexer() {
		index = new HashMap<String, String>();
		lastModified = new HashMap<String, Long>();
	}

	public String get(String key) {
		return index.get(key);
	}

	public long getLastModified(String key) {
		if (lastModified.containsKey(key)) {
			return lastModified.get(key).longValue();
		}
		return 0;
	}

	public void index(Document doc, long lastModified) {
		Element root = doc.getDocumentElement();
		String id = root.getAttribute("id");
		this.lastModified.put(id + ".xml", Long.valueOf(lastModified));
		// SysDebugger.getInstance().println(this.lastModified.get(id+".xml"));
		NodeList commonNames = doc.getElementsByTagName("commonName");
		String name = doc.getDocumentElement().getAttribute("name");

		// put scientific name in search
		String currentName = index.get(name.toLowerCase());
		if (currentName == null) {
			index.put(name.toLowerCase(), id);
		} else {
			ArrayList<String> items = new ArrayList<String>(Arrays.asList(currentName.split(",")));
			if (!items.contains(id))
				items.add(id);
			String toWrite = "";
			for (int p = 0; p < items.size(); p++) {
				if (p > 0)
					toWrite += ",";
				toWrite += items.get(p).toLowerCase();
			}

			index.put(name.toLowerCase(), toWrite);

		}

		// put common name in search
		for (int i = 0; i < commonNames.getLength(); i++) {
			String[] split = commonNames.item(i).getTextContent().trim().split(" ");
			for (int j = 0; j < split.length; j++) {
				String current = index.get(split[j].toLowerCase());
				if (current == null) {
					index.put(split[j].toLowerCase(), id);
				} else {
					ArrayList<String> items = new ArrayList<String>(Arrays.asList(current.split(",")));
					if (!items.contains(id))
						items.add(id);
					String toWrite = "";
					for (int p = 0; p < items.size(); p++) {
						if (p > 0)
							toWrite += ",";
						toWrite += items.get(p).toLowerCase();
					}
					// SysDebugger.getInstance().println(split[j] + " "+
					// toWrite);
					index.put(split[j].toLowerCase(), toWrite);

				}
			}
		}

	}
}
