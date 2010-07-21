package org.iucn.sis.client.components.panels.changes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.utilities.FormattedDate;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.GenericPagingLoader;
import com.solertium.util.extjs.client.PagingLoaderFilter;

/**
 * UI that allows you to view the changes that have been done to the assessment
 * 
 * @author liz.schwartz
 *
 */
public class AssessmentChangesPanel extends LayoutContainer{

	protected Grid<ChangesModelData> grid = null;
	protected ListStore<ChangesModelData> listStore = null;
	protected AssessmentData ass = null;
	protected GenericPagingLoader<ChangesModelData> loader;
	protected PagingToolBar pagingBar = null;

	protected TextField<String> usernameFilter;
	protected TextField<String> fieldFilter;
	protected TextField<String>  nameFilter;
	protected TextField<String>  valueFilter;
	protected DateField dateBeforeFilter;
	protected DateField dateAfterFilter;


	public AssessmentChangesPanel() {
		ass = AssessmentCache.impl.getCurrentAssessment();

		if (ass == null) {
			Window.alert("Unable to view changes.  Please set your current assessment to the assessment you want to view");
		} else {
			initGrid();
			getChanges();
			draw();
		}
		
		setScrollMode(Scroll.AUTO);

	}

	protected void draw() {
		String assessmentTitle = "";
		if (ass.isRegional())
			assessmentTitle = "Regional ";

		if (ass.getType().equalsIgnoreCase(BaseAssessment.DRAFT_ASSESSMENT_STATUS))
			assessmentTitle += "Draft Assessment";
		else if (ass.getType().equalsIgnoreCase(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS))
			assessmentTitle += "Published Assessment";
		else
			assessmentTitle += "User Assessment";

		assessmentTitle += " for " + ass.getSpeciesName(); 

		setLayout(new RowLayout());

		Html html = new Html(assessmentTitle);		
		html.addStyleName("changesTitle");

		HorizontalPanel formPanels = new HorizontalPanel();
		FormPanel formPanel = new FormPanel();
		formPanel.setHeaderVisible(false);
		formPanel.setBorders(false);
		formPanel.setBodyBorder(false);
		formPanel.addStyleName("formPanelBorder");
		formPanel.addText("Filter results by value <br/> <br/>");
		formPanel.add(usernameFilter);
		formPanel.add(fieldFilter);
		formPanel.add(nameFilter);
		formPanel.add(valueFilter);		
		formPanel.addStyleName("changesFormPanel");
		formPanels.add(formPanel);
		formPanel = new FormPanel();
		formPanel.setHeaderVisible(false);
		formPanel.setBorders(false);
		formPanel.addStyleName("formPanelBorder");
		formPanel.addText("<br/> <br/>");
		formPanel.add(dateAfterFilter);
		formPanel.add(dateBeforeFilter);
		formPanel.setBodyBorder(false);
		formPanel.addStyleName("changesFormPanel");
		Button button = new Button("Filter", new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) {
				loader.applyFilter("");
				pagingBar.setActivePage(1);
				loader.getPagingLoader().load();

			}
		});
		formPanel.addButton(button);

		formPanels.add(formPanel);


		VerticalPanel vp = new VerticalPanel();
		vp.add(html);
		vp.add(formPanels);




		add(vp, new RowData(1, -1));
		add(grid, new RowData(1,1));
		add(pagingBar, new RowData(1, -1));



	}

	private void getChanges() {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.get("/asmchanges/" + ass.getAssessmentID(), new GenericCallback<String>() {

			public void onSuccess(String result) {
				loader.getFullList().clear();

				NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName(ChangesModelData.CHANGE);
				for (int i = 0; i < list.getLength(); i++) {
					loader.getFullList().add(new ChangesModelData(list.elementAt(i)));	

				}

				com.solertium.lwxml.shared.utils.ArrayUtils.quicksort(loader.getFullList(), new Comparator<ChangesModelData>(){
					public int compare(ChangesModelData o1, ChangesModelData o2) {
						if (o1.deleted)
							return 1;
						if (o2.deleted)
							return -1;
						if (o1.get(ChangesModelData.FIELD).equals(o2.get(ChangesModelData.FIELD)))
						{
							if (((String)o1.get(ChangesModelData.NAME)).equals(((String)o2.get(ChangesModelData.NAME))))
								return ((Date)o1.get(ChangesModelData.DATE)).compareTo((Date)o2.get(ChangesModelData.DATE));
							else
								return ((String)o1.get(ChangesModelData.NAME)).compareTo((String)o2.get(ChangesModelData.NAME));
						}
						else
							return ((String)o1.get(ChangesModelData.FIELD)).compareTo((String)o2.get(ChangesModelData.FIELD));
					}
				});

				loader.getPagingLoader().load(0,20);
				pagingBar.setActivePage(1);
				layout();


			}

			public void onFailure(Throwable caught) {
				Window.alert("Unable to get changes.");
			}
		});
	}

	private void initGrid() {
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		configs.add(new ColumnConfig(ChangesModelData.FIELD, ChangesModelData.FIELD_NAME, 200));
		configs.add(new ColumnConfig(ChangesModelData.NAME, ChangesModelData.NAME, 150));
		configs.add(new ColumnConfig(ChangesModelData.VALUE, ChangesModelData.VALUE, 150));

		ColumnConfig config = new ColumnConfig(ChangesModelData.DATE, ChangesModelData.DATE, 125);
		config.setRenderer(new GridCellRenderer<ChangesModelData>() {
			public Object render(ChangesModelData model, String property, ColumnData config, 
					int rowIndex, int colIndex, ListStore<ChangesModelData> store, 
					Grid<ChangesModelData> grid) {
				Date date = (Date) model.get(ChangesModelData.DATE);
				DateTimeFormat formatter = DateTimeFormat.getFormat("HH:mm yyyy-MM-dd");
				return formatter.format(date);
			}
		});
		configs.add(config);

		configs.add(new ColumnConfig(ChangesModelData.USER, ChangesModelData.USER, 125));
		ColumnModel cm = new ColumnModel(configs);

		loader = new GenericPagingLoader<ChangesModelData>();		
		listStore = new ListStore<ChangesModelData>(loader.getPagingLoader());

		grid = new Grid<ChangesModelData>(listStore, cm);

		pagingBar = new PagingToolBar(20);
		pagingBar.bind(loader.getPagingLoader());

		usernameFilter = new TextField<String>();
		usernameFilter.setFieldLabel(ChangesModelData.USER);
		fieldFilter = new TextField<String>();
		fieldFilter.setFieldLabel(ChangesModelData.FIELD);
		nameFilter = new TextField<String>();
		nameFilter.setFieldLabel(ChangesModelData.NAME);
		valueFilter = new TextField<String>();
		valueFilter.setFieldLabel(ChangesModelData.VALUE);
		dateAfterFilter = new DateField();
		dateAfterFilter.setFormatValue(true);
		dateAfterFilter.getPropertyEditor().setFormat(FormattedDate.impl.getDateTimeFormat());
		dateAfterFilter.setFieldLabel("from");
		dateBeforeFilter = new DateField();
		dateBeforeFilter.setFieldLabel("to");
		dateBeforeFilter.setFormatValue(true);
		dateBeforeFilter.getPropertyEditor().setFormat(FormattedDate.impl.getDateTimeFormat());

		loader.setFilter(new PagingLoaderFilter<ChangesModelData>() {

			public boolean filter(ChangesModelData item, String property) {
				try{
					if (filterOut( (String)item.get(ChangesModelData.USER), (String)usernameFilter.getRawValue())
							|| filterOut((String)item.get(ChangesModelData.FIELD), (String)fieldFilter.getRawValue())
							|| filterOut((String)item.get(ChangesModelData.NAME), (String)nameFilter.getRawValue())
							|| filterOut((String)item.get(ChangesModelData.VALUE), (String)valueFilter.getRawValue())
							|| filterOutDate((Date)item.get(ChangesModelData.DATE), (Date)dateAfterFilter.getValue(), true)
							|| filterOutDate((Date)item.get(ChangesModelData.DATE), (Date)dateBeforeFilter.getValue(), false))
						return true;


					return false;
				} catch (Throwable e) {
					e.printStackTrace();
					return false;
				}
			}

			private boolean filterOutDate(Date value, Date givenDate, boolean after) {					
				if (givenDate == null)
					return false;
				if (value == null)
					return true;
				if (!after)
					givenDate.setTime(givenDate.getTime() + (1*24 * 60 * 60 * 1000));
				if (value.equals(givenDate))
					return false;
				if (after && value.after(givenDate))
					return false;
				if (!after && value.before(givenDate))
					return false;
				return true;					
			}

			private boolean filterOut(String value, String filterBy) {
				if (filterBy.equalsIgnoreCase(""))
					return false;
				if (value == null || value.equalsIgnoreCase(""))
					return false;
				if (!value.toLowerCase().startsWith(filterBy.toLowerCase()))
					return true;
				return false;

			}

		});







	}



}
