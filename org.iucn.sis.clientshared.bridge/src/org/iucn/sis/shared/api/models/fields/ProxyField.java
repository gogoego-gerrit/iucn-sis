package org.iucn.sis.shared.api.models.fields;

import java.util.Date;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.DatePrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;

public abstract class ProxyField {
	
	protected final Field proxy;
	
	public ProxyField(Field field) {
		this.proxy = field == null ? new Field() : field;
	}
	
	protected Integer getForeignKeyPrimitiveField(String key) {
		return getForeignKeyPrimitiveField(key, null);
	}
	
	protected Integer getForeignKeyPrimitiveField(String key, Integer defaultValue) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field == null)
			return defaultValue;
		else
			return ((ForeignKeyPrimitiveField)field).getValue();
	}
	
	protected void setForeignKeyPrimitiveField(String key, Integer value) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null) {
			if (value != null)
				((ForeignKeyPrimitiveField)field).setValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null)
			proxy.addPrimitiveField(new ForeignKeyPrimitiveField(key, proxy, value, null));
	}
	
	protected String getStringPrimitiveField(String key) {
		String value;
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field == null)
			value = null;
		else
			value = ((StringPrimitiveField)field).getValue();
		if (value == null)
			value = "";
		return value;
	}
	
	protected void setStringPrimitiveField(String key, String value) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null) {
			if (value != null && !"".equals(value))
				field.setRawValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null && !"".equals(value))
			proxy.addPrimitiveField(new StringPrimitiveField(key, proxy, value));
	}
	
	protected String getTextPrimitiveField(String key) {
		String value;
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field == null)
			value = null;
		else
			value = ((TextPrimitiveField)field).getValue();
		if (value == null)
			value = "";
		return value;
	}
	
	protected void setTextPrimitiveField(String key, String value) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null) {
			if (value != null && !"".equals(value))
				field.setRawValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null && !"".equals(value))
			proxy.addPrimitiveField(new TextPrimitiveField(key, proxy, value));
	}
	
	protected Boolean getBooleanPrimitiveField(String key, boolean defaultValue) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null)
			return ((BooleanPrimitiveField)field).getValue();
		else
			return defaultValue;
	}
	
	protected void setBooleanPrimitiveField(String key, Boolean value, Boolean defaultValue) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null) {
			if (value != null && !value.equals(defaultValue))
				((BooleanPrimitiveField)field).setValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null && !value.equals(defaultValue))
			proxy.addPrimitiveField(new BooleanPrimitiveField(key, proxy, value));
		else
			proxy.getPrimitiveField().remove(field);
	}
	
	protected Date getDatePrimitiveField(String key) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field == null)
			return null;
		else
			return ((DatePrimitiveField)field).getValue();
	}
	
	protected void setDatePrimitiveField(String key, Date value) {
		PrimitiveField<?> field = proxy.getPrimitiveField(key);
		if (field != null) {
			if (value != null)
				((DatePrimitiveField)field).setValue(value);
			else
				proxy.getPrimitiveField().remove(field);
		}
		else if (value != null)
			proxy.addPrimitiveField(new DatePrimitiveField(key, proxy, value));
	}

}
