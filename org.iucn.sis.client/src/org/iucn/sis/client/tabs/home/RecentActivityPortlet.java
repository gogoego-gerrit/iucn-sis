package org.iucn.sis.client.tabs.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.FetchMode;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.models.NameValueModelData;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.panels.utils.RefreshPortlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

public class RecentActivityPortlet extends RefreshPortlet {
	
	private static enum ActivityMode {
		ALL("All", "all"), WORKING_SETS("My Working Sets", "ws"), MINE("Mine", "mine");
		public static ActivityMode get(String key) {
			for (ActivityMode mode : ActivityMode.values())
				if (mode.getKey().equals(key))
					return mode;
			return null;
		}
		private final String name, key;
		private ActivityMode(String name, String key) { this.name = name; this.key = key; }
		public String getName() { return name; }
		public String getKey() { return key; }
	}
	
	private static ActivityMode DEFAULT = ActivityMode.ALL;
	
	private List<Activity> activities;
	private ActivityMode mode;
	
	public RecentActivityPortlet() {
		super("x-panel");
		setHeading("Recent Activity");
		setLayout(new FlowLayout());
		setLayoutOnChange(true);
		setHeight(200);
		setScrollMode(Scroll.AUTOY);
		
		configureThisAsPortlet();
		
		//TODO: make this configurable via drop-down? tabs?
		setMode(DEFAULT);
	}
	
	@Override
	protected void initTools() {
		NameValueModelData[] options = new NameValueModelData[ActivityMode.values().length];
		int index = 0;
		for (ActivityMode option : ActivityMode.values())
			options[index++] = new NameValueModelData(option.getName(), option.getKey());
		
		ComboBox<NameValueModelData> box = FormBuilder.createModelComboBox("mode", DEFAULT.getKey(), "", false, options);
		box.addSelectionChangedListener(new SelectionChangedListener<NameValueModelData>() {
			public void selectionChanged(SelectionChangedEvent<NameValueModelData> se) {
				NameValueModelData selection = se.getSelectedItem();
				if (selection != null)
					setMode(ActivityMode.get(selection.getValue()));
			}
		});
		head.addTool(box);
		
		super.initTools();
	}
	
	private void setMode(ActivityMode mode) {
		if (mode != null) {
			this.mode = mode;
			
			refresh();
		}
	}
	
	private void update() {
		removeAll();
		
		if (activities.isEmpty())
			add(new HTML("No recent activity yet."));
		else {
			final VerticalPanel panel = new VerticalPanel();
			panel.setSpacing(3);
			panel.setWidth("100%");
			
			for (final Activity activity : activities) {
				HTML link;
				panel.add(link = new HTML("<b>" + activity.taxon + "</b>: " + activity.reason));
				panel.add(new HTML("-- " + FormattedDate.FULL.getDate(activity.date) + " by " + activity.user));
				
				link.addStyleName("SIS_HyperlinkBehavior");
				link.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						open(activity);
					}
				});
			}
			
			add(panel);
		}
	}
	
	private void open(Activity activity) {
		WindowUtils.showLoadingAlert("Loading...");
		AssessmentCache.impl.fetchAssessment(activity.assessmentid, FetchMode.FULL, new GenericCallback<Assessment>() {
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
			}
			public void onSuccess(final Assessment assessment) {
				TaxonomyCache.impl.fetchTaxon(assessment.getSpeciesID(), new GenericCallback<Taxon>() {
					@Override
					public void onFailure(Throwable caught) {
						WindowUtils.hideLoadingAlert();
						WindowUtils.errorAlert("Could not load assessment, please try again later.");
					}
					public void onSuccess(Taxon result) {
						WindowUtils.hideLoadingAlert();
						StateManager.impl.setState(null, result, assessment);
					}
				});
			}
		});
	}
	
	@Override
	public void refresh() {
		mask("Loading...");
		
		final StringBuilder uri = new StringBuilder();
		uri.append(UriBase.getInstance().getRecentAssessmentsBase());
		uri.append("/activity/");
		uri.append(mode.getKey());
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(uri.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				activities = new ArrayList<Activity>();
				
				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("activity");
				for (int i = 0; i < nodes.getLength(); i++) {
					NativeElement el = nodes.elementAt(i);
					
					Activity activity = new Activity();
					activity.taxonid = Integer.valueOf(el.getAttribute("taxonid"));
					activity.assessmentid = Integer.valueOf(el.getAttribute("assessmentid"));
					
					NativeNodeList children = el.getChildNodes();
					for (int k = 0; k < children.getLength(); k++) {
						NativeNode child = children.item(k);
						if ("user".equals(child.getNodeName()))
							activity.user = child.getTextContent();
						else if ("date".equals(child.getNodeName()))
							activity.date = new Date(Long.valueOf(child.getTextContent()));
						else if ("reason".equals(child.getNodeName()))
							activity.reason = child.getTextContent().replaceFirst("Assessment Changes:", "").trim();
						else if ("taxon".equals(child.getNodeName()))
							activity.taxon = child.getTextContent();
					}
					
					activities.add(activity);
				}
				
				update();
				
				unmask();
			}
			public void onFailure(Throwable caught) {
				activities = new ArrayList<Activity>();
				
				update();
				
				unmask();
			}
		});
	}
	
	private static class Activity  {
		
		@SuppressWarnings("unused")
		private int taxonid, assessmentid;
		private String user, reason, taxon;
		private Date date;
		
	}

}
