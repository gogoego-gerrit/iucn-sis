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

package com.solertium.vfs.restlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.util.DateHelper;
import com.solertium.util.restlet.MediaTypeManager;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.VersionedVFS;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

@NotThreadSafe
public class VFSVersionAccessResource extends Resource {

	protected final Reference ref;
	protected final VFSPath uri;
	protected final VersionedVFS vfs;

	public VFSVersionAccessResource(final Context context,
			final Request request, final Response response) {
		super(context, request, response);
		setModifiable(true);

		final VFS uvfs = ((VFSProvidingApplication) Application.getCurrent())
				.getVFS();
		if (uvfs instanceof VersionedVFS)
			vfs = (VersionedVFS) uvfs;
		else
			vfs = null;

		//Get Remaining part includes the query portion of the request
		String encodedUri = request.getResourceRef().getRemainingPart();
		try {
			uri = VFSResource.decodeVFSPath(encodedUri);
		} catch (VFSPathParseException vx) {
			vx.printStackTrace();
			throw new RuntimeException(vx);
		}
		ref = new Reference(request.getResourceRef().toString());
		
		if (vfs == null || ref == null)
			return;

		if (vfs.exists(uri)) {
			getVariants().add(new Variant(MediaType.TEXT_URI_LIST));
			getVariants().add(new Variant(MediaType.TEXT_PLAIN));
			getVariants().add(new Variant(MediaType.TEXT_XML));
		} else {
			final VFSPath lh = uri.getCollection();
			final String rh = uri.getName();
			if (("HEAD".equals(rh) && vfs.exists(lh)) || vfs.exists(lh, rh)) {
				getVariants().add(
						new Variant(MediaTypeManager.getMediaType(lh.toString())));
				if (!getVariants().contains(MediaType.APPLICATION_OCTET_STREAM)) {
					getVariants().add(
							new Variant(MediaType.APPLICATION_OCTET_STREAM));
				}
				getVariants().add(new Variant(MediaType.TEXT_URI_LIST));
			} else {
				System.out.println("Requested version " + lh + ":" + rh
						+ " not found");
			}
		}
	}

	@Override
	public Representation represent(final Variant variant) {
		try {
			if (vfs.exists(uri)) {
				if (vfs.isCollection(uri)) { // normal collection
					if (!ref.toString().endsWith("/"))
						ref.addSegment("");
					final VFSPathToken[] vl = vfs.list(uri);
					final ReferenceList rl = new ReferenceList(vl.length);
					for (final VFSPathToken token : vl)
						rl.add(new Reference(ref, token.toString()).getTargetRef());
					final Representation result = rl.getTextRepresentation();
					if (!variant.getMediaType().equals(MediaType.TEXT_URI_LIST))
						result.setMediaType(MediaType.TEXT_PLAIN);
					return result;
				} else { // collection of versions for uri
					MediaType mt = variant.getMediaType();
					System.out.println(""+mt);
					if (mt.equals(MediaType.TEXT_URI_LIST) || mt.equals(MediaType.TEXT_PLAIN)){
						if (!ref.toString().endsWith("/"))
							ref.addSegment("");
						final List<String> vl = vfs.getRevisionIDsBefore(uri, null,
								-1);
						final ReferenceList rl = new ReferenceList(vl.size() + 1);
						rl.add(new Reference(ref, "HEAD").getTargetRef());
						for (final String s : vl)
							rl.add(new Reference(ref, s).getTargetRef());
						final Representation result = rl.getTextRepresentation();
						result.setMediaType(mt);
						return result;
					} else { // assume mt.equals(MediaType.TEXT_XML))
						StringBuilder sb = new StringBuilder();
						sb.append("<?xml version=\"1.0\"?>\n");
						sb.append("<versions>\n");
						if (!ref.toString().endsWith("/"))
							ref.addSegment("");
						final List<String> vl = vfs.getRevisionIDsBefore(uri, null,
								-1);
						for (final String s : vl){
							Reference r = new Reference(ref, s).getTargetRef();
							Date rdate = new Date(vfs.getLastModified(uri, s));
							SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.httpDateFormat);
							sb.append("  <version uri=\""+r.toString()+"\" date=\""+sdf.format(rdate)+"\"/>\n");
						}		
						sb.append("</versions>");
						final Representation result = new StringRepresentation(sb.toString(),mt);
						return result;
					}
				}
			} else {
				// special case for when Directory requests a TEXT_URI_LIST
				if (variant.getMediaType().equals(MediaType.TEXT_URI_LIST)) {
					final ReferenceList rl = new ReferenceList(0);
					rl.add(ref);
					return rl.getTextRepresentation();
				}
				final VFSPath lh = uri.getCollection();
				final String rh = uri.getName();
				if ("HEAD".equals(rh)) {
					return VFSResource.getRepresentationForFile(vfs, lh);
				} else {
					final Representation r = new InputRepresentation(vfs
							.getInputStream(lh, rh), MediaTypeManager
							.getMediaType(lh.toString()));
					r.setSize(vfs.getLength(lh, rh));
					r
							.setModificationDate(new Date(vfs.getLastModified(
									lh, rh)));
					return r;
				}
			}
		} catch (final NotFoundException nf) {
			nf.printStackTrace();
			throw new RuntimeException(
					"A VFS resource reported as existing could not be found.");
		}
	}
	
	public void handlePost() {
		try {
			acceptRepresentation(getRequest().getEntity());
		} catch (ResourceException re) {
            getResponse().setStatus(re.getStatus());
        }
	}
	
	public void acceptRepresentation() {
		String version = getHeader("Version");
		if (version == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
			
		if (!vfs.exists(uri, version)) {
			getResponse().setStatus(Status.CLIENT_ERROR_GONE);
			return;
		}
			
		try {
			copyStream(
				vfs.getInputStream(uri, version), 
				vfs.getOutputStream(uri)
			);
			getResponse().setStatus(Status.SUCCESS_OK);
		} catch (IOException e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}
	
	private void copyStream(final InputStream is, final OutputStream os)
		throws IOException {
		final byte[] buf = new byte[65536];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
		is.close();
		os.close();
	}
	
	private String getHeader(final String header) {
		String ret = null;
		try {
			final org.restlet.data.Form headers = (org.restlet.data.Form) getRequest()
				.getAttributes().get("org.restlet.http.headers");
			ret = headers.getFirstValue(header);
			if (ret == null)
				ret = headers.getFirstValue(header.toLowerCase());
		} catch (final Exception poorly_handled) {
			poorly_handled.printStackTrace();
		}
		return ret;
	}
}
