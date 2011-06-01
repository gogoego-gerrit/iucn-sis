package org.iucn.sis.shared.conversions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

public class ConverterResource extends Restlet {
	
	@Override
	public void handle(Request arg0, Response arg1) {
		String step = (String)arg0.getResourceRef().getLastSegment();
		if (step == null || step.endsWith(".conversions"))
			step = "all";
		Form parameters = arg0.getResourceRef().getQueryAsForm();
		boolean proceed = "true".equals(arg0.getResourceRef().getQueryAsForm().getFirstValue("proceed"));
		
		boolean run = "true".equals(parameters.getFirstValue("run"));
		if (!run) {
			String url = arg0.getResourceRef().getPath();
			if (arg0.getResourceRef().getQuery() == null || "".equals(arg0.getResourceRef().getQuery()))
				url += "?run=true";
			else
				url += "?" + arg0.getResourceRef().getQuery() + "&run=true";
			arg1.setStatus(Status.SUCCESS_OK);
			arg1.setEntity("<html><head><title>IUCN SIS Conversion Script</title></head>" +
				"<body><div><a href=\"" + url + "\">Click to Execute Script</a></div></body></html>", MediaType.TEXT_HTML);
			return;
		}
		
		if (step.equals("all")) {
			step = "libraries";
			proceed = true;
		}
		
		final PipedInputStream inputStream = new PipedInputStream(); 
		final Representation representation = new OutputRepresentation(MediaType.TEXT_PLAIN) {
			public void write(OutputStream out) throws IOException {
				byte[] b = new byte[8];
				int read;
				while ((read = inputStream.read(b)) != -1) {
					out.write(b, 0, read);
					out.flush();
				}
			}
		};
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(inputStream)), true);
		} catch (IOException e) {
			arg1.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			return;
		}
		
		new Thread(new ConverterWorker(writer, step, proceed, parameters)).start();
		
		arg1.setEntity(representation);
	}

}
