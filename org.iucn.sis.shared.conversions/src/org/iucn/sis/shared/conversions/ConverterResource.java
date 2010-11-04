package org.iucn.sis.shared.conversions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import org.iucn.sis.server.api.application.SISApplication;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

import com.solertium.util.restlet.RestletUtils;

public class ConverterResource extends Restlet {
	
	@Override
	public void handle(Request arg0, Response arg1) {
		String step = (String)arg0.getResourceRef().getLastSegment();
		if (step == null || step.endsWith(".conversions"))
			step = "all";
		boolean proceed = "true".equals(arg0.getResourceRef().getQueryAsForm().getFirstValue("proceed"));
		
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
		
		new Thread(new ConverterWorker(writer, step, proceed)).start();
			
		RestletUtils.addHeaders(arg1, SISApplication.NO_TRANSACTION_HANDLE, "true");
		
		arg1.setEntity(representation);
	}

}
