package org.iucn.sis.client.panels.utils;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.models.NameValueModelData;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.UserPreferences;
import org.iucn.sis.shared.api.models.Assessment;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Window;
import com.solertium.util.extjs.client.FormBuilder;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * 
 * @author carl.scott
 * @author rasanka.jayawardana@iucn.org
 *
 */
public class ReportOptionsPanel extends BasicWindow {
	
	private final int assessmentID;
	private final String defaultReportType; 
	
	public ReportOptionsPanel(Assessment assessment) {
		this(assessment.getId());
	}
	
	public ReportOptionsPanel(int assessmentID) {
		super("Generate Assessment Report", "icon-report");
		this.assessmentID = assessmentID;
		this.defaultReportType = SISClientBase.currentUser.
			getPreference(UserPreferences.PREFERRED_REPORT, "redlist");
		
		setSize(400, 300);
		setButtonAlign(HorizontalAlignment.CENTER);
		
		final ComboBox<NameValueModelData> type = 
			FormBuilder.createModelComboBox("type", defaultReportType, "Report Type", true, 
			newModel("Red List Report", "redlist"), 
			newModel("All Fields Report", "full"), 
			newModel("Available Fields Report", "available"));
		
		final CheckBox empty = FormBuilder.createCheckBoxField("empty", true, "Show Empty Fields");
		final CheckBox limitedSet = FormBuilder.createCheckBoxField("limited", false, "Limited Set");
		
		final FieldSet set = new FieldSet();
		set.setLayout(new FormLayout());
		set.setHeading("Options");
		set.add(empty);
		set.add(limitedSet);
		
		final FormPanel form = new FormPanel();
		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.add(type);
		form.add(set);
		
		add(form);
		
		addButton(new Button("Generate", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (!form.isValid())
					WindowUtils.errorAlert("Please fill in required fields.");
				else
					open(type.getValue().getValue(), empty.getValue(), limitedSet.getValue());
			}
		}));
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	private void open(String type, boolean empty, boolean limited) {
		StringBuilder url = new StringBuilder();
		url.append(UriBase.getInstance().getReportBase());
		url.append("/reports/");
		url.append(type);
		url.append('/');
		url.append(assessmentID);
		url.append('?');
		url.append("empty=" + empty);
		url.append('&');
		url.append("limited=" + limited);
		
		hide();
		Window.open(url.toString(), "_blank", "");
		
		if (!type.equals(defaultReportType))
			SISClientBase.currentUser.setPreference(UserPreferences.PREFERRED_REPORT, type);
	}
	
	private NameValueModelData newModel(String text, String value) {
		return new NameValueModelData(text, value);
	}
	
}