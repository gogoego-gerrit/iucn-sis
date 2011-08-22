package org.iucn.sis.client.api.utils;

import java.util.Collection;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GWTResponseException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.NativeDocumentSerializer;
import com.solertium.util.portable.XMLWritingUtils;

public class SIS {
	
	public static void fetchList(final Collection<String> uriList, final Map<String, GenericCallback<NativeDocument>> listeners) {
		fetchList(uriList, listeners, null);
	}
	
	public static void fetchList(final Collection<String> uriList, final Map<String, GenericCallback<NativeDocument>> listeners, final GenericCallback<String> wayBack) {
		final StringBuilder entity = new StringBuilder();
		entity.append("<root>");
		for (String uri : uriList)
			entity.append(XMLWritingUtils.writeCDATATag("uri", uri.replaceFirst("/proxy-service", "")));
		entity.append("</root>");
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getSISBase() + "/utils/documents", entity.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("response");
				for (int i = 0; i < nodes.getLength(); i++) {
					NativeElement node = nodes.elementAt(i);
					int code = Integer.valueOf(node.getAttribute("status"));
					
					String uri = null;
					NativeDocument subDoc = null;
					NativeNodeList children = node.getChildNodes();
					for (int k = 0; k < children.getLength(); k++) {
						NativeNode child = children.item(k);
						if ("uri".equals(child.getNodeName()))
							uri = child.getTextContent();
						else { 
							subDoc = NativeDocumentFactory.newNativeDocument();
							subDoc.parse(NativeDocumentSerializer.serialize(child));
						}
					}
					
					if (uri == null)
						continue;
					if (UriBase.getInstance().isHostedMode())
						uri = "/proxy-service" + uri;
					
					GenericCallback<NativeDocument> callback = listeners.get(uri);
					if (callback == null)
						continue;
					
					if (code >= 200 && code <= 299)
						callback.onSuccess(subDoc);
					else
						callback.onFailure(new GWTResponseException(code));
				}
				
				DeferredCommand.addCommand(new Command() {
					public void execute() {
						if (wayBack != null)
							wayBack.onSuccess(null);
					}
				});
			}
			public void onFailure(Throwable caught) {
				wayBack.onFailure(caught);
			}
		});
	}

}
