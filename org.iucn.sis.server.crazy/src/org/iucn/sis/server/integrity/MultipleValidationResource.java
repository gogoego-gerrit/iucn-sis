package org.iucn.sis.server.integrity;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.VFSPathToken;

public class MultipleValidationResource extends ValidationResource {

	public MultipleValidationResource(Context context, Request request,
			Response response) {
		super(context, request, response);
	}
	
	protected Document handle(Collection<AssessmentInfo> assessmentInfo) throws ResourceException {
		//All rules are interrogated
		final Map<String, Document> rulesetDocuments = getRulesetDocuments();
		
		final Document response = BaseDocumentUtils.impl.newDocument();
		final Element root = response.createElement("div");
		root.setAttribute("class", "sis_integrity_multiple");
		
		for (AssessmentInfo info : assessmentInfo) {
			info.setName(getSpeciesName(info));
			
			if (!isAvailable(info))
				continue;
			
			final Element header = BaseDocumentUtils.impl
				.createElementWithText(response, "div", info.getName() + " -- " + info.getType().substring(0, info.getType().indexOf("_")) + " -- ");
			header.setAttribute("class", "sis_integrity_header");
			
			final Element assessmentInfoDiv = response.createElement("div");
			
			for (Map.Entry<String, Document> entry : rulesetDocuments.entrySet()) {
				try {
					runAssessment(entry.getKey(), entry.getValue(), response, info.getName(), info.getID(), info.getType(), assessmentInfoDiv, false, false);
				} catch (ResourceException e) {
					e.printStackTrace();
					final Element failure = BaseDocumentUtils.impl
						.createElementWithText(response, "p", "Failed Due to Server Exception");
					failure.setAttribute("class", "sis_integrity_failure");
					assessmentInfoDiv.appendChild(failure);
				}
			}
			
			if (assessmentInfoDiv.hasChildNodes()) {
				root.appendChild(header);
				root.appendChild(assessmentInfoDiv);
			}
		}
		
		/**
		 * If there are no child nodes, then all assessments were valid...
		 */
		if (!root.hasChildNodes()) {
			for (AssessmentInfo info : assessmentInfo) {
				if (isAvailable(info)) {
					final Element header = BaseDocumentUtils.impl
						.createElementWithText(response, "div", info.getName() + " -- " + info.getType().substring(0, info.getType().indexOf("_")) + " -- ");
					header.setAttribute("class", "sis_integrity_header");
					root.appendChild(header);

					final Element success = BaseDocumentUtils.impl.createElementWithText(response, "p","This assessment is valid.");
					success.setAttribute("class", "sis_integrity_success");
					root.appendChild(success);
				}
			}
		}
		
		response.appendChild(root);
		
		return response;
	}
	
	private Map<String, Document> getRulesetDocuments() throws ResourceException {
		final VFSPathToken[] tokens;
		try {
			tokens = vfs.list(ROOT_PATH);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not list rules.", e);
		}
		
		final Map<String, Document> documents = new LinkedHashMap<String, Document>();
		
		for (VFSPathToken token : tokens) {
			System.out.println("Adding " + token);
			try {
				documents.put(token.toString(), getRuleset(token.toString()));
			} catch (ResourceException e) {
				System.out.println("Failed to run rule " + token);
				continue;
			}
		}
		
		return documents;
	}

}
