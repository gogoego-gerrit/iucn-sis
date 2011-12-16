package org.iucn.sis.shared.api.integrity;

import org.iucn.sis.client.panels.integrity.IntegrityQuery;
import org.iucn.sis.client.panels.integrity.SQLValidationDesigner;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;

/**
 * AssessmentValidationDesigner.java
 * 
 * This is going to closely follow the QueryDesigner, but override functionality
 * so that SIS can use it for it's specific needs, such as:
 * 
 * - Don't show the fields, show them as "Tables" - Allow only tables to be
 * chosen in custom table chooser for field selection - Force selection of
 * assessement table.
 * 
 * @see com.solertium.util.querybuilder.gwt.client.QueryDesigner
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class AssessmentValidationDesigner extends LayoutContainer {

	private IntegrityQuery query;
	
	private String savedXML;
	
	private SimpleListener beforeSave;
	
	public AssessmentValidationDesigner() {
		super();
		setScrollMode(Scroll.AUTO);
	}
	
	public void draw(IntegrityQuery query) {
		removeAll();
		
		this.query = query;
	
		if (query instanceof SISQBQuery)
			drawQueryBuilder(((SISQBQuery)query));
		else
			drawSQLWriter(((SQLQuery)query));
		
		updateSavedXML();
	}
	
	private void drawSQLWriter(final SQLQuery query) {
		final SQLValidationDesigner designer = new SQLValidationDesigner(query);
		beforeSave = new SimpleListener() {
			public void handleEvent() {
				designer.save();
			}
		};
		
		add(designer);
	}
	
	private void drawQueryBuilder(final SISQBQuery query) {
		beforeSave = null;
		
		SISQBTreeItem item = new SISQBTreeItem(0, true) {
			public GWTQBQuery getQuery() {
				return query;
			}
		};
		add(item);
		item.show("Tables");

		SISQBConditionGroupRootItem cond = new SISQBConditionGroupRootItem() {
			public GWTQBQuery getQuery() {
				return query;
			}
		};
		add(cond);
		cond.show("Conditions");
		cond.expandTree();
	}

	public IntegrityQuery getQuery() {
		return query;
	}
	
	public void updateSavedXML() {
		savedXML = query.toXML();
	}
	
	public void clearChanges() {
		savedXML = null;
	}
	
	public boolean hasChanged() {
		return savedXML != null && query != null && !savedXML.equals(query.toXML());
	}
	
	public String save() {
		if (beforeSave != null)
			beforeSave.handleEvent();
		
		return query.toXML();
	}

}
