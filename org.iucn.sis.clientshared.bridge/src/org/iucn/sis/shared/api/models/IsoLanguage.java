package org.iucn.sis.shared.api.models;
/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: 
 * License Type: Evaluation
 */
import java.io.Serializable;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;
public class IsoLanguage implements Serializable {
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	public IsoLanguage(String name, String code) {
		setCode(code);
		setName(name);
	}
	
	public void setId(int value) {
		this.id = value;
	}
	
	public static IsoLanguage getByIso(String iso)  {
		return new IsoLanguage(null,iso);
	}
	
	public static IsoLanguage getByLanguage(String language) {
		return new IsoLanguage(language, null);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IsoLanguage) {
			return ((IsoLanguage)obj).code.equals(this.code);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return this.code.hashCode();
	}
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	
	public IsoLanguage() {
	}
	
	private int id;
	
	private String name;
	
	private String code;
	
	private java.util.Set<CommonName> commonName = new java.util.HashSet<CommonName>();
	
	
	
	public int getId() {
		return id;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public void setName(String value) {
		this.name = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setCode(String value) {
		this.code = value;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCommonName(java.util.Set<CommonName> value) {
		this.commonName = value;
	}
	
	public java.util.Set<CommonName> getCommonName() {
		return commonName;
	}
	
	public String toXML() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<language id=\"" + id + "\">");
		builder.append(XMLWritingUtils.writeCDATATag("name", name));
		builder.append(XMLWritingUtils.writeCDATATag("code", code));
		builder.append("</language>");
		return builder.toString();
	}
	
	public static IsoLanguage fromXML(NativeElement element) {
		IsoLanguage language = new IsoLanguage();
		language.setId(Integer.valueOf(element.getAttribute("id")));
		
		final NativeNodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final NativeNode node = children.item(i);
			if ("code".equals(node.getNodeName()))
				language.setCode(node.getTextContent());
			else if ("name".equals(node.getNodeName()))
				language.setName(node.getTextContent());
		}
		
		return language;
	}
	
}
