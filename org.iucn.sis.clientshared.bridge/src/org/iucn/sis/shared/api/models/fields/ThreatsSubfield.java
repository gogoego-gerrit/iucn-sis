package org.iucn.sis.shared.api.models.fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

public class ThreatsSubfield {
	
	private static final long serialVersionUID = 1L;
	
	protected Field proxy;
	
	public ThreatsSubfield(Field data) {
		this.proxy = data == null ? new Field("ThreatsSubfield", null) : data;
	}
	
	public Integer getThreat() {
		PrimitiveField<?> field = proxy.getPrimitiveField("ThreatsLookup");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setThreat(Integer threatID) {
		proxy.addPrimitiveField(new ForeignKeyPrimitiveField("ThreatsLookup", proxy, threatID, null));
	}
	
	public Integer getTiming() {
		PrimitiveField<?> field = proxy.getPrimitiveField("timing");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setTiming(Integer timing) {
		proxy.addPrimitiveField(new ForeignKeyPrimitiveField("timing", proxy, timing, null));
	}
	
	public Integer getScope() {
		PrimitiveField<?> field = proxy.getPrimitiveField("scope");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setScope(Integer scope) {
		proxy.addPrimitiveField(new ForeignKeyPrimitiveField("scope", proxy, scope, null));
	}
	
	public String getScore() {
		PrimitiveField<?> field = proxy.getPrimitiveField("score");
		if (field == null)
			return null;
		else
			return ((StringPrimitiveField)field).getValue();
	}
	
	public void setScore(String score) {
		proxy.addPrimitiveField(new StringPrimitiveField("score", proxy, score));
	}
	
	public Integer getSeverity() {
		PrimitiveField<?> field = proxy.getPrimitiveField("severity");
		if (field == null)
			return null;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	public void setSeverity(Integer severity) {
		proxy.addPrimitiveField(new ForeignKeyPrimitiveField("severity", proxy, severity, null));
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
			
			proxy.getFields().add(field.getField());
		}
	}

}
