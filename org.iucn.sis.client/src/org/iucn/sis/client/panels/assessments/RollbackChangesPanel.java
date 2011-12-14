package org.iucn.sis.client.panels.assessments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils.MergeMode;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.container.StateChangeEvent;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.models.NameValueModelData;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.io.AssessmentChangePacket;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class RollbackChangesPanel extends BasicWindow {
	
	private final Assessment assessment;
	
	public RollbackChangesPanel(final Assessment assessment, final Map<String, AssessmentChange> changes) {
		super("Rollback Changes", "icon-changes");
		this.assessment = assessment;
		
		setLayout(new FillLayout());
		setSize(400, 400);
		
		final Html warning = new Html("<b>Are you sure you want to rollback to this change?</b><br/><br/>The " +
			"changes checked below will be applied:");
		
		ListStore<NameValueModelData> store = new ListStore<NameValueModelData>();
		for (AssessmentChange change : changes.values()) {
			Field fauxField = new Field(change.getFieldName(), null);
			if (!fauxField.isClassificationScheme())
				store.add(new NameValueModelData(getDisplayText(change), change.getFieldName()));
		}
		
		final CheckBoxListView<NameValueModelData> view = new CheckBoxListView<NameValueModelData>();
		view.setStore(store);
		for (NameValueModelData m : store.getModels())
			view.setChecked(m, true);

		final LayoutContainer wrapper = new LayoutContainer(new FillLayout());
		wrapper.setScrollMode(Scroll.AUTO);
		wrapper.add(view);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		
		int size = 50;
		final BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, size, size, size);
		north.setSplit(false);
		
		container.add(warning, north);
		container.add(wrapper, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
		
		addButton(new Button("Rollback", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
				
				List<AssessmentChange> list = new ArrayList<AssessmentChange>();
				for (NameValueModelData model : view.getChecked())
					list.add(changes.get(model.getValue()));
			
				if (list.isEmpty())
					WindowUtils.errorAlert("Cancelled", "Rollback request cancelled since no fields were selected.");
				else
					rollback(list);	
			}
		}));
	}
	
	private void rollback(List<AssessmentChange> changes) {
		WindowUtils.showLoadingAlert("Please wait...");
		
		AssessmentChangePacket packet = new AssessmentChangePacket(assessment.getId());
		
		for (AssessmentChange change : changes) {
			Field existing = assessment.getField(change.getFieldName());
			if (existing == null) {
				//If add or edit, perform an add... otherwise, nothing to do
				if (change.getType() == AssessmentChange.EDIT || 
						change.getType() == AssessmentChange.DELETE) {
					Field field = change.getOldField().deepCopy(false, null);
					field.setName(change.getFieldName());
					field.setAssessment(assessment);
					
					packet.addAddition(field);
				}
			}
			else {
				if (change.getType() == AssessmentChange.EDIT || 
						change.getType() == AssessmentChange.DELETE) {
					Field field = change.getOldField().deepCopy(false, null);
					field.setId(existing.getId());
					field.setName(change.getFieldName());
					field.setAssessment(assessment);
					field.setNotes(new HashSet<Notes>(existing.getNotes()));
					field.setReference(new HashSet<Reference>(existing.getReference()));
					
					packet.addChange(field);
				}
				else if (change.getType() == AssessmentChange.ADD) {
					//Undo makes this a deletion
					Field field = new Field(existing.getName(), assessment);
					field.setId(existing.getId());
					
					packet.addChange(field);
				}
			}
		}
		
		try {
			AssessmentClientSaveUtils.saveAssessment(packet, assessment, MergeMode.Replace, new GenericCallback<AssessmentChangePacket>() {
				public void onSuccess(AssessmentChangePacket result) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.infoAlert("Changes rolled back successfully.");
					
					StateChangeEvent event = new StateChangeEvent(
						StateManager.impl.getWorkingSet(), StateManager.impl.getTaxon(), 
						AssessmentCache.impl.getAssessment(assessment.getId()), null);
					
					StateManager.impl.reset(event);
				}
				public void onFailure(Throwable caught) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Could not rollback changes, please try again later.");
				}
			});
		} catch (InsufficientRightsException e) {
			WindowUtils.hideLoadingAlert();
			WindowUtils.errorAlert("Sorry, you do not have permission to perform this action.");
		}
	}
	
	private String getDisplayText(AssessmentChange change) {
		String text;
		switch (change.getType()) {
			case AssessmentChange.ADD:
				text = "Remove field "; break;
			case AssessmentChange.EDIT:
				text = "Edit field "; break;
			case AssessmentChange.DELETE:
				text = "Restore field "; break;
			default:
				text = "Edit field ";
		}
		return text + change.getFieldName(); 
	}

}
