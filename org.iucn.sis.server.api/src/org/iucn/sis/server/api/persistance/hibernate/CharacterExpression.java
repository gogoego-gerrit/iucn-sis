package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class CharacterExpression extends AbstractORMExpression {
	public CharacterExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public CharacterExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(char aValue) {
		addCriterion(Expression.eq(_propName, new Character(aValue)));
	}

	public void eq(Character aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(char aValue) {
		addCriterion(Expression.ne(_propName, new Character(aValue)));
	}

	public void ne(Character aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}

	public void in(char[] aValues) {
		Character[] aWrapper = new Character[aValues.length];
		for (int i = 0; i < aWrapper.length; i++) {
			aWrapper[i] = new Character(aValues[i]);
		}
		in(aWrapper);
	}

	public void in(Character[] aValues) {
		addCriterion(Expression.in(_propName, aValues));
	}
}