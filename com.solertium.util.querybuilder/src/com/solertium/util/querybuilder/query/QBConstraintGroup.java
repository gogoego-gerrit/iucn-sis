package com.solertium.util.querybuilder.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class QBConstraintGroup implements QBConstraint {

    public ArrayList<Object> constraints = new ArrayList<Object>();
    //Either an Integer or QBConstraint

    private int defaultOperator = QBConstraint.CG_AND;
    
    protected String id;

    public QBConstraintGroup() {
    	id = "" + new Date().getTime();
    }

    public void remove(int index){
        Object a = constraints.get(index);
        if (a instanceof Integer)
        	return; // mistake; don't remove an operator directly
        if (index > 0) {
            Object operator = constraints.get(index-1);
            if (operator instanceof Integer) {
                constraints.remove(index-1); // remove the preceding operator
                constraints.remove(index-1); // remove the desired item
            }
        } else {
            constraints.remove(index); // remove the desired (first) item
            if (!constraints.isEmpty())
            	constraints.remove(index); // remove the subsequent operator
        }
    }

    public String saveConfig(){
        String xml = "<constraint id=\"" + id + "\" class=\"com.solertium.db.query.QConstraintGroup\">";
        Iterator<Object> it = constraints.iterator();
        while(it.hasNext()) {
        	Object in = it.next();
            if (in instanceof Integer) {
            	Integer operator = (Integer) in;
            	String innerXML = "<operator mode=\"" + operator + "\">\r\n";
            	innerXML += ((QBConstraint)it.next()).saveConfig();
            	innerXML += "\r\n</operator>\r\n";
            	xml += innerXML;
            } else {
            	String innerXML = "<operator>\r\n";
            	QBConstraint c = (QBConstraint) in;
            	innerXML += c.saveConfig() + "\r\n</operator>\r\n";
            	xml += innerXML;
            }
        }
        xml += "</constraint>";
        return xml;
    }

    public void loadConfig(NativeElement config){
    	id = config.getAttribute("id");
    	if (id == null)
    		id = "" + new Date().getTime();
        NativeNodeList children = config.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
        	NativeElement n = children.elementAt(i);
            if(n.getNodeType()!=NativeNode.ELEMENT_NODE) continue;
            if(!"operator".equals(n.getNodeName())) continue;
            NativeElement operator = n;
            String mode = operator.getAttribute("mode");
            if (mode != null && !mode.equals(""))
            	constraints.add(new Integer(mode));

            NativeNodeList constraintChildren = operator.getChildNodes();
            for (int k = 0; k < constraintChildren.getLength(); k++) {
            	NativeElement child = constraintChildren.elementAt(k);
            	if (child.getNodeName().equals("constraint")) {
            		QBConstraint c = create(child.getAttribute("class"));
                    if (c != null) {
                    	c.loadConfig(child);
                    	constraints.add(c);
                    }
            	}
            }
        }
    }

    public void getFieldsWithAskValues(ArrayList<QBComparisonConstraint> list) {
    	for (int i = 0; i < constraints.size(); i++) {
    		Object cur = constraints.get(i);
    		if (cur instanceof QBComparisonConstraint) {
    			QBComparisonConstraint current = (QBComparisonConstraint)cur;
    			if (current.ask != null && current.ask.booleanValue())
    				list.add(current);
    		}
    		else if (cur instanceof QBConstraintGroup)
    			((QBConstraintGroup)cur).getFieldsWithAskValues(list);
    	}
    }

    public QBConstraint create(String name) {
    	if (name.endsWith("QArbitraryConstraint"))
    		return new QBArbitraryConstraint();
    	else if (name.endsWith("QComparisonConstraint"))
    		return new QBComparisonConstraint();
    	else if (name.endsWith("QConstraintGroup"))
    		return new QBConstraintGroup();
    	else if (name.endsWith("QRelationConstraint"))
    		return new QBRelationConstraint();
    	else
    		return null;
    }

    public int size(){
        return constraints.size();
    }

    public boolean isEmpty(){
        return (constraints.size()<1);
    }

    public void addConstraint(QBConstraint c){
    	if (constraints.isEmpty())
    		constraints.add(c);
    	else
    		addConstraint(defaultOperator, c);
    }

    public void addConstraint(int operator, QBConstraint c){
        constraints.add(Integer.valueOf(operator));
        constraints.add(c);
    }

    public void setDefaultOperator(int operator) {
    	this.defaultOperator = operator;
    }
    
    public String getID() {
    	return id;
    }

    public Iterator<Object> iterator() {
    	return constraints.listIterator();
    }

}
