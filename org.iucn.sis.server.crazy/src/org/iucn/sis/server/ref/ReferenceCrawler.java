package org.iucn.sis.server.ref;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.SelectCountDBProcessor;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;

import com.solertium.db.CString;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.StringLiteral;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

public class ReferenceCrawler implements Runnable {

	private final VFS vfs;
	private final ExecutionContext ec;

	private AtomicBoolean crawling = new AtomicBoolean(false);
	private AtomicInteger files = new AtomicInteger(0);
	private static HashMap<String, String> seen = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.load(new FileInputStream("eclipse_config.properties"));
		DBSessionFactory.registerDataSources(properties);

		VFS ivfs = VFSFactory.getVFS(new File("/home/rob.heittman/sisimp/sis/vfs"));
		SysDebugger.getInstance().println(ivfs.getClass().getName());
		ReferenceCrawler crawler = new ReferenceCrawler(ivfs);
		crawler.run();
	}

	/**
	 * This mechanism examines an incoming Assessment document, computes correct
	 * hash ID for each Reference contained therein, and if the ID varies,
	 * updates the reference element in the document with the updated ID (and
	 * other standardizations).
	 * 
	 * An attempt is made to insert each reference in the global reference
	 * database.
	 * 
	 * @param assessment
	 *            the parsed assessment
	 * @param ec
	 *            An ExecutionContext bound to the "default" datasource
	 * @return AssessmentData - null if no writeback needed, not null if it was
	 *         changed
	 */
	public static AssessmentData updateHashesAndCaptureToDatabase(AssessmentData assessment, ExecutionContext ec) {
		boolean changed = false;
		String assessmentID = assessment.getAssessmentID();
		String assessmentType = assessment.getType();

		DeleteQuery del = new DeleteQuery("assessment_reference", "asm_id", assessmentID);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

		try {
			ec.doUpdate(del);
		} catch (DBException e) {
			e.printStackTrace();
			System.out.println("Unable to remove assessments in assessment_reference column for assessment "
					+ assessmentID);
		}

		for (Entry<String, ArrayList<ReferenceUI>> refMap : assessment.getReferences().entrySet()) {
			String field = refMap.getKey();
			ArrayList<ReferenceUI> refs = refMap.getValue();

			for (ReferenceUI ref : refs) {
				Reference r = new Reference(ref);

				String id = r.getId();
				if (!id.equals(ref.getReferenceID())) {
					ref.setReferenceID(id);
					changed = true;
				}
				if (!seen.containsKey(id)) {
					try {
						InsertQuery iq = new InsertQuery();
						iq.setTable("bibliography");
						iq.setRow(r);
						ec.doUpdate(iq);
						SysDebugger.getInstance().println("  " + id + " inserted");
					} catch (Exception ignored) {
						SysDebugger.getInstance().println("  " + id + " not inserted");
					}
				}
				seen.put(id, "");

				// Make sure it's not already indexed
				String sql = "SELECT COUNT(*) AS rowcount FROM assessment_reference WHERE ref_id="
						+ ec.formatLiteral(new StringLiteral(id)) + " AND asm_id="
						+ ec.formatLiteral(new StringLiteral(assessmentID)) + ";";
				SelectCountDBProcessor proc = new SelectCountDBProcessor();
				try {
					ec.doQuery(sql, proc);
				} catch (DBException dbx) {
					dbx.printStackTrace();
				}

				if (proc.getCount() == 0) {
					final Row asmRefRow = new Row();
					asmRefRow.add(new CString("asm_id", assessmentID));
					asmRefRow.add(new CString("field", field));
					asmRefRow.add(new CString("asm_type", assessmentType));
					asmRefRow.add(new CString("user", null));
					asmRefRow.add(new CString("ref_id", id));
					try {
						final InsertQuery iqr = new InsertQuery("assessment_reference", asmRefRow);
						ec.doUpdate(iqr);
					} catch (final DBException dbx) {
						dbx.printStackTrace();
					}
				}
			}
		}
		if (changed)
			return assessment;
		else
			return null;
	}

	/**
	 * This mechanism examines an incoming Assessment document, computes correct
	 * hash ID for each Reference contained therein, and if the ID varies,
	 * updates the reference element in the document with the updated ID (and
	 * other standardizations).
	 * 
	 * An attempt is made to insert each reference in the global reference
	 * database.
	 * 
	 * @param dom
	 *            Assessment document DOM
	 * @param ec
	 *            An ExecutionContext bound to the "default" datasource
	 * @return AssessmentData - null if no writeback needed, not null if it was
	 *         changed
	 */
	public static AssessmentData updateHashesAndCaptureToDatabase(NativeDocument dom, ExecutionContext ec) {
		AssessmentData assessment = new AssessmentParser(dom).getAssessment();
		return updateHashesAndCaptureToDatabase(assessment, ec);
	}

	public ReferenceCrawler(VFS vfs) throws NamingException {
		this.vfs = vfs;
		ec = new SystemExecutionContext(ReferenceApplication.DBNAME);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}

	public String getStatus() {
		return ("ReferenceCrawler crawling: " + crawling.get() + "\n" + "references seen: " + seen.size() + "\n"
				+ "files crawled: " + files.get());
	}

	public void run() {

		if (crawling.get() == true)
			return; // do not crawl if already crawling

		crawling.set(true);
		try {
			DeleteQuery dq = new DeleteQuery("bibliography");
			ec.doUpdate(dq);
		} catch (DBException ex) {
			SysDebugger.getInstance().println("Could not flush bibliography table");
		}
		try {
			traverse("/browse/assessments");
			traverse("/drafts");
		} catch (NotFoundException nf) {
			nf.printStackTrace();
			SysDebugger.getInstance().println("Could not crawl for references");
		}
		crawling.set(false);
	}

	private void traverse(String uri) throws NotFoundException {
		SysDebugger.getInstance().println(uri);
		if (vfs.isCollection(uri))
			for (String child : vfs.list(uri))
				traverse(uri + "/" + child);
		else if (uri.endsWith(".xml")) {
			NativeDocument dom = NativeDocumentFactory.newNativeDocument();
			dom.parse(DocumentUtils.getVFSFileAsString(uri, vfs));
			files.addAndGet(1);
			AssessmentData changed = updateHashesAndCaptureToDatabase(dom, ec);
			if (changed != null) {
				AssessmentIO.writeAssessment(changed, "ReferenceUpdate", vfs, true);
				SysDebugger.getInstance().println("  document updated");
			}
		}
	}

}
