package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class DoubleExpression extends AbstractORMExpression {
	public DoubleExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public DoubleExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(double aValue) {
		addCriterion(Expression.eq(_propName, new Double(aValue)));
	}

	public void eq(Double aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(double aValue) {
		addCriterion(Expression.ne(_propName, new Double(aValue)));
	}

	public void ne(Double aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void gt(double aValue) {
		addCriterion(Expression.gt(_propName, new Double(aValue)));
	}

	public void gt(Double aValue) {
		addCriterion(Expression.gt(_propName, aValue));
	}

	public void ge(double aValue) {
		addCriterion(Expression.ge(_propName, new Double(aValue)));
	}

	public void ge(Double aValue) {
		addCriterion(Expression.ge(_propName, aValue));
	}

	public void lt(double aValue) {
		addCriterion(Expression.lt(_propName, new Double(aValue)));
	}

	public void lt(Double aValue) {
		addCriterion(Expression.lt(_propName, aValue));
	}

	public void le(double aValue) {
		addCriterion(Expression.le(_propName, new Double(aValue)));
	}

	public void le(Double aValue) {
		addCriterion(Expression.le(_propName, aValue));
	}

	public void between(double aValue1, double aValue2) {
		addCriterion(Expression.between(_propName, new Double(aValue1), new Double(aValue2)));
	}

	public void between(Double aValue1, Double aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(double[] aValues) {
		Double[] aWrapper = new Double[aValues.length];
		for (int i = 0; i < aWrapper.length; i++) {
			aWrapper[i] = new Double(aValues[i]);
		}
		in(aWrapper);
	}

	public void in(Double[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}