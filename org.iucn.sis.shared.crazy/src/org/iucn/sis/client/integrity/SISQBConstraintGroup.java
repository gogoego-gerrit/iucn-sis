package org.iucn.sis.client.integrity;

import com.solertium.util.querybuilder.query.QBArbitraryConstraint;
import com.solertium.util.querybuilder.query.QBComparisonConstraint;
import com.solertium.util.querybuilder.query.QBConstraint;
import com.solertium.util.querybuilder.query.QBConstraintGroup;
import com.solertium.util.querybuilder.query.QBRelationConstraint;

public class SISQBConstraintGroup extends QBConstraintGroup {
	
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

}
