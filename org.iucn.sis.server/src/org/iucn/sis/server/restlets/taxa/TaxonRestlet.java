package org.iucn.sis.server.restlets.taxa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.ElementCollection;
import com.solertium.vfs.NotFoundException;

public class TaxonRestlet extends ServiceRestlet {

	public TaxonRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	/**
	 * DELETES a taxa
	 * 
	 * @param request
	 * @param response
	 * @param nodeID
	 */
	private void deleteTaxa(Request request, Response response, Integer nodeID) {
		// SHOULD BE DONE FROM TAXOMATICRESTLET`

		// Taxon taxon = SIS.get().getTaxonIO().getTaxon(nodeID);
		// if (taxon != null) {
		// if (SIS.get().getTaxonIO().deleteTaxon(taxon,
		// SIS.get().getUser(request))) {
		// response.setEntity("Taxa has been deleted", MediaType.TEXT_PLAIN);
		// response.setStatus(Status.SUCCESS_OK);
		// } else {
		// response.setEntity("Taxa was unable to be deleted",
		// MediaType.TEXT_PLAIN);
		// response.setStatus(Status.SERVER_ERROR_INTERNAL);
		// }
		// } else {
		// response.setEntity("Taxon does not exist", MediaType.TEXT_PLAIN);
		// response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		// }

	}

	/**
	 * 
	 * @param response
	 * @param id
	 */
	private void getFootprintOfIDsAndChildren(Response response, Integer id) {
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(id);
		response.setEntity(taxon.getXMLofFootprintAndChildren(), MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_OK);
	}

	private void getFullFootprint(Request request, Response response, String ids) throws IOException {
		String[] taxaIDs;
		if (ids == null || ids.equals("") || ids.equalsIgnoreCase("null"))
			taxaIDs = request.getEntity().getText().split(",");
		else
			taxaIDs = ids.split(",");

		StringBuilder xml = new StringBuilder("<taxa>");

		for (int i = 0; i < taxaIDs.length; i++) {
			Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(taxaIDs[i]));
			if (taxon != null) {
				String name = taxon.getLevel() == TaxonLevel.KINGDOM ? taxon.getName() : taxon.getFootprintAsString(0,
						",")
						+ "," + taxon.getName();
				xml.append("<species>");
				xml.append(name);
				xml.append("</species>\n");
			}
		}

		xml.append("</taxa>");
		response.setStatus(Status.SUCCESS_OK);

