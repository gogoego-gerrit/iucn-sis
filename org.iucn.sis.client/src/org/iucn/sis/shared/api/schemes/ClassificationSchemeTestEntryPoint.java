package org.iucn.sis.shared.api.schemes;

import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.structures.WidgetGenerator;
import org.iucn.sis.shared.api.utils.FieldParser;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;

public class ClassificationSchemeTestEntryPoint extends SISClientBase {

	@Override
	public void loadModule() {
		instance = this;
		
		FieldWidgetCache.impl.registerWidgetGenerator(new WidgetGenerator());
		
		String server = Window.Location.getParameter("server");
		if ("remote".equals(server))
			SimpleSupport.doLogin("admin", "s3cr3t");
		else
			buildPostLogin();
	}
	
	@Override
	public void buildLogin(String message) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void initializeCaches() {
		
	}
	
	@Override
	public void buildPostLogin() {
		String field = Window.Location.getParameter("field");
		if (field == null)
			field = "Threats";
		String server = Window.Location.getParameter("server");
		if (server == null || "local".equals(server))
			server = "/org.iucn.sis.ClassificationSchemes";
		else
			server = UriBase.getInstance().getSISBase();
		
		open(server + "/field/" + field + ".xml", field);
	}
	
	public void open(String uri, final String fieldName) {
		final NativeDocument document = getHttpBasicNativeDocument();
		document.get(uri, new GenericCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {};
			public void onSuccess(String result) {
				final Field field = new Field(fieldName, null);
				field.setParent(null);
				
				final Display display;
				final FieldParser parser = new FieldParser();
				try {
					display = parser.parseField(document);
				} catch (Exception e) {
					WindowUtils.errorAlert("Could not parse this field.");
					return;
				}
				display.setData(field);
				
				final LayoutContainer displayArea = new LayoutContainer(new FlowLayout());
				displayArea.setScrollMode(Scroll.AUTO);
				displayArea.setLayoutOnChange(true);
				displayArea.add(display.showDisplay());
				
				final ToolBar toolbar = new ToolBar();
				toolbar.add(new Button("Has Changed", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						WindowUtils.infoAlert("Has Changes is <b>" + display.hasChanged() + "</b>");
					}
				}));
				toolbar.add(new Button("Save", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						if (!display.hasChanged())
							Info.display("Info", "No changes to save.");
						else {
							display.save();
							simulateSave(field, 1);
							System.out.println(field.toXML());
							Info.display("Info", "Changes saved.");
						}
					}
				}));
				toolbar.add(new Button("View Only", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						display.setData(field);
						Button source = ce.getButton();
						if ("View Only".equals(source.getText())) {
							displayArea.removeAll();
							displayArea.add(display.showViewOnly());
							source.setText("Edit Mode");
						}
						else {
							displayArea.removeAll();
							displayArea.add(display.showDisplay());
							source.setText("View Only");
						}
					}
				}));
				
				final LayoutContainer container = new LayoutContainer(new BorderLayout());
				container.add(displayArea, new BorderLayoutData(LayoutRegion.CENTER));
				container.add(toolbar, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
				
				Viewport vp = new Viewport();
				vp.setLayout(new FillLayout());
				vp.add(container);
				
				RootPanel.get().add(vp);
			}
		});
	}
	
	private void simulateSave(Field field, int id) {
		field.setId(id);
		for (Field sub : field.getFields())
			simulateSave(sub, ++id);
	}
	
	@Override
	public void onAssessmentChanged() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onLogout() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onShowReferenceEditor(String title,
			Referenceable referenceable, GenericCallback<Object> onAddCallback,
			GenericCallback<Object> onRemoveCallback) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onTaxonChanged() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onWorkingSetChanged() {
		// TODO Auto-generated method stub
		
	}

}
