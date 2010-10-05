package org.iucn.sis.client.panels;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.panels.zendesk.ZendeskPanel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Window;

public class FooterContainer extends ToolBar {
	
	public FooterContainer() {
		super();
		
		add(new FillToolItem());
		
		if ("true".equals(Window.Location.getParameter("debug"))) {
			Button log = new Button("Debugging Output");
			log.addSelectionListener(new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					final com.extjs.gxt.ui.client.widget.Window window = 
						new com.extjs.gxt.ui.client.widget.Window();
					window.setHeading("Debugging Output");
					window.setModal(false);
					window.setClosable(true);
					window.setAutoHide(true);
					window.setLayout(new FitLayout());
					window.setButtonAlign(HorizontalAlignment.CENTER);
					window.setSize(450, 300);
					window.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							window.hide();
						}
					}));
					final StringBuilder builder = new StringBuilder();
					final String[] out = SISClientBase.instance.getLog().getAll();
					for (String x : out)
						builder.append(x + "\r\n-----------------------------\r\n");
					
					final TextArea area = new TextArea();
					area.setReadOnly(true);
					area.setValue(builder.toString());
					
					window.add(area);
					window.show();
				}
			});
			add(log);
		}
		
		Button zendesk = new Button("Report Bug");
		zendesk.setStyleName("icon-zendesk");
		zendesk.setWidth("150px");
		zendesk.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				 new ZendeskPanel();
			}
		});
		add(zendesk);
	}
	
	

}

