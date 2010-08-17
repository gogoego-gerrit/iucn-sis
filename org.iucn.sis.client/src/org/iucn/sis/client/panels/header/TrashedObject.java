package org.iucn.sis.client.panels.header;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;

import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;

public class TrashedObject extends TableItem {

	private String id;
	private String status;
	private String nodeID;
	private String user;
	private String date;
	private String type;
	private String parent;

	public TrashedObject(NativeElement doc) {
		this(new String[] { doc.getAttribute("date"), doc.getAttribute("type"), doc.getAttribute("id"),
				doc.getAttribute("display"), doc.getAttribute("status"), doc.getAttribute("user") });

		id = doc.getAttribute("id");
		nodeID = doc.getAttribute("node");
		status = doc.getAttribute("status");
		user = doc.getAttribute("user");
		date = doc.getAttribute("date");
		type = doc.getAttribute("type");
		parent = doc.getAttribute("parent");

	}

	public TrashedObject(Object[] values) {
		super(values);
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
		return id;
	}

	public String getNodeID() {
		return nodeID;
	}

	public String getStatus() {
		return status;
	}

	public String getType() {
		return type;
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
		xml += "<data id=\"" + id + "\" parent=\"" + parent + "\" type=\"" + type + "\" status=\"" + status
				+ "\" user=\"" + user + "\" date=\"" + date + "\" node=\"" + nodeID + "\"></data>";

		xml += "</trash>";
		return xml;
	}
}
