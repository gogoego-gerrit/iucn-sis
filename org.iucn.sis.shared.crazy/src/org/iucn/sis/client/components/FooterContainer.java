package org.iucn.sis.client.components;


import org.iucn.sis.client.components.panels.ZendeskPanel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class FooterContainer extends LayoutContainer {
	
	
	public FooterContainer() {
		ToolBar tb = new ToolBar();
		
		Button i = new Button("Report Bug");
		i.setStyleName("icon-zendesk");
		i.setWidth("150px");
		i.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				 final ZendeskPanel window = new ZendeskPanel();
			}
		
			
			
		});
		tb.add(new FillToolItem());
		tb.add(i);
		
		
		add(tb);
	}
	
	

}

