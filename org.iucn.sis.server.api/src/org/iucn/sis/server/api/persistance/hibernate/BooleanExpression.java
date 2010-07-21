package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class BooleanExpression extends AbstractORMExpression {
	public BooleanExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public BooleanExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(boolean aValue) {
		addCriterion(Expression.eq(_propName, new Boolean(aValue)));
	}

	public void eq(Boolean aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(boolean aValue) {
		addCriterion(Expression.ne(_propName, new Boolean(aValue)));
	}

	public void ne(Boolean aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}
}