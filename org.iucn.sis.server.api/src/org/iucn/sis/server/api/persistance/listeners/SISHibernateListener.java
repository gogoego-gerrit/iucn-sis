package org.iucn.sis.server.api.persistance.listeners;

import java.util.Arrays;

import org.hibernate.AssertionFailure;
import org.hibernate.event.PostCollectionUpdateEvent;
import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Taxon;

public class SISHibernateListener implements PostUpdateEventListener, PostDeleteEventListener, PostInsertEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		doUpdate(event.getEntity());
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		doUpdate(event.getEntity());
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {

		doUpdate(event.getEntity());
	}

	protected void doUpdate(Object obj) {
		try {
			System.out.println("in doUpdate hibernate with object " + obj);
			if (obj instanceof Assessment) {
				System.out.println("obj is assessment");
				SIS.get().getAssessmentIO().afterSaveAssessment((Assessment) obj);
			} else if (obj instanceof PrimitiveField) {
				System.out.println("obj is primitive field");
				// ONE OF THESE TWO CALLS SHOULD BE CORRECT, TRY GETTING FROM
				// CACHE FIRST
				Assessment assessment = SIS.get().getPrimitiveFieldIO().getUpdatedAssessment(
						((PrimitiveField) obj).getId());
				if (assessment == null) {
					assessment = ((PrimitiveField) obj).getField().getAssessment();
				}
				doUpdate(assessment);
			} else if (obj instanceof Taxon) {
				System.out.println("obj is taxon with xml " + ((Taxon) obj).toXML());
				SIS.get().getTaxonIO().afterSaveTaxon((Taxon) obj);
			} else if (obj instanceof Notes) {
				System.out.println("obj is notes");
				Notes note = (Notes) obj;
				Taxon taxon = SIS.get().getNoteIO().getNoteFromTaxon(note.getId());
				if (taxon != null)
					doUpdate(taxon);
				//TODO: DO FOR FIELDS
			} else {
				System.out.println("obj is " + obj);
			}
		} catch (Throwable e) {
			
			// e.printStackTrace();
			// throw new RuntimeException(e.getCause());
		}

	}

}
