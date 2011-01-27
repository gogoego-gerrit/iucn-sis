package org.iucn.sis.client.tabs.home;

import org.iucn.sis.client.panels.utils.BasicSearchPanel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
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
				final BasicSearchPanel panel = new BasicSearchPanel();
				
				Window window = WindowUtils.getWindow(true, false, "Taxonomy Search");
				window.addListener(Events.Show, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						String value = field.getValue();
						if (value != null && !"".equals(value))
							panel.setSearchText(field.getValue(), true);
						else
							panel.setSearchText("", true);
					}
				});
				window.setSize(800, 600);
				window.setLayout(new FillLayout());
				window.add(panel);
				window.show();
			}
		}));
	}
	
	private void search() {
		String value = field.getValue();
		if (value == null || "".equals(value)) {
			WindowUtils.errorAlert("Please enter search terms.");
			return;
		}
		
		final BasicSearchPanel panel = new BasicSearchPanel();
		
		Window window = WindowUtils.getWindow(true, false, "Taxonomy Search");
		window.addListener(Events.Show, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				panel.setSearchText(field.getValue(), false);
				panel.search(field.getValue());
				
				field.reset();
			}
		});
		window.setSize(800, 600);
		window.setLayout(new FillLayout());
		window.add(panel);
		window.show();
	}

}
