package org.iucn.sis.shared.api.models;

import java.io.Serializable;
import java.util.Set;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class FieldAttachment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private String name;
	
	private String key;
	
	private boolean publish;
	
	private java.util.Set<Field> fields;
	
	private java.util.Set<Edit> edits;
	
	public FieldAttachment() {
		fields = new java.util.HashSet<Field>();
		edits = new java.util.HashSet<Edit>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public boolean getPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	public java.util.Set<Field> getFields() {
		return fields;
	}
	
	public void setFields(java.util.Set<Field> fields) {
		this.fields = fields;
	}

	public Set<Edit> getEdits() {
		return edits;
	}

	public void setEdits(Set<Edit> edits) {
		this.edits = edits;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldAttachment other = (FieldAttachment) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public String toXML() {
		final StringBuilder out = new StringBuilder();
		out.append("<attachment id=\"" + getId() + "\">");
		out.append(XMLWritingUtils.writeCDATATag("name", getName()));
		out.append(XMLWritingUtils.writeCDATATag("key", getKey()));
		out.append(XMLWritingUtils.writeTag("publish", Boolean.toString(getPublish())));
		for (Edit edit : getEdits())
			out.append(edit.toXML());
		for (Field field : getFields())
			out.append("<field id=\"" + field.getId() + "\" name=\"" + field.getName() + "\" />");
		out.append("</attachment>");
		return out.toString();
	}
	
	public static FieldAttachment fromXML(NativeElement root) {
		return fromXML(root, new FieldFinder() {
			public Field get(String id, String name) {
				Field fauxField = new Field();
				fauxField.setId(Integer.valueOf(id));
				fauxField.setName(name);
				
				return fauxField;
			}
		});
	}
	
	public static FieldAttachment fromXML(NativeElement root, FieldFinder finder) {
		final FieldAttachment attachment = new FieldAttachment();
		attachment.setId(Integer.valueOf(root.getAttribute("id")));
		
		final NativeNodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if ("name".equals(node.getNodeName()))
				attachment.setName(node.getTextContent());
			else if ("key".equals(node.getNodeName()))
				attachment.setKey(node.getTextContent());
			else if ("publish".equals(node.getNodeName()))
				attachment.setPublish("true".equals(node.getTextContent()));
			else if (Edit.ROOT_TAG.equals(node.getNodeName()))
				attachment.getEdits().add(Edit.fromXML((NativeElement)node));
			else if ("field".equals(node.getNodeName())) {
				final NativeElement el = (NativeElement)node;
				final Field field = 
					finder.get(el.getAttribute("id"), el.getAttribute("name"));
				if (field != null)
					attachment.getFields().add(field);
			}
		}
		
		return attachment;
	}

	public static interface FieldFinder {
		
		public Field get(String id, String name);		

	}
}
