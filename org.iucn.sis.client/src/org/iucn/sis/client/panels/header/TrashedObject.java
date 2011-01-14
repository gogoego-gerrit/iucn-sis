package org.iucn.sis.client.panels.header;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;

public class TrashedObject extends BaseModelData {
	
	private static final long serialVersionUID = 1L;

	public TrashedObject(NativeElement doc) {
		set("id", doc.getAttribute("id"));
		set("taxon", doc.getAttribute("node"));
		set("status", doc.getAttribute("status"));
		set("user", doc.getAttribute("user"));
		set("date", doc.getAttribute("date"));
		set("type", doc.getAttribute("type"));
	}
	

	public void delete(final GenericCallback<String> wayback) {
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.post(UriBase.getInstance().getSISBase() + "/trash/delete", toXML(), new GenericCallback<String>() {
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				wayback.onFailure(arg0);
			}

			public void onSuccess(String arg0) {
				// TODO Auto-generated method stub
				wayback.onSuccess(arg0);
			}
		});
	}

	public String getID() {
		return get("id");
	}

	public String getNodeID() {
		return get("taxon");
	}

	public String getStatus() {
		return get("status");
	}

	public String getType() {
		return get("type");
	}
	
	public String getIdentifier() {
		return getType() + ":" + getStatus();
	}

	public void restore(boolean recurse, final GenericCallback<String> wayback) {

		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.post(UriBase.getInstance().getSISBase() +"/trash/restore/" + recurse, toXML(), new GenericCallback<String>() {
			public void onFailure(Throwable arg0) {
				wayback.onFailure(arg0);
			}

			public void onSuccess(String arg0) {
				wayback.onSuccess(arg0);
			}
		});
	}

	public String toXML() {
		String xml = "<trash>";
		xml += "<data id=\"" + get("id") + "\" type=\"" + get("type") + "\" status=\"" + get("status")
				+ "\" user=\"" + get("user") + "\" date=\"" + get("date") + "\" node=\"" + get("taxon") + "\"></data>";

		xml += "</trash>";
		return xml;
	}
}
