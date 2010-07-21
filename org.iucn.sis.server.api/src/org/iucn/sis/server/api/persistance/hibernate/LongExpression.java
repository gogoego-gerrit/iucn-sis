package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class LongExpression extends AbstractORMExpression {
	public LongExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public LongExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(long aValue) {
		addCriterion(Expression.eq(_propName, new Long(aValue)));
	}

	public void eq(Long aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(long aValue) {
		addCriterion(Expression.ne(_propName, new Long(aValue)));
	}

	public void ne(Long aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void gt(long aValue) {
		addCriterion(Expression.gt(_propName, new Long(aValue)));
	}

	public void gt(Long aValue) {
		addCriterion(Expression.gt(_propName, aValue));
	}

	public void ge(long aValue) {
		addCriterion(Expression.ge(_propName, new Long(aValue)));
	}

	public void ge(Long aValue) {
		addCriterion(Expression.ge(_propName, aValue));
	}

	public void lt(long aValue) {
		addCriterion(Expression.lt(_propName, new Long(aValue)));
	}

	public void lt(Long aValue) {
		addCriterion(Expression.lt(_propName, aValue));
	}

	public void le(long aValue) {
		addCriterion(Expression.le(_propName, new Long(aValue)));
	}

	public void le(Long aValue) {
		addCriterion(Expression.le(_propName, aValue));
	}

	public void between(long aValue1, long aValue2) {
		addCriterion(Expression.between(_propName, new Long(aValue1), new Long(aValue2)));
	}

	public void between(Long aValue1, Long aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(long[] aValues) {
		Long[] aWrapper = new Long[aValues.length];
		for (int i = 0; i < aWrapper.length; i++) {
			aWrapper[i] = new Long(aValues[i]);
		}
		in(aWrapper);
	}

	public void in(Long[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}