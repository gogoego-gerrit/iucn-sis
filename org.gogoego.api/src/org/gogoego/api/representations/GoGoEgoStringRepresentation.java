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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

/**
 * GoGoEgoStringRepresentation.java
 * 
 * Representation for a string.  Like StringRepresentation, it caches 
 * the version it has.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class GoGoEgoStringRepresentation extends GoGoEgoBaseRepresentation {
	
	private CharSequence text;

	public GoGoEgoStringRepresentation(CharSequence text, Language language) {
		this(text, MediaType.TEXT_HTML, language);
	}

	public GoGoEgoStringRepresentation(CharSequence text, MediaType mediaType, Language language, CharacterSet characterSet) {
		this(text, mediaType);
	}

	public GoGoEgoStringRepresentation(CharSequence text, MediaType mediaType, Language language) {
		this(text, mediaType);
	}

	public GoGoEgoStringRepresentation(CharSequence text) {
		this(text, MediaType.TEXT_HTML);
	}
	
	public GoGoEgoStringRepresentation(CharSequence text, MediaType mediaType) {
		super(mediaType);
		this.text = text;
	}
		
    public String getText() {
        return (this.text == null) ? null : this.text.toString();
    }
	
    public void write(OutputStream outputStream) throws IOException {
    	if (getText() != null) {
            OutputStreamWriter osw = null;

            if (getCharacterSet() != null) {
                osw = new OutputStreamWriter(outputStream, getCharacterSet()
                        .getName());
            } else {
                osw = new OutputStreamWriter(outputStream);
            }

            osw.write(getText());
            osw.flush();
        }
    }
   
    public InputStream getStream() throws IOException {
    	if (getText() != null) {
    		if (getCharacterSet() != null) {
    			return new ByteArrayInputStream(getText().getBytes(
    					getCharacterSet().getName()));
    		}
    		return new ByteArrayInputStream(getText().getBytes());
    	}

    	return null;
    }
   
    /**
     * Closes and releases the input stream.
     */
    public void release() {
    	setText(null);
    	super.release();
    }

    public void setCharacterSet(CharacterSet characterSet) {
    	super.setCharacterSet(characterSet);
    	updateSize();
    }
   
    /**
     * Sets the string value.
     * 
     * @param text
     *            The string value.
     */
    public void setText(String text) {
    	this.text = text;
    	updateSize();
    }
    
    public void setContent(String content) {
    	setText(content);
    }
   
    /**
     * Updates the expected size according to the current string value.
     */
    protected void updateSize() {
    	if (getText() != null) {
    		try {
    			if (getCharacterSet() != null) {
    				setSize(getText().getBytes(getCharacterSet().getName()).length);
    			} else {
    				setSize(getText().getBytes().length);
    			}
    		} catch (UnsupportedEncodingException e) {
    			Context.getCurrentLogger().log(Level.WARNING,
    					"Unable to update size", e);
    			setSize(UNKNOWN_SIZE);
    		}
    	} else {
    		setSize(UNKNOWN_SIZE);
    	}
    }

}
