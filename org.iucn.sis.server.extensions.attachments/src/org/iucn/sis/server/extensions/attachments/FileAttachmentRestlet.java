package org.iucn.sis.server.extensions.attachments;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.assessments.AssessmentAttachment;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.restlet.VFSResource;

public class FileAttachmentRestlet extends BaseServiceRestlet {

	private final VFSPath rootDir;
	private final VFSPathToken registryFile = new VFSPathToken("_attachments.xml");
	private final VFS vfs;
	

	public FileAttachmentRestlet(Context context) {
		super(context);
		
		rootDir = new VFSPath("/attachments");
		vfs = SIS.get().getVFS();
	}
	
	public void definePaths() {
		paths.add("/attachment/{assessmentID}");
		paths.add("/attachment/file/{attachmentID}");
	}

	private boolean addToRegistry(VFSPath registryPath,
			AssessmentAttachment attachment) {
		String doc = DocumentUtils.getVFSFileAsString(registryPath.toString(), vfs);
		if (doc == null)
			doc = "<attachments></attachments>";
		
		doc = doc.replace("</attachments>", "");
		doc += attachment.toXML() + "\r\n</attachments>";
		return DocumentUtils.writeVFSFile(registryPath.toString(), vfs, doc);
	}

	private boolean deleteFile(VFSPath path) {
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

	private String getAssessmentIDFromAttachmentID(String attachmentID) {
		int splitIndex = attachmentID.lastIndexOf("_");
		return attachmentID.substring(0, splitIndex);
	}

	private VFSPath getAssessmentPath(String assessmentID) {
		return new VFSPath(rootDir.toString() + "/"
				+ FilenameStriper.getIDAsStripedPath(assessmentID));
	}

	private String getAttachmentID(String assessmentID,
			String attachmentFileName) {
		if (attachmentFileName.contains("/")) {
			attachmentFileName = attachmentFileName.substring(
					attachmentFileName.lastIndexOf("/") + 1, attachmentFileName
							.length());
		}
		return assessmentID + "_" + attachmentFileName;
	}

	private VFSPath getAttachmentPath(String attachmentID) {
		VFSPathToken token = new VFSPathToken(
				getFilenameFromAttachmentID(attachmentID));
		return getAssessmentPath(getAssessmentIDFromAttachmentID(attachmentID))
				.child(token);
	}

	private String getFilenameFromAttachmentID(String attachmentID) {
		int splitIndex = attachmentID.lastIndexOf("_");
		return attachmentID.substring(splitIndex + 1);
	}
	
	@Override
	public void handleDelete(Request request, Response response) throws ResourceException {
		String attachmentID = getAttachmentID(request);
		if (attachmentID == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		if (removeFromRegistry(getAssessmentPath(
				getAssessmentIDFromAttachmentID(attachmentID)).child(
				registryFile), attachmentID)
				&& deleteFile(getAttachmentPath(attachmentID))) {

			response.setStatus(Status.SUCCESS_OK);
			response.setEntity("Attachment deleted", MediaType.TEXT_PLAIN);
		} else {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
					"Unable to delete attachment");
		}
	}
	
	@Override
	public Representation handleGet(Request request, Response response) throws ResourceException {
		final String assessmentID = getAssessmentID(request);
		final String attachmentID = getAttachmentID(request);

		// FIND ATTACHMENT
		if (assessmentID == null && attachmentID != null) {
			VFSPath attachmentFile = getAttachmentPath(attachmentID);
			if (vfs.exists(attachmentFile)) {
				Representation rep;
				try {
					rep = VFSResource.getRepresentationForFile(vfs, attachmentFile);
				} catch (NotFoundException e) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Attachment not found", e);
				}
				return rep;
			} else {
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Attachment not found");
			}
		}
		else if (assessmentID != null && attachmentID == null) {
			VFSPath path = getAssessmentPath(assessmentID);
			if (vfs.exists(path)) {
				Document registryDoc = DocumentUtils.getVFSFileAsDocument(path
						.child(registryFile).toString(), vfs);
				if (registryDoc == null)
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
							"Unable to find registry for attachment "
									+ assessmentID);

				return (new DomRepresentation(MediaType.TEXT_XML, registryDoc));
			} else {
				return new StringRepresentation("<attachments></attachments>",
						MediaType.TEXT_XML);
			}
		}
		else
			return super.handleGet(request, response);
	}

	private void handleModifyAttachment(Request request, Response response,
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
	
	@Override
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		String assessmentID = getAssessmentID(request);
		String attachmentID = getAttachmentID(request);
		if (assessmentID != null)
			handlePost(request, response, assessmentID);
		else if (attachmentID != null)
			handleModifyAttachment(request, response, attachmentID);
		else
			super.handlePost(entity, request, response);
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

	private boolean isValidFilename(String attachmentFileName) {
		if (attachmentFileName.contains("/")) {
			attachmentFileName = attachmentFileName.substring(
					attachmentFileName.lastIndexOf("/") + 1, attachmentFileName
							.length());
		}
		return !attachmentFileName.contains("_");
	}
	
	private String getAssessmentID(Request request) {
		return (String) request.getAttributes().get("assessmentID");
	}
	
	private String getAttachmentID(Request request) {
		return (String) request.getAttributes().get("attachmentID");
	}

	private boolean removeFromRegistry(VFSPath registryPath,
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

	private boolean updatedRegistry(VFSPath registryPath,
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

	private boolean writeFile(VFSPath path, FileItem file) {
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
