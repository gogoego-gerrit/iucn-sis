package org.iucn.sis.server.restlets.taxa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

/**
 * Returns the taxon with the given status.
 * 
 * @author liz.schwartz
 * 
 */
public class TaxonByStatusRestlet extends BaseServiceRestlet {

	public TaxonByStatusRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void definePaths() {
		paths.add("/taxaFinder/{status}");
		paths.add("/taxaFinder/workingSet");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final String status = (String)request.getAttributes().get("status");
		if (!isValid(status))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid status code.");
		
		final TaxonIO io = new TaxonIO(session);
		final Taxon[] results = io.getTaxaByStatus(status);
		
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		
		for (Taxon taxon : results)
			out.append(taxon.toXMLMinimal());
		
		out.append("</root>");
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
	}
	
	private boolean isValid(String code) {
		for (String current : TaxonStatus.ALL)
			if (current.equals(code))
				return true;
		return false;
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		if (request.getResourceRef().getPath().endsWith("/workingSet")) {
			createWorkingSet(request, response, session);
		} else {
			/*
			 * TODO: implement server-side paging and allow a limit and offset to be 
			 * passed from the client.
			 */
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		}
	}

	/**
	 * Creates a private working set based on new status TODO: COULD BE EXTENDED
	 * HERE TO TAKE DIFFERENT STATUS
	 * 
	 * @param request
	 * @param response
	 */
	protected void createWorkingSet(Request request, Response response, Session session) {
		WorkingSetIO workingSetIO = new WorkingSetIO(session);
		
		final User user = getUser(request, session);
		final Taxon[] taxa = getSpeciesByStatus(session, TaxonStatus.STATUS_NEW);
		final Date today = new Date();
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		final String output = formatter.format(today);

		AssessmentFilter filter = new AssessmentFilter();
		WorkingSet workingSet = new WorkingSet();
		workingSet.setName("New taxa");
		workingSet.setDescription("Taxa with new status as of " + output);
		workingSet.setCreatedDate(new Date());
		workingSet.setCreator(user);
		workingSet.setTaxon(new HashSet<Taxon>());
		workingSet.setUsers(new HashSet<User>());
		workingSet.getUsers().add(user);
		for (Taxon taxon : taxa) {
			workingSet.getTaxon().add(taxon);
		}
		workingSet.setFilter(filter);


		// SUCCESSFULLY CREATED WORKINGSET
		if (workingSetIO.saveWorkingSet(workingSet, user, "Working set created.")) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity("Working set was successfully created", MediaType.TEXT_PLAIN);
		}
		// WAS UNABLE TO CREATE WORKING SET
		else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity("There was a server error when trying to create the working set", MediaType.TEXT_PLAIN);

		}
	}

	/**
	 * Given a status finds returns a linked hashmap of all ids to names of taxa
	 * with the given status, and limits it only to Species an lower
	 * 
	 * @param status
	 * @return
	 */
	protected Taxon[] getSpeciesByStatus(Session session, String status) {
		TaxonIO io = new TaxonIO(session);
		TaxonStatus statusObj = TaxonStatus.fromCode(status);
		
		TaxonCriteria criteria = new TaxonCriteria(session);
		criteria.createTaxonStatusCriteria().id.eq(statusObj.getId());
		criteria.createTaxonLevelCriteria().level.ge(TaxonLevel.SPECIES);
		
		return io.search(criteria);
	}

}
