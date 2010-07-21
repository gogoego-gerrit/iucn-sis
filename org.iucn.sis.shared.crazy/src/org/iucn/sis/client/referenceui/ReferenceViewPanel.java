package org.iucn.sis.client.referenceui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.InsufficientRightsException;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.data.assessments.AssessmentUtilFactory;
import org.iucn.sis.shared.data.references.ReferenceUtils;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binder.TableBinder;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableSelectionModel;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

/**
 * ReferenceViewPanel.java
 * 
 * A widget container that acts as a filter and viewer for references.
 * 
 * @author carl.scott
 * 
 */
public class ReferenceViewPanel extends TabPanel {

	private final int SHOW_COUNT = 25;
	private int current = 0;

	private TabItem searchContainer;
	private TabItem biblioContainer;

	private Button createNew;
	private Button editExisting;
	private Button add;
	
	private NativeDocument document;
	private Table searchTable;
	private ListStore<ReferenceModel> searchStore;
	private TableBinder<ReferenceModel> searchBinder;
	private GenericLazyPagingLoader<ReferenceModel> searchPagingLoader;
	private PagingToolBar searchPagingToolbar;

	private HTML searchResultCount;

	private Table bibTable;
	private ListStore<ReferenceModel> bibStore;
	private TableBinder<ReferenceModel> bibBinder;

	private boolean fieldsShowing = false;

	private TableColumnModel biblioColumnModel;
	private TableColumnModel searchColumnModel;

	private boolean nonUniqueIDs;
	private HashMap<String, ReferenceUI> currentUniqueReferences;
	private ArrayList<ReferenceUI> currentReferences;

	// private String currentAssessmentID;
	// private ArrayList currentRefs;
	private TextBox author;
	private TextBox title;
	private TextBox year;

	private ToolBar toolBar;
	private ToolBar biblioBar;

	private HorizontalPanel filterBar;

	private GenericCallback<Object> callback;
	private GenericCallback<Object> rCallback;

	private GenericCallback<Object> defaultCallback = new GenericCallback<Object>() {
		public void onFailure(Throwable caught) {
			WindowUtils.errorAlert("Error!", "Error committing changes to the "
					+ "server. Ensure you are connected to the server, then try " + "the process again.");
		}

		public void onSuccess(Object result) {
			WindowUtils.infoAlert("Success!", "Successfully committed reference " + "changes.");
		}
	};

	private Referenceable curReferenceable;

	private KeyboardListenerAdapter keyListener = new KeyboardListenerAdapter() {
		@Override
		public void onKeyUp(Widget sender, char keyCode, int modifiers) {
			super.onKeyUp(sender, keyCode, modifiers);

			if (keyCode == KEY_ENTER) {
				searchPagingLoader.getPagingLoader().setOffset(0);
				search();
			}
		}
	};

