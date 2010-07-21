package com.solertium.util.querybuilder.query;

import java.util.Date;

import com.solertium.lwxml.shared.NativeElement;



public class QBArbitraryConstraint implements QBConstraint {
    
    protected String id;
	protected String sql="";

    public QBArbitraryConstraint() {
    	id = ""+new Date().getTime();
    }
    
    public QBArbitraryConstraint(String sql) {
    	this();
        this.sql = sql;
    }
    
    public String saveConfig() {
    	return "<constraint id=\"" + id + "\" class=\"com.solertium.db.query.QArbitraryConstraint\"><![CDATA[" + 
    		sql + "]]></constraint>";
    }
    
    public void loadConfig(NativeElement config) {
    	id = config.getAttribute("id");
    	if (id == null)
    		id = ""+new Date().getTime();
    	sql = config.getTextContent();
    }
    
    public String getID() {
    	return id;
    }
}