		// FIXME: Cannot return just the String for whatever bizarre reason -
		// must be transformed
		// to a Document first, even if we trip, then wrap everything in CDATA.
		// Anyone know why?
		Document doc = DocumentUtils.createDocumentFromString(xml.toString());
		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, doc));
	}

	private void getFullFootprintOfIDs(Response response, Integer id) {
		try {
			String xml = "<ids>" + SIS.get().getTaxonIO().getTaxon(id).getIDFootprintAsString(0, ",") + "</ids>\r\n";
			response.setEntity(xml, MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);

		} catch (NullPointerException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Override
	public void definePaths() {
		paths.add("/browse/{action}");
		paths.add("/browse/{action}/{first}");
		paths.add("/browse/{action}/{first}/{second}");
	}

	@Override
	public void performService(Request request, Response response) {
		String action = (String) request.getAttributes().get("action");
		String first = (String) request.getAttributes().get("first");
		String second = (String) request.getAttributes().get("second");

		try {
			if (action.equalsIgnoreCase("nodes")) {

				if (request.getMethod() == Method.PUT)
					updateTaxon(request, response, Integer.valueOf(first));
				else if (request.getMethod() == Method.DELETE) {
					deleteTaxa(request, response, Integer.valueOf(first));
				} else
					serveInfo(request, response, first);
			} else if (action.equalsIgnoreCase("taxonName"))
				serveByName(request, response, first, second);
			else if (action.equalsIgnoreCase("children"))
				serveChildrenByID(request, response, Integer.valueOf(first));
			else if (action.equalsIgnoreCase("taxonomy"))
				serveBrowsing(response, first);
			else if (action.equalsIgnoreCase("lowestTaxa"))
				serveLowestTaxa(request, response);
			else if (action.equalsIgnoreCase("footprint"))
				getFullFootprint(request, response, first);
			else if (action.equalsIgnoreCase("footprintIDs"))
				getFullFootprintOfIDs(response, Integer.valueOf(first));
			else if (action.equalsIgnoreCase("hierarchy"))
				getFootprintOfIDsAndChildren(response, Integer.valueOf(first));
			else {
				Debug.println("This is a bad request: " + request.getResourceRef().getPath());
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void updateTaxon(Request request, Response response, Integer nodeID) {
		if (nodeID != null) {
			ArrayList<Taxon> nodesToSave = new ArrayList<Taxon>();
			try {
				User user = SIS.get().getUser(request);
				String nodeString = request.getEntity().getText();
				NativeDocument newDoc = SIS.get().newNativeDocument(request.getChallengeResponse());
				newDoc.parse(nodeString);
				Taxon newNode = Taxon.fromXML(newDoc);
//				Set<Edit> edits = new HashSet<Edit>();
//				for (Edit edit : newNode.getEdits()) {
//					System.out.println("looking at edit " + edit.getId());
//					if (edit.getId() != 0) {
//						System.out.println("getting edit from session");
//						Edit editted =(Edit) SISPersistentManager.instance().getSession().get(edit.getClass(), edit.getId());
//						if (editted != null)
//							SISPersistentManager.instance().getSession().evict(editted);
////						SIS.get().getEditIO().get(edit.getId());
////						edits.add((Edit) SISPersistentManager.instance().getSession().merge(edit));
//					} else {
////						edits.add(edit);
//					}
//				}
//				newNode.setEdits(edits);
				newNode = (Taxon) SIS.get().getManager().getSession().merge(newNode);
				nodesToSave.add(newNode);
				boolean success = SIS.get().getTaxonIO().writeTaxa(nodesToSave, user, true);
				if (success) {
					StringBuilder xml = new StringBuilder();
					xml.append("<nodes>");
					for (Taxon taxon : nodesToSave) {
						xml.append(taxon.getId() + ",");
					}
					xml.append("</nodes>");
					response.setStatus(Status.SUCCESS_OK);
					response.setEntity(xml.toString(), MediaType.TEXT_XML);
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
		Taxon yeah = null;

		if (hierarchy != null) {
			if (hierarchy.indexOf("-") < 0) {
				yeah = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(hierarchy));

				response.setEntity(getHierarchyFootprintXML(yeah), MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			} else {
				String[] split = hierarchy.split("-");
				yeah = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(split[split.length - 1]));
			}
		}

		response.setEntity(getHierarchyFootprintXML(yeah), MediaType.TEXT_XML);
		response.setStatus(Status.SUCCESS_OK);
	}

	private String getHierarchyFootprintXML(Taxon root) {
		String xml = "<hierarchy>\r\n";
		xml += "<footprint>" + (root == null ? "" : root.getIDFootprintAsString(0, "-")) + "</footprint>\r\n";

		xml += "<options>\r\n";
		if (root != null)
			for (Taxon child : root.getChildren())
				xml += "<option>" + child.getId() + "</option>\r\n";
		else {
			xml += "<option>" + SIS.get().getTaxonIO().readTaxonByName("ANIMALIA", "ANIMALIA").getId()
					+ "</option>\r\n";
			xml += "<option>" + SIS.get().getTaxonIO().readTaxonByName("PLANTAE", "PLANTAE").getId() + "</option>\r\n";
			xml += "<option>" + SIS.get().getTaxonIO().readTaxonByName("PROTISTA", "PROTISTA").getId()
					+ "</option>\r\n";
			xml += "<option>" + SIS.get().getTaxonIO().readTaxonByName("FUNGI", "FUNGI").getId() + "</option>\r\n";
		}
		xml += "</options>\r\n";

		xml += "</hierarchy>";

		return xml;
	}

	private void serveByName(Request request, Response response, String kingdomName, String fullName) {
		Taxon taxon = SIS.get().getTaxonIO().readTaxonByName(kingdomName, fullName);
		if (taxon == null) {
			Debug.println("Node name {0} is not found.", fullName);
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Node name " + fullName + " is not found.");
		} else {
			// System.out.println("Trying to fetch node by id " +
			// taxon.getId());
			serveInfo(request, response, taxon.getId() + "");
		}
	}

	private void serveChildrenByID(Request request, Response response, Integer nodeID) {

		Taxon taxon = SIS.get().getTaxonIO().getTaxon(nodeID);
		if (taxon == null) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		} else {
			String ids = taxon.getChildrenCSV();
			if (!ids.equals("")) {
				serveInfo(request, response, ids);
				// try {
				// System.out.println("----");
				// System.out.println(response.getEntity().getText());
				// System.out.println("----");
				// } catch (Exception e) {
				// }
			} else {
				response.setEntity("<empty></empty>", MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			}
		}
	}

	private void serveInfo(Request request, Response response, String nodeID) {
		if (nodeID != null) {
			if (nodeID.equalsIgnoreCase("list")) {
				try {

					Document doc = new DomRepresentation(request.getEntity()).getDocument();
					ElementCollection list = new ElementCollection(doc.getElementsByTagName("id"));

					StringBuilder retXML = new StringBuilder("<nodes>");

					for (Element curEl : list) {
						retXML.append(SIS.get().getTaxonIO().getTaxonXML(Integer.valueOf(curEl.getTextContent())));
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
				StringBuilder retXML = new StringBuilder("<nodes>\r\n");

				for (int i = 0; i < list.length; i++) {
					Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(list[i]));
					if (taxon != null)
						retXML.append(taxon.toXML());
				}
				retXML.append("</nodes>");
				response.setEntity(retXML.toString(), MediaType.TEXT_XML);
				response.getEntity().setCharacterSet(CharacterSet.UTF_8);
				response.setStatus(Status.SUCCESS_OK);

			} else {
				Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(nodeID));
				if (taxon == null) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return;
				}
				StringBuilder retXML = new StringBuilder("<nodes>\r\n");
				retXML.append(taxon.toXML());
				retXML.append("</nodes>");

				response.setEntity(retXML.toString(), MediaType.TEXT_XML);
				response.setStatus(Status.SUCCESS_OK);
			}
		} else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}

	private void serveLowestTaxa(Request request, Response response) throws IOException {

		Document idsDoc = new DomRepresentation(request.getEntity()).getDocument();
		ElementCollection idsList = new ElementCollection(idsDoc.getElementsByTagName("id"));
		ArrayList<Taxon> idsLeftToCheck = new ArrayList<Taxon>();

		for (Element curCode : idsList) {
			idsLeftToCheck.add(SIS.get().getTaxonIO().getTaxon(Integer.valueOf(curCode.getTextContent())));
		}

		StringBuffer idsToFetch = new StringBuffer();
		while (idsLeftToCheck.size() != 0) {
			Taxon cur = idsLeftToCheck.remove(0);

			if (cur.getLevel() == TaxonLevel.INFRARANK || cur.getLevel() == TaxonLevel.SUBPOPULATION) {
				idsToFetch.append(cur.getId() + ",");
			} else {
				if (cur.getLevel() == TaxonLevel.SPECIES) {
					idsToFetch.append(cur.getId() + ",");
				}
				Set<Taxon> children = cur.getChildren();
				for (Taxon curChild : children)
					idsLeftToCheck.add(curChild);
			}
		}
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
		return SIS.get().getTaxonIO().getTaxon(Integer.valueOf(nodeID)).getLevel() + "";
	}

}
