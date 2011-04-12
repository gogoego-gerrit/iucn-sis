package org.iucn.sis.client.api.caches;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.Reference;

import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * ReferenceCache.java
 * 
 * A static instance of a glorified HashMap that stores lists of recently used
 * references for assessments.
 * 
 * Caches an ArrayList
 * 
 * @author carl.scott
 * 
 */
public class ReferenceCache {

	public static final ReferenceCache impl = new ReferenceCache();

	private final Map<Integer, Reference> cache; 
	
	private ReferenceCache() {
		cache = new HashMap<Integer, Reference>();
	}
	
	public void cache(Reference reference) {
		if (reference != null)
			cache.put(reference.getId(), reference);
	}
	
	public boolean contains(Integer id) {
		return cache.containsKey(id);
	}
	
	public Reference get(Integer id) {
		return cache.get(id);
	}
	
	/**
	 * Replaces an existing cached reference with the given 
	 * reference.  If this reference is not already cached, 
	 * it will NOT be cached via this method. 
	 * @param reference
	 */
	public void update(Reference reference) {
		if (reference != null && contains(reference.getId()))
			cache.put(reference.getId(), reference);
	}
	
	public void save(Reference reference, GenericCallback<ReferenceSaveResult> callback) {
		post(reference, false, reference.getId() == 0, callback);
	}
	
	private void post(final Reference reference, final boolean force, final boolean asNew, final GenericCallback<ReferenceSaveResult> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getReferenceBase() +"/refsvr/submit?force=" + Boolean.toString(force), 
				"<references>" + reference.toXML() + "</references>", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				if (caught instanceof GWTConflictException) {
					WindowUtils.confirmAlert("Confirm", "This reference is being used in some assessments, " +
						"and any changes you make will be reflected in those assessments.  Do you want to " +
						"save these changes as a new reference, or save these changes to the existing " +
						"reference?", new WindowUtils.MessageBoxListener() {
							public void onYes() {
								reference.setId(0);
								post(reference, true, true, callback);
							}
							public void onNo() {
								post(reference, true, false, callback);
							}
						}, "Save as New", "Save Existing");
				}
				else {
					callback.onFailure(caught);
				}
			}
			public void onSuccess(String result) {
				final Reference returnedRef = Reference.fromXML(document.getDocumentElement().getElementByTagName("reference"));
				cache(returnedRef);
				
				callback.onSuccess(new ReferenceSaveResult(returnedRef, asNew));
			}
		});
	}
	
	public void doLogout() {
		cache.clear();
	}
	
	public static class ReferenceSaveResult {
		
		private final Reference reference;
		private final boolean asNew;
		
		public ReferenceSaveResult(Reference reference, boolean asNew) {
			this.reference = reference;
			this.asNew = asNew;
		}
		
		public Reference getReference() {
			return reference;
		}
		
		public boolean asNew() {
			return asNew;
		}
		
	}

}
