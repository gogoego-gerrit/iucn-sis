package com.solertium.util.querybuilder.struct;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;


public class QBLookupTable {

	private final String tableName;
	private final String keyColumn;
	private final String valueColumn;

	private final String lookupRoute;

	public QBLookupTable(NativeElement node, String lookupRoute) {
		this.lookupRoute = lookupRoute;
		tableName = node.getAttribute("table");
		keyColumn = node.getAttribute("keyColumn");
		valueColumn = node.getAttribute("valueColumn");
	}

	public String getKeyColumn() {
		return keyColumn;
	}

	public String getTableName() {
		return tableName;
	}

	public String getValueColumn() {
		return valueColumn;
	}

	public void getLookupValues(final GenericCallback<NativeDocument> callback) {
		String body = "<root>" +
			"<lookup table=\"" + tableName + "\" " +
				"keyColumn=\"" + keyColumn + "\" " +
				"valueColumn=\"" + valueColumn + "\" />" +
			"</root>";

		final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
		doc.post(lookupRoute, body, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			public void onSuccess(String result) {
				callback.onSuccess(doc);
			}
		});
	}
}
