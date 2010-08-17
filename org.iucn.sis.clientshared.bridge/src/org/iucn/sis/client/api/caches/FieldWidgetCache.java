package org.iucn.sis.client.api.caches;

import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.utils.FieldParser;

import com.google.gwt.user.client.Window;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class FieldWidgetCache {
	public static final FieldWidgetCache impl = new FieldWidgetCache();

	/*
	 * HashMap<String, Display>
	 */
	public HashMap<String, Display> widgetMap = null;

	private FieldParser fieldParser = null;

	private static final int maxURL = 250;

	private FieldWidgetCache() {
		fieldParser = new FieldParser();
		widgetMap = new HashMap<String, Display>();
	}

	// TODO: THIS IS BEING CALLED TOO MANY TIMES!! Fix it.
	public void addAssessmentToDisplay(Display display) {
		if (AssessmentCache.impl.getCurrentAssessment() != null && display != null) {
			Field field = AssessmentCache.impl.getCurrentAssessment().getField(display.getCanonicalName());
			if( field == null )
				field = new Field(display.getCanonicalName(), AssessmentCache.impl.getCurrentAssessment());
		
			display.setData(field);
		}
	}

	private void doListFetch(String names, final GenericCallback<String> wayBack) {
		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		doc.get(UriBase.getInstance().getSISBase() + "/field/" + names, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayBack.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				NativeNodeList fields = doc.getDocumentElement().getElementsByTagName("field");
				for (int i = 0; i < fields.getLength(); i++) {
					Display dis = fieldParser.parseField(fields.elementAt(i));
					if (dis != null && dis.getCanonicalName() != null && !dis.getCanonicalName().equals("")) {
						// SysDebugger.getNamedInstance("info").println(
						// "Putting " + dis + " as " + dis.getCanonicalName());
						widgetMap.put(dis.getCanonicalName(), dis);
					} else
						SysDebugger.getNamedInstance("info").println(
								"Parsed a " + "display with null canonical " + "name. Description is: "
										+ dis.getDescription());
				}

				NativeNodeList trees = doc.getDocumentElement().getElementsByTagName("tree");
				for (int i = 0; i < trees.getLength(); i++) {
					Display dis = fieldParser.parseField(trees.elementAt(i));
					if (dis != null && dis.getCanonicalName() != null && !dis.getCanonicalName().equals(""))
						widgetMap.put(dis.getCanonicalName().trim(), dis);
					else
						SysDebugger.getNamedInstance("info").println(
								"Parsed a " + "display with null canonical " + "name. Description is: "
										+ dis.getDescription());
				}

				wayBack.onSuccess("OK");
			}
		});
	}

	public void doLogout() {
		widgetMap.clear();
	}

	/**
	 * Gets the Widget from the master list, or returns null if it's not found.
	 * 
	 * This DOES NOT attempt to fetch the field description from the server, and
	 * WILL NOT build the Widget if it has not yet been built (see
	 * fetchField(...)).
	 * 
	 * @param canonicalName
	 * @return Display object - MAY BE NULL
	 */
	public Display get(String canonicalName) {
		Display cur = (Display) widgetMap.get(canonicalName);
		addAssessmentToDisplay(cur);

		return cur;
	}

	public String getCurrentFields() {

		Iterator iter = widgetMap.values().iterator();
		StringBuffer xml = new StringBuffer();

		while (iter.hasNext()) {
			xml.append(((Display) iter.next()).toXML());
		}

		return xml.toString();
	}

	/**
	 * Fetches a list of fields in one call. Supplied argument should be the
	 * canonical names of the fields separated by commas. If the asked-for
	 * fields are found in the master list already, this function will call the
	 * wayBack.onSuccess() immediately.
	 * 
	 * @param fieldName
	 * @param wayBack
	 */
	public void prefetchList(final String names, final GenericCallback<String> wayBack) {
		String namesToFetch = "";
		String[] list = null;

		if (names.indexOf(",") > -1)
			list = names.split(",");
		else
			list = new String[] { names };

		for (int i = 0; i < list.length; i++)
			if (!widgetMap.containsKey(list[i])) {
				namesToFetch += list[i] + ",";

				// IF THE LIST IS TOO LONG, FETCH WHAT YOU'VE GOT, THEN START OVER
				if (namesToFetch.length() >= maxURL) {
					final String whatsLeft = names.replaceAll(namesToFetch, "");
					doListFetch(namesToFetch.substring(0, namesToFetch.length() - 1), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							Window.alert("Terminal failure! Please check your Internet connection, then restart SIS.");
						}

						public void onSuccess(String arg0) {
							prefetchList(whatsLeft, wayBack);
						}
					});

					return;
				}
			}

		if (namesToFetch.equalsIgnoreCase("")) {
			wayBack.onSuccess("OK");
			return;
		} else
			namesToFetch = namesToFetch.substring(0, namesToFetch.length() - 1);

		doListFetch(namesToFetch, wayBack);
	}

	public void resetWidgetContents() {
		if (AssessmentCache.impl.getCurrentAssessment() != null) {
			for (Iterator iter = widgetMap.values().iterator(); iter.hasNext();) {
				addAssessmentToDisplay((Display) iter.next());
			}
		}
	}

}
