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
import java.util.List;

import org.hibernate.classic.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMDetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.Reference;

public class ReferenceDetachedCriteria extends AbstractORMDetachedCriteria {
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
	
	public ReferenceDetachedCriteria() throws ClassNotFoundException {
		super(Reference.class, ReferenceCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		type = new StringExpression("type", this.getDetachedCriteria());
		citationShort = new StringExpression("citationShort", this.getDetachedCriteria());
		citation = new StringExpression("citation", this.getDetachedCriteria());
		citationComplete = new StringExpression("citationComplete", this.getDetachedCriteria());
		author = new StringExpression("author", this.getDetachedCriteria());
		year = new StringExpression("year", this.getDetachedCriteria());
		title = new StringExpression("title", this.getDetachedCriteria());
		secondaryAuthor = new StringExpression("secondaryAuthor", this.getDetachedCriteria());
		secondaryTitle = new StringExpression("secondaryTitle", this.getDetachedCriteria());
		placePublished = new StringExpression("placePublished", this.getDetachedCriteria());
		publisher = new StringExpression("publisher", this.getDetachedCriteria());
		volume = new StringExpression("volume", this.getDetachedCriteria());
		numberOfVolumes = new StringExpression("numberOfVolumes", this.getDetachedCriteria());
		number = new StringExpression("number", this.getDetachedCriteria());
		pages = new StringExpression("pages", this.getDetachedCriteria());
		section = new StringExpression("section", this.getDetachedCriteria());
		tertiaryAuthor = new StringExpression("tertiaryAuthor", this.getDetachedCriteria());
		tertiaryTitle = new StringExpression("tertiaryTitle", this.getDetachedCriteria());
		edition = new StringExpression("edition", this.getDetachedCriteria());
		dateValue = new StringExpression("dateValue", this.getDetachedCriteria());
		subsidiaryAuthor = new StringExpression("subsidiaryAuthor", this.getDetachedCriteria());
		shortTitle = new StringExpression("shortTitle", this.getDetachedCriteria());
		alternateTitle = new StringExpression("alternateTitle", this.getDetachedCriteria());
		isbnIssn = new StringExpression("isbnIssn", this.getDetachedCriteria());
		keywords = new StringExpression("keywords", this.getDetachedCriteria());
		url = new StringExpression("url", this.getDetachedCriteria());
	}
	
	public ReferenceDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, ReferenceCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		type = new StringExpression("type", this.getDetachedCriteria());
		citationShort = new StringExpression("citationShort", this.getDetachedCriteria());
		citation = new StringExpression("citation", this.getDetachedCriteria());
		citationComplete = new StringExpression("citationComplete", this.getDetachedCriteria());
		author = new StringExpression("author", this.getDetachedCriteria());
		year = new StringExpression("year", this.getDetachedCriteria());
		title = new StringExpression("title", this.getDetachedCriteria());
		secondaryAuthor = new StringExpression("secondaryAuthor", this.getDetachedCriteria());
		secondaryTitle = new StringExpression("secondaryTitle", this.getDetachedCriteria());
		placePublished = new StringExpression("placePublished", this.getDetachedCriteria());
		publisher = new StringExpression("publisher", this.getDetachedCriteria());
		volume = new StringExpression("volume", this.getDetachedCriteria());
		numberOfVolumes = new StringExpression("numberOfVolumes", this.getDetachedCriteria());
		number = new StringExpression("number", this.getDetachedCriteria());
		pages = new StringExpression("pages", this.getDetachedCriteria());
		section = new StringExpression("section", this.getDetachedCriteria());
		tertiaryAuthor = new StringExpression("tertiaryAuthor", this.getDetachedCriteria());
		tertiaryTitle = new StringExpression("tertiaryTitle", this.getDetachedCriteria());
		edition = new StringExpression("edition", this.getDetachedCriteria());
		dateValue = new StringExpression("dateValue", this.getDetachedCriteria());
		subsidiaryAuthor = new StringExpression("subsidiaryAuthor", this.getDetachedCriteria());
		shortTitle = new StringExpression("shortTitle", this.getDetachedCriteria());
		alternateTitle = new StringExpression("alternateTitle", this.getDetachedCriteria());
		isbnIssn = new StringExpression("isbnIssn", this.getDetachedCriteria());
		keywords = new StringExpression("keywords", this.getDetachedCriteria());
		url = new StringExpression("url", this.getDetachedCriteria());
	}
	
	public SynonymDetachedCriteria createSynonymCriteria() {
		return new SynonymDetachedCriteria(createCriteria("synonym"));
	}
	
	public CommonNameDetachedCriteria createCommon_nameCriteria() {
		return new CommonNameDetachedCriteria(createCriteria("common_name"));
	}
	
	public AssessmentDetachedCriteria createAssessmentCriteria() {
		return new AssessmentDetachedCriteria(createCriteria("assessment"));
	}
	
	public FieldDetachedCriteria createFieldCriteria() {
		return new FieldDetachedCriteria(createCriteria("field"));
	}
	
	public TaxonDetachedCriteria createTaxonCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxon"));
	}
	
	public Reference uniqueReference(Session session) {
		return (Reference) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Reference[] listReference(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Reference[]) list.toArray(new Reference[list.size()]);
	}
}

