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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field.ReferenceCopyHandler;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
import org.iucn.sis.shared.api.models.fields.RegionField;
import org.iucn.sis.shared.api.models.parsers.FieldV1Parser;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;
import com.solertium.util.portable.XMLWritingUtils;

public class Assessment implements Serializable, AuthorizableObject {

	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	public static final String ROOT_TAG = "assessment";
	public static final int DELETED = -1;
	public static final int ACTIVE = 0;
	
	protected int state;
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	
	public List<Integer> getRegionIDs() {
		RegionField proxy = new RegionField(getField(CanonicalNames.RegionInformation));
		return proxy.getRegionIDs();
	}
	
	public void setId(int value) {
		this.id = value;
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
		Assessment other = (Assessment) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	/**
	 * @deprecation use RedListCriteriaField directly instead 
	 */
	@Deprecated
	public String getCategoryAbbreviation() {
		Field field = getField(CanonicalNames.RedListCriteria);
		if (field == null)
			return "N/A";
		
		RedListCriteriaField proxy = new RedListCriteriaField(field);
		if (proxy.isManual())
			return proxy.getManualCategory();
		else
			return proxy.getGeneratedCategory();
	}
	
	public static Assessment fromXML(NativeElement element) {
		Assessment assessment = new Assessment();
		assessment.setId(Integer.valueOf(element.getAttribute("id")));
		assessment.setInternalId(element.getAttribute("internalID"));
		assessment.setEdit(new HashSet<Edit>());
		assessment.setField(new HashSet<Field>());
		assessment.setReference(new HashSet<Reference>());
		
		
		final NativeNodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final NativeNode current = children.item(i);
			
			if ("source".equals(current.getNodeName())) {
				assessment.setSource(current.getTextContent());		
			}
			else if ("sourceDate".equals(current.getNodeName())) {
				assessment.setSourceDate(current.getTextContent());		
			}
			else if ("schema".equals(current.getNodeName()))
				assessment.setSchema(current.getTextContent());
			else if (AssessmentType.ROOT_TAG.equals(current.getNodeName())) {
				assessment.setAssessmentType(AssessmentType.fromXML((NativeElement)current));		
			}
			else if (Taxon.ROOT_TAG.equals(current.getNodeName())) {
				assessment.setTaxon(Taxon.fromXMLminimal((NativeElement)current));		
			}
			else if ("publicationReference".equals(current.getNodeName())) {
				assessment.setPublicationReference(Reference.fromXML(current));
			}
			else if (AssessmentIntegrityValidation.ROOT_TAG.equals(current.getNodeName()))
				assessment.setValidation(AssessmentIntegrityValidation.fromXML((NativeElement)current));
			else if (Edit.ROOT_TAG.equals(current.getNodeName())) {
				Edit cur = Edit.fromXML((NativeElement)current);
				if (cur.getAssessment() == null)
					cur.setAssessment(new HashSet<Assessment>());
				cur.getAssessment().add(assessment);
				
				assessment.getEdit().add(cur);
			}
			else if (Reference.ROOT_TAG.equals(current.getNodeName())) {
				Reference cur = Reference.fromXML((NativeElement)current);
				if (cur.getAssessment() == null)
					cur.setAssessment(new HashSet<Assessment>());
				cur.getAssessment().add(assessment);
				
				assessment.getReference().add(cur);
			}
			else if ("field".equals(current.getNodeName())) {
				Field field;
				try {
					field = FieldV1Parser.parse((NativeElement)current);
				} catch (ClassCastException e) {
					e.printStackTrace();
					continue;
				} catch (Throwable e) {
					e.printStackTrace();
					continue;
				}
				
				field.setAssessment(assessment);
				
				assessment.getField().add(field);
			}
			else if ("fields".equals(current.getNodeName())) {
				final NativeNodeList fields = current.getChildNodes();
				for (int k = 0; k < fields.getLength(); k++) {
					final NativeNode child = fields.item(k);
					if (NativeNode.TEXT_NODE == child.getNodeType())
						continue;
					
					Field field;
					try {
						field = Field.fromXML((NativeElement)child);
					} catch (ClassCastException e) {
						Debug.println(e);
						continue;
					} catch (Throwable e) {
						Debug.println(e);
						continue;
					}
					
					field.setAssessment(assessment);
					
					assessment.getField().add(field);
				}
			}
		}
		
		return assessment;
		
	}
	
	public static Assessment fromXML(NativeDocument ndoc) {
		return fromXML(ndoc.getDocumentElement());
	}
	
	@Override
	public String getFullURI() {
		return "resource/assessment/" + getAssessmentType().getName();
	}

