package com.solertium.util.querybuilder.query;

import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;


public class QBRelationConstraint implements QBConstraint {

    protected String f_tfspec;
    protected String t_tfspec;
    protected String id;
    
    protected int comparisonType;

    public QBRelationConstraint() {
    	id = ""+new Date().getTime();
    }

    public QBRelationConstraint(String f_tfspec, String t_tfspec) {
    	this();
        this.f_tfspec = f_tfspec;
        this.t_tfspec = t_tfspec;
        this.comparisonType = QBConstraint.CT_EQUALS;
    }
    
    public String getLeftField() {
    	return f_tfspec;
    }
    
    public void setLeftField(String f_tfspec) {
    	this.f_tfspec = f_tfspec;
    }
    
    public String getRightField() {
		return t_tfspec;
	}
    
    public void setRightField(String t_tfspec) {
    	this.t_tfspec = t_tfspec;
    }
    
    public void setComparisonType(int comparisonType) {
    	this.comparisonType = comparisonType;
    }
    
    public int getComparisonType() {
		return comparisonType;
	}
    
    public String getID() {
    	return id;
    }

    public String saveConfig() {
    	return "<constraint" + getAttrXML() + "/>";
    }

    public String getAttrXML() {
    	String xml = "";
    	xml += " id=\"" + id + "\"";
        xml += " class=\"com.solertium.db.query.QRelationConstraint\"";
        xml += " from_fieldspec=\"" +  f_tfspec + "\"";
        xml += " to_fieldspec=\"" + t_tfspec + "\"";
        xml += " comparisonType=\"" + comparisonType + "\"";
        return xml;
    }

    public void loadConfig(NativeElement config){
    	id = config.getAttribute("id");
    	if (id == null)
    		id = new Date().getTime()+"";
        f_tfspec = config.getAttribute("from_fieldspec");
        t_tfspec = config.getAttribute("to_fieldspec");
        try {
			comparisonType = Integer.parseInt(config.getAttribute("comparisonType"));
		} catch (Exception e) {
			comparisonType = QBConstraint.CT_EQUALS;
		}
    }

}
