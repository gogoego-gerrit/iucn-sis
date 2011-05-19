package org.iucn.sis.client.panels.references;

import java.util.ArrayList;

import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

public class ReferenceViewTabPanel extends TabPanel implements ReferenceViewAPI {
	
	private Referenceable curReferenceable;
	private GenericCallback<Object> addCallback, removeCallback;
	
	private final GenericCallback<Object> defaultCallback = new GenericCallback<Object>() {
		public void onFailure(Throwable caught) {
			WindowUtils.errorAlert("Error!", "Error committing changes to the "
					+ "server. Ensure you are connected to the server, then try " + "the process again.");
		}

		public void onSuccess(Object result) {
			WindowUtils.infoAlert("Success!", "Successfully committed reference " + "changes.");
		}
	};
	
	private BibliographyViewTab bibliography;
	private ReferenceSearchViewTab search;
	
	public ReferenceViewTabPanel() {
		super();
		
		final TabItem bibTab = new TabItem();
		bibTab.setLayout(new FillLayout());
		bibTab.setText("Bibliography");
		bibTab.add(bibliography = new BibliographyViewTab(this));
		
		add(bibTab);
		
		final TabItem searchTab = new TabItem();
		searchTab.setLayout(new FillLayout());
		searchTab.setText("Reference Search");
		searchTab.add(search = new ReferenceSearchViewTab(this));
		
		add(searchTab);
		
		addListener(Events.Select, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				TabItem item = be.getItem();
				PagingPanel<ReferenceModel> panel = (PagingPanel)item.getItem(0);
				if (panel instanceof BibliographyViewTab)
					((BibliographyViewTab)panel).refreshView();
				else if (panel instanceof ReferenceSearchViewTab)
					((ReferenceSearchViewTab)panel).refreshView();
			}
		});
	}
	
	public void showSearchTab() {
		setSelection(getItem(1));
	}
	
	@Override
	public Referenceable getReferenceable() {
		return curReferenceable;
	}
	
	/**
	 * To override, called when user clicks "Add Selected" button. Treat as a
	 * callback
	 * 
	 * @param selectedValues
	 *            the selected values from the table.
	 */
	public void onAddSelected(ArrayList<Reference> selectedValues) {
		if (curReferenceable != null) {
			curReferenceable.addReferences(selectedValues, new GenericCallback<Object>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Error!", "Error committing changes to the "
							+ "server. Ensure you are connected to the server, then try " + "the process again.");
					
					bibliography.setReferences(curReferenceable);
					search.refreshView();
				}
				public void onSuccess(Object result) {
					WindowUtils.infoAlert("Success!", "Successfully committed reference changes.");
					
					bibliography.setReferences(curReferenceable);
					search.refreshView();
				}
			});
		}
	}
	
	public void onRemoveSelected(ArrayList<Reference> selectedValues) {
		if (curReferenceable != null) {
			curReferenceable.removeReferences(selectedValues, removeCallback);
			setReferences(curReferenceable, addCallback, removeCallback);
		}
	}
	
	public void setReferences(Referenceable referenceable) {
		setReferences(referenceable, null, null);
	}
	
	public void setReferences(Referenceable referenceable, GenericCallback<Object> addCallback,
			GenericCallback<Object> removeCallback) {
		curReferenceable = referenceable;

		this.addCallback = addCallback == null ? defaultCallback : addCallback;
		this.removeCallback = removeCallback == null ? defaultCallback : removeCallback;
		
		if (curReferenceable == null) {
			getItem(0).setEnabled(false);
			setSelection(getItem(1));
		}
		else {
			getItem(0).setEnabled(true);
			setSelection(getItem(0));
			bibliography.setReferences(referenceable);
		}
	}
	
	/**
	 * 
	 * @param reference
	 * @param saveListener
	 * @param asNew
	 */
	public static void openEditor(final ReferenceModel reference, final ComplexListener<ReferenceModel> saveListener) {
		openEditor(reference, saveListener, null);
	}
	
	public static void openEditor(final ReferenceModel reference, final ComplexListener<ReferenceModel> saveListener, final SimpleListener deleteListener) {
		final MyReferenceEditor editor = new MyReferenceEditor(reference);
		editor.setDeleteListener(deleteListener);
		editor.setSaveListener(saveListener);
		
		//editor.show();
	}
	
	private static class MyReferenceEditor extends ReferenceEditor {
		
		private final ReferenceModel original;
		
		private SimpleListener deleteListener;
		private ComplexListener<ReferenceModel> saveListener;
		
		public MyReferenceEditor(ReferenceModel reference) {
			super(reference == null ? null : reference.getModel());
			this.original = reference;
		}
		
		public void setDeleteListener(SimpleListener deleteListener) {
			this.deleteListener = deleteListener;
		}
		
		public void setSaveListener(ComplexListener<ReferenceModel> saveListener) {
			this.saveListener = saveListener;
		}

		@Override
		protected void afterDelete() {
			super.afterDelete();
			if (deleteListener != null)
				deleteListener.handleEvent();
		}
		
		private void afterSave(final ReferenceModel reference) {
			hide();
			
			if (saveListener != null)
				saveListener.handleEvent(reference);
		}

		@Override
		public void onSaveSuccessful(final Reference returnedRef, final boolean asNew) {
			if (original == null || asNew)
				afterSave(null);
			else {
				original.getModel().setId(returnedRef.getReferenceID());
				afterSave(original);
			}
		}
	}

}
