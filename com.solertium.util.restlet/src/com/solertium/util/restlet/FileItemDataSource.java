/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.util.restlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.fileupload.FileItem;

/**
 * FileItemDataSource
 * 
 * Represents a file item as a datasource, so it can 
 * be attached to mail.  File items actually implement 
 * this class, but it's not recognized by the datasource impl.
 *
 * @author carl.scott
 *
 */
public class FileItemDataSource implements DataSource {
	
	private FileItem item;
	
	public FileItemDataSource(FileItem item) {
		this.item = item;
	}
	
	public String getContentType() {
		return item.getContentType();
	}

	public InputStream getInputStream() throws IOException {
		return item.getInputStream();
	}

	public String getName() {
		return item.getName();
	}

	public OutputStream getOutputStream() throws IOException {
		return item.getOutputStream();
	}
	
}