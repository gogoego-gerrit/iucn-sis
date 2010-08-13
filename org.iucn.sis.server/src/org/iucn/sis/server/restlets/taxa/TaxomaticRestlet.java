package org.iucn.sis.server.restlets.taxa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.db.DBException;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeElementCollection;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.ElementCollection;
import com.solertium.util.Mutex;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;

public class TaxomaticRestlet extends ServiceRestlet {

	private Mutex lock = new Mutex();

	public TaxomaticRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	public TaxomaticRestlet(VFS vfs, Context context) {
		super(vfs, context);
	}

	public void definePaths() {
		paths.add("/taxomatic/{operation}");
	}

	private void trashTaxon(Integer id, Request request, final Response response) {
		try {
			Taxon taxon = SIS.get().getTaxonIO().getTaxon(id);
			if (taxon != null && SIS.get().getTaxonIO().trashTaxon(taxon, SIS.get().getUser(request))) {
				response.setStatus(Status.SUCCESS_OK);

			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		}
	}

	/**
	 * For demotions, a species is being demoted (Demoted1) to infrarank. The
	 * user selects a species for which the species will be the new parent of
	 * Demoted1. The children of Demoted1. The subpopulations of Demoted1 is
	 * moved to the status of an infrarank subpopulation. If the species had
	 * infrarank, the species can not be demoted until the infrarank has already
	 * been moved out.
	 * 
	 * when demoting a -something to a sub-something, the sub-something gets a
	 * synonym pointing to the demoted -something
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void demoteNode(Element documentElement, Request request, final Response response) {

		Element demoted = (Element) documentElement.getElementsByTagName("demoted").item(0);
		String id = demoted.getAttribute("id");
		String newParentID = demoted.getTextContent();
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(id));
		Taxon parent = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(newParentID));

		try {
			if (SIS.get().getTaxomaticIO().demoteSpecies(taxon, parent, SIS.get().getUser(request))) {
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} catch (RuntimeException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		}

	}

	private Taxon doAddNewTaxon(NativeElement newTaxon, User user) throws IOException, ConflictException, NotFoundException {

		Taxon taxon = Taxon.fromXML(newTaxon);
		if (SIS.get().getTaxomaticIO().saveNewTaxon(taxon, user)) {
			return taxon;
		} else {
			return null;
		}
	}

	/**
	 * Gets the last taxomatic operation if it is the correct user, or returns
	 * bad request if the user is not able to do an undo.
	 * 
	 * @param request
	 * @param response
	 */
	public void getLastTaxomaticOperation(Request request, Response response) {
		String username = SIS.get().getUsername(request);
		String lastUser = SIS.get().getTaxomaticIO().getLastOperationUsername();

		if (username.equalsIgnoreCase(lastUser) || username.equalsIgnoreCase("admin")) {
			Collection<Integer> taxaIDs = SIS.get().getTaxomaticIO().getTaxaIDsChanged();
			StringBuilder files = new StringBuilder();
			for (Integer file : taxaIDs) {
				files.append(file + ", ");
			}
			if (taxaIDs.size() > 0) {
				files.replace(files.length() - 2, files.length(), "");
			} else
				files.append("none (no taxa was affected)");
			String lastOperation = SIS.get().getTaxomaticIO().getLastOperationType();
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity("A " + lastOperation
					+ " was the last taxomatic operation that was performed, which affected taxa " + files.toString()
					+ ".", MediaType.TEXT_PLAIN);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

	private String getNewFullName(Taxon node) {
		return node.generateFullName();
	}

	/**
	 * For merge the taxomatic is designed so that you select one or more
	 * species ( say Old1 and Old2) to merge into a species (New1, which has
	 * already been previously created). Old1 and Old 2 will then receive a
	 * synonym to New1 and Old1 and Old2 will both be marked as deprecated. None
	 * of the information that was associated with Old1 or Old2 are moved into
	 * New1. New1 will receive deprecated synonyms to both Old1 and Old2.
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void mergeTaxa(Element documentElement, Request request, final Response response) {

		String mergedIDs = "";
		String mainID = "";
		boolean deprecated = false;
		try {
			mergedIDs = documentElement.getElementsByTagName("merged").item(0).getTextContent();
			mainID = documentElement.getElementsByTagName("main").item(0).getTextContent();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}
		
		HashMap<Integer, Taxon> nodes = new HashMap<Integer, Taxon>();
		List<Taxon> mergedTaxa = SIS.get().getTaxonIO().getTaxa(mergedIDs);
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(mainID));
		if (SIS.get().getTaxomaticIO().mergeTaxa(mergedTaxa, taxon, SIS.get().getUser(request))) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	/**
	 * Given a document which holds the speciesID and the infranks ids, merges
	 * the infraranks into the species and moves the suppopulations of the
	 * infraranks to be suppopulations of the species. Places a synonym in the
	 * infrarank, and in the suppopulatiosn.
	 * 
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void mergeUpInfraranks(Element documentElement, Request request, final Response response) {

		String mergedIDs = "";
		String mainID = "";
		try {
			mergedIDs = documentElement.getElementsByTagName("infrarank").item(0).getTextContent();
			mainID = documentElement.getElementsByTagName("species").item(0).getTextContent();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
		}
		System.out.println("This is infrarankIDs  " + mergedIDs);
		System.out.println("This is mainID " + mainID);

		List<Taxon> taxa = SIS.get().getTaxonIO().getTaxa(mergedIDs);
		Taxon main = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(mainID));

		if (SIS.get().getTaxomaticIO().mergeUpInfraranks(taxa, main, SIS.get().getUser(request))) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	/**
	 * Given a document which has the oldNode id and the assessment ids to move,
	 * moves the assessmentID to the new parentID
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void moveAssessments(final Element documentElement, final Request request, final Response response) {
		final NodeList oldNode = documentElement.getElementsByTagName("oldNode");
		final NodeList nodeToMoveAssessmentsInto = documentElement.getElementsByTagName("nodeToMoveInto");
		final NodeList assessmentNodeList = documentElement.getElementsByTagName("assessmentID");

		if (oldNode.getLength() != 1) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else if (nodeToMoveAssessmentsInto.getLength() != 1) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else if (assessmentNodeList.getLength() == 0) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else {
			try {
				String newNodeID = nodeToMoveAssessmentsInto.item(0).getTextContent();
				ArrayList<Assessment> assessments = new ArrayList<Assessment>();
				for (Node id : new NodeCollection(assessmentNodeList)) {

					assessments.add(SIS.get().getAssessmentIO().getAttachedAssessment(
							Integer.valueOf(id.getTextContent())));

				}

				if (SIS.get().getAssessmentIO().moveAssessments(
						SIS.get().getTaxonIO().getTaxon(Integer.valueOf(newNodeID)), assessments,
						SIS.get().getUser(request))) {
					response.setStatus(Status.SUCCESS_OK);
				} else {
					response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			} catch (DBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}
	}

	/**
	 * For the lateral move, (for a species or any other rank) you choose the
	 * new genus (or rank directly higher) where you would like to move the
	 * species to. All of the children of the moved taxa are also moved. Besides
	 * this, the information about the the moved species is unchanged.
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void moveNodes(Element documentElement, Request request, final Response response) {

		boolean success = true;
		Element parentElement = (Element) documentElement.getElementsByTagName("parent").item(0);
		String parentID = parentElement.getAttribute("id");
		final Taxon parentNode = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(parentID));
		final Collection<Taxon> childrenTaxa = new ArrayList<Taxon>();

		NodeList newChildren = documentElement.getElementsByTagName("child");
		for (int i = 0; i < newChildren.getLength() && success; i++) {
			String id = ((Element) newChildren.item(i)).getAttribute("id");
			childrenTaxa.add(SIS.get().getTaxonIO().getTaxon(Integer.valueOf(id)));
		}

		if (SIS.get().getTaxomaticIO().moveTaxa(parentNode, childrenTaxa, SIS.get().getUser(request))) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.CLIENT_ERROR_LOCKED);
		}

	}

	public void performService(Request request, Response response) {
		String operation = (String) request.getAttributes().get("operation");
		Method method = request.getMethod();

		boolean acquired = lock.attempt();

		try {
			if (!acquired) {
				response.setStatus(Status.CLIENT_ERROR_LOCKED);
			} else if (operation.equalsIgnoreCase("new") && method.equals(Method.PUT)) {
				putNewTaxon(request, response);
			} else if (operation.equalsIgnoreCase("update") && method.equals(Method.POST)) {
				updateTaxon(request, response);
			} else if (operation.equalsIgnoreCase("batch") && method.equals(Method.PUT)) {
				putBatch(request, response);
			} else if (operation.equalsIgnoreCase("merge") && method.equals(Method.POST)) {
				mergeTaxa(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request,
						response);
			} else if (operation.equalsIgnoreCase("moveAssessments") && method.equals(Method.POST)) {
				moveAssessments(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request,
						response);
			} else if (operation.equalsIgnoreCase("mergeupinfrarank") && method.equals(Method.POST)) {
				mergeUpInfraranks(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(),
						request, response);
			} else if (operation.equalsIgnoreCase("split") && method.equals(Method.POST)) {
				splitNodes(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request,
						response);
			} else if (operation.equalsIgnoreCase("move") && method.equals(Method.POST)) {
				moveNodes(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request,
						response);
			} else if (operation.equalsIgnoreCase("promote") && method.equals(Method.POST)) {
				promoteInfrarank(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request,
						response);
			} else if (operation.equalsIgnoreCase("demote") && method.equals(Method.POST)) {
				demoteNode(new DomRepresentation(request.getEntity()).getDocument().getDocumentElement(), request,
						response);
			} else if (operation.equalsIgnoreCase("undo") && method.equals(Method.POST)) {
				undoLastTaxomaticOperation(request, response);
			} else if (operation.equalsIgnoreCase("undo") && method.equals(Method.GET)) {
				getLastTaxomaticOperation(request, response);
			} else if (method.equals(Method.DELETE)) {
				Integer id = Integer.valueOf(operation);
				trashTaxon(id, request, response);
			} else
				response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);

			if (acquired)
				lock.release();

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		} finally {
			if (acquired)
				lock.release();
		}
	}

	/**
	 * For promotions an infrank is being moved into a species. The infrarank
	 * will just be promoted to be "brothers" with its previous parent, and all
	 * of the subpopulations of the infrarank are moved to be subpopulations of
	 * the newly promoted species. Besides this, the information about the the
	 * promoted species is unchanged.
	 * 
	 * if you promote a sub-something to a -something, the -something isn't
	 * getting a synonym pointing to the promoted sub-something. Check to make
	 * sure it puts the authority in the synonym.
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void promoteInfrarank(Element documentElement, Request request, final Response response) {

		Element promoted = (Element) documentElement.getElementsByTagName("promoted").item(0);
		String id = promoted.getAttribute("id");
		Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(id));
		if (SIS.get().getTaxomaticIO().promoteInfrarank(taxon, SIS.get().getUser(request))) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

	/**
	 * 
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private void putBatch(Request request, Response response) throws Exception {

		String taxaAsString = request.getEntity().getText();
		NativeDocument ndoc = SIS.get().newNativeDocument(request.getChallengeResponse());
		NativeNodeList taxa = ndoc.getDocumentElement().getElementsByTagName(Taxon.ROOT_TAG);
		NativeElementCollection collection = new NativeElementCollection(taxa);
		StringBuilder ids = new StringBuilder();
		User user = SIS.get().getUser(request);
		boolean error = false;
		for (NativeElement element : collection) {
			Taxon taxon = doAddNewTaxon(element, user);
			if (taxon == null) {
				ids.append(taxon.getId() + ",");
			} else {
				error = true;
				break;
			}
		}

		if (error)
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "One of these taxa already existed");
		else
			response.setStatus(Status.SUCCESS_OK);

		if (ids.toString().endsWith(","))
			response.setEntity(ids.substring(0, ids.length() - 1), MediaType.TEXT_PLAIN);
		else
			response.setEntity(ids.toString(), MediaType.TEXT_PLAIN);

	}

	private long putNewTaxon(Request request, Response response) throws Exception {
		String text = request.getEntityAsText();
		NativeDocument ndoc = SIS.get().newNativeDocument(request.getChallengeResponse());
		ndoc.parse(text);
		User user = SIS.get().getUser(request);
		Taxon taxon = doAddNewTaxon(ndoc.getDocumentElement(), user);
		
		if (taxon != null) {
			response.setEntity(taxon.getId() + "", MediaType.TEXT_PLAIN);
			response.setStatus(Status.SUCCESS_OK);
			return taxon.getId();
		}

		else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return -1;
		}

		
	}

	/**
	 * For split the taxomatic is designed so that you select the node (Old1)
	 * that you would like to split. You are then prompted to create new taxa
	 * which you would like to split the node into (at least 2, lets say New1,
	 * New2). For each child of Old1, you can choose to place it under either
	 * New1 or in New2. Old1 is deprecated, and has two new Synonyms linking to
	 * New1 and New2. New1 and New2 receive the children that was selected for
	 * them, and they both receive a deprecated synonym to Old1. None of the
	 * assessments are transferred from Old1.
	 * 
	 * @param documentElement
	 * @param request
	 * @param response
	 */
	private void splitNodes(Element documentElement, Request request, final Response response) {
		boolean success = true;
		boolean deprecated = false;

		Element originalNode = (Element) documentElement.getElementsByTagName("current").item(0);
		String oldId = originalNode.getTextContent();
		Taxon oldNode = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(oldId));
		ElementCollection parents = new ElementCollection(documentElement.getElementsByTagName("parent"));
		HashMap<Taxon, ArrayList<Taxon>> parentToChildren = new HashMap<Taxon, ArrayList<Taxon>>();
		int childrenSize = 0;
		for (Element parent : parents) {
			String parentID = parent.getAttribute("id");
			Taxon parentTaxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(parentID));
			ElementCollection children = new ElementCollection(parent.getElementsByTagName("child"));
			ArrayList<Taxon> childrenTaxa = new ArrayList<Taxon>();
			for (Element child : children) {
				String childID = child.getTextContent();
				childrenTaxa.add(SIS.get().getTaxonIO().getTaxon(Integer.valueOf(childID)));
			}
			parentToChildren.put(parentTaxon, childrenTaxa);
			childrenSize += childrenTaxa.size();

		}
		// DOING VALIDATION TO MAKE SURE THEY SPLIT ALL CHILDREN
		if (oldNode.getChildren().size() != childrenSize) {
			success = false;
		}

		if (success) {
			if (SIS.get().getTaxomaticIO().splitNodes(oldNode, SIS.get().getUser(request), parentToChildren)) {
				response.setStatus(Status.SUCCESS_OK);
			} else {
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param docElement
	 * @param request
	 * @param response
	 */
	public void undoLastTaxomaticOperation(Request request, Response response) {
		String username = SIS.get().getUsername(request);
		boolean success = SIS.get().getTaxomaticIO().performTaxomaticUndo(username);
		if (success) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}
	
	public void updateTaxon(Request request, Response response) {
		String text = request.getEntityAsText();
		NativeDocument ndoc = SIS.get().newNativeDocument(request.getChallengeResponse());
		ndoc.parse(text);
		
		Taxon updatedTaxon = Taxon.fromXML(ndoc);
		Taxon currentTaxon = SIS.get().getTaxonIO().getTaxon(updatedTaxon.getId());
		currentTaxon.setName(updatedTaxon.getName());
		currentTaxon.setTaxonLevel(updatedTaxon.getTaxonLevel());
		currentTaxon.setHybrid(updatedTaxon.getHybrid());
		currentTaxon.setTaxonomicAuthority(updatedTaxon.getTaxonomicAuthority());
		currentTaxon.setStatus(updatedTaxon.getStatusCode());
		if (updatedTaxon.getInfratype() == null) 
			currentTaxon.setInfratype(null);
		else {
			currentTaxon.setInfratype(SIS.get().getInfratypeIO().getInfratype(updatedTaxon.getInfratype().getName()));
		}
		if (SIS.get().getTaxomaticIO().writeTaxon(currentTaxon, SIS.get().getUser(request))) {
			response.setStatus(Status.SUCCESS_OK);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		
		
		
	}

}
