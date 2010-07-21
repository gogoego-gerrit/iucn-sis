package org.iucn.sis.server.utils.logging;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class EventLogger extends Timer {

	private ArrayList<EventBuffer> eventBuffers;
	private static final int DELAY = 15000;

	public final static EventLogger impl = new EventLogger();

	private TimerTask bufferFlush = new TimerTask() {
		@Override
		public void run() {
			flushBuffers();

		}
	};

	private EventLogger() {
		eventBuffers = new ArrayList<EventBuffer>();
		scheduleAtFixedRate(bufferFlush, DELAY, DELAY);

	}

	public void addBuffer(EventBuffer event) {
		eventBuffers.add(event);
	}

	public void flushBuffers() {
		// SysDebugger.getInstance().println("Flushing EventLog Buffers...");
		for (int i = 0; i < eventBuffers.size(); i++) {
			if (eventBuffers.get(i).getBufferSize() > 0)
				eventBuffers.get(i).flush();
		}

	}

}
