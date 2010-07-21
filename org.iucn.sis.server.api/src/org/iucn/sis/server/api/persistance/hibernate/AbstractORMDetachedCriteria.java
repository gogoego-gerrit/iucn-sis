package org.iucn.sis.server.api.persistance.hibernate;

import java.lang.reflect.Constructor;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

public class AbstractORMDetachedCriteria {
	
	private final DetachedCriteria _detachedCriteria;
	private final Class _criteriaClass;
	
	protected AbstractORMDetachedCriteria(Class aName, Class aCriteria) throws ClassNotFoundException {
		this(getDetachedCriteria(aName.getName()), aCriteria);
	}
	
	protected AbstractORMDetachedCriteria(String aName, Class aCriteriaClass) throws ClassNotFoundException  {
		this(getDetachedCriteria(aName), aCriteriaClass);
	}

	protected AbstractORMDetachedCriteria(DetachedCriteria aDetachedCriteria, Class aCriteriaClass) {
		if (!AbstractORMCriteria.class.isAssignableFrom(aCriteriaClass))
			throw new IllegalArgumentException(aCriteriaClass.getName()+" must be a subclass of "+AbstractORMCriteria.class.getName());
		_detachedCriteria = aDetachedCriteria;
		_criteriaClass = aCriteriaClass;
	}
	
	public AbstractORMCriteria createExecutableCriteria(Session aSession) {
		try {
			Constructor constructor = _criteriaClass.getConstructor(new Class[] {Criteria.class});
			return (AbstractORMCriteria) constructor.newInstance(new Object[] {_detachedCriteria.getExecutableCriteria(aSession)});
		} catch (Throwable e) {
			return null;
		}
	}

	public DetachedCriteria getDetachedCriteria() {
		return _detachedCriteria;
	}
	
	public static DetachedCriteria getDetachedCriteria(String aClassName) throws ClassNotFoundException  {
		
			return DetachedCriteria.forClass(AbstractORMDetachedCriteria.class.getClassLoader().loadClass(aClassName));
		
	}
	
	public DetachedCriteria createCriteria(String associationPath) throws HibernateException {
		return _detachedCriteria.createCriteria(associationPath);
	}

	
}
