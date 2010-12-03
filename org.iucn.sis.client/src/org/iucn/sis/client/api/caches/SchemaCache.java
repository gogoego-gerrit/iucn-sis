package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;

public class SchemaCache {

	public static final SchemaCache impl = new SchemaCache();
	
	private Map<String, AssessmentSchema> cache; 
	
	private SchemaCache() {
		cache = null;
	}
	
	private void init(final SimpleListener callback) {
		if (cache != null) {
			callback.handleEvent();
			return;
		}
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getSISBase() + "/application/schema", new GenericCallback<String>() {
			public void onSuccess(String result) {
				cache = new HashMap<String, AssessmentSchema>();
				
				final RowParser parser = new RowParser(document);
				for (RowData row : parser.getRows()) {
					AssessmentSchema schema = new AssessmentSchema(row.getField("id"), row.getField("name"), row.getField("description"));
					cache.put(schema.id, schema);
				}
				
				callback.handleEvent();
			}
			public void onFailure(Throwable caught) {
				callback.handleEvent();
			}
		});
	}
	
	public void get(final String id, final ComplexListener<AssessmentSchema> callback) {
		init(new SimpleListener() {
			public void handleEvent() {
				callback.handleEvent(cache.get(id));
			}
		});
	}
	
	public void list(final ComplexListener<List<AssessmentSchema>> callback) {
		init(new SimpleListener() {
			public void handleEvent() {
				callback.handleEvent(new ArrayList<AssessmentSchema>(cache.values()));
			}
		});
	}
	
	public List<AssessmentSchema> listFromCache() {
		if (cache == null)
			return new ArrayList<AssessmentSchema>();
		else
			return new ArrayList<AssessmentSchema>(cache.values());
	}
	
	public static class AssessmentSchema {
		
		private final String id, name, description;
		
		public AssessmentSchema(String id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
	}
	
}
