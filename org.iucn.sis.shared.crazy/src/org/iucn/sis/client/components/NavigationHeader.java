package org.iucn.sis.client.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.panels.HeaderSummaryPanel;
import org.iucn.sis.client.components.panels.TaxonomyBrowserPanel.TaxonListElement;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.data.assessments.AssessmentFetchRequest;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.utilities.FormattedDate;
import org.iucn.sis.client.utilities.TaxonPagingLoader;
import org.iucn.sis.shared.acl.InsufficientRightsException;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.MarkedCache;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.WorkingSetCache;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentUtilFactory;
import org.iucn.sis.shared.data.assessments.RegionCache;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DataListEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Popup;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class NavigationHeader extends LayoutContainer {
	private static final int NAVIGATOR_SIZE = 225;
	private static final int LIST_SIZE = 175;

	private ToolBar bar = null;

	private Button nextTaxa;
	private Button prevTaxa;
	private Button currentTaxa;

	private HeaderSummaryPanel summaryPanel = null;

	private Popup navPopup = null;
	private LayoutContainer navigator = null;
	private LayoutContainer sets = null;
	private LayoutContainer taxa = null;
	private LayoutContainer assessments = null;

	private DataList setList = null;
	private DataList taxonList = null;
	private DataList assessmentList = null;

	private ListStore<TaxonListElement> taxonListStore = null;
	private DataListBinder<TaxonListElement> taxonListBinder = null;
	private PagingToolBar taxonPagingToolBar = null;
	private TaxonPagingLoader taxonPagingLoader = null;

	private WorkingSetData curNavSet = null;
	private TaxonNode curNavTaxon = null;
	private AssessmentData curNavAssessment = null;

	private Button monkeyToolItem;

	private Button goToSet = null;
	private Button goToTaxon = null;
	private Button goToAss = null;

	private int setToSelect = 0;
	private Object taxonToSelect = null;
	private int assessmentToSelect = 0;

	public NavigationHeader() {
		setBorders(true);
		setLayout(new RowLayout(Orientation.VERTICAL));
		MarkedCache.impl.update();

		bar = new ToolBar();
		bar.setHeight(25);

		monkeyToolItem = new Button();
		monkeyToolItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (!navigator.isRendered())
					update();

				refreshSets(new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}

					public void onSuccess(String result) {
						setListSelected();
					}
				});

				monkeyToolItem.fireEvent(Events.OnMouseOut);

				// navPopup.showAt(getAbsoluteLeft() - 30, getAbsoluteTop());
				navPopup.showAt(10, 0);
				// navPopup.setSize(getHeaderWidth() + 60, NAVIGATOR_SIZE);
				// navigator.setSize(getHeaderWidth() + 60, NAVIGATOR_SIZE);
				navPopup.setSize(Window.getClientWidth() - 20, NAVIGATOR_SIZE);
				navigator.setSize(Window.getClientWidth() - 20, NAVIGATOR_SIZE);
				navigator.layout();
				navPopup.layout();
			};
		});

		monkeyToolItem.setText("Navigate");
		monkeyToolItem.setIconStyle("icon-monkey-face");
		bar.add(monkeyToolItem);

		nextTaxa = new Button();
		nextTaxa.setIconStyle("icon-arrow-right");
		nextTaxa.setToolTip("Next Taxon");
		nextTaxa.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if (AssessmentCache.impl.getCurrentAssessment() != null
						&& AssessmentUtilFactory.getSaveUtils().shouldSaveCurrentAssessment()) {
					WindowUtils.confirmAlert("By the way...", "Navigating away from this page will"
							+ " revert unsaved changes. Would you like to save?", new Listener<MessageBoxEvent>() {
						public void handleEvent(MessageBoxEvent be) {
							if (be.getButtonClicked().getText().equalsIgnoreCase("cancel")) {

							} else if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
								try {
									AssessmentUtilFactory.getSaveUtils().saveAssessment(AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
										public void onFailure(Throwable caught) {
										}

										public void onSuccess(Object arg0) {
											Info.display("Save Complete", "Successfully saved assessment {0}.",
													AssessmentCache.impl.getCurrentAssessment().getSpeciesName());

											doMoveNext();
										};
									});
								} catch (InsufficientRightsException e) {
									WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
											+ "permission to modify this assessment. The changes you "
											+ "just made will not be saved.");
								}
							} else
								doMoveNext();
						}
					});
				} else
					doMoveNext();
			}
		});

		currentTaxa = new Button();
		currentTaxa.setText("Quick Taxon Navigation");
		currentTaxa.setToolTip(new ToolTipConfig("Quick Taxon Navigation",
				"These arrows will allow you to navigate,<br>" + "in order, through the taxa in your current<br>"
				+ "working set, selecting the global draft assessment<br>" + "by default, should one exist."));
		currentTaxa.addStyleName("bold");

		prevTaxa = new Button();
		prevTaxa.setIconStyle("icon-arrow-left");
		prevTaxa.setToolTip("Previous Taxon");
		prevTaxa.addListener(Events.Select, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if (AssessmentCache.impl.getCurrentAssessment() != null
						&& AssessmentUtilFactory.getSaveUtils().shouldSaveCurrentAssessment()) {
					WindowUtils.confirmAlert("By the way...", "Navigating away from this page will"
							+ " revert unsaved changes. Would you like to save?", new Listener<MessageBoxEvent>() {
						public void handleEvent(MessageBoxEvent be) {
							{
								if (be.getButtonClicked().getText().equalsIgnoreCase("cancel")) {

								} else if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
									try {
										AssessmentUtilFactory.getSaveUtils().saveAssessment(AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
											public void onFailure(Throwable caught) {
											}

											public void onSuccess(Object arg0) {
												Info.display("Save Complete", "Successfully saved assessment {0}.",
														AssessmentCache.impl.getCurrentAssessment().getSpeciesName());

												doMovePrev();
											};
										});
									} catch (InsufficientRightsException e) {
										WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
												+ "permission to modify this assessment. The changes you "
												+ "just made will not be saved.");
									}
								} else
									doMovePrev();
							}
						}
					});

				} else
					doMovePrev();
			}
		});

		bar.add(new SeparatorToolItem());

		Button blah = new Button();
		blah.setText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		blah.setEnabled(false);
		bar.add(blah);

		bar.add(prevTaxa);
		bar.add(currentTaxa);
		bar.add(nextTaxa);

		navPopup = new Popup();
		navPopup.setLayout(new FitLayout());

		navigator = new LayoutContainer();

		sets = new LayoutContainer();
		taxa = new LayoutContainer();
		assessments = new LayoutContainer();

		goToSet = new Button();
		goToSet.setIconStyle("icon-go-jump");
		goToSet.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (setList.getSelectedItem() != null) {
					WorkingSetData selected = (WorkingSetData) setList.getSelectedItem().getData("workingSet");
					if (selected != null) {
						WorkingSetCache.impl.setCurrentWorkingSet(selected.getId());

						if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
								ClientUIContainer.bodyContainer.tabManager.workingSetPage))
							ClientUIContainer.bodyContainer.fireEvent(Events.SelectionChange);
						else
							ClientUIContainer.bodyContainer
							.setSelection(ClientUIContainer.bodyContainer.tabManager.workingSetPage);

						navPopup.hide();
					}
				}
			}
		});

		goToTaxon = new Button();
		goToTaxon.setIconStyle("icon-go-jump");
		goToTaxon.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (setList.getSelectedItem() != null) {
					WorkingSetData selectedSet = (WorkingSetData) setList.getSelectedItem().getData("workingSet");
					if (selectedSet != null)
						WorkingSetCache.impl.setCurrentWorkingSet(selectedSet.getId());
				}

				TaxonNode selected = (TaxonNode) taxonList.getSelectedItem().getData("taxon");

				if (selected == null && taxonListBinder.getSelection().size() > 0)
					selected = taxonListBinder.getSelection().get(0).getNode();

				if (selected != null) {
					TaxonomyCache.impl.setCurrentNode(selected);

					if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
							ClientUIContainer.bodyContainer.tabManager.taxonHomePage))
						ClientUIContainer.bodyContainer.fireEvent(Events.SelectionChange);
					else
						ClientUIContainer.bodyContainer
						.setSelection(ClientUIContainer.bodyContainer.tabManager.taxonHomePage);

					navPopup.hide();
				}
			}
		});

		goToAss = new Button();
		goToAss.setIconStyle("icon-go-jump");
		goToAss.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (setList.getSelectedItem() != null) {
					WorkingSetData selectedSet = (WorkingSetData) setList.getSelectedItem().getData("workingSet");
					if (selectedSet != null)
						WorkingSetCache.impl.setCurrentWorkingSet(selectedSet.getId());
				}

				AssessmentData selected = (AssessmentData) assessmentList.getSelectedItem().getData("assessment");
				if (selected != null) {
					AssessmentCache.impl.setCurrentAssessment(selected);

					if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
							ClientUIContainer.bodyContainer.tabManager.assessmentEditor))
						ClientUIContainer.bodyContainer.fireEvent(Events.SelectionChange);
					else
						ClientUIContainer.bodyContainer
						.setSelection(ClientUIContainer.bodyContainer.tabManager.assessmentEditor);

					navPopup.hide();
				}
			}
		});

		buildLists();

		sets.setHeight(LIST_SIZE);
		taxa.setHeight(LIST_SIZE);
		assessments.setHeight(LIST_SIZE);

		BorderLayout navLayout = new BorderLayout();
		// navLayout.setMargin(0);
		// navLayout.setSpacing(2);
		navigator.setLayout(navLayout);
		navigator.setStyleName("navigator");

		BorderLayoutData left = new BorderLayoutData(LayoutRegion.WEST, .30f, 5, 4000);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER, .37f, 5, 4000);
		BorderLayoutData right = new BorderLayoutData(LayoutRegion.EAST, .33f, 5, 4000);

		navigator.add(sets, left);
		navigator.add(taxa, center);
		navigator.add(assessments, right);

		navPopup.add(navigator);

		summaryPanel = new HeaderSummaryPanel();
		summaryPanel.setHeight(175);
		add(bar, new RowData(1, 25));
		add(summaryPanel, new RowData(1, .8));

		layout();
	}

	private void buildLists() {
		setList = new DataList() {

			@Override
			protected void onClick(DataListItem item, DataListEvent dle) {
				if (item != null && item.getData("workingSet") != curNavSet)
					setSelectedAction(item);
			}

			protected void onShowContextMenu(int x, int y) {
				if (setList.getSelectedItem() != null && setList.getItems().indexOf(setList.getSelectedItem()) != 0)
					super.onShowContextMenu(x, y);
				else
					super.onHideContextMenu();
			}
		};
		setList.setId("workingset");

		taxonList = new DataList() {

			// @Override
			// protected void onClick(DataListItem item, DataListEvent dle) {
			// if (item != null && item.getData("taxon") != curNavTaxon)
			// taxonSelectedAction(item);
			// }

			@Override
			protected void onRightClick(ComponentEvent ce) {
				if (taxonList.getSelectedItem() != null && taxonList.getSelectedItem().isEnabled())
					super.onRightClick(ce);
			}
		};
		taxonList.setId("taxon");
		taxonPagingLoader = new TaxonPagingLoader();
		taxonListStore = new ListStore<TaxonListElement>(taxonPagingLoader.getPagingLoader());
		taxonListBinder = new DataListBinder<TaxonListElement>(taxonList, taxonListStore);
		taxonListBinder.setDisplayProperty("fullName");
		taxonListBinder.setStyleProvider(new ModelStringProvider<TaxonListElement>() {
			public String getStringValue(TaxonListElement model, String property) {
				String style = "";
				if (model.getNode() != null) {

					style = MarkedCache.impl.getTaxaStyle(model.getNode().getId() + "");
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

		taxonPagingToolBar = new PagingToolBar(40);
		taxonPagingToolBar.bind(taxonPagingLoader.getPagingLoader());

		Button i = new Button();
		i.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				goToTaxon.fireEvent(Events.Select);
			}
		});
		i.setIconStyle("icon-go-jump");
		taxonPagingToolBar.add(i);

		assessmentList = new DataList() {
			protected void onShowContextMenu(int x, int y) {
				if (getSelectedItem() != null && getSelectedItem().isEnabled())
					super.onShowContextMenu(x, y);
				else
					super.onHideContextMenu();
			}
		};
		assessmentList.setId("assessment");

		Menu menu = new Menu();
		MenuItem green = new MenuItem();
		green.addStyleName("green-menu");
		green.setText("Mark Green");
		green.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataList list = setList;
				DataListItem item = list.getSelectedItem();
				if (item == null)
					return;


				if (!MarkedCache.impl.getWorkingSetStyle(item.getId()).equalsIgnoreCase(MarkedCache.GREEN))
					item.removeStyleName(MarkedCache.impl.getWorkingSetStyle(item.getId()));
				MarkedCache.impl.markWorkingSet(item.getId(), MarkedCache.GREEN);
				item.addStyleName(MarkedCache.GREEN);
			}

		});
		MenuItem blue = new MenuItem();
		blue.addStyleName("blue-menu");
		blue.setText("Mark Blue");
		blue.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataList list = setList;
				DataListItem item = list.getSelectedItem();

				if (item == null)
					return;

				if (!MarkedCache.impl.getWorkingSetStyle(item.getId()).equalsIgnoreCase(MarkedCache.BLUE))
					item.removeStyleName(MarkedCache.impl.getWorkingSetStyle(item.getId()));
				MarkedCache.impl.markWorkingSet(item.getId(), MarkedCache.BLUE);
				item.addStyleName(MarkedCache.BLUE);

			}

		});
		MenuItem red = new MenuItem();
		red.addStyleName("red-menu");
		red.setText("Mark Red");
		red.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataList list = setList;
				DataListItem item = list.getSelectedItem();

				if (item == null)
					return;

				if (!MarkedCache.impl.getWorkingSetStyle(item.getId()).equalsIgnoreCase(MarkedCache.RED))
					item.removeStyleName(MarkedCache.impl.getWorkingSetStyle(item.getId()));
				MarkedCache.impl.markWorkingSet(item.getId(), MarkedCache.RED);
				item.addStyleName(MarkedCache.RED);

			}

		});
		MenuItem unmark = new MenuItem();
		unmark.addStyleName(MarkedCache.NONE);
		unmark.setText("Unmark");
		unmark.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataList list = setList;
				DataListItem item = list.getSelectedItem();

				if (item == null)
					return;

				if (!MarkedCache.impl.getWorkingSetStyle(item.getId()).equalsIgnoreCase(MarkedCache.NONE)) {
					item.removeStyleName(MarkedCache.impl.getWorkingSetStyle(item.getId()));
				}
				MarkedCache.impl.markWorkingSet(item.getId(), "regular-menu");

			}

		});
		menu.add(red);
		menu.add(blue);
		menu.add(green);
		menu.add(unmark);

		setList.setContextMenu(menu);

		menu = new Menu();
		green = new MenuItem();
		green.addStyleName("green-menu");
		green.setText("Mark Green");
		green.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataListItem item = taxonList.getSelectedItem();
				String itemID =""+ taxonListBinder.getSelection().get(0).getNode().getId();

				if (!MarkedCache.impl.getTaxaStyle(itemID).equalsIgnoreCase(MarkedCache.GREEN))
					item.removeStyleName(MarkedCache.impl.getTaxaStyle(itemID));
				MarkedCache.impl.markTaxa(itemID, MarkedCache.GREEN);
				item.addStyleName(MarkedCache.GREEN);

			}

		});
		blue = new MenuItem();
		blue.addStyleName("blue-menu");
		blue.setText("Mark Blue");
		blue.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataListItem item = taxonList.getSelectedItem();
				String itemID =""+ taxonListBinder.getSelection().get(0).getNode().getId();

				if (!MarkedCache.impl.getTaxaStyle(itemID).equalsIgnoreCase(MarkedCache.BLUE))
					item.removeStyleName(MarkedCache.impl.getTaxaStyle(itemID));
				MarkedCache.impl.markTaxa(itemID, MarkedCache.BLUE);
				item.addStyleName(MarkedCache.BLUE);

			}

		});
		red = new MenuItem();
		red.addStyleName("red-menu");
		red.setText("Mark Red");
		red.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataListItem item = taxonList.getSelectedItem();
				String itemID =""+ taxonListBinder.getSelection().get(0).getNode().getId();

				if (!MarkedCache.impl.getTaxaStyle(itemID).equalsIgnoreCase(MarkedCache.RED))
					item.removeStyleName(MarkedCache.impl.getTaxaStyle(itemID));
				MarkedCache.impl.markTaxa(itemID, MarkedCache.RED);

				item.addStyleName(MarkedCache.RED);

			}

		});
		unmark = new MenuItem();
		unmark.addStyleName(MarkedCache.NONE);
		unmark.setText("Unmark");
		unmark.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataListItem item = taxonList.getSelectedItem();
				String itemID =""+ taxonListBinder.getSelection().get(0).getNode().getId();

				if (!MarkedCache.impl.getTaxaStyle(itemID).equalsIgnoreCase(MarkedCache.NONE))
					item.removeStyleName(MarkedCache.impl.getTaxaStyle(itemID));
				MarkedCache.impl.markTaxa(itemID, MarkedCache.NONE);
				item.addStyleName("regular-menu");
				taxonListStore.update(taxonListBinder.getSelection().get(0));
			}

		});
		menu.add(red);
		menu.add(blue);
		menu.add(green);
		menu.add(unmark);

		taxonList.setContextMenu(menu);

		menu = new Menu();
		green = new MenuItem();
		green.addStyleName("green-menu");
		green.setText("Mark Green");
		green.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataList list = assessmentList;
				DataListItem item = list.getSelectedItem();

				if (item == null)
					return;

				if (!MarkedCache.impl.getAssessmentStyle(item.getId()).equalsIgnoreCase(MarkedCache.GREEN))
					item.removeStyleName(MarkedCache.impl.getAssessmentStyle(item.getId()));
				MarkedCache.impl.markAssement(item.getId(), MarkedCache.GREEN);
				item.addStyleName(MarkedCache.GREEN);

			}

		});
		blue = new MenuItem();
		blue.addStyleName("blue-menu");
		blue.setText("Mark Blue");
		blue.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataList list = assessmentList;
				DataListItem item = list.getSelectedItem();

				if (item == null)
					return;

				if (!MarkedCache.impl.getAssessmentStyle(item.getId()).equalsIgnoreCase(MarkedCache.BLUE))
					item.removeStyleName(MarkedCache.impl.getAssessmentStyle(item.getId()));
				MarkedCache.impl.markAssement(item.getId(), MarkedCache.BLUE);
				item.addStyleName(MarkedCache.BLUE);

			}

		});
		red = new MenuItem();
		red.addStyleName("red-menu");
		red.setText("Mark Red");
		red.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataList list = assessmentList;
				DataListItem item = list.getSelectedItem();

				if (item == null)
					return;

				if (!MarkedCache.impl.getAssessmentStyle(item.getId()).equalsIgnoreCase(MarkedCache.RED))
					item.removeStyleName(MarkedCache.impl.getAssessmentStyle(item.getId()));
				MarkedCache.impl.markAssement(item.getId(), MarkedCache.RED);
				item.setStylePrimaryName(MarkedCache.RED);

			}

		});
		unmark = new MenuItem();
		unmark.addStyleName(MarkedCache.NONE);
		unmark.setText("Unmark");
		unmark.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent ce) {
				DataList list = assessmentList;
				DataListItem item = list.getSelectedItem();

				if (item == null)
					return;

				if (!MarkedCache.impl.getAssessmentStyle(item.getId()).equalsIgnoreCase(MarkedCache.NONE))
					item.removeStyleName(MarkedCache.impl.getAssessmentStyle(item.getId()));
				MarkedCache.impl.markAssement(item.getId(), MarkedCache.NONE);
			}

		});
		menu.add(red);
		menu.add(blue);
		menu.add(green);
		menu.add(unmark);

		assessmentList.setContextMenu(menu);
	}

	private void doMoveNext() {
		if (curNavSet == null) {
			Info.display(new InfoConfig("No Working Set", "Please select a working set."));
		} else if (curNavSet.getSpeciesIDs().size() == 0) {
			Info.display(new InfoConfig("Empty Working Set", "Your current working set {0} contains no species.",
					new Params(curNavSet.getWorkingSetName())));
		} else if (curNavTaxon == null)
			TaxonomyCache.impl.fetchNode(curNavSet.getSpeciesIDs().get(0).toString(), true,
					new GenericCallback<TaxonNode>() {
				public void onFailure(Throwable caught) {
				}

				public void onSuccess(TaxonNode result) {
					AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, curNavSet.getSpeciesIDs().get(0)
							.toString()), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
						};

						public void onSuccess(String result) {
							if (AssessmentCache.impl.getDraftAssessment(curNavSet.getSpeciesIDs().get(0)
									.toString(), false) != null) {
								AssessmentCache.impl.getDraftAssessment(curNavSet.getSpeciesIDs().get(0)
										.toString(), true);
							} else {
								List<AssessmentData> draftAss = AssessmentCache.impl.getDraftAssessmentsForTaxon(curNavSet
										.getSpeciesIDs().get(0).toString());
								if (draftAss.size() > 0) {
									AssessmentCache.impl.setCurrentAssessment((AssessmentData) draftAss.get(0));
								}
							}
							ClientUIContainer.bodyContainer.refreshBody();
						};
					});
				}
			});
		else {
			String curID = "" + curNavTaxon.getId();
			boolean found = false;
			for (Iterator iter = curNavSet.getSpeciesIDs().listIterator(); iter.hasNext() && !found;) {
				if (iter.next().toString().equalsIgnoreCase(curID)) {
					String newCurrent;

					if (iter.hasNext())
						newCurrent = iter.next().toString();
					else
						newCurrent = curNavSet.getSpeciesIDs().get(0).toString();

					final String fetchMe = newCurrent;
					TaxonomyCache.impl.fetchNode(fetchMe, true, new GenericCallback<TaxonNode>() {
						public void onFailure(Throwable caught) {
						}

						public void onSuccess(TaxonNode result) {
							AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, fetchMe),
									new GenericCallback<String>() {
								public void onFailure(Throwable caught) {
								};

								public void onSuccess(String result) {
									if (AssessmentCache.impl.getDraftAssessment(fetchMe, false) != null) {
										AssessmentCache.impl.getDraftAssessment(fetchMe, true);
									} else {
										List<AssessmentData> draftAss = AssessmentCache.impl.getDraftAssessmentsForTaxon(fetchMe);
										if (draftAss.size() > 0) {
											AssessmentCache.impl.setCurrentAssessment((AssessmentData) draftAss
													.get(0));
										}
									}
									ClientUIContainer.bodyContainer.refreshBody();
								};
							});
						}
					});

					found = true;
				}
			}
		}
	}

	private void doMovePrev() {
		if (curNavSet == null) {
			Info.display(new InfoConfig("No Working Set", "Please select a working set."));
		} else if (curNavSet.getSpeciesIDs().size() == 0) {
			Info.display(new InfoConfig("Empty Working Set", "Your current working set {0} contains no species.",
					new Params(curNavSet.getWorkingSetName())));
		} else if (curNavTaxon == null)
			TaxonomyCache.impl.fetchNode(curNavSet.getSpeciesIDs().get(0).toString(), true,
					new GenericCallback<TaxonNode>() {
				public void onFailure(Throwable caught) {
				}

				public void onSuccess(TaxonNode result) {
					AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, curNavSet.getSpeciesIDs().get(0)
							.toString()), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
						};

						public void onSuccess(String result) {
							if (AssessmentCache.impl.getDraftAssessment(curNavSet.getSpeciesIDs().get(0)
									.toString(), false) != null) {
								AssessmentCache.impl.getDraftAssessment(curNavSet.getSpeciesIDs().get(0)
										.toString(), true);
							} else {
								List<AssessmentData> draftAss = AssessmentCache.impl.getDraftAssessmentsForTaxon(curNavSet
										.getSpeciesIDs().get(0).toString());
								if (draftAss.size() > 0) {
									AssessmentCache.impl.setCurrentAssessment((AssessmentData) draftAss.get(0));
								}
							}
							ClientUIContainer.bodyContainer.refreshBody();
						};
					});
				}
			});
		else {
			String curID = "" + curNavTaxon.getId();

			// Initialize to the end of the list, in case the first item is
			// current
			String newCurrent = curNavSet.getSpeciesIDs().get(curNavSet.getSpeciesIDs().size() - 1).toString();

			for (Iterator iter = curNavSet.getSpeciesIDs().listIterator(); iter.hasNext();) {
				String cur = iter.next().toString();

				if (cur.equalsIgnoreCase(curID))
					break;
				else
					newCurrent = cur;
			}

			final String fetchMe = newCurrent;
			TaxonomyCache.impl.fetchNode(fetchMe, true, new GenericCallback<TaxonNode>() {
				public void onFailure(Throwable caught) {
				}

				public void onSuccess(TaxonNode result) {
					AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, fetchMe), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
						};

						public void onSuccess(String result) {
							if (AssessmentCache.impl.getDraftAssessment(fetchMe, false) != null) {
								AssessmentCache.impl.getDraftAssessment(fetchMe, true);
							} else {
								List<AssessmentData> draftAss = AssessmentCache.impl.getDraftAssessmentsForTaxon(fetchMe);
								if (draftAss.size() > 0) {
									AssessmentCache.impl.setCurrentAssessment((AssessmentData) draftAss.get(0));
								}
							}
							ClientUIContainer.bodyContainer.refreshBody();
						};
					});
				}
			});
		}

	}

	private int getHeaderWidth() {
		return getWidth(true);
	}

	protected void onResize(int width, int height) {
		super.onResize(width, height);

		// summaryPanel.setWidth(width + "px");
		// summaryPanel.setHeight((height - 25) + "px");

		// bar.setWidth(width);
		// bar.getItem(0).setWidth(width-20);

		// navPopup.setBounds(getAbsoluteLeft()-30, getAbsoluteTop(), width+60,
		// NAVIGATOR_SIZE);
		// navPopup.setSize(getHeaderWidth() + 60, NAVIGATOR_SIZE);
	}

	/**
	 * This is invoked when you want to refresh assessment list. This is the
	 * only function that will also invoke the postRefreshCallback's methods, as
	 * all refreshX calls are propagated down to this level. Normally, this
	 * callback contains a call to select the "current" working
	 * set/taxon/assessment in the appropriate list.
	 * 
	 * @param postRefreshCallback
	 */
	private void refreshAssessments(final GenericCallback<String> postRefreshCallback) {
		assessmentToSelect = -1;
		assessments.setScrollMode(Scroll.NONE);
		assessments.removeAll();

		if (curNavTaxon == null) {
			assessments.setScrollMode(Scroll.NONE);
			HTML header = new HTML("Please select a taxon.");
			header.addStyleName("bold");
			header.addStyleName("color-dark-blue");
			assessments.add(header);
			assessments.layout();

			if (postRefreshCallback != null)
				postRefreshCallback.onSuccess("OK");

		} else {
			HTML header = new HTML("Building list...");
			header.addStyleName("bold");
			header.addStyleName("color-dark-blue");
			assessments.add(header);

			assessmentList.removeAll();
			assessmentList.setHeight(LIST_SIZE);

			AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, curNavTaxon.getId() + ""), 
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					if (postRefreshCallback != null)
						postRefreshCallback.onFailure(caught);
				}

				public void onSuccess(String arg0) {
					try {
					AssessmentData curAss = null;
					DataListItem curItem = null;

					curAss = AssessmentCache.impl.getUserAssessment("" + curNavTaxon.getId(), false);
					if (curAss != null) {
						curItem = new DataListItem("My Assessment");
						// curItem.setDisabledStyle("bold");
						curItem.setEnabled(false);
						assessmentList.add(curItem);

						String displayable = "";

						if (curAss.getDateModified() != 0)
							displayable = FormattedDate.impl.getDate(new Date(curAss.getDateModified()));
						else
							displayable = curAss.getDateAssessed();

						if (curAss.isRegional())
							displayable += " --- " + RegionCache.impl.getRegionName(curAss.getRegionIDs());
						else
							displayable += " --- " + "Global";

						displayable += " --- " + curAss.getProperCategoryAbbreviation();

						curItem = new DataListItem(displayable);
						curItem.setData("assessment", curAss);
						curItem.setId(curAss.getAssessmentID() + "!" + curAss.getType());
						curItem.addStyleName(MarkedCache.impl.getAssessmentStyle(curItem.getId()));
						assessmentList.add(curItem);

						if (curAss.equals(curNavAssessment))
							assessmentToSelect = assessmentList.getItemCount() - 1;
					}

					List<AssessmentData> draftAssessments = AssessmentCache.impl.getDraftAssessmentsForTaxon("" + curNavTaxon.getId());
					Collections.sort(draftAssessments, new Comparator() {
						public int compare(Object o1, Object o2) {
							String date1 = ((AssessmentData) o1).getDateFinalized();
							String date2 = ((AssessmentData) o2).getDateFinalized();
							return date1.compareTo(date2) * -1;
						}
					});

					if (draftAssessments.size() > 0) {
						curItem = new DataListItem("Draft Assessment(s)");
						// curItem.setDisabledStyle("bold");
						curItem.setEnabled(false);
						assessmentList.add(curItem);
					}
					for (int i = 0; i < draftAssessments.size(); i++) {
						curAss = (AssessmentData) draftAssessments.get(i);

						String displayable;

						displayable = curAss.getDateAssessed();

						if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, curAss) ) {
							if (displayable == null || displayable.equals(""))
								displayable = FormattedDate.impl.getDate(new Date(curAss.getDateModified()));

							if (curAss.isRegional())
								displayable += " --- " + RegionCache.impl.getRegionName(curAss.getRegionIDs());
							else
								displayable += " --- " + "Global";

							displayable += " --- " + curAss.getProperCategoryAbbreviation();
						
							curItem = new DataListItem(displayable);
						} else {
							if (curAss.isRegional())
								curItem = new DataListItem("Regional Draft Assessment");
							else
								curItem = new DataListItem("Global Draft Assessment");
							curItem.setIconStyle("icon-lock");
							
							curItem.setEnabled(false);
						}
						
						curItem.setData("assessment", curAss);
						curItem.setId(curAss.getAssessmentID() + "!" + curAss.getType());
						curItem.addStyleName(MarkedCache.impl.getAssessmentStyle(curItem.getId()));
						assessmentList.add(curItem);

						if (curAss.equals(curNavAssessment))
							assessmentToSelect = assessmentList.getItemCount() - 1;
					}


					if (TaxonomyCache.impl.getNode(curNavTaxon.getId()).getAssessments().size() > 0) {
						curItem = new DataListItem("Published Assessment(s)");
						// curItem.setDisabledStyle("bold");
						curItem.setEnabled(false);
						assessmentList.add(curItem);

						ArrayList pubAssessments = new ArrayList();
						ArrayList assess = TaxonomyCache.impl.getNode(curNavTaxon.getId()).getAssessments();
						for (Iterator iter = assess.listIterator(); iter.hasNext();) {
							pubAssessments
							.add(AssessmentCache.impl.getPublishedAssessment((String) iter.next(), false));
						}

						Collections.sort(pubAssessments, new Comparator() {

							public int compare(Object o1, Object o2) {
								String date1 = ((AssessmentData) o1).getDateFinalized();
								String date2 = ((AssessmentData) o2).getDateFinalized();
								return date1.compareTo(date2) * -1;
							}
						});

						// ArrayList assess=
						// TaxonomyCache.impl.getNode(curNavTaxon
						// .getId()).getAssessments();
						for (Iterator iter = pubAssessments.listIterator(); iter.hasNext();) {
							// curAss =
							// AssessmentCache.impl.getPublishedAssessment(
							// (String)iter.next(), false);
							curAss = (AssessmentData) iter.next();
							if (curAss != null) {
								String displayable;

								displayable = curAss.getDateFinalized();

								if (curAss.isRegional())
									displayable += " --- " + RegionCache.impl.getRegionName(curAss.getRegionIDs());
								else
									displayable += " --- " + "Global";

								displayable += " --- " + curAss.getProperCategoryAbbreviation();

								curItem = new DataListItem(displayable);
								curItem.setData("assessment", curAss);
								curItem.setId(curAss.getAssessmentID() + "!" + curAss.getType());
								SysDebugger.getInstance().println(
										"This is the assessment id " + curAss.getAssessmentID() + " and this is "
										+ "the style "
										+ MarkedCache.impl.getAssessmentStyle(curAss.getAssessmentID() + ""));
								curItem.addStyleName(MarkedCache.impl.getAssessmentStyle(curItem.getId()));
								assessmentList.add(curItem);

								if (curAss.equals(curNavAssessment))
									assessmentToSelect = assessmentList.getItemCount() - 1;
							}
						}

					}

					if (assessmentList.getItemCount() == 0) {
						assessments.removeAll();
						assessments.setScrollMode(Scroll.NONE);
						HTML header = new HTML("No assessments available.");
						header.addStyleName("bold");
						header.addStyleName("color-dark-blue");
						assessments.add(header);
						assessments.layout();
					} else {
						assessments.removeAll();
						HTML assHeader = new HTML("Assessment List");
						assHeader.addStyleName("bold");
						assHeader.addStyleName("color-dark-blue");
						assessments.add(assHeader);
						assessments.add(assessmentList);

						ToolBar assBar = new ToolBar();
						assBar.add(new SeparatorToolItem());
						assBar.add(goToAss);
						assBar.add(new Button("Open Assessment", new SelectionListener<ButtonEvent>() {
							public void componentSelected(ButtonEvent ce) {
								goToAss.fireEvent(Events.Select);
							};
						}));
						assessments.add(assBar);

						// assessments.add(goToAss);
						assessments.layout();
					}

					if (postRefreshCallback != null)
						postRefreshCallback.onSuccess("OK");
					
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private void refreshSets(GenericCallback<String> postRefreshCallback) {
		setToSelect = -1;
		sets.setScrollMode(Scroll.NONE);
		sets.removeAll();

		HTML setsHeader = new HTML("Working Set List");
		setsHeader.addStyleName("bold");
		setsHeader.addStyleName("color-dark-blue");
		sets.add(setsHeader);

		setList.removeAll();
		setList.setHeight(LIST_SIZE);

		DataListItem curItem = new DataListItem("&lt;None&gt;");

		setList.add(curItem);

		for (Iterator iter = WorkingSetCache.impl.getWorkingSets().values().iterator(); iter.hasNext();) {
			WorkingSetData cur = (WorkingSetData) iter.next();
			curItem = new DataListItem(cur.getWorkingSetName());
			curItem.setData("workingSet", cur);
			curItem.setId(cur.getId());
			curItem.addStyleName(MarkedCache.impl.getWorkingSetStyle(cur.getId()));
			setList.add(curItem);

			if (cur.equals(curNavSet))
				setToSelect = setList.getItemCount() - 1;
		}

		sets.add(setList);
		ToolBar assBar = new ToolBar();
		assBar.add(new SeparatorToolItem());
		assBar.add(goToSet);
		assBar.add(new Button("Open Working Set", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				goToSet.fireEvent(Events.Select);
			};
		}));
		sets.add(assBar);
		// sets.add(goToSet);
		sets.layout();

		refreshTaxa(postRefreshCallback);
	}

	private void refreshTaxa(final GenericCallback<String> postRefreshCallback) {
		taxonToSelect = null;
		taxonList.setHeight(LIST_SIZE);
		taxonList.removeAll();

		taxa.setScrollMode(Scroll.NONE);

		DataListItem curItem = null;

		if (curNavSet == null) {
			if (TaxonomyCache.impl.getRecentlyAccessed().size() == 0) {
				taxa.removeAll();
				HTML taxaHeader = new HTML("No Recently Accessed Taxa");
				taxaHeader.addStyleName("bold");
				taxaHeader.addStyleName("color-dark-blue");
				taxa.add(taxaHeader);
				// taxa.layout();
				// navigator.layout();
				navPopup.layout();
			} else {
				for (Iterator iter = TaxonomyCache.impl.getRecentlyAccessed().listIterator(); iter.hasNext();) {
					TaxonNode curNode = (TaxonNode) iter.next();

					curItem = new DataListItem(curNode.getFullName());
					curItem.setData("taxon", curNode);
					curItem.setId(curNode.getId() + "");
					curItem.addStyleName(MarkedCache.impl.getTaxaStyle(curNode.getId() + ""));
					taxonList.add(curItem);

					if (curNode.equals(curNavTaxon))
						taxonToSelect = taxonList.getItem(taxonList.getItemCount() - 1);
				}

				taxa.removeAll();
				HTML taxaHeader = new HTML("Taxon List");
				taxaHeader.addStyleName("bold");
				taxaHeader.addStyleName("color-dark-blue");
				taxa.add(taxaHeader);
				taxa.add(taxonList);
				ToolBar assBar = new ToolBar();
				assBar.add(new SeparatorToolItem());
				assBar.add(goToTaxon);
				assBar.add(new Button("Open Taxon", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						goToTaxon.fireEvent(Events.Select);
					};
				}));
				taxa.add(assBar);
				navPopup.layout();
			}
		} else {
			WindowUtils.showLoadingAlert("Building list...");
			taxa.removeAll();
			WindowUtils.loadingBox.updateProgress(0, "Building list...");

			if (curNavSet.getSpeciesIDs().size() == 0) {
				taxa.removeAll();
				HTML taxaHeader = new HTML("No taxa in this set.");
				taxaHeader.addStyleName("bold");
				taxaHeader.addStyleName("color-dark-blue");
				taxa.add(taxaHeader);
				// taxa.layout();
				navPopup.layout();
				WindowUtils.hideLoadingAlert();

			} else {
				Timer timer = new Timer() {
					public void run() {
						WorkingSetCache.impl.fetchTaxaForWorkingSet(curNavSet, new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								taxa.removeAll();
								HTML taxaHeader = new HTML("No taxa available.");
								taxaHeader.addStyleName("bold");
								taxaHeader.addStyleName("color-dark-blue");
								taxa.add(taxaHeader);

								WindowUtils.hideLoadingAlert();
								// taxa.layout();
								navPopup.layout();
							}

							public void onSuccess(String arg0) {
								taxonPagingLoader.getFullList().clear();
								taxonPagingLoader.getPagingLoader().setOffset(0);

								String[] taxaList = curNavSet.getSpeciesIDsAsString().split(",");

								String currentFamily = "";

								for (int i = 0; i < taxaList.length; i++) {
									TaxonNode curNode = TaxonomyCache.impl.getNode(taxaList[i]);

									if (!currentFamily.equals(curNode.getFootprint()[TaxonNode.FAMILY])) {
										currentFamily = curNode.getFootprint()[TaxonNode.FAMILY];
										taxonPagingLoader.getFullList().add(new TaxonListElement(currentFamily));
									}

									TaxonListElement curEl = new TaxonListElement(curNode, "");


									taxonPagingLoader.getFullList().add(curEl);

									if (curNode.equals(curNavTaxon))
										taxonToSelect = curEl;

									WindowUtils.loadingBox.updateProgress((double) i / 100.0, "Building list...");
								}

								taxonPagingLoader.getPagingLoader().load();

								taxa.removeAll();
								HTML taxaHeader = new HTML("Taxon List");
								taxaHeader.addStyleName("bold");
								taxaHeader.addStyleName("color-dark-blue");
								taxa.add(taxaHeader);
								taxa.add(taxonList);
								taxa.add(taxonPagingToolBar);

								WindowUtils.hideLoadingAlert();
								//FIXME: We might need to expose this method to make the header hide properly again
								//navPopup.getPreview().add();
								navPopup.layout();

								if (postRefreshCallback != null)
									postRefreshCallback.onSuccess("OK");
							}
						});
					}
				};
				timer.schedule(150);
			}
		}

		refreshAssessments(postRefreshCallback);
	}

	private void setListSelected() {
		taxonListBinder.removeAllListeners();

		if (setToSelect != -1 && setToSelect < setList.getItemCount() && setList.isRendered())
			setList.getSelectionModel().select(setToSelect, false);
		else
			setList.getSelectionModel().select(0, false);

		if (taxonToSelect != null && taxonList.isRendered()) {
			if (taxonToSelect instanceof DataListItem)
				taxonList.getSelectionModel().select((DataListItem) taxonToSelect, false);
			else if (taxonToSelect instanceof TaxonListElement) {
				int elIndex = taxonPagingLoader.getFullList().indexOf((TaxonListElement) taxonToSelect);
				int activePage = ((elIndex + 1) / taxonPagingToolBar.getPageSize()) + 1;
				taxonPagingToolBar.setActivePage(activePage);

				DataListItem item = (DataListItem) taxonListBinder.findItem((TaxonListElement) taxonToSelect);
				taxonList.scrollIntoView(item);
				taxonList.getSelectionModel().select(item, false);
			}
		} else
			taxonList.getSelectionModel().deselectAll();

		if (assessmentToSelect != -1 && assessmentToSelect < assessmentList.getItemCount()
				&& assessmentList.isRendered())
			assessmentList.getSelectionModel().select(assessmentToSelect, false);
		else
			assessmentList.getSelectionModel().deselectAll();

		taxonListBinder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
				if (se.getSelectedItem() != null && se.getSelectedItem().getNode() != null) {
					curNavTaxon = se.getSelectedItem().getNode();
					curNavAssessment = null;

					refreshAssessments(null);
				}
			}
		});
	}

	private void setSelectedAction(DataListItem item) {
		curNavSet = (WorkingSetData) item.getData("workingSet");
		curNavTaxon = null;
		curNavAssessment = null;

		refreshTaxa(null);
	}

	private void taxonSelectedAction(DataListItem item) {
		curNavTaxon = (TaxonNode) item.getData("taxon");
		curNavAssessment = null;

		refreshAssessments(null);
	}

	public void update() {
		curNavSet = WorkingSetCache.impl.getCurrentWorkingSet();
		curNavTaxon = TaxonomyCache.impl.getCurrentNode();
		curNavAssessment = AssessmentCache.impl.getCurrentAssessment();

		// refreshSets();
		if (curNavSet == null) {
			// nextTaxa.setEnabled(false);
			// prevTaxa.setEnabled(false);
			currentTaxa.setText("Quick Taxon Navigation");
		} else {
			nextTaxa.setEnabled(true);
			prevTaxa.setEnabled(true);
			currentTaxa.setText(curNavTaxon == null ? "Quick Taxon Navigation" : curNavTaxon.getFullName());
		}

		summaryPanel.update();
		ClientUIContainer.bodyContainer.tabManager.update();
	}
}
