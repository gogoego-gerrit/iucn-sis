package com.solertium.util.querybuilder.gwt.client.chooser;

import java.util.HashMap;
import java.util.Map;

import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.struct.DBStructure;

/**
 * TableChooserCreater.java
 * 
 * Used to create and register table choosers.
 *  
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class TableChooserCreator {
	
	private static TableChooserCreator instance;
	
	public static TableChooserCreator getInstance() {
		if (instance == null)
			instance = new TableChooserCreator();
		return instance;
	}
	
	private final Map<String, TableChooserFactory> map;
	
	private TableChooserCreator() {
		map = new HashMap<String, TableChooserFactory>();
		map.put(DBStructure.TABLE_CHOOSER_FRIENDLY, new TableChooserFactory() {
			public TableChooser newInstance(GWTQBQuery query, boolean isMultipleSelect) {
				return new FriendlyNameTableChooser(query, isMultipleSelect); 
			}
		});
		map.put(DBStructure.TABLE_CHOOSER_OFFICIAL, new TableChooserFactory() {
			public TableChooser newInstance(GWTQBQuery query, boolean isMultipleSelect) {
				return new OfficialNameTableChooser(query, isMultipleSelect);
			}
		});
	}
	
	public void register(String key, TableChooserFactory value) {
		map.put(key, value);
	}
	
	public TableChooser getTableChooser(String key, GWTQBQuery query, boolean isMultipleSelect) {
		return map.containsKey(key) ? map.get(key).newInstance(query, isMultipleSelect) : null;
			
	}

}
