package org.iucn.sis.server.api.persistance.hibernate;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class DateExpression extends AbstractORMExpression {
	public DateExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public DateExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(Date aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(Date aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void between(Date aValue1, Date aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(Date[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
	
	public void le(Date aValue) {
		addCriterion(Expression.le(_propName, aValue));
	}
	
	public void lt(Date aValue) {
		addCriterion(Expression.lt(_propName, aValue));
	}
	
	public void ge(Date aValue) {
		addCriterion(Expression.ge(_propName, aValue));
	}
	
	public void gt(Date aValue) {
		addCriterion(Expression.gt(_propName, aValue));
	}
}