package org.iucn.sis.client.panels.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.google.gwt.user.client.ui.CheckBox;

@SuppressWarnings("deprecation")
public class CheckableSearchPanel extends SearchPanel {
	
	public CheckableSearchPanel() {
		super();
	}
	
	public Collection<Taxon> getSelection() {
		final List<Taxon> list = new ArrayList<Taxon>();
		for (TableItem item : table.getItems()) {
			if (((CheckBox) item.getValue(0)).isChecked()) {
				list.add(TaxonomyCache.impl.getTaxon(getTaxonID(item)));
			}
		}
		return list;
	}
	
	public void deselectAll() {
		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			if (((CheckBox) item.getValue(0)).isEnabled()) {
				((CheckBox) item.getValue(0)).setChecked(false);
			}
		}
	}
	
	@Override
	protected TableColumnModel getColumnModel() {
		TableColumn[] columns = new TableColumn[6];

		columns[0] = new TableColumn("", .01f);
		columns[0].setMinWidth(25);
		columns[0].setMaxWidth(25);
		columns[0].setAlignment(HorizontalAlignment.CENTER);

		columns[1] = new TableColumn("Scientific Name", .35f);
		columns[1].setMinWidth(75);
		columns[1].setMaxWidth(300);

		columns[2] = new TableColumn("Common Name", .35f);
		columns[2].setMinWidth(75);
		columns[2].setMaxWidth(300);
		columns[2].setAlignment(HorizontalAlignment.LEFT);

		columns[3] = new TableColumn("Level", .15f);
		columns[3].setMinWidth(30);
		columns[3].setMaxWidth(100);

		columns[4] = new TableColumn("Category", .14f);
		columns[4].setMinWidth(30);
		columns[4].setMaxWidth(100);
		columns[4].setAlignment(HorizontalAlignment.LEFT);

		columns[5] = new TableColumn("id", 0);
		columns[5].setHidden(true);
		
		return new TableColumnModel(columns);
	}
	
	protected Integer getTaxonID(TableItem item) {
		return Integer.valueOf((String)item.getValue(5));
	}
	
	@Override
	protected TableItem buildTableItem(Taxon taxon, Object[] row) {
		row[0] = new CheckBox();
		row[1] = taxon.getFullName(); 
		if (taxon.getCommonNames().size() > 0)
			row[2] = (new ArrayList<CommonName>(taxon.getCommonNames()).get(0)).getName().toLowerCase();
		else
			row[2] = "";
		row[3] = Taxon.getDisplayableLevel(taxon.getLevel());
		Set<Assessment> aData = AssessmentCache.impl.getPublishedAssessmentsForTaxon(taxon.getId());
		if (aData == null || aData.isEmpty())
			row[4] = "N/A";
		else
			row[4] = aData.iterator().next().getCategoryAbbreviation();
		row[5] = String.valueOf(taxon.getId());
		
		return new TableItem(row);
	}
	
	public int getNumberToDisplay() {
		return NUMBER_OF_RESULTS;
	}

	public String getSelected() {
		StringBuffer taxonToAdd = new StringBuffer();
		List<TableItem> items = table.getItems();
		for (TableItem item : items) {
			if (((CheckBox) item.getValue(0)).isChecked()) {
				taxonToAdd.append(getTaxonID(item));
				taxonToAdd.append(",");
			}
		}
		if (taxonToAdd.length() > 0) {
			return taxonToAdd.substring(0, taxonToAdd.length() - 1);
		} else
			return "";
	}

	public void selectAll() {
		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			((CheckBox) item.getValue(0)).setChecked(true);
		}
	}

	public void updateTable() {
		table.removeAll();
		resetSearchBox();
	}

}
