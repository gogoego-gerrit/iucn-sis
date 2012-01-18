package org.iucn.sis.server.extensions.reports;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.hibernate.Session;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.utils.FormattedDate;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.AlphanumericComparator;
import com.solertium.util.MD5Hash;

public class AggregateReporter {
	
	private final Session session;
	private final WorkingSet workingSet;
	private final User user;
	
	private String logo;
	
	public AggregateReporter(Session session, WorkingSet workingSet, User user) {
		this.session = session;
		this.workingSet = workingSet;
		this.user = user;
		
		setLogo("redListLogo.jpg");
	}
	
	public void setLogo(String logo) {
		this.logo = logo;
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
	public String generate(final boolean showEmpties, final boolean useLimited, 
			final boolean singleReport, final NativeDocument filterDocument) throws ResourceException {
		LinkedHashMap<Integer, Taxon> taxaIDToNode = new LinkedHashMap<Integer, Taxon>();
		LinkedHashMap<Integer, List<Assessment>> taxaIDToAssessments = 
			new LinkedHashMap<Integer, List<Assessment>>();
		
		AssessmentFilter filter = AssessmentFilter.fromXML(filterDocument);
		AssessmentFilterHelper helper = new AssessmentFilterHelper(session, filter);

		for (Taxon taxon : workingSet.getTaxon()) {
			taxaIDToNode.put(taxon.getId(), taxon);
			taxaIDToAssessments.put(taxon.getId(), helper.getAssessments(taxon.getId()));
		}

		if (taxaIDToAssessments.isEmpty())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		// CREATE REPORT
		String speciesURL = null;
		String reportID = new MD5Hash(workingSet.getId() + user.getUsername()).toString();
		if (singleReport)
			speciesURL = getZippedReport(showEmpties, useLimited, taxaIDToNode, taxaIDToAssessments, reportID);
		else
			speciesURL = getZippedReports(showEmpties, useLimited, taxaIDToNode, taxaIDToAssessments, reportID);

		if (speciesURL == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
				
		return speciesURL;
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
	private String getZippedReport(final boolean showEmpties, final boolean useLimited, Map<Integer, Taxon> taxaIDToNode,
			LinkedHashMap<Integer,List<Assessment>> taxaIDToAssessments, String reportID) throws ResourceException {
		
		StringBuilder report = new StringBuilder();
		report.append("<html>\n" +
				"<head>\n" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"" + AssessmentHtmlTemplate.CSS_FILE + "\">");
		report.append("<title>");
		report.append(workingSet.getName());
		report.append("</title></head><body>");

		ArrayList<Entry<Integer, Taxon>> taxaSet = new ArrayList<Entry<Integer,Taxon>>(taxaIDToNode.entrySet());
		try {
			Collections.sort(taxaSet, new TextComparator<Entry<Integer, Taxon>>() {
				protected String getString(Entry<Integer, Taxon> model) {
					return model.getValue().getFootprintAsString() + model.getValue().getFullName();
				}
			});
		} catch (Exception e) {
			Debug.println("Failed to sort taxa for reports: {0}", e.getMessage());
		}
		
		for (Entry<Integer, Taxon> entry : taxaSet) {
			List<Assessment> assessments = taxaIDToAssessments.get(entry.getKey());
			if (assessments != null) {
				for (Assessment assessment : assessments) {
					AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(session, assessment, showEmpties, useLimited);
					template.setAggregate(true);
					template.parse();
					
					report.append(replaceLogoLocation(template.getHtmlString(), logo));

					report.append("<br /><hr/><hr/><hr/><br />");
				}
			}
		}
		report.append("</body>\r\n</html>");

		ByteArrayInputStream bis = new ByteArrayInputStream(report.toString().getBytes());
		
		File folder = new File(createTempFolder(reportID));
		File tmp = new File(folder, reportID + ".zip");
		
		try {
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
			out.putNextEntry(new ZipEntry("report.html"));
			
			int len;
			byte[] buf = new byte[1024];
			while ((len = bis.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.closeEntry();
			
			addFileToZipped(out, AssessmentHtmlTemplate.CSS_FILE, AssessmentHtmlTemplate.CSS_FILE);
			addFileToZipped(out, logo, logo);
			out.close();
			bis.close();
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		return tmp.getName();
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
	private String getZippedReports(final boolean showEmpties, final boolean useLimited, Map<Integer, Taxon> taxaIDToNode,
			LinkedHashMap<Integer, List<Assessment>> taxaIDToAssessments, String reportID) throws ResourceException {
		File folder = new File(createTempFolder(reportID));
		File tmp = new File(folder, reportID + ".zip");
		
		ZipOutputStream out;
		try {
			out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		for (Entry<Integer, Taxon> entry : taxaIDToNode.entrySet()) {
			List<Assessment> assessments = taxaIDToAssessments.get(entry.getKey());
			if (assessments != null) {
				for (Assessment assessment : assessments) {
					AssessmentHtmlTemplate template = new AssessmentHtmlTemplate(session, assessment, showEmpties, useLimited);
					template.parse();
					
					String stringToSave = replaceCSSLocation(template.getHtmlString());
					stringToSave = replaceLogoLocation(stringToSave, logo);
					
					ByteArrayInputStream bis = new ByteArrayInputStream(stringToSave.getBytes());

					String path;
					if (assessment.isPublished()) {
						path = assessment.getSpeciesName()
						+ "_published_"
						+ FormattedDate.impl.getDate(assessment.getDateAssessed());
					}
					else if (assessment.isGlobal())
						path = assessment.getSpeciesName() + "_" + assessment.getType();
					else
						path = assessment.getSpeciesName() + "_" + assessment.getType() + assessment.getRegionIDs();

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
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}
				}
			}
		}

		try {
			addFileToZipped(out, AssessmentHtmlTemplate.CSS_FILE, AssessmentHtmlTemplate.CSS_FILE);
			addFileToZipped(out, logo, logo);
			out.close();
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		return tmp.getName();
	}

	/**
	 * Replaces the css location for zipped files
	 * 
	 * @param inString
	 * @return
	 */
	private String replaceCSSLocation(String inString) {
		return inString.replaceAll("\\Q" + AssessmentHtmlTemplate.createUrl(AssessmentHtmlTemplate.CSS_FILE) + "\\E", AssessmentHtmlTemplate.CSS_FILE);
	}
	
	private String replaceLogoLocation(String inString, String logo) {
		return inString.replaceAll("\\Q" + AssessmentHtmlTemplate.createUrl(logo) + "\\E", logo);
	}
	
	private String createTempFolder(String fileName) throws ResourceException {
		File tmp;
		try {
			tmp = File.createTempFile("toDelete", "tmp");
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INSUFFICIENT_STORAGE, e);
		}
		
		File folder = tmp.getParentFile();
		File tmpFolder = new File(folder, fileName);
		tmpFolder.mkdirs();
		
		tmp.delete();
		
		return tmpFolder.getAbsolutePath();
	}
	
	/**
	 * adds the css file to the zippedoutputstream in the location
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void addFileToZipped(ZipOutputStream out, String zipName, String location) throws IOException {
		out.putNextEntry(new ZipEntry(zipName));
		InputStream inputStream = getClass().getResourceAsStream(location);
		int len;
		byte[] buf = new byte[1024];
		while ((len = inputStream.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.closeEntry();
	}
	
	private static abstract class TextComparator<T> implements Comparator<T> {
		
		private static final long serialVersionUID = 1L;
		
		private final AlphanumericComparator comparator = new AlphanumericComparator();
	
		public int compare(T arg0, T arg1) {
			return comparator.compare(getString(arg0), getString(arg1));
		}
		
		protected abstract String getString(T model);
		
	}
	
}
