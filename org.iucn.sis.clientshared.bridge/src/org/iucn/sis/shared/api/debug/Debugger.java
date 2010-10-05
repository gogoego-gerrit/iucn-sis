package org.iucn.sis.shared.api.debug;

public interface Debugger {
	
	public void println(Throwable e);
	
	public void println(Object obj);
	
	public void println(String template, Object... args);

}
