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
import java.util.Date;
import java.util.HashSet;

import org.iucn.sis.shared.api.models.interfaces.HasNotes;
import org.iucn.sis.shared.api.models.interfaces.HasReferences;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
public class CommonName implements Serializable, HasReferences, HasNotes {
	
	public static CommonName createCommonName(String name, String language,
			String isoCode, boolean primary) {
		return CommonName.createCommonName(name, language, isoCode, false, primary);
	}
	
	public static CommonName createCommonName(String name, String language,
			String isoCode, boolean validated, boolean isPrimary) {
		return new CommonName(name, language, isoCode, validated, isPrimary);
	}	
	
	public static CommonName fromXML(NativeElement commonNameTag) {
		String id = commonNameTag.getAttribute("id");
		boolean validated = commonNameTag.getAttribute("validated")
				.equalsIgnoreCase("true");
		String name = commonNameTag.getAttribute("name");
		boolean primary = commonNameTag.getAttribute("primary")
				.equalsIgnoreCase("true");
		String reason = commonNameTag.getAttribute("reason");
		
		CommonName curName = new CommonName();
		curName.setId(Integer.valueOf(id).intValue());
		curName.setName(name);
		curName.setValidated(validated);
		curName.setPrincipal(primary);
		if (!validated)
			curName.setChangeReason(Integer.valueOf(reason).intValue());
		
		NativeNodeList children = commonNameTag.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			NativeNode current = children.item(i);
			if ("language".equals(current.getNodeName()))
				curName.setIso(IsoLanguage.fromXML((NativeElement)current));
			else if ("sources".equals(current.getNodeName())) {
				NativeNodeList refs = current.getChildNodes();
				for (int k = 0; k < refs.getLength(); k++) {
					NativeNode curChild = refs.item(k);
					if (Reference.ROOT_TAG.equals(curChild.getNodeName()))
						curName.getReference().add(Reference.fromXML((NativeElement)curChild));
				}
			}
			else if ("notes".equals(current.getNodeName())) {
				NativeNodeList notes = current.getChildNodes();
				for (int k = 0; k < notes.getLength(); k++) {
					NativeNode curChild = notes.item(k);
					if (Notes.ROOT_TAG.equals(curChild.getNodeName()))
						curName.getNotes().add(Notes.fromXML((NativeElement)curChild));
				}
			}
		}
		
		return curName;
	}
	
	
	
	public static final String ROOT_TAG = "commonName";
	public static final int UNSET = 0;
	public static final int ADDED = 1;
	public static final int DELETED = 2;
	public static final int ISO_CHANGED = 3;
	public static final int NAME_CHANGED = 4;
	public static final String[] reasons = { "", "ADDED", "DELETED", "ISO CHANGED", "NAME CHANGED" };
	
	
	private int id;
	private String name;
	private boolean principal;
	private int changeReason;
	private boolean validated;	
	private IsoLanguage iso;
	private Taxon taxon;
	private java.util.Set<Reference> reference;
	private java.util.Set<Notes> notes;
	private long generationID;
	
	public CommonName() {
		this(null, null, null);
	}
	
	public CommonName(String name, String language, String isoCode) {
		this(name, language, isoCode, false, false);
	}

	public CommonName(String name, String language, String isoCode, boolean validated, boolean isPrimary) {
		super();
		this.name = name;
		if (language != null && isoCode != null)
			this.iso = new IsoLanguage(language, isoCode);
		this.validated = validated;
		this.principal = isPrimary;
		this.reference = new HashSet<Reference>();
		this.notes = new HashSet<Notes>();
		this.generationID = new Date().getTime();
	}
	
	public void setGenerationID(long generationID) {
		this.generationID = generationID;
	}
	
	public CommonName deepCopy() {
		CommonName cn =  new CommonName(name, iso == null ? null : iso.getName(), iso == null ? null : iso.getCode(), validated, principal);
		cn.setIso(getIso());
		cn.setId(getId());
		cn.setNotes(new HashSet<Notes>());
		cn.setChangeReason(getChangeReason());
		for (Notes note : getNotes()) {
			Notes newNote = note.deepCopy();
			newNote.setCommonName(cn);
			cn.getNotes().add(newNote);
		}
		return cn;
	}
	
	
	@Override
	public int hashCode() {
		return new Long(generationID).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommonName other = (CommonName) obj;
		if (generationID != other.generationID)
			return false;
		return true;
	}
	
	public int getChangeReason() {
		return changeReason;
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	
	public int getId() {
		return id;
	}
	
	public IsoLanguage getIso() {
		return iso;
	}
	
	public String getIsoCode() {
		return iso == null ? null : iso.getCode();
	}
	
	public String getLanguage() {
		return iso == null ? null : iso.getName();
	}
	
	public String getName() {
		return name;
	}
	
	public java.util.Set<Notes> getNotes() {
		return notes;
	}
	
	public int getORMID() {
		return getId();
	}
	
	public boolean getPrincipal() {
		return principal;
	}
	
	public java.util.Set<Reference> getReference() {
		return reference;
	}
	
	
	
	public Taxon getTaxon() {
		return taxon;
	}
	
	public boolean getValidated() {
		return validated;
	}
	
	
	public boolean isPrimary() {
		return getPrincipal();
	}
	
	
	public void setChangeReason(int reason) {
		validated = false;
		changeReason = reason;
	}
	
	public void setId(int value) {
		this.id = value;
		this.generationID = value;
	}
	
	public void setIso(IsoLanguage value) {
		this.iso = value;
	}
	
	public void setIsoCode(String isoCode) {
		validated = false;
		changeReason = ISO_CHANGED;
		iso = IsoLanguage.getByIso(isoCode);
	}
	
	public void setLanguage(String language) {
		validated = false;
		changeReason = ISO_CHANGED;
		iso = IsoLanguage.getByLanguage(language);
	}
	
	public void setName(String value) {
		this.name = value;
	}
	
	
	public void setNotes(java.util.Set<Notes> value) {
		this.notes = value;
	}
	
	public void setPrincipal(boolean value) {
		this.principal = value;
	}
	
	
	public void setReference(java.util.Set<Reference> value) {
		this.reference = value;
	}

	public void setTaxon(Taxon value) {
		this.taxon = value;
	}

	public void setValidated(boolean value) {
		this.validated = value;
	}

	public String toString() {
		return String.valueOf(getId());
	}

	public String toXML() {
		String xml = "<" + ROOT_TAG + " ";
		xml += "validated=\"" + getValidated() + "\" name=\""
				+ XMLUtils.clean(getName()) + "\" primary=\""
				+ getPrincipal() + "\" id=\"" + getId() + "\"";
		if (!getValidated())
			xml += " reason=\"" + getChangeReason() + "\"";
		xml += ">";
		
		if (getIso() != null)
			xml += getIso().toXML();
	
		xml += "<sources>";
		for (Reference ref : getReference()) {
			xml += ref.toXML();
		}
		xml += "</sources>";
	
		xml += "<notes>";
		for (Notes note : getNotes()) {
			xml += note.toXML();
		}
		xml += "</notes>";
	
		xml += "</commonName>\r\n";
	
		return xml;
	}
	
}
