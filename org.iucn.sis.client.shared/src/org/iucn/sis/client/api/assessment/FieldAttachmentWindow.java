package org.iucn.sis.client.api.assessment;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

public class FieldAttachmentWindow extends BasicWindow {
	
	private final FormPanel form;
	private final Assessment assessment;
	private final Field field;
	private final FieldAttachment model;
	
	private FieldSet group;
	
	public FieldAttachmentWindow(Assessment assessment, Field field) {
		this(assessment, field, null);
	}
	
	public FieldAttachmentWindow(Assessment assessment, Field field, FieldAttachment model) {
		super("Upload File", "icon-attachment");
		setLayout(new FillLayout());
		
		this.assessment = assessment;
		this.field = field;
		this.model = model;
		
		if (model == null)
			setSize(400, 450);
		else
			setSize(400, 150);
		
		form = new FormPanel();
		if (model == null)
			form.setAction(UriBase.getInstance().getAttachmentBase() + "/assessments/" + assessment.getId());
		else
			form.setAction(UriBase.getInstance().getAttachmentBase() + "/assessments/" + assessment.getId() + "/" + model.getId());
		form.setMethod(Method.POST);
		form.setEncoding(Encoding.MULTIPART);
		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
			public void handleEvent(FormEvent be) {
				getButtonBar().disable();
			}
		});
		form.addListener(Events.Submit, new Listener<FormEvent>() {
			public void handleEvent(FormEvent be) {
				hide();
				Debug.println("Attachment upload results: " + be.getResultHtml());
				boolean read = true;
				
				StringBuilder out = new StringBuilder();
				for (char c : be.getResultHtml().toCharArray()) {
					if (c == '<')
						read = false;
					else if (c == '>')
						read = true;
					else if (read)
						out.append(c);
				}
				
				WindowUtils.infoAlert("Results", out.toString());
			}
		});
		
		build();
		
		add(form);
		addButton(new Button("Submit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (form.isValid()) {
					boolean found = FieldAttachmentWindow.this.model != null;
					for (Component c : group.getItems()) {
						if (c instanceof CheckBox) {
							Boolean value = ((CheckBox)c).getValue();
							if (value != null && value.booleanValue()) {
								found = true;
								break;
							}
						}
					}
					if (!found)
						WindowUtils.errorAlert("Please select at least one field for this attachment.");
					else
						form.submit();
				}
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	private void build() {
		FieldSet other = new FieldSet();
		other.setLayout(new FormLayout());
		other.setHeading("File Attachment");
		other.add(FormBuilder.createFileUploadField("file", "Select File", true));
		if (model == null)
			other.add(FormBuilder.createCheckBoxField("publish", null, "Publish this file"));
		
		group = new FieldSet();
		group.setLayout(new FormLayout());
		group.setHeading("Select Fields");
		
		for (String fieldName : CanonicalNames.attachable) {
			CheckBox box = FormBuilder.createCheckBoxField(fieldName, field != null && fieldName.equals(field.getName()) ? Boolean.TRUE : null, fieldName);
			if (assessment.getField(fieldName) == null) {
				box.setValue(false);
				box.setEnabled(false);
			}
			group.add(box);
		}
		
		form.add(other);
		if (model == null)
			form.add(group);
		form.add(FormBuilder.createHiddenField("user", SISClientBase.currentUser.getId()+""));
	}

}