	public ReferenceViewPanel() {
		// currentAssessmentID=null;
		// currentRefs=new ArrayList();
		TabItem bibTab = new TabItem();
		bibTab.setText("Bibliography");
		add(bibTab);

		biblioContainer = bibTab;
		RowLayout biblioLayout = new RowLayout(Orientation.VERTICAL);
		biblioContainer.setLayout(biblioLayout);
		// biblioContainer.setLayoutOnChange(true);

		author = new TextBox();
		title = new TextBox();
		year = new TextBox();

		author.addKeyboardListener(keyListener);
		title.addKeyboardListener(keyListener);
		year.addKeyboardListener(keyListener);

		TabItem searchTab = new TabItem();
		searchTab.setText("Reference Search");
		add(searchTab);

		searchContainer = searchTab;
		RowLayout searchLayout = new RowLayout(Orientation.VERTICAL);
		searchContainer.setLayout(searchLayout);
		// searchContainer.setLayoutOnChange(true);

		currentUniqueReferences = new HashMap<String, ReferenceUI>();

		TableColumn[] columns = new TableColumn[4];
		columns[0] = new TableColumn("author", "Author", 300f);
		columns[0].setWidth(300);
		columns[0].setResizable(true);
		columns[0].setComparator(new PortableAlphanumericComparator());

		columns[1] = new TableColumn("title", "Title", 300f);
		columns[1].setWidth(300);
		columns[1].setResizable(true);
		columns[1].setComparator(new PortableAlphanumericComparator());

		columns[2] = new TableColumn("year", "Year", 50f);
		columns[2].setWidth(50);
		columns[2].setResizable(true);
		columns[2].setComparator(new PortableAlphanumericComparator());

		columns[3] = new TableColumn("count", "# used", 50f);
		columns[3].setWidth(50);
		columns[3].setResizable(true);
		columns[3].setComparator(new PortableAlphanumericComparator());

		searchColumnModel = new TableColumnModel(columns);

		columns = new TableColumn[2];
		columns[0] = new TableColumn("citation", "Citation", 633f);
		columns[0].setMaxWidth(1500);
		columns[0].setMinWidth(200);
		columns[0].setResizable(true);
		columns[0].setComparator(new PortableAlphanumericComparator());

		columns[1] = new TableColumn("field", "Field", 67f);
		columns[1].setResizable(true);
		columns[1].setComparator(new PortableAlphanumericComparator());
		columns[1].setHidden(true);

		biblioColumnModel = new TableColumnModel(columns);

		searchTable = new Table(searchColumnModel);
		searchTable.setSelectionModel(new TableSelectionModel(SelectionMode.SINGLE));
		searchTable.setBorders(false);
		searchTable.setBulkRender(true);
		searchPagingLoader = new GenericLazyPagingLoader<ReferenceModel>() {
			@Override
			public void fetchSublist(int start, AsyncCallback<List<ReferenceModel>> showResultsCallback) {
				doFetchSublist(start, showResultsCallback);
			}
		};
		searchPagingToolbar = new PagingToolBar(25);
		searchPagingToolbar.bind(searchPagingLoader.getPagingLoader());
		searchStore = new ListStore<ReferenceModel>(searchPagingLoader.getPagingLoader());
		searchBinder = new TableBinder<ReferenceModel>(searchTable, searchStore);

		bibTable = new Table(biblioColumnModel);
		bibTable.setSelectionModel(new TableSelectionModel(SelectionMode.MULTI));
		bibTable.setBorders(true);
		bibTable.setBulkRender(false);

		bibStore = new ListStore<ReferenceModel>();
		bibBinder = new TableBinder<ReferenceModel>(bibTable, bibStore);

		createToolbar();
		createFilterBar();
		buildBiblioToolbar();

		RowData fill_horizontal = new RowData();
		fill_horizontal.setWidth(1d);
		fill_horizontal.setHeight(25);

		searchContainer.add(toolBar, fill_horizontal);
		searchContainer.add(filterBar, fill_horizontal);
		searchResultCount = new HTML();
		searchContainer.add(searchResultCount, fill_horizontal);
		searchContainer.add(searchTable, new RowData(1d, 1d));
		searchContainer.add(searchPagingToolbar, new RowData(1d, -1d));
	}

	/**
	 * Puts the referenceUI object into the current unique references map.
	 * Checks first to see if its already in the map, and sets nonUniqueIDs to
	 * true if applicable;
	 * 
	 * @param curRef
	 */
	private void addToUniqueReferences(ReferenceUI curRef) {
		if (currentUniqueReferences.containsKey(curRef.getReferenceID()))
			nonUniqueIDs = true;
		else
			currentUniqueReferences.put(curRef.getReferenceID(), curRef);
	}