	@Override
	public String getProperty(String key) {
		if ("region".equalsIgnoreCase(key))
			return isGlobal() ? "global" : serializeRegionIDs(getRegionIDs());
		else if ("schema".equalsIgnoreCase(key))
			return getSchema("");
		else
			return "";
	}

	
	private String serializeRegionIDs(List<Integer> regionIDs) {
        ArrayUtils.insertionSort(regionIDs, new PortableAlphanumericComparator());
        StringBuilder str = new StringBuilder();
        for (Iterator<Integer> iter = regionIDs.iterator(); iter.hasNext(); )
            str.append(iter.next() + (iter.hasNext() ? "," : ""));
       
        return str.substring(1);
    }
	
	public void setDateAssessed(Date dateAssessed) {
		Field field = getField(CanonicalNames.RedListAssessmentDate);
		if (field == null) {
			field = new Field(CanonicalNames.RedListAssessmentDate, this);
			getField().add(field);
			keyToField.put(field.getName(), field);
		}
		
		ProxyField proxy = new ProxyField(field);
		proxy.setDatePrimitiveField("value", dateAssessed);
	}
	
	public Date getDateAssessed() {
		ProxyField proxy = new ProxyField(getField(CanonicalNames.RedListAssessmentDate));
		return proxy.getDatePrimitiveField("value");
	}

	public boolean isGlobal() {
		return getRegionIDs().contains(Region.GLOBAL_ID);
	}

	public boolean isEndemic() {
		RegionField proxy = new RegionField(getField(CanonicalNames.RegionInformation));
		return proxy.isEndemic();
	}

	public boolean isRegional() {
		return !isGlobal() && hasRegions();
	}

	public boolean hasRegions() {
		return !getRegionIDs().isEmpty();
	}

	public boolean hasAttachments() {
		if (getField() == null)
			return false;
		
		for (Field field : getField())
			if (field.isAttachable() && field.getFieldAttachment() != null && !field.getFieldAttachment().isEmpty())
				return true;
		
		return false;
	}
	
	protected String dateFinalized;
	public String getDateFinalized() {
		return dateFinalized;
	}
	public void setDateFinalized(String date) {
		this.dateFinalized = date;
	}

	public boolean isPublished() {
		return getAssessmentType().getId() == AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID;
	}

	public boolean isDraft() {
		return getAssessmentType().getId() == AssessmentType.DRAFT_ASSESSMENT_STATUS_ID;
	}

	public String toXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("<" + ROOT_TAG + " id=\"" + getId() + "\" internalID=\"" + getInternalId() + "\">");
		xml.append("<source><![CDATA[" + getSource() + "]]></source>");
		xml.append("<sourceDate><![CDATA[" + getSourceDate() + "]]></sourceDate>");
		xml.append(XMLWritingUtils.writeCDATATag("schema", getSchema(), true));
		xml.append(getTaxon().toXMLMinimal());
		xml.append(getAssessmentType().toXML());
		
		if (getPublicationReference() != null)
			xml.append(getPublicationReference().toXML("publicationReference"));
		
		if (getValidation() != null)
			xml.append(getValidation().toXML());
		
		if (getEdit() != null) {
			for (Edit edit : getEdit())
				xml.append(edit.toXML());
		}
		
		if (getReference() != null) {
			for (Reference edit : getReference())
				xml.append(edit.toXML());
		}
		
		if (getField() != null) {
			xml.append("<fields>");
			for (Field field : getField())
				xml.append(field.toXML());
			xml.append("</fields>");
		}
		
		xml.append("</" + ROOT_TAG + ">");
		return xml.toString();		
		
	}
	
	public int getSpeciesID() {
		return getTaxon().getId();
	}

	public String getType() {
		return getAssessmentType().getName();
	}

	public Assessment deepCopy() {
		return deepCopy(new DeepCopyFilter() {
			public Field copy(Assessment assessment, Field field) {
				return field.deepCopy(false, this);
			}
			public Reference copyReference(Reference source) {
				return source.deepCopy();
			}
		});
	}
	
	public Assessment deepCopy(DeepCopyFilter filter) {
		Assessment assessment = new Assessment();
		assessment.setAssessmentType(getAssessmentType());
		assessment.setDateFinalized(getDateFinalized());
		assessment.setSource(getSource());
		assessment.setSourceDate(getSourceDate());
		assessment.setSchema(getSchema());
		assessment.setState(getState());
		assessment.setTaxon(getTaxon());
		assessment.setPublicationReference(getPublicationReference());
		//Never copy over the validation...
		//assessment.setValidation(getValidation());
		
		assessment.setField(new HashSet<Field>());
		for (Field field : getField()) {
			Field copy = filter.copy(assessment, field);
			if (copy != null) {
				copy.setAssessment(assessment);
				assessment.getField().add(copy);
			}
		}
		
		assessment.setReference(new HashSet<Reference>());
		for (Reference ref : getReference())
			assessment.getReference().add(ref.deepCopy());	
		
		return assessment;
	}

