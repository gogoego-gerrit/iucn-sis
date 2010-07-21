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

import org.restlet.data.MediaType;
import org.restlet.service.MetadataService;

public class MediaTypeManager {
	
	private static MetadataService metaDataService;

	public static MetadataService getMetadataService() {
		synchronized (MediaTypeManager.class){
			if (metaDataService == null) {
				metaDataService = new MetadataService();
				metaDataService.addExtension("htm", MediaType.TEXT_HTML);
			}
		}
		return metaDataService;
	}

	/**
	 * Returns the MediaType given the URI (just checks the extension against a Map)
	 * @param uri
	 * @return
	 */
    public static MediaType getMediaType(String uri){
		String ext = uri.substring(uri.lastIndexOf(".")+1);
		MediaType mediaType = (MediaType) getMetadataService().getMetadata(ext);
		
		if (mediaType==null) 
			mediaType = MediaType.APPLICATION_OCTET_STREAM;
		
		return mediaType;
    }

}
