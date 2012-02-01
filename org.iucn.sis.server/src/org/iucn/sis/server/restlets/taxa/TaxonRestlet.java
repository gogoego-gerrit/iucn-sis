package org.iucn.sis.server.restlets.taxa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonHierarchy;
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
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		Triple<String, String, String> parameters = getParameters(request);

		TaxonIO taxonIO = new TaxonIO(session);
		
		final String action = parameters.getFirst();
		final Representation representation;
		if (action.equalsIgnoreCase("hierarchy"))
			representation = getFootprintOfIDsAndChildren(parseIdentifier(parameters.getSecond()), taxonIO);
		else if (action.equalsIgnoreCase("children"))
			representation = serveChildrenByID(parseIdentifier(parameters.getSecond()), taxonIO);
		else if (action.equalsIgnoreCase("nodes"))
			representation = serveInfo(parameters.getSecond(), taxonIO);
		else if (action.equalsIgnoreCase("taxonName"))
			representation = serveByName(parameters.getSecond(), parameters.getThird(), taxonIO);
		else if (action.equalsIgnoreCase("taxonomy"))
			representation = serveBrowsing(parameters.getSecond(), taxonIO, session);
		else if (action.equalsIgnoreCase("footprint")) {
			representation = getFullFootprint(parameters.getSecond(), taxonIO);
		}
		else if (action.equalsIgnoreCase("footprintIDs"))
			representation = getFullFootprintOfIDs(response, parseIdentifier(parameters.getSecond()), taxonIO);
		else if (action.equalsIgnoreCase("workingSets"))
			representation = fetchWorkingSetsForTaxon(response, parseIdentifier(parameters.getSecond()), session);
		else
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		
		return representation;
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		Triple<String, String, String> parameters = getParameters(request);
		TaxonIO taxonIO = new TaxonIO(session);
		final String action = parameters.getFirst();
		if (action.equalsIgnoreCase("nodes")) {
			if ("list".equals(parameters.getSecond())) {
				Document doc = getEntityAsDocument(entity);
				ElementCollection list = new ElementCollection(doc.getElementsByTagName("id"));
	
				StringBuilder retXML = new StringBuilder("<nodes>");
				List<Integer> ids = new ArrayList<Integer>();
				for (Element curEl : list)
					ids.add(parseIdentifier(curEl.getTextContent()));
					
				for (Taxon taxon : taxonIO.getTaxa(ids.toArray(new Integer[ids.size()]), false))
					retXML.append(taxon.toXML());
				
				retXML.append("</nodes>");
				
				response.setEntity(retXML.toString(), MediaType.TEXT_XML);
				response.getEntity().setCharacterSet(CharacterSet.UTF_8);
				response.setStatus(Status.SUCCESS_OK);
			}
			else if ("references".equals(parameters.getThird())) {
				updateReferences(entity, parseIdentifier(parameters.getSecond()), request, response, taxonIO, session);
			}
		}
		else if (action.equalsIgnoreCase("lowestTaxa"))
			serveLowestTaxa(response, entity, taxonIO);
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
			response.setEntity(getFullFootprint(values, taxonIO));
		}
	}
	
	@Override
	public void handlePut(Representation entity, Request request,
			Response response, Session session) throws ResourceException {
		Triple<String, String, String> parameters = getParameters(request);
		
		TaxonIO taxonIO = new TaxonIO(session);
		
		final String action = parameters.getFirst();
		if (action.equalsIgnoreCase("nodes"))
			updateTaxon(request, response, parseIdentifier(parameters.getSecond()), taxonIO, session);
	}
	
	private void updateReferences(Representation entity, Integer taxonID, Request request, Response response, TaxonIO taxonIO, Session session) throws ResourceException {
		Taxon taxon = taxonIO.getTaxonNonLazy(taxonID);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		Document document = getEntityAsDocument(entity);
		
		taxon.getReference().clear();
		for (Element el : new ElementCollection(document.getDocumentElement().getElementsByTagName("reference"))) {
			try {
				Reference reference = 
					SIS.get().getManager().getObject(session, Reference.class, Integer.valueOf(el.getAttribute("id")));
				if (reference.getTaxon() == null)
					reference.setTaxon(new HashSet<Taxon>());
				reference.getTaxon().add(taxon);
				taxon.getReference().add(reference);
			} catch (PersistentException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No reference with the ID " + el.getAttribute("id") + " was found.");
			}
		}
		
		try {
			taxonIO.writeTaxon(taxon, getUser(request, session), "Taxon references updated.");
		} catch (TaxomaticException e) {
			throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	private Representation getFootprintOfIDsAndChildren(Integer id, TaxonIO taxonIO) throws ResourceException {
		Taxon taxon = taxonIO.getTaxon(id);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Taxon with ID " + id + " not found.");
		
		return new StringRepresentation(TaxonHierarchy.fromTaxon(taxon).toXML(), MediaType.TEXT_XML);
	}

	private Representation getFullFootprint(String ids, TaxonIO taxonIO) throws ResourceException {
		String[] taxaIDs = ids.split(",");

		StringBuilder xml = new StringBuilder("<taxa>");

		for (int i = 0; i < taxaIDs.length; i++) {
			Taxon taxon = taxonIO.getTaxon(parseIdentifier(taxaIDs[i]));
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

	private Representation getFullFootprintOfIDs(Response response, Integer id, TaxonIO taxonIO) throws ResourceException {
		final Taxon taxon = taxonIO.getTaxon(id);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		return new StringRepresentation("<ids>" + taxon.getIDFootprintAsString(0, ",") + "</ids>", 
				MediaType.TEXT_XML);
	}	

	private void updateTaxon(Request request, Response response, Integer nodeID, TaxonIO taxonIO, Session session) throws ResourceException {
		if (nodeID != null) {
			User user = getUser(request, session);
			
			NativeDocument newDoc = new JavaNativeDocument();
			try {
				newDoc.parse(request.getEntity().getText());
			} catch (Exception e) {
				throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
			}
			
			Taxon source = Taxon.fromXML(newDoc);
			Taxon target = taxonIO.getTaxon(source.getId());
			
			Field possibleUnsaved = source.getTaxonomicNotes();
			if (possibleUnsaved != null) {
				if (possibleUnsaved.getId() == 0) {
					try {
						SIS.get().getManager().saveObject(session, possibleUnsaved);
					} catch (Exception e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}
				}
				else {
					target.getTaxonomicNotes().getPrimitiveField("value").setRawValue(
						possibleUnsaved.getPrimitiveField("value").getRawValue()
					);
				}
			}
			
			if (target.getTaxonomicNotes() != null && possibleUnsaved == null) {
				try {
					FieldDAO.deleteAndDissociate(target.getTaxonomicNotes(), session);
				} catch (Exception e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
				source.setTaxonomicNotes(null);
			}
			
			try {
				source = SIS.get().getManager().mergeObject(session, source);
			} catch (PersistentException e) {
				Debug.println(e);
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			try {
				taxonIO.writeTaxon(source, user, "Taxon metadata updated.", true);
			} catch (TaxomaticException e) {
				throw new ResourceException(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, e);
			}
			
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(source.toXML(), MediaType.TEXT_XML);
			
		} else
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}

	private Representation serveBrowsing(String hierarchy, TaxonIO taxonIO, Session session) throws ResourceException {
		Taxon taxon = null;

		if (hierarchy != null) {
			if (hierarchy.indexOf("-") < 0) {
				taxon = taxonIO.getTaxon(parseIdentifier(hierarchy));
			} else {
				String[] split = hierarchy.split("-");
				taxon = taxonIO.getTaxon(parseIdentifier(split[split.length - 1]));
			}
		}

		return new StringRepresentation(getHierarchyFootprintXML(taxon, taxonIO, session), MediaType.TEXT_XML);
	}

	private String getHierarchyFootprintXML(Taxon root, TaxonIO taxonIO, Session session) {
		final TaxonHierarchy hierarchy;
		
		if (root != null)
			hierarchy = TaxonHierarchy.fromTaxon(root);
		else {
			TaxonCriteria criteria = new TaxonCriteria(session);
			criteria.createTaxonLevelCriteria().level.eq(TaxonLevel.KINGDOM);
			
			Taxon[] list = taxonIO.search(criteria);
			List<Taxon> results = new ArrayList<Taxon>();
			for (Taxon taxon : list)
				results.add(taxon);
			
			//TODO: this should be at the database level
			final List<String> order = new ArrayList<String>();
			order.add("ANIMALIA");
			order.add("PLANTAE");
			order.add("PROTISTA");
			order.add("FUNGI");
			
			Collections.sort(results, new Comparator<Taxon>() {
				public int compare(Taxon arg0, Taxon arg1) {
					int left = order.indexOf(arg0.getName());
					int right = order.indexOf(arg1.getName());
					if (left == right)
						return 0;
					else if (left == -1)
						return 1;
					else if (right == -1)
						return -1;
					else
						return left > right ? -1 : 1;
				}
			});
			
			hierarchy = new TaxonHierarchy();
			
			final List<Integer> children = new ArrayList<Integer>();
			for (Taxon taxon : results)
				children.add(taxon.getId());
			
			hierarchy.setChildren(children);
		}
		
		return hierarchy.toXML();
	}

	private Representation serveByName(String kingdomName, String fullName, TaxonIO taxonIO) throws ResourceException {
		Taxon taxon = taxonIO.readTaxonByName(kingdomName, fullName);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Node name " + fullName + " is not found.");
		
		return serveInfo(taxon.getId() + "", taxonIO);
	}

	private Representation serveChildrenByID(Integer nodeID, TaxonIO taxonIO) throws ResourceException {
		final Taxon taxon = taxonIO.getTaxon(nodeID);
		if (taxon == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		String ids = taxon.getChildrenCSV();
		if ("".equals(ids))
			return new StringRepresentation("<empty></empty>", MediaType.TEXT_XML); 
		else
			return serveInfo(ids, taxonIO);
	}

	private Representation serveInfo(String nodeID, TaxonIO taxonIO) throws ResourceException {
		if (nodeID == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		Representation representation;
		if (nodeID.contains(",")) {
			String[] list = nodeID.split(",");
			StringBuilder retXML = new StringBuilder("<nodes>\r\n");

			for (int i = 0; i < list.length; i++) {
				Taxon taxon = taxonIO.getTaxon(parseIdentifier(list[i]));
				if (taxon != null)
					retXML.append(taxon.toXML());
			}
			retXML.append("</nodes>");

			representation = new StringRepresentation(retXML.toString(), MediaType.TEXT_XML);

		} else {
			Taxon taxon = taxonIO.getTaxon(parseIdentifier(nodeID));
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

	private void serveLowestTaxa(Response response, Representation entity, TaxonIO taxonIO) throws ResourceException {
		Document idsDoc = getEntityAsDocument(entity);
		ElementCollection idsList = new ElementCollection(idsDoc.getElementsByTagName("id"));
		ArrayList<Taxon> idsLeftToCheck = new ArrayList<Taxon>();

		for (Element curCode : idsList) {
			idsLeftToCheck.add(taxonIO.getTaxon(parseIdentifier(curCode.getTextContent())));
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
			representation = serveInfo(idsToFetch.substring(0, idsToFetch.length() - 1), taxonIO);
		else
			representation = new StringRepresentation("<empty></empty>", MediaType.TEXT_XML);
		
		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(representation);
	}
	
	private Representation fetchWorkingSetsForTaxon(Response response, Integer taxonID, Session session) throws ResourceException {
		Representation representation;
		try {
			WorkingSetIO workingSetIO = new WorkingSetIO(session);
			WorkingSet[] sets = workingSetIO.getWorkingSetsForTaxon(taxonID);
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
