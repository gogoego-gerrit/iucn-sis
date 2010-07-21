package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class IntegerExpression extends AbstractORMExpression {
	public IntegerExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public IntegerExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(int aValue) {
		addCriterion(Expression.eq(_propName, new Integer(aValue)));
	}

	public void eq(Integer aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(int aValue) {
		addCriterion(Expression.ne(_propName, new Integer(aValue)));
	}

	public void ne(Integer aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void gt(int aValue) {
		addCriterion(Expression.gt(_propName, new Integer(aValue)));
	}

	public void gt(Integer aValue) {
		addCriterion(Expression.gt(_propName, aValue));
	}

	public void ge(int aValue) {
		addCriterion(Expression.ge(_propName, new Integer(aValue)));
	}

	public void ge(Integer aValue) {
		addCriterion(Expression.ge(_propName, aValue));
	}

	public void lt(int aValue) {
		addCriterion(Expression.lt(_propName, new Integer(aValue)));
	}

	public void lt(Integer aValue) {
		addCriterion(Expression.lt(_propName, aValue));
	}

	public void le(int aValue) {
		addCriterion(Expression.le(_propName, new Integer(aValue)));
	}

	public void le(Integer aValue) {
		addCriterion(Expression.le(_propName, aValue));
	}

	public void between(int aValue1, int aValue2) {
		addCriterion(Expression.between(_propName, new Integer(aValue1), new Integer(aValue2)));
	}

	public void between(Integer aValue1, Integer aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(int[] aValues) {
		Integer[] aWrapper = new Integer[aValues.length];
		for (int i = 0; i < aWrapper.length; i++) {
			aWrapper[i] = new Integer(aValues[i]);
		}
		in(aWrapper);
	}

	public void in(Integer[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}