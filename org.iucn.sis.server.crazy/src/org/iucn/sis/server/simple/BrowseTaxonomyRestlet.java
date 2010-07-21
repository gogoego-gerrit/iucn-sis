package org.iucn.sis.server.simple;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.taxa.TaxomaticHelper;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.ElementCollection;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;

public class BrowseTaxonomyRestlet extends ServiceRestlet {
	public static final String KINGDOM = "0";
	public static final String PHYLUM = "1";
	public static final String CLASS = "2";
	public static final String ORDER = "3";
	public static final String FAMILY = "4";
	public static final String GENUS = "5";
	public static final String SPECIES = "6";
	public static final String INFRARANK = "7";
	public static final String SUBPOPULATION = "8";
	public static final String INFRARANK_SUBPOPULATION = "9";
	public static final int KINGDOM_INT = 0;
	public static final int PHYLUM_INT = 1;
	public static final int CLASS_INT = 2;
	public static final int ORDER_INT = 3;
	public static final int FAMILY_INT = 4;
	public static final int GENUS_INT = 5;
	public static final int SPECIES_INT = 6;
	public static final int INFRARANK_INT = 7;
	public static final int SUBPOPULATION_INT = 8;
	public static final int INFRARANK_SUBPOPULATION_INT = 9;

	public static String getPublishedAssessments(String nodeID, VFS vfs) {
		try {
			Document doc = DocumentUtils.getVFSFileAsDocument(ServerPaths.getURLForTaxa(nodeID), vfs);
			String ids = doc.getDocumentElement().getElementsByTagName("assessments").item(0).getTextContent();
			return ids;
		} catch (NullPointerException e) {
			// Catches if there is no taxa, and/or if there's no assessments tag
			return "";
		}
	}

	public BrowseTaxonomyRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/browse/taxonomy/");
		paths.add("/browse/taxonomy/{hierarchy}");

		paths.add("/browse/hierarchy/{nodeID}");

		paths.add("/browse/children/{nodeID}");

