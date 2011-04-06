package org.iucn.sis.client.panels.references;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.utils.PagingPanel;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;
import com.solertium.util.portable.XMLWritingUtils;

public class ReferenceSearchViewTab extends PagingPanel<ReferenceModel> {
	
	private final ReferenceViewAPI parent;
	
	private final TextBox author;
	private final TextBox title;
	private final TextBox year;
	
	private Grid<ReferenceModel> grid;
	
	public ReferenceSearchViewTab(ReferenceViewAPI parent) {
		super();
		setLayout(new FillLayout());
		
		this.parent = parent;
		
		author = new TextBox();
		title = new TextBox();
		year = new TextBox();
		
		final KeyUpHandler handler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				int keyCode = event.getNativeKeyCode();
				
				if (keyCode == KeyCodes.KEY_ENTER) {
					refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
			}
		};
		
		author.addKeyUpHandler(handler);
		title.addKeyUpHandler(handler);
		year.addKeyUpHandler(handler);
		
		draw();
	}
	
	public void draw() {
		final GridSelectionModel<ReferenceModel> sm = 
			new GridSelectionModel<ReferenceModel>();
		sm.setSelectionMode(SelectionMode.MULTI);
		
		grid = new Grid<ReferenceModel>(getStoreInstance(), new ColumnModel(getColumnConfig()));
		grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<ReferenceModel>>() {
			public void handleEvent(GridEvent<ReferenceModel> be) {
				editReference(be.getModel());
			}
		});
		grid.setSelectionModel(sm);
		grid.setAutoExpandColumn("title");
		
		final LayoutContainer north = new LayoutContainer();
		north.add(createToolbar());
		north.add(createFilterBar());
		
		final LayoutContainer wrapper = new LayoutContainer();
		wrapper.add(getPagingToolbar());
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(north, new BorderLayoutData(LayoutRegion.NORTH, 75, 75, 75));
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(wrapper, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
		
		add(container);
	}
	
	private ToolBar createToolbar() {
		final ToolBar toolBar = new ToolBar();
		toolBar.setHeight(25);

		final Button createNew = new Button();
		createNew.setIconStyle("icon-add");
		createNew.setText("Enter New Reference...");
		createNew.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				
				final ComplexListener<ReferenceModel> saveListener = new ComplexListener<ReferenceModel>() {
					public void handleEvent(ReferenceModel reference) {
						//showSearchPanel();
						refreshView();
					}
				};
				
				ReferenceViewTabPanel.openEditor(null, saveListener);
				
			}
		});

		final Button editExisting = new Button();
		editExisting.setIconStyle("icon-book-edit");
		editExisting.setText("Edit/View Selected Reference");
		editExisting.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				editReference(grid.getSelectionModel().getSelectedItem());
			}
		});

		final Button add = new Button();
		add.setIconStyle("icon-save");
		add.setText("Attach Selected");
		add.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				if( !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, 
						AssessmentCache.impl.getCurrentAssessment()) ) {
					WindowUtils.errorAlert("Cannot add", "You do not have permission to edit this " +
							"assessment, so you may not add references.");
				} else {
					ArrayList<Reference> list = new ArrayList<Reference>();
					
					for (ReferenceModel curItem : grid.getSelectionModel().getSelectedItems())
						list.add(curItem.getModel());
					
					if (!list.isEmpty())
						parent.onAddSelected(list);
				}
			}
		});

		toolBar.add(createNew);
		toolBar.add(new SeparatorToolItem());
		toolBar.add(editExisting);
		toolBar.add(new SeparatorToolItem());
		toolBar.add(add);
		
		return toolBar;
	}
	
	private void editReference(final ReferenceModel model) {
		if (model == null)
			WindowUtils.errorAlert("Error", "Please select a record to view.");
		else {
			/*
			String count = searchTable.getSelectedItem().getValue(3).toString();
			openEditor(searchBinder.getSelection().get(0), searchStore, !count.trim().equals("0"));
			*/
			
			final ComplexListener<ReferenceModel> saveListener = new ComplexListener<ReferenceModel>() {
				public void handleEvent(ReferenceModel reference) {
					//showSearchPanel();
					if (reference != null)
						reference.rebuild();
					refreshView();
				}
			};
			
			final SimpleListener deleteListener = new SimpleListener() {
				public void handleEvent() {
					refreshView();
				}
			};
			
			WindowUtils.MessageBoxListener listener = new WindowUtils.MessageBoxListener() {
				public void onNo() {
					open(true);
				}
				public void onYes() {
					open(false);
				}
				private void open(boolean asNew) {
					ReferenceViewTabPanel.openEditor(model, saveListener, deleteListener);
				}
			};

			String count = model.get("count").toString();
			int countInt;
			try {
				countInt = Integer.parseInt(count);
			} catch (NumberFormatException e) {
				countInt = 0;
			}
			listener.onNo();
			/*
			 * To prompt the user before they open the window, uncomment the code below.
			 */
			/*if (countInt > 0) {
				WindowUtils.confirmAlert("Confirm", "This reference is being used on at least one assessment, and " +
					"any changes you make to this reference will be reflected on those assessments.  Would " +
					"you like to make changes to this existing reference, or save your changes as a " +
					"new reference?", listener, "Save Existing", "Save as New");
			}
			else {
				listener.onNo();
			}*/
		}
	}
	
	private List<ColumnConfig> getColumnConfig() {
		final List<ColumnConfig> searchColumns = new ArrayList<ColumnConfig>();
		searchColumns.add(new ColumnConfig("author", "Author", 300));
		searchColumns.add(new ColumnConfig("title", "Title", 300));
		searchColumns.add(new ColumnConfig("year", "Year", 50));
		searchColumns.add(new ColumnConfig("count", "# used", 50));
		
		return searchColumns;
	}
	
	@Override
	protected void refreshView() {
		grid.getView().refresh(false);
	}
	
	@Override
	protected void getStore(final GenericCallback<ListStore<ReferenceModel>> callback) {
		StringBuilder q = new StringBuilder("<query>");
		q.append(XMLWritingUtils.writeCDATATag("author", author.getText(), true));
		q.append(XMLWritingUtils.writeCDATATag("title", title.getText(), true));
		q.append(XMLWritingUtils.writeCDATATag("year", year.getText(), true));
		q.append("</query>");

		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getReferenceBase() + "/refsvr/search/reference", q.toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Failure: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				final ListStore<ReferenceModel> ret = new ListStore<ReferenceModel>();
				ret.setStoreSorter(new StoreSorter<ReferenceModel>(new PortableAlphanumericComparator()));
				
				final NativeNodeList referenceList = document.getDocumentElement().getChildNodes();
				
				int total = referenceList.getLength();
				
				for (int i = 0; i < referenceList.getLength(); i++) {
					final NativeNode node = referenceList.item(i);
					if ("reference".equals(node.getNodeName())) {
						final NativeElement currentReference;
						try {
							currentReference = (NativeElement)node;
						} catch (ClassCastException e) {
							Debug.println(e);
							continue;
						}
						
						Reference current;
						try {
							current = Reference.fromXML(currentReference);
						} catch (Throwable e) {
							Debug.println(e);
							continue;
						}
						String count = currentReference.getAttribute("count");
						if (count == null ) 
							count = "N/A";
						
						ret.add(new ReferenceModel(current,  count));
					}
					else if ("totalcount".equalsIgnoreCase(node.getNodeName())) {
						NativeElement totalCountEl;
						try {
							totalCountEl = (NativeElement)node;
							total = Integer.parseInt(totalCountEl.getAttribute("total"));
						} catch (ClassCastException e) {
							continue;
						} catch (NumberFormatException e) {
							total -= 1;
							continue;
						}
					}
				}
						
				Debug.println("Found {0} references and {1} in list, added {2}.", total, referenceList.getLength(), ret.getCount());
				
				callback.onSuccess(ret);
			}
		});
	}
	
	/**
	 * Creates the filtering widgets for author, title, and year
	 * 
	 */
	private HorizontalPanel createFilterBar() {
		final HorizontalPanel filterBar = new HorizontalPanel();
		filterBar.setSpacing(5);
		filterBar.setWidth("400px");

		HorizontalPanel authorFilter = new HorizontalPanel();
		authorFilter.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		authorFilter.add(new HTML("Author:"));
		authorFilter.add(author);

		HorizontalPanel titleFilter = new HorizontalPanel();
		titleFilter.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		titleFilter.add(new HTML("Title: "));
		titleFilter.add(title);

		HorizontalPanel yearFilter = new HorizontalPanel();
		yearFilter.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		yearFilter.add(new HTML("Year: "));
		yearFilter.add(year);

		filterBar.add(authorFilter);
		filterBar.add(titleFilter);
		filterBar.add(yearFilter);

		filterBar.add(new Button("Search", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
			}
		}));

		/*
		HTML spacer = new HTML("&nbsp;");
		spacer.setWidth("40px");
		
		filterBar.add(spacer);*/
		
		return filterBar;
	}

}
