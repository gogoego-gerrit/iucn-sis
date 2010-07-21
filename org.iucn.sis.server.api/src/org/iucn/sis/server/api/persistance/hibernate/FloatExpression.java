package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class FloatExpression extends AbstractORMExpression {
	public FloatExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public FloatExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(float aValue) {
		addCriterion(Expression.eq(_propName, new Float(aValue)));
	}

	public void eq(Float aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(float aValue) {
		addCriterion(Expression.ne(_propName, new Float(aValue)));
	}

	public void ne(Float aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void gt(float aValue) {
		addCriterion(Expression.gt(_propName, new Float(aValue)));
	}

	public void gt(Float aValue) {
		addCriterion(Expression.gt(_propName, aValue));
	}

	public void ge(float aValue) {
		addCriterion(Expression.ge(_propName, new Float(aValue)));
	}

	public void ge(Float aValue) {
		addCriterion(Expression.ge(_propName, aValue));
	}

	public void lt(float aValue) {
		addCriterion(Expression.lt(_propName, new Float(aValue)));
	}

	public void lt(Float aValue) {
		addCriterion(Expression.lt(_propName, aValue));
	}

	public void le(float aValue) {
		addCriterion(Expression.le(_propName, new Float(aValue)));
	}

	public void le(Float aValue) {
		addCriterion(Expression.le(_propName, aValue));
	}

	public void between(float aValue1, float aValue2) {
		addCriterion(Expression.between(_propName, new Float(aValue1), new Float(aValue2)));
	}

	public void between(Float aValue1, Float aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(float[] aValues) {
		Float[] aWrapper = new Float[aValues.length];
		for (int i = 0; i < aWrapper.length; i++) {
			aWrapper[i] = new Float(aValues[i]);
		}
		in(aWrapper);
	}

	public void in(Float[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}