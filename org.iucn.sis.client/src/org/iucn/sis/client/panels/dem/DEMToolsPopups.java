package org.iucn.sis.client.panels.dem;

import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Notes;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;

public class DEMToolsPopups {
	

	public static void buildNotePopup() {
		Assessment curAss = AssessmentCache.impl.getCurrentAssessment();

		if (curAss == null)
			return;

		Window s = WindowUtils.getWindow(false, false, "Notes for " + curAss.getSpeciesName());
		final LayoutContainer container = s;

		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		String type = AssessmentCache.impl.getCurrentAssessment().getType();
		String url = UriBase.getInstance().getNotesBase() + "/notes/" + type;
		url += "/" + AssessmentCache.impl.getCurrentAssessment().getId();

		if (type.equals(AssessmentType.USER_ASSESSMENT_TYPE))
			url += "/" + SimpleSISClient.currentUser.getUsername();

		doc.get(url, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				container.add(new HTML("There are no notes for this assessment."));
				container.layout();
			}

			public void onSuccess(String result) {
				List<Notes> notes = Notes.notesFromXML(doc.getDocumentElement());
				if (notes == null || notes.size() == 0)
					container.add(new HTML("There are no notes for this assessment."));
				else {
					// VerticalPanel panel = new VerticalPanel();
					// panel.setSpacing( 3 );

					for (Iterator iter = notes.listIterator(); iter.hasNext();) {
						Notes current = (Notes) iter.next();
						container.add(new HTML("<div style='padding-top:10px'> <span style='font-weight:bold'>"
								+ current.getEdit().getUser().getUsername() + "</span> " + current.getEdit().getCreatedDate().toString() + "</div>" + "<div> ["
								+ current.getField().getName() + "] " + current.getValue() + "</div>"));
					}
					// container.add( panel );
				}
				container.layout();
			}
		});
		s.setScrollMode(Scroll.AUTO);
		s.setSize(400, 500);

		s.show();
		s.center();
	}

	
}
