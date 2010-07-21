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
package com.solertium.gogoego.server.lib.clienttools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.gogoego.api.images.ImageManipulator;
import org.gogoego.api.images.ImageManipulatorPreferences;
import org.gogoego.api.images.ImageProperties;
import org.gogoego.api.images.ImageManipulator.RotationStyle;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.RestletUtils;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * ImageResize.java
 * 
 * Provides image size properties and means to resize an image given well-known
 * properties
 * 
 * @author carl.scott
 * 
 */
public class ImageResize extends Resource {

	public final static int POS_90DEG_ROT = 0;
	public final static int NEG_90DEG_ROT = 1;
	public final static int IMAGE_FLIP = 2;

	private final VFS vfs;
	private final VFSPath uri;

	private ArrayList<String> imgTypes;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	public ImageResize(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		vfs = ServerApplication.getFromContext(context).getVFS();
		uri = new VFSPath(request.getResourceRef().getRemainingPart());

		getVariants().add(new Variant(MediaType.TEXT_XML));

		imgTypes = new ArrayList<String>();
		imgTypes.add("png");
		imgTypes.add("jpg");
		imgTypes.add("jpeg");
		imgTypes.add("gif");
		imgTypes.add("bmp");
		imgTypes.add("icon");
		imgTypes.add("svg");

	}

	/**
	 * Determines if a file is a proper image.
	 * 
	 * @return true if so, false otherwise
	 */
	private boolean isAllowedImageTypes() throws ResourceException {
		return imgTypes.contains(getExtension());
	}
	
	private String getExtension() throws ResourceException {
		String s = uri.getName();
		int index = s.lastIndexOf(".");
		if (index == -1 || s.endsWith("."))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Must supply a valid image.");

		return s.substring(index + 1).toLowerCase();		
	}
	
	private ImageManipulator getImageManipulator() throws ResourceException {
		final ImageManipulatorPreferences preferences = new ImageManipulatorPreferences();
		preferences.setImageType(getExtension());
		
		final ImageManipulator manipulator = 
			PluginAgent.getImageManipulatorBroker().getImageManipulator(
				GoGoEgo.get().getFromContext(getContext()), preferences
			);
	
		if (manipulator == null)
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "No image manipulator found to handle this request, please install a supported plugin.");

		return manipulator;
	}

	/**
	 * Gets properties of an image, specifically the width and height.
	 */
	public Representation represent(Variant variant) throws ResourceException {
		if (!vfs.exists(uri))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		if (!isAllowedImageTypes())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		final ImageManipulator manipulator = getImageManipulator();
		
		final ImageProperties properties;
		try {
			properties = manipulator.getImageProperties(vfs.getInputStream(uri));
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		getResponse().setStatus(Status.SUCCESS_OK);
		
		final Document result = BaseDocumentUtils.impl.newDocument();
		final Element root = result.createElement("root");
		root.appendChild(BaseDocumentUtils.impl.createElementWithText(
			result, "uri", uri.toString())
		);
		//If there are any future additional properties, add them in the same manner
		root.appendChild(BaseDocumentUtils.impl.createElementWithText(
			result, "width", Integer.toString(properties.getWidth()))
		);
		root.appendChild(BaseDocumentUtils.impl.createElementWithText(
			result, "height", Integer.toString(properties.getHeight()))
		);
		result.appendChild(root);
		
		return new GoGoEgoDomRepresentation(result);
	}

	/**
	 * Resizes a graphic based on a given instructions file.
	 */
	public void acceptRepresentation(Representation entity) throws ResourceException {
		if (!vfs.exists(uri))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		if (!isAllowedImageTypes())
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		
		final Document doc;
		try {
			doc = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		}

		final HashMap<String, String> properties = new HashMap<String, String>();
		NodeList nodes = doc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node current = nodes.item(i);
			if (current.getNodeType() != Node.TEXT_NODE) {
				properties.put(current.getNodeName(), current.getTextContent());
			}
		}
		
		final VFSPath target = uri.getCollection().child(new VFSPathToken(properties.get("name")));
		
		final ImageManipulator manipulator = getImageManipulator();
		
		try {
			manipulator.resize(
				vfs.getInputStream(uri), vfs.getOutputStream(target), 
				getExtension(), Integer.parseInt(properties.get("width")), 
				Integer.parseInt(properties.get("height"))
			);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

		getResponse().setStatus(Status.SUCCESS_CREATED);
	}

	public boolean allowRotate() {
		return true;
	}

	/**
	 * Rotates an image based on instruction headers.
	 */
	public void handleRotate() {
		String direction = getHeader("Direction");
		String createNew = getHeader("CreateNew");
		
		final ImageManipulator manipulator;
		try {
			manipulator = getImageManipulator();
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
			return;
		}

		if (direction == null) {
			getResponse()
					.setEntity(new GoGoEgoStringRepresentation("<root><uri>" + uri + "</uri></root>", MediaType.TEXT_XML));
			return;
		}

		if (!"false".equals(createNew))
			createNew = "true";

		if (Boolean.valueOf(createNew)) {
			VFSPath returnURI = uri;
			ImageRotation ir = new ImageRotation();
			
			try {
				if ("left".equals(direction)) {
					returnURI = ir.getRotatedURI(manipulator, getExtension(), vfs, uri, RotationStyle.NEG_90DEG_ROT);
				} else if ("right".equals(direction)) {
					returnURI = ir.getRotatedURI(manipulator, getExtension(), vfs, uri, RotationStyle.POS_90DEG_ROT);
				}
			} catch (NotFoundException e) {
				TrivialExceptionHandler.ignore(this, e);
			} catch (ResourceException e) {
				//???
				TrivialExceptionHandler.ignore(this, e);
			}
			GoGoEgo.debug().println("New Rotated URI is {0}", returnURI);
			getResponse().setEntity(
					new StringRepresentation("<root><uri>" + returnURI + "</uri></root>", MediaType.TEXT_XML));
			
		} else {
			try {
				if ("left".equals(direction)) {
					manipulator.rotate(vfs.getInputStream(uri), vfs.getOutputStream(uri), getExtension(), RotationStyle.NEG_90DEG_ROT);
				} else if ("right".equals(direction)) {
					manipulator.rotate(vfs.getInputStream(uri), vfs.getOutputStream(uri), getExtension(), RotationStyle.POS_90DEG_ROT);
				}
			} catch (NotFoundException e) {
				TrivialExceptionHandler.ignore(this, e);
			} catch (ConflictException e) {
				TrivialExceptionHandler.ignore(this, e);
			} catch (IOException e) {
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
			} catch (ResourceException e) {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
				return;
			}

			getResponse()
					.setEntity(new StringRepresentation("<root><uri>" + uri + "</uri></root>", MediaType.TEXT_XML));
		}
	}

	private String getHeader(final String header) {
		return RestletUtils.getHeader(getRequest(), header);
	}
}