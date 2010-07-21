package org.iucn.sis.server.api.persistance.hibernate;

import java.math.BigDecimal;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class BigDecimalExpression extends AbstractORMExpression {
	public BigDecimalExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public BigDecimalExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(BigDecimal aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(BigDecimal aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void gt(BigDecimal aValue) {
		addCriterion(Expression.gt(_propName, aValue));
	}

	public void ge(BigDecimal aValue) {
		addCriterion(Expression.ge(_propName, aValue));
	}

	public void lt(BigDecimal aValue) {
		addCriterion(Expression.lt(_propName, aValue));
	}

	public void le(BigDecimal aValue) {
		addCriterion(Expression.le(_propName, aValue));
	}

	public void between(BigDecimal aValue1, BigDecimal aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(BigDecimal[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}