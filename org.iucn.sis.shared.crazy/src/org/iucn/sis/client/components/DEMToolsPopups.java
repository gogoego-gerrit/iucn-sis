package org.iucn.sis.client.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.Note;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.extjs.client.WindowUtils;

public class DEMToolsPopups {
	public static void buildBibliographyPopup() {
		AssessmentData curAss = AssessmentCache.impl.getCurrentAssessment();

		if (curAss == null)
			return;

		Window s = WindowUtils.getWindow(false, false, "Bibliography for " + curAss.getSpeciesName());

		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(3);

		HashMap uniqueReferences = new HashMap();

		if (curAss.getReferences().keySet().size() == 0 || curAss.getReferences().values().size() == 0)
			panel.add(new HTML("There are no references for this assessment."));
		else {
			for (Iterator iter = curAss.getReferences().keySet().iterator(); iter.hasNext();) {
				String curField = (String) iter.next();
				ArrayList refs = curAss.getReferences(curField);

				if (refs != null) {
					for (Iterator refsIter = refs.listIterator(); refsIter.hasNext();) {
						ReferenceUI ref = (ReferenceUI) refsIter.next();

						if (!uniqueReferences.containsKey(ref.getReferenceID()))
							uniqueReferences.put(ref.getReferenceID(), ref);
					}
				}
			}

			Object[] objects = uniqueReferences.values().toArray();
			ReferenceUI[] uniqueRefs = new ReferenceUI[objects.length];

			for (int i = 0; i < objects.length; i++)
				uniqueRefs[i] = (ReferenceUI) objects[i];

			ArrayUtils.quicksort(uniqueRefs);

			for (int i = 0; i < uniqueRefs.length; i++) {
				panel.add(new HTML("<ul>"));
				panel.add(new HTML("<li>" + uniqueRefs[i].getReferenceBody() + "</li>"));
				panel.add(new HTML("</ul>"));
			}
		}

		s.add(panel);
		s.setScrollMode(Scroll.AUTO);
		s.setSize(400, 500);

		s.show();
		s.center();
	}

	public static void buildNotePopup() {
		AssessmentData curAss = AssessmentCache.impl.getCurrentAssessment();

		if (curAss == null)
			return;

		Window s = WindowUtils.getWindow(false, false, "Notes for " + curAss.getSpeciesName());
		final LayoutContainer container = s;

		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		String type = AssessmentCache.impl.getCurrentAssessment().getType();
		String url = "/notes/" + type;
		url += "/" + AssessmentCache.impl.getCurrentAssessment().getAssessmentID();

		if (type.equals(BaseAssessment.USER_ASSESSMENT_STATUS))
			url += "/" + SimpleSISClient.currentUser.username;

		doc.get(url, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				container.add(new HTML("There are no notes for this assessment."));
				container.layout();
			}

			public void onSuccess(String result) {
				ArrayList notes = Note.notesFromXML(doc.getDocumentElement());
				if (notes == null || notes.size() == 0)
					container.add(new HTML("There are no notes for this assessment."));
				else {
					// VerticalPanel panel = new VerticalPanel();
					// panel.setSpacing( 3 );

					for (Iterator iter = notes.listIterator(); iter.hasNext();) {
						Note current = (Note) iter.next();
						container.add(new HTML("<div style='padding-top:10px'> <span style='font-weight:bold'>"
								+ current.getUser() + "</span> " + current.getDate() + "</div>" + "<div> ["
								+ current.getCanonicalName() + "] " + current.getBody() + "</div>"));
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

	public static void buildReferencePopup() {
		AssessmentData curAss = AssessmentCache.impl.getCurrentAssessment();

		if (curAss == null)
			return;

		Window s = WindowUtils.getWindow(false, false, "Bibliography for " + curAss.getSpeciesName());
		LayoutContainer container = s;

		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(3);

		if (curAss.getReferences().keySet().size() == 0 || curAss.getReferences().values().size() == 0)
			panel.add(new HTML("There are no references for this assessment."));
		else {
			for (Iterator iter = curAss.getReferences().keySet().iterator(); iter.hasNext();) {
				String curField = (String) iter.next();
				ArrayList refs = curAss.getReferences(curField);

				if (refs != null && refs.size() > 0) {
					panel.add(new HTML("<u>References for field " + curField + "</u>"));

					panel.add(new HTML("<ul>"));
					for (Iterator refsIter = refs.listIterator(); refsIter.hasNext();)
						panel.add(new HTML("<li>" + ((ReferenceUI) refsIter.next()).getReferenceBody() + "</li>"));
					panel.add(new HTML("</ul>"));
				}

			}
		}

		s.add(panel);
		s.setScrollMode(Scroll.AUTO);
		s.setSize(400, 500);
		s.show();
		s.center();
	}
}
