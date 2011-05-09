package org.iucn.sis.server.extensions.attachments.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.fileupload.FileItem;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.util.MD5Hash;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;


public class VFSAttachmentStorage implements AttachmentStorage {
	
	private final VFS vfs;
	private final VFSPath rootDir;
	
	public VFSAttachmentStorage() {
		this(SIS.get().getVFS());
	}
	
	public VFSAttachmentStorage(VFS vfs) {
		this.vfs = vfs;
		this.rootDir = new VFSPath("/attachments");
		
		if (!vfs.exists(rootDir)) {
			try {
				vfs.makeCollection(rootDir);
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}
	
	@Override
	public void delete(String key) throws ResourceException {
		try {
			vfs.delete(new VFSPath(key));
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	public InputStream get(String key) throws ResourceException {
		try {
			return vfs.getInputStream(new VFSPath(key));
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		}
	}
	
	@Override
	public String save(Assessment assessment, FieldAttachment attachment, FileItem file) throws ResourceException {
		MD5Hash hash = new MD5Hash(attachment.getName());
		VFSPathToken token = new VFSPathToken(hash.toString());
		
		VFSPath uri = getAssessmentPath(Integer.toString(assessment.getId())).child(token);
		
		try {
			writeFile(uri, file, false);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		return uri.toString();
	}
	
	@Override
	public void update(Assessment assessment, FieldAttachment attachment,
			FileItem file) throws ResourceException {
		VFSPath uri = new VFSPath(attachment.getKey());
		
		try {
			writeFile(uri, file, true);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	private VFSPath getAssessmentPath(String assessmentID) {
		return new VFSPath(rootDir.toString() + "/"
				+ FilenameStriper.getIDAsStripedPath(assessmentID));
	}
	
	private void writeFile(VFSPath path, FileItem file, boolean overwrite) throws IOException {
		if (overwrite || !vfs.exists(path)) {
			OutputStream outputStream = vfs.getOutputStream(path);
			outputStream.write(file.get());
			
			try {
				outputStream.close();
			} catch (IOException e) {
				// IGNORE
			}
		}
		else
			throw new ConflictException();
	}

}
