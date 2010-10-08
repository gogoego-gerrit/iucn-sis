package org.iucn.sis.shared.api.models.fields;

import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class ThreatsSubfield extends Field {
	
	private static final long serialVersionUID = 1L;
	
	public ThreatsSubfield(ThreatsField parent, Field data) {
		super(CanonicalNames.Threats+"Subfield", null);
		setParent(parent);
		parse(data);
	}
	
	public Integer getThreat() {
		PrimitiveField<?> field = getPrimitiveField("ThreatsLookup");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setThreat(Integer threatID) {
		addPrimitiveField(new ForeignKeyPrimitiveField("ThreatsLookup", this, threatID, null));
	}
	
	public Integer getScope() {
		PrimitiveField<?> field = getPrimitiveField("scope");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setScope(Integer scope) {
		addPrimitiveField(new ForeignKeyPrimitiveField("scope", this, scope, null));
	}
	
	public String getScore() {
		PrimitiveField<?> field = getPrimitiveField("score");
		if (field == null)
			return null;
		else
			return ((StringPrimitiveField)field).getValue();
	}
	
	public void setScore(String score) {
		addPrimitiveField(new StringPrimitiveField("score", this, score));
	}
	
	public Integer getSeverity() {
		PrimitiveField<?> field = getPrimitiveField("severity");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setSeverity(Integer severity) {
		addPrimitiveField(new ForeignKeyPrimitiveField("severity", this, severity, null));
	}
	
	public Set<StressField> getStresses() {
		Set<StressField> set = new HashSet<StressField>();
		if (getFields() != null)
			for (Field field : getFields())
				getFields().add(((StressField)field));
		return set;
	}
	
	public void setStresses(Set<StressField> stresses) {
		setFields(new HashSet<Field>(stresses));
	}
	
	public void parse(Field field) {
		if (field != null){
			setFields(field.getFields());
			setPrimitiveField(field.getPrimitiveField());
		}
	}

}
