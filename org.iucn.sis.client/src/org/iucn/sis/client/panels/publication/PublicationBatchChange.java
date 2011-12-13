package org.iucn.sis.client.panels.publication;

import java.util.List;

import org.iucn.sis.client.api.caches.PublicationCache;
import org.iucn.sis.client.panels.publication.targets.PublicationTargetEditor;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.PublicationTarget;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.solertium.util.extjs.client.FormBuilder;

public class PublicationBatchChange extends FormPanel {
	
	private ComboBox<NameValueModelData> status, goal, approved;
	private TextArea notes;
	
	public PublicationBatchChange() {
		super();
		setHeading("Batch Update");
		setBorders(false);
		setBodyBorder(false);
		setLabelWidth(150);
		
		addButton(new Button("Add Publication Targets", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				PublicationTargetEditor editor = new PublicationTargetEditor(null);
				editor.addListener(Events.Update, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						refreshStores();
					}
				});
				editor.show();
			}
		}));
		
		addButton(new Button("Submit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				doSubmit();
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				cancel();
			}
		}));
	}
	
	public void update() {
		removeAll();
		
		status = newComboBox("Status");
		for (String value : new String[] { AssessmentType.DRAFT_ASSESSMENT_TYPE, AssessmentType.SUBMITTED_ASSESSMENT_TYPE, 
				AssessmentType.FOR_PUBLICATION_ASSESSMENT_TYPE, AssessmentType.PUBLISHED_ASSESSMENT_TYPE }) {
			AssessmentType type = AssessmentType.getAssessmentType(value);
			status.getStore().add(new NameValueModelData(type.getDisplayName(true), type.getName()));
		}
		add(status);
		
		List<PublicationTarget> options = PublicationCache.impl.listTargetsFromCache();
		
		goal = newComboBox("Publication Target");
		for (PublicationTarget option : options)
			goal.getStore().add(new NameValueModelData(option.getName(), option.getId()+""));
		
		add(goal);
		
		approved = newComboBox("For Publication");
		for (PublicationTarget option : options)
			approved.getStore().add(new NameValueModelData(option.getName(), option.getId()+""));
		
		add(approved);
		
		add(notes = FormBuilder.createTextArea("notes", null, "Notes", false));
		
		layout();
	}
	
	private void refreshStores() {
		List<PublicationTarget> options = PublicationCache.impl.listTargetsFromCache();
		
		goal.getStore().removeAll();
		approved.getStore().removeAll();
		
		for (PublicationTarget option : options) {
			goal.getStore().add(new NameValueModelData(option.getName(), option.getId()+""));
			approved.getStore().add(new NameValueModelData(option.getName(), option.getId()+""));
		}
	}
	
	private ComboBox<NameValueModelData> newComboBox(String label) {
		ComboBox<NameValueModelData> box = new ComboBox<NameValueModelData>();
		box.setEditable(false);
		box.setForceSelection(true);
		box.setTriggerAction(TriggerAction.ALL);
		box.setStore(new ListStore<NameValueModelData>());
		box.setFieldLabel(label);
		
		return box;
	}
	
	private void doSubmit() {
		BatchUpdateEvent event = new BatchUpdateEvent();
		
		NameValueModelData selStatus = status.getValue();
		if (selStatus != null)
			event.setStatus(selStatus.getValue());
		
		NameValueModelData selGoal = goal.getValue();
		if (selGoal != null)
			event.setTargetGoal(Integer.valueOf(selGoal.getValue()));
		
		NameValueModelData selApproved = approved.getValue();
		if (selApproved != null)
			event.setTargetApproved(Integer.valueOf(selApproved.getValue()));
		
		if (notes.getValue() != null && !"".equals(notes.getValue()))
			event.setNotes(notes.getValue());
		
		doCollapse();
		
		reset();
		
		fireEvent(event.getType(), event);
	}

	private void cancel() {
		doCollapse();
	}
	
	private void doCollapse() {
		fireEvent(Events.CancelEdit);
	}
	
	private static class NameValueModelData extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public NameValueModelData(String name, String value) {
			super();
			set("text", name);
			set("value", value);
		}
		
		public String getValue() {
			return get("value");
		}
		
	}
	
	public static class BatchUpdateEvent extends BaseEvent {
		
		private String status, notes;
		private Integer targetGoal, targetApproved;
		
		public BatchUpdateEvent() {
			super(Events.StartEdit);
		}
		
		public void setTargetGoal(Integer targetGoal) {
			this.targetGoal = targetGoal;
		}
		
		public void setTargetApproved(Integer targetApproved) {
			this.targetApproved = targetApproved;
		}
		
		public void setStatus(String status) {
			this.status = status;
		}
		
		public void setNotes(String notes) {
			this.notes = notes;
		}
		
		public String getStatus() {
			return status;
		}
		
		public Integer getTargetApproved() {
			return targetApproved;
		}
		
		public Integer getTargetGoal() {
			return targetGoal;
		}
		
		public String getNotes() {
			return notes;
		}
		
	}
	
}
