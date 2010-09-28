package org.iucn.sis.server.extensions.attachments;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.assessments.AssessmentAttachment;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.restlet.VFSResource;

public class FileAttachmentRestlet extends ServiceRestlet {

	protected final VFSPath rootDir;
	protected final VFSPathToken registryFile = new VFSPathToken(
			"_attachments.xml");

	public FileAttachmentRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		rootDir = new VFSPath("/attachments");

	}

	protected boolean addToRegistry(VFSPath registryPath,
			AssessmentAttachment attachment) {
		String doc = DocumentUtils.getVFSFileAsString(registryPath.toString(), vfs);
		if (doc == null)
			doc = "<attachments></attachments>";
		
		doc = doc.replace("</attachments>", "");
		doc += attachment.toXML() + "\r\n</attachments>";
		return DocumentUtils.writeVFSFile(registryPath.toString(), vfs, doc);
	}

	@Override
	public void definePaths() {
		paths.add("/attachment/{assessmentID}");
		paths.add("/attachment/file/{attachmentID}");

	}

	protected boolean deleteFile(VFSPath path) {
		if (vfs.exists(path)) {
			try {
				vfs.delete(path);
			} catch (NotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (ConflictException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	protected String getAssessmentIDFromAttachmentID(String attachmentID) {
		int splitIndex = attachmentID.lastIndexOf("_");
		return attachmentID.substring(0, splitIndex);
	}

	protected VFSPath getAssessmentPath(String assessmentID) {
		return new VFSPath(rootDir.toString() + "/"
				+ FilenameStriper.getIDAsStripedPath(assessmentID));
	}

	protected String getAttachmentID(String assessmentID,
			String attachmentFileName) {
		if (attachmentFileName.contains("/")) {
			attachmentFileName = attachmentFileName.substring(
					attachmentFileName.lastIndexOf("/") + 1, attachmentFileName
							.length());
		}
		return assessmentID + "_" + attachmentFileName;
	}

	protected VFSPath getAttachmentPath(String attachmentID) {
		VFSPathToken token = new VFSPathToken(
				getFilenameFromAttachmentID(attachmentID));
		return getAssessmentPath(getAssessmentIDFromAttachmentID(attachmentID))
				.child(token);
	}

	protected String getFilenameFromAttachmentID(String attachmentID) {
		int splitIndex = attachmentID.lastIndexOf("_");
		return attachmentID.substring(splitIndex + 1);
	}

	protected void handleDelete(Request request, Response response,
			String attachmentID) {

		if (removeFromRegistry(getAssessmentPath(
				getAssessmentIDFromAttachmentID(attachmentID)).child(
				registryFile), attachmentID)
				&& deleteFile(getAttachmentPath(attachmentID))) {

			response.setStatus(Status.SUCCESS_OK);
			response.setEntity("Attachment deleted", MediaType.TEXT_PLAIN);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL,
					"Unable to delete attachment");
		}
	}

	private void handleGet(Request request, Response response,
			String assessmentID, String attachmentID) {

		// FIND ATTACHMENT
		if (assessmentID == null && attachmentID != null) {
			VFSPath attachmentFile = getAttachmentPath(attachmentID);
			if (vfs.exists(attachmentFile)) {
				try {
					Representation rep = VFSResource.getRepresentationForFile(
							vfs, attachmentFile);
					response.setEntity(rep);
					response.setStatus(Status.SUCCESS_OK);
				} catch (NotFoundException e) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
							"Attachment not found");
				}
				return;
			} else {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
						"Attachment not found");
			}
		}

		else if (assessmentID != null && attachmentID == null) {
			VFSPath path = getAssessmentPath(assessmentID);
			if (vfs.exists(path)) {
				Document registryDoc = DocumentUtils.getVFSFileAsDocument(path
						.child(registryFile).toString(), vfs);
				if (registryDoc == null) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL,
							"Unable to find registry for attachment "
									+ assessmentID);
					return;
				}

				response.setEntity(new DomRepresentation(MediaType.TEXT_XML,
						registryDoc));
			} else {
				response.setEntity("<attachments></attachments>",
						MediaType.TEXT_XML);
			}
			response.setStatus(Status.SUCCESS_OK);
			return;
		}

		response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);

	}

	protected void handleModifyAttachment(Request request, Response response,
			String attachmentID) {
		String assessmentID = getAssessmentIDFromAttachmentID(attachmentID);
		String isPublic = request.getEntityAsForm().getFirstValue("isPublic");
		if (updatedRegistry(
				getAssessmentPath(assessmentID).child(registryFile),
				attachmentID, isPublic)) {
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity("Modified attachment", MediaType.TEXT_PLAIN);
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL,
					"Failed to modify attachment");
		}

	}

	private void handlePost(Request request, Response response,
			String assessmentID) {
		RestletFileUpload fileUploaded = new RestletFileUpload(
				new DiskFileItemFactory());
		List<FileItem> list;
		try {
			list = fileUploaded.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					"unable to read file");
			return;
		}
		Boolean isPublished = null;
		FileItem file = null;

		for (FileItem item : list) {
			if (item.isFormField()) {
				if (item.getFieldName().equalsIgnoreCase("publish"))
					isPublished = Boolean.valueOf(item.getString());
			} else
				file = item;
		}

		if (isPublished == null || file == null || assessmentID == null) {
			response
					.setStatus(
							Status.CLIENT_ERROR_BAD_REQUEST,
							"File, publish status, and Assessment ID must be sent in order to attach to assessment");
			return;
		} else if (!isValidFilename(file.getName())) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					"Filename must not contain \"_\"");
			return;
		}

		String attachmentID = getAttachmentID(assessmentID, file.getName());
		VFSPath filePath = getAttachmentPath(attachmentID);
		VFSPath registryPath = getAssessmentPath(assessmentID).child(
				registryFile);
		AssessmentAttachment attachment = new AssessmentAttachment();
		attachment.assessmentID = assessmentID;
		attachment.id = attachmentID;
		attachment.filename = getFilenameFromAttachmentID(attachmentID);
		attachment.isPublished = isPublished;

		// TRY TO WRITE FILE
		if (writeFile(filePath, file)) {
			// ADD TO REGISTRY
			if (addToRegistry(registryPath, attachment)) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity(attachmentID, MediaType.TEXT_PLAIN);
			}
			// FAILED ADDING TO REGISTY
			else {
				deleteFile(filePath);
				response
						.setStatus(Status.SERVER_ERROR_INTERNAL,
								"Unable to add file to the attachment registry, try again later.");
			}
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL,
					"Unable to save attachment");
		}

	}

	protected boolean isValidFilename(String attachmentFileName) {
		if (attachmentFileName.contains("/")) {
			attachmentFileName = attachmentFileName.substring(
					attachmentFileName.lastIndexOf("/") + 1, attachmentFileName
							.length());
		}
		return !attachmentFileName.contains("_");
	}

	@Override
	public void performService(Request request, Response response) {
		String assessmentID = (String) request.getAttributes().get(
				"assessmentID");
		String attachmentID = (String) request.getAttributes().get(
				"attachmentID");
		if (request.getMethod().equals(Method.GET))
			handleGet(request, response, assessmentID, attachmentID);
		else if (request.getMethod().equals(Method.POST)
				&& assessmentID != null)
			handlePost(request, response, assessmentID);
		else if (request.getMethod().equals(Method.POST)
				&& attachmentID != null)
			handleModifyAttachment(request, response, attachmentID);
		else if (request.getMethod().equals(Method.DELETE))
			handleDelete(request, response, attachmentID);
		else
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);

	}

	protected boolean removeFromRegistry(VFSPath registryPath,
			String attachmentID) {
		Document doc = DocumentUtils.getVFSFileAsDocument(registryPath
				.toString(), vfs);
		if (doc == null)
			return false;

		Element docElement = doc.getDocumentElement();
		Element elementToRemove = null;
		NodeList attachments = docElement.getElementsByTagName("attachment");
		for (int i = 0; i < attachments.getLength(); i++) {
			Element element = (Element) attachments.item(i);
			if (element.getAttribute("id").equalsIgnoreCase(attachmentID)) {
				elementToRemove = element;
			}
		}

		if (elementToRemove == null)
			return false;

		docElement.removeChild(elementToRemove);
		return DocumentUtils.writeVFSFile(registryPath.toString(), vfs, doc);
	}

	protected boolean updatedRegistry(VFSPath registryPath,
			String attachmentID, String isPublic) {
		Document doc = DocumentUtils.getVFSFileAsDocument(registryPath
				.toString(), vfs);
		if (doc == null) {
			return false;
		}

		Element docElement = doc.getDocumentElement();
		Element elementToModify = null;
		NodeList attachments = docElement.getElementsByTagName("attachment");
		for (int i = 0; i < attachments.getLength(); i++) {
			Element element = (Element) attachments.item(i);
			if (element.getAttribute("id").equalsIgnoreCase(attachmentID)) {
				elementToModify = element;
			}
		}

		if (elementToModify == null) {
			return false;
		}

		NodeList published = elementToModify.getElementsByTagName("published");
		published.item(0).setTextContent(isPublic);
		return DocumentUtils.writeVFSFile(registryPath.toString(), vfs, true,
				doc);
	}

	protected boolean writeFile(VFSPath path, FileItem file) {
		if (!vfs.exists(path)) {
			OutputStream outputStream = null;
			try {
				outputStream = vfs.getOutputStream(path);
				outputStream.write(file.get());
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			try {
				outputStream.close();
			} catch (IOException e) {
				// IGNORE
			}
			return true;
		}
		return false;

	}

}
