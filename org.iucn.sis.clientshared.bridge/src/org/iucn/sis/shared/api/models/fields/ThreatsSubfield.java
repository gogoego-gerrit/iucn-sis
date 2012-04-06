package org.iucn.sis.shared.api.models.fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.shared.api.models.Field;

public class ThreatsSubfield extends ProxyField {
	
	private static final long serialVersionUID = 1L;
	
	public ThreatsSubfield(Field data) {
		super(data);
	}
	
	public Integer getThreat() {
		return getForeignKeyPrimitiveField("ThreatsLookup");
	}
	
	public void setThreat(Integer threatID) {
		setForeignKeyPrimitiveField("ThreatsLookup", threatID, "ThreatsLookup");
	}
	
	public Integer getTiming() {
		return getForeignKeyPrimitiveField("timing");
	}
	
	public void setTiming(Integer timing) {
		setForeignKeyPrimitiveField("timing", timing, "Threats_timingLookup");
	}
	
	public Integer getScope() {
		return getForeignKeyPrimitiveField("scope");
	}
	
	public void setScope(Integer scope) {
		setForeignKeyPrimitiveField("scope", scope, "Threats_scopeLookup");
	}
	
	public String getScore() {
		return getStringPrimitiveField("score");
	}
	
	public void setScore(String score) {
		setStringPrimitiveField("score", score);
	}
	
	public Integer getSeverity() {
		return getForeignKeyPrimitiveField("severity");
	}
	
	public void setSeverity(Integer severity) {
		setForeignKeyPrimitiveField("severity", severity, "Threats_severityLookup");
	}
	
	public Set<StressField> getStresses() {
		Set<StressField> set = new HashSet<StressField>();
		
		for (Field field : proxy.getFields())
			set.add(new StressField(field));
		
		return set;
	}
	
	public void setStresses(Collection<Integer> stresses) {
		proxy.getFields().clear();
		
		for (Integer stress : stresses) {
			Field raw = new Field("StressesSubfield", null);
			raw.setParent(proxy);
			
			StressField field = new StressField(raw);
			field.setStress(stress);
			
			proxy.getFields().add(raw);
		}
	}

}
