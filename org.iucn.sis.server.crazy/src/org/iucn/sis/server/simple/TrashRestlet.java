package org.iucn.sis.server.simple;

import java.util.ArrayList;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.FilenameStriper;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFSPath;

public class TrashRestlet extends ServiceRestlet {

	public static boolean isRegional(String assessmentID) {
		if (assessmentID.indexOf("_") > -1) {
			return true;
		} else
			return false;
	}

	private String xml;

	public TrashRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	public void definePaths() {
		paths.add("/trash/{action}");
		paths.add("/trash/{action}/{option1}");

	}

	private String getRestoreUrl(String type, String status, String id) {

		String stripedAssID = FilenameStriper.getIDAsStripedPath(id);
		if (type.equals("TAXON")) {
			return "/browse/nodes/" + stripedAssID + ".xml";
		}

		if (status.equalsIgnoreCase("published"))
			return "/browse/assessments/" + stripedAssID + ".xml";

		String path = ServerPaths.getDraftAssessmentURL(id);

		// String stripedAssID = FilenameStriper.getIDAsStripedPath( id );
		String baseId = "";
		if (status.equalsIgnoreCase("draft"))
			baseId = "/drafts/";
		if (status.equalsIgnoreCase("draft_regional"))
			baseId = "/drafts/";

		// return baseId+stripedAssID+".xml";
		SysDebugger.getInstance().println("returning to...." + path);
		return path;
	}

	private String getTrashUrl(String type, String status, String id) {
		String stripedAssID = FilenameStriper.getIDAsStripedPath(id);
		System.out.println(type);

		if (type.equals("TAXON")) {
			return "/trash/nodes/" + stripedAssID + ".xml";
		}

		if (status.equalsIgnoreCase("published"))
			return "/trash/assessments/" + stripedAssID + ".xml";
		else
			return "/trash" + ServerPaths.getDraftAssessmentURL(id);
		
	}

