package org.iucn.sis.client.api.ui.users.panels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.RecentlyAccessedCache;
import org.iucn.sis.client.api.caches.UserCache;
import org.iucn.sis.client.api.caches.RecentlyAccessedCache.RecentUser;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.acl.InsufficientRightsException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.RecentlyAccessed;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.api.structures.SISOptionsList;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.dnd.ListViewDragSource;
import com.extjs.gxt.ui.client.dnd.ListViewDropTarget;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

/**
 * ManageCreditsWindow.java
 * 
 * Allows browsing of the database and find a list of users, and has an abstract
 * callback method when a selection has been made.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 *  
 */
public class ManageCreditsWindow extends Window implements DrawsLazily {

	/** 
	 * Wrapper for the RowData results returned from a search query.
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	public static class MCSearchResults extends BaseModelData {
		private static final long serialVersionUID = 1L;

		protected final ClientUser user;

		public MCSearchResults(ClientUser user) {
			this.user = user;
			set("name", user.getDisplayableName());
			set("userid", user.getId());
			
			for (Map.Entry<String, String> entry : user.properties.entrySet())
				set(entry.getKey().toLowerCase(), entry.getValue());
			
			if ("".equals(user.getFirstName()) && !"".equals(user.getInitials()))
				set("firstname", user.getInitials());
		}

		public ClientUser getUser() {
			return user;
		}

		public String toString() {
			return user.getDisplayableName();
		}
		
		@Override
		public boolean equals(Object obj) {
			if( obj instanceof MCSearchResults ) {
				return ((MCSearchResults)obj).getUser().getId() == user.getId();
			} else
				return super.equals(obj);
		}
	}

	/**
	 * MCSearchResultsComparator
	 * 
	 * Wrapper for the PortableAlphanumericComparators that integrates with the
	 * StoreSorter.
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	private static class MCSearchResultsComparator extends
			StoreSorter<MCSearchResults> {
		private static final long serialVersionUID = 1L;
		private final PortableAlphanumericComparator c = new PortableAlphanumericComparator(); 

		@Override
		public int compare(Store<MCSearchResults> store, MCSearchResults m1,
				MCSearchResults m2, String property) {
			/*
			 * Requirement: Sort is done "by Last, First in ascending order"
			 */
			for (String current : new String[]{ "lastname", "firstname" }) {
				int value = c.compare(m1.get(current), m2.get(current));
				if (value != 0)
					return value;
			}
			return 0;
		}
	}

	private final ListView<MCSearchResults> results, recent, assessors, reviewers, contributors, facilitators;
	private final HTML status;

	public final static int windowWidth = 750;
	public final static int windowHeight = 550;

	private Html recentUsersHeading = new Html("<b>Recently Used</b>");
	private Html searchResultHeading = new Html("<b>Search Result</b>");
	
	private Html assessorsHeading = new Html("<b>Assessors*</b>");
	private Html reviewersHeading = new Html("<b>Reviewers*</b>");
	private Html contributorsHeading = new Html("<b>Contributors</b>");
	private Html facilitatorsHeading = new Html("<b>Facilitators</b>");
	
	private boolean drawn = false;
	
	//private ComplexListener<List<ClientUser>> listener;
 
	public ManageCreditsWindow() {
		super();
		setClosable(true);
		setHeading("Assessment Credits");

		assessors = newListView();
		assessors.getStore().setStoreSorter(new MCSearchResultsComparator());
		
		reviewers = newListView();
		reviewers.getStore().setStoreSorter(new MCSearchResultsComparator());
				
		contributors = newListView();
		contributors.getStore().setStoreSorter(new MCSearchResultsComparator());
		
		facilitators = newListView();
		facilitators.getStore().setStoreSorter(new MCSearchResultsComparator());

		ListViewSelectionModel<MCSearchResults> sm = new ListViewSelectionModel<MCSearchResults>();
		sm.setSelectionMode(SelectionMode.MULTI);

		results = newListView(300);
		results.setSelectionModel(sm); 
		/*
		 * Requirement: "Search results are returned, including email addresses"
		 */
		results.setSimpleTemplate("<div style=\"text-align:left;\">{lastname}, {firstname} ({email})</div>");

		sm = new ListViewSelectionModel<MCSearchResults>();
		sm.setSelectionMode(SelectionMode.MULTI);
		
		recent = newListView(150);
		recent.setSelectionModel(sm);

		status = new HTML();
		
		setSize(windowWidth, windowHeight);

	}
	
	private ListView<MCSearchResults> newListView() {
		return newListView(160, 150);
	}
	
	private ListView<MCSearchResults> newListView(int width) {
		return newListView(width, 150);
	}
	
	private ListView<MCSearchResults> newListView(int width, int height) {
		ListView<MCSearchResults> view = new ListView<MCSearchResults>();
		view.setHeight(height);
		view.setWidth(width);
		/*
		 * Requirement: "All names in all boxes must appear using 
		 * Last Name, First name format." 
		 */
		view.setSimpleTemplate("<div style=\"text-align:left;\">{lastname}, {firstname}</div>");
		view.setStore(new ListStore<MCSearchResults>());
		
		return view;
	}

	public boolean containsSearchResult(ListStore<MCSearchResults> store,
			MCSearchResults result) {
		for (int i = 0; i < store.getCount(); i++) {
			if (store.getAt(i).getUser().getId() == result.getUser().getId())
				return true;
		}
		return false;
	}
	
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	private void open() {
		super.show();
	}
	
	/**
	 * Draws the UI, only need be called once.
	 * 
	 */
	public void draw(final DoneDrawingCallback callback) {
		if (drawn) {
			callback.isDrawn();
			return;
		}
		
		drawn = true;
		
		final TextBox first = new TextBox();
		first.setName("firstname");
		final TextBox last = new TextBox();
		last.setName("lastname");
		final TextBox nickname = new TextBox();
		nickname.setName("nickname");
		final TextBox affiliation = new TextBox();
		affiliation.setName("affiliation");

		final FlexTable form = new FlexTable();
		final ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		bar.add(new Button("Search", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Map<String, String> params = new HashMap<String, String>();
				for (int i = 0; i < form.getRowCount(); i++) {
					if (form.getCellCount(i) > 1) {
						TextBox box = (TextBox) form.getWidget(i, 1);
						if (!box.getText().equals(""))
							params.put(box.getName(), box.getText());
					}
				}
				if (params.isEmpty())
					WindowUtils.errorAlert("You must specify at least one criteria");
				else
					search(params);
			}
		}));
			
		form.setHTML(0, 0, "First Name: ");
		form.setWidget(0, 1, first);
		form.setHTML(1, 0, "Last Name: ");
		form.setWidget(1, 1, last);
		form.setHTML(2, 0, "Nickname: ");
		form.setWidget(2, 1, nickname);
		form.setHTML(3, 0, "Affiliation: ");
		form.setWidget(3, 1, affiliation);
		form.setWidget(4, 0, bar);
		form.getFlexCellFormatter().setColSpan(4, 0, 2);
					
		/*----------------------------------------------------------------*/
		final LayoutContainer topContainer = new LayoutContainer();
		final TableLayout topLayout = new TableLayout(3);
		topLayout.setWidth("100%");
		topLayout.setHeight("100%");
			
		final TableData header = new TableData();
		header.setHorizontalAlign(HorizontalAlignment.CENTER);
		header.setHeight("25px");
		
		final TableData body = new TableData();
		body.setHorizontalAlign(HorizontalAlignment.CENTER);
		body.setMargin(5);
		
		final TableData selectButton = new TableData();
		selectButton.setHorizontalAlign(HorizontalAlignment.CENTER);
		selectButton.setHeight("25px");
			
		final TableData filler = new TableData();
		filler.setColspan(2);
		
		topContainer.setLayout(topLayout);
		
		topContainer.add(recentUsersHeading, header);
		topContainer.add(new HTML(""), header);
		topContainer.add(searchResultHeading, header);
					
		topContainer.add(recent, body);
		topContainer.add(form, body);
		topContainer.add(results, body);
			
		topContainer.add(new Button("Select All",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				recent.getSelectionModel().selectAll();
			}
		}), selectButton);
		topContainer.add(new HTML(""), selectButton);
		topContainer.add(new HTML(""), selectButton);
		
		topContainer.add(new Button("Clear Recent Users",
				new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove all of your recent users?", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						RecentlyAccessedCache.impl.deleteAll(RecentlyAccessed.USER, new GenericCallback<Object>() {
							public void onSuccess(Object result) {
								loadRecentUsers();	
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Failed to clear recent users, please try again later.");
							}
						});
					}
				});
			}
		}), selectButton);
		topContainer.add(new HTML(""), selectButton);
		topContainer.add(new HTML(""), selectButton);
		
		/*----------------------------------------------------------------*/
					
		final LayoutContainer bottomContainer = new LayoutContainer();
		final TableLayout bottomLayout = new TableLayout(4);
		bottomLayout.setWidth("100%");
		bottomLayout.setHeight("100%");
		
		final TableData listHeader = new TableData();
		listHeader.setHorizontalAlign(HorizontalAlignment.CENTER);
		listHeader.setHeight("25px");
		
		final TableData lists = new TableData();
		lists.setHorizontalAlign(HorizontalAlignment.CENTER);
		lists.setMargin(3);
		
		final TableData filler1 = new TableData();
		filler1.setColspan(3);
		
		final TableData sortButtons = new TableData();
		sortButtons.setHorizontalAlign(HorizontalAlignment.CENTER);
		sortButtons.setHeight("25px");
		
		bottomContainer.setLayout(bottomLayout);
		
		bottomContainer.add(assessorsHeading, listHeader);
		bottomContainer.add(reviewersHeading, listHeader);
		bottomContainer.add(contributorsHeading, listHeader);
		bottomContainer.add(facilitatorsHeading, listHeader);
		
		bottomContainer.add(assessors, lists);
		bottomContainer.add(reviewers, lists);
		bottomContainer.add(contributors, lists);
		bottomContainer.add(facilitators, lists);
				
		bottomContainer.add(new Button("Sort A-Z",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				assessors.getStore().sort("name", SortDir.ASC);
			}
		}), sortButtons);
		bottomContainer.add(new Button("Sort A-Z",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				reviewers.getStore().sort("name", SortDir.ASC);
			}	
		}), sortButtons);
		bottomContainer.add(new Button("Sort A-Z",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				contributors.getStore().sort("name", SortDir.ASC);
			}
		}), sortButtons);
		bottomContainer.add(new Button("Sort A-Z",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				facilitators.getStore().sort("name", SortDir.ASC);
			}
		}), sortButtons);
					
		/*----------------------------------------------------------------*/
		
		new ListViewDragSource(results);			
		new ListViewDragSource(recent);
		new ListViewDragSource(assessors);
		new ListViewDragSource(reviewers);
		new ListViewDragSource(contributors);
		new ListViewDragSource(facilitators);
		
		new ListViewDropTarget(results);
		
		allowDropItems(recent,"RECENT");
		allowDropItems(assessors,"OTHER");
		allowDropItems(reviewers,"OTHER");
		allowDropItems(contributors,"OTHER");
		allowDropItems(facilitators,"OTHER");
		
		final LayoutContainer topWrapper = new LayoutContainer();
		topWrapper.setLayout(new BorderLayout());
		BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH,
				110, 200, 110);
		data.setMargins(new Margins(10));
		topWrapper.add(topContainer, data);

		final LayoutContainer bottomWrapper = new LayoutContainer();
		bottomWrapper.setLayout(new BorderLayout());
		bottomWrapper.add(bottomContainer, data);
				
		setLayout(new FillLayout());
		add(topWrapper);
		add(bottomWrapper);
		
		addButtons();
		loadRecentUsers();

		loadAssessmentData(callback);
	}
	
	/*
	 * Finds all the user IDs selected and does an initial search to load 
	 * up all the users that are not in the UserCache.  Then, adds the users 
	 * to the appropriate panels.
	 * 
	 * Borrowed from SISCompleteListTextArea.java
	 */
	protected void loadAssessmentData(final DrawsLazily.DoneDrawingCallback callback) {
		final Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
		
		final String[] fieldNames = new String[] {
			CanonicalNames.RedListAssessors, CanonicalNames.RedListEvaluators, 
			CanonicalNames.RedListContributors, CanonicalNames.RedListFacilitators
		};
		
		final Set<String> userids = new HashSet<String>();
		for (String fieldName : fieldNames) {
			Field field = assessment.getField(fieldName);
			if (field == null)
				continue;
			
			ProxyField proxy = new ProxyField(field);
			List<Integer> values = 
				proxy.getForeignKeyListPrimitiveField(SISOptionsList.FK_LIST_KEY);
			
			if (values != null)
				for (Integer value : values)				
					if(value != 0)
						if (!UserCache.impl.hasUser(value))
							userids.add(value.toString());

		}
		
		if (userids.isEmpty()){
			loadSavedDetails(CanonicalNames.RedListAssessors,assessors);
			loadSavedDetails(CanonicalNames.RedListEvaluators,reviewers);
			loadSavedDetails(CanonicalNames.RedListContributors,contributors);
			loadSavedDetails(CanonicalNames.RedListFacilitators,facilitators);
			
			callback.isDrawn();
		}else {
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			map.put("userid", new ArrayList<String>(userids));
			
			search(map, new GenericCallback<List<MCSearchResults>>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Error loading existing values, please try again later.");
					//No callback, no need to draw the window.
				}
				@Override
				public void onSuccess(List<MCSearchResults> results) {
					for (MCSearchResults result : results)
						UserCache.impl.addUser(result.getUser());
					
					loadSavedDetails(CanonicalNames.RedListAssessors,assessors);
					loadSavedDetails(CanonicalNames.RedListEvaluators,reviewers);
					loadSavedDetails(CanonicalNames.RedListContributors,contributors);
					loadSavedDetails(CanonicalNames.RedListFacilitators,facilitators);
					
					callback.isDrawn();
				}
			});
		}		
	}
	
	protected void addButtons() {
		addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSave();
			}
		}));
		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}

	protected void onSave() {
		try{
			Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
		
			saveField(CanonicalNames.RedListAssessors, assessment, assessors.getStore());
			saveField(CanonicalNames.RedListEvaluators, assessment, reviewers.getStore());
			saveField(CanonicalNames.RedListContributors, assessment, contributors.getStore());
			saveField(CanonicalNames.RedListFacilitators, assessment, facilitators.getStore());

			WindowUtils.showLoadingAlert("Saving Assessors...");
			AssessmentClientSaveUtils.saveAssessment(null,assessment, new GenericCallback<Object>() {
				public void onFailure(Throwable arg0) {
					WindowUtils.hideLoadingAlert();
					WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
				}
	
				public void onSuccess(Object arg0) {
					WindowUtils.hideLoadingAlert();
					Info.display("Save Complete", "Successfully saved assessment {0}.",
							AssessmentCache.impl.getCurrentAssessment().getSpeciesName());
					Debug.println("Explicit save happened at {0}", AssessmentCache.impl.getCurrentAssessment().getLastEdit().getCreatedDate());
					
					/*
					 * Requirement: "Upon clicking Save & Close, the panel would close and
					 * bring the user back to the Assessment Information tab, where the text 
					 * strings for each of these data boxes would be updated/refreshed." 
					 */
					ManageCreditsWindow.this.hide();
					FieldWidgetCache.impl.resetWidgetContents(
						CanonicalNames.RedListAssessors, CanonicalNames.RedListEvaluators, 
						CanonicalNames.RedListContributors, CanonicalNames.RedListFacilitators
					);
				}
			});
			
			
		}catch(InsufficientRightsException e){
			WindowUtils.errorAlert("Sorry, but you do not have sufficient rights to perform this action.");
		}
		
	}

	private void saveField(String fieldName, Assessment assessment, ListStore<MCSearchResults> store) {
		final Set<Integer> userIDs = new HashSet<Integer>();
		final ArrayList<ClientUser> selectedUsers = new ArrayList<ClientUser>();
		
		for (MCSearchResults model : store.getModels())
			userIDs.add(model.getUser().getId());
			
		Field field = assessment.getField(fieldName);
		if (field == null) {
			field = new Field(fieldName, assessment);
			assessment.getField().add(field);
		}
		
		/*
		 * The SISOptionsList is the field that drives the original 
		 * UI widget for assessors, evaluatiors, and contributors. 
		 * So, I am going to use the same field keys from there for 
		 * this save operation.
		 */
		ProxyField proxy = new ProxyField(field);
		proxy.setForeignKeyListPrimitiveField(SISOptionsList.FK_LIST_KEY, new ArrayList<Integer>(userIDs));
		
		/* 
		 * Add UI Selected User's to the UserCache		
		 */
		final Iterator<MCSearchResults> iterator = store.getModels().iterator();
		while (iterator.hasNext()) {
			ClientUser user = iterator.next().getUser();		
			selectedUsers.add(user);
		}
		UserCache.impl.addUsers(selectedUsers);
	}
	
	public void loadRecentUsers() {
		ListStore<MCSearchResults> recentUserStore = new ListStore<MCSearchResults>();
		recentUserStore.setStoreSorter(new MCSearchResultsComparator());
	
		List<RecentUser> users = RecentlyAccessedCache.impl.list(RecentlyAccessed.USER); 
			
		for (RecentUser user : users)
			recentUserStore.add(new MCSearchResults(user.getUser()));
		
		recentUserStore.sort("name", SortDir.ASC);
		
		recent.setStore(recentUserStore);

	}
	
	public void loadSavedDetails(String canonicalName, ListView<MCSearchResults> list) {
		
		Assessment assessment = AssessmentCache.impl.getCurrentAssessment();			
		Field field = assessment.getField(canonicalName);
		if (field == null)
			return;

		ListStore<MCSearchResults> store = new ListStore<MCSearchResults>();
		store.setStoreSorter(new MCSearchResultsComparator());
								
		List<Integer> ids;
		ProxyField proxy = new ProxyField(field);
		ids = proxy.getForeignKeyListPrimitiveField(SISOptionsList.FK_LIST_KEY);
		
		if (ids != null)
			for (Integer userID : ids)
				if(userID != 0)
					if (UserCache.impl.hasUser(userID))
						store.add(new MCSearchResults(UserCache.impl.getUser(userID)));
					
		list.setStore(store);
	}

	/**
	 * Performs a search for the parameters given, based on an or search, and
	 * returns the results in the callback
	 * 
	 * @param params
	 * @param callback
	 */
	public void search(Map<String, List<String>> params,
			final GenericCallback<List<MCSearchResults>> callback) {
		String query = "";
		if (!params.isEmpty()) {
			query = "?";
			final Iterator<Map.Entry<String, List<String>>> iter = params.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, List<String>> entry = iter.next();
				for (Iterator<String> valIter = entry.getValue().iterator(); valIter.hasNext(); ) {
					query += entry.getKey() + "=" + valIter.next();
					if (valIter.hasNext())
						query += "&";
				}
			}
		}

		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getUserBase()
				+ "/browse/profile" + query, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				List<MCSearchResults> results = new ArrayList<MCSearchResults>();
				
				for (RowData rowData : parser.getRows()) {
					ClientUser user = new ClientUser();
					final Iterator<String> iterator = rowData.keySet().iterator();
					while (iterator.hasNext()) {
						final String property = iterator.next().toLowerCase();
						user.setProperty(property, rowData.getField(property));
						if (property.equalsIgnoreCase("firstname"))
							user.setFirstName(rowData.getField(property));
						else if (property.equalsIgnoreCase("lastname"))
							user.setLastName(rowData.getField(property));
						else if (property.equalsIgnoreCase("nickname"))
							user.setNickname(rowData.getField("nickname"));
						else if (property.equalsIgnoreCase("initials"))
							user.setInitials(rowData.getField(property));
						else if (property.equalsIgnoreCase("email"))
							user.setEmail(rowData.getField(property));
						else if (property.equalsIgnoreCase("userid"))
							user.setId(Integer.parseInt(rowData.getField(property)));
						else if (property.equalsIgnoreCase("quickgroup"))
							user.setProperty("quickGroup", rowData.getField(property));
						else if (property.equalsIgnoreCase("username"))
							user.setUsername(rowData.getField(property));
						else
							user.setProperty(property, rowData.getField(property));
					}
					
					results.add(new MCSearchResults(user));
				}
				
				callback.onSuccess(results);
			}
		});
	}

	/**
	 * Performs the search and updates the status text. Appropriate parameter
	 * keys are "lastname", "firstname", and "affiliation", although "email"
	 * could be added in the future as any column in the profile table is
	 * applicable at this time. A like query will be performed if parameters are
	 * supplied, with multiple parameters being OR'd together.
	 * 
	 * @param params
	 *            - search paramteers.
	 */
	protected void search(Map<String, String> params) {
		GenericCallback<List<MCSearchResults>> callback = new GenericCallback<List<MCSearchResults>>() {

			public void onFailure(Throwable caught) {
				ManageCreditsWindow.this.hide();
				WindowUtils.errorAlert("Error",
						"Could not load results, please try again later");

			}

			public void onSuccess(List<MCSearchResults> result) {
				final ListStore<MCSearchResults> store = new ListStore<MCSearchResults>();
				store.setStoreSorter(new MCSearchResultsComparator());
				for (MCSearchResults res : result) {
					if (!containsSearchResult(assessors.getStore(), res)) {
						store.add(res);
					}
				}
				store.sort("name", SortDir.ASC);

				results.setStore(store);

				String statusText = "Found " + store.getCount() + " results.";
				status.setHTML(statusText);
			}
		};

		Map<String, List<String>> newParams = new HashMap<String, List<String>>();
		for (Entry<String, String> entry : params.entrySet()) {
			ArrayList<String> list = new ArrayList<String>();
			list.add(entry.getValue());
			newParams.put(entry.getKey(), list);
		}
		search(newParams, callback);

	}

	public void setSearchResultHeading(String searchResultHeading) {
		this.searchResultHeading.setHtml("<b>" + searchResultHeading + "</b>");
	}

	public void setRecentUsersHeading(String recentUsersHeading) {
		this.recentUsersHeading.setHtml("<b>" + recentUsersHeading + "</b>");
	}

	public void setAssessorsHeading(String assessorsHeading) {
		this.assessorsHeading.setHtml("<b>" + assessorsHeading + "</b>");
	}
	
	public void setReviewersHeading(String reviewersHeading) {
		this.reviewersHeading.setHtml("<b>" + reviewersHeading + "</b>");
	}
	
	public void setContributorsHeading(String contributorsHeading) {
		this.contributorsHeading.setHtml("<b>" + contributorsHeading + "</b>");
	}
	
	public void setFacilitatorsHeading(String facilitatorsHeading) {
		this.facilitatorsHeading.setHtml("<b>" + facilitatorsHeading + "</b>");
	}
	
	@SuppressWarnings("unchecked")
	private void allowDropItems(final ListView<MCSearchResults> drList,final String id){
		ListViewDropTarget selectedTarget = new ListViewDropTarget(drList); 
		selectedTarget.addDNDListener(new DNDListener() {
			@Override
			public void dragEnter(DNDEvent e) {
				try {
					boolean hasCollision = false;
					List<MCSearchResults> data = (List<MCSearchResults>)(e.getData());
					for (MCSearchResults datum : data) {
						if (hasCollision = detectCollision(datum)) {
							e.setCancelled(true);
							e.getStatus().update("Duplicate User Detected");
							break;
						}
					}
					if (!hasCollision)
						e.getStatus().update(GXT.MESSAGES.grid_ddText(data.size()));
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
			}
			
			@Override
			public void dragDrop(DNDEvent e) {
				if (!id.equals("RECENT")) {
					List<MCSearchResults> data = (List<MCSearchResults>)(e.getData());
					List<RecentUser> recent = new ArrayList<RecentUser>();
					for (MCSearchResults user : data)
						recent.add(new RecentUser(user.getUser()));
					
					RecentlyAccessedCache.impl.add(RecentlyAccessed.USER, recent);
					DeferredCommand.addPause();
					DeferredCommand.addCommand(new Command() {
						public void execute() {
							loadRecentUsers();
						}
					});
				}
			}
			
			private boolean detectCollision(MCSearchResults datum) {
				for( MCSearchResults cur : drList.getStore().getModels() ) {	
					if( datum.get("userid").toString().trim().equals(cur.get("userid").toString().trim()))
						return true;
				}
				return false;
			}
		});	
	}
}