//	public String getAssessmentID() {
//		return getInternalId();
//	}
	
	public void clearReferences() {
		getReference().clear();
		
		for( Field cur : getField() )
			cur.getReference().clear();
	}
	
	public void setRegions(Collection<Region> regions) {
		setRegions(regions, false);
	}
	
	public void setRegions(Collection<Region> regions, boolean endemic) {
		final List<Integer> regionIDs = new ArrayList<Integer>();
		for (Region region : regions)
			regionIDs.add(region.getId());
		
		Field field = getField(CanonicalNames.RegionInformation);
		if (field == null)
			field = new Field(CanonicalNames.RegionInformation, this);
		
		RegionField proxy = new RegionField(field);
		proxy.setEndemic(endemic);
		proxy.setRegions(regionIDs);
		
		setField(field);
	}
	
	public void setField(Field field) {
		final Field existing = getField(field.getName());
		this.field.remove(existing);
		
		this.field.add(field);
		this.keyToField.put(field.getName(), field);
	}

	public void setType(String type) {
		setAssessmentType(AssessmentType.getAssessmentType(type));
	}

	public String getSpeciesName() {
		return getTaxon().getFriendlyName();
	}
	
	protected Edit lastEdit;
	public Edit getLastEdit() {
		if (lastEdit == null && getEdit() != null && !getEdit().isEmpty()) {
			List<Edit> edits = new ArrayList<Edit>(getEdit());
			Collections.sort(edits, Collections.reverseOrder());
			
			lastEdit = edits.get(0);
		}
		return lastEdit;
		
	}
	
	private Map<String, Field> keyToField;
	
	public void generateFields() {
		keyToField = new HashMap<String, Field>();
		for (Field field : getField()) {
			keyToField.put(field.getName(), field);
		}
	}
	
	public Set<String> getFieldKeys() {
		if (keyToField == null)
			generateFields();
		return keyToField.keySet();
	}
	
	public Field getField(String fieldName) {
		if (keyToField == null || keyToField.size() != getField().size()) {
			generateFields();
		}
		return keyToField.get(fieldName);		
	}

	public boolean addReference(Reference ref, String fieldName) {
		Field field = getField(fieldName);
		if (field != null) {
			field.getReference().add(ref);
			return true;
		}
		return false;
	}
	
	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */

	public Assessment() {
		state = ACTIVE;
	}

	private int id;

	private AssessmentType assessment_type;

	private String source;

	private String sourceDate;
	
	private String schema;

	private Taxon taxon;

	private String internalId;
	
	private Reference publicationReference;
	
	private AssessmentIntegrityValidation validation;

	private java.util.Set<Edit> edit = new java.util.HashSet<Edit>();

	private java.util.Set<Reference> reference = new java.util.HashSet<Reference>();

	private java.util.Set<Field> field = new java.util.HashSet<Field>();

	public int getId() {
		return id;
	}

	public int getORMID() {
		return getId();
	}

	public void setSource(String value) {
		this.source = value;
	}

	public String getSource() {
		return source;
	}

	public void setSourceDate(String value) {
		this.sourceDate = value;
	}

	public String getSourceDate() {
		return sourceDate;
	}
	
	public String getSchema() {
		return schema;
	}
	
	/**
	 * On the client, use 
	 *  - SchemaCache.impl.getDefaultSchema()
	 * On the server, use
	 *  - SIS.get().getDefaultSchema()
	 *  
	 * if you wish to default to the default schema 
	 * for this SIS instance.
	 * @param defaultValue
	 * @return
	 */
	public String getSchema(String defaultValue) {
		return schema == null || "".equals(schema) ? defaultValue : schema; 
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setInternalId(String value) {
		this.internalId = value;
	}

	public String getInternalId() {
		return internalId;
	}

	public void setAssessmentType(AssessmentType value) {
		this.assessment_type = value;
	}

	public AssessmentType getAssessmentType() {
		return assessment_type;
	}

	public void setTaxon(Taxon value) {
		this.taxon = value;
	}

	public Taxon getTaxon() {
		return taxon;
	}
	
	public void setPublicationReference(Reference publicationReference) {
		this.publicationReference = publicationReference;
	}
	
	public Reference getPublicationReference() {
		return publicationReference;
	}

	public void setEdit(java.util.Set<Edit> value) {
		this.edit = value;
		lastEdit = null;
	}

	public java.util.Set<Edit> getEdit() {
		return edit;
	}

	public void setReference(java.util.Set<Reference> value) {
		this.reference = value;
	}

	public java.util.Set<Reference> getReference() {
		return reference;
	}

	public void setField(java.util.Set<Field> value) {
		this.field = value;
	}

	public java.util.Set<Field> getField() {
		return field;
	}
	
	public void setValidation(AssessmentIntegrityValidation validation) {
		this.validation = validation;
	}
	
	public AssessmentIntegrityValidation getValidation() {
		return validation;
	}

	public String toString() {
		return String.valueOf(getId());
	}
	
	public String getDisplayText() {
		return getAssessmentType().getDisplayName() + " " + getTaxon().getFriendlyName();
	}
	
	public static interface DeepCopyFilter extends Field.ReferenceCopyHandler {
		
		public Field copy(Assessment assessment, Field field);
		
	}

}
