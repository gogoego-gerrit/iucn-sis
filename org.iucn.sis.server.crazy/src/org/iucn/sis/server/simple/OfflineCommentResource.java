package org.iucn.sis.server.simple;

import java.util.Date;

import net.jcip.annotations.NotThreadSafe;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.util.SysDebugger;

@NotThreadSafe
public class OfflineCommentResource extends Resource {

	String latestComment = null;

	public OfflineCommentResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		if (request.getMethod().equals(Method.POST)) {
			Form f = request.getEntityAsForm();
			latestComment = f.getFirstValue("comment");
			SysDebugger.getInstance().println("latest comment " + latestComment);
		}
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handleGet() {
		String s = "<html><head><title>Comments</title></head><body style='font-family:Verdana; font-size:x-small'>"
				+ "<form method='post'><div><span style='font-weight:bold'>rob.heittman</span> 10 Feb 2008, 8:02 pm</div>"
				+ "<div>Please change some factors, then re-run the expert system.</div>"
				+ "<div style='padding-top:10px'> <span style='font-weight:bold'>liz.schwartz</span> 10 Feb 2008, 8:14 pm</div>"
				+ "<div>Be sure to use some uncertainty!</div>";
		if (latestComment != null) {
			SysDebugger.getInstance().println("insert " + latestComment);
			s = s + "<div style='padding-top:10px'> <span style='font-weight:bold'>jim.ragle</span> "
					+ new Date().toString() + "</div>" + "<div>" + latestComment + "</div>";
		}
		s = s
				+ "<div style='padding-top:10px'> <span style='font-weight:bold'>Post New Comment</span></div>"
				+ "<div><textarea name='comment' rows='4' cols='20' style='border:1px solid #a0a0c0; background: #f0f0f0; font-family:Verdana; font-size:x-small'></textarea></div>"
				+ "<div><input type='submit' value='Post'></div>" + "</form></body></html>";
		SysDebugger.getInstance().println("yay " + s);
		getResponse().setEntity(new StringRepresentation(s, MediaType.TEXT_HTML));
	}

	@Override
	public void handlePost() {
		handleGet();
	}

}
