/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package org.gogoego.api.representations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.logging.Level;

import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;

/**
 * GoGoEgoInputRepresentation.java
 * 
 * Wrapper for an InputRepresentation which also caches the text 
 * after the initial parse.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class GoGoEgoInputRepresentation extends GoGoEgoBaseRepresentation {
	
	 /** The representation's stream. */
    private volatile InputStream stream;
    private String text;

    /**
     * Constructor.
     * 
     * @param inputStream
     *            The representation's stream.
     * @param mediaType
     *            The representation's media type.
     */
    public GoGoEgoInputRepresentation(InputStream inputStream, MediaType mediaType) {
        this(inputStream, mediaType, UNKNOWN_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param inputStream
     *            The representation's stream.
     * @param mediaType
     *            The representation's media type.
     * @param expectedSize
     *            The expected input stream size.
     */
    public GoGoEgoInputRepresentation(InputStream inputStream, MediaType mediaType, long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
        setTransient(true);
        setStream(inputStream);
    }

    @Override
    public InputStream getStream() throws IOException {
    	if (this.stream == null) {
    		GoGoEgo.debug("error").println("Somehow, Restlet " +
    			"called InputRepresentation.getStream() a second " +
    			"time after reading the entity.  Going to send text " +
    			"representation as a byte stream instead of null."
    		);
    		return new ByteArrayInputStream(this.text.getBytes());
    	}
    	else {
	        final InputStream result = this.stream;
	        setStream(null);
	        return result;
    	}
    }
    
    public void setContent(String content) {
    	this.text = content;
    }

    public String getText() throws IOException {
    	if (text != null)
    		return text;
    	
    	final InputStream inputStream = getStream();
    	final CharacterSet characterSet = getCharacterSet();

    	String result = null;

    	if (inputStream != null) {
    		try {
    			if (characterSet != null) {
    				result = toString(new InputStreamReader(inputStream,
    						characterSet.getName()));
    			} else {
    				result = toString(new InputStreamReader(inputStream));
    			}
    		} catch (Exception e) {
    			// Returns an empty string
    		}
    	}

    	return text = result;
    }

    /**
     * Closes and releases the input stream.
     */
    public void release() {
        if (this.stream != null) {
            try {
                this.stream.close();
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Error while releasing the representation.", e);
            }
            this.stream = null;
        }
        super.release();
    }

    /**
     * Sets the input stream to use.
     * 
     * @param stream
     *            The input stream to use.
     */
    public void setStream(InputStream stream) {
        this.stream = stream;
        setAvailable(stream != null);
    }

    public void write(OutputStream outputStream) throws IOException {
    	final InputStream inputStream = getStream();
        int bytesRead;
        final byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
    }
    
    public static String toString(Reader reader) {
        String result = null;

        if (reader != null) {
            try {
                final StringBuilder sb = new StringBuilder();
                final BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader
                        : new BufferedReader(reader);
                char[] buffer = new char[8192];
                int charsRead = br.read(buffer);

                while (charsRead != -1) {
                    sb.append(buffer, 0, charsRead);
                    charsRead = br.read(buffer);
                }

                br.close();
                result = sb.toString();
            } catch (Exception e) {
                // Returns an empty string
            }
        }

        return result;
    }

}
