package org.iucn.sis.shared.api.models;

import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class VideoSource {
	
	private String title;
	
	private String image;
	
	private String caption;
	
	private String url;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String toXML() {
		StringBuilder out = new StringBuilder();
		out.append("<video>");
		out.append(XMLWritingUtils.writeCDATATag("title", getTitle(), true));
		out.append(XMLWritingUtils.writeCDATATag("image", getImage()));
		out.append(XMLWritingUtils.writeCDATATag("caption", getCaption(), true));
		out.append(XMLWritingUtils.writeCDATATag("url", getUrl()));
		out.append("</video>");
		
		return out.toString();
	}
	
	public static VideoSource fromXML(NativeNode node) {
		final VideoSource source = new VideoSource();
		
		final NativeNodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode current = nodes.item(i);
			if ("title".equals(current.getNodeName()))
				source.setTitle(current.getTextContent());
			else if ("image".equals(current.getNodeName()))
				source.setImage(current.getTextContent());
			else if ("caption".equals(current.getNodeName()))
				source.setCaption(current.getTextContent());
			else if ("url".equals(current.getNodeName()))
				source.setUrl(current.getTextContent());
		}
		
		return source;
	}

}
