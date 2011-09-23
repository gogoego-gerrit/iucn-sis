package org.iucn.sis.client.panels.assessments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.ui.models.assessment.ChangesModelData;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.models.Assessment;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * UI that allows you to view the changes that have been done to the assessment
 * 
 * @author liz.schwartz
 *
 */
public class AssessmentChangesPanel extends PagingPanel<ChangesModelData> implements DrawsLazily {

	private final Assessment assessment;
	
	private Grid<ChangesModelData> grid;
	
	private TextField<String> usernameFilter;
	private TextField<String> fieldFilter;
	private TextField<String>  nameFilter;
	private TextField<String>  valueFilter;
	private DateField dateBeforeFilter;
	private DateField dateAfterFilter;

	public AssessmentChangesPanel() {
		super();
		setLayout(new FillLayout());
		setPageCount(20);
		getProxy().setFilter(new ChangeFilter());
		
		assessment = AssessmentCache.impl.getCurrentAssessment();
	}
	
	@Override
	public void draw(DoneDrawingCallback callback) {
		if (assessment == null) {
			WindowUtils.infoAlert("Unable to view changes.  Please set your current assessment to the assessment you want to view");
		} else {
			grid = new Grid<ChangesModelData>(getStoreInstance(), getColumnModel());
			
			final LayoutContainer container = new LayoutContainer(new BorderLayout());
			container.add(createFilterPanel(), new BorderLayoutData(LayoutRegion.NORTH, 225, 225, 225));
			container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
			container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
			
			add(container);
			
			refresh(callback);
		}
	}
	
	protected ColumnModel getColumnModel() {
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
		
		return new ColumnModel(configs);
	}
	
	private String generateTitle() {
		String assessmentTitle = "";
		if (assessment.isRegional())
			assessmentTitle = "Regional ";

		assessmentTitle += assessment.getAssessmentType().getDisplayName(true);

		assessmentTitle += " Assessment for " + assessment.getSpeciesName(); 
		
		return assessmentTitle;
	}
	
	protected LayoutContainer createFilterPanel() {
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

		Html html = new Html(generateTitle());		
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
				getProxy().filter("", "");
			}
		});
		formPanel.addButton(button);

		formPanels.add(formPanel);


		LayoutContainer container = new LayoutContainer();
		container.add(html);
		container.add(formPanels);

		return container;
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}

	@Override
	protected void getStore(final GenericCallback<ListStore<ChangesModelData>> callback) {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getSISBase() + "/asmchanges/" + assessment.getId(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				//callback.onFailure(caught);
				
				/*ListStore<ChangesModelData> result = new ListStore<ChangesModelData>();
				//result.add(store);
				
				callback.onSuccess(result);*/
				
				WindowUtils.errorAlert("This feature is not yet available in SIS 2.0");
			}
			public void onSuccess(String nullString) {
				final List<ChangesModelData> store = new ArrayList<ChangesModelData>();

				NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName(ChangesModelData.CHANGE);
				for (int i = 0; i < list.getLength(); i++)
					store.add(new ChangesModelData(list.elementAt(i)));	
				
				com.solertium.lwxml.shared.utils.ArrayUtils.quicksort(store, new Comparator<ChangesModelData>(){
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
				
				ListStore<ChangesModelData> result = new ListStore<ChangesModelData>();
				result.add(store);
				
				callback.onSuccess(result);
			}
		});
	}

	private class ChangeFilter implements StoreFilter<ChangesModelData> {
		
		@Override
		public boolean select(Store<ChangesModelData> store, ChangesModelData parent, ChangesModelData item, String property) {
			boolean result = false;
			try{
				if (filterOut( (String)item.get(ChangesModelData.USER), (String)usernameFilter.getRawValue())
						|| filterOut((String)item.get(ChangesModelData.FIELD), (String)fieldFilter.getRawValue())
						|| filterOut((String)item.get(ChangesModelData.NAME), (String)nameFilter.getRawValue())
						|| filterOut((String)item.get(ChangesModelData.VALUE), (String)valueFilter.getRawValue())
						|| filterOutDate((Date)item.get(ChangesModelData.DATE), (Date)dateAfterFilter.getValue(), true)
						|| filterOutDate((Date)item.get(ChangesModelData.DATE), (Date)dateBeforeFilter.getValue(), false))
					result = true;
			} catch (Throwable e) {
				//TODO: debug
			}
			
			return result; //or !result?
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
		
	}

}
