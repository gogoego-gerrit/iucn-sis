package org.iucn.sis.shared.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.displays.ClassificationScheme;
import org.iucn.sis.client.displays.Display;
import org.iucn.sis.client.displays.Field;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.data.assessments.FieldParser;
import org.iucn.sis.shared.structures.Structure;

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
	public HashMap widgetMap = null;

	private FieldParser fieldParser = null;

	private static final int maxURL = 250;

	private FieldWidgetCache() {
		fieldParser = new FieldParser();
		widgetMap = new HashMap();
	}

	// TODO: THIS IS BEING CALLED TOO MANY TIMES!! Fix it.
	public void addAssessmentDataToDisplay(Display display) {
		if (AssessmentCache.impl.getCurrentAssessment() != null && display != null) {
			try {
				ArrayList data = (ArrayList) AssessmentCache.impl.getCurrentAssessment().getFieldData(
						display.getCanonicalName());

				if (data != null) {
					int dataOffset = 0;
					for (int i = 0; i < ((Field) display).getStructures().size(); i++) {
						Structure cur = (Structure) ((Field) display).getStructures().get(i);
						// SysDebugger.getInstance().println(
						// "Setting data for struct " + i + " in " +
						// display.getCanonicalName()
						// + " to " + data.get( dataOffset ) );
						try {
							dataOffset = ((Structure) ((Field) display).getStructures().get(i)).setData(data,
									dataOffset);
						} catch (Exception e) {
							SysDebugger.getInstance().println(
									"setData error in FieldWidgetCache for display " + display.getCanonicalName());
							e.printStackTrace();
						}
					}
				} else
					for (int i = 0; i < ((Field) display).getStructures().size(); i++) {
						// SysDebugger.getInstance().println(
						// "Clearing data for struct " + i );
						((Structure) ((Field) display).getStructures().get(i)).clearData();
					}
			} catch (ClassCastException e) {
				// SysDebugger.getInstance().println( display.getCanonicalName()
				// + " is not a Field.");

				try {
					HashMap selected = (HashMap) AssessmentCache.impl.getCurrentAssessment().getFieldData(
							display.getCanonicalName());

					((ClassificationScheme) display).setSelectedFromData(selected);
				} catch (ClassCastException e1) {
					e1.printStackTrace();
					// SysDebugger.getInstance().println(
					// display.getCanonicalName() + " is not a Tree, either.");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void doListFetch(String names, final GenericCallback<String> wayBack) {
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.get("/field/" + names, new GenericCallback<String>() {
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
	 * Checks to see if the Widget has already been built. If it has, it invokes
	 * the onSuccess(...) on the wayBack object, supplying the Widget. If not,
	 * the appropriate call to the server will be made, it will build the Widget
	 * from the results, then call onSuccess(...) on the wayBack object,
	 * supplying the Widget as the argument.
	 * 
	 * @param fieldName
	 * @param wayBack
	 */
	public void fetchField(final String fieldName, final GenericCallback<Display> wayBack) {
		if (fieldName == null || fieldName.equals(""))
			return;

		if (!widgetMap.containsKey(fieldName)) {
			final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			doc.get("/field/" + fieldName, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					wayBack.onFailure(null);
				}

				public void onSuccess(String arg0) {
					Display dis = fieldParser.parseField(doc);
					if (dis != null)
						widgetMap.put(fieldName.trim(), dis);

					addAssessmentDataToDisplay(dis);

					wayBack.onSuccess(dis);
				}
			});
		} else {
			Display dis = get(fieldName);
			addAssessmentDataToDisplay(dis);
			wayBack.onSuccess(dis);
		}
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
		addAssessmentDataToDisplay(cur);

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
				addAssessmentDataToDisplay((Display) iter.next());
			}
		}
	}

}
