/**
 *
 */
package com.solertium.util.querybuilder.gwt.client.chooser;

import java.util.ArrayList;

/**
 * TableChooserSaveListener.java
 *
 * @author carl.scott
 *
 */
public interface TableChooserSaveListener {

	public void onSave(String selectedTable, ArrayList<String> selectedColumns);

}
