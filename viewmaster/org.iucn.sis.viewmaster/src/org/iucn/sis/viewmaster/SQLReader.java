package org.iucn.sis.viewmaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

public class SQLReader extends ArrayList<String> {
	
	private static final long serialVersionUID = 1L;
	
	public SQLReader(String filename) throws IOException {
		this(SQLReader.class.getResourceAsStream(filename));
	}
	
	public SQLReader(InputStream source) throws IOException {
		this(new InputStreamReader(source));
	}
	
	public SQLReader(Reader reader) throws IOException {
		final BufferedReader buff = new BufferedReader(reader);
			
		StringBuilder read = new StringBuilder();
		String line = null;
			
		while ((line = buff.readLine()) != null) {
			if (!line.startsWith("--"))
				read.append(line + "\n");
			if (line.endsWith(";")) {
				String sql = read.toString();
				add(sql);
				read = new StringBuilder();
			}
		}
		
		try {
			buff.close();
		} catch (IOException e) {
			//meh
		}
	}

}
