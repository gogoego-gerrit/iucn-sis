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

import java.io.Serializable;

public class ByteArray implements Serializable
{
	private static final long serialVersionUID = -3327822241654199553L;
	private byte [] array;
	
	public ByteArray() {}
	
	public ByteArray( byte [] a ) 
	{
		array = a;
	}
	
	public ByteArray( String a )
	{
		array = new byte [a.length()];
		
		for( int i = 0; i < a.length(); i++ )
			array[i] = Byte.parseByte( "" + a.charAt(i) );
	}
	
	public byte[] getArray() {
		return array;
	}
	
	public void setArray(byte[] array) {
		this.array = array;
	}
	
	public int length()
	{
		return array.length;
	}
	
	public boolean equals(Object obj)
	{
		if( obj instanceof byte [])
			return equals( (byte[])obj );
		else if( obj instanceof ByteArray )
			return equals( ((ByteArray)obj).getArray() );
		else if( obj instanceof String )
			return obj.toString().equalsIgnoreCase( toString() );
		else
			return false;
	}
	
	public boolean equals( ByteArray a )
	{
		return equals( a.getArray() );
	}
	
	public int hashCode() 
	{
		return toString().hashCode();
	}
	
	public boolean equals(byte [] temp) 
	{
		if( array.length != temp.length )
				return false;
			
		for( int i = 0; i < temp.length; i++ )
			if( temp[i] != array[i] )
				return false;
			
		return true;
	}
	
	/**
	 * Writes each byte of the session key as a character in a string.
	 */
	public String toString() {
		String ret = "";
		
		for( int i = 0; i < array.length; i++ )
			ret += array[i];
		
		return ret;
	}
}
