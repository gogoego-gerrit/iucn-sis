package com.solertium.util.querybuilder.query;

import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.util.querybuilder.utils.SQLDateTimeFormat;


public class QBComparisonConstraint implements QBConstraint {

	public static final String COMPARE_VALUE_INTEGER = "java.lang.Integer";
	public static final String COMPARE_VALUE_STRING = "java.lang.String";
	public static final String COMPARE_VALUE_FLOAT = "java.lang.Float";
	public static final String COMPARE_VALUE_DOUBLE = "java.lang.Double";
	public static final String COMPARE_VALUE_BOOLEAN = "java.lang.Boolean";
	public static final String COMPARE_VALUE_DATE = "java.util.Date";
	public static final String COMPARE_VALUE_OTHER = "java.lang.Object";

    public String tfspec;
    public Integer comparisonType;
    public Object compareValue;
    public Boolean ask;
    
    protected String id;

    public QBComparisonConstraint() {
    	id = ""+new Date().getTime();
    }

    public String saveConfig() {
    	return "<constraint" + getAttrXML() + "/>";
    }

    public String getAttrXML() {
    	String xml = "";
    	xml += " id=\"" + id + "\"";
    	if (ask != null)
    		xml += " ask=\"" + ask.toString() + "\"";
    	xml += " class=\"com.solertium.db.query.QComparisonConstraint\"";
    	xml += " fieldspec=\"" + tfspec + "\"";
    	xml += " comparisonType=\"" + comparisonType + "\"";
    	if (compareValue != null) {
	    	xml += " value=\"" + writeCompareValue() + "\"";
	    	xml += " valueClass=\"" + getCompareValueType() + "\"";
    	}

    	return xml;
    }

	private String getCompareValueType() {
		if (compareValue == null)
			return COMPARE_VALUE_STRING;

		if (compareValue instanceof Integer)
			return COMPARE_VALUE_INTEGER;
		else if (compareValue instanceof Date)
			return COMPARE_VALUE_DATE;
		else if (compareValue instanceof String)
			return COMPARE_VALUE_STRING;
		else if (compareValue instanceof Float)
			return COMPARE_VALUE_FLOAT;
		else if (compareValue instanceof Double)
			return COMPARE_VALUE_DOUBLE;
		else if (compareValue instanceof Boolean)
			return COMPARE_VALUE_BOOLEAN;
		else
			return COMPARE_VALUE_OTHER;
	}
	
	public String writeCompareValue() {
		if (compareValue == null)
			return "null";
		else if (compareValue instanceof Date)
			return SQLDateTimeFormat.getInstance().format((Date)compareValue);
		else
			return compareValue.toString();
	}

    public void loadConfig(NativeElement config){
    	id = config.getAttribute("id");
    	if (id == null)
    		id = ""+new Date().getTime();
        tfspec = config.getAttribute("fieldspec");
        comparisonType = new Integer(config.getAttribute("comparisonType"));
        String a = config.getAttribute("ask");
        if(a!=null&&(!"".equals(a))) ask = Boolean.valueOf(a.toLowerCase().equals("true"));
        String valueClass = config.getAttribute("valueClass");
        if((valueClass!=null)&&(!"".equals(valueClass))){
            if(valueClass.equals("java.lang.Integer"))
                compareValue = new Integer(config.getAttribute("value"));
            else if(valueClass.equals("java.lang.Float"))
                compareValue = new Float(config.getAttribute("value"));
            else if(valueClass.equals("java.lang.Double"))
                compareValue = Double.parseDouble(config.getAttribute("value"));
            else if(valueClass.equals("java.lang.Boolean"))
                compareValue = Boolean.valueOf(config.getAttribute("value"));
            else if (valueClass.equals("java.util.Date"))
            	compareValue = SQLDateTimeFormat.getInstance().parse(config.getAttribute("value"));
            else {
                compareValue = config.getAttribute("value");
            }
        }
    }

    public QBComparisonConstraint(String tfspec, int comparisonType, Object compareValue) {
    	this();
        this.tfspec = tfspec;
        this.comparisonType = Integer.valueOf(comparisonType);
        this.compareValue = compareValue;
    }

    public String getField() {
    	return tfspec;
    }

    public Object getComparisonValue() {
    	return compareValue;
    }

    public String getComparisonType() {
    	return comparisonType == null ? null : comparisonType+"";
    }

    public void setField(String canonicalFieldName) {
    	this.tfspec = canonicalFieldName;
    }

    public void setComparisonValue(Object value) {
    	this.compareValue = value;
    }

    public void setComparisonType(int type) {
    	this.comparisonType = Integer.valueOf(type);
    }
    
    public String getID() {
    	return id;
    }

}
