package org.iucn.sis.server.utils.logging;

import java.util.ArrayList;

import org.w3c.dom.Document;

public abstract class EventBuffer {

	protected ArrayList<Document> events = new ArrayList<Document>();

	public void addEvent(Document e) {
		events.add(e);
	}

	public abstract void flush();

	public int getBufferSize() {
		return events.size();
	}
}
