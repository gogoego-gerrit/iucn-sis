package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.FilenameStriper;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.solertium.util.AlphanumericComparator;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

public abstract class BasePublishedAssessmentModder implements Runnable {

	public static boolean stop = false;
	public static boolean running = false;

	public static StringBuilder results = new StringBuilder();
	public static int count = 0;

	public AlphanumericComparator comparator = new AlphanumericComparator();
	protected File vfsRoot;
	protected VFS vfs;

	public BasePublishedAssessmentModder(File vfsRoot) {
		setVFSRoot(vfsRoot);
	}

	public BasePublishedAssessmentModder(VFS vfs) {
		setVfs(vfs);
	}

	private void crawlVFS(VFS vfs, String rootPath) throws NotFoundException, VFSPathParseException {
		for (VFSPathToken url : vfs.list(VFSUtils.parseVFSPath(rootPath))) {
			String curURL = rootPath + url.toString();

			try {
				if (vfs.isCollection(VFSUtils.parseVFSPath(curURL)))
					crawlVFS(vfs, curURL + "/");
				else {
					if( curURL.endsWith(".xml" ) )
						parseNode(vfs, DocumentUtils.getVFSFileAsDocument(curURL, vfs));
				}
			} catch (NotFoundException e) {
				System.out.println("Could not find path " + curURL);
				throw e;
			}
		}
	}

	protected void parseNode(VFS vfs, Document node) {
		count++;

		if (count % 500 == 0)
			System.out.println("Through " + count + " taxa.");

		NodeList list = node.getElementsByTagName("assessments");

		if (list.getLength() > 0) {
			final String[] assessmentIds;
			String assList = list.item(0).getTextContent();

			if (assList.contains(","))
				assessmentIds = assList.split(",");
			else
				assessmentIds = new String[] { assList };

			ArrayList<AssessmentData> toSort = new ArrayList<AssessmentData>();

			for (int i = 0; i < assessmentIds.length; i++) {
				final String id = assessmentIds[i];
				AssessmentData ass = null;
				try {
					ass = AssessmentIO.readAssessment(vfs, id, BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, "script");

					if(ass != null)
						toSort.add(ass);
					else if( ass == null )
						System.out.println("ERROR fetching assessment " + id + " for node "
								+ node.getDocumentElement().getAttribute("id"));
				} catch (Exception e) {
					System.out.println("Error fetching assessment " + id + " for taxon " + 
							node.getDocumentElement().getAttribute("id") + ". Exception was of type " + 
							e.getClass().getCanonicalName());
				}
			}
			Collections.sort(toSort, new Comparator<AssessmentData>() {
				public int compare(AssessmentData o1, AssessmentData o2) {
					return (comparator.compare(o1.getDateAssessed() != null ? o1.getDateAssessed() : "", o2
							.getDateAssessed() != null ? o2.getDateAssessed() : ""));
				}
			});

			workOnFullList(toSort);
			
			for (int i = 0; i < toSort.size() - 1; i++)
				workOnHistorical(toSort.get(i));

			if (toSort.size() > 0)
				workOnMostRecent(toSort.get(toSort.size() - 1));
		}
	}

	/**
	 * Do not call me without setting a VFS root. I will get angry.
	 */
	public void run() {
		File f = new File(".sis_script_lock");
		if (f.exists()) {
			System.out.println("A script is already running. If you're sure "
					+ "one is not, ensure the lock file does not exist.");
			return;
		} else {
			try {
				if (!f.createNewFile())
					System.out.println("This script is running without a lock. "
							+ "Please ensure you do not launch another modification " + "script concurrently.");
			} catch (Exception e) {
				System.out.println("This script is running without a lock. "
						+ "Please ensure you do not launch another modification " + "script concurrently.");
			}
		}

		try {
			running = true;
			if (vfs == null)
				vfs = VFSFactory.getVFS(vfsRoot);

			crawlVFS(vfs, "/browse/nodes/");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			running = false;

			if (f.exists() && !f.delete())
				System.out.println("Could not remove lock file programmatically. " + "Please remove it manually.");
		}
	}

	public void setVfs(VFS vfs) {
		this.vfs = vfs;
	}

	public void setVFSRoot(File vfsRoot) {
		this.vfsRoot = vfsRoot;
	}

	protected void workOnFullList(List<AssessmentData> assessments) {
		//Does nothing by default.
	}
	
	protected abstract void workOnHistorical(AssessmentData data);

	protected abstract void workOnMostRecent(AssessmentData data);

	protected boolean writeBackPublishedAssessment(AssessmentData data) {
		//return AssessmentIO.writeAssessment(data, "script", vfs, false).status.isSuccess();
		return DocumentUtils.writeVFSFile("/browse/assessments/"
				+ FilenameStriper.getIDAsStripedPath(data.getAssessmentID()) + ".xml", vfs, true, DocumentUtils
				.createDocumentFromString(data.toXML()));
	}
}
