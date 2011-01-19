package org.iucn.sis.server.restlets.taxa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.util.Triple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.ElementCollection;

public class TaxonRestlet extends BaseServiceRestlet {

	public TaxonRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/browse/{action}");
		paths.add("/browse/{action}/{first}");
		paths.add("/browse/{action}/{first}/{second}");
	}
	
	private Triple<String, String, String> getParameters(Request request) {
		String action = (String) request.getAttributes().get("action");
		String first = (String) request.getAttributes().get("first");
		String second = (String) request.getAttributes().get("second");
		
		return new Triple<String, String, String>(action, first, second);
	}
	
	private Integer parseIdentifier(String identifier) throws ResourceException {
		try {
			return Integer.valueOf(identifier);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		Triple<String, String, String> parameters = getParameters(request);

		final String action = parameters.getFirst();
		final Representation representation;
		if (action.equalsIgnoreCase("hierarchy"))
			representation = getFootprintOfIDsAndChildren(parseIdentifier(parameters.getSecond()));
		else if (action.equalsIgnoreCase("children"))
			representation = serveChildrenByID(parseIdentifier(parameters.getSecond()));
		else if (action.equalsIgnoreCase("nodes"))
			representation = serveInfo(parameters.getSecond());
		else if (action.equalsIgnoreCase("taxonName"))
			representation = serveByName(parameters.getSecond(), parameters.getThird());
		else if (action.equalsIgnoreCase("taxonomy"))
			representation = serveBrowsing(parameters.getSecond());
		else if (action.equalsIgnoreCase("footprint")) {
			representation = getFullFootprint(parameters.getSecond());
		}
		else if (action.equalsIgnoreCase("footprintIDs"))
			representation = getFullFootprintOfIDs(response, parseIdentifier(parameters.getSecond()));
		else if (action.equalsIgnoreCase("workingSets"))
			representation = fetchWorkingSetsForTaxon(response, parseIdentifier(parameters.getSecond()));
		else
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		
		return representation;
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		Triple<String, String, String> parameters = getParameters(request);
		
		final String action = parameters.getFirst();
		if (action.equalsIgnoreCase("nodes")) {
			if ("list".equals(parameters.getSecond())) {
				Document doc = getEntityAsDocument(entity);
				ElementCollection list = new ElementCollection(doc.getElementsByTagName("id"));
	
				StringBuilder retXML = new StringBuilder("<nodes>");
				List<Integer> ids = new ArrayList<Integer>();
				for (Element curEl : list)
					ids.add(parseIdentifier(curEl.getTextContent()));
					
				for (Taxon taxon : SIS.get().getTaxonIO().getTaxa(ids.toArray(new Integer[ids.size()]), false))
					retXML.append(taxon.toXML());
				
				retXML.append("</nodes>");
				
				response.setEntity(retXML.toString(), MediaType.TEXT_XML);
				response.getEntity().setCharacterSet(CharacterSet.UTF_8);
				response.setStatus(Status.SUCCESS_OK);
			}
			else if ("references".equals(parameters.getThird())) {
				updateReferences(entity, parseIdentifier(parameters.getSecond()), request, response);
			}
		}
		else if (action.equalsIgnoreCase("lowestTaxa"))
			serveLowestTaxa(response, entity);
		else if (action.equalsIgnoreCase("footprint")) {
			String values = parameters.getSecond();
			if (values != null)
				Debug.println("WARNING: TaxonRestlet -- You should be GET instead of POST here.");
			else {
				try {
					values = entity.getText();
				} catch (Exception e ) {
					throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
				}
			}
			response.setEntity(getFullFootprint(values));
		}
	}
	
	@Override
	public void handlePut(Representation entity, Request request,
			Response response) throws ResourceException {
		Triple<String, String, String> parameters = getParameters(request);
		
		final String action = parameters.getFirst();
		if (action.equalsIgnoreCase("nodes"))
			updateTaxon(request, response, parseIdentifier(parameters.getSecond()));
	}
	
	private void updateReferences(Representation entity, Integer taxonID, Request request, Response response) throws ResourceException {
		Taxon taxon = SIS.get().getTaxonIO().getTaxonNonLazy(taxonID);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		Document document = getEntityAsDocument(entity);
		
		taxon.getReference().clear();
		for (Element el : new ElementCollection(document.getDocumentElement().getElementsByTagName("reference"))) {
			try {
				Reference reference = 
					SIS.get().getManager().getObject(Reference.class, Integer.valueOf(el.getAttribute("id")));
				if (reference.getTaxon() == null)
					reference.setTaxon(new HashSet<Taxon>());
				reference.getTaxon().add(taxon);
				taxon.getReference().add(reference);
			} catch (PersistentException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No reference with the ID " + el.getAttribute("id") + " was found.");
			}
		}
		
		try {
			SIS.get().getTaxonIO().writeTaxon(taxon, SIS.get().getUser(request));
		} catch (TaxomaticException e) {
			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	private Representation getFootprintOfIDsAndChildren(Integer id) {
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(id);
		
		return new StringRepresentation(taxon.getXMLofFootprintAndChildren(), MediaType.TEXT_XML);
	}

	private Representation getFullFootprint(String ids) throws ResourceException {
		String[] taxaIDs = ids.split(",");

		StringBuilder xml = new StringBuilder("<taxa>");

		for (int i = 0; i < taxaIDs.length; i++) {
			Taxon taxon = SIS.get().getTaxonIO().getTaxon(parseIdentifier(taxaIDs[i]));
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

		// FIXME: Cannot return just the String for whatever bizarre reason -
		// must be transformed
		// to a Document first, even if we trip, then wrap everything in CDATA.
		// Anyone know why?
		Document doc = DocumentUtils.createDocumentFromString(xml.toString());
		return new DomRepresentation(MediaType.TEXT_XML, doc);
	}

	private Representation getFullFootprintOfIDs(Response response, Integer id) throws ResourceException {
		final Taxon taxon = SIS.get().getTaxonIO().getTaxon(id);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		return new StringRepresentation("<ids>" + taxon.getIDFootprintAsString(0, ",") + "</ids>", 
				MediaType.TEXT_XML);
	}	

	private void updateTaxon(Request request, Response response, Integer nodeID) throws ResourceException {
		if (nodeID != null) {
			ArrayList<Taxon> nodesToSave = new ArrayList<Taxon>();
			User user = SIS.get().getUser(request);
			
			NativeDocument newDoc = new JavaNativeDocument();
			try {
				newDoc.parse(request.getEntity().getText());
			} catch (Exception e) {
				throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
			}
				
			Taxon newNode = Taxon.fromXML(newDoc);
			try {
				newNode = SIS.get().getManager().mergeObject(newNode);
			} catch (PersistentException e) {
				Debug.println(e);
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			nodesToSave.add(newNode);
			try {
				SIS.get().getTaxonIO().writeTaxa(nodesToSave, user, true);
			} catch (TaxomaticException e) {
				throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e);
			}
				
			StringBuilder xml = new StringBuilder();
			xml.append("<nodes>");
			for (Taxon taxon : nodesToSave) {
				xml.append(taxon.getId() + ",");
			}
			xml.append("</nodes>");
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(xml.toString(), MediaType.TEXT_XML);
			
		} else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}

	private Representation serveBrowsing(String hierarchy) throws ResourceException {
		Taxon taxon = null;

		if (hierarchy != null) {
			if (hierarchy.indexOf("-") < 0) {
				taxon = SIS.get().getTaxonIO().getTaxon(parseIdentifier(hierarchy));
			} else {
				String[] split = hierarchy.split("-");
				taxon = SIS.get().getTaxonIO().getTaxon(parseIdentifier(split[split.length - 1]));
			}
		}

		return new StringRepresentation(getHierarchyFootprintXML(taxon), MediaType.TEXT_XML);
	}

	private String getHierarchyFootprintXML(Taxon root) {
		String xml = "<hierarchy>\r\n";
		xml += "<footprint>" + (root == null ? "" : root.getIDFootprintAsString(0, "-")) + "</footprint>\r\n";

		xml += "<options>\r\n";
		if (root != null)
			for (Taxon child : root.getChildren()) {
				if (Taxon.ACTIVE == child.getState())
					xml += "<option>" + child.getId() + "</option>\r\n";
			}
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

	private Representation serveByName(String kingdomName, String fullName) throws ResourceException {
		Taxon taxon = SIS.get().getTaxonIO().readTaxonByName(kingdomName, fullName);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Node name " + fullName + " is not found.");
		
		return serveInfo(taxon.getId() + "");
	}

	private Representation serveChildrenByID(Integer nodeID) throws ResourceException {
		final Taxon taxon = SIS.get().getTaxonIO().getTaxon(nodeID);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		String ids = taxon.getChildrenCSV();
		if ("".equals(ids))
			return new StringRepresentation("<empty></empty>", MediaType.TEXT_XML); 
		else
			return serveInfo(ids);
	}

	private Representation serveInfo(String nodeID) throws ResourceException {
		if (nodeID == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		Representation representation;
		if (nodeID.contains(",")) {
			String[] list = nodeID.split(",");
			StringBuilder retXML = new StringBuilder("<nodes>\r\n");

			for (int i = 0; i < list.length; i++) {
				Taxon taxon = SIS.get().getTaxonIO().getTaxon(parseIdentifier(list[i]));
				if (taxon != null)
					retXML.append(taxon.toXML());
			}
			retXML.append("</nodes>");

			representation = new StringRepresentation(retXML.toString(), MediaType.TEXT_XML);

		} else {
			Taxon taxon = SIS.get().getTaxonIO().getTaxon(parseIdentifier(nodeID));
			if (taxon == null)
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
									
			StringBuilder retXML = new StringBuilder("<nodes>\r\n");
			retXML.append(taxon.toXML());
			retXML.append("</nodes>");

			representation = new StringRepresentation(retXML.toString(), MediaType.TEXT_XML);
		}
		
		representation.setCharacterSet(CharacterSet.UTF_8);
		return representation;
	}

	private void serveLowestTaxa(Response response, Representation entity) throws ResourceException {
		Document idsDoc = getEntityAsDocument(entity);
		ElementCollection idsList = new ElementCollection(idsDoc.getElementsByTagName("id"));
		ArrayList<Taxon> idsLeftToCheck = new ArrayList<Taxon>();

		for (Element curCode : idsList) {
			idsLeftToCheck.add(SIS.get().getTaxonIO().getTaxon(parseIdentifier(curCode.getTextContent())));
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
		
		final Representation representation;
		if (idsToFetch.length() > 0)
			representation = serveInfo(idsToFetch.substring(0, idsToFetch.length() - 1));
		else
			representation = new StringRepresentation("<empty></empty>", MediaType.TEXT_XML);
		
		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(representation);
	}
	
	private Representation fetchWorkingSetsForTaxon(Response response, Integer taxonID) throws ResourceException {
		Representation representation;
		try {
			
			WorkingSet[] sets = SIS.get().getWorkingSetIO().getWorkingSetsForTaxon(taxonID);
			StringBuilder xml = new StringBuilder("<xml>\r\n");

			for (WorkingSet set : sets) {
				xml.append(set.toXMLMinimal());
			}

			xml.append("</xml>");
				
			representation = new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
			representation.setCharacterSet(CharacterSet.UTF_8);
		
			//Automatically assumes success when an entity is returned
			return representation;
		} catch (PersistentException e) {
			Debug.println(e);
			/*
			 * If you want to return the error to the client, 
			 * use this pattern.  Then, unwrap the entity via 
			 * ClientDocumentUtils.parseStatus
			 */
			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createErrorDocument(e.getMessage())));
			
			/*
			 * Throws an exception that's handled up the chain, and 
			 * uses the given status as the error status, so the client 
			 * knows something went wrong.
			 */
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
		}
	}

}
