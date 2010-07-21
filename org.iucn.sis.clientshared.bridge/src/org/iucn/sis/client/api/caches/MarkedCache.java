package org.iucn.sis.client.api.caches;

import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.AssessmentType;

import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class MarkedCache {

	public static final String GREEN = "green-style";
	public static final String RED = "red-style";
	public static final String BLUE = "blue-style";
	public static final String NONE = "regular-style";

	public static final MarkedCache impl = new MarkedCache();

	/*
	 * goes from id -> style
	 */
	private HashMap markedWorkingSets = null;
	private HashMap markedTaxa = null;
	private HashMap markedAssessments = null;

	private MarkedCache() {
		markedWorkingSets = new HashMap();
		markedTaxa = new HashMap();
		markedAssessments = new HashMap();
	}

	public String getAssessmentStyle(String id) {
		String style = NONE;
		if (markedAssessments.containsKey(id)) {
			style = (String) markedAssessments.get(id);
		}
		return style;
	}

	public String getTaxaStyle(String id) {
		String style = NONE;
		if (markedTaxa.containsKey(id)) {
			style = (String) markedTaxa.get(id);
		}
		return style;
	}

	public String getWorkingSetStyle(String id) {
		String style = NONE;
		if (markedWorkingSets.containsKey(id)) {
			style = (String) markedWorkingSets.get(id);
		}
		return style;
	}

	public void markAssement(String id, String style) {
		if (style.equalsIgnoreCase(GREEN) || style.equalsIgnoreCase(RED) || style.equalsIgnoreCase(BLUE)) {
			markedAssessments.put(id, style);
			save();
		} else {
			unmarkAssessment(id);
		}
	}

	public void markTaxa(String id, String style) {
		if (style.equalsIgnoreCase(GREEN) || style.equalsIgnoreCase(RED) || style.equalsIgnoreCase(BLUE)) {
			markedTaxa.put(id, style);
			save();
		} else {
			unmarkTaxon(id);
		}
	}

	public void markWorkingSet(String id, String style) {
		if (style.equalsIgnoreCase(GREEN) || style.equalsIgnoreCase(RED) || style.equalsIgnoreCase(BLUE)) {
			markedWorkingSets.put(id, style);
			save();
		} else {
			unmarkWorkingSet(id);
		}

	}

	public void onLogout() {
		markedAssessments.clear();
		markedTaxa.clear();
		markedWorkingSets.clear();
	}

	private void parseXML(NativeDocument doc) throws NullPointerException {
		NativeElement docElement = doc.getDocumentElement();
		NativeElement taxa = docElement.getElementByTagName("taxa");
		NativeElement workingset = docElement.getElementByTagName("workingSets");
		NativeElement assessments = docElement.getElementByTagName("assessments");
		NativeElement draft = assessments.getElementByTagName("draft");
		NativeElement user = assessments.getElementByTagName("user");
		NativeElement published = assessments.getElementByTagName("published");

		// GET WORKINGSETS
		NativeNodeList list = workingset.getElementsByTagName("workingSet");
		for (int i = 0; i < list.getLength(); i++) {
			String[] info = list.item(i).getTextContent().split(",");
			markedWorkingSets.put(info[0], info[1]);
		}

		// GET Taxa
		list = taxa.getElementsByTagName("taxon");
		for (int i = 0; i < list.getLength(); i++) {
			String[] info = list.item(i).getTextContent().split(",");
			markedTaxa.put(info[0], info[1]);
		}

		// GET Assessments
		list = draft.getElementsByTagName("assessment");
		for (int i = 0; i < list.getLength(); i++) {
			SysDebugger.getInstance().println("This is draft content " + list.item(i).getTextContent());
			String[] info = list.item(i).getTextContent().split(",");
			markedAssessments.put(info[0] + "!" + AssessmentType.DRAFT_ASSESSMENT_TYPE, info[1]);
		}

		list = published.getElementsByTagName("assessment");
		for (int i = 0; i < list.getLength(); i++) {
			String[] info = list.item(i).getTextContent().split(",");
			markedAssessments.put(info[0] + "!" + AssessmentType.PUBLISHED_ASSESSMENT_TYPE, info[1]);
		}

		list = user.getElementsByTagName("assessment");
		for (int i = 0; i < list.getLength(); i++) {
			String[] info = list.item(i).getTextContent().split(",");
			markedAssessments.put(info[0] + "!" + AssessmentType.USER_ASSESSMENT_TYPE, info[1]);

		}
	}

	private void save() {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.put(UriBase.getInstance().getTagBase() +"/mark/" + SISClientBase.currentUser.getUsername(), toXML(), new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				update();
			}

			public void onSuccess(String arg0) {
			}

		});
	}

	private String toXML() {

		String workingsets = "";
		Iterator iter = markedWorkingSets.keySet().iterator();
		while (iter.hasNext()) {
			String id = (String) iter.next();
			workingsets += "<workingSet>" + id + "," + markedWorkingSets.get(id) + "</workingSet>\r\n";
		}

		String taxa = "";
		iter = markedTaxa.keySet().iterator();
		while (iter.hasNext()) {
			String id = (String) iter.next();
			taxa += "<taxon>" + id + "," + markedTaxa.get(id) + "</taxon>\r\n";
		}

		String published = "";
		String drafts = "";
		String user = "";
		iter = markedAssessments.keySet().iterator();
		while (iter.hasNext()) {
			String allid = (String) iter.next();
			String id = allid.substring(0, allid.indexOf("!"));
			if (allid.endsWith(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
				drafts += "<assessment>" + id + "," + markedAssessments.get(allid) + "</assessment>\r\n";
			} else if (allid.endsWith(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
				published += "<assessment>" + id + "," + markedAssessments.get(allid) + "</assessment>\r\n";
			} else if (allid.endsWith(AssessmentType.USER_ASSESSMENT_TYPE)) {
				user += "<assessment>" + id + "," + markedAssessments.get(allid) + "</assessment>\r\n";
			}
		}

		String xml = "<marked>\r\n";
		xml += "<workingSets>" + workingsets + "</workingSets>\r\n";
		xml += "<taxa>" + taxa + "</taxa>\r\n";
		xml += "<assessments>\r\n";
		xml += "<published>" + published + "</published>\r\n";
		xml += "<draft>" + drafts + "</draft>\r\n";
		xml += "<user>" + user + "</user>\r\n";
		xml += "</assessments>\r\n";
		xml += "</marked>";
		return xml;
	}

	public void unmarkAssessment(String id) {
		SysDebugger.getInstance().println("I am trying to unmark " + id);
		if (markedAssessments.containsKey(id)) {
			SysDebugger.getInstance().println("The id was in there");
			markedAssessments.remove(id);
			save();
		}

	}

	public void unmarkTaxon(String id) {
		SysDebugger.getInstance().println("I am trying to unmark " + id);
		if (markedTaxa.containsKey(id)) {
			SysDebugger.getInstance().println("The id was in there");
			markedTaxa.remove(id);
			save();
		}
	}

	public void unmarkWorkingSet(String id) {
		SysDebugger.getInstance().println("I am trying to unmark " + id);
		if (markedWorkingSets.containsKey(id)) {
			SysDebugger.getInstance().println("The id was in there");
			markedWorkingSets.remove(id);
			save();
		}
	}

	/**
	 * Gets the last save version of marked.xml
	 */
	public void update() {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getTagBase() + "/mark/" + SISClientBase.currentUser.getUsername(), new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				markedAssessments.clear();
				markedTaxa.clear();
				markedWorkingSets.clear();
			}

			public void onSuccess(String arg0) {
				try {
					parseXML(ndoc);
				} catch (Exception e) {
					// update();
				}
			}

		});
	}

}
