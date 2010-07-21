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

package com.solertium.util.restlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

/**
 * This Representation will gobble up the wrapped Representation and store the resulting
 * bytes locally. Subsequent calls to this object's write method will wrap the contents 
 * of the byte array in a new InputRepresentation each time, allowing this Representation
 * to be cached and reused as a response Entity. Reconsitution also occurs when release
 * or exhaust is called.
 * 
 * @author adam.schwartz
 *
 */
public class CacheableRepresentation extends WrapperRepresentation {
	
	private byte [] contents;
	private Representation wrapped;
	private MediaType mediaType;
	private CharacterSet charSet;
	private Date expiry;
	private Date lastMod;
	private Encoding encoding;
	
	/**
	 * Create a new, unencoded Cacheable Representation.
	 * 
	 * @param wrappedRepresentation - the Representation to cache
	 * @throws IOException
	 */
	public CacheableRepresentation(Representation wrappedRepresentation) throws IOException {
		this(null, wrappedRepresentation);
	}
	
	/**
	 * Create a new Cacheable Representation. Supply a value for encoding if you want the
	 * wrappedRepresentation to be encoded, null if you don't or if the wrappedRepresentation
	 * is already an EncodedRepresentation.
	 * 
	 * @param encoding - a Restlet Encoding type, or null
	 * @param wrappedRepresentation - the Representation to cache
	 * @throws IOException
	 */
	public CacheableRepresentation(Encoding encoding, Representation wrappedRepresentation) throws IOException {
        super(wrappedRepresentation);
        
        this.encoding = encoding;
        mediaType = wrappedRepresentation.getMediaType();
        charSet = wrappedRepresentation.getCharacterSet();
        expiry = wrappedRepresentation.getExpirationDate();
        lastMod = wrappedRepresentation.getModificationDate();

        //Create the Encoded representation, and steal its bytes
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        
        if( encoding == null || wrappedRepresentation instanceof EncodeRepresentation )
        	super.write(b);
        else
        	new EncodeRepresentation(encoding, wrappedRepresentation).write(b);
        
        contents = b.toByteArray();
        
        this.wrapped = regenerateWrappedRepresentation();      
    }
	
	@Override
	public void release() {
		super.release();
		this.wrapped = regenerateWrappedRepresentation(); 
	}
	
	@Override
	public long exhaust() throws IOException {
		long ret = super.exhaust();
		this.wrapped = regenerateWrappedRepresentation(); 
        return ret;
	}
	
	@Override
	public Representation getWrappedRepresentation() {
		if( wrapped == null )
			return super.getWrappedRepresentation();
		else
			return wrapped;
	}
	
	@Override
	public long getSize() {
		if( contents != null )
			return contents.length;
		else
			return super.getSize();
	}
	
	public Representation regenerateWrappedRepresentation() {
		 Representation rep = new InputRepresentation(new ByteArrayInputStream(contents), mediaType);
		 
		 if( encoding != null ) {
			 List<Encoding> encodings = new ArrayList<Encoding>();
			 encodings.add(encoding);
			 rep.setEncodings(encodings);
		 }
			
		 rep.setCharacterSet(charSet);
		 rep.setExpirationDate(expiry);
		 rep.setModificationDate(lastMod);
		 
		 return rep;
	}
	
	@Override
	public void write(OutputStream outputStream) throws IOException {
		if( wrapped != null )
			wrapped.write(outputStream);
		else
			super.write(outputStream);
	}
	
	@Override
	public void write(WritableByteChannel writableChannel) throws IOException {
		if( wrapped != null )
			wrapped.write(writableChannel);
		else
			super.write(writableChannel);
	}
	
	@Override
	public void write(Writer writer) throws IOException {
		if( wrapped != null )
			wrapped.write(writer);
		else
			super.write(writer);
	}
}
