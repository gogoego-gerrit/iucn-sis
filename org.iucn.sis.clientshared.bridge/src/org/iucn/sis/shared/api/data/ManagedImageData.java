package org.iucn.sis.shared.api.data;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.user.client.ui.Image;

public class ManagedImageData {

	public static String getExtensionFromEncoding(String encoding) {
		if (encoding.equals(IMG_JPEG))
			return ".jpg";
		if (encoding.equals(IMG_GIF))
			return ".gif";
		if (encoding.equals(IMG_TIFF))
			return ".tiff";
		if (encoding.equals(IMG_PNG))
			return ".png";
		if (encoding.equals(IMG_BMP))
			return "bmp";
		return null;
	}

	public static boolean isAcceptedFormat(Image image) {
		return isAcceptedFormat(image.getUrl());
	}

	public static boolean isAcceptedFormat(String filename) {
		if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".gif")
				|| filename.toLowerCase().endsWith(".png") || filename.toLowerCase().endsWith(".tiff")
				|| filename.toLowerCase().endsWith(".bmp"))
			return true;
		return false;
	}


	protected int id;
	protected String encoding;
	protected HashMap fields;
	
	public static final String IMG_JPEG = "image/jpeg";

	public static final String IMG_GIF = "image/gif";

	public static final String IMG_TIFF = "image/tiff";

	public static final String IMG_PNG = "image/png";

	public static final String IMG_BMP = "image/bmp";

	public ManagedImageData() {
		fields = new HashMap();
	}

	public String getEncoding() {
		return encoding;
	}

	public String getField(String field) {
		return (String) fields.get(field);
	}
	
	public boolean containsField(String field){
		return fields.keySet().contains(field);
	}
	

	public int getId() {
		return id;
	}
	
	public void setField(String field, String value) {
		fields.put(field, value);
	}

	public String toXML() {
		String xml = "<image id=\"" + id + "\" encoding=\"" + encoding + "\"";
		Iterator iter = fields.keySet().iterator();
		while (iter.hasNext()) {
			String field = (String) iter.next();
			String value = (String) fields.get(field);
			// xml+= "<"+field+">" +value+"</"+field+">";
			xml += " " + field + "=\"" + value + "\"";
		}
		// xml+="<image/>";
		xml += "/>";
		return xml;
	}
}
