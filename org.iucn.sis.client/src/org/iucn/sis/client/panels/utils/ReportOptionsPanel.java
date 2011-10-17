package org.iucn.sis.client.panels.utils;

import org.iucn.sis.client.api.utils.UriBase;
/*
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.solertium.util.extjs.client.WindowUtils;
*/
/**
 * 
 * @author rasanka.jayawardana@iucn.org
 *
 */
public class ReportOptionsPanel{
	
	public void loadAssessmentReport(int assessmentID){
		
		String target = "/reports/redlist/";
		com.google.gwt.user.client.Window.open(UriBase.getInstance().getReportBase()+ target + assessmentID
				+ "?special=true" ,
				"_blank", "");		
		/*
		 * TODO: Have to clarify from the RedList Unit about what should they want additionally in the report
		 * 
		 *
		final CheckBox specialCase = new CheckBox();
		//specialCase.setValue(Boolean.valueOf(true));
		specialCase.setFieldLabel("Show Color-coded Special Case Options");
		
		final FormPanel form = new FormPanel();
		form.setLabelSeparator("?");
		form.setLabelWidth(300);
		form.setFieldWidth(50);
		form.setHeaderVisible(false);
		form.setBorders(false);
		form.add(specialCase);
		
		final Window w = WindowUtils.newWindow("Report Options", null, false, true);
		
		form.addButton(new Button("Submit", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {				
				w.hide();
				
				com.google.gwt.user.client.Window.open(UriBase.getInstance().getReportBase()+ target + assessmentID
						+ "?special=" + specialCase.getValue() ,
						"_blank", "");
			}
		}));
		form.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				w.hide();
			}
		}));
		
		w.add(form);
		w.setSize(400, 250);
		w.show();
		w.center();
		*/
		
	}
}