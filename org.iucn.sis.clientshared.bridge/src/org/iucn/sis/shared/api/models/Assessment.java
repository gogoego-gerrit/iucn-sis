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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
import org.iucn.sis.shared.api.models.fields.RegionField;
import org.iucn.sis.shared.api.models.parsers.FieldV1Parser;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldType;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;
import com.solertium.util.portable.XMLWritingUtils;

public class Assessment implements Serializable, AuthorizableObject, Referenceable {
	
	public static final String DEFAULT_SCHEMA = "org.iucn.sis.server.schemas.redlist";

	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
	public static final String ROOT_TAG = "assessment";
	public static final int DELETED = -1;
	public static final int ACTIVE = 0;
	
	protected boolean isHistorical;
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
	public String getCategoryAbbreviation() {
		if( (Boolean)getPrimitiveValue(CanonicalNames.RedListCriteria, "isManual") )
			return (String)getPrimitiveValue(CanonicalNames.RedListCriteria, "manualCategory");
		else
			return (String)getPrimitiveValue(CanonicalNames.RedListCriteria, "generatedCategory");
	}
	
	public Set<Reference> getReferences(String canonicalName) {
		Field field = getField(canonicalName);
		return field == null ? null : getField(canonicalName).getReference();
	}
	
	public boolean removeReference(Reference ref, String canonicalName) {
		Field field = getField(canonicalName);
		return field == null ? false : getField(canonicalName).getReference().remove(ref);
	}
	
