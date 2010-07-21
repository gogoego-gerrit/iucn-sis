package org.iucn.sis.client.referenceui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.references.ReferenceCitationGeneratorShared;
import org.iucn.sis.shared.data.references.ReferenceCitationGeneratorShared.ReturnedCitation;
import org.iucn.sis.shared.xml.XMLUtils;


import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

/**
 * Reference.java
 * 
 * A glorified HashMap that contains the reference ID and reference type for a
 * given reference.
 * 
 * @author carl.scott
 * 
 */
public class ReferenceUI extends HashMap<String, String> implements Comparable<ReferenceUI>, AuthorizableObject {
	private static final long serialVersionUID = 1L;

	public static boolean isCitationValid(String complete) {
		return (complete != null && complete.equalsIgnoreCase("y"));
	}

	protected String referenceID;
	protected String referenceType;

	protected String associatedField;

	/**
	 * Default constructor
	 * 
	 */
	public ReferenceUI() {
		super();
	}

	public ReferenceUI(Map<String, String> ref, String id, String type) {
		super(ref);
		referenceID = id;
		referenceType = type;

		if (referenceType == null)
			referenceType = "other";
		else if (referenceType.equalsIgnoreCase("null"))
			referenceType = "rldb";
	}

	/**
	 * Creates a new reference from the given native element, which is expected
	 * to have the id and type attributes, followed by a node list of child
	 * "field" elements.
	 * 
	 * @param referenceElement
	 *            the element
	 */
	public ReferenceUI(NativeElement referenceElement) {
		super();

		this.referenceID = referenceElement.getAttribute("id");
		this.referenceType = referenceElement.getAttribute("type");
		if (referenceType == null)
			referenceType = "other";
		else if (referenceType.equalsIgnoreCase("null"))
			referenceType = "rldb";

		NativeNodeList children = referenceElement.getElementsByTagName("field");
		for (int i = 0; i < children.getLength(); i++) {
			NativeElement currentField = children.elementAt(i);
			if (currentField.getNodeName().equalsIgnoreCase("field")) {
				addField(currentField.getAttribute("name").toLowerCase(), currentField.getTextContent());
			}
		}

	}

	public String getFullURI() {
		return "resource/reference";
	}
	
	public String getProperty(String key) {
		return "";
	}
	
	/**
	 * Adds a new field to this reference
	 * 
	 * @param fieldID
	 *            the field ID
	 * @param value
	 *            the value
	 */
	public void addField(String fieldID, String value) {
		if (fieldID == null || value == null)
			throw new Error("Field / value cannot be null.");

		put(fieldID.trim(), value.trim());
	}

	public int compareTo(ReferenceUI arg0) {
		ReferenceUI other = (ReferenceUI) arg0;
		String myAuthor = (String) get("author");
		String myTitle = (String) get("title");
		String myYear = (String) get("year");

		String otherAuthor = (String) other.get("author");
		String otherTitle = (String) other.get("title");
		String otherYear = (String) other.get("year");

		return myAuthor.equals(otherAuthor) ? (myTitle.equals(otherTitle) ? (myYear.equals(otherYear) ? 0 : myYear
				.compareTo(otherYear)) : myTitle.compareTo(otherTitle)) : myAuthor.compareTo(otherAuthor);

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReferenceUI))
			return false;

		final ReferenceUI other = (ReferenceUI) obj;
		if (referenceID == null) {
			if (other.referenceID != null)
				return false;
		} else if (!referenceID.equals(other.referenceID))
			return false;

		return true;
	}

	public void generateCitation() {
		ReturnedCitation citation = ReferenceCitationGeneratorShared.generateNewCitation(this, referenceType);
		if (citation != null) {
			String bool = (citation.allFieldsEntered) ? "Y" : "N";
			put("citation", citation.citation);
			put("citation_complete", bool);
		}
	}

	public void generateCitationIfNotAlreadyGenerate() {

		String oldCitation = getCitation();
		if (oldCitation == null || oldCitation.equalsIgnoreCase("")) {
			ReturnedCitation citation = ReferenceCitationGeneratorShared.generateNewCitation(this, referenceType);
			if (citation != null) {
				String bool = (citation.allFieldsEntered) ? "Y" : "N";
				put("citation", citation.citation);
				put("citation_complete", bool);
			}	
		}
	}

	public String getAssociatedField() {
		return associatedField;
	}

	public String getCitation() {
		return XMLUtils.cleanFromXML(get("citation"));
	}

