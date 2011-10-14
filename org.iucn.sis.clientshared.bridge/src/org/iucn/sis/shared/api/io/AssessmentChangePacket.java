package org.iucn.sis.shared.api.io;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.shared.api.models.Field;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class AssessmentChangePacket {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ROOT_TAG = "changePacket"; 
	
	private final Set<Field> add, edit, delete;
	private final int assessmentID;
	
	private long version;
	
	public AssessmentChangePacket(int assessmentID) {
		this.assessmentID = assessmentID;
		this.version = new Date().getTime();
		
		this.add = new HashSet<Field>();
		this.edit = new HashSet<Field>();
		this.delete = new HashSet<Field>();
	}
	
	public void addChange(Field field) {
		if (field == null)
			return;
		
		if (field.getId() == 0)
			addAddition(field);
		else if (field.hasData() || !field.getReference().isEmpty() || !field.getNotes().isEmpty())
			addEdit(field);
		else
			addDeletion(field);
	}
	
	public void addAddition(Field field) {
		add.add(field);
	}
	
	public void addEdit(Field field) {
		edit.add(field);
	}
	
	public void addDeletion(Field field) {
		delete.add(field);
	}
	
	public Set<Field> getAdditions() {
		return add;
	}
	
	public Set<Field> getEdits() {
		return edit;
	}
	
	public Set<Field> getDeletions() {
		return delete;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}
	
	public String toHTML() {
		StringBuilder out = new StringBuilder();
		appendHTML(out, add, "added", "No new fields were added.");
		appendHTML(out, edit, "edited", "No existing fields were edited.");
		appendHTML(out, delete, "deleted", "No empty fields were deleted.");
		return out.toString();
	}
	
	private void appendHTML(StringBuilder out, Set<Field> fields, String verb, String emptyMessage) {
		if (fields.isEmpty())
			out.append("<p>" + emptyMessage + "</p>");
		else {
			out.append("<p>The following ");
			if ("added".equals(verb))
				out.append("new ");
			out.append("fields were " + verb + ":</p>");
			for (Field field : fields)
				out.append("- " + field.getName() + "<br/>");
		}
	}
	
	public int getAssessmentID() {
		return assessmentID;
	}
	
	public long getVersion() {
		return version;
	}
	
	public String toXML() {
		final StringBuilder out = new StringBuilder();
		out.append("<" + ROOT_TAG + " assessmentid=\"" + getAssessmentID() + "\">");
	
		out.append(XMLWritingUtils.writeTag("version", getVersion()+""));
		
		out.append("<add>");
		for (Field field : add)
			out.append(field.toXML());
		out.append("</add>");
		
		out.append("<edit>");
		for (Field field : edit)
			out.append(field.toXML());
		out.append("</edit>");
		
		out.append("<delete>");
		for (Field field : delete)
			out.append(field.toXML());
		out.append("</delete>");
		
		out.append("</" + ROOT_TAG + ">");
		return out.toString();
	}
	
	public static AssessmentChangePacket fromXML(NativeElement root) {
		final Integer id;
		try {
			id = Integer.valueOf(root.getAttribute("assessmentid"));
		} catch (Exception e) {
			return null;
		}
		
		final AssessmentChangePacket packet = new AssessmentChangePacket(id);
		final NativeNodeList nodes = root.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode node = nodes.item(i);
			
			if ("version".equals(node.getNodeName()))
				packet.setVersion(Long.parseLong(node.getTextContent()));
			else if ("add".equals(node.getNodeName())) {
				NativeNodeList children = node.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					NativeNode child = children.item(k);
					if (child.getNodeType() != NativeNode.TEXT_NODE)
						packet.addAddition(Field.fromXML((NativeElement)child));
				}
			}
			else if ("edit".equals(node.getNodeName())) {
				NativeNodeList children = node.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					NativeNode child = children.item(k);
					if (child.getNodeType() != NativeNode.TEXT_NODE)
						packet.addEdit(Field.fromXML((NativeElement)child));
				}
			}
			else if ("delete".equals(node.getNodeName())) {
				NativeNodeList children = node.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					NativeNode child = children.item(k);
					if (child.getNodeType() != NativeNode.TEXT_NODE)
						packet.addDeletion(Field.fromXML((NativeElement)child));
				}
			}
		}
		
		return packet;
	}

}

