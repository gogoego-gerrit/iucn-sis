package org.iucn.sis.client.api.ui.models.image;

import com.google.gwt.user.client.ui.Image;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNamedNodeMap;

public class ManagedImage extends ManagedImageData{
	
	private Image image;
	
	public ManagedImage(Image image, String encoding) {
		super();
		this.image = image;
		this.encoding = encoding;
	}

	public ManagedImage() {
		super();
	}
	
	public Image getImage() {
		return image;
	}


	public static ManagedImage managedImageFromXML(NativeElement ndoc) {
		ManagedImage toReturn = new ManagedImage();
		NativeNamedNodeMap attribs = ndoc.getAttributes();
		for (int i = 0; i < attribs.getLength(); i++) {
			if (attribs.item(i).getNodeName().equals("id")) {
				toReturn.id = Integer.valueOf(attribs.item(i).getNodeValue()).intValue();
			} else if (attribs.item(i).getNodeName().equals("encoding")) {
				toReturn.encoding = attribs.item(i).getNodeValue();
			} else {
				toReturn.setField(attribs.item(i).getNodeName(), attribs.item(i).getNodeValue());
			}
		}
		return toReturn;
	}
	
	public void setImage(Image image, String encoding) {
		this.image = image;
		this.encoding = encoding;
	}

	
}
