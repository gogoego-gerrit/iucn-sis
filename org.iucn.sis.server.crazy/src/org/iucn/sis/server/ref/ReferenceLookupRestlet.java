package org.iucn.sis.server.ref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.NamingException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.references.ReferenceUtils;
import org.iucn.sis.shared.io.AssessmentIOMessage;
import org.iucn.sis.shared.xml.XMLUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.Row.Set;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.ClasspathResources;

public class ReferenceLookupRestlet extends ServiceRestlet {

	private ExecutionContext ec = null;
	private static final String DS = "ref_lookup";

	public ReferenceLookupRestlet(final String path, final Context context) {
		super(path, context);
		definePaths();

		try {
			createAndConnect();
		} catch (Exception e) {
			System.out.println("Unable to connect to the reference lookup database.");
			e.printStackTrace();
		}
	}

	private void createAndConnect() throws IOException, NamingException, DBException {
		DBSessionFactory.getDBSession(DS);

		ec = new SystemExecutionContext(DS);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

		try {
			ec.createStructure(ClasspathResources.getDocument(ReferenceDBBuilder.class, "reflookup-struct.xml"));
		} catch (final DBException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void definePaths() {
		paths.add("/reference/lookup");
		paths.add("/reference/replace");
		paths.add("/reference/rebuild/doit");
	}

	private String performLookup(List<String> refIDs) throws IOException, DBException {

		StringBuilder builder = new StringBuilder("<lookupResults>\n");

		for (String curRefID : refIDs) {
			SelectQuery select = new SelectQuery();
			select.select("assessment_reference", "*");
			select.constrain(new CanonicalColumnName("assessment_reference", "ref_id"), QConstraint.CT_EQUALS, curRefID);

			Set set = new Set();
			ec.doQuery(select, set);

			builder.append("<reference id=\"" + curRefID + "\">\n");
			for (Row curRow : set.getSet()) {
				builder.append("<result>\n");
				builder.append("<assessmentID>");
				builder.append(curRow.get("asm_id").getString());
				builder.append("</assessmentID>\n");

				builder.append("<type>");
				builder.append(curRow.get("asm_type").getString());
				builder.append("</type>\n");

				builder.append("<field>");
				builder.append(curRow.get("field").getString());
				builder.append("</field>\n");

				builder.append("<user>");
				builder.append(curRow.get("user").getString(Column.NEVER_NULL));
				builder.append("</user>\n");
				builder.append("</result>\n");
			}

			builder.append("</reference>\n");
		}
		builder.append("</lookupResults>");

		return builder.toString();
	}

	@Override
	public void performService(Request request, Response response) {
		if (request.getResourceRef().getPath().startsWith("/reference/rebuild/doit")) {
			ReferenceDBBuilder builder = new ReferenceDBBuilder();
			new Thread(builder).start();
		} else if (request.getResourceRef().getPath().startsWith("/reference/lookup")) {
			try {
				String payload = request.getEntity().getText();
				String ret = performLookup(ReferenceUtils.deserializeLookupRequest(payload));

				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(ret, MediaType.TEXT_XML);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else if (request.getResourceRef().getPath().startsWith("/reference/replace")) {
			try {
				String payload = request.getEntity().getText();

				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
				ndoc.parse(payload);

				NativeElement originalEl = ndoc.getDocumentElement().getElementByTagName("original");
				NativeElement replacementEl = ndoc.getDocumentElement().getElementByTagName("replacement");
				NativeElement exclude = ndoc.getDocumentElement().getElementByTagName("exclude");

				ReferenceUI original = new ReferenceUI(originalEl.getElementByTagName("reference"));
				ReferenceUI replacement = new ReferenceUI(replacementEl.getElementByTagName("reference"));

				String excludeID = null;
				String excludeType = null;
				if (exclude != null) {
					excludeID = exclude.getAttribute("id");
					excludeType = exclude.getAttribute("type");
				}

				String ret = replaceReference(request.getChallengeResponse().getIdentifier(), original, replacement,
						excludeID, excludeType);
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(ret, MediaType.TEXT_XML);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	private String replaceReference(String username, ReferenceUI original, ReferenceUI replacement, String excludeID,
			String excludeType) throws IOException, DBException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(original.getReferenceID());

		String xml = performLookup(list);
		NativeDocument lookupDoc = NativeDocumentFactory.newNativeDocument();
		lookupDoc.parse(xml);

		HashMap<String, AssessmentData> writeback = new HashMap<String, AssessmentData>();
		HashMap<String, AssessmentData> locked = new HashMap<String, AssessmentData>();

		NativeNodeList refs = lookupDoc.getDocumentElement().getElementsByTagName("reference");
		for (int i = 0; i < refs.getLength(); i++) {
			NativeElement el = refs.elementAt(i);

			NativeNodeList results = el.getElementsByTagName("result");
			for (int j = 0; j < results.getLength(); j++) {
				NativeElement resultEl = results.elementAt(j);
				String id = resultEl.getElementByTagName("assessmentID").getTextContent();
				String type = resultEl.getElementByTagName("type").getTextContent();
				String user = resultEl.getElementByTagName("user").getTextContent();
				String field = resultEl.getElementByTagName("field").getTextContent();

				if (id.equals(excludeID) && type.equals(excludeType))
					continue;

				if (field.equals("global"))
					field = "Global";

				AssessmentData curAss = AssessmentIO.readAssessment(vfs, id, type, user);

				if (curAss == null) {
					System.out.println("Failure to read assessment " + id + ":" + type);
				} else {
					if (!writeback.containsKey(curAss.getAssessmentID() + curAss.getType()))
						writeback.put(curAss.getAssessmentID() + curAss.getType(), curAss);
					else
						curAss = writeback.get(curAss.getAssessmentID() + curAss.getType());

					if (!curAss.removeReference(original, field))
						System.out.println("COULD NOT REMOVE REFERENCE " + original + " FOR FIELD " + field
								+ " FOR ASSESSMENT " + curAss.getAssessmentID());
					else
						curAss.addReference(replacement, field);
				}
			}
		}

		String message = null;
		if (writeback.size() == 0)
			message = "<message></message>";
		else {
			System.out.println("Writing back assessments with replaced references; turning off verbose lock output.");
			FileLocker.impl.verboseOutput = false;
			AssessmentIOMessage ret = AssessmentIO.writeAssessments(new ArrayList<AssessmentData>(writeback.values()),
					username, vfs, true);
			FileLocker.impl.verboseOutput = true;

			for (AssessmentData cur : writeback.values())
				FileLocker.impl.persistentEagerRelease(cur.getAssessmentID(), cur.getType(), username);

			Row row = new Row();
			row.add(new CString("original_ref_id", original.getReferenceID()));
			row.add(new CString("changed_to_ref_id", replacement.getReferenceID()));

			InsertQuery insert = new InsertQuery("changed_references", row);
			ec.doUpdate(insert);

			message = "<message>" + XMLUtils.clean(ret.toHTML()) + "</message>";
		}

		return message;
	}

}
