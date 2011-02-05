package org.iucn.sis.server.api.persistance;
/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: 
 * License Type: Evaluation
 */
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMCriteria;
import org.iucn.sis.server.api.persistance.hibernate.BooleanExpression;
import org.iucn.sis.server.api.persistance.hibernate.DateExpression;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.WorkingSet;

public class WorkingSetCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression description;
	public final StringExpression workflow;
	public final BooleanExpression isMostRecentPublished;
	public final DateExpression createdDate;
	
	public WorkingSetCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		name = new StringExpression("name", this);
		description = new StringExpression("description", this);
		workflow = new StringExpression("workflow", this);
		createdDate = new DateExpression("createdDate", this);
		isMostRecentPublished = new BooleanExpression("isMostRecentPublished", this);
	}
	
	public WorkingSetCriteria(Session session) {
		this(session.createCriteria(WorkingSet.class));
	}
	
	public RelationshipCriteria createRelationshipCriteria() {
		return new RelationshipCriteria(createCriteria("relationship"));
	}
	
	public AssessmentTypeCriteria createAssessment_typeCriteria() {
		return new AssessmentTypeCriteria(createCriteria("AssessmentTypes"));
	}
	
	public UserCriteria createSubscribedUsersCriteria() {
		return new UserCriteria(createCriteria("Users"));
	}
	
	public TaxonCriteria createTaxonCriteria() {
		return new TaxonCriteria(createCriteria("taxon"));
	}
	
	public RegionCriteria createRegionCriteria() {
		return new RegionCriteria(createCriteria("region"));
	}
	
	public EditCriteria createEditCriteria() {
		return new EditCriteria(createCriteria("edit"));
	}
	
	public UserCriteria createCreatorCriteria() {
		return new UserCriteria(createCriteria("creator"));
	}
	
	public WorkingSet uniqueWorkingSet() {
		return (WorkingSet) super.uniqueResult();
	}
	
	public WorkingSet[] listWorkingSet() {
		java.util.List list = super.list();
		return (WorkingSet[]) list.toArray(new WorkingSet[list.size()]);
	}

	@Override
	public Criteria createAlias(String arg0, String arg1, int arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(String arg0, String arg1, int arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnlyInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Criteria setReadOnly(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}

