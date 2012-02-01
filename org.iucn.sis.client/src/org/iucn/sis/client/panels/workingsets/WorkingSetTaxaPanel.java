package org.iucn.sis.client.panels.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingSetTaxaPanel extends BasicWindow {
	
	private ComplexListener<List<Integer>> saveListener;
	private ListStore<BaseModelData> store;
	
	private CheckBoxListView<BaseModelData> view;
	
	public WorkingSetTaxaPanel() {
		super("Add Taxa");
		setSize(500, 500);
		setLayout(new FillLayout());
		
		updateStore();
		
		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
				
				final List<Integer> checked = new ArrayList<Integer>();
				for (BaseModelData model : view.getChecked()){
					checked.add((Integer)model.get("value"));
				}
			
				saveListener.handleEvent(checked);

			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}

	public void setSaveListener(ComplexListener<List<Integer>> saveListener) {
		this.saveListener = saveListener;
	}
	
	public void updateStore() {

		final WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
		final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
		store.setKeyProvider(new ModelKeyProvider<BaseModelData>() {
			public String getKey(BaseModelData model) {
				return model.get("value");
			}
		});
		
		if (ws != null) {			
			for (int i = 0; i < ws.getSpeciesIDs().size(); i++) {
				Integer id = ws.getSpeciesIDs().get(i);	
				String name = TaxonomyCache.impl.getTaxon(id).getFullName();
				
				BaseModelData model = new BaseModelData();
				model.set("text", name);
				model.set("value", id);
				
				store.add(model);		
			}
		}
		this.store = store;
		
		view = new CheckBoxListView<BaseModelData>();
		view.setStore(store);
	}
	
	private void draw() {
		store.setDefaultSort("text", SortDir.ASC);
		store.sort("text", SortDir.ASC);
		
		removeAll();
		add(view);
	}
	
	@Override
	public void show() {
		WindowUtils.showLoadingAlert("Loading Taxa for Working Set...");
		DeferredCommand.addPause();
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				Timer t = new Timer() {
					public void run() {
						draw();
						DeferredCommand.addCommand(new Command() {
							public void execute() {
								open();	
							}
						});
					}
				};
				t.schedule(1500);
			}
		});
	}
	
	private void open() {
		WindowUtils.hideLoadingAlert();
		super.show();
	}

}
