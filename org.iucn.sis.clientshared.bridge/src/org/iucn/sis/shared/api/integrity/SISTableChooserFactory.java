package org.iucn.sis.shared.api.integrity;

import com.solertium.util.querybuilder.gwt.client.chooser.FriendlyNameTableChooser;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooser;
import com.solertium.util.querybuilder.gwt.client.chooser.TableChooserFactory;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;


/**
 * SISTableChooserFactory.java
 * 
 * TableChooserFactory implementation that returns the SISTableChooser for the
 * "Add Field" functionality, and a friendly table chooser for any other case.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class SISTableChooserFactory implements TableChooserFactory {

	public TableChooser newInstance(GWTQBQuery query, boolean isMultipleSelect) {
		if (isMultipleSelect)
			return new SISTableChooser(query, isMultipleSelect);
		else
			return new FriendlyNameTableChooser(query, isMultipleSelect);
	}

}
