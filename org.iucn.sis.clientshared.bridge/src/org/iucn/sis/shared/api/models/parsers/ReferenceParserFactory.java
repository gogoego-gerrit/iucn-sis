package org.iucn.sis.shared.api.models.parsers;


public class ReferenceParserFactory {
	
	private static ReferenceParserFactory impl;
	
	public static ReferenceParserFactory get() {
		if (impl == null)
			impl = new ReferenceParserFactory();
		return impl;
	}
	
	public static ReferenceParser getParser() {
		return get().parser;
	}
	
	private ReferenceParser parser;
	
	private ReferenceParserFactory() {
		parser = new ReferenceParser();
	}
	
	public void setParser(ReferenceParser parser) {
		this.parser = parser;
	}

}