		paths.add("/browse/nodes/{nodeID}");
		paths.add("/browse/taxonName/{kingdomName}/{nodeName}");
		paths.add("/browse/footprint/{nodeID}");
		paths.add("/browse/footprintIDs/{nodeID}");
		paths.add("/browse/lowestTaxa");

	}

	private void deleteTaxa(Request request, Response response, String nodeID) {

	}

	private void getFootprintOfIDsAndChildren(Response response, String id) {
		try {
			response.setEntity(TaxonomyDocUtils.getHierarchyAndChildrenByID(id), MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getFullFootprint(Request request, Response response, String ids) throws IOException {
		String[] taxaIDs;
		if( ids == null || ids.equals("") || ids.equalsIgnoreCase("null") )
			taxaIDs = request.getEntity().getText().split(",");
		else
			taxaIDs = ids.split(",");
		
		StringBuilder xml = new StringBuilder("<taxa>");

		for (int i = 0; i < taxaIDs.length; i++) {
			TaxonNode taxon = TaxaIO.readNode(taxaIDs[i], vfs);
			if( taxon != null ) {
				String name = taxon.getLevel() == TaxonNode.KINGDOM ? taxon.getName() : taxon.getFootprintAsString(0, ",") + "," + taxon.getName();
				xml.append("<species>");
				xml.append(name);
				xml.append("</species>\n");
			}
		}

		xml.append("</taxa>");
		response.setStatus(Status.SUCCESS_OK);
		
		//FIXME: Cannot return just the String for whatever bizarre reason - must be transformed 
		//to a Document first, even if we trip, then wrap everything in CDATA. Anyone know why?
		Document doc = DocumentUtils.createDocumentFromString(xml.toString());
		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
	}

	private void getFullFootprintOfIDs(Response response, String id) {
		try {
			String xml = id;
			Document doc = DocumentUtils.getVFSFileAsDocument(ServerPaths.getURLForTaxa(id), vfs);
			boolean moreIdsToGet = true;
			while (moreIdsToGet) {
				String tempID = doc.getDocumentElement().getAttribute("parentid");
				SysDebugger.getInstance().println("This is temp id " + tempID);
				if (tempID.trim().equalsIgnoreCase("")) {
					moreIdsToGet = false;
				} else {
					xml = tempID + "," + xml;
					doc = DocumentUtils.getVFSFileAsDocument(ServerPaths.getURLForTaxa(tempID), vfs);
				}
			}
			xml = "<ids>" + xml + "</ids>\r\n";
			SysDebugger.getInstance().println("I am returning xml " + xml);
			response.setEntity(xml, MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);

		} catch (Exception e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Override
	public void performService(Request request, Response response) {
		String hierarchy = (String) request.getAttributes().get("hierarchy");
		String nodeID = (String) request.getAttributes().get("nodeID");
		String kingdomName = (String) request.getAttributes().get("kingdomName");
		String nodeName = (String) request.getAttributes().get("nodeName");

		try {
			if (request.getResourceRef().getPath().startsWith("/browse/nodes/")) {
				if (request.getMethod() == Method.PUT)
					putInfo(request, response, nodeID);
				else if (request.getMethod() == Method.DELETE)
					deleteTaxa(request, response, nodeID);
				else
					serveInfo(request, response, nodeID);
			} else if (request.getResourceRef().getPath().startsWith("/browse/taxonName/"))
				serveByName(request, response, kingdomName, nodeName);
			else if (request.getResourceRef().getPath().startsWith("/browse/children/"))
				serveChildrenByID(request, response, nodeID);
			else if (request.getResourceRef().getPath().startsWith("/browse/taxonomy/"))
				serveBrowsing(response, hierarchy);
			else if (request.getResourceRef().getPath().startsWith("/browse/lowestTaxa"))
				serveLowestTaxa(request, response);
			else if (request.getResourceRef().getPath().startsWith("/browse/footprint/"))
				getFullFootprint(request, response, nodeID);
			else if (request.getResourceRef().getPath().startsWith("/browse/footprintIDs/"))
				getFullFootprintOfIDs(response, nodeID);
			else if (request.getResourceRef().getPath().startsWith("/browse/hierarchy/"))
				getFootprintOfIDsAndChildren(response, nodeID);
			else {
				SysDebugger.getInstance().println("This is a bad request: " + request.getResourceRef().getPath());
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}	
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void putInfo(Request request, Response response, String nodeID) {
		SysDebugger.getInstance().println("i am in put info with id  " + nodeID);
		if (nodeID != null) {
			ArrayList<TaxonNode> nodesToSave = new ArrayList<TaxonNode>();
			ArrayList<AssessmentData> assessmentsToSave = new ArrayList<AssessmentData>();

			String url = ServerPaths.getURLForTaxa(nodeID);
			try {
				String nodeString = request.getEntity().getText();

				NativeDocument newDoc = SISContainerApp.newNativeDocument(request.getChallengeResponse());
				newDoc.parse(nodeString);
				TaxonNode newNode = TaxonNodeFactory.createNode(newDoc);

				nodesToSave.add(newNode);

				if (vfs.exists(url)) {
					// Check to see if name changed. If not, save it. If so,
					// change children.
					NativeDocument oldDoc = SISContainerApp.newNativeDocument(request.getChallengeResponse());
					oldDoc.parse(DocumentUtils.getVFSFileAsString(ServerPaths.getURLForTaxa(nodeID), vfs));
					TaxonNode oldNode = TaxonNodeFactory.createNode(oldDoc);

					if (!newNode.getFullName().equalsIgnoreCase(oldNode.getFullName())) {
						String id = TaxonomyDocUtils.getIDByName(newNode.getKingdomName(), newNode.getFullName());
						if( id != null && !id.equalsIgnoreCase(newNode.getId()+"") ) {
							response.setStatus(Status.CLIENT_ERROR_CONFLICT);
							return;
						} else {
							nodesToSave.addAll(TaxomaticHelper.taxonNameChanged(newNode, oldNode, vfs));
							assessmentsToSave.addAll(TaxomaticHelper.updateAssessmentTaxonName(newNode, oldNode, vfs));
						}
					}
				}

				//Best effort to rename the assessments.
				String user = "taxonAutoRename";
				if( request.getChallengeResponse() != null && request.getChallengeResponse().getIdentifier() != null )
					user = request.getChallengeResponse().getIdentifier();
				AssessmentIO.writeAssessments(assessmentsToSave, user, vfs, false);
				
				String ret = TaxaIO.writeNodes(nodesToSave, vfs, true);
				if (ret != null) {
					response.setStatus(Status.SUCCESS_OK);
					response.setEntity(ret, MediaType.TEXT_XML);
				} else {
					response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				}

			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}

	private void serveBrowsing(Response response, String hierarchy) throws NotFoundException {
		response.setEntity(TaxonomyDocUtils.getHierarchyXML(hierarchy), MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_OK);
	}

	private void serveByName(Request request, Response response, String kingdomName, String nodeName) {
		try {
			String id = TaxonomyDocUtils.getIDByName(kingdomName, URLDecoder.decode(nodeName, "UTF-8"));
			SysDebugger.getInstance().println("Trying to fetch node by id " + id);
			serveInfo(request, response, id);
		} catch (Exception e) {
			e.printStackTrace();
			SysDebugger.getInstance().println("Node name " + nodeName + " is not found.");
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Node name " + nodeName + " is not found.");
		}
	}

	private void serveChildrenByID(Request request, Response response, String nodeID) {
		String ids = "";

		try {
			ids = TaxonomyDocUtils.getChildrenTaxaByID(nodeID);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}

		if (!ids.equals("")) {
			serveInfo(request, response, ids);
			try {
				SysDebugger.getInstance().println("----");
				SysDebugger.getInstance().println(response.getEntity().getText());
				SysDebugger.getInstance().println("----");
			} catch (Exception e) {
			}
		} else {
			response.setEntity("<empty></empty>", MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);
		}
	}

	private void serveInfo(Request request, Response response, String nodeID) {
		if (nodeID != null) {
			if (nodeID.equalsIgnoreCase("list")) {
				try {

					Document doc = new DomRepresentation(request.getEntity()).getDocument();
					ElementCollection list = new ElementCollection(doc.getElementsByTagName("id"));

					StringBuilder retXML = new StringBuilder("<nodes>\r\n");

					for (Element curEl : list) {
						String url = ServerPaths.getURLForTaxa(curEl.getTextContent());
						String nodeXML = DocumentUtils.getVFSFileAsString(url, vfs);

						if (nodeXML != null) {
							nodeXML = nodeXML.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
							retXML.append(nodeXML);
						} else
							SysDebugger.getNamedInstance("error").println(
									"Request for taxon with " + "id " + curEl.getTextContent()
											+ " failed. Taxon was not found.");
					}

					retXML.append("</nodes>");
					response.setEntity(retXML.toString(), MediaType.TEXT_XML);
					response.getEntity().setCharacterSet(CharacterSet.UTF_8);
					response.setStatus(Status.SUCCESS_OK);
				} catch (IOException e) {
					e.printStackTrace();
					response.setEntity("Unprocessable entity: " + request.getEntityAsText(), MediaType.TEXT_PLAIN);
					response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
				}
			} else if (nodeID.contains(",")) {
				String[] list = nodeID.split(",");
				String retXML = "<nodes>\r\n";

				for (int i = 0; i < list.length; i++) {
					String url = ServerPaths.getURLForTaxa(list[i]);
					String nodeXML = DocumentUtils.getVFSFileAsString(url, vfs);

					if (nodeXML != null) {
						nodeXML = nodeXML.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
						retXML += nodeXML;
					} else
						SysDebugger.getNamedInstance("error").println(
								"Request for taxon with " + "id " + list[i] + " failed. Taxon was not found.");
				}

				retXML += "</nodes>";
				response.setEntity(retXML, MediaType.TEXT_XML);
				response.getEntity().setCharacterSet(CharacterSet.UTF_8);
				response.setStatus(Status.SUCCESS_OK);

			} else {
				String url = ServerPaths.getURLForTaxa(nodeID);
				Document vfsDoc = DocumentUtils.getVFSFileAsDocument(url, vfs);
				if (vfsDoc == null) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return;
				}

				Representation doc = new DomRepresentation(MediaType.TEXT_XML, vfsDoc);
				doc.setMediaType(MediaType.TEXT_XML);
				doc.setCharacterSet(CharacterSet.UTF_8);
				response.setEntity(doc);
				response.setStatus(Status.SUCCESS_OK);
			}
		} else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}

	private void serveLowestTaxa(Request request, Response response) throws IOException {

		Document idsDoc = new DomRepresentation(request.getEntity()).getDocument();
		ElementCollection idsList = new ElementCollection(idsDoc.getElementsByTagName("id"));
		ArrayList<String> idsLeftToCheck = new ArrayList<String>();

		for (Element curCode : idsList) {
			idsLeftToCheck.add(curCode.getTextContent());
		}

		StringBuffer idsToFetch = new StringBuffer();
		while (idsLeftToCheck.size() != 0) {
			String idToCheck = idsLeftToCheck.remove(0);
			String idLevel = taxaLevel(idToCheck);
			SysDebugger.getInstance().println("This is id " + idToCheck + " and this is the level " + idLevel);

			if (idLevel.equalsIgnoreCase(INFRARANK) || idLevel.equalsIgnoreCase(SUBPOPULATION)) {
				idsToFetch.append(idToCheck + ",");
			}
			// MAKE SURE THAT IT DOES EXIST
			else if (!idLevel.equalsIgnoreCase("-1")) {
				if (idLevel.equalsIgnoreCase(SPECIES)) {
					idsToFetch.append(idToCheck + ",");
				}
				String[] children = TaxonomyDocUtils.getChildrenTaxaByID(idToCheck).split(",");
				for (int i = 0; i < children.length; i++) {
					if (!children[i].equals("")) {
						idsLeftToCheck.add(children[i]);
						SysDebugger.getInstance().println("adding to idsLeftToCheck " + children[i]);
					}
				}
			}

		}
		SysDebugger.getInstance().println("these are the ids we are going to fetch " + idsToFetch.toString());

		if (idsToFetch.length() > 0) {
			serveInfo(request, response, idsToFetch.substring(0, idsToFetch.length() - 1));
		} else {
			response.setEntity("<empty></empty>", MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);
		}

	}

	/**
	 * Returns level, or -1 if not found
	 * 
	 * @param nodeID
	 *            -- id of the taxa
	 * @return
	 */
	private String taxaLevel(String nodeID) {
		String level = "-1";
		String url = ServerPaths.getURLForTaxa(nodeID);
		try {
			Document doc = DocumentUtils.getVFSFileAsDocument(url, vfs);
			level = doc.getDocumentElement().getAttribute("level");
		} catch (Exception e) {
		}

		return level;
	}

}
