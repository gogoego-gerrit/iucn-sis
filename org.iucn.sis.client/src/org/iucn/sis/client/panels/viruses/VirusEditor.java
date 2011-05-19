package org.iucn.sis.client.panels.viruses;

import java.util.Date;

import org.iucn.sis.client.api.caches.VirusCache;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.models.Virus;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;

public class VirusEditor extends BasicWindow {
	
	private final VirusModelData model;
	
	private final TextField<String> name;
	private final TextField<String> comments;
	
	private SimpleListener saveListener;
	
	public VirusEditor(VirusModelData model) {
		super(model == null ? "Create New Virus" : "Edit Virus");
		setSize(400, 300);
		
		this.model = model;
		
		name = new TextField<String>();
		name.setAllowBlank(false);
		name.setFieldLabel("Virus Name");
		
		comments = new TextArea();
		comments.setFieldLabel("Comments");
	}
	
	public void draw() {
		if (model != null) {
			name.setValue(model.getVirus().getName());
			comments.setValue(model.getVirus().getComments());
		}
		
		final FormPanel form = new FormPanel();
		form.setHeaderVisible(false);
		form.setBorders(false);
		form.setBodyBorder(false);
		form.setLabelWidth(100);
		
		form.add(name);
		form.add(comments);
		
		add(form);
	
		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (form.isValid())
					save();
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	private void save() {
		final Virus virus;
		if (model == null) {
			virus = new Virus();
			virus.setAdded(new Date());
			virus.setUser(SimpleSISClient.currentUser);
		}
		else
			virus = model.getVirus();
		
		virus.setName(name.getValue());
		virus.setComments(comments.getValue() == null ? "" : comments.getValue());
		
		if (model == null) {
			VirusCache.impl.add(virus, new GenericCallback<Virus>() {
				public void onSuccess(Virus result) {
					fireEventAndClose();
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Could not save, please try again later.");
				}
			});
		}
		else {
			VirusCache.impl.update(virus, new GenericCallback<Virus>() {
				public void onSuccess(Virus result) {
					fireEventAndClose();
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Could not save, please try again later.");
				}
			});
		}
	}
	
	private void fireEventAndClose() {
		hide();
		if (saveListener != null) {
			try {
				saveListener.handleEvent();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setSaveListener(SimpleListener saveListener) {
		this.saveListener = saveListener;
	}
	
	@Override
	public void show() {
		draw();
		super.show();
	}
	
	

}
