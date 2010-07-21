package org.iucn.sis.server.api.persistance.hibernate;

import java.sql.Time;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class TimeExpression extends AbstractORMExpression {
	public TimeExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public TimeExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(Time aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(Time aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void between(Time aValue1, Time aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(Time[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}