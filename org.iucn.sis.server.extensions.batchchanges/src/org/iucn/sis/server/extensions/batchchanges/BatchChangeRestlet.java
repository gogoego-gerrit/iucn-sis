package org.iucn.sis.server.extensions.batchchanges;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.extensions.batchchanges.BatchAssessmentChanger.BatchChangeMode;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class BatchChangeRestlet extends BaseServiceRestlet {
	
	public BatchChangeRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/batchChange");
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		AssessmentIO assessmentIO = new AssessmentIO(session);
		User user = getUser(request, session);
		
		final NativeDocument dataDoc = getEntityAsNativeDocument(entity);

		Assessment template = null;
		AssessmentFilter filter = null;
		String taxaIDs = null;
		List<String> fieldNames = new ArrayList<String>();
		BatchChangeMode mode = null;
		
		final NativeNodeList nodes = dataDoc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			NativeNode current = nodes.item(i);
			if ("assessment".equals(current.getNodeName())) {
				template = assessmentIO.getAssessment(Integer.valueOf(current.getTextContent()));
			} 
			else if (AssessmentFilter.ROOT_TAG.equals(current.getNodeName())) {
				filter = AssessmentFilter.fromXML((NativeElement)current);
			}
			else if ("fields".equals(current.getNodeName())) {
				NativeNodeList children = current.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					NativeNode child = children.item(k);
					if ("field".equals(child.getNodeName()))
						fieldNames.add(child.getTextContent());
				}
			}
			else if ("taxa".equals(current.getNodeName())) {
				taxaIDs = current.getTextContent();
			}
			else if ("append".equals(current.getNodeName())) {
				if ("true".equalsIgnoreCase(current.getTextContent()))
					mode = BatchChangeMode.APPEND;
			}
			else if ("overwrite".equals(current.getNodeName())) {
				if ("true".equalsIgnoreCase(current.getTextContent()))
					mode = BatchChangeMode.OVERWRITE_IF_BLANK;
			}
			else if ("set".equals(current.getNodeName()))
				if ("true".equalsIgnoreCase(current.getTextContent()))
					mode = BatchChangeMode.OVERWRITE;
		}
		
		if (template == null || filter == null || mode == null || taxaIDs == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		AssessmentFilterHelper helper = new AssessmentFilterHelper(session, filter);

		StringBuilder returnXML = new StringBuilder("<changes>");
		
		List<Assessment> assessments = new ArrayList<Assessment>();
		
		for (String taxonID : taxaIDs.split(",")) {
			for (Assessment asmToChange : helper.getAssessments(Integer.valueOf(taxonID))) {
				if (BatchAssessmentChanger.changeAssessment(session,
						asmToChange, template, mode,
						fieldNames)) {
					returnXML.append("<change id=\""
							+ asmToChange.getId() + "\">"
							+ asmToChange.getDisplayText()
							+ "</change>");
					assessments.add(asmToChange);
				}
			}
		}
		returnXML.append("</changes>");
			
		try {
			assessmentIO.saveAssessments(assessments, user);
		} catch (PersistentException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Unable to save the changes in the assessment", e);
		}
			
		response.setEntity(returnXML.toString(), MediaType.TEXT_XML);
		response.getEntity().setCharacterSet(CharacterSet.UTF_8);
		response.setStatus(Status.SUCCESS_OK);
	}
}
