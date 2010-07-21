package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class StringExpression extends AbstractORMExpression {
	public StringExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public StringExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(String aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(String aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void like(String aValue) {
		addCriterion(Expression.like(_propName, aValue));
	}
	

	public void ilike(String aValue) {
		addCriterion(Expression.ilike(_propName, aValue));
	}
}