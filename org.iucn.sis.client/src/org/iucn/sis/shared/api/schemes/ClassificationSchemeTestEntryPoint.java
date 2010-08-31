package org.iucn.sis.shared.api.schemes;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.utils.FieldParser;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;

public class ClassificationSchemeTestEntryPoint extends SISClientBase {

	@Override
	public void loadModule() {
		instance = this;
		
		String field = Window.Location.getParameter("field");
		if (field == null) 
			buildPostLogin();
		else
			SimpleSupport.doLogin("admin", "s3cr3t");
	}
	
	@Override
	public void buildLogin(String message) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void buildPostLogin() {
		String field = Window.Location.getParameter("field");
		if (field == null) 
			open("/org.iucn.sis.ClassificationSchemes/field/FAOOccurrence.xml");
		else
			open(UriBase.getInstance().getSISBase() + "/field/" + field);
	}
	
	public void open(String uri) {
		final NativeDocument document = getHttpBasicNativeDocument();
		document.get(uri, new GenericCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {};
			public void onSuccess(String result) {
				final Display display;
				final FieldParser parser = new FieldParser();
				try {
					display = parser.parseField(document);
				} catch (Exception e) {
					WindowUtils.errorAlert("Could not parse this field.");
					return;
				}
				
				Viewport vp = new Viewport();
				vp.setLayout(new FillLayout());
				vp.add(display.showDisplay());
				
				RootPanel.get().add(vp);
			}
		});
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
