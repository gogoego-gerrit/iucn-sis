package org.iucn.sis.server.restlets.workingsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;

/**
 * The Working Set Restlet, handles calls to get and modify a user's
 * workingSet.xml file. Makes assumptions that an xml file exists in the users
 * working space.
 * 
 * @author liz.schwartz
 * 
 */
public class WorkingSetRestlet extends ServiceRestlet {

	public WorkingSetRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	protected void editTaxaInWorkingSet(Request request, Response response, String username, Integer id) {
		WorkingSet ws = SIS.get().getWorkingSetIO().readWorkingSet(id);
		User user = SIS.get().getUser(request);
		try {
			if (ws != null && user != null) {
				Document doc = new DomRepresentation(request.getEntity()).getDocument();
				Element docElement = doc.getDocumentElement();

				List<String> removeTaxonIDs = new ArrayList<String>();
				NodeList removed = docElement.getElementsByTagName("remove");
				for (int i = 0; i < removed.getLength(); i++) {
					removeTaxonIDs.add(removed.item(i).getTextContent());
				}

				List<String> addedTaxonIDs = new ArrayList<String>();
				NodeList added = docElement.getElementsByTagName("add");
				for (int i = 0; i < added.getLength(); i++) {
					addedTaxonIDs.add(added.item(i).getTextContent());
				}

				if (!removeTaxonIDs.isEmpty()) {
					Collection<Taxon> taxaToRemove = new HashSet<Taxon>();
					for (Taxon taxon : ws.getTaxon())
						if (removeTaxonIDs.contains(Integer.toString(taxon.getId()))) {
							taxaToRemove.add(taxon);
						}
					ws.getTaxon().removeAll(taxaToRemove);
				}

				for (String taxonID : addedTaxonIDs) {
					Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(taxonID));
					if (taxon != null) {
						ws.getTaxon().add(taxon);
					}
				}

				if (SIS.get().getWorkingSetIO().saveWorkingSet(ws, user)) {
					response.setEntity("Successfully editted the taxa in your working set", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (IOException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

	private void createWorkingSet(Request request, Response response, String username) {

		try {
			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
			ndoc.parse(request.getEntityAsText());
			WorkingSet ws = WorkingSet.fromXML(ndoc.getDocumentElement());

			// SETTING THE CREATOR AND ADDING THEM AS A SUBSCRIBED USER
			User creator = SIS.get().getUserIO().getUserFromUsername(ws.getCreatorUsername());
			ws.setCreator(creator);
			ws.getUsers().add(creator);
			User user = SIS.get().getUserIO().getUserFromUsername(username);
			if (ws.getId() == 0 && user != null) {
				SIS.get().getWorkingSetIO().saveWorkingSet(ws, user);
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(Integer.toString(ws.getId()), MediaType.TEXT_PLAIN);
			} else {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}

		} catch (Exception e) {
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
			e.printStackTrace();
		}
	}

	@Override
	public void definePaths() {
		paths.add("/workingSet/{action}");
		paths.add("/workingSet/{action}/{username}");
		paths.add("/workingSet/{action}/{username}/{id}");
	}

	/**
	 * Deletes a working set
	 */
	private void deleteWorkingSet(Request request, Response response, Integer workingsetID) {

		WorkingSet ws = SIS.get().getWorkingSetIO().readWorkingSet(workingsetID);
		if (ws != null) {
			if (SIS.get().getWorkingSetIO().deleteWorkingset(ws)) {
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	/**
	 * Posts to a public working set and adds creates taxa
	 * 
	 * @param request
	 * @param response
	 */
	private void editWorkingSet(Request request, Response response, String username) {

		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(request.getEntityAsText());
		WorkingSet ws = WorkingSet.fromXML(ndoc.getDocumentElement());
		// NEEDED BECAUSE WORKINGSET UI DOESN"T KNOW USER's ID
		ws.setCreator(SIS.get().getUserIO().getUserFromUsername(ws.getCreatorUsername()));
		User user = SIS.get().getUserIO().getUserFromUsername(username);
		if (user != null && SIS.get().getWorkingSetIO().saveWorkingSet(ws, user)) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	/**
	 * Gets all possible subscribable working sets, leaving out ones that users
	 * are currently subscribed to.
	 * 
	 * @param request
	 * @param response
	 */
	private void getSubscribableWorkingSets(Request request, Response response, String username) {

		try {
			WorkingSet[] sets = SIS.get().getWorkingSetIO().getUnsubscribedWorkingSets(username);
			StringBuilder xml = new StringBuilder("<xml>\r\n");
			for (WorkingSet set : sets) {
				xml.append(set.toXMLMinimal());
			}
			xml.append("</xml>");
			System.out.println("these are the unsubscribed working sets " + xml.toString());
			response.setEntity(xml.toString(), MediaType.TEXT_XML);
			response.setStatus(Status.SUCCESS_OK);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(e.getMessage(), MediaType.TEXT_PLAIN);
		}

	}

	/**
	 * For each taxa in the working set, returns the entire footprint.
	 * 
	 * @param request
	 * @param response
	 */
	private void getTaxaFootprintForWorkingSet(Request request, Response response, String username, Integer workingSetID) {

		WorkingSet ws = SIS.get().getWorkingSetIO().readWorkingSet(workingSetID);
		if (ws != null) {
			StringBuilder csv = new StringBuilder();
			for (Taxon taxon : ws.getTaxon()) {
				csv.append(taxon.getFootprintCSV() + "\r\n");
			}
			writeReport(csv.toString(), username);
			response.setStatus(Status.SUCCESS_CREATED);
			response.setEntity("/raw" + getURLToSaveFootprint(username), MediaType.TEXT_ALL);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
	
	private void writeReport(String report, String username) {
		DocumentUtils.writeVFSFile(getURLToSaveFootprint(username), vfs, report);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 */
	private void getTaxaFootprintWithAdditionalInfo(Request request, Response response, String username, Integer id) {

		try {
			WorkingSet ws = SIS.get().getWorkingSetIO().readWorkingSet(id);
			if (ws != null) {
				NativeDocument entityDoc = NativeDocumentFactory.newNativeDocument();
				entityDoc.parse(request.getEntityAsText());
				AssessmentFilter filter = AssessmentFilter.fromXML(entityDoc);
				AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);

				StringBuilder csv = new StringBuilder();
				for (Taxon taxon : ws.getTaxon()) {
					List<Assessment> assessments = helper.getAssessments(taxon.getId());
					if (!assessments.isEmpty()) {
						csv.append(taxon.getFootprintAsString() + ",");
						csv.append("\"" + assessments.get(0).getProperCriteriaString() + "\","
								+ assessments.get(0).getProperCategoryAbbreviation() + "\r\n");
					}

				}
				writeReport(csv.toString(), username);
				response.setEntity("/raw" + getURLToSaveFootprint(username), MediaType.TEXT_ALL);
				response.setStatus(Status.SUCCESS_CREATED);

			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}

	}

	/**
	 * Returns all taxa associated with a working set
	 * 
	 * @param request
	 * @param response
	 */
	private void getTaxaListForWorkingSet(Request request, Response response, String username, Integer id) {
		WorkingSet ws = SIS.get().getWorkingSetIO().readWorkingSet(id);
		if (ws != null) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(ws.getSpeciesIDsAsString(), MediaType.TEXT_PLAIN);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	public String getURLToSaveFootprint(String username) {
		return "/users/" + username + "/reports/footprint.csv";
	}

	/**
	 * Returns the xml of the working set associated with workingsetID
	 * 
	 * @param request
	 * @param response
	 * @param username
	 * @param workingSetID
	 */
	private void getWorkingSet(Request request, Response response, String username, Integer workingSetID) {
		WorkingSet ws = SIS.get().getWorkingSetIO().readWorkingSet(workingSetID);
		if (ws != null) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(ws.toXML(), MediaType.TEXT_XML);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	/**
	 * Gets all working sets the user is subscribed to.
	 * 
	 * @param request
	 * @param response
	 */
	private void getWorkingSets(Request request, Response response, String username) {
		// System.out.println("Getting working sets for " + username);
		WorkingSet[] sets;
		try {
			sets = SIS.get().getWorkingSetIO().getSubscribedWorkingSets(username);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		StringBuilder xml = new StringBuilder("<workingsets>");
		for (WorkingSet set : sets)
			xml.append(set.toXML());
		xml.append("</workingsets>");
		// System.out.println(xml.toString());
		response.setStatus(Status.SUCCESS_OK);
		response.setEntity(xml.toString(), MediaType.TEXT_XML);
	}

	@Override
	public void performService(Request request, Response response) {
		try {
			final String username = (String) request.getAttributes().get("username");
			final String id = (String) request.getAttributes().get("id");
			final String action = (String) request.getAttributes().get("action");

			if (request.getMethod().equals(Method.GET)) {
				if (action.equalsIgnoreCase("subscribe"))
					getSubscribableWorkingSets(request, response, username);
				else if (action.equalsIgnoreCase("taxaList"))
					getTaxaFootprintForWorkingSet(request, response, username, Integer.valueOf(id));
				else if (action.equalsIgnoreCase("taxaIDs"))
					getTaxaListForWorkingSet(request, response, username, Integer.valueOf(id));
				else if (id != null && id.matches("\\d+"))
					getWorkingSet(request, response, username, Integer.valueOf(id));
				else
					getWorkingSets(request, response, username);
			} else if (request.getMethod().equals(Method.DELETE)) {
				if (action.equalsIgnoreCase("unsubscribe"))
					unsubscribeFromWorkingSet(request, response, username, Integer.valueOf(id));
				else
					deleteWorkingSet(request, response, Integer.valueOf(id));
			} else if (request.getMethod().equals(Method.PUT)) {
				if (action.equalsIgnoreCase("subscribe"))
					subscribeToPublicWorkingSet(request, response, username, Integer.valueOf(id));
				else
					createWorkingSet(request, response, username);
			} else if (request.getMethod().equals(Method.POST)) {
				if (action.equalsIgnoreCase("taxaList"))
					getTaxaFootprintWithAdditionalInfo(request, response, username, Integer.valueOf(id));
				else if (action.equalsIgnoreCase("editTaxa"))
					editTaxaInWorkingSet(request, response, username, Integer.valueOf(id));
				else
					editWorkingSet(request, response, username);
			} else
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void unsubscribeFromWorkingSet(Request request, Response response, String username, Integer id) {
		if (SIS.get().getWorkingSetIO().unsubscribeFromWorkingset(username, id)) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	private void subscribeToPublicWorkingSet(Request request, Response response, String username, Integer id) {
		if (SIS.get().getWorkingSetIO().subscribeToWorkingset(id, username)) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(SIS.get().getWorkingSetIO().readWorkingSet(id).toXML(), MediaType.TEXT_XML);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

}
