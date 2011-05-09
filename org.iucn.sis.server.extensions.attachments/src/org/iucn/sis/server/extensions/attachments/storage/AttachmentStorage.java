package org.iucn.sis.server.extensions.attachments.storage;

import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.restlet.resource.ResourceException;

public interface AttachmentStorage {
	
	/**
	 * Delete the attachment with the given storage key.
	 * @param key the storage key
	 * @throws ResourceException
	 */
	public void delete(String key) throws ResourceException;
	
	/**
	 * Fetch the attachment given its storage key.
	 * @param key the storage key
	 * @return an input stream to access the file
	 * @throws ResourceException
	 */
	public InputStream get(String key) throws ResourceException;
	
	/**
	 * Save this file to storage. The attachment model 
	 * will contains the appropriate fields for this 
	 * attachment.
	 * @param attachment the attachment model object
	 * @param file the file item information garnered from upload.
	 * @return the unique storage key to access this file with later.
	 */
	public String save(Assessment assessment, FieldAttachment attachment, FileItem file) throws ResourceException;
	
	/**
	 * Updates an existing file to the given version. 
	 * @param assessment the assessment
	 * @param attachment the attachment model object
	 * @param file the file item information garnered from upload
	 * @throws ResourceException
	 */
	public void update(Assessment assessment, FieldAttachment attachment, FileItem file) throws ResourceException;

}
