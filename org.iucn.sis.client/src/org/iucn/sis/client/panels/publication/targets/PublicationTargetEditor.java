package org.iucn.sis.client.panels.publication.targets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.client.api.caches.PublicationCache;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.PublicationTarget;
import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

public class PublicationTargetEditor extends BasicWindow implements Referenceable {
	
	private final PublicationTargetModelData model;
	
	private final TextField<String> name;
	private final DateField date;
	private final Html referenceDisplay;
	private Reference reference;
	
	public PublicationTargetEditor(PublicationTargetModelData model) {
		super(model == null ? "New Publication Target" : "Edit Publication Target");
		this.model = model;
		
		setLayout(new FillLayout());
		setSize(400, 200);
		
		String nameValue = null;
		Date dateValue = null;
		reference = null;
		
		if (model != null) {
			nameValue = model.getModel().getName();
			dateValue = model.getModel().getDate();
			reference = model.getModel().getReference();
		}
		
		final FormPanel form = new FormPanel();
		form.setLabelWidth(75);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.setHeaderVisible(false);
		form.add(name = FormBuilder.createTextField("name", nameValue, "Name", true));
		form.add(date = FormBuilder.createDateField("date", dateValue, "Date", true));
		
		HorizontalPanel refButtons = new HorizontalPanel();
		refButtons.add(new Button("Select", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				openReferenceEditor();
			}
		}));
		refButtons.add(new Button("Clear", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				reference = null;
				updateReferenceDisplay();
			}
		}));
		
		VerticalPanel refView = new VerticalPanel();
		refView.add(refButtons);
		refView.add(referenceDisplay = new Html());
		
		AdapterField refField = new AdapterField(refView);
		refField.setFieldLabel("Reference");
		
		form.add(refField);
		
		add(form);
		
		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (form.isValid())
					save();
				else
					WindowUtils.errorAlert("Please fill in all required fields.");
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
		
		updateReferenceDisplay();
	}
	
	private void openReferenceEditor() {
		ClientUIContainer.bodyContainer.openReferenceManager(this, "Select Publication Target Reference");
	}
	
	private void save() {
		final PublicationTarget source = model == null ? new PublicationTarget() : model.getModel();
		source.setName(name.getValue());
		source.setDate(date.getValue());
		source.setReference(reference);
		
		if (source.getId() == 0) {
			PublicationCache.impl.createTarget(source, new GenericCallback<Object>() {
				public void onSuccess(Object result) {
					saveAndClose();
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Could not save, please try again later.");
				}
			});
		}
		else {
			PublicationCache.impl.updateTarget(source, new GenericCallback<Object>() {
				public void onSuccess(Object result) {
					model.update(source);
					saveAndClose();
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Could not save, please try again later.");
				}
			});
		}
	}
	
	private void saveAndClose() {
		Info.display("Success", "Publication target saved");
		hide();
		fireEvent(Events.Update);
	}
	
	private void updateReferenceDisplay() {
		if (reference == null)
			referenceDisplay.setHtml("No reference selected");
		else
			referenceDisplay.setHtml("Selected: " + reference.getCitation());
	}
	
	@Override
	public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
		if (references.isEmpty())
			reference = null;
		else
			reference = references.get(0);
		updateReferenceDisplay();
		callback.onSuccess(null);
	}
	
	@Override
	public Set<Reference> getReferencesAsList() {
		HashSet<Reference> refs = new HashSet<Reference>();
		if (reference != null)
			refs.add(reference);
		return refs;
	}
	
	@Override
	public ReferenceGroup groupBy() {
		return null;
	}
	
	@Override
	public void onReferenceChanged(GenericCallback<Object> callback) {
	}
	
	@Override
	public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> listener) {
		reference = null;
		updateReferenceDisplay();
		listener.onSuccess(null);
	}
	
}
