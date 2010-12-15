package org.iucn.sis.client.api.debug;

import org.iucn.sis.shared.api.debug.Debugger;

public class LiveDebugger implements Debugger {
	
	@Override
	public void println(Throwable e) {
		// No reason to print :)
	}

	@Override
	public void println(Object obj) {
		// No reason to print :)
	}

	@Override
	public void println(String template, Object... args) {
		// No reason to print :)
	}

}
