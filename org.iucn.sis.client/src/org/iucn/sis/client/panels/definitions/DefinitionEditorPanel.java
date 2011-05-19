package org.iucn.sis.client.panels.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.DefinitionCache;
import org.iucn.sis.shared.api.models.Definition;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class DefinitionEditorPanel extends ContentPanel {

	protected String defColumnWidth = "150px";
	protected String definitionsColumnWidth = "400px";

//	private final LayoutContainer definitionsPanel;
	private final Map<TextField<String>, TextArea> fields;
	private boolean drawn = false;

	public DefinitionEditorPanel() {
//		definitionsPanel = new LayoutContainer();
		fields = new HashMap<TextField<String>, TextArea>();
		setScrollMode(Scroll.AUTO);
		setHeaderVisible(false);
		
		setLayout(new TableLayout(3));
		
		draw();
	}

	private TextField<String> addDefinition(Definition definition) {
		TableData deleteColumn = new TableData();
		deleteColumn.setWidth("20px");
		deleteColumn.setVerticalAlign(VerticalAlignment.TOP);

		TableData defColumn = new TableData();
		defColumn.setWidth(defColumnWidth);
		defColumn.setVerticalAlign(VerticalAlignment.TOP);

		TableData definitionsColumn = new TableData();
		definitionsColumn.setWidth(definitionsColumnWidth);
		definitionsColumn.setVerticalAlign(VerticalAlignment.TOP);

		final TextField<String> defText = new TextField<String>();
		defText.setWidth(defColumnWidth);

		final TextArea definitionText = new TextArea();		
		definitionText.setWidth(definitionsColumnWidth);

		fields.put(defText, definitionText);
		
		final Image image = new Image("images/icon-delete.png");
		image.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				WindowUtils.confirmAlert("Delete?", "Are you sure you want to delete this definition?", new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if( be.getButtonClicked().getText().equalsIgnoreCase("yes") ) {
							remove(defText);
							remove(definitionText);
							remove(image);
							fields.remove(defText);
						}
					};
				});
			}
		});
		
		if (definition != null) {
			defText.setValue(definition.getName());
			definitionText.setValue(definition.getValue());
		}

		add(image, deleteColumn);
		add(defText, defColumn);
		add(definitionText, definitionsColumn);
		
		return defText;
	}

	private void draw() {
		for (Definition definition : DefinitionCache.impl.getDefinitions())
			addDefinition(definition);

		Button save = new Button();
		save.setText("Save");
		save.setIconStyle("icon-save");
		save.setTitle("Save");
		save.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				save();
			}
		});

		Button add = new Button();
		add.setText("Add new definition");
		add.setIconStyle("icon-add");
		add.setTitle("Add new definition");
		add.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				TextField<String> f = addDefinition(null);
				layout();
				
				scrollIntoView(f);
			}
		});

		ToolBar toolbar = new ToolBar();
		toolbar.add(save);
		toolbar.add(add);
		setTopComponent(toolbar);
	}

	protected boolean isSaveable() {
		List<String> strings = new ArrayList<String>();
		for (Entry<TextField<String>, TextArea> entry : fields.entrySet()) {
			if (entry.getKey() != null) {
				if (strings.contains(entry.getKey().getValue().toLowerCase())) {
					WindowUtils.errorAlert("Unable to save as there are multiple entries for "
									+ entry.getKey().getValue());
					return false;
				} else if (entry.getKey().getValue().trim()
						.equalsIgnoreCase("")) {
					WindowUtils.errorAlert("Unable to save as there are empty definitions");
					return false;
				} else {
					strings.add(entry.getKey().getValue().toLowerCase());
				}
			}

		}
		return true;
	}

	protected void save() {
		if (isSaveable()) {
			Map<String, Definition> definitionsMap = new HashMap<String, Definition>();
			for (Entry<TextField<String>, TextArea> entry : fields.entrySet()) {
				String name = entry.getKey().getValue().toLowerCase();
				String value = entry.getValue().getValue();
				
				Definition definition = DefinitionCache.impl.getDefinition(name);
				if (definition == null)
					definition = new Definition(name, value);
				definition.setValue(value);
				
				definitionsMap.put(definition.getName(), definition);
			}
			DefinitionCache.impl.saveDefinitions(definitionsMap,
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Failure saving definitions.");
				}
				public void onSuccess(String result) {
					Info.display("", "Saved definitions");
				}
			});
		}
	}

}