	private void handleDelete(Request request, Response response, ExecutionContext ec) {
		try {
			Document doc = new DomRepresentation(request.getEntity()).getDocument();
			Element element = (Element) doc.getDocumentElement().getElementsByTagName("data").item(0);
			String id = element.getAttribute("id");
			String status = element.getAttribute("status");
			String type = element.getAttribute("type");

			VFSPath url = new VFSPath(getTrashUrl(type, status, id));
			SysDebugger.getInstance().println("Looking for " + type + " " + id + " in path " + url);
			if (!vfs.exists(url))
				throw new Exception("No assessment!");

			if (type.equals("TAXON")) {
				TaxonNode taxon = TaxaIO.readNode(id, vfs);
				vfs.delete(url);
				
				if( !vfs.exists(url) )
					TaxonomyDocUtils.removeTaxonFromHierarchy(taxon.getId(), taxon.getFootprint().length > 0 ? 
							taxon.getFootprint()[0] : taxon.getName(), taxon.getFullName());
			} else
				vfs.delete(url);
			
			if( !vfs.exists(url) ) {
				DeleteQuery dq = new DeleteQuery("trashLog", "ID", id);
				if (!status.equals("")) {
					QComparisonConstraint qc = new QComparisonConstraint(new CanonicalColumnName("trashLog", "STATUS"),
							QConstraint.CT_EQUALS, status);

					dq.constrain(qc);
				}
				SysDebugger.getInstance().println(dq.getSQL(ec.getDBSession()));
				ec.doUpdate(dq);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private void handleDeleteAll(Request request, Response response, final ExecutionContext ec) {
		try {
			SelectQuery sq = new SelectQuery();
			sq.select("trashLog", "*");
			ec.doQuery(sq, new RowProcessor() {
				public void process(Row row) {
					try {
						String status = row.get("STATUS").getString();
						String id = row.get("ID").getString();
						String type = row.get("TYPE").getString();

						String url = getTrashUrl(type, status, id);
						SysDebugger.getInstance().println("Looking for " + type + " " + id + " in path " + url);
						if (!vfs.exists(url))
							throw new Exception("No assessment!");

						vfs.delete(url);
						DeleteQuery dq = new DeleteQuery("trashLog", "ID", id);
						if (!status.equals("")) {
							QComparisonConstraint qc = new QComparisonConstraint(new CanonicalColumnName("trashLog",
									"STATUS"), QConstraint.CT_EQUALS, status);
							dq.constrain(qc);
						}
						SysDebugger.getInstance().println(dq.getSQL(ec.getDBSession()));
						ec.doUpdate(dq);
					} catch (Exception ignore) {
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleGet(Request request, Response response, ExecutionContext ec) {
		SelectQuery sq = new SelectQuery();
		sq.select("trashLog", "*");
		xml = "<trash>";
		try {
			ec.doQuery(sq, new RowProcessor() {
				public void process(Row row) {
					xml += "<data id=\"" + row.get("ID") + "\" type=\"" + row.get("TYPE") + "\" status=\""
							+ row.get("STATUS") + "\" user=\"" + row.get("USER") + "\" date=\"" + row.get("DATE")
							+ "\" node=\"" + row.get("NODE") + "\" display=\"" + row.get("DISPLAY") + "\"></data>";
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		xml += "</trash>";
		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.createDocumentFromString(xml)));

	}

	private void handleRestore(Request request, Response response, ExecutionContext ec) {
		try {
			String option = (String) request.getAttributes().get("option1");

			Document doc = new DomRepresentation(request.getEntity()).getDocument();;
			Element element = (Element) doc.getDocumentElement().getElementsByTagName("data").item(0);
			String id = element.getAttribute("id");
			String status = element.getAttribute("status");
			String type = element.getAttribute("type");
			String parent = element.getAttribute("parent");
			String node = element.getAttribute("node");

			String url = getTrashUrl(type, status, id);

			// String stripedAssID = FilenameStriper.getIDAsStripedPath(
			// assessmentID );
			SysDebugger.getInstance().println("Looking for " + type + " " + id + " in path " + url);
			/*
			 * if( vfs.exists( "/trash/assessments/" + stripedAssID + ".xml" ) )
			 * url = "/trash/assessments/" + stripedAssID + ".xml"; else if(
			 * vfs.exists( "/trash/assessments/" + stripedAssID ) ) url =
			 * "/trash/assessments/" + stripedAssID;
			 */

			if (!vfs.exists(url))
				throw new Exception("No Object to Restore.");

			String restoreURL = getRestoreUrl(type, status, id);
			System.out.println(restoreURL);
			
			if( status.startsWith("draft") ) {
				//PUT it to the Assessment restlet.
				String xml = DocumentUtils.getVFSFileAsString(url, vfs);
				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
				ndoc.parse(xml);
				AssessmentParser p = new AssessmentParser(ndoc);
				p.getAssessment().setAssessmentID("new");
				
				Request req = new Request(Method.PUT, "riap://application/assessments",
						new StringRepresentation(p.getAssessment().toXML(), MediaType.TEXT_XML,
							null, CharacterSet.UTF_8));
				req.setChallengeResponse(new ChallengeResponse(request.getChallengeResponse().getScheme(), 
						request.getChallengeResponse().getCredentials()));
				
				Response res = getContext().getClientDispatcher().handle(req);
				
				if( res.getStatus().isSuccess() )
					vfs.delete(url);
				else if( res.getStatus() == Status.CLIENT_ERROR_CONFLICT ) {
					response.setStatus(res.getStatus());
					throw new Exception("An assessment with this locality already exists. Cannot restore.");
				} else {
					response.setStatus(res.getStatus());
					throw new Exception("Server error. Cannot restore.");
				}
				
			} else {
				vfs.move(url, restoreURL);
			}
			
			DeleteQuery dq = new DeleteQuery("trashLog", "ID", id);
			// QConstraint = new QConstraint();
			// QComparisonConstraint qc = new QComparisonConstraint(new
			// CanonicalColumnName("trashLog", "ASSESSMENT"),
			// QConstraint.CT_EQUALS, assessmentID);
			if (!status.equals("")) {
				QComparisonConstraint qc = new QComparisonConstraint(new CanonicalColumnName("trashLog", "STATUS"),
						QConstraint.CT_EQUALS, status);
				dq.constrain(qc);
			}
			SysDebugger.getInstance().println(dq.getSQL(ec.getDBSession()));
			ec.doUpdate(dq);

			if (type.equals("TAXON")) {

				final Request req = new Request(Method.PUT, "riap://host/taxomatic/" + id);
				StringRepresentation sr = new StringRepresentation(DocumentUtils.getVFSFileAsString(getRestoreUrl(type,
						status, id), vfs), MediaType.TEXT_XML);
				sr.setCharacterSet(CharacterSet.UTF_8);
				req.setEntity(sr);
				Response resp = getContext().getClientDispatcher().handle(req);
				if (!(resp.getStatus()).isSuccess()) {
					System.out.println("Unable to restore Taxa");
				} else {
					System.out.println("Taxa Restored");
				}

				if (option != null && option.equals("true")) {
					// check for related assessments
					SelectQuery sq = new SelectQuery();
					sq.select("trashLog", "*");
					sq.constrain(new QComparisonConstraint(new CanonicalColumnName("trashLog", "NODE"),
							QConstraint.CT_EQUALS, id));
					sq.constrain(new QComparisonConstraint(new CanonicalColumnName("trashLog", "TYPE"),
							QConstraint.CT_EQUALS, "ASSESSMENT"));

					ec.doQuery(sq, new RowProcessor() {
						public void process(Row row) {
							String xml = "<trash>";
							xml += "<data id=\"" + row.get("ID").getString() + "\" parent=\""
									+ row.get("PARENT").getString() + "\" type=\"" + row.get("TYPE").getString()
									+ "\" status=\"" + row.get("STATUS").getString() + "\" user=\""
									+ row.get("USER").getString() + "\" date=\"" + row.get("DATE").getString()
									+ "\" node=\"" + row.get("NODE").getString() + "\"></data>";
							xml += "</trash>";
							final Request req = new Request(Method.POST, "riap://host/trash/restore/");
							req.setEntity(xml, MediaType.TEXT_XML);
							Response resp = getContext().getClientDispatcher().handle(req);
							if (!(resp.getStatus()).isSuccess()) {

								System.out.println("Unable to Restore Related Assessment");
							} else {
								System.out.println("Related Assessment Restored");
							}
							;
						}
					});
				}
			} else {
				if (status.equals("published")) {
					// restore assessment to taxa
					url = ServerPaths.getURLForTaxa(node);
					ArrayList<TaxonNode> nodelist = new ArrayList<TaxonNode>();

					SysDebugger.getInstance().println("Looking for taxon " + node + " in path " + url);
					System.out.println(url);
					if (!vfs.exists(url))
						throw new Exception("No Taxon!");

					TaxonNode taxonNode = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
							.getURLForTaxa(node), vfs), null, false);

					taxonNode.addAssessment(id);
					nodelist.add(taxonNode);

					TaxaIO.writeNodes(nodelist, vfs, true);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void performService(Request request, Response response) {
		try {
			ExecutionContext ec;
			ec = new SystemExecutionContext("default");
			ec.setExecutionLevel(ExecutionContext.ADMIN);

			if (((String) request.getAttributes().get("action")).equals("list"))
				handleGet(request, response, ec);
			if (((String) request.getAttributes().get("action")).equals("restore"))
				handleRestore(request, response, ec);
			if (((String) request.getAttributes().get("action")).equals("delete"))
				handleDelete(request, response, ec);
			if (((String) request.getAttributes().get("action")).equals("deleteall"))
				handleDeleteAll(request, response, ec);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
