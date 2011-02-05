package org.iucn.sis.server.extensions.user.resources;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.PermissionIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.ElementCollection;

public class BatchUpdateRestlet extends BaseServiceRestlet {

	public BatchUpdateRestlet(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/list/batch");
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final UserIO userIO = new UserIO(session);
		final PermissionIO permissionIO = new PermissionIO(session);
		
		final ElementCollection nodes = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("user")	
		);
		for (Element el : nodes) {
			final Integer id = Integer.valueOf(el.getAttribute("id"));
			final User user;
			try {
				user = SISPersistentManager.instance().loadObject(session, User.class, id);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			final ElementCollection fields = new ElementCollection(el.getElementsByTagName("field"));
			for (Element field : fields) {
				String name = field.getAttribute("name");
				String value = field.getTextContent();
				if ("quickgroup".equalsIgnoreCase(name)) {
					if ("".equals(value))
						user.getPermissionGroups().clear();
					else {
						String[] csv = value.split(",");
						for (String current : csv) {
							if ("".equals(current))
								continue;
							
							PermissionGroup group;
							try {
								group = permissionIO.getPermissionGroup(current);
							} catch (PersistentException e) {
								continue;
							}
							
							user.getPermissionGroups().add(group);
						}
					}
				}
				else {
					//TODO: implement batch update by property
				}
			}
			try {
				userIO.saveUser(user);
			} catch (PersistentException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
	}

}
