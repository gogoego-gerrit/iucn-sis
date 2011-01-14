package org.iucn.sis.client.api.caches;

import java.util.ArrayList;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.findreplace.FindReplaceData;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class FindResultCache {

	public static final FindResultCache impl = new FindResultCache();

	public static final int MAXNUMBEROFRESULTS = 500;

	public static final int ALLTAXON = 1;
	public static final int DRAFTASSESSMENT = 2;
	public static final int PUBLISHEDASSESSMENT = 4;
	public static final int TAXON = 8;
	public static final int CURRENTTAXON = 16;
	public static final int ALLDRAFTASSESSMENTS = 32;
	public static final int ALLPUBLISHEDASSESSMENTS = 64;
	public static final int ALLASSESSMENTS = 128;
	public static final int ASSESSMENT = 256;
	public static final int PUBLISHEDASSESSMENTS_FOR_TAXA = 512;

	/**
	 * an arrayList that holds findResultData objects
	 */
	private ArrayList<FindReplaceData> findResults;
	public FindReplaceData currentFindResultData;
	/**
	 * The search ID returned from the restlet. Reset to null once done
	 * searching the particular query.
	 */
	private String searchID;
	private boolean lastDeleted;

	private FindResultCache() {
		findResults = new ArrayList<FindReplaceData>();
		searchID = null;
		currentFindResultData = null;
		lastDeleted = false;
	}

	private boolean calculateDoneFinding(Object arg0) {
		try {

			if (((String) (arg0)).trim().equalsIgnoreCase("202")) {
				return false;
			} else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private void clearPreviousResults() {
		findResults.clear();
		currentFindResultData = null;
	}

	@SuppressWarnings("unused")
	private String createXML(ArrayList<FindReplaceData> findResultDataObjects) {
		String xml = "<xml>\r\n";
		for (int i = 0; i < findResultDataObjects.size(); i++) {
			xml += (findResultDataObjects.get(i)).toXML();
		}
		xml += "</xml>";
		return xml;
	}

	private String createXML(FindReplaceData data, String options, String criteria) {
		String xml = "<xml>\r\n";
		xml += "<options>" + options + "</options>\r\n";
		xml += "<field>" + criteria + "</field>\r\n";
		xml += data.toXML();
		xml += "</xml>";
		return xml;
	}

	public void deleteSearch(final GenericCallback<String> wayback) {
		if (searchID != null) {
			GenericCallback<String> callback = new GenericCallback<String>() {

				public void onFailure(Throwable caught) {
					searchID = null;
					wayback.onFailure(caught);
				}

				public void onSuccess(String arg0) {
					searchID = null;
					wayback.onSuccess(arg0);
				}

			};
			NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			lastDeleted = true;
			ndoc.delete(UriBase.getInstance().getFindReplaceBase() + "/find/kill/" + searchID, callback);

		} else {
			wayback.onFailure(new Throwable("Can't delete null"));
		}
	}

	public boolean doneFinding() {
		return (searchID == null);
	}

	public boolean doneWithCurrent() {
		boolean done = true;
		if (currentFindResultData != null && currentFindResultData.getCurrentSentence() != null) {
			done = false;
		}
		return done;
	}

	public void find(String text, final String options, final String criteria, final String workingSetID, final AssessmentFilter filter,
			final GenericCallback<String> wayBack) {

		String searchid = searchID;

		// NEW SEARCH, OTHERWISE CONTINUING SEARCH
		if (searchid == null) {
			clearPreviousResults();
		}

		// CANCEL THE SEARCH
		if (reachedMax()) {
			deleteSearch(wayBack);
		}

		else {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			final String xml = "<xml>\n<options>" + options + "</options>\n" + "<field>" + criteria
					+ "</field>\n" + "<text>" + XMLUtils.clean(text) + "</text>\n" + "<workingSetID>" + workingSetID
					+ "</workingSetID>\n" + filter.toXML() + "</xml>";
			lastDeleted = false;

			GenericCallback<String> callback = new GenericCallback<String>() {

				public void onFailure(Throwable arg0) {
					searchID = null;
					if (!lastDeleted) {
						clearPreviousResults();
					}
					wayBack.onFailure(arg0);
				}

				public void onSuccess(String arg0) {
					try {
						if (!startSearching(arg0)) {
							parseResults(ndoc, arg0);
							wayBack.onSuccess(arg0);
						} else {
							searchID = parseStartSearching(ndoc);
							if (searchID == null) {
								wayBack.onFailure(new Throwable());
							} else {
								find(xml, options, criteria, workingSetID, filter, wayBack);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						onFailure(e.getCause());
					}
				}
			};
			ndoc.post(UriBase.getInstance().getFindReplaceBase() + "/find/" + searchid, xml, callback);

//			if (type == TAXON) {
//				ndoc.post("/find/taxa/" + searchid, xml, callback);
//			} else if (type == DRAFTASSESSMENT) {
//				ndoc.post("/find/draft/assessments/" + searchid, xml, callback);
//			} else if (type == PUBLISHEDASSESSMENT) {
//				ndoc.post("/find/published/assessments/" + searchid, xml, callback);
//			} else if (type == PUBLISHEDASSESSMENTS_FOR_TAXA) {
//				ndoc.post("/find/publishedForTaxa/assessments/" + searchid, xml, callback);
//			} else if (type == ALLTAXON) {
//				ndoc.post("/findAll/taxa/" + searchid, xml, callback);
//			} else if (type == ALLDRAFTASSESSMENTS) {
//				ndoc.post("/findAll/draft/assessments/" + searchid, xml, callback);
//			} else if (type == ALLPUBLISHEDASSESSMENTS) {
//				ndoc.post("/findAll/published/assessments/" + searchid, xml, callback);
//			} else if (type == ALLASSESSMENTS) {
//				ndoc.post("/findRelated/taxa/" + searchid, xml, callback);
//			}
		}
	}

	public FindReplaceData getCurrentFindResultData() {
		return currentFindResultData;
	}

	public FindReplaceData getFindResultData(int index) {
		if (index < findResults.size()) {
			return findResults.get(index);
		} else
			return null;
	}

	public ArrayList<FindReplaceData> getFindResults() {
		return findResults;
	}

	/**
	 * returns whether or not the currentFindDataResult needs to be sent to the
	 * server for replacement
	 * 
	 * @return
	 */
	public boolean needsReplacement() {
		if (currentFindResultData != null) {
			return currentFindResultData.needsReplacing();
		} else {
			return false;
		}
	}

	private void parseResults(NativeDocument doc, Object arg0) {
		try{
		NativeElement docElement = doc.getDocumentElement();
		
		NativeNodeList list = docElement.getElementsByTagName("result");
		for (int i = 0; i < list.getLength() && !reachedMax(); i++) {
			FindReplaceData data = FindReplaceData.fromXML((NativeElement) list.item(i));
			findResults.add(data);
		}

		if (calculateDoneFinding(arg0)) {
			searchID = null;
		} else {
			searchID = docElement.getAttribute("searchid");
		}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private String parseStartSearching(NativeDocument doc) {
		try {
			return doc.getDocumentElement().getText();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean reachedMax() {

		return findResults.size() >= MAXNUMBEROFRESULTS;

	}

	public void removeData(FindReplaceData data) {
		findResults.remove(data);
	}

	/**
	 * sends currentFINDDATARESULT object to the server ... WARNING >>> WILL
	 * SEND IT NO MATTER WHAT, EVEN IF THINGS DON'T NEED REPLACEMENT
	 */
	public void replace(final FindReplaceData sendingObject, final GenericCallback<String> wayback, String options,
			String criteria) {

		if (sendingObject != null) {

			findResults.remove(sendingObject);
			NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			GenericCallback<String> callback = new GenericCallback<String>() {

				public void onFailure(Throwable arg0) {
					findResults.add(sendingObject);
					wayback.onFailure(new Throwable(findResults.indexOf(sendingObject) + ""));
				}

				public void onSuccess(String arg0) {
					wayback.onSuccess(arg0);
				}

			};
			ndoc.put(UriBase.getInstance().getFindReplaceBase() +"/replace", createXML(sendingObject, options, criteria), callback);
		} else {
			wayback.onFailure(new Throwable("Not sending null object"));
		}
	}

	public void replaceAll(String replacementString, GenericCallback<String> callback) {
	}

	public void replaceAllSentences(String replacementString) {
		while (currentFindResultData != null && currentFindResultData.getCurrentSentence() != null) {
			currentFindResultData.replaceCurrentSentence(replacementString);
		}
	}

	public void replaceCurrentSentence(String replacementString) {
		if (currentFindResultData != null && currentFindResultData.getCurrentSentence() != null) {
			currentFindResultData.replaceCurrentSentence(replacementString);
		}

	}

	public void setCurrentFindResult(FindReplaceData data) {
		currentFindResultData = data;
	}

	public void skipAllSentences() {
		while (currentFindResultData != null && currentFindResultData.getCurrentSentence() != null) {
			currentFindResultData.skipCurrentSentence();
		}
	}

	public void skipCurrentSentence() {

		if (currentFindResultData != null && currentFindResultData.getCurrentSentence() != null) {
			currentFindResultData.skipCurrentSentence();
		}

	}

	private boolean startSearching(Object arg0) {
		try {

			if (((String) (arg0)).trim().equalsIgnoreCase("201")) {
				return true;
			} else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

}
