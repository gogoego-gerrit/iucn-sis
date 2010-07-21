package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class ShortExpression extends AbstractORMExpression {
	public ShortExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public ShortExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(short aValue) {
		addCriterion(Expression.eq(_propName, new Short(aValue)));
	}

	public void eq(Short aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(short aValue) {
		addCriterion(Expression.ne(_propName, new Short(aValue)));
	}

	public void ne(Short aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void gt(short aValue) {
		addCriterion(Expression.gt(_propName, new Short(aValue)));
	}

	public void gt(Short aValue) {
		addCriterion(Expression.gt(_propName, aValue));
	}

	public void ge(short aValue) {
		addCriterion(Expression.ge(_propName, new Short(aValue)));
	}

	public void ge(Short aValue) {
		addCriterion(Expression.ge(_propName, aValue));
	}

	public void lt(short aValue) {
		addCriterion(Expression.lt(_propName, new Short(aValue)));
	}

	public void lt(Short aValue) {
		addCriterion(Expression.lt(_propName, aValue));
	}

	public void le(short aValue) {
		addCriterion(Expression.le(_propName, new Short(aValue)));
	}

	public void le(Short aValue) {
		addCriterion(Expression.le(_propName, aValue));
	}

	public void between(short aValue1, short aValue2) {
		addCriterion(Expression.between(_propName, new Short(aValue1), new Short(aValue2)));
	}

	public void between(Short aValue1, Short aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(short[] aValues) {
		Short[] aWrapper = new Short[aValues.length];
		for (int i = 0; i < aWrapper.length; i++) {
			aWrapper[i] = new Short(aValues[i]);
		}
		in(aWrapper);
	}

	public void in(Short[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}