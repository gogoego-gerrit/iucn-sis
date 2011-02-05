package org.iucn.sis.server.extensions.findreplace;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.Session;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.findreplace.FindReplaceData;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

/**
 * 
 */
public class FindReplaceRestlet extends BaseServiceRestlet {

	class MonitorThread extends Thread {

		Response response;
		Request request;
		String searchID;
		SearchThread searchThread;
		List<Integer> assessments;
		String textToLookFor;
		AtomicBoolean isSleeping;
		String options;
		String field;

		public MonitorThread(String searchID, List<Integer> assessments, String textToLookFor, String options,
				String field, Request request) {
			this.searchID = searchID;
			this.response = null;
			this.searchThread = null;
			this.assessments = assessments;
			this.request = request;
			this.textToLookFor = textToLookFor;
			this.isSleeping = new AtomicBoolean(false);
			this.options = options;
			this.field = field;
		}

		private void getSearchResults(boolean firstTime) {
			boolean sent = false;
			int counter = 0;
			int MAXLOOPS = 5;
			while (!sent) {
				counter++;
				try {
					isSleeping.set(true);
					sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}

				isSleeping.set(false);
				synchronized (searchThread) {
					searchThread.paused = true;
				}

				Vector<FindReplaceData> vector = searchThread.getAndClear();

				if (vector.size() > 0 || searchThread.done || ((counter >= MAXLOOPS) && firstTime)) {
					response.setEntity(toXMLSearchResults(vector, searchID), MediaType.TEXT_XML);
					if (searchThread.done) {
						response.setStatus(Status.SUCCESS_OK);
						currentSearches.remove(searchID);
					} else {
						response.setStatus(Status.SUCCESS_ACCEPTED);
					}

					sent = true;
				}
				synchronized (searchThread) {
					searchThread.paused = false;
					searchThread.notify();
				}
			}

		}

		public SearchThread getSearchThread() {
			return searchThread;
		}

		public void killdash9() {
			searchThread.killdash9();
		}

		public void resetResponse(Response response, boolean firstTime) {
			this.response = response;
			getSearchResults(firstTime);
		}

		@Override
		public void run() {
			this.searchThread = new SearchThread(assessments, searchID, textToLookFor, options, field, this);
			Thread searchThreaded = new Thread(searchThread);
			searchThreaded.start();

		}

	}

	class SearchThread implements Runnable {

		private String id;
		private Vector<FindReplaceData> resultsNotSent;
		private List<Integer> assessments;
		private String textToLookFor;
		private Date timeStarted;
		private MonitorThread monitorThread;
		public boolean paused;
		public boolean done;
		public AtomicBoolean kill;
		private FindReplace searcher;
		public String options;
		public String field;

		public SearchThread(List<Integer> assessments, String id, String textToLookFor, String options,
				String field, MonitorThread monitorThread) {
			this.id = id;
			resultsNotSent = new Vector<FindReplaceData>();
			this.assessments = assessments;
			this.textToLookFor = textToLookFor;
			this.paused = false;
			done = false;
			this.monitorThread = monitorThread;
			this.kill = new AtomicBoolean(false);
			this.searcher = new FindReplace();
			this.options = options;
			this.field = field;
		}

		private void determineIfRanTooLong() {
			long timeNow = new Date().getTime();
			long oldTime = timeStarted.getTime();
			if (timeNow - oldTime > MAXRUNTIME) {
				killdash9();
			}
		}

		public Vector<FindReplaceData> getAndClear() {
			synchronized (resultsNotSent) {

				Vector<FindReplaceData> returned = new Vector<FindReplaceData>(resultsNotSent);
				resultsNotSent.removeAllElements();
				return returned;
			}
		}

		public Date getDate() {
			return timeStarted;
		}

		public String getID() {
			return id;
		}

		public void killdash9() {
			kill.set(true);
		}

		public void run() {

			try {
				this.timeStarted = new Date();
				Session session = SISPersistentManager.instance().openSession();
				AssessmentIO io = new AssessmentIO(session);
				while ((assessments.size() > 0) && !kill.get()) {
					Assessment assessment = io.getAssessment(assessments.remove(0));
					FindReplaceData data;
					try {
						data = searcher.find(assessment, textToLookFor, options, field);
					} catch (FindReplaceException e1) {
						e1.printStackTrace();
						data = null;
					}
					if (data != null) {
						synchronized (this) {
							while (paused) {
								try {
									wait();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							synchronized (resultsNotSent) {
								resultsNotSent.add(data);
							}
						}
					}

					determineIfRanTooLong();
				}
				session.close();
				done = true;
				if (monitorThread.isSleeping.get()) {
					monitorThread.interrupt();
				}
			} catch (NullPointerException e) {
				done = true;
			} catch (PersistentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				done = true;
			}

		}

	}

	private final long MAXRUNTIME = 500000;

	private AtomicLong searchid;

	private ConcurrentHashMap<String, MonitorThread> currentSearches;

	private FindReplace replacer;

	public FindReplaceRestlet(Context context) {
		super(context);
		searchid = new AtomicLong(0);
		currentSearches = new ConcurrentHashMap<String, MonitorThread>();
		replacer = new FindReplace();
	}

	@Override
	public void definePaths() {
		paths.add("/find/{searchid}");
		paths.add("/replace");
		paths.add("/find/kill/{searchid}");
	}

	private List<Integer> getAssessmentsToSearch(NativeDocument doc, Session session) {
		WorkingSetIO workingSetIO = new WorkingSetIO(session);
		NativeElement element = doc.getDocumentElement();
		String workingSetId = element.getElementsByTagName("workingSetID").item(0).getTextContent();
		AssessmentFilter filter = AssessmentFilter.fromXML(element.getElementByTagName(
				AssessmentFilter.ROOT_TAG));
		AssessmentFilterHelper helper = new AssessmentFilterHelper(session, filter);
		List<Integer> assessments = new ArrayList<Integer>();
		for (Taxon taxon : workingSetIO.readWorkingSet(Integer.valueOf(workingSetId)).getTaxon()) {
			assessments.addAll(helper.getAssessmentIds(taxon.getId()));
		}
		
		return assessments;

	}
	
	private String getField(NativeDocument doc) throws ResourceException {
		try {
			return doc.getDocumentElement().getElementsByTagName("field").item(0).getTextContent();
		} catch (Exception poorlyHandled) {
			throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, poorlyHandled);
		}
	}

	/**
	 * CaseSensitive, RestrictToWholeWord, REGEX
	 **/
	private String getOptions(NativeDocument doc) throws ResourceException {
		try {
			return doc.getDocumentElement().getElementsByTagName("options").item(0).getTextContent();
		} catch (Exception poorlyHandled) {
			throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, poorlyHandled);
		}
	}

	private String getText(NativeDocument doc) throws ResourceException {
		try {
			return doc.getDocumentElement().getElementsByTagName("text").item(0).getTextContent();
		} catch (Exception poorlyHandled) {
			throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, poorlyHandled);
		}
	}
	
	@Override
	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		String searchID = (String) request.getAttributes().get("searchid");
		if (currentSearches.containsKey(searchID)) {
			currentSearches.remove(searchID).killdash9();
			response.setStatus(Status.SUCCESS_OK);
		} else
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
	}

	private ArrayList<FindReplaceData> parseDocument(NativeDocument doc) throws NullPointerException {
		ArrayList<FindReplaceData> list = new ArrayList<FindReplaceData>();
		NativeNodeList nodeList = doc.getDocumentElement().getElementsByTagName("result");
		for (int i = 0; i < nodeList.getLength(); i++) {
			list.add(FindReplaceData.fromXML((nodeList.elementAt(i))));
		}
		return list;
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		String searchid = (String) request.getAttributes().get("searchid");
		if (searchid.trim().equalsIgnoreCase("null")) {
			NativeDocument doc = new JavaNativeDocument();
			try {
				doc.parse(entity.getText());
			} catch (Exception e) {
				throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
			}
					
			List<Integer> assessmentIDs;
			try {
				assessmentIDs = getAssessmentsToSearch(doc, session);
			} catch (Exception poorlyHandled) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, poorlyHandled);
			}
			String text = getText(doc);
			String options = getOptions(doc);
			String field = getField(doc);

			if (options != null) {
				startNewSearch(text, assessmentIDs, options, field, request, response);
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else
			postContinueSearching(searchid, response);
	}

	private void postContinueSearching(String searchID, Response response) {
		if (currentSearches.containsKey(searchID.trim())) {
			currentSearches.get(searchID).resetResponse(response, false);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		NativeDocument ndoc = new JavaNativeDocument();
		try {
			ndoc.parse(entity.getText());
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		StringBuilder xml = new StringBuilder("<errors>\r\n");
			
		String options = getOptions(ndoc);
		String field = getField(ndoc);
		ArrayList<FindReplaceData> list = parseDocument(ndoc);
	
		boolean overallSuccess = true;
		for (int i = 0; i < list.size(); i++) {
			boolean success = replacer.replace(getUser(request, session), list.get(i), options, field, session);
			if (!success) {
				xml.append("<error>" + list.get(i).getAssessmentName() + "</error>\r\n");
				overallSuccess = false;
			}
		}
		xml.append("</errors>");

		if (overallSuccess) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(xml.toString(), MediaType.TEXT_XML);
		}
	}

	private void startNewSearch(String text, List<Integer> assessments, String options, String field,
			Request request, Response response) {
		String searchID = searchid.getAndIncrement() + "";
		MonitorThread thread = new MonitorThread(searchID, assessments, text, options, field, request);
		currentSearches.put(searchID.trim(), thread);
		(new Thread(thread)).start();
		response.setEntity("<xml>" + searchID + "</xml>", MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_CREATED);
	}

	private String toXMLSearchResults(Vector<FindReplaceData> list, String searchID) {
		StringBuffer xml = new StringBuffer("<xml searchid=\"" + searchID + "\">\r\n");
		for (int i = 0; i < list.size(); i++) {

			xml.append(list.get(i).toXML());

		}
		xml.append("</xml>\r\n");
		return xml.toString();
	}

}
