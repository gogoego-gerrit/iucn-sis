package org.iucn.sis.shared.api.integrity;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.solertium.util.querybuilder.gwt.client.utils.GWTQBQuery;
import com.solertium.util.querybuilder.query.QBQuery;

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

	private SISQBQuery query;
	
	private String savedXML;
	
	public AssessmentValidationDesigner() {
		super();
		setScrollMode(Scroll.AUTO);
	}
	
	public void draw(SISQBQuery query) {
		removeAll();
		
		this.query = query;
		
		SISQBTreeItem item = new SISQBTreeItem(0, true) {
			public GWTQBQuery getQuery() {
				return AssessmentValidationDesigner.this.query;
			}
		};
		add(item);
		item.show("Tables");

		SISQBConditionGroupRootItem cond = new SISQBConditionGroupRootItem() {
			public GWTQBQuery getQuery() {
				return AssessmentValidationDesigner.this.query;
			}
		};
		add(cond);
		cond.show("Conditions");
		cond.expandTree();
		
		updateSavedXML();
	}

	public QBQuery getQuery() {
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

}
