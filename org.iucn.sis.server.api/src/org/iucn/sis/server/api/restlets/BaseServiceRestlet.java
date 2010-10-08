package org.iucn.sis.server.api.restlets;

import org.iucn.sis.shared.api.debug.Debug;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.VFS;

/**
 * BaseServiceRestlet.java
 * 
 * This restlet provides a means of creating 
 * service restlets with simplified methods 
 * exposed that handle some of the more 
 * mundane operations related to handling 
 * requests, as well as providing exception 
 * handling.
 * 
 * Use this instead of directly using 
 * ServiceRestlet to get this free lunch.
 * 
 * @author carl.scott
 *
 */
public abstract class BaseServiceRestlet extends ServiceRestlet {

	public BaseServiceRestlet(Context context) {
		super(context);
	}

	public BaseServiceRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	public BaseServiceRestlet(VFS vfs, Context context) {
		super(vfs, context);
	}

	@Override
	public final void performService(Request request, Response response) {
		if (Method.GET.equals(request.getMethod())) {
			final Representation representation;
			try {
				representation = handleGet(request, response);
			} catch (ResourceException e) {
				response.setStatus(e.getStatus());
				return;
			} catch (Throwable e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e, "Uncaught exception occurred");
				Debug.println(e);
				return;
			}
			
			updateStatus(response);
			
			if (representation != null)
				response.setEntity(representation);
		}
		else if (Method.POST.equals(request.getMethod())) {
			try {
				handlePost(request.getEntity(), request, response);
			} catch (ResourceException e) {
				response.setStatus(e.getStatus());
				return;
			} catch (Throwable e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e, "Uncaught exception occurred");
				Debug.println(e);
				return;
			}
			
			updateStatus(response);
		}
		else if (Method.PUT.equals(request.getMethod())) {
			try {
				handlePut(request.getEntity(), request, response);
			} catch (ResourceException e) {
				response.setStatus(e.getStatus());
				return;
			} catch (Throwable e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e, "Uncaught exception occurred");
				Debug.println(e);
				return;
			}
			
			updateStatus(response);
		}
		else if (Method.DELETE.equals(request.getMethod())) {
			try {
				handleDelete(request, response);
			} catch (ResourceException e) {
				response.setStatus(e.getStatus());
				return;
			} catch (Throwable e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e, "Uncaught exception occurred");
				Debug.println(e);
				return;
			}
			
			updateStatus(response);
		}
		else  {
			try {
				handleMethod(request.getMethod(), request, response);
			} catch (ResourceException e) {
				response.setStatus(e.getStatus());
				return;
			} catch (Throwable e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e, "Uncaught exception occurred");
				Debug.println(e);
				return;
			}
			
			updateStatus(response);
		}
	}
	
	private void updateStatus(Response response) {
		if (!response.getStatus().isSuccess() && !response.getStatus().isRedirection())
			response.setStatus(Status.SUCCESS_OK);
	}
	
	public Representation handleGet(Request request, Response response) throws ResourceException {
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	public void handlePost(Representation entity, Request request, Response response) throws ResourceException {
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	public void handlePut(Representation entity, Request request, Response response) throws ResourceException {
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	public void handleDelete(Request request, Response response) throws ResourceException {
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}

	public void handleMethod(Method method, Request request, Response response) throws ResourceException {
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	
	protected Document getEntityAsDocument(Representation entity) throws ResourceException {
		try {
			return new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
	}
	
	protected Representation createSuccessMessage(String message) {
		return new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createConfirmDocument(message));
	}
	
	protected Representation createFailureMessage(String message) {
		return new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createErrorDocument(message));
	}
	
}
