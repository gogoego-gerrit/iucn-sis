/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.gogoego.server;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

/**
 * Uses GZip encoding to save bandwidth on text media types.  If this uses
 * too much CPU for your platform, it can be turned off bodily by setting
 * DISABLE_GZIP=true in your configuration properties.
 * 
 * @author Rob Heittman
 */
public class GzipFilter extends Filter {

	private boolean disable = false;
	
	public GzipFilter(final Context context) {
		super(context);
		disable = "true".equalsIgnoreCase(GoGoEgo.getInitProperties().getProperty("DISABLE_GZIP"));
	}
	
	@Override
	public void afterHandle(final Request request, final Response response) {
		/*
		 * Explicit check for GZIP
		 */
		if (disable || !request.getAttributes().containsKey(Constants.ALLOW_GZIP)) 
			return;
		final Protocol p = request.getResourceRef().getSchemeProtocol();
		// Only encode actual HTTP traffic please
		if (Protocol.HTTP.equals(p) || Protocol.HTTPS.equals(p)) {
			Representation e = response.getEntity();
			if(e!=null && e.getMediaType() != null){
				MediaType mt = e.getMediaType();
				if(mt.equals(MediaType.TEXT_HTML)||
				   mt.equals(MediaType.APPLICATION_JAVASCRIPT)||
				   mt.equals(MediaType.TEXT_CSS)||
				   mt.equals(MediaType.TEXT_XML)||
				   mt.equals(MediaType.APPLICATION_XML)){
					 if(e.getSize()>1024){
					    response.setEntity(new EncodeRepresentation(Encoding.GZIP,e));
					 }
				}
				
				/*
				 * FIXME this is not the right place for this.  It is a temporary fix to
				 * a Page Speed observation that results in bad IE performance and
				 * suboptimal performance on other browsers.  A better fix would be to
				 * think about how content negotiation is/isn't used in GoGoEgo.  When we
				 * aren't doing any content negotation (e.g. VFSResource) we should
				 * strip these Dimensions at the root.
				 */
				if(mt.equals(MediaType.IMAGE_GIF)||
				   mt.equals(MediaType.APPLICATION_JAVASCRIPT)||
				   mt.equals(MediaType.IMAGE_PNG)||
				   mt.equals(MediaType.TEXT_CSS)||
				   mt.equals(MediaType.IMAGE_JPEG)){
					 // No content negotiation for these types please
					 response.getDimensions().clear();
				}
				
			}
			else if (e != null && e.getMediaType() == null)
				GoGoEgo.debug("error").println("Media type null for {0}", request.getResourceRef());
		}
	}

}
