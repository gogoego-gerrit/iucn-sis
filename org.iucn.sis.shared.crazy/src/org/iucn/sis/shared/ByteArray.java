package org.iucn.sis.shared;

import java.io.Serializable;

public class ByteArray implements Serializable {
	private static final long serialVersionUID = -3327822241654199553L;
	private byte[] array;

	private int hash = 0;

	public ByteArray() {
	}

	public ByteArray(byte[] a) {
		array = a;
	}

	public ByteArray(String a) {
		array = new byte[a.length()];

		for (int i = 0; i < a.length(); i++)
			array[i] = Byte.parseByte("" + a.charAt(i));
	}

	public boolean equals(byte[] temp) {
		if (array.length != temp.length)
			return false;

		for (int i = 0; i < temp.length; i++)
			if (temp[i] != array[i])
				return false;

		return true;
	}

	public boolean equals(ByteArray a) {
		return equals(a.getArray());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof byte[])
			return equals((byte[]) obj);
		else if (obj instanceof ByteArray)
			return equals(((ByteArray) obj).getArray());
		else if (obj instanceof String)
			return obj.toString().equalsIgnoreCase(toString());
		else
			return false;
	}

	public byte[] getArray() {
		return array;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public int length() {
		return array.length;
	}

	public void setArray(byte[] array) {
		this.array = array;
	}

	/**
	 * Writes each byte of the session key as a character in a string.
	 */
	@Override
	public String toString() {
		String ret = "";

		for (int i = 0; i < array.length; i++)
			ret += array[i];

		return ret;
	}
}
