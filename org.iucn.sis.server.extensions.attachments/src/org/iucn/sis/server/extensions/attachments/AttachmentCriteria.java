package org.iucn.sis.server.extensions.attachments;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.iucn.sis.server.api.persistance.EditCriteria;
import org.iucn.sis.server.api.persistance.FieldCriteria;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMCriteria;
import org.iucn.sis.server.api.persistance.hibernate.BooleanExpression;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.FieldAttachment;

public class AttachmentCriteria extends AbstractORMCriteria {
	
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression key;
	public final BooleanExpression publish;
	
	public AttachmentCriteria(Session session) {
		this(session.createCriteria(FieldAttachment.class));
	}
	
	public AttachmentCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		name = new StringExpression("name", this);
		key = new StringExpression("key", this);
		publish = new BooleanExpression("publish", this);
	}
	
	public EditCriteria createEditCriteria() {
		return new EditCriteria(createCriteria("Edits"));
	}
	
	public FieldCriteria createFieldCriteria() {
		return new FieldCriteria(createCriteria("Fields"));
	}
	
	public FieldAttachment uniqueAttachment() {
		return (FieldAttachment)super.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public FieldAttachment[] listAttachment() {
		java.util.List list = super.list();
		return (FieldAttachment[]) list.toArray(new FieldAttachment[list.size()]);
	}
	
	@Override
	public Criteria createAlias(String associationPath, String alias,
			int joinType, Criterion withClause) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Criteria createCriteria(String associationPath, String alias,
			int joinType, Criterion withClause) throws HibernateException {
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
	public Criteria setReadOnly(boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

}
