package org.iucn.sis.server.extensions.reports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

public class HTMLReader extends ArrayList<String> {
	
	private static final long serialVersionUID = 1L;
	protected StringBuilder read = new StringBuilder();
		
	public HTMLReader(String filename) throws IOException {		
		this(HTMLReader.class.getResourceAsStream(filename));
	}
	
	public HTMLReader(InputStream source) throws IOException {
		this(new InputStreamReader(source));
	}
	
	public HTMLReader(Reader reader) throws IOException {
		final BufferedReader buff = new BufferedReader(reader);
		String line = null;
			
		while ((line = buff.readLine()) != null) 
			read.append(line); 
		
		try {
			buff.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getHTML(){
		return this.read.toString();
	}

}
