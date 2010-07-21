package org.iucn.sis.server.api.persistance.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

public class AbstractORMExpression {
	
	private final int TYPE_NORMAL = 0;
	private final int TYPE_DETACHED = 1;
	
	protected final Criteria _criteria;
	protected final DetachedCriteria _detachedCriteria;
	protected final int _type;
	protected final String _propName;

	public AbstractORMExpression(String aName, Criteria aCriteria) {
		this._propName = aName;
		this._criteria = aCriteria;
		this._detachedCriteria = null;
		this._type = TYPE_NORMAL; 
	}
	
	public AbstractORMExpression(String aName, DetachedCriteria aDetachedCriteria) {
		this._propName = aName;
		this._criteria = null;
		this._detachedCriteria = aDetachedCriteria;
		this._type = TYPE_DETACHED;
	}
	
	public void isEmpty() {
		addCriterion(Expression.isEmpty(_propName));
	}
	
	public void isNotEmpty() {
		addCriterion(Expression.isNotEmpty(_propName));
	}

	public void isNull() {
		addCriterion(Expression.isNull(_propName));
	}
	
	public void isNotNull() {
		addCriterion(Expression.isNotNull(_propName));
	}
	
	public void order(boolean aAscending) {
		Order lOrder;
		if (aAscending) {
			lOrder = Order.asc(_propName);
		} else {
			lOrder = Order.desc(_propName);
		}
		if (_type==TYPE_NORMAL)
			_criteria.addOrder(lOrder);
		else
			_detachedCriteria.addOrder(lOrder);			
	}
	
	public void addCriterion(Criterion aCriterion) {
		if (_type==TYPE_DETACHED)
			_detachedCriteria.add(aCriterion);
		else
			_criteria.add(aCriterion);
	}
}
