package org.iucn.sis.client.api.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.shared.api.debug.Debug;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GWTResponseException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.NativeDocumentSerializer;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.XMLWritingUtils;

public class SIS {
	
	private static Boolean isOnline = null;
	private static Boolean hasAnalytics = null;
	private static Boolean isAnalyticsRecorded = null;
	
	public static void recordAnalytics(String pageName) {
		if (hasAnalytics == null) {
			try {
				hasAnalytics = _hasAnalytics();
			} catch (Throwable e) {
				Debug.println("Error determining analytics: {0}", e);
			}
			if (hasAnalytics == null) {
				Debug.println("Failed to determine status of analytics");
				hasAnalytics = false;
			}
			else
				Debug.println("Analytics enabled: {0}", hasAnalytics);
		}
		
		if (hasAnalytics.booleanValue()) {
			boolean result = _recordAnalyticsHit(pageName);
			if (isAnalyticsRecorded == null)
				Debug.println("Analytics recorded: {0}", isAnalyticsRecorded = result);
		}
	}
	
	public static native boolean _hasAnalytics() /*-{
	    return $wnd._gaq ? true : false;
	}-*/;
	
	public static native boolean _recordAnalyticsHit(String pageName) /*-{
		try {
	    	$wnd._gaq.push(['_trackPageview', pageName]);
	    	return true;
	    } catch (e) {
	    	return false;
	    }
	}-*/;
	
	public static String getBuildNumber() {
		String buildNumber = "2.0.0";
		try {
			buildNumber = RootPanel.get("version").getElement().getInnerText();
		} catch (Throwable e) {
			Debug.println("Error loading build number.");
		}
		return buildNumber;
	}
	
	public static boolean isDebugMode() {
		return "true".equals(com.google.gwt.user.client.Window.Location.getParameter("debug"));
	}
	
	public static boolean isOffline() {
		return !isOnline();
	}
	
	public static boolean isOnline() {
		if (isOnline == null) {
			boolean online = true;
			try {
				online = !"false".equals(RootPanel.get("online").getElement().getInnerText());
			} catch (Throwable e) {
				Debug.println("Error determining connection status.");
			}
			if (online && UriBase.getInstance().isHostedMode())
				online = !"true".equals(Window.Location.getParameter("offline"));
			
			isOnline = online;
		}
		return isOnline;
	}
	
	public static void fetchList(final Collection<String> uriList, final Map<String, GenericCallback<NativeDocument>> listeners) {
		fetchList(uriList, listeners, null, true);
	}
	
	public static void fetchList(final Collection<String> uriList, final Map<String, GenericCallback<NativeDocument>> listeners, final GenericCallback<String> wayBack, final boolean hideLoadingScreen) {
		final StringBuilder entity = new StringBuilder();
		entity.append("<root>");
		for (String uri : uriList)
			entity.append(XMLWritingUtils.writeCDATATag("uri", uri.replaceFirst("/proxy-service", "")));
		entity.append("</root>");
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getSISBase() + "/utils/documents", entity.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final Map<String, Response> documents = new HashMap<String, Response>();
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
					
					documents.put(uri, new Response(subDoc, code));
				}
				
				DocumentLoader loader = new DocumentLoader(documents, listeners, true);
				loader.setFinishedListener(new SimpleListener() {
					public void handleEvent() {
						DeferredCommand.addCommand(new Command() {
							public void execute() {
								if (hideLoadingScreen)
									WindowUtils.hideLoadingAlert();
								
								if (wayBack != null)
									wayBack.onSuccess(null);
							}
						});
					}
				});
				
				DeferredCommand.addPause();
				DeferredCommand.addCommand(loader);
			}
			public void onFailure(Throwable caught) {
				wayBack.onFailure(caught);
			}
		});
	}
	
	public static class Response {
		private NativeDocument document;
		private int code;
		public Response(NativeDocument document, int code) {
			this.document = document;
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
		
		public NativeDocument getDocument() {
			return document;
		}
		
		public boolean isSuccess() {
			return code >= 200 && code <= 299;
		}
	}
	
	public static class DocumentLoader implements IncrementalCommand {
		
		Map<String, Response> documents;
		Map<String, GenericCallback<NativeDocument>> listeners;
		
		Stack<String> uris;
		
		SimpleListener finished;
		
		boolean showLoadingScreens;
		
		public DocumentLoader(Map<String, Response> documents, Map<String, GenericCallback<NativeDocument>> listeners, boolean showLoadingScreens) {
			this.documents = documents;
			this.listeners = listeners;
			this.showLoadingScreens = showLoadingScreens;
			
			uris = new Stack<String>();
			for (String key : documents.keySet())
				uris.push(key);
		}
		
		public void setFinishedListener(SimpleListener finished) {
			this.finished = finished;
		}
		
		@Override
		public boolean execute() {
			if (uris.isEmpty()) {
				if (finished != null)
					finished.handleEvent();
				return false;
			}
			else {
				if (showLoadingScreens)
					WindowUtils.showLoadingAlert("Loading data...");
				
				String uri = uris.pop();
				Response response = documents.remove(uri);
				
				GenericCallback<NativeDocument> callback = listeners.get(uri);
				if (callback != null) {
					if (response.isSuccess())
						callback.onSuccess(response.getDocument());
					else
						callback.onFailure(new GWTResponseException(response.getCode()));
				}
				
				return true;
			}
		}
	}

}
