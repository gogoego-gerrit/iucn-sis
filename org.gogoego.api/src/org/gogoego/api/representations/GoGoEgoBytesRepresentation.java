package org.gogoego.api.representations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.MediaType;

/**
 * This Representation is reusable.
 * @author robheittman
 */
public class GoGoEgoBytesRepresentation extends GoGoEgoBaseRepresentation {

	private final byte[] bytes;

	public GoGoEgoBytesRepresentation(byte[] bytes, MediaType mt) {
		super(mt);
		this.bytes = bytes;
	}

	public static void copyStream(final InputStream is, final OutputStream os) throws IOException {
		final byte[] buf = new byte[65535];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
	}
	
	@Override
	public void write(OutputStream os) throws IOException {
		copyStream(new ByteArrayInputStream(bytes),os);
	}

	@Override
	public String getText() throws IOException {
		return new String(bytes);
	}

	@Override
	public void setContent(String content) {
		throw new UnsupportedOperationException("Can't reset the content of a byte representation");
	}

	@Override
	public InputStream getStream() throws IOException {
		return new ByteArrayInputStream(bytes);
	}
	
	public byte[] getBytes() {
		return bytes;
	}

}
