package org.iucn.sis.server.simple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.filters.AssessmentFilterHelper;
import org.iucn.sis.server.findReplace.FindReplace;
import org.iucn.sis.server.findReplace.FindResultDataServer;
import org.iucn.sis.server.io.WorkingSetIO;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFSPath;

/**
 * 
 */
public class FileSearcherRestlet extends ServiceRestlet {

	class MonitorThread extends Thread {

		Response response;
		Request request;
		String searchID;
		SearchThread searchThread;
		List<AssessmentData> assessments;
		String textToLookFor;
		AtomicBoolean isSleeping;
		String options;
		String field;

		public MonitorThread(String searchID, List<AssessmentData> assessments,
				String textToLookFor, String options, String field,
				Request request) {
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

				Vector<FindResultDataServer> vector = searchThread
						.getAndClear();

				if (vector.size() > 0 || searchThread.done
						|| ((counter >= MAXLOOPS) && firstTime)) {
					response.setEntity(toXMLSearchResults(vector, searchID),
							MediaType.TEXT_XML);
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
			this.searchThread = new SearchThread(assessments, searchID,
					textToLookFor, options, field, this);
			Thread searchThreaded = new Thread(searchThread);
			searchThreaded.start();

		}

	}

	class SearchThread implements Runnable {

		private String id;
		private Vector<FindResultDataServer> resultsNotSent;
		private List<AssessmentData> assessments;
		private String textToLookFor;
		private Date timeStarted;
		private MonitorThread monitorThread;
		public boolean paused;
		public boolean done;
		public AtomicBoolean kill;
		private FindReplace searcher;
		public String options;
		public String field;

		public SearchThread(List<AssessmentData> assessments, String id,
				String textToLookFor, String options, String field,
				MonitorThread monitorThread) {
			this.id = id;
			resultsNotSent = new Vector<FindResultDataServer>();
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

		public Vector<FindResultDataServer> getAndClear() {
			synchronized (resultsNotSent) {

				Vector<FindResultDataServer> returned = new Vector<FindResultDataServer>(
						resultsNotSent);
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
				while ((assessments.size() > 0) && !kill.get()) {

					VFSPath url = null;

					AssessmentData assessment = assessments.remove(0);
					url = new VFSPath(ServerPaths.getPathForAssessment(
							assessment, null));

					if (url != null) {
						FindResultDataServer data = searcher
								.findAndReturnLineRegex(vfs, url,
										textToLookFor, options, field);
						System.out.println("server found -- " + data.toXML());
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
					}

					determineIfRanTooLong();
				}
				done = true;
				if (monitorThread.isSleeping.get()) {
					monitorThread.interrupt();
				}
			} catch (NullPointerException e) {
				done = true;
			}

		}

	}

	private final long MAXRUNTIME = 500000;

	private AtomicLong searchid;

	private ConcurrentHashMap<String, MonitorThread> currentSearches;

	private FindReplace replacer;

	public FileSearcherRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
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

	private String getField(Document doc) throws Exception {
		return doc.getDocumentElement().getElementsByTagName("field").item(0)
				.getTextContent();
	}

	private List<AssessmentData> getIDs(Document doc) throws Exception {
		Element element = doc.getDocumentElement();
		String workingSetId = element.getElementsByTagName("workingSetID")
				.item(0).getTextContent();
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(DocumentUtils.serializeNodeToString(doc));
		AssessmentFilter filter = AssessmentFilter.parseXML(ndoc
				.getDocumentElement().getElementByTagName(
						AssessmentFilter.HEAD_TAG));
		AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);
		List<AssessmentData> assessments = new ArrayList<AssessmentData>();
		for (String speciesId : WorkingSetIO
				.readPublicWorkingSetAsWorkingSetData(vfs, workingSetId)
				.getSpeciesIDs()) {
			assessments.addAll(helper.getAssessments(speciesId, vfs));
		}
		return assessments;

	}

	/**
	 * CaseSensitive, RestrictToWholeWord, REGEX
	 **/
	private String getOptions(Document doc) throws Exception {
		return doc.getDocumentElement().getElementsByTagName("options").item(0)
				.getTextContent();
	}

	private String getText(Document doc) throws IOException,
			NullPointerException {
		return doc.getDocumentElement().getElementsByTagName("text").item(0)
				.getTextContent();
	}

	private void killdash9(String searchID, Response response) {
		if (currentSearches.containsKey(searchID)) {
			currentSearches.remove(searchID).killdash9();
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private ArrayList<FindResultDataServer> parseDocument(Document doc)
			throws NullPointerException {
		ArrayList<FindResultDataServer> list = new ArrayList<FindResultDataServer>();

		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				"result");
		for (int i = 0; i < nodeList.getLength(); i++) {
			list
					.add(FindResultDataServer.fromXML(((Element) nodeList
							.item(i))));
		}

		return list;
	}

	@Override
	public void performService(Request request, Response response) {
		if (request.getMethod().equals(Method.POST)) {
			try {

				String searchid = (String) request.getAttributes().get(
						"searchid");
				Document doc = new DomRepresentation(request.getEntity())
						.getDocument();
				String text = getText(doc);
				List<AssessmentData> assessments = getIDs(doc);

				if (searchid.trim().equalsIgnoreCase("null")) {
					String options = getOptions(doc);
					String field = getField(doc);
					SysDebugger.getInstance().println("This is field" + field);
					if (options != null) {
						startNewSearch(text, assessments, options, field,
								request, response);
					} else {
						response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					}
				} else {
					postContinueSearching(searchid, response);
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
			}
		} else if (request.getMethod().equals(Method.PUT)
				&& (request.getResourceRef().getPath().startsWith("/replace"))) {
			replace(request, response);
		} else if (request.getMethod().equals(Method.DELETE)) {
			try {
				String id = (String) request.getAttributes().get("searchid");
				killdash9(id, response);
			} catch (Exception e) {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

	private void postContinueSearching(String searchID, Response response) {
		if (currentSearches.containsKey(searchID.trim())) {
			currentSearches.get(searchID).resetResponse(response, false);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private void replace(Request request, Response response) {
		try {
			Document doc = new DomRepresentation(request.getEntity())
					.getDocument();
			
			StringBuffer xml = new StringBuffer("<errors>\r\n");
			String options = getOptions(doc);
			String field = getField(doc);
			ArrayList<FindResultDataServer> list = parseDocument(doc);

			boolean overallSuccess = true;
			for (int i = 0; i < list.size(); i++) {
				System.out.println("replacing " + list.get(i).toXML());
				boolean success = replacer.replace(vfs, list.get(i), options,
						field);
				if (!success) {
					xml.append("<error>" + list.get(i).getName()
							+ "</error>\r\n");
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
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}

	}

	private void startNewSearch(String text, List<AssessmentData> assessments,
			String options, String field, Request request, Response response) {
		String searchID = searchid.getAndIncrement() + "";
		MonitorThread thread = new MonitorThread(searchID, assessments, text,
				options, field, request);
		// SysDebugger.getInstance().println(
		// "Created monitor thread with search id "
		// + searchID);
		currentSearches.put(searchID.trim(), thread);
		(new Thread(thread)).start();
		response.setEntity("<xml>" + searchID + "</xml>", MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_CREATED);
	}

	private String toXMLSearchResults(Vector<FindResultDataServer> list,
			String searchID) {
		StringBuffer xml = new StringBuffer("<xml searchid=\"" + searchID
				+ "\">\r\n");
		for (int i = 0; i < list.size(); i++) {

			xml.append(list.get(i).toXML());

		}
		xml.append("</xml>\r\n");
		return xml.toString();
	}

}
