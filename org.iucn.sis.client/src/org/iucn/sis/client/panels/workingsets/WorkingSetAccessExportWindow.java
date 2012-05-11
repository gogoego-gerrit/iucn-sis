package org.iucn.sis.client.panels.workingsets;

import java.util.Date;

import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.models.NameValueModelData;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class WorkingSetAccessExportWindow extends BasicWindow implements DrawsLazily {
	
	private final WorkingSet ws;
	
	private ComboBox<NameValueModelData> mode;
	private CheckBox historical;

	public WorkingSetAccessExportWindow() {
		super("Export to Access");
		setSize(450, 200);
		
		ws = WorkingSetCache.impl.getCurrentWorkingSet();
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		final GenericCallback<String> asyncCallback = new GenericCallback<String>() {
			public void onSuccess(String result) {
				//TODO: parse XML
				
				/*
				 * Hard-coded.  Can move this server-side to allow for a listing of 
				 * available access export types.
				 */
				final NameValueModelData defaultValue = new NameValueModelData("Access (SIS 2.0 Format)", "access");
				
				draw(new NameValueModelData[] { defaultValue }, defaultValue.getValue());
				
				callback.isDrawn();
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load export types, please try again later.");
			}
		};
		
		/*
		 * TODO: server call to get the XML containing export types.
		 * Should be {export-base}/sources
		 */
		asyncCallback.onSuccess(null);
	}
	
	private void draw(NameValueModelData[] modes, final String defaultMode) {
		final FormLayout layout = new FormLayout();
		layout.setLabelWidth(165);
		layout.setLabelPad(30);
		
		final FormPanel form = new FormPanel();
		form.setLayout(layout);
		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		
		form.add(FormBuilder.createLabelField("ws", ws.getName(), "Working Set"));
		form.add(mode = FormBuilder.createModelComboBox("mode", defaultMode, "Export Mode", true, modes));
		form.add(historical = FormBuilder.createCheckBoxField("historical", Boolean.FALSE, "Include historical assessments?"));
		
		add(form);
		
		addButton(new Button("Export", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
				export();
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	public void export() {
		final WorkingSet workingSet = WorkingSetCache.impl.getCurrentWorkingSet(); 
		final StringBuilder url = new StringBuilder();
		url.append(UriBase.getInstance().getExportBase());
		url.append("/sources/");
		url.append(mode.getValue().getValue());
		url.append('/');
		url.append(ws.getId());
		url.append("?time=" + new Date().getTime());
		if (historical.getValue().booleanValue())
			url.append("&historical=true");
			
		final Window exportWindow = WindowUtils.newWindow("Export " + workingSet.getName() + "...");
		exportWindow.setScrollMode(Scroll.AUTO);
		exportWindow.setSize(500, 400);
		exportWindow.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				exportWindow.hide();
			}
		}));
		exportWindow.setUrl(url.toString());
		exportWindow.show();
	}
	
	@Override
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();	
			}
		});
	}
	
	private void open() {
		super.show();
	}

}