	private void buildBiblioToolbar() {
		biblioBar = new ToolBar();
		biblioBar.setHeight(25);

		ToggleButton removeItem = new ToggleButton();
		removeItem.setText("Remove Reference");
		removeItem.setIconStyle("icon-book-delete");
		removeItem.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				if (!fieldsShowing && nonUniqueIDs) {
					WindowUtils.errorAlert("Cannot remove", "Your bibliography has "
							+ "the same reference attached to multiple fields. To ensure "
							+ "the reference is removed from the correct place, please "
							+ "filter your references by field, then try removing again.");
				} else if( !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, 
						AssessmentCache.impl.getCurrentAssessment()) ) {
					WindowUtils.errorAlert("Cannot remove", "You do not have permission to edit this " +
							"assessment, so you may not remove references.");
				} else {
					ArrayList<ReferenceUI> list = new ArrayList<ReferenceUI>();
					List<ReferenceModel> items = bibBinder.getSelection();
					for (ReferenceModel curItem : items) {
						ReferenceUI curRef = curItem.ref;
						list.add(curRef);
						// currentReferences.remove(curRef);
						removeFromCurrentList(curRef);
					}
					if (list.size() > 0) {
						onRemoveSelected(list);
						rebuildUniqueRefs(currentReferences);
					}
				}
			}
		});
		Button editExisting = new Button();
		editExisting.setIconStyle("icon-book-edit");
		editExisting.setText("Edit/View Selected Reference");
		editExisting.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				if (bibBinder.getSelection().size() > 1) {
					WindowUtils.errorAlert("Error", "Please select only one record to view.");
				} else if (bibBinder.getSelection().size() == 0) {
					WindowUtils.errorAlert("Error", "Please select a record to view.");
				} else {
					openEditor(bibBinder.getSelection().get(0), bibStore, true);
				}
			}
		});
		final ToggleButton showFields = new ToggleButton();
		showFields.setIconStyle("icon-accept");
		showFields.setText("Show Field Info");
		showFields.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {

				boolean showing = bibTable.getColumnModel().getColumn(1).isHidden();
				bibTable.getColumnModel().getColumn(1).setHidden(!showing);

				if (showing) {
					fieldsShowing = true;

					showFields.setText("Hide Field Info");
					showFields.setIconStyle("icon-stop");

					populateBibTable(currentReferences.listIterator());
					biblioContainer.layout();
					bibTable.sort(0, SortDir.ASC);
				} else {
					fieldsShowing = false;

					showFields.setText("Show Field Info");
					showFields.setIconStyle("icon-accept");

					populateBibTable(currentUniqueReferences.values().iterator());
					biblioContainer.layout();
					bibTable.sort(0, SortDir.ASC);
				}
			}
		});

		biblioBar.add(removeItem);
		biblioBar.add(editExisting);
		biblioBar.add(showFields);
	}

	private Widget createCitationWidget(String text, boolean citationValid) {
		if (text == null || text.equals(""))
			text = "Not Available";
		final String text2 = text;
		HTML html = new HTML(text);
		html.setWordWrap(true);
		if (!citationValid) {
			HorizontalPanel hp = new HorizontalPanel() {
				@Override
				public String toString() {
					return text2;
				}
			};
			hp.add(html);
			html = new HTML("*");
			html.addStyleName("red-menu");
			hp.add(html);
			hp.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_LEFT);

			return hp;
		} else
			return html;
	}

	/**
	 * Creates the filtering widgets for author, title, and year
	 * 
	 */
	private void createFilterBar() {
		filterBar = new HorizontalPanel();
		filterBar.setSpacing(5);

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
			@Override
			public void componentSelected(ButtonEvent ce) {
				searchPagingLoader.getPagingLoader().setOffset(0);
				search();
			}
		}));

		HTML spacer = new HTML("&nbsp;");
		spacer.setWidth("40px");
		filterBar.add(spacer);
	}

	private void createToolbar() {
		toolBar = new ToolBar();
		toolBar.setHeight(25);

		createNew = new Button();
		createNew.setIconStyle("icon-add");
		createNew.setText("Enter New Reference...");
		createNew.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				openEditor(null, null, true);
			}
		});

		editExisting = new Button();
		editExisting.setIconStyle("icon-book-edit");
		editExisting.setText("Edit/View Selected Reference");
		editExisting.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				if (searchBinder.getSelection().size() > 1) {
					WindowUtils.errorAlert("Error", "Please select only one record to view.");
				} else if (searchBinder.getSelection().size() == 0) {
					WindowUtils.errorAlert("Error", "Please select a record to view.");
				} else {
					String count = searchTable.getSelectedItem().getValue(3).toString();
					openEditor(searchBinder.getSelection().get(0), searchStore, !count.trim().equals("0"));
				}
			}
		});

		add = new Button();
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
					ArrayList<ReferenceUI> list = new ArrayList<ReferenceUI>();
					List<ReferenceModel> items = searchBinder.getSelection();
					for (ReferenceModel curItem : items) {
						ReferenceUI refToAdd = curItem.ref;
						list.add(refToAdd);
					}

					if (list.size() > 0) {
						onAddSelected(list);
					}
				}
			}
		});

		toolBar.add(createNew);
		toolBar.add(new SeparatorToolItem());
		toolBar.add(editExisting);
		toolBar.add(new SeparatorToolItem());
		toolBar.add(add);
	}

	private void doFetchSublist(final int start, final AsyncCallback<List<ReferenceModel>> finishFetchingCallback) {
		StringBuilder q = new StringBuilder("<query>");

		if (!author.getText().equals("")) {
			q.append("<author><![CDATA[");
			q.append(author.getText());
			q.append("]]></author>");
		}

		if (!title.getText().equals("")) {
			q.append("<title><![CDATA[");
			q.append(title.getText());
			q.append("]]></title>");
		}

		if (!year.getText().equals("")) {
			q.append("<year><![CDATA[");
			q.append(year.getText());
			q.append("]]></year>");
		}

		q.append("</query>");

		document = SimpleSISClient.getHttpBasicNativeDocument();
		document.post("/refsvr/search/reference", q.toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Failure: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				NativeNodeList referenceList = null;
				NativeElement totalCountEl = null;
				int total = 0;
				try {

					List<ReferenceModel> ret = new ArrayList<ReferenceModel>();
					referenceList = document.getDocumentElement().getElementsByTagName("reference");
					totalCountEl = document.getDocumentElement().getElementByTagName("totalCount");
					if( totalCountEl != null )
						total = Integer.parseInt(totalCountEl.getAttribute("total"));

					for (int i = 0; i < referenceList.getLength(); i++) {	
						final NativeElement currentReference = referenceList.elementAt(i);
						if (currentReference.getNodeName().equalsIgnoreCase("reference")) {
							ReferenceUI current = new ReferenceUI(currentReference);
							String count = currentReference.getAttribute("count");
							if( count == null ) count = "N/A";
							ret.add(new ReferenceModel(current,  count));
						}
					}

					searchResultCount.setText(total + " Filtered Result(s)");
					searchPagingLoader.setTotal(total);
					finishFetchingCallback.onSuccess(ret);
				} catch (Throwable e) {
					e.printStackTrace();
					finishFetchingCallback.onFailure(e);
				}
			}
		});
	}

	/**
	 * To override, called when user clicks "Add Selected" button. Treat as a
	 * callback
	 * 
	 * @param selectedValues
	 *            the selected values from the table.
	 */
	public void onAddSelected(ArrayList<ReferenceUI> selectedValues) {
		if( curReferenceable != null ) {
			curReferenceable.addReferences(selectedValues, new GenericCallback<Object>() {
				public void onFailure(Throwable caught) {
					setReferences(curReferenceable, callback, rCallback);
					WindowUtils.errorAlert("Error!", "Error committing changes to the "
							+ "server. Ensure you are connected to the server, then try " + "the process again.");
					search();
				}
				public void onSuccess(Object result) {
					setReferences(curReferenceable, callback, rCallback);
					WindowUtils.infoAlert("Success!", "Successfully committed reference changes.");
					search();
				}
			});
		}
	}

	public void onRemoveSelected(ArrayList<ReferenceUI> selectedValues) {
		if( curReferenceable == null )
			return;
		else {
			curReferenceable.removeReferences(selectedValues, rCallback);
			setReferences(curReferenceable, callback, rCallback);
		}
	}

	/**
	 * Opens a ReferenceEditor instance.
	 * 
	 * @param reference
	 *            the reference to edit, or null to create a new one
	 * @param promptToReplace TODO
	 */
	private void openEditor(final ReferenceModel reference, final Store<ReferenceModel> fromStore, final boolean promptToReplace) {
		ReferenceUI ref = null;
		if( reference != null )
			ref = reference.ref;
			
		ReferenceEditor editor = new ReferenceEditor(ref) {
			@Override
			protected void afterDelete() {
				super.afterDelete();
				search();
			}
			
			private void afterSave(final ReferenceModel reference, final Store<ReferenceModel> fromStore) {
				if( reference != null && fromStore != null ) {
					reference.rebuild();
					fromStore.update(reference);
				}

				if( curReferenceable != null ) {
					setReferences(curReferenceable, callback, rCallback);
					showBibliography();
				}
				if( fromStore == bibStore )
					curReferenceable.onReferenceChanged(defaultCallback);
				else if( fromStore == searchStore )
					showSearchPanel();
								
				close();
			}
			
			private void doReplace(final ReferenceModel reference, final Store<ReferenceModel> fromStore,
					final ReferenceUI returnedRef, final String assessmentID, String assessmentType) {
				final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
				ndoc.post("/reference/replace", ReferenceUtils.seralizeReplaceRequest(
						reference.ref, returnedRef, assessmentID, assessmentType), new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						WindowUtils.hideLoadingAlert();
						System.out.println("ERROR preforming replace.");
						reference.ref.setReferenceID(returnedRef.getReferenceID());

						afterSave(reference, fromStore);
					}
					public void onSuccess(String result) {
						WindowUtils.hideLoadingAlert();
						
						reference.ref.setReferenceID(returnedRef.getReferenceID());
						String message = "";
						
						if( assessmentID != null )
							message += "Replaced the reference on your current assessment.<br/><br/>";
							
						message += XMLUtils.cleanFromXML(ndoc.getDocumentElement().getTextContent());

						final Dialog dialog = new Dialog();
						dialog.setButtons(Dialog.OK);
						dialog.setClosable(true);
						dialog.setHideOnButtonClick(true);
						dialog.addWindowListener(new WindowListener() {
							@Override
							public void windowHide(WindowEvent we) {
								afterSave(reference, fromStore);
							}
						});
						dialog.setSize(350, 300);
						dialog.setScrollMode(Scroll.AUTOY);
						dialog.add(new HTML(message));
						dialog.show();
					}
				});
			}

			@Override
			public void onSaveSuccessful(final ReferenceUI returnedRef) {
				// WindowUtils.infoAlert("Success", "Save Successful.");\
				if( reference == null ) {
					afterSave(null, null);
				} else if( promptToReplace && !reference.ref.getReferenceID().equals(returnedRef.getReferenceID())
						&& AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.REFERENCE_REPLACE_FEATURE)) {
					promptToReplace(reference, fromStore, returnedRef);
				} else {
					reference.ref.setReferenceID(returnedRef.getReferenceID());
					afterSave(reference, fromStore);
				}
			}

			private void promptToReplace(final ReferenceModel reference, final Store<ReferenceModel> fromStore,
					final ReferenceUI returnedRef) {
				
				String message = (fromStore == bibStore ? "Apply the changes you made to this " +
					"assessment's reference to all other assessments that use it?" :
					"Your edits have been saved as a new reference. Would you like to replace " +
					"the old reference on all assessments with your new one? If you select no, " +
					"this reference will not be attached to any assessments by default.");
				
				WindowUtils.confirmAlert("Reference Changed", message, new Listener<MessageBoxEvent>() {
					
					public void handleEvent(MessageBoxEvent we) {
						if( we.getButtonClicked().getText().equalsIgnoreCase("yes") ) {
							String assessmentID = null;
							String assessmentType = null;
							
							if( fromStore == bibStore ) { //It's already replaced locally.
								WindowUtils.showLoadingAlert("Performing replace. This could take a few minutes...");
								assessmentID = AssessmentCache.impl.getCurrentAssessment().getAssessmentID();
								assessmentType = AssessmentCache.impl.getCurrentAssessment().getType();
								
								doReplace(reference, fromStore, returnedRef, assessmentID, assessmentType);
							} else { 
								if( AssessmentCache.impl.getCurrentAssessment() != null ) {
									WindowUtils.showLoadingAlert("Performing replace. This could take a few minutes...");
									boolean found = false;
									
									HashMap<String, ArrayList<ReferenceUI>> curRefs = AssessmentCache.impl.getCurrentAssessment().getReferences();
									for( Entry<String, ArrayList<ReferenceUI>> curEntry : curRefs.entrySet() ) {
										if( curEntry.getValue().contains(reference.ref) ) {
											curEntry.getValue().remove(reference.ref);
											returnedRef.associatedField = reference.ref.associatedField;
											
											curEntry.getValue().add(returnedRef);
											found = true;
										}
									}
									
									if( found ) {
										assessmentID = AssessmentCache.impl.getCurrentAssessment().getAssessmentID();
										assessmentType = AssessmentCache.impl.getCurrentAssessment().getType();

										try {
											AssessmentUtilFactory.getSaveUtils().saveAssessment(AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
												public void onFailure(Throwable caught) {
													doReplace(reference, fromStore, returnedRef, null, null);
												}
												public void onSuccess(Object result) {
													doReplace(reference, fromStore, returnedRef, 
															AssessmentCache.impl.getCurrentAssessment().getAssessmentID(), 
															AssessmentCache.impl.getCurrentAssessment().getType());
												}
											});
										} catch (InsufficientRightsException e) {
											AssessmentCache.impl.resetCurrentAssessment();
											doReplace(reference, fromStore, returnedRef, null, null);
										}
										
									} else {
										doReplace(reference, fromStore, returnedRef, assessmentID, assessmentType);
									}
								}
								else
									doReplace(reference, fromStore, returnedRef, null, null);
							}
						} else {
							reference.ref.setReferenceID(returnedRef.getReferenceID());
							afterSave(reference, fromStore);
						}
					}
				});
			}
		};
		editor.setHeading(reference == null ? "New Reference" : "Edit Reference");
	}

	private void populateBibTable(Iterator<ReferenceUI> iter) {
		bibStore.removeAll();

		while (iter.hasNext()) {
			ReferenceUI current = iter.next();// list.get(k);
			current.generateCitationIfNotAlreadyGenerate();
			bibStore.add(new ReferenceModel(current));
		}
	}

	private void rebuildUniqueRefs(ArrayList<ReferenceUI> refs) {
		currentUniqueReferences.clear();
		for (int i = 0; i < refs.size(); i++)
			addToUniqueReferences(refs.get(i));
	}

	private void removeFromCurrentList(ReferenceUI ref) {
		int removeIndex = -1;
		for (int i = 0; i < currentReferences.size() && removeIndex == -1; i++) {
			ReferenceUI cur = (ReferenceUI) currentReferences.get(i);

			if (ref.getReferenceID() == cur.getReferenceID() && ref.getAssociatedField() == cur.getAssociatedField())
				removeIndex = i;
		}

		currentReferences.remove(removeIndex);
	}

	/**
	 * Fetches a fresh document from the DB and performs the given query.
	 * 
	 * @param query
	 *            the query parameters to add to the url
	 * @param reset
	 *            true to start back at 0, false to keep the current index
	 */
	public void search() {
		String query = "?";
		if (!author.getText().equals(""))
			query += "Author=" + author.getText();
		if (!title.getText().equals(""))
			query += (query.equals("?") ? "" : "&") + "Title=" + title.getText();
		if (!year.getText().equals(""))
			query += (query.equals("?") ? "" : "&") + "Year=" + year.getText();

		if (query.equals("?")) {
			Window.alert("Please enter at least one search criterion.");
			return;
		}

		/*		document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get("/refsvr/search/reference" + query, new GenericCallback<Object>() {
			public void onFailure(Throwable caught) {
				Window.alert("Failure: " + caught.getMessage());
			}

			public void onSuccess(Object result) {*/
		showSearchPanel();
		//			}
		//		});
	}

	public void setOnAddSelected(GenericCallback<Object> aCallback) {
		callback = aCallback;
	}

	public void setOnRemoveSelected(GenericCallback<Object> aCallback) {
		rCallback = aCallback;
	}

	public void setReferences(Referenceable referenceable) {
		setReferences(referenceable, defaultCallback, defaultCallback);
	}

	public void setReferences(Referenceable referenceable, GenericCallback<Object> addCallback,
			GenericCallback<Object> removeCallback) {
		curReferenceable = referenceable;

		callback = (addCallback == null ? defaultCallback : addCallback);
		rCallback = (removeCallback == null ? defaultCallback : removeCallback);

		if( curReferenceable != null ) {
			currentReferences = curReferenceable.getReferencesAsList();
			rebuildUniqueRefs(currentReferences);
			biblioContainer.removeAll();
			biblioContainer.getHeader().setEnabled(true);
			
			if (currentUniqueReferences.size() > 0) {
				showBibliography();
			} else {
				biblioContainer.add(new HTML("<br><b>No References.</b>"));
				biblioContainer.layout();
			}
			
			add.setEnabled(true);
		} else {
			currentReferences = null;
			biblioContainer.getHeader().setEnabled(false);
			setSelection(searchContainer);
			add.setEnabled(false);
		}
		
		nonUniqueIDs = false;
	}

	/**
	 * Shows recently used references. We just want to show them all, and we
	 * don't have download concerns since this is store on the client. Hence we
	 * are not reusing the show() function directly.
	 * 
	 */
	public void showBibliography() {
		fieldsShowing = false;
		bibTable.getColumnModel().getColumn(1).setHidden(true);

		if (currentUniqueReferences != null) {
			Iterator<ReferenceUI> iter = currentUniqueReferences.values().iterator();
			populateBibTable(iter);
		}

		if (biblioBar.getParent() == null) {
			HorizontalPanel hp = new HorizontalPanel();
			HTML html = new HTML("* Citation possibly incomplete");
			hp.setSpacing(10);
			hp.add(html);
			html.addStyleName("red-menu");

			RowData fill_horizontal = new RowData();
			fill_horizontal.setWidth(1d);
			fill_horizontal.setHeight(25);
			biblioContainer.add(biblioBar, fill_horizontal);
			biblioContainer.add(bibTable, new RowData(1d, 1d));
			biblioContainer.add(hp, fill_horizontal);
		}

		try {
			biblioContainer.layout();
			bibTable.sort(0, SortDir.ASC);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Resets the panel and shows a set of data given a starting point. This can
	 * be optimized to not redraw with each showing, but I leave that as
	 * practice for a Google Summer of Code enthusiast.
	 */
	public void showSearchPanel() {
		searchPagingLoader.getPagingLoader().load();

		try {
			searchContainer.layout();
		} catch (Exception e) {
		}
	}
}
