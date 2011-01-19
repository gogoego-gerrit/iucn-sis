package org.iucn.sis.server.api.restlets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SIS;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class SettingsRestlet extends BaseServiceRestlet {

	private final Collection<String> settings;
	
	public SettingsRestlet(Context context, Collection<String> settings) {
		super(context);
		
		//FIXME: this can be split into multiple files per app?
		this.settings = settings;
	}
	
	@Override
	public void definePaths() {
		paths.add("/settings");
	}
	
	@Override
	public Representation handleGet(Request request, Response response)
			throws ResourceException {
		
		final Properties properties = SIS.get().getSettings(getContext());
		
		final StringBuilder builder = new StringBuilder();
		builder.append("<html><head>");
		builder.append("<title>Manage Settings</title>");
		builder.append("</head><body><h1>Application Settings</h1>");
		builder.append("<form method=\"POST\">");
		for (String setting : settings) {
			builder.append("<label for=\"" + setting + "\">" + setting + "</label>");
			builder.append("<input type=\"text\" name=\"" + setting + "\" value=\""+ 
				properties.getProperty(setting, "") + "\" /><br/>");
		}
		builder.append("<br/>");
		builder.append("<input type=\"submit\" value=\"Submit\" />");
		builder.append("</form></body></html>");
		
		return new StringRepresentation(builder.toString(), MediaType.TEXT_HTML);
	}
	
	@Override
	public void handlePost(Representation entity, Request request,
			Response response) throws ResourceException {
		
		final Form form;
		try {
			form = new Form(entity);
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final Properties properties = SIS.get().getSettings(getContext());
		
		for (String name : form.getNames()) {
			String value = form.getFirstValue(name);
			if (value != null)
				properties.setProperty(name, value);
		}
		
		final String rootFolder = 
			GoGoEgo.getInitProperties().getProperty("sis_settings", "/ebs/sis/test/files/settings");
		
		File folder = new File(rootFolder);
		if (!folder.exists()) {
			try {
				folder.mkdirs();
			} catch (Exception e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		
		try {
			properties.store(new FileWriter(new File(folder, "global.properties")), null);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		response.redirectSeeOther(request.getResourceRef().getPath() + "?token=" + new Date().getTime());
	}

}