//	public HTML getClickableReferenceBody() {
//		HTML html = new HTML("<li><span class=\"SIS_HyperlinkLookAlike\">Reference #" + referenceID + "</span>: "
//				+ getReferenceBody() + "...</li>");
//		html.setTitle("Click for full information");
//		html.addClickListener(new ClickListener() {
//			public void onClick(Widget sender) {
//				showEditor();
//			}
//		});
//
//		return html;
//
//	}

	/**
	 * Gets a field from the reference
	 * 
	 * @param fieldID
	 *            the id/name of the field
	 * @return the value
	 */
	public String getField(String fieldID) {
		return (String) get(fieldID);
	}

	/**
	 * Returns just the fields associated with this reference as XML.
	 * 
	 * @return the xml
	 */
	public String getFieldXML() {
		String xml = "";
		for (Entry<String, String> entry : entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value != null && !value.trim().equalsIgnoreCase("")) {
				value = XMLUtils.clean(value);
				xml += "<field name=\"" + key + "\">" + value + "</field>\n";
			}
		}
		return xml;
	}

	public String getReferenceBody() {
		String body = "&nbsp;" + "<b>" + get("author") + "</b>: " + "&nbsp;" + "<b>" + get("title") + "</b>: "
				+ "&nbsp;" + "<b>" + get("year") + "</b>: ";
		return body;

	}

	public String getReferenceID() {
		return referenceID;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public boolean hasField(String fieldID) {
		return containsKey(fieldID);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((referenceID == null) ? 0 : referenceID.hashCode());
		return result;
	}

	public boolean isCitationValid() {
		String complete = (String) get("citation_complete");
		return isCitationValid(complete);
	}

	public void setAssociatedField(String associatedField) {
		this.associatedField = associatedField;
	}

	public void setCitationComplete(boolean complete) {
		if (complete)
			addField("citation_complete", "Y");
		else
			addField("citation_complete", "N");
	}

	public void setCitationValid(boolean valid) {
		String validString = (valid) ? "Y" : "N";
		put("citation_complete", validString);
	}

	public void setReferenceID(String referenceID) {
		this.referenceID = referenceID;
	}

	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;

		if (referenceType == null)
			referenceType = "other";
		else if (referenceType.equalsIgnoreCase("null"))
			referenceType = "rldb";
	}

	public void showEditor() {
//		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
//		document.get("/refsvr/reference/" + referenceID, new GenericCallback<String>() {
//			public void onFailure(Throwable caught) {
//			}
//
//			public void onSuccess(String result) {
//				NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("reference");
//				if (nodes.getLength() == 0)
//					WindowUtils.errorAlert("Error", "No reference data found.");
//				else {
//					for (int i = 0; i < nodes.getLength(); i++) {
//						NativeElement current = nodes.elementAt(i);
//						if (current.getNodeName().equalsIgnoreCase("reference")) {
//							ReferenceEditor editor = new ReferenceEditor(
//									new org.iucn.sis.client.referenceui.ReferenceUI(current), true);
//							editor.setHeading("Reference Information");
//							break;
//						}
//					}
//				}
//			}
//		});
	}

	/**
	 * Returns the full XML for this reference
	 * 
	 * @return the xml
	 */
	public String toXML() {
		String xml = "<reference" + (referenceID == null ? "" : " id=\"" + referenceID + "\"") + " type=\""
				+ referenceType + "\">\n";
		xml += getFieldXML();
		xml += "</reference>\n";
		return xml;
	}

}
