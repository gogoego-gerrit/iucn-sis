package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class ByteArrayExpression extends AbstractORMExpression {
	public ByteArrayExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public ByteArrayExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(byte[] aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(byte[] aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}
}