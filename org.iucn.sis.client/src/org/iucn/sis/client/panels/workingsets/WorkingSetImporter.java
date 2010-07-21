package org.iucn.sis.client.panels.workingsets;

import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.gwt.debug.SysDebugger;

public class WorkingSetImporter extends RefreshLayoutContainer {

	private PanelManager manager = null;
	private HTML instructions = null;

	public WorkingSetImporter(PanelManager manager) {
		super();
		this.manager = manager;
		build();
	}

	private void build() {

		setLayout(new TableLayout(1));
		setScrollMode(Scroll.AUTO);
		addStyleName("gwt-background");

		instructions = new HTML("<b>Instructions:</b> Please select "
				+ "a .zip file to import as a working set.  All draft " + "assessments will be overwritten if "
				+ "they are imported, all taxa will be merged.  Working Sets "
				+ "will be copied in if they already existed.  Importing a public working set"
				+ " will create a copied public one.");

		refresh();
	}

	@Override
	public void refresh() {

		SysDebugger.getInstance().println("In the working set importer refresh");
		removeAll();

		add(instructions, new TableData());
		WorkingsetImportWidget uploadWidget = new WorkingsetImportWidget(manager);
		add(uploadWidget, new TableData());

		layout();

	}

}
