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

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.AbstractORMDetachedCriteria;
import org.iucn.sis.server.api.persistance.hibernate.BooleanExpression;
import org.iucn.sis.server.api.persistance.hibernate.IntegerExpression;
import org.iucn.sis.server.api.persistance.hibernate.StringExpression;
import org.iucn.sis.shared.api.models.Taxon;

public class TaxonDetachedCriteria extends AbstractORMDetachedCriteria {
	public final IntegerExpression id;
	public final StringExpression name;
	public final StringExpression friendlyName;
	public final BooleanExpression hybrid;
	public final StringExpression taxonomicAuthority;
	
	public TaxonDetachedCriteria() throws ClassNotFoundException {
		super(Taxon.class, TaxonCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		friendlyName = new StringExpression("friendlyName", this.getDetachedCriteria());
		hybrid = new BooleanExpression("hybrid", this.getDetachedCriteria());
		taxonomicAuthority = new StringExpression("taxonomicAuthority", this.getDetachedCriteria());
	}
	
	public TaxonDetachedCriteria(DetachedCriteria aDetachedCriteria) {
		super(aDetachedCriteria, TaxonCriteria.class);
		id = new IntegerExpression("id", this.getDetachedCriteria());
		name = new StringExpression("name", this.getDetachedCriteria());
		friendlyName = new StringExpression("friendlyName", this.getDetachedCriteria());
		hybrid = new BooleanExpression("hybrid", this.getDetachedCriteria());
		taxonomicAuthority = new StringExpression("taxonomicAuthority", this.getDetachedCriteria());
	}
	
	public TaxonLevelDetachedCriteria createTaxonLevelCriteria() {
		return new TaxonLevelDetachedCriteria(createCriteria("taxonLevel"));
	}
	
	public TaxonStatusDetachedCriteria createTaxonStatusCriteria() {
		return new TaxonStatusDetachedCriteria(createCriteria("taxonStatus"));
	}
	
	public TaxonDetachedCriteria createTaxaCriteria() {
		return new TaxonDetachedCriteria(createCriteria("taxa"));
	}
	
	public WorkingSetDetachedCriteria createWorking_setCriteria() {
		return new WorkingSetDetachedCriteria(createCriteria("working_set"));
	}
	
	public ReferenceDetachedCriteria createReferenceCriteria() {
		return new ReferenceDetachedCriteria(createCriteria("reference"));
	}
	
	public TaxonDetachedCriteria createParentCriteria() {
		return new TaxonDetachedCriteria(createCriteria("parent"));
	}
	
	public EditDetachedCriteria createEditsCriteria() {
		return new EditDetachedCriteria(createCriteria("edits"));
	}
	
	public NotesDetachedCriteria createNotesCriteria() {
		return new NotesDetachedCriteria(createCriteria("notes"));
	}
	
	public AssessmentDetachedCriteria createAssessmentsCriteria() {
		return new AssessmentDetachedCriteria(createCriteria("assessments"));
	}
	
	public SynonymDetachedCriteria createSynonymsCriteria() {
		return new SynonymDetachedCriteria(createCriteria("synonyms"));
	}
	
	public CommonNameDetachedCriteria createCommonNamesCriteria() {
		return new CommonNameDetachedCriteria(createCriteria("commonNames"));
	}
	
	public InfratypeDetachedCriteria createInfratypeCriteria() {
		return new InfratypeDetachedCriteria(createCriteria("infratype"));
	}
	
	public Taxon uniqueTaxon(Session session) {
		return (Taxon) super.createExecutableCriteria(session).uniqueResult();
	}
	
	public Taxon[] listTaxon(Session session) {
		List list = super.createExecutableCriteria(session).list();
		return (Taxon[]) list.toArray(new Taxon[list.size()]);
	}
}

