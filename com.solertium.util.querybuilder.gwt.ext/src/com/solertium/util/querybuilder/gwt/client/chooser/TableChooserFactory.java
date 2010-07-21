package com.solertium.util.querybuilder.gwt.client.chooser;

import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;

/**
 * TableChooserFactory.java
 * 
 * Creates a new instance of a table chooser.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public interface TableChooserFactory {
	
	public TableChooser newInstance(GWTQBQuery query, boolean isMultipleSelect);

}
