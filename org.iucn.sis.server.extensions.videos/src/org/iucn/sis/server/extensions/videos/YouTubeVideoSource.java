package org.iucn.sis.server.extensions.videos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.shared.api.models.VideoSource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;

/**
 * YouTubeVideoSource.java
 * 
 * Connect to a YouTube video source via PlayList RSS and 
 * grab data.
 * 
 * @author carl.scott@solertium.com
 *
 */
public class YouTubeVideoSource extends BaseServiceRestlet {
	
	public static final String SOURCE_KEY = "org.iucn.sis.server.extensions.videos.sources.youtube";
	
	public YouTubeVideoSource(Context context) {
		super(context);
	}

	@Override
	public void definePaths() {
		paths.add("/sources/youtube/featured/image.png");
		paths.add("/sources/youtube");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		if (request.getResourceRef().getPath().endsWith("image.png")) {
			return new InputRepresentation(getClass().getResourceAsStream("sis-video.png"), MediaType.IMAGE_PNG);
		}
		
		String url = SIS.get().getSettings(getContext()).getProperty(SOURCE_KEY);
		if (url == null)
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
		
		final Request extReq = new Request(Method.GET, url);
		final Response extResp = getContext().getClientDispatcher().handle(extReq);
		
		if (!extResp.getStatus().isSuccess())
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
		
		final Document respDoc = getEntityAsDocument(extResp.getEntity());
		
		final List<VideoSource> list = new ArrayList<VideoSource>();
		final NodeList nodes = respDoc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if ("entry".equals(node.getNodeName())) {
				VideoSource video = new VideoSource();
				video.setImage("/sources/youtube/featured/image.png");
				NodeCollection children = new NodeCollection(node.getChildNodes());
				for (Node child : children) {
					if ("title".equals(child.getNodeName()))
						video.setTitle(child.getTextContent());
					else if ("content".equals(child.getNodeName()))
						video.setCaption(child.getTextContent());
					else if ("link".equals(child.getNodeName())) {
						if ("alternate".equals(BaseDocumentUtils.impl.getAttribute(child, "rel"))) {
							Reference videoRef = new Reference(BaseDocumentUtils.impl.getAttribute(child, "href"));
							String videoID = videoRef.getQueryAsForm().getFirstValue("v");
							if (videoID == null)
								video.setUrl(BaseDocumentUtils.impl.getAttribute(child, "href"));
							else
								video.setUrl("http://www.youtube.com/embed/" + videoID);
						}
					}
				}
				list.add(video);
			}
		}
		
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		for (VideoSource source : list)
			out.append(source.toXML());
		out.append("</root>");
		
		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
	}

}
