package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class ByteExpression extends AbstractORMExpression {
	public ByteExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public ByteExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(byte aValue) {
		addCriterion(Expression.eq(_propName, new Byte(aValue)));
	}

	public void eq(Byte aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(byte aValue) {
		addCriterion(Expression.ne(_propName, new Byte(aValue)));
	}

	public void ne(Byte aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void gt(byte aValue) {
		addCriterion(Expression.gt(_propName, new Byte(aValue)));
	}

	public void gt(Byte aValue) {
		addCriterion(Expression.gt(_propName, aValue));
	}

	public void ge(byte aValue) {
		addCriterion(Expression.ge(_propName, new Byte(aValue)));
	}

	public void ge(Byte aValue) {
		addCriterion(Expression.ge(_propName, aValue));
	}

	public void lt(byte aValue) {
		addCriterion(Expression.lt(_propName, new Byte(aValue)));
	}

	public void lt(Byte aValue) {
		addCriterion(Expression.lt(_propName, aValue));
	}

	public void le(byte aValue) {
		addCriterion(Expression.le(_propName, new Byte(aValue)));
	}

	public void le(Byte aValue) {
		addCriterion(Expression.le(_propName, aValue));
	}

	public void between(byte aValue1, byte aValue2) {
		addCriterion(Expression.between(_propName, new Byte(aValue1), new Byte(aValue2)));
	}

	public void between(Byte aValue1, Byte aValue2) {
		addCriterion(Expression.between(_propName, aValue1, aValue2));
	}

	public void in(byte[] aValues) {
		Byte[] aWrapper = new Byte[aValues.length];
		for (int i = 0; i < aWrapper.length; i++) {
			aWrapper[i] = new Byte(aValues[i]);
		}
		in(aWrapper);
	}

	public void in(Byte[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}