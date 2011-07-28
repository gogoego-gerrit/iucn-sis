package org.iucn.sis.shared.api.schemes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.iucn.sis.shared.api.data.TreeData;
import org.iucn.sis.shared.api.schemes.BasicClassificationSchemeViewer.ClassificationSchemeModelDataComparator;
import org.iucn.sis.shared.api.structures.Structure;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ClassificationSchemeReadOnlyFactory {
	
	@SuppressWarnings("unchecked")
	public static void buildReadOnlyContainer(TreeData treeData, VerticalPanel container, 
			Collection<? extends ClassificationSchemeModelData> thinData, Structure<?> defaultStructure) {
		container.clear();
		
		if (thinData.isEmpty()) {
			HTML empty = new HTML("No selections made");
			empty.setWidth("400px");
			container.add(empty);
		}
		else {
			List<ClassificationSchemeModelData> rows = new ArrayList<ClassificationSchemeModelData>(thinData);
			Collections.sort(rows, new ClassificationSchemeModelDataComparator(treeData.getTopLevelDisplay()));
			
			List<String> columns = defaultStructure.extractDescriptions();

			Grid grid = new Grid(rows.size() + 1, columns.size() + 1);
			grid.setBorderWidth(1);
			grid.setCellSpacing(0);
			grid.setCellPadding(0);
			grid.addStyleName("page_assessment_classScheme_grid");
			
			int col = 1;
			for (String column : columns)
				grid.setHTML(0, col++, "<span class=\"page_assessment_classScheme_grid_th\">" + column + "</span>");
			
			int row = 1;
			for (ClassificationSchemeModelData model : rows) {
				col = 0;
				grid.setHTML(row, col++, "<span class=\"page_assessment_classScheme_content\">" + model.get("text") + "</span>");
				for (String column : columns)
					grid.setHTML(row, col++, "<span class=\"page_assessment_classScheme_content\">" + model.get(column) + "</span>");
				row++;
				//container.add(new HTML(model.getSelectedRow().getFullLineage()));
			}
			
			String optionColWidth = "350px";
			String dataColWidth = "80px";
			if (CanonicalNames.ConservationActions.equals(treeData.getCanonicalName()) || 
				CanonicalNames.Research.equals(treeData.getCanonicalName())) {
				optionColWidth = "500px";
				dataColWidth = "250px";
			}
			
			grid.getColumnFormatter().setWidth(0, optionColWidth);
			for (int i = 1; i < grid.getColumnCount(); i++)
				grid.getColumnFormatter().setWidth(i, dataColWidth);
			
			container.add(grid);
		}
	}

}
