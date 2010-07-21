package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.HashMap;

import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.restlet.data.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.AlphanumericComparator;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

public abstract class BaseDraftAssessmentModder implements Runnable {

	public static boolean stop = false;
	public static boolean running = false;

	public static StringBuilder results;
	public static int count = 0;

	public AlphanumericComparator comparator = new AlphanumericComparator();
	protected File vfsRoot;
	protected VFS vfs;

	protected String workingSetURL;
	protected boolean includeRegionals;

	protected HashMap<String, Boolean> ids;

	protected boolean writeback;
	
	public BaseDraftAssessmentModder(File vfsRoot, String workingSetURL, boolean includeRegionals) {
		setVFSRoot(vfsRoot);
		this.workingSetURL = workingSetURL;
		this.includeRegionals = includeRegionals;
	}

	public BaseDraftAssessmentModder(VFS vfs, String workingSetURL, boolean includeRegionals) {
		setVfs(vfs);
		this.workingSetURL = workingSetURL;
		this.includeRegionals = includeRegionals;
	}

	private void crawlVFS(VFS vfs, String rootPath) throws NotFoundException, VFSPathParseException {
		for (VFSPathToken url : vfs.list(VFSUtils.parseVFSPath(rootPath))) {
			String curURL = rootPath + url.toString();
			try {
				if (vfs.isCollection(VFSUtils.parseVFSPath(curURL)))
					crawlVFS(vfs, curURL + "/");
				else if( curURL.endsWith(".xml") )
					parseAssessment(vfs, url.toString().replace(".xml", ""));
				else
					System.out.println("Skipping non-xml file " + curURL);
			} catch (NotFoundException e) {
				System.out.println("Could not find path " + curURL);
				throw e;
			}
		}
	}

	protected void getIDsToMod() {
		if (workingSetURL == null)
			return;

		try {
			Document doc = DocumentUtils.getVFSFileAsDocument(workingSetURL, vfs);
			NodeCollection nodes = new NodeCollection(doc.getDocumentElement().getElementsByTagName("species"));

			System.out.println("Modifying " + nodes.size() + " taxa.");
			ids = new HashMap<String, Boolean>();

			for (Node node : nodes)
				ids.put(node.getTextContent(), new Boolean(true));
		} catch (Exception ignored) {
		}
	}

	protected boolean ignoreThisAssessment(String id) {
		if (ids == null)
			return false;

		return !ids.containsKey(id);
	}

	private void parseAssessment(VFS vfs, final String id) {
		count++;

		if (count % 1000 == 0)
			System.out.println("Through " + count + " taxa.");

		String taxonID = id.contains("_") ? id.substring(0, id.indexOf("_")) : id;

		if (ignoreThisAssessment(taxonID))
			return;

		AssessmentParser ap = new AssessmentParser();

		NativeDocument doc = SISContainerApp.newNativeDocument(null);
		String xml = DocumentUtils.getVFSFileAsString(ServerPaths.getDraftAssessmentURL(id), vfs);
		
		if( xml != null && !xml.equals("") ) {
			doc.parse(xml);
			ap.parse(doc);
			workOnAssessment(ap.getAssessment(), parseNode(vfs, taxonID));
		}	
	}

	protected TaxonNode parseNode(VFS vfs, final String id) {
		count++;

		if (count % 1000 == 0)
			System.out.println("Through " + count + " taxa.");

		return TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths.getURLForTaxa(id), vfs), null,
				false);
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
			writeback = true;
			results = new StringBuilder();
			if (vfs == null)
				vfs = VFSFactory.getVFS(vfsRoot);

			getIDsToMod();

			if( workingSetURL != null && workingSetURL.equalsIgnoreCase("/workingsets/false.xml") )
				writeback = false;
			System.out.println("Writeback is " + writeback + ".");
			
			crawlVFS(vfs, "/drafts/");
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

	protected abstract void workOnAssessment(AssessmentData data, TaxonNode node);

	protected boolean writeBackDraftAssessment(AssessmentData data) {
		return AssessmentIO.writeAssessment(data, "scriptedChange", vfs, false).status == Status.SUCCESS_OK;
	}

	protected boolean writeBackTaxon(TaxonNode taxon) {
		return DocumentUtils.writeVFSFile(ServerPaths.getURLForTaxa(taxon.getId() + ""), vfs, true, TaxonNodeFactory
				.nodeToDetailedXML(taxon));
	}
}
