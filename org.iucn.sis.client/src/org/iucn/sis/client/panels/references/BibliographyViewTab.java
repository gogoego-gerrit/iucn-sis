package org.iucn.sis.client.panels.references;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.assessment.ReferenceableAssessment;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.displays.FieldDisplay;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class BibliographyViewTab extends PagingPanel<ReferenceModel> {
	
	private final ReferenceViewAPI parent;
	
	private Grid<ReferenceModel> grid;
	
	private boolean fieldsShowing;
	
	private Set<Reference> currentReferences;
	
	public BibliographyViewTab(ReferenceViewAPI parent) {
		super();
		this.parent = parent;
		setLayout(new FillLayout());
		setPageCount(200);
		draw();
	}
	
	public void draw() {
		fieldsShowing = false;
		final GridSelectionModel<ReferenceModel> sm = 
			new GridSelectionModel<ReferenceModel>();
		sm.setSelectionMode(SelectionMode.MULTI);
		
		grid = new Grid<ReferenceModel>(getStoreInstance(), getColumnModel());
		grid.setSelectionModel(sm);
		grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<ReferenceModel>>() {
			public void handleEvent(GridEvent<ReferenceModel> be) {
				editReference(be.getModel());
			}
		});
		
		final LayoutContainer wrapper = new LayoutContainer();
		wrapper.add(getPagingToolbar());
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(createToolbar(), new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(wrapper, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		add(container);
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}

	public void setReferences(Referenceable curReferenceable) {
		currentReferences = curReferenceable.getReferencesAsList();
		showBibliography();
	}
	
	private void removeFromCurrentList(Reference ref) {
		Reference remove = null;
		for (Reference cur : currentReferences) {
			if (ref.getReferenceID() == cur.getReferenceID() && ref.getField().containsAll(cur.getField()))
				remove = cur;
		}

		currentReferences.remove(remove);
	}
	
	private ToolBar createToolbar() {
		final ToolBar biblioBar = new ToolBar();
		biblioBar.setHeight(25);

		ToggleButton removeItem = new ToggleButton();
		removeItem.setText("Remove Reference");
		removeItem.setIconStyle("icon-book-delete");
		removeItem.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				/*if (!fieldsShowing && nonUniqueIDs) {
					WindowUtils.errorAlert("Cannot remove", "Your bibliography has "
							+ "the same reference attached to multiple fields. To ensure "
							+ "the reference is removed from the correct place, please "
							+ "filter your references by field, then try removing again.");
				} else */
				if( !AuthorizationCache.impl.hasRight(AuthorizableObject.WRITE, 
						AssessmentCache.impl.getCurrentAssessment()) ) {
					WindowUtils.errorAlert("Cannot remove", "You do not have permission to edit this " +
							"assessment, so you may not remove references.");
				} else {
					ArrayList<Reference> list = new ArrayList<Reference>();
					List<ReferenceModel> items = grid.getSelectionModel().getSelectedItems();
					for (ReferenceModel curItem : items) {
						Reference curRef = curItem.getModel();
						list.add(curRef);
						removeFromCurrentList(curRef);
					}
					if (!list.isEmpty()) {
						parent.onRemoveSelected(list);
					}
				}
			}
		});
		Button editExisting = new Button();
		editExisting.setIconStyle("icon-book-edit");
		editExisting.setText("Edit/View Selected Reference");
		editExisting.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				editReference(grid.getSelectionModel().getSelectedItem());
			}
		});
		
		final ToggleButton showFields = new ToggleButton();
		showFields.setIconStyle("icon-accept");
		showFields.setText("Show Field Info");		
		showFields.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				final ColumnConfig column = grid.getColumnModel().getColumnById("field");
				
				final boolean showing = column.isHidden();
				column.setHidden(!showing);
				if (showing) {
					showFields.setText("Hide Field Info");
					showFields.setIconStyle("icon-cross");
					fieldsShowing = true;
					refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				} else {
					showFields.setText("Show Field Info");
					showFields.setIconStyle("icon-accept");
					fieldsShowing = false;	
					refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
			}
		});
		
		biblioBar.add(removeItem);
		biblioBar.add(editExisting);
		biblioBar.add(showFields);
		
		return biblioBar;
	}
	
	private void displayFieldColomn(boolean value){
		ColumnConfig column = grid.getColumnModel().getColumnById("field");
		column.setHidden(value);
	}
	
	private void editReference(ReferenceModel selection) {
		if (selection == null)
			WindowUtils.errorAlert("Error", "Please select a record to view.");
		else {
			final ComplexListener<ReferenceModel> saveListener = new ComplexListener<ReferenceModel>() {
				public void handleEvent(ReferenceModel reference) {
					if (reference != null && getProxy().getStore() != null) {
						reference.rebuild();
						getProxy().getStore().update(reference);
					}
					
					final Referenceable curReferenceable = parent.getReferenceable();

					if (curReferenceable != null) {
						setReferences(curReferenceable);
						showBibliography();
					}
					
					curReferenceable.onReferenceChanged(new GenericCallback<Object>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Error!", "Error committing changes to the "
									+ "server. Ensure you are connected to the server, then try " + "the process again.");
						}

						public void onSuccess(Object result) {
							WindowUtils.infoAlert("Success!", "Successfully committed reference " + "changes.");
						}
					});
				}
			};
			
			ReferenceViewTabPanel.openEditor(selection, saveListener);
		}
	}
	
	private ColumnModel getColumnModel() {
		final ColumnConfig citation = new ColumnConfig("citation", "Citation", 500);
		citation.setRenderer(new GridCellRenderer<ReferenceModel>() {
			public Object render(ReferenceModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ReferenceModel> store, Grid<ReferenceModel> grid) {
				return model.getVisibleCitation();
			}
		});

		final ColumnConfig field = new ColumnConfig("field", "Field", 150);
		
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
		list.add(citation);
		list.add(field);
		
		return new ColumnModel(list);
	}
	
	@Override
	protected void getStore(GenericCallback<ListStore<ReferenceModel>> callback) {
		if (currentReferences != null) {
			final ListStore<ReferenceModel> store = new ListStore<ReferenceModel>();
			store.setStoreSorter(new StoreSorter<ReferenceModel>(new PortableAlphanumericComparator()));
			
			if (parent.getReferenceable() != null) {
				if (parent.getReferenceable() instanceof ReferenceableAssessment) {
					for (Reference reference : parent.getReferenceable().getReferencesAsList()) {
						ReferenceModel model = new ReferenceModel(reference);
						model.setField("Global");
						
						store.add(model);
					}
					for (Field field : ((ReferenceableAssessment)parent.getReferenceable()).getAssessment().getField()) {
						for (Reference reference : field.getReference()) {
							ReferenceModel model = new ReferenceModel(reference);
							model.setField(field.getName());
							
							store.add(model);
						}
					}
				}
				else {
					boolean isField = parent.getReferenceable() != null && parent.getReferenceable() instanceof FieldDisplay; 
					for (Iterator<Reference> local = currentReferences.iterator(); local.hasNext(); ) {
						final Reference current = local.next();
						current.generateCitationIfNotAlreadyGenerate();
						
						final ReferenceModel model = new ReferenceModel(current);
						
						if (isField)
							model.setField(((FieldDisplay)parent.getReferenceable()).getCanonicalName());
			
						store.add(model);
					}
				}
			}
			
			if(fieldsShowing){
				callback.onSuccess(store);
				displayFieldColomn(false);
			}else{
				callback.onSuccess(removeDuplicates(store));
				displayFieldColomn(true);
			}
			
		}
	}

	private ListStore<ReferenceModel> removeDuplicates(ListStore<ReferenceModel> store){
		ListStore<ReferenceModel> newStore = new ListStore<ReferenceModel>();
		for(int i = 0; i < store.getCount(); i++){

			if(newStore.getCount() == 0)
				newStore.add(store.getAt(i));
			else{ 
				boolean found = true; 
				for(ReferenceModel newModel : newStore.getModels()){ 
					if(newModel.getModel().getCitation().equals(store.getAt(i).getModel().getCitation())){ 
						found = true;
						break;
					}else{
						found = false;	
					}
				}
				if(!found)
					newStore.add(store.getAt(i));
			}
		}
		return newStore;
	}
	
	/**
	 * Shows recently used references. We just want to show them all, and we
	 * don't have download concerns since this is store on the client. Hence we
	 * are not reusing the show() function directly.
	 * 
	 */
	private void showBibliography() {
		grid.getColumnModel().getColumnById("field").setHidden(parent.getReferenceable() == null || !(parent.getReferenceable() instanceof ReferenceableAssessment));
		
		/*if (biblioBar.getParent() == null) {
			HorizontalPanel hp = new HorizontalPanel();
			HTML html = new HTML("* Citation possibly incomplete");
			hp.setSpacing(10);
			hp.add(html);
			html.addStyleName("red-menu");

			RowData fill_horizontal = new RowData();
			fill_horizontal.setWidth(1d);
			fill_horizontal.setHeight(25);
			biblioContainer.add(biblioBar, fill_horizontal);
			biblioContainer.add(bibTable, new RowData(1d, 1d));
			biblioContainer.add(hp, fill_horizontal);
		}*/

		refresh(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				layout();
			}
		});
		/*try {
			biblioContainer.layout();
			bibTable.sort(0, SortDir.ASC);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
}
