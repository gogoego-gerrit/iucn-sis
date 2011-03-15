package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.structures.CreatesWidget;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class FieldWidgetCache {
	public static final FieldWidgetCache impl = new FieldWidgetCache();
	
	public static FieldWidgetCache newInstance() {
		FieldWidgetCache cache = new FieldWidgetCache();
		for (CreatesWidget generator : impl.getWidgetGenerators())
			cache.registerWidgetGenerator(generator);
		
		return cache;
	}

	private final Map<String, Map<String, Display>> schemaToWidgetMap;
	private final Set<CreatesWidget> widgetGenerators;
	
	private CreatesDisplay fieldParser;

	private FieldWidgetCache() {
		schemaToWidgetMap = new HashMap<String, Map<String,Display>>();
		widgetGenerators = new HashSet<CreatesWidget>();
	}
	
	public void registerWidgetGenerator(CreatesWidget generator) {
		widgetGenerators.add(generator);
	}
	
	public Set<CreatesWidget> getWidgetGenerators() {
		return widgetGenerators;
	}
	
	public void setFieldParser(CreatesDisplay fieldParser) {
		this.fieldParser = fieldParser;
	}
	
	public CreatesDisplay getFieldParser() {
		return fieldParser;
	}

	// TODO: THIS IS BEING CALLED TOO MANY TIMES!! Fix it.
	private void addAssessmentToDisplay(Display display) {
		if (AssessmentCache.impl.getCurrentAssessment() != null && display != null) {
			Field field = AssessmentCache.impl.getCurrentAssessment().getField(display.getCanonicalName());
			if (field == null)
				field = new Field(display.getCanonicalName(), AssessmentCache.impl.getCurrentAssessment());
		
			display.setData(field);
		}
	}

	private void doListFetch(final String schema, Collection<String> names, final GenericCallback<String> wayBack) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<fields>");
		for (String name : names)
			builder.append(XMLWritingUtils.writeTag("field", name));
		builder.append("</fields>");
		
		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		final String uri = UriBase.getInstance().getSISBase() + "/application/schema/" + schema + "/field";
		doc.post(uri, builder.toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayBack.onFailure(caught);
			}
			public void onSuccess(String arg0) {
				Map<String, Display> widgetMap = schemaToWidgetMap.get(schema);
				if (widgetMap == null)
					widgetMap = new HashMap<String, Display>();
				
				final NativeNodeList displays = doc.getDocumentElement().getChildNodes();
				for (int i = 0; i < displays.getLength(); i++) {
					final NativeNode current = displays.item(i);
					if (current.getNodeType() != NativeNode.TEXT_NODE && current instanceof NativeElement) {
						Display dis = fieldParser.parseField((NativeElement)current);
						if (dis != null && dis.getCanonicalName() != null && !dis.getCanonicalName().equals("")) {
							widgetMap.put(dis.getCanonicalName(), dis);
						} else
							Debug.println("Parsed a " + "display with null canonical " + 
								"name. Description is: {0}", dis.getDescription());
					}
				}
				
				schemaToWidgetMap.put(schema, widgetMap);

				wayBack.onSuccess("OK");
			}
		});
	}

	public void doLogout() {
		schemaToWidgetMap.clear();
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
		if (AssessmentCache.impl.getCurrentAssessment() == null)
			return null;
		
		String schema = AssessmentCache.impl.getCurrentAssessment().getSchema(SchemaCache.impl.getDefaultSchema());
		if (!schemaToWidgetMap.containsKey(schema))
			return null;
		
		Display cur = schemaToWidgetMap.get(schema).get(canonicalName);
		addAssessmentToDisplay(cur);

		return cur;
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
	public void prefetchList(final Collection<String> names, final GenericCallback<String> wayBack) {
		if (AssessmentCache.impl.getCurrentAssessment() == null) {
			wayBack.onSuccess("OK");
			return;
		}
		
		final List<String> uncachedNames = new ArrayList<String>();
		
		final String schema = AssessmentCache.impl.getCurrentAssessment().
			getSchema(SchemaCache.impl.getDefaultSchema());
		
		final Map<String, Display> widgetMap = schemaToWidgetMap.get(schema);
		if (widgetMap == null)
			uncachedNames.addAll(names);
		else
			for (String fieldName : names) 
				if (!widgetMap.containsKey(fieldName)) 
					uncachedNames.add(fieldName);
		
		if (uncachedNames.isEmpty())
			wayBack.onSuccess("OK");
		else
			doListFetch(schema, uncachedNames, wayBack);
	}

	public void resetWidgetContents() {
		resetWidgetContents(new String[0]);
	}
	
	public void resetWidgetContents(String... fieldNames) {
		if (AssessmentCache.impl.getCurrentAssessment() != null) {
			String schema = AssessmentCache.impl.getCurrentAssessment().
				getSchema(SchemaCache.impl.getDefaultSchema());
			
			final Map<String, Display> widgetMap = schemaToWidgetMap.get(schema);
			if (widgetMap != null) {
				if (fieldNames == null || fieldNames.length == 0)
					for (Iterator<Display> iter = widgetMap.values().iterator(); iter.hasNext();)
						addAssessmentToDisplay(iter.next());
				else {
					for (String fieldName : fieldNames) {
						Display current = widgetMap.get(fieldName);
						if (current != null)
							addAssessmentToDisplay(current);
					}
				}	
			}
		}
	}
	
	public static interface CreatesDisplay {
		
		public Display parseField(NativeElement element);
		
	}

}
