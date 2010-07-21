package org.iucn.sis.server.api.persistance.hibernate;

import java.sql.Blob;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

public class BlobExpression extends AbstractORMExpression {
	public BlobExpression(String aName, Criteria aCriteria) {
		super(aName, aCriteria);
	}
	
	public BlobExpression(String aName, DetachedCriteria aDetachedCriteria) {
		super(aName, aDetachedCriteria);
	}

	public void eq(Blob aValue) {
		addCriterion(Expression.eq(_propName, aValue));
	}

	public void ne(Blob aValue) {
		addCriterion(Expression.ne(_propName, aValue));
	}
}