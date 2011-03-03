package org.iucn.sis.client.panels.assessments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class TrackChangesPanel extends Window implements DrawsLazily {
	
	private final Assessment assessment;
	private Edit edit;
	
	private final LayoutContainer oldField, newField;
	private final FieldWidgetCache cache;
	
	public TrackChangesPanel(Assessment assessment) {
		this(assessment, null);
	}

	public TrackChangesPanel(Assessment assessment, Edit edit) {
		super();
		setSize(800, 650);
		setLayout(new FillLayout());
		setIconStyle("icon-changes");
		setHeading("View Assessment Changes");
		
		this.assessment = assessment;
		this.edit = edit;
		
		oldField = new LayoutContainer();
		newField = new LayoutContainer();
		
		cache = FieldWidgetCache.newInstance();
	}
	
	@Override
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
				if (edit != null)
					view(edit);
			}
		});
	}
	
	public void open() {
		super.show();
	}
	
	@Override
	public void draw(DoneDrawingCallback callback) {
		final ContentPanel oldFieldPanel = new ContentPanel();
		oldFieldPanel.setHeading("Old Version");
		oldFieldPanel.setCollapsible(true);
		oldFieldPanel.add(oldField);
		
		final ContentPanel newFieldPanel = new ContentPanel();
		newFieldPanel.setHeading("New Version");
		newFieldPanel.setCollapsible(true);
		newFieldPanel.add(newField);
		
		final LayoutContainer center = new LayoutContainer(new BorderLayout());
		center.add(oldFieldPanel, new BorderLayoutData(LayoutRegion.NORTH, 300));
		center.add(newFieldPanel, new BorderLayoutData(LayoutRegion.CENTER, 300));
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(drawLeft(), new BorderLayoutData(LayoutRegion.WEST, 200, 200, 200));
		container.add(center, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
		
		callback.isDrawn();
	}

	/*
	 * TODO: maybe pull this live from the server
	 * via /changes/assessments/{id}
	 */
	private LayoutContainer drawLeft() {
		List<Edit> edits = new ArrayList<Edit>(assessment.getEdit());
		Collections.sort(edits, new Comparator<Edit>() {
			public int compare(Edit o1, Edit o2) {
				return o2.getCreatedDate().compareTo(o1.getCreatedDate());
			}
		});
		final LayoutContainer container = new LayoutContainer();
		container.setScrollMode(Scroll.AUTOY);
		for (final Edit edit : edits) {
			final LayoutContainer info = new LayoutContainer();
			info.add(new Html(edit.getUser().getDisplayableName()));
			info.add(new Html(FormattedDate.FULL.getDate(edit.getCreatedDate())));
			info.add(new Button("View", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					view(edit);
				}
			}));
			container.add(info);
		}
		
		return container;
	}
	
	private void view(Edit edit) {
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getSISBase() + "/changes/assessments/" + assessment.getId() + "/edit/" + edit.getId(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final Map<String, AssessmentChange> changes = new LinkedHashMap<String, AssessmentChange>();
				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("change");
				for (int i = 0; i < nodes.getLength(); i++) {
					AssessmentChange change = AssessmentChange.fromXML(nodes.elementAt(i));
					changes.put(change.getFieldName(), change);
				}
			
				showChanges(changes);
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load this change, please try again later.");
			}
		});		
	}
	
	private void showChanges(final Map<String, AssessmentChange> changes) {
		cache.prefetchList(changes.keySet(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				oldField.removeAll();
				newField.removeAll();
				for (AssessmentChange change : changes.values()) {
					showChange(change.getOldField(), change.getFieldName(), oldField);
					showChange(change.getNewField(), change.getFieldName(), newField);
				}
				oldField.layout();
				newField.layout();
			}
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void showChange(Field field, String fieldName, LayoutContainer target) {
		final Display display = cache.get(fieldName);
		if (display == null)
			return;
		display.setData(field);
		
		target.add(display.showViewOnly());
	}
	
}
