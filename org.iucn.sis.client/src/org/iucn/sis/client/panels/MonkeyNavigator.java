package org.iucn.sis.client.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.MarkedCache;
import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.api.utils.TaxonPagingLoader;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.workingsets.WorkingSetSubscriber;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.utils.AssessmentFormatter;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DataListEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

@SuppressWarnings("deprecation")
public class MonkeyNavigator extends LayoutContainer implements DrawsLazily {
	
	//private static final int LIST_SIZE = 175;
	//private static final int NAVIGATOR_SIZE = 225 + 25;
	
	private final DataList workingSetList, taxonList, assessmentList;
	private final MonkeyNavigatorPanel workingSetContainer, taxonContainer, assessmentContainer;
	
	private final ListStore<TaxonListElement> taxonListStore;
	private final TaxonPagingLoader taxonPagingLoader;

	private final TaxonDataListBinder taxonListBinder;
	private PagingToolBar taxonPagingToolBar;
	//private int selectedTaxonPage;
	
	private DataListItem selectedWorkingSet, selectedTaxon, selectedAssessment;
	
	private WorkingSet curNavWorkingSet;
	private Taxon curNavTaxon;
	private Assessment curNavAssessment;
	
	private boolean isDrawn;
	
	public MonkeyNavigator() {
		super();
		setBorders(false);
		/*setClosable(true);
		setHeading("Monkey Navigator 2.0");
		setIconStyle("icon-monkey-face");*/
		setStyleName("navigator");
		//setSize(com.google.gwt.user.client.Window.getClientWidth() - 20, NAVIGATOR_SIZE);
		setLayout(new FillLayout());
		setLayoutOnChange(true);
		
		isDrawn = false;
		
		workingSetContainer = new MonkeyNavigatorPanel(new FillLayout());
		workingSetContainer.setHeading("Working Sets");
		workingSetList = new DataList();
		workingSetList.setBorders(false);
		workingSetList.setSelectionMode(SelectionMode.SINGLE);
		//workingSetList.setHeight(LIST_SIZE);
		workingSetList.setContextMenu(createMarkingContextMenu(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataListItem item = workingSetList.getSelectedItem();
				if (item == null || item.getData("workingSet") == null)
					return;
				
				markWorkingSet(item, ce.getItem().getItemId());
			}
		}));
		setupWorkingSetsToolbar();
		
		taxonContainer = new MonkeyNavigatorPanel(new FillLayout());
		taxonContainer.setHeading("Taxon List");
		taxonList = new DataList();
		taxonList.setSelectionMode(SelectionMode.SINGLE);
		taxonList.setBorders(false);
		taxonList.setContextMenu(createMarkingContextMenu(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataListItem item = taxonList.getSelectedItem();
				Integer itemID = taxonListBinder.getSelection().get(0).getNode().getId();
				
				markTaxa(item, itemID, ce.getItem().getItemId());
			}
		}));
		
		taxonPagingLoader = new TaxonPagingLoader();
		
		taxonListStore =
			new ListStore<TaxonListElement>(taxonPagingLoader.getPagingLoader());
		
		taxonListBinder =
			new TaxonDataListBinder(taxonList, taxonListStore);
		taxonListBinder.setDisplayProperty("fullName");
		taxonListBinder.setStyleProvider(new ModelStringProvider<TaxonListElement>() {
			public String getStringValue(TaxonListElement model, String property) {
				String style = "";
				if (model.getNode() != null) {
					style = MarkedCache.impl.getTaxaStyle(model.getNode().getId());
					if (style.contains("green"))
						style = "green-menu";
					else if (style.contains("red"))
						style = "red-menu";
					else if (style.contains("blue"))
						style = "blue-menu";
					else
						style = "";
				} else {
					style =  "font-weight: bold !important; color: gray !important;";
				}
				return style;
			}
		});
		setupTaxonToolbar();
		
		assessmentContainer = new MonkeyNavigatorPanel(new FillLayout());
		assessmentContainer.setHeading("Assessments");
		assessmentList = new DataList();
		assessmentList.setSelectionMode(SelectionMode.SINGLE);
		assessmentList.setBorders(false);
		assessmentList.setContextMenu(createMarkingContextMenu(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataListItem item = assessmentList.getSelectedItem();
				if (item == null)
					return;

				markAssessment(item, ce.getItem().getItemId());
			}
		}));
		setupAssessmentsToolbar();
	}
	
	/*@Override
	protected void afterRender() {
		super.afterRender();
		DeferredCommand.addPause();
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				if (selectedTaxon != null) {
					try {
						taxonList.scrollIntoView(selectedTaxon);
					} catch (Throwable e) {
						Debug.println(e);
					}
				}
			}
		});
	}*/
	
	/*public void show() {
		draw(new DoneDrawingCallback() {
			public void isDrawn() {
				showAt(10, 0);
			}
		});
	}
	
	private void showAt(int left, int top) {
		setPosition(left, top);
		super.show();
	}*/
	
	public void draw(final DrawsLazily.DoneDrawingCallback callback) {
		selectedWorkingSet = null;
		selectedTaxon = null;
		selectedAssessment = null;
		
		curNavWorkingSet = WorkingSetCache.impl.getCurrentWorkingSet();
		curNavTaxon = TaxonomyCache.impl.getCurrentTaxon();
		curNavAssessment = AssessmentCache.impl.getCurrentAssessment();
		
		drawTaxa(curNavWorkingSet, new DoneDrawingCallbackWithParam<LayoutContainer>() {
			public void isDrawn(final LayoutContainer taxa) {
				drawAssessments(curNavTaxon, new DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer>() {
					public void isDrawn(LayoutContainer assessments) {
						workingSetContainer.removeAll();
						taxonContainer.removeAll();
						assessmentContainer.removeAll();
						
						workingSetContainer.add(drawWorkingSets());
						taxonContainer.add(taxa);
						assessmentContainer.add(assessments);
						
						setSelectionAndAddListeners(!isDrawn);
						
						final LayoutContainer container = new LayoutContainer(new BorderLayout());
						container.add(workingSetContainer, new BorderLayoutData(LayoutRegion.WEST, .30f, 5, 4000));
						container.add(taxonContainer, new BorderLayoutData(LayoutRegion.CENTER, .37f, 5, 4000));
						container.add(assessmentContainer, new BorderLayoutData(LayoutRegion.EAST, .33f, 5, 4000));
						
						removeAll();
						add(container);
						
						isDrawn = true;
						
						callback.isDrawn();
					}
				});
			}
		});
	}
	
	public void setSelectionAndAddListeners(boolean addListeners) {
		if (selectedWorkingSet != null)
			workingSetList.setSelectedItem(selectedWorkingSet);
		else
			workingSetList.setSelectedItem(workingSetList.getItem(0));
		
		if (selectedTaxon != null) {
			if (selectedTaxon.getData("taxon") != null)
				taxonList.getSelectionModel().select(selectedTaxon, false);
			else {
				Integer index = selectedTaxon.getData("index");
				int activePage;
				if (index != null)
					activePage = ((index + 1) / taxonPagingToolBar.getPageSize()) + 1;
				else
					activePage = 1;
				
				selectedTaxon = taxonList.getItemByItemId(selectedTaxon.getText());
				
				taxonPagingToolBar.setActivePage(activePage);
				
				taxonList.getSelectionModel().select(selectedTaxon, false);
			}
		}
		
		if (selectedAssessment != null)
			assessmentList.setSelectedItem(selectedAssessment);
		
		if (!addListeners)
			return;
		
		workingSetList.addListener(Events.SelectionChange, new Listener<DataListEvent>() {
			public void handleEvent(DataListEvent be) {
				if (be.getSelected().isEmpty())
					return;
				
				DataListItem item = be.getSelected().get(0);
				
				curNavWorkingSet = (WorkingSet) item.getData("workingSet");
				curNavTaxon = null;
				curNavAssessment = null;
				
				taxonContainer.removeAll();
				assessmentContainer.removeAll();
				
				drawTaxa(curNavWorkingSet, new DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer>() {
					public void isDrawn(LayoutContainer parameter) {
						taxonContainer.add(parameter);
					}
				});
			}
		});
		
		taxonList.addListener(Events.SelectionChange, new Listener<DataListEvent>() {
			public void handleEvent(DataListEvent be) {
				if (be.getSelected().isEmpty())
					return;
				
				DataListItem item = be.getSelected().get(0);
				if (item.getData("taxon") == null)
					return;
				
				curNavTaxon = (Taxon) item.getData("taxon");
				curNavAssessment = null;
				
				assessmentContainer.removeAll();
				
				drawAssessments(curNavTaxon, new DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer>() {
					public void isDrawn(LayoutContainer parameter) {
						assessmentContainer.add(parameter);
					}
				});
			}
		});
		
		taxonListBinder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
			public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
				if (se.getSelectedItem() != null && se.getSelectedItem().getNode() != null) {
					curNavTaxon = (Taxon) se.getSelectedItem().getNode();
					curNavAssessment = null;
					
					assessmentContainer.removeAll();
					
					drawAssessments(curNavTaxon, new DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer>() {
						public void isDrawn(LayoutContainer parameter) {
							assessmentContainer.add(parameter);
						}
					});
				}
			}
		});
	}
	
	private Menu createMarkingContextMenu(SelectionListener<MenuEvent> listener) {
		Menu menu = new Menu();
		menu.add(createMenuItem("green-menu", MarkedCache.GREEN, "Mark Green", listener));
		menu.add(createMenuItem("blue-menu", MarkedCache.BLUE, "Mark Blue", listener));
		menu.add(createMenuItem("red-menu", MarkedCache.RED, "Mark Red", listener));
		menu.add(createMenuItem("regular-menu", MarkedCache.NONE, "Unmark",  listener));
		
		return menu;
	}
	
	private MenuItem createMenuItem(String style, String itemID, String text, SelectionListener<MenuEvent> listener) {
		MenuItem item = new MenuItem();
		item.addStyleName(style);
		item.setItemId(itemID);
		item.setText(text);
		item.addSelectionListener(listener);
		
		return item;
	}
	
	private void markWorkingSet(DataListItem item, String color) {
		final Integer workingSetID = Integer.valueOf(item.getItemId());
		if (!MarkedCache.impl.getWorkingSetStyle(workingSetID).equalsIgnoreCase(color))
			item.removeStyleName(MarkedCache.impl.getWorkingSetStyle(workingSetID));
		
		MarkedCache.impl.markWorkingSet(workingSetID, color);
		
		item.addStyleName(color);
	}
	
	private void markTaxa(DataListItem item, Integer itemID, String color) {
		if (!MarkedCache.impl.getTaxaStyle(itemID).equalsIgnoreCase(color))
			item.removeStyleName(MarkedCache.impl.getTaxaStyle(itemID));
		
		MarkedCache.impl.markTaxa(itemID, color);
		
		item.addStyleName(color);
	}
	
	private void markAssessment(DataListItem item, String color) {
		final Integer assessmentID = Integer.valueOf(item.getItemId());
		if (!MarkedCache.impl.getAssessmentStyle(assessmentID).equalsIgnoreCase(color))
			item.removeStyleName(MarkedCache.impl.getAssessmentStyle(assessmentID));
		
		MarkedCache.impl.markAssement(assessmentID, color);
		
		item.addStyleName(color);
	}
	
	private void setupWorkingSetsToolbar() {
		final IconButton goToSet = createIconButton("icon-go-jump", "Open Working Set", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				if (workingSetList.getSelectedItem() != null) {
					WorkingSet selected = (WorkingSet) workingSetList.getSelectedItem().getData("workingSet");
					if (selected != null) {
						navigate(selected, null, null);
						/*WorkingSetCache.impl.setCurrentWorkingSet(selected.getId(), true, new SimpleListener() {
							public void handleEvent() {
								
								//hide();								
							}
						});*/
					}
				}
			}
		});
		
		workingSetContainer.addTool(createIconButton("icon-add", "Add New Working Set", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				// TODO Create new working set
				WindowUtils.infoAlert("Coming soon...");
			}
		}));
		workingSetContainer.addTool(new SeparatorToolItem());
		workingSetContainer.addTool(goToSet);
		workingSetContainer.addTool(createIconButton("icon-information", "Working Set Details", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				if (workingSetList.getSelectedItem() == null)
					WindowUtils.errorAlert("Please select a working set.");
				
				final WorkingSet ws = (WorkingSet) workingSetList.getSelectedItem().getData("workingSet");
				if (ws == null)
					return;
					
				final Window s = WindowUtils.getWindow(false, true, ws.getWorkingSetName());
				s.setLayout(new FillLayout());
				s.setTitle(ws.getWorkingSetName());
				s.setSize(600, 400);
				
				final Grid table = new Grid(5, 2);
				table.setCellSpacing(4);

				// IF THERE ARE SPECIES TO GET
				if (ws.getSpeciesIDs().size() > 0) {
					WorkingSetCache.impl.fetchTaxaForWorkingSet(ws, new GenericCallback<List<Taxon>>() {
						public void onFailure(Throwable caught) {
							table.setHTML(0, 0, "<b>Manager: </b>");
							table.setHTML(0, 1, ws.getCreator().getUsername());
							table.setHTML(1, 0, "<b>Date: </b>");
							table.setHTML(1, 1, FormattedDate.impl.getDate(ws.getCreatedDate()));
							table.setHTML(2, 0, "<b>Number of Species: </b>");
							table.setHTML(2, 1, "" + ws.getSpeciesIDs().size());
							table.setHTML(3, 0, "<b>Description: </b>");
							table.setHTML(3, 1, ws.getDescription());
							s.layout();
						};

						public void onSuccess(List<Taxon> arg0) {
							table.setHTML(0, 0, "<b>Manager: </b>");
							table.setHTML(0, 1, ws.getCreator().getUsername());
							table.setHTML(1, 0, "<b>Date: </b>");
							table.setHTML(1, 1, FormattedDate.impl.getDate(ws.getCreatedDate()));
							table.setHTML(2, 0, "<b>Number of Species: </b>");
							table.setHTML(2, 1, "" + ws.getSpeciesIDs().size());

							String species = "";
							for (Taxon taxon : arg0) {
								species += taxon.getFullName() + ", ";
							}
							if (species.length() > 0) {
								table.setHTML(3, 0, "<b>Species: </b>");
								table.setHTML(3, 1, species.substring(0, species.length() - 2));
								table.setHTML(4, 0, "<b>Description: </b>");
								table.setHTML(4, 1, ws.getDescription());
							} else {
								table.setHTML(3, 0, "<b>Description: </b>");
								table.setHTML(3, 1, ws.getDescription());
							}
							s.add(table);
							s.show();
						};
					});
				}
				// ELSE LOAD NO SPECIES
				else {
					table.setHTML(0, 0, "There are no species in the " + ws.getWorkingSetName() + " working set.");
					s.add(table);
					
					s.show();
				}
				
			}
		}));
		workingSetContainer.addTool(createIconButton("icon-image", "Download Working Set", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				// TODO Auto-generated method stub
				
			}
		}));
		workingSetContainer.addTool(createIconButton("icon-favorite", "Subscribe to Working Set", new SelectionListener<IconButtonEvent>() {
			public void componentSelected(IconButtonEvent ce) {
				final WorkingSetSubscriber panel = new WorkingSetSubscriber();
				
				Window window = new Window();
				window.setLayout(new FillLayout());
				window.setSize(700, 500);
				window.setHeading("Subscribe to Working Set");
				window.addListener(Events.Show, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						panel.refresh();
					}
				});
				window.add(panel);
				window.show();
			}
		}));
	}
	
	public LayoutContainer drawWorkingSets() {
		workingSetList.removeAll();
		workingSetList.add(new DataListItem("&lt;None&gt;"));
		
		for (WorkingSet cur : WorkingSetCache.impl.getWorkingSets().values()) {
			DataListItem curItem = new DataListItem(cur.getWorkingSetName());
			curItem.setData("workingSet", cur);
			curItem.setItemId(cur.getId()+"");
			curItem.addStyleName(MarkedCache.impl.getWorkingSetStyle(cur.getId()));
			workingSetList.add(curItem);

			if (cur.equals(curNavWorkingSet))
				selectedWorkingSet = curItem;
		}
		
		LayoutContainer sets = new LayoutContainer(new FillLayout());
		sets.setScrollMode(Scroll.NONE);
		sets.setBorders(false);
		sets.add(workingSetList);
		
		return sets;
	}
	
	private IconButton createIconButton(String icon, String title, SelectionListener<IconButtonEvent> listener) {
		IconButton button = new IconButton(icon);
		button.addSelectionListener(listener);
		button.setTitle(title);
		
		return button;
	}
	
	private void setupTaxonToolbar() {
		final Button goToTaxon = new Button();
		goToTaxon.setIconStyle("icon-go-jump");
		goToTaxon.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
					public void handleEvent() {
						Taxon selected = null;
						if (taxonList.getSelectedItem() != null)
							selected = (Taxon) taxonList.getSelectedItem().getData("taxon");

						if (selected == null && taxonListBinder != null && taxonListBinder.getSelection().size() > 0)
							selected = taxonListBinder.getSelection().get(0).getNode();

						if (selected != null) {
							WorkingSet curWorkingSet = null;
							if (curNavWorkingSet != null)
								curWorkingSet = WorkingSetCache.impl.getWorkingSet(curNavWorkingSet.getId());
							
							navigate(curWorkingSet, selected, null);
							//TaxonomyCache.impl.setCurrentTaxon(selected, false);

							/*if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
									ClientUIContainer.bodyContainer.tabManager.taxonHomePage))
								ClientUIContainer.bodyContainer.fireEvent(Events.SelectionChange);
							else
								ClientUIContainer.bodyContainer
								.setSelection(ClientUIContainer.bodyContainer.tabManager.taxonHomePage);*/

							//hide();
						}
					}
				});
			}
		});
		
		taxonContainer.addTool(goToTaxon);
	}
	
	public void drawTaxa(WorkingSet curNavWorkingSet, final DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer> callback) {
		/**
		 * By default, show recently assessed taxa.  Why, I don't know, 
		 * since it's on the homepage...
		 */
		if (curNavWorkingSet == null) {
			if (TaxonomyCache.impl.getRecentlyAccessed().isEmpty()) {
				callback.isDrawn(getNoTaxaScreen("No Recently Accessed Taxa"));
			} else {
				taxonList.removeAll();
				
				for (Taxon curTaxon : TaxonomyCache.impl.getRecentlyAccessed()) {
					DataListItem curItem = new DataListItem(curTaxon.getFriendlyName());
					curItem.setData("taxon", curTaxon);
					curItem.setItemId(curTaxon.getId() + "");
					curItem.addStyleName(MarkedCache.impl.getTaxaStyle(curTaxon.getId()));
					
					taxonList.add(curItem);

					if (curTaxon.equals(curNavTaxon))
						selectedTaxon = curItem;
					/*if (curTaxon.equals(curNavTaxon))
						taxonToSelect = taxonList.getItem(taxonList.getItemCount() - 1);*/
				}
				
				final LayoutContainer taxa = new LayoutContainer(new FillLayout());
				taxa.setScrollMode(Scroll.NONE);
				taxa.add(taxonList);
				
				callback.isDrawn(taxa);
			}
		} 
		/*
		 * Otherwise, show taxa from the selected working set.
		 */
		else {
			taxonPagingLoader.getFullList().clear();
			
			taxonPagingToolBar = new PagingToolBar(40);
			taxonPagingToolBar.bind(taxonPagingLoader.getPagingLoader());
			
			final List<Integer> speciesIDs = 
				curNavWorkingSet.getSpeciesIDs();

			if (speciesIDs.isEmpty()) {
				callback.isDrawn(getNoTaxaScreen());
			} 
			else {
				//This is so the loading box will show properly
				/*DeferredCommand.addPause();
				DeferredCommand.addCommand(new Command() {
					public void execute() {*/
						WorkingSetCache.impl.fetchTaxaForWorkingSet(curNavWorkingSet, new GenericCallback<List<Taxon>>() {
							public void onFailure(Throwable caught) {
								WindowUtils.hideLoadingAlert();
								
								callback.isDrawn(getNoTaxaScreen());
							}

							public void onSuccess(List<Taxon> result) {								
								taxonPagingLoader.getFullList().clear();
								taxonPagingLoader.getPagingLoader().setOffset(0);
								
								String currentFamily = "";
								
								int index = 0;
								for (Integer species : speciesIDs) {
									Taxon curTaxon = TaxonomyCache.impl.getTaxon(species);
									if (curTaxon != null) {
										String familyFootprint = curTaxon.getFootprint()[TaxonLevel.FAMILY];
										if (!currentFamily.equals(familyFootprint))
											taxonPagingLoader.getFullList().add(
												new TaxonListElement(currentFamily = familyFootprint)
											);
	
										TaxonListElement curEl = new TaxonListElement(curTaxon, "");
										
										taxonPagingLoader.getFullList().add(curEl);
	
										if (curTaxon.equals(curNavTaxon)) {
											selectedTaxon = new DataListItem(curTaxon.getId()+"");
											selectedTaxon.setItemId(curTaxon.getId() + "");
											selectedTaxon.setData("index", index);
										}
										
										index++;
									}
									else
										Debug.println("MonkeyNav2.0 found species {0} to be null.", species);
								}

								taxonPagingLoader.getPagingLoader().load();
								
								WindowUtils.hideLoadingAlert();
								
								final LayoutContainer taxa = new LayoutContainer(new BorderLayout());
								taxa.setBorders(false);
								taxa.setScrollMode(Scroll.NONE);
								taxa.add(taxonList, new BorderLayoutData(LayoutRegion.CENTER));
								taxa.add(taxonPagingToolBar, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
								//taxa.add(taxonPagingToolBar);
								
								callback.isDrawn(taxa);
							}
						});
					/*}
				});*/
			}
		}
	}
	
	private LayoutContainer getNoTaxaScreen() {
		return getNoTaxaScreen("No taxa available.");
		
	}

	private LayoutContainer getNoTaxaScreen(String message) {
		HTML taxaHeader = new HTML(message);
		taxaHeader.addStyleName("bold");
		taxaHeader.addStyleName("color-dark-blue");
		
		final LayoutContainer taxa = new LayoutContainer();
		taxa.setScrollMode(Scroll.NONE);
		taxa.add(taxaHeader);
		
		return taxa;
	}
	
	private void setupAssessmentsToolbar() {
		final Button jump = new Button();
		jump.setIconStyle("icon-go-jump");
		jump.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
					public void handleEvent() {
						WorkingSet selectedSet = null;
						if (workingSetList.getSelectedItem() != null) {
							selectedSet = (WorkingSet) workingSetList.getSelectedItem().getData("workingSet");
							/*if (selectedSet != null)
								WorkingSetCache.impl.setCurrentWorkingSet(selectedSet.getId(), false);*/
						}
						
						/*
						 * Don't need to do this, since this will happen when the assessment 
						 * changes.
						 */
						Taxon selectedTaxon = null;
						if (taxonList.getSelectedItem() != null)
							selectedTaxon = (Taxon) taxonList.getSelectedItem().getData("taxon");

						if (selectedTaxon == null && taxonListBinder != null && taxonListBinder.getSelection().size() > 0)
							selectedTaxon = taxonListBinder.getSelection().get(0).getNode();
						
						Assessment selected = (Assessment) assessmentList.getSelectedItem().getData("assessment");
						if (selected != null) {
							//AssessmentCache.impl.setCurrentAssessment(selected, false);
							navigate(selectedSet, selectedTaxon, selected);

							/*if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
									ClientUIContainer.bodyContainer.tabManager.assessmentEditor))
								ClientUIContainer.bodyContainer.fireEvent(Events.SelectionChange);
							else
								ClientUIContainer.bodyContainer
								.setSelection(ClientUIContainer.bodyContainer.tabManager.assessmentEditor);*/

							//hide();
						}
					}
				});
			}
		});

		assessmentContainer.addTool(jump);	
	}
	
	private void drawAssessments(final Taxon curNavTaxon, final DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer> callback) {
		if (curNavTaxon == null) {
			HTML header = new HTML("Please select a taxon.");
			header.addStyleName("bold");
			header.addStyleName("color-dark-blue");
			
			final LayoutContainer assessments = new LayoutContainer();
			assessments.setBorders(false);
			assessments.setScrollMode(Scroll.NONE);
			assessments.add(header);

			callback.isDrawn(assessments);
		} else {
			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, curNavTaxon.getId()), 
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					final LayoutContainer assessments = new LayoutContainer();
					assessments.setBorders(false);
					assessments.setScrollMode(Scroll.NONE);
					assessments.add(new HTML("Failed to load assessments"));
					
					callback.isDrawn(assessments);
				}

				public void onSuccess(String arg0) {
					List<Assessment> draftAssessments =
						new ArrayList<Assessment>(AssessmentCache.impl.getDraftAssessmentsForTaxon(curNavTaxon.getId()));

					assessmentList.removeAll();
					
					if (draftAssessments.size() > 0) {
						DataListItem curItem = new DataListItem("Draft Assessment(s)");
						curItem.setEnabled(false);
						assessmentList.add(curItem);
						
						Collections.sort(draftAssessments, new AssessmentDateComparator());
					}
					
					DataListItem selected = null;
					for (Assessment current : draftAssessments) {
						String displayable;
						if (current.getDateAssessed() != null )
							displayable = FormattedDate.impl.getDate();
						else
							displayable = "";

						final DataListItem curItem;
						if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, 
								current)) {
							if (displayable == null || displayable.equals(""))
								displayable = FormattedDate.impl.getDate(new Date(current.getDateModified()));

							if (current.isRegional())
								displayable += " --- " + RegionCache.impl.getRegionName(current.getRegionIDs());
							else
								displayable += " --- " + "Global";

							curItem = new DataListItem(displayable);
						} else {
							if (current.isRegional())
								curItem = new DataListItem("Regional Draft Assessment");
							else
								curItem = new DataListItem("Global Draft Assessment");
							
							curItem.setIconStyle("icon-lock");
							curItem.setEnabled(false);
						}
						
						curItem.setData("assessment", current);
						curItem.setItemId(current.getId() + "");
						curItem.addStyleName(MarkedCache.impl.getAssessmentStyle(current.getId()));
						
						assessmentList.add(curItem);

						if (current.equals(curNavAssessment))
							selected = curItem;
					}

					List<Assessment> pubAssessments = 
						new ArrayList<Assessment>(AssessmentCache.impl.getPublishedAssessmentsForTaxon(curNavTaxon.getId()));
					if (pubAssessments.size() > 0) {
						DataListItem curItem = new DataListItem("Published Assessment(s)");
						curItem.setEnabled(false);
						assessmentList.add(curItem);

						Collections.sort(pubAssessments, new AssessmentDateComparator());

						for (Assessment current : pubAssessments) {
							if (current != null) {
								String displayable;
								if (current.getDateAssessed() != null)
									displayable = FormattedDate.impl.getDate(current.getDateAssessed());
								else
									displayable = "";

								if (current.isRegional())
									displayable += " --- " + RegionCache.impl.getRegionName(current.getRegionIDs());
								else
									displayable += " --- " + "Global";

								displayable += " --- " + AssessmentFormatter.getProperCategoryAbbreviation(current);

								curItem = new DataListItem(displayable);
								curItem.setData("assessment", current);
								curItem.setItemId(current.getId() + "");
								curItem.addStyleName(MarkedCache.impl.getAssessmentStyle(current.getId()));
								assessmentList.add(curItem);

								if (current.equals(curNavAssessment))
									selected = curItem;
							}
						}
					}

					if (selected != null)
						assessmentList.setSelectedItem(selected);

					final LayoutContainer assessments = new LayoutContainer();
					assessments.setLayout(new FillLayout());
					assessments.setBorders(false);
					assessments.setScrollMode(Scroll.NONE);
					
					if (assessmentList.getItemCount() == 0) {
						HTML header = new HTML("No assessments available.");
						header.addStyleName("bold");
						header.addStyleName("color-dark-blue");
						
						assessments.add(header);
					} else {
						assessments.add(assessmentList);
					}
					
					callback.isDrawn(assessments);
				}
			});
		}
	}
	
	private void navigate(WorkingSet ws, Taxon taxon, Assessment assessment) {
		StateManager.impl.setState(ws, taxon, assessment);
	}
	
	public static class TaxonDataListBinder extends DataListBinder<TaxonListElement> {
		
		public TaxonDataListBinder(DataList list, ListStore<TaxonListElement> store) {
			super(list, store);
		}
		
		@Override
		protected DataListItem createItem(TaxonListElement model) {
			DataListItem item = super.createItem(model);
			if (model != null && model.getNode() != null)
				item.setItemId(model.getNode().getId() + "");
				
			return item;
		}
		
	}
	
	public static class AssessmentDateComparator implements Comparator<Assessment> {
		
		public int compare(Assessment o1, Assessment o2) {
			Date date1 = o1.getDateAssessed();
			Date date2 = o2.getDateAssessed();
			
			if (date1 == null && date2 == null)
				return 0;
			else if (date1 == null)
				return 1;
			else if (date2 == null)
				return -1;
			else
				return date1.compareTo(date2) * -1;
		}		
		
	}

}
