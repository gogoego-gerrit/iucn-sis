package org.iucn.sis.server.api.persistance.listeners;

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

	/**
	 * Persist copies to the file system...
	 * @param obj
	 */
	protected void doUpdate(Object obj) {
		
		/*try {
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
				else if (note.getCommonName() != null) {
					doUpdate(note.getCommonName());
				}
				else if (note.getSynonym() != null)
					doUpdate(note.getSynonym());
				//TODO: DO FOR FIELDS
			} else if (obj instanceof Synonym){
				Synonym syn = ((Synonym)obj);
				if (syn.getTaxon() != null)
					doUpdate(syn.getTaxon());
			} else if (obj instanceof CommonName) {
				CommonName cn = ((CommonName)obj);
				if (cn.getTaxon() != null) 
					doUpdate(cn.getTaxon());
			} 
		}catch (AssertionFailure e) {
			
		}catch (Throwable e) {
			
			// e.printStackTrace();
			// throw new RuntimeException(e.getCause());
		}*/
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
