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
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.Reference;

public class ReferenceCriteria extends AbstractORMCriteria {
	public final IntegerExpression id;
	public final StringExpression type;
	public final StringExpression citationShort;
	public final StringExpression citation;
	public final StringExpression citationComplete;
	public final StringExpression author;
	public final StringExpression year;
	public final StringExpression title;
	public final StringExpression secondaryAuthor;
	public final StringExpression secondaryTitle;
	public final StringExpression placePublished;
	public final StringExpression publisher;
	public final StringExpression volume;
	public final StringExpression numberOfVolumes;
	public final StringExpression number;
	public final StringExpression pages;
	public final StringExpression section;
	public final StringExpression tertiaryAuthor;
	public final StringExpression tertiaryTitle;
	public final StringExpression edition;
	public final StringExpression dateValue;
	public final StringExpression subsidiaryAuthor;
	public final StringExpression shortTitle;
	public final StringExpression alternateTitle;
	public final StringExpression isbnIssn;
	public final StringExpression keywords;
	public final StringExpression url;
	public final StringExpression hash;
	public final BooleanExpression offlineStatus;
	
	public ReferenceCriteria(Criteria criteria) {
		super(criteria);
		id = new IntegerExpression("id", this);
		type = new StringExpression("type", this);
		citationShort = new StringExpression("citationShort", this);
		citation = new StringExpression("citation", this);
		citationComplete = new StringExpression("citationComplete", this);
		author = new StringExpression("author", this);
		year = new StringExpression("year", this);
		title = new StringExpression("title", this);
		secondaryAuthor = new StringExpression("secondaryAuthor", this);
		secondaryTitle = new StringExpression("secondaryTitle", this);
		placePublished = new StringExpression("placePublished", this);
		publisher = new StringExpression("publisher", this);
		volume = new StringExpression("volume", this);
		numberOfVolumes = new StringExpression("numberOfVolumes", this);
		number = new StringExpression("number", this);
		pages = new StringExpression("pages", this);
		section = new StringExpression("section", this);
		tertiaryAuthor = new StringExpression("tertiaryAuthor", this);
		tertiaryTitle = new StringExpression("tertiaryTitle", this);
		edition = new StringExpression("edition", this);
		dateValue = new StringExpression("dateValue", this);
		subsidiaryAuthor = new StringExpression("subsidiaryAuthor", this);
		shortTitle = new StringExpression("shortTitle", this);
		alternateTitle = new StringExpression("alternateTitle", this);
		isbnIssn = new StringExpression("isbnIssn", this);
		keywords = new StringExpression("keywords", this);
		url = new StringExpression("url", this);
		hash = new StringExpression("hash", this);
		offlineStatus = new BooleanExpression("offlineStatus", this);
	}
	
	public ReferenceCriteria(Session session) {
		this(session.createCriteria(Reference.class));
	}
	
	public SynonymCriteria createSynonymCriteria() {
		return new SynonymCriteria(createCriteria("synonym"));
	}
	
	public CommonNameCriteria createCommon_nameCriteria() {
		return new CommonNameCriteria(createCriteria("common_name"));
	}
	
	public AssessmentCriteria createAssessmentCriteria() {
		return new AssessmentCriteria(createCriteria("assessment"));
	}
	
	public FieldCriteria createFieldCriteria() {
		return new FieldCriteria(createCriteria("field"));
	}
	
	public TaxonCriteria createTaxonCriteria() {
		return new TaxonCriteria(createCriteria("taxon"));
	}
	
	public Reference uniqueReference() {
		return (Reference) super.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public Reference[] listReference() {
		java.util.List list = super.list();
		return (Reference[]) list.toArray(new Reference[list.size()]);
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

