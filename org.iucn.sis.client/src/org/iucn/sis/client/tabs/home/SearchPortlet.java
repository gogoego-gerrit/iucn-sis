package org.iucn.sis.client.tabs.home;

import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.search.SearchQuery;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.solertium.util.extjs.client.WindowUtils;

public class SearchPortlet extends Portlet {
	
	private final TextField<String> field;
	
	public SearchPortlet() {
		super();
		setCollapsible(true);
		setAnimCollapse(false);
		setLayout(new FitLayout());
		setLayoutOnChange(true);
		setHeading("Search Taxonomy");
		setButtonAlign(HorizontalAlignment.CENTER);
		
		field = new TextField<String>();
		field.setEmptyText("Enter search terms");
		field.addKeyListener(new KeyListener() {
			public void componentKeyPress(ComponentEvent event) {
				if (KeyCodes.KEY_ENTER == event.getKeyCode())
					search();
			}
		});
		
		add(field);
		
		addButton(new Button("Search", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				search();
			}
		}));
		addButton(new Button("Advanced Search...", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				String value = field.getValue();
				
				SearchQuery query;
				if (value == null || "".equals(value))
					query = new SearchQuery("");
				else
					query = new SearchQuery(value);
				
				field.reset();
				
				ClientUIContainer.bodyContainer.openSearch(query, true, false);
			}
		}));
	}
	
	private void search() {
		String value = field.getValue();
		if (value == null || "".equals(value)) {
			WindowUtils.errorAlert("Please enter search terms.");
			return;
		}
		
		if(value.toString().length() < 3){
			WindowUtils.errorAlert("Please enter at least 3 Characters to search.");
			return;			
		}
		
		SearchQuery query = new SearchQuery(value);
		query.setCommonName(true);
		query.setScientificName(true);
		query.setSynonym(true);
		
		field.reset();
		
		ClientUIContainer.bodyContainer.openSearch(query);
	}

}
