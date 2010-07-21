package org.iucn.sis.server.extensions.reports;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.shared.api.assessments.AssessmentParser;
import org.iucn.sis.shared.api.data.WorkingSetParser;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonFactory;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.views.ViewParser;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.AlphanumericComparator;
import com.solertium.util.ArrayUtils;
import com.solertium.util.MD5Hash;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

public class ReportRestlet extends ServiceRestlet {

	protected final static String ZIPPED_CSS_LOCATION = "styles.css";

	public ReportRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	/**
	 * adds the css file to the zippedoutputstream in the location
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void addCSSFileToZipped(ZipOutputStream out) throws IOException {
		out.putNextEntry(new ZipEntry(ZIPPED_CSS_LOCATION));
		InputStream inputStream = vfs.getInputStream(VFSUtils.parseVFSPath(AssessmentHtmlTemplate.CSS_VFS_LOCATION));
		int len;
		byte[] buf = new byte[1024];
		while ((len = inputStream.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.closeEntry();
	}

	@Override
	public void definePaths() {
		paths.add("/reports/published/{id}");
		paths.add("/reports/draft/{id}");
		paths.add("/reports/user/{username}/{id}");
		paths.add("/reports/workingset/{id}/{isPublic}");

	}

	private void doGet(boolean showEmpties, boolean useLimited, Response response, Request request, String type, String assessmentID, String username) {
		
		
		String assessmentUrl = null;
		if (type.equalsIgnoreCase(AssessmentType.PUBLISHED_ASSESSMENT_TYPE))
			assessmentUrl = ServerPaths.getPublishedAssessmentURL(assessmentID);
		else if (type.equalsIgnoreCase(AssessmentType.DRAFT_ASSESSMENT_TYPE))
			assessmentUrl = ServerPaths.getDraftAssessmentURL(assessmentID);
		else
			assessmentUrl = ServerPaths.getUserAssessmentUrl(username, assessmentID);

		String xml = DocumentUtils.getVFSFileAsString(assessmentUrl, vfs);

		NativeDocument ndoc = SIS.get().newNativeDocument(request.getChallengeResponse());
		ndoc.parse(xml);


		try {

			Assessment assessment = Assessment.fromXML(ndoc);
			long lastMod = vfs.getLastModified(new VFSPath(assessmentUrl));
			assessment.setDateModified(lastMod);

			String taxonXML = DocumentUtils.getVFSFileAsString(ServerPaths.getURLForTaxa(assessment.getSpeciesID()),
					vfs);
			Taxon taxon = TaxonFactory.createNode(taxonXML, null, false);

			NativeDocument viewsDoc = NativeDocumentFactory.newNativeDocument();
			viewsDoc.parse(DocumentUtils.getVFSFileAsString("/browse/docs/views.xml", vfs));
			ViewParser view = new ViewParser();
			view.parse(viewsDoc);

			AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(showEmpties, useLimited);
			template.parse(assessment, taxon, view.getViews().get("FullView"));

			ByteArrayInputStream bis = new ByteArrayInputStream(template.getHtmlBytes());
			InputRepresentation pdfFile = new InputRepresentation(bis, MediaType.TEXT_HTML);
			pdfFile.setCharacterSet(CharacterSet.UTF_8);
			response.setEntity(pdfFile);
			// report.getOutputStream().writeTo(fout);
			// fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a url to a zipped file containing a single report which is the
	 * concatination of all assessment reports.
	 * 
	 * @param taxaIDToNode
	 * @param taxaIDToAssessments
	 * @param reportID
	 * @return
	 */
	protected String getZippedReport(final boolean showEmpties, final boolean useLimited, Map<String, Taxon> taxaIDToNode,
			LinkedHashMap<String,List<Assessment>> taxaIDToAssessments, String reportID) {
		StringBuilder report = new StringBuilder();
		NativeDocument viewsDoc = NativeDocumentFactory.newNativeDocument();
		viewsDoc.parse(DocumentUtils.getVFSFileAsString("/browse/docs/views.xml", vfs));
		ViewParser view = new ViewParser();
		view.parse(viewsDoc);

		Pattern removerPattern = Pattern.compile("<head(.*?)<body>");
		Pattern endBody = Pattern.compile("</body>.*");
		String endingTemplate = null;

		ArrayList<Entry<String, Taxon>> taxaSet = new ArrayList<Entry<String,Taxon>>(taxaIDToNode.entrySet());
		ArrayUtils.quicksort(taxaSet, new Comparator<Entry<String, Taxon>>() {
			public int compare(Entry<String, Taxon> o1, Entry<String, Taxon> o2) {
				String fp1 = o1.getValue().getFootprintAsString() + o1.getValue().getFullName();
				String fp2 = o2.getValue().getFootprintAsString() + o2.getValue().getFullName();
				return new AlphanumericComparator().compare(fp1, fp2);
			}
		});
		for (Entry<String, Taxon> entry : taxaSet) {
			String taxaID = entry.getKey();
			Taxon taxon = entry.getValue();
			List<Assessment> assessments = taxaIDToAssessments.get(taxaID);

			if (assessments != null) {
				for (Assessment assessment : assessments) {
					AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(showEmpties, useLimited);
					template.parse(assessment, taxon, view.getViews().get("FullView"));
					String htmlString = template.getHtmlString();
					if (report.length() == 0) {
						StringBuffer string = new StringBuffer();
						Matcher matcher = endBody.matcher(htmlString);
						matcher.find();
						endingTemplate = matcher.group();
						matcher.appendReplacement(string, "");
						report.append(replaceCSSLocation(string.toString()));
					} else {
						Matcher match = removerPattern.matcher(htmlString);
						String modifiedHTML = match.replaceFirst("");
						modifiedHTML = endBody.matcher(modifiedHTML).replaceFirst("");
						report.append(modifiedHTML);
					}

					report.append("<br /><hr/><hr/><hr/><br />");
				}
			}
		}
		report.append(endingTemplate);

		ByteArrayInputStream bis = new ByteArrayInputStream(report.toString().getBytes());
		ZipOutputStream out;

		try {
			if (!vfs.exists(VFSUtils.parseVFSPath(ServerPaths.getSpeciesReportPath()))) {

				vfs.makeCollection(VFSUtils.parseVFSPath(ServerPaths.getSpeciesReportPath()));

			}
		} catch (NotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (ConflictException e) {
			e.printStackTrace();
			return null;
		} catch (VFSPathParseException e) {
			e.printStackTrace();
			return null;
		}

		try {
			out = new ZipOutputStream(vfs.getOutputStream(VFSUtils.parseVFSPath(ServerPaths
					.getSpeciesReportURL(reportID))));
			out.putNextEntry(new ZipEntry("report.html"));
			int len;
			byte[] buf = new byte[1024];
			while ((len = bis.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.closeEntry();
			addCSSFileToZipped(out);
			out.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return "/raw" + ServerPaths.getSpeciesReportURL(reportID);
	}

	/**
	 * Returns a url to a zipped file containing a different report for every
	 * assessment
	 * 
	 * @param taxaIDToNode
	 * @param taxaIDToAssessments
	 * @param reportID
	 * @return
	 */
	protected String getZippedReports(final boolean showEmpties, final boolean useLimited, Map<String, Taxon> taxaIDToNode,
			LinkedHashMap<String, List<Assessment>> taxaIDToAssessments, String reportID) {
		NativeDocument viewsDoc = NativeDocumentFactory.newNativeDocument();
		viewsDoc.parse(DocumentUtils.getVFSFileAsString("/browse/docs/views.xml", vfs));
		ViewParser view = new ViewParser();
		view.parse(viewsDoc);

		VFSPath vfsPath;
		try {
			vfsPath = VFSUtils.parseVFSPath(ServerPaths.getSpeciesReportPath());
		} catch (VFSPathParseException e) {
			return null;
		}

		if (!vfs.exists(vfsPath)) {
			try {
				vfs.makeCollection(vfsPath);
			} catch (NotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (ConflictException e) {
				e.printStackTrace();
				return null;
			}
		}
		ZipOutputStream out;
		try {
			vfsPath = VFSUtils.parseVFSPath(ServerPaths.getSpeciesReportURL(reportID));
			out = new ZipOutputStream(vfs.getOutputStream(vfsPath));
		} catch (NotFoundException e1) {
			e1.printStackTrace();
			return null;
		} catch (ConflictException e1) {
			e1.printStackTrace();
			return null;
		} catch (VFSPathParseException e) {
			e.printStackTrace();
			return null;
		}

		for (Entry<String, Taxon> entry : taxaIDToNode.entrySet()) {
			String taxaID = entry.getKey();
			Taxon taxon = entry.getValue();
			List<Assessment> assessments = taxaIDToAssessments.get(taxaID);

			if (assessments != null) {
				for (Assessment assessment : assessments) {
					AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(showEmpties, useLimited);
					template.parse(assessment, taxon, view.getViews().get("FullView"));
					String stringToSave = replaceCSSLocation(template.getHtmlString());
					ByteArrayInputStream bis = new ByteArrayInputStream(stringToSave.getBytes());

					String path;
					if (assessment.getType().equalsIgnoreCase(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS))
						path = assessment.getSpeciesName()
						+ "_published_"
						+ (assessment.getDateAssessed().length() > 10 ? assessment.getDateAssessed().substring(
								0, 10) : assessment.getDateAssessed());
					else if (assessment.isGlobal())
						path = assessment.getSpeciesName() + "_draft";
					else
						path = assessment.getSpeciesName() + "_draft" + assessment.getRegionIDs();

					path = path + ".html";

					try {
						out.putNextEntry(new ZipEntry(path));

						int len;
						byte[] buf = new byte[1024];
						while ((len = bis.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						out.closeEntry();
						bis.close();
					} catch (ZipException e) {
						e.printStackTrace();
						try {
							out.closeEntry();
							bis.close();
						} catch (IOException cleaningUpIgnored) {
						}
						continue;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}

				}
			}
		}

		try {
			addCSSFileToZipped(out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "/raw" + ServerPaths.getSpeciesReportURL(reportID);
	}

	@Override
	public void performService(Request request, Response response) {
		Form queryString = request.getResourceRef().getQueryAsForm();
		boolean showEmpties = true; 
		boolean useLimited = false;
		
		try {
			showEmpties = Boolean.valueOf(queryString.getFirstValue("empty"));
			useLimited = Boolean.valueOf(queryString.getFirstValue("limited"));
			
			if (request.getResourceRef().getPath().startsWith("/reports/workingset/") && request.getMethod().equals(Method.POST)) {
				try {
					reportOnWorkingSet(showEmpties, useLimited, (String) request.getAttributes().get("id"), 
							new DomRepresentation(request.getEntity()).getDocument(), request, response);
				} catch (IOException e) {
					e.printStackTrace();
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				}
			} else {
				String id = (String) request.getAttributes().get("id");
				String user = "";
				String type = "";
				if (request.getResourceRef().getPath().startsWith("/reports/user/")) {
					type = BaseAssessment.USER_ASSESSMENT_STATUS;
					user = (String) request.getAttributes().get("username");
				} else {
					if (request.getResourceRef().getPath().startsWith("/reports/draft/")) {
						type = BaseAssessment.DRAFT_ASSESSMENT_STATUS;
					} else {
						type = BaseAssessment.PUBLISHED_ASSESSMENT_STATUS;
					}
				}

				if (id == null)
					response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
				else
					doGet(showEmpties, useLimited, response, request, type, id, user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Replaces the css location for zipped files
	 * 
	 * @param inString
	 * @return
	 */
	protected String replaceCSSLocation(String inString) {
		return inString.replaceAll("\\Q" + AssessmentHtmlTemplate.CSS_LOCATION + "\\E", ZIPPED_CSS_LOCATION);
	}

	/**
	 * Given a workingsetID and a documentElement which holds report preferences
	 * generates a report, saves it to the file system, and hands down a link to
	 * the report
	 * 
	 * @param workingSetID
	 * @param docElement
	 * @param request
	 * @param response
	 */
	protected void reportOnWorkingSet(final boolean showEmpties, final boolean useLimited, final String workingSetID, 
			final Document requestDoc, final Request request, final Response response) {
		boolean singleReport = false;
		LinkedHashMap<String, Taxon> taxaIDToNode = new LinkedHashMap<String, Taxon>();
		LinkedHashMap<String, List<Assessment>> taxaIDToAssessments = new LinkedHashMap<String, List<Assessment>>();
		Element docElement = requestDoc.getDocumentElement();

		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(DocumentUtils.serializeNodeToString(requestDoc));
		AssessmentFilter filter = AssessmentFilter.parseXML(ndoc);
		AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);

		
		NodeList files = docElement.getElementsByTagName("file");
		if (files.getLength() > 0)
			singleReport = files.item(0).getTextContent().equals("single");

		NativeDocument wsDoc = NativeDocumentFactory.newNativeDocument();
		String isPublic = (String)request.getAttributes().get("isPublic");
		if( "true".equalsIgnoreCase(isPublic) )
			wsDoc.fromXML(WorkingSetIO.readPublicWorkingSetAsString(vfs, workingSetID));
		else
			wsDoc.fromXML(WorkingSetIO.readPrivateWorkingSetAsString(vfs, workingSetID, SIS.get().getUsername(request)));

		WorkingSetParser wsParser = new WorkingSetParser();
		WorkingSet  ws = wsParser.parseSingleWorkingSet(wsDoc.getDocumentElement());

		System.out.println("operating on " + ws.getSpeciesIDs().size());

		for (String taxaID : ws.getSpeciesIDs()) {
			Taxon node = TaxonFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
					.getURLForTaxa(taxaID), vfs), null, false);
			if (node != null) {
				taxaIDToNode.put(taxaID, node);
				taxaIDToAssessments.put(taxaID, helper.getAssessments(taxaID, vfs));
			}
		}

		if (taxaIDToAssessments.size() == 0) {
			System.out.println("No assessments actually found...");
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else {
			// CREATE REPORT
			String reportID = new MD5Hash(workingSetID + SIS.get().getUsername(request)).toString();
			String speciesURL;
			if (singleReport)
				speciesURL = getZippedReport(showEmpties, useLimited, taxaIDToNode, taxaIDToAssessments, reportID);
			else
				speciesURL = getZippedReports(showEmpties, useLimited, taxaIDToNode, taxaIDToAssessments, reportID);

			if (speciesURL != null) {
				response.setEntity(speciesURL, MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}

	}

}