	/**
	 * Checks the individual regions to see if there is an overlap.
	 * @param other assessment to compare with
	 * @return Integer representing the conflicting region, null if no conflict
	 */
	public Integer isRegionConflict(Assessment other) {
		List<Integer> otherRegions = other.getRegionIDs();
		for( Integer cur : getRegionIDs() )
			if( otherRegions.contains(cur) )
				return cur;
		
		return null;
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
	public void addReferences(ArrayList<Reference> references,
			GenericCallback<Object> callback) {
		getReferences().addAll(references);
		callback.onSuccess(this);
	}
	
	@Override
	public Set<Reference> getReferencesAsList() {
		return getReferences();
	}
	
	@Override
	public void onReferenceChanged(GenericCallback<Object> callback) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeReferences(ArrayList<Reference> references,
			GenericCallback<Object> callback) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getFullURI() {
		return "resource/assessment/" + getAssessmentType().getName();
	}

	@Override
	public String getProperty(String key) {
		if ("region".equalsIgnoreCase(key))
			return isGlobal() ? "global" : serializeRegionIDs(getRegionIDs());
		else
			return "";
	}

	
	private String serializeRegionIDs(List<Integer> regionIDs) {
        ArrayUtils.insertionSort(regionIDs, new PortableAlphanumericComparator());
        StringBuilder str = new StringBuilder();
        for( Integer curID : regionIDs )
            str.append("," + curID);
       
        return str.substring(1);
    }
	
	public boolean setDateAssessed(Date dateAssessed) {
		return setPrimitiveValue(dateAssessed, CanonicalNames.RedListAssessmentDate, "value", PrimitiveFieldType.DATE_PRIMITIVE.toString());
	}
	
	public Date getDateAssessed() {
		return (Date)getPrimitiveValue(CanonicalNames.RedListAssessmentDate, "value");
	}
	
	public boolean setPrimitiveValue(Object value, String canonicalName, String primitiveName, String primitiveType) {
		boolean ret = false;
		
		Field field = getField(canonicalName);
		if (field == null) {
			field = new Field(canonicalName, this);
			field.setPrimitiveField(new HashSet<PrimitiveField>());
			
			this.field.add(field);
		}

		PrimitiveField prims = field.getKeyToPrimitiveFields().get(primitiveName);
		if (prims != null) {
			prims.setValue(value);
			ret = true;
		} else {
			PrimitiveField prim = PrimitiveFieldFactory.generatePrimitiveField(PrimitiveFieldType.DATE_PRIMITIVE);
			prim.setField(field);
			prim.setName(primitiveName);
			prim.setValue(value);
			
			field.getPrimitiveField().add(prim);
			
			ret = true;
		}
		
		return ret;
	}
	
	public Object getPrimitiveValue(String canonicalName, String primitiveName) {
		PrimitiveField field = getPrimitiveField(canonicalName, primitiveName);
		if (field != null)
			return field.getValue();
		return null;
	}
	
	public PrimitiveField getPrimitiveField(String canonicalName, String primitiveName) {
		Field field = getField(canonicalName);
		if (field != null)
			return field.getKeyToPrimitiveFields().get(primitiveName);
		return null;
	}

	public boolean isGlobal() {
		return getRegionIDs().contains(Region.GLOBAL_ID);
	}

	public boolean isEndemic() {
		PrimitiveField pf = getField(CanonicalNames.RegionInformation).getKeyToPrimitiveFields().get("endemic");
		if (pf != null)
			return (Boolean) pf.getValue();
		return false;
	}

	public boolean isRegional() {
		return !isGlobal();
	}

	
	public boolean getIsHistorical() {
		return isHistorical;
	}
	
	public void setIsHistorical(Boolean historical) {
		isHistorical = historical;
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
				return field.deepCopy(false, true);
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
		
		assessment.setField(new HashSet<Field>());
		for (Field field : getField()) {
			Field copy = filter.copy(assessment, field);
			if (copy != null)
				assessment.getField().add(copy);
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
		getReferences().clear();
		
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
	
	public Set<Reference> getReferences() {
		
		return getReference();
	}
	
	public String getUID() {
		return getInternalId() + "_" + getAssessmentType().getName();
	}
	
	protected Edit lastEdit;
	public Edit getLastEdit() {
		if (lastEdit == null) {
			if (getEdit().size() > 1) {
				List<Edit> edits = new ArrayList<Edit>();
				for (Edit edit : getEdit()) {
					edits.add(edit);
				}
				Collections.sort(edits);
				lastEdit = edits.get(0);
			} else if (getEdit().size() == 1) {
				lastEdit = getEdit().iterator().next();
			}			
		}
		return lastEdit;
		
	}
	
	public long getDateModified() {
		long dateModified = 0;
		Edit edit = getLastEdit();
		if (edit != null) {
			dateModified = edit.getCreatedDate().getTime();
		}
		return dateModified;
	}
	
	public String getCategoryFuzzyResult() {
		Field field = getField(CanonicalNames.RedListFuzzyResult);
		if (field == null)
			return "";
		else
			return field.getPrimitiveField("text").getRawValue();
	}
	
	public String getCategoryCriteria() {
		RedListCriteriaField field = new RedListCriteriaField(getField(CanonicalNames.RedListCriteria));
		return field.getGeneratedCriteria();
	}
	
	public String getCrCriteria() {
		return "TODO";
	}
	
	public String getEnCriteria() {
		return "TODO";
	}
	
	public String getVuCriteria() {
		return "TODO";
	}
	
	private Map<String, Field> keyToField;
	
	public void generateFields() {
		keyToField = new HashMap<String, Field>();
		for (Field field : getField()) {
			keyToField.put(field.getName(), field);
		}
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

	public void setEdit(java.util.Set<Edit> value) {
		this.edit = value;
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

	public String toString() {
		return String.valueOf(getId());
	}
	
	public void setCategoryCriteria(String criteriaString) {
		// TODO Auto-generated method stub
		RedListCriteriaField field = new RedListCriteriaField(getField(CanonicalNames.RedListCriteria));
		field.setGeneratedCriteria(criteriaString);
	}
	
	public void setCrCriteria(String criteriaStringCR) {
		// TODO Auto-generated method stub
		
	}
	public void setCategoryFuzzyResult(String string) {
		Field field = getField(CanonicalNames.RedListFuzzyResult);
		if (field == null) {
			field = new Field(CanonicalNames.RedListFuzzyResult, this);
			getField().add(field);
		}
		
		PrimitiveField value = field.getPrimitiveField("text");
		if (value == null) {
			value = new StringPrimitiveField("text", field);
			field.getPrimitiveField().add(value);
		}
		if (string == null) {
			if (value != null)
				field.getPrimitiveField().remove(value);
		}
		else
			value.setRawValue(string);
	}
	
	public void setCategoryAbbreviation(String abbreviatedCategory) {
		RedListCriteriaField field = new RedListCriteriaField(getField(CanonicalNames.RedListCriteria));
		field.setGeneratedCategory(abbreviatedCategory);
	}
	
	public String getDisplayText() {
		return getAssessmentType().getDisplayName() + " " + getTaxon().getFriendlyName();
	}
	
	public static interface DeepCopyFilter {
		
		public Field copy(Assessment assessment, Field field);
		
	}

}
