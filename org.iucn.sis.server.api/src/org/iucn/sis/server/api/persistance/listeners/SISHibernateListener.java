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
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;

public class SISHibernateListener implements PreInsertEventListener, PreUpdateEventListener, PostUpdateEventListener, PostDeleteEventListener, PostInsertEventListener {

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
			if (obj instanceof Assessment) {
				SIS.get().getAssessmentIO().afterSaveAssessment((Assessment) obj);
			} else if (obj instanceof PrimitiveField) {
				// ONE OF THESE TWO CALLS SHOULD BE CORRECT, TRY GETTING FROM
				// CACHE FIRST
				Assessment assessment = SIS.get().getPrimitiveFieldIO().getUpdatedAssessment(
						((PrimitiveField) obj).getId());
				if (assessment == null) {
					assessment = ((PrimitiveField) obj).getField().getAssessment();
				}
				doUpdate(assessment);
			} else if (obj instanceof Taxon) {
				SIS.get().getTaxonIO().afterSaveTaxon((Taxon) obj);
				
			} else if (obj instanceof Notes) {
				Notes note = (Notes) obj;
				Taxon taxon = SIS.get().getNoteIO().getNoteFromTaxon(note.getId());
				if (taxon != null)
					doUpdate(taxon);
				//TODO: DO FOR FIELDS
			} else if (obj instanceof Synonym){
				Synonym syn = ((Synonym)obj);
				if (syn.getTaxon() != null)
					doUpdate(syn.getTaxon());
			}
		}catch (AssertionFailure e) {
			
		}catch (Throwable e) {
			
			// e.printStackTrace();
			// throw new RuntimeException(e.getCause());
		}
	}

	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {
		//System.out.println("in preupdate event with " + event.getEntity().getClass() + " with id " + event.getId());
		return true;
	}

	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		//System.out.println("in preinsert event with "+ event.getEntity().getClass() + " with id " + event.getId());
		return true;
	}

}
