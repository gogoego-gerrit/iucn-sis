package org.iucn.sis.client.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.client.api.ui.notes.NoteAPI;
import org.iucn.sis.client.api.ui.notes.NotesWindow;
import org.iucn.sis.client.api.utils.MemoryProxy;
import org.iucn.sis.client.api.utils.SIS;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.assessments.NewAssessmentPanel;
import org.iucn.sis.client.panels.images.ImageManagerPanel;
import org.iucn.sis.client.panels.taxa.TaxonAssessmentInformationTab;
import org.iucn.sis.client.panels.taxa.TaxonHomeGeneralInformationTab;
import org.iucn.sis.client.panels.taxa.TaxonHomeWorkingSetsTab;
import org.iucn.sis.client.panels.taxa.TaxonTreePopup;
import org.iucn.sis.client.panels.taxomatic.CreateNewTaxonPanel;
import org.iucn.sis.client.panels.taxomatic.LateralMove;
import org.iucn.sis.client.panels.taxomatic.MergePanel;
import org.iucn.sis.client.panels.taxomatic.MergeUpInfrarank;
import org.iucn.sis.client.panels.taxomatic.NewCommonNameEditor;
import org.iucn.sis.client.panels.taxomatic.NewTaxonSynonymEditor;
import org.iucn.sis.client.panels.taxomatic.SplitNodePanel;
import org.iucn.sis.client.panels.taxomatic.TaxomaticAssessmentMover;
import org.iucn.sis.client.panels.taxomatic.TaxomaticDemotePanel;
import org.iucn.sis.client.panels.taxomatic.TaxomaticHistoryPanel;
import org.iucn.sis.client.panels.taxomatic.TaxomaticUtils;
import org.iucn.sis.client.panels.taxomatic.TaxomaticWindow;
import org.iucn.sis.client.panels.taxomatic.TaxonBasicEditor;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonHierarchy;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.utils.CommonNameComparator;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.BufferView;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class TaxonHomePageTab extends FeaturedItemContainer<Integer> {
	
	private Button assessmentTools;
	private Button goToParent;
	
	private MemoryProxy<TaxonListElement> proxy;
	private BasePagingLoader<BasePagingLoadResult<TaxonListElement>> loader;
 
	/**
	 * Defaults to having Style.NONE
	 */
	public TaxonHomePageTab() {
		super();
		
		proxy = new MemoryProxy<TaxonListElement>();
		proxy.setSort(false);
		
		loader = new BasePagingLoader<BasePagingLoadResult<TaxonListElement>>(proxy);
		loader.setRemoteSort(false);
	}
	
	@Override
	protected void drawBody(DoneDrawingCallback callback) {
		Taxon taxon = TaxonomyCache.impl.getTaxon(getSelectedItem());
		
		ToolBar toolBar = buildToolBar();
		//if (bodyContainer.getItemCount() == 0) {
			bodyContainer.removeAll();
			
			goToParent.setText("Up to Parent (" + taxon.getParentName() + ")");
			goToParent.setVisible(taxon.getTaxonLevel().getLevel() > TaxonLevel.KINGDOM);
			
			final TaxonHomeGeneralInformationTab generalContent = 
				new TaxonHomeGeneralInformationTab();
			
			final TabItem general = new TabItem();
			general.setLayout(new FillLayout());
			general.setText("General Information");
			general.addListener(Events.Select, new Listener<BaseEvent>() {
				public void handleEvent(BaseEvent be) {
					generalContent.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
			});
			general.add(generalContent);
			
			final TaxonAssessmentInformationTab assessmentContent = 
				new TaxonAssessmentInformationTab();
			
			final TabItem assessment = new TabItem();
			assessment.setLayout(new FillLayout());
			assessment.setText("Assessments");
			assessment.addListener(Events.Select, new Listener<BaseEvent>() {
				public void handleEvent(BaseEvent be) {
					assessmentContent.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
			});
			assessment.add(assessmentContent);
			
			final TaxonHomeWorkingSetsTab workingSetContent = 
				new TaxonHomeWorkingSetsTab();
			
			final TabItem workingSet = new TabItem();
			workingSet.setLayout(new FillLayout());
			workingSet.setText("Working Sets");
			workingSet.addListener(Events.Select, new Listener<BaseEvent>() {
				public void handleEvent(BaseEvent be) {
					workingSetContent.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
				}
			});
			workingSet.add(workingSetContent);
			
			final TabPanel tabPanel = new TabPanel();
			tabPanel.add(general);
			if (TaxonomyCache.impl.getCurrentTaxon().getLevel() >= TaxonLevel.SPECIES) {
				tabPanel.add(assessment);
				tabPanel.add(workingSet);
			}
			
			BorderLayoutData toolbarData = new BorderLayoutData(LayoutRegion.NORTH);
			toolbarData.setSize(25);
	
			/*
			BorderLayoutData summaryData = new BorderLayoutData(LayoutRegion.CENTER, .5f, 100, 500);
			summaryData.setSize(300);
			BorderLayoutData browserData = new BorderLayoutData(LayoutRegion.EAST, .5f, 100, 500);
			browserData.setSize(250);
	
			final LayoutContainer container = new LayoutContainer(new BorderLayout());
			container.add(taxonomicSummaryPanel, summaryData);
			container.add(toolBar, toolbarData);*/
			
			final LayoutContainer container = new LayoutContainer(new BorderLayout());
			container.add(toolBar, toolbarData);
			container.add(tabPanel, new BorderLayoutData(LayoutRegion.CENTER));
	
			bodyContainer.add(container);
		//}
		
		callback.isDrawn();
	}
	
	@SuppressWarnings("unchecked")
	protected void drawOptions(final DrawsLazily.DoneDrawingCallback callback) {
		if (optionsContainer.getItemCount() == 0) {
			final ContentPanel children = new ContentPanel();
			children.setLayout(new FillLayout());
			
			final ListStore<TaxonListElement> store = new ListStore<TaxonListElement>(loader);
			
			final ColumnConfig col = new ColumnConfig("name", "Name", 100);
			col.setRenderer(new GridCellRenderer<TaxonListElement>() {
				public Object render(TaxonListElement model, String property, ColumnData config, int rowIndex, int colIndex,
						ListStore<TaxonListElement> store, Grid<TaxonListElement> grid) {
					return model.toHtml((String)model.get(property));
				}
			});
			final List<ColumnConfig> cols = new ArrayList<ColumnConfig>();
			cols.add(col);
			
			final BufferView view = new BufferView();
			view.setForceFit(true);
			view.setRowHeight(20);
			
			final Grid<TaxonListElement> grid = new Grid<TaxonListElement>(store, new ColumnModel(cols));
			grid.setBorders(false);
			grid.setHideHeaders(true);
			grid.setLoadMask(true);
			grid.setView(view);
			grid.setAutoExpandColumn("name");
			grid.addListener(Events.RowClick, new Listener<GridEvent<TaxonListElement>>() {
				public void handleEvent(GridEvent<TaxonListElement> be) {
					TaxonListElement model = be.getGrid().getStore().getAt(be.getRowIndex());
					if (model != null)
						updateSelection(model.getNode().getId());
				}
			});
			
			children.add(grid);
			
			optionsContainer.add(children);
		}
		
		final ContentPanel children = (ContentPanel)optionsContainer.getItem(0);
		
		final Taxon taxon = TaxonomyCache.impl.getTaxon(getSelectedItem());
		if (Taxon.getDisplayableLevelCount() > taxon.getLevel() + 1) {
			final Grid<TaxonListElement> grid = (Grid)children.getItem(0);
			grid.mask("Loading...");
			grid.getView().setEmptyText("No " + Taxon.getDisplayableLevel(taxon.getLevel() + 1) + ".");

			TaxonomyCache.impl.fetchChildren(taxon, new GenericCallback<List<TaxonListElement>>() {
				public void onFailure(Throwable caught) {
					final ListStore<TaxonListElement> store = new ListStore<TaxonListElement>();
					store.setStoreSorter(new StoreSorter<TaxonListElement>(new PortableAlphanumericComparator()));
					
					proxy.setStore(store);
					
					loader.load(0, store.getCount());
					
					children.setHeading(Taxon.getDisplayableLevel(taxon.getLevel() + 1));
					grid.unmask();
					callback.isDrawn();
				}
				public void onSuccess(List<TaxonListElement> result) {
					final ListStore<TaxonListElement> store = new ListStore<TaxonListElement>();
					store.setStoreSorter(new StoreSorter<TaxonListElement>(new PortableAlphanumericComparator()));
					store.add(result);
					store.sort("name", SortDir.ASC);
					
					proxy.setStore(store);
					
					loader.load(0, store.getCount());
		
					children.setHeading(Taxon.getDisplayableLevel(taxon.getLevel() + 1));
					grid.unmask();
					callback.isDrawn();
				}
			});
		} else {
			children.setHeading("Not available.");
			
			final ListStore<TaxonListElement> store = new ListStore<TaxonListElement>();
			store.setStoreSorter(new StoreSorter<TaxonListElement>(new PortableAlphanumericComparator()));
			
			proxy.setStore(store);
			
			loader.load(0, store.getCount());
			
			callback.isDrawn();
		}
	}
	
	@Override
	protected LayoutContainer updateFeature() {
		final Taxon node = getTaxon();
		
		final Image taxonImage = new Image(UriBase.getInstance().getImageBase() + "/images/view/thumb/" + node.getId() + "/primary?size=100&unique=" + new Date().getTime());
		/*taxonImage.setWidth("100px");
		taxonImage.setHeight("100px");*/
		taxonImage.setStyleName("SIS_taxonSummaryHeader_image");
		taxonImage.setTitle("Click for Image Viewer");
		taxonImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ImageManagerPanel manager = new ImageManagerPanel(node);
				manager.update(new DrawsLazily.DoneDrawingCallback() {
					public void isDrawn() {
						Window window = WindowUtils.newWindow("Manage Images");
						window.add(manager);
						window.setWidth(600);
						window.setHeight(300);
						window.show();
					}
				});
			}
		});
		
		final HorizontalPanel taxonImageWrapper = new HorizontalPanel();
		taxonImageWrapper.setWidth("100%");
		taxonImageWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		taxonImageWrapper.add(taxonImage);
		
		LayoutContainer vp = new LayoutContainer(new FlowLayout());
		vp.add(createSpacer(10));
		vp.add(taxonImageWrapper);
		
		/*final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.get(UriBase.getInstance().getImageBase() + "/images/" + getSelectedItem().getId(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				Debug.println("Taxon image loader failed to fetch xml");
				taxonImage.setUrl("images/unavailable.png");
			}
			public void onSuccess(String result) {
				NativeNodeList list = doc.getDocumentElement().getElementsByTagName("image");
				boolean found = false;
				for (int i = 0; i < list.getLength(); i++) {
					boolean primary = ((NativeElement) list.item(i)).getAttribute("primary").equals("true");
					if (primary) {
						String ext = "";
						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/jpeg"))
							ext = "jpg";
						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/gif"))
							ext = "gif";
						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/png"))
							ext = "png";

						taxonImage.setUrl(UriBase.getInstance().getSISBase() + "/raw/images/bin/"
										+ ((NativeElement) list.item(i)).getAttribute("id") + "." + ext);
						found = true;
						break;
					}
				}
				if (!found) {
					taxonImage.setUrl("images/unavailable.png");
				}		
			}
		});*/
		vp.add(createSpacer(20));
		if (node.getLevel() >= TaxonLevel.SPECIES)
			vp.add(new StyledHTML("<center><i>" + node.getFullName() + "</i></center>", "SIS_taxonSummaryHeader"));
		else
			vp.add(new StyledHTML("<center><i>" + node.getName() + "</i></center>", "SIS_taxonSummaryHeader"));
		
		return vp;
	}
	
	@Override
	protected void updateSelection(final Integer selection) {
		TaxonomyCache.impl.fetchPathWithID(selection, new GenericCallback<TaxonHierarchy>() {
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.errorAlert("Could not load this taxon. Please try again later.");
			}
			public void onSuccess(TaxonHierarchy result) {
				if (getSelectedItem() == selection)
					draw(new DrawsLazily.DoneDrawingCallback() {
						public void isDrawn() {
							layout();
						}
					});
				else
					StateManager.impl.setTaxon(result.getTaxon());
			}
		});
	}
	
	public Taxon getTaxon() {
		return TaxonomyCache.impl.getTaxon(getSelectedItem()); 
	}
	
	private ToolBar buildToolBar() {
		ToolBar toolbar = new ToolBar();
		
		goToParent = new Button("Up to Parent");
		goToParent.setIconStyle("icon-previous");
		goToParent.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				Taxon parent = getTaxon().getParent();
				if (parent == null)
					return;
				
				updateSelection(parent.getId());
			}
		});
		
		toolbar.add(goToParent);

		assessmentTools = new Button();
		assessmentTools.setText("Assessment Tools");
		assessmentTools.setIconStyle("icon-preferences-wrench");

		Button hierarchy = new Button();
		hierarchy.setText("View Hierarchy");
		hierarchy.setIconStyle("icon-hierarchy");
		hierarchy.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				TaxonTreePopup.open(getTaxon());
			}
		});
		
		toolbar.add(hierarchy);
		toolbar.add(new SeparatorToolItem());

		Button assessTaxon = new Button();
		assessTaxon.setText("Assess Taxon");
		assessTaxon.setIconStyle("icon-new-document");
		assessTaxon.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final NewAssessmentPanel panel = new NewAssessmentPanel();
				panel.show();
			}
		});
		assessTaxon.setEnabled(getTaxon().getTaxonLevel().getLevel() >= TaxonLevel.SPECIES);
			
		toolbar.add(assessTaxon);
		
		if (!AuthorizationCache.impl.hasRight(AuthorizableObject.WRITE, getTaxon()))
			return toolbar;
		
		toolbar.add(new SeparatorToolItem());

		Menu mainMenu = new Menu();
		
		// BEGIN TAXOMATIC FEATURES
		if (SIS.isOnline()) {
			boolean canUseTaxomatic = AuthorizationCache.impl.canUse(AuthorizableFeature.TAXOMATIC_FEATURE);
			
			MenuItem mItem;
			
			if (canUseTaxomatic) {
				mItem = new MenuItem();
				mItem.setText("Edit Taxon");
				mItem.setIconStyle("icon-note-edit");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						popupChooser(new TaxonBasicEditor());
					}
				});
				mainMenu.add(mItem);
			}

			mItem = new MenuItem();
			mItem.setText("Edit Synonyms");
			mItem.setIconStyle("icon-note-edit");
			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					popupChooser(new NewTaxonSynonymEditor());
					//popupChooser(new TaxonSynonymEditor());
				}
			});
			mainMenu.add(mItem);

			mItem = new MenuItem();
			mItem.setText("Edit Common Names");
			mItem.setIconStyle("icon-note-edit");
			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					//popupChooser(new TaxonCommonNameEditor());
					final NewCommonNameEditor editor = new NewCommonNameEditor();
					editor.draw(new DrawsLazily.DoneDrawingCallback() {
						public void isDrawn() {
							popupChooser(editor);
						}
					});
					
				}
			});
			mainMenu.add(mItem);
			
			mItem = new MenuItem();
			mItem.setText("Set Primary Common Name");
			mItem.setIconStyle("icon-note-edit");
			{
				final SelectionListener<MenuEvent> listener = new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						final MenuItem item = (MenuItem)ce.getItem();
						final CommonName name = item.getData("model");
						
						TaxonomyCache.impl.setPrimaryCommonName(getTaxon(), name, new GenericCallback<String>() {
							public void onSuccess(String result) {
								Info.display("Success", "Primary common name set to {0}.", name.getName());
								
								ClientUIContainer.bodyContainer.refreshTaxonPage();
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Error saving primary common name, please try again later.");
							}
						});
					}
				};
					
				Menu commonNameMenu = new Menu();
				final ArrayList<CommonName> list = new ArrayList<CommonName>(getTaxon().getCommonNames());
				Collections.sort(list, new CommonNameComparator());
				for (CommonName current : list) {
					MenuItem item = new MenuItem();
					item.addSelectionListener(listener);
					item.setData("model", current);
					if (current.isPrimary())
						item.setText("* " + current.getName());
					else
						item.setText(current.getName());
					
					commonNameMenu.add(item);
				}
				
				mItem.setSubMenu(commonNameMenu);
			}
			
			mainMenu.add(mItem);
			
			if (canUseTaxomatic) {

				// TODO: Decide if need to guard against deprecated nodes
				mItem = new MenuItem();
				mItem.setText("Add New Child Taxon");
				mItem.setIconStyle("icon-new-document");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						Taxon curNode = TaxonomyCache.impl.getCurrentTaxon();
	
						if (curNode != null) {
							if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, curNode)) {
								CreateNewTaxonPanel panel = new CreateNewTaxonPanel(TaxonomyCache.impl.getCurrentTaxon());
								panel.setHeading("Add New Child Taxon");
								panel.show();
							}
							else
								WindowUtils.errorAlert("Insufficient Permission", "Sorry. You do not have create permissions for this taxon.");
						} else {
							WindowUtils.errorAlert("Please select a taxon to attach to.");
						}
					}
				});
				mainMenu.add(mItem);
	
				mItem = new MenuItem();
				mItem.setText("Lateral Move");
				mItem.setIconStyle("icon-lateral-move");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						popupChooser(new LateralMove());
					}
				});
				mainMenu.add(mItem);
	
				mItem = new MenuItem();
				mItem.setText("Promote Taxon");
				mItem.setIconStyle("icon-promote");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						final Taxon currentNode = TaxonomyCache.impl.getCurrentTaxon();
						if (currentNode == null)  //Not possible??
							WindowUtils.errorAlert("Please first select a taxon");
						else if (currentNode.getLevel() != TaxonLevel.INFRARANK)
							WindowUtils.errorAlert("You may only promote infraranks.");
						else {
							String message = "<b>Instructions:</b> By promoting " + currentNode.getFullName() + ", "
								+ currentNode.getFullName() + " will become a species " + " and will have the same parent that "
								+ currentNode.getParentName() + " has.";
							
							WindowUtils.confirmAlert("Confirm", message, new WindowUtils.SimpleMessageBoxListener() {
								public void onYes() {
									TaxomaticUtils.impl.performPromotion(currentNode, new GenericCallback<String>() {
										public void onFailure(Throwable arg0) {
											//Error message handled via default callback
										}
										public void onSuccess(String arg0) {
											WindowUtils.infoAlert("Success", currentNode.getName() + " has successfully been promoted.");
										}
									});
								}
							}, "OK", "Cancel");
						}
					}
				});
				mainMenu.add(mItem);
	
				mItem = new MenuItem();
				mItem.setText("Demote Taxon");
				mItem.setIconStyle("icon-demote");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						Taxon node = TaxonomyCache.impl.getCurrentTaxon();
						if (node == null || node.getLevel() != TaxonLevel.SPECIES)
							WindowUtils.infoAlert("Not allowed", "You can only demote a species.");
						else
							popupChooser(new TaxomaticDemotePanel());
					}
				});
				mainMenu.add(mItem);
	
				mItem = new MenuItem();
				mItem.setIconStyle("icon-merge");
				mItem.setText("Merge Taxa");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						Taxon node = TaxonomyCache.impl.getCurrentTaxon();
						if (node != null)
							popupChooser(new MergePanel());
					}
				});
				mainMenu.add(mItem);
	
				mItem = new MenuItem();
				mItem.setIconStyle("icon-merge-up");
				mItem.setText("Merge Up Subspecies");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
	
					@Override
					public void componentSelected(MenuEvent ce) {
						final Taxon node = TaxonomyCache.impl.getCurrentTaxon();
						if (node == null || node.getLevel() != TaxonLevel.SPECIES) {
							WindowUtils.infoAlert("Not Allowed", "You can only merge subspecies into a species, please "
									+ "visit the species you wish to merge a subspecies into.");
						} else {
							TaxonomyCache.impl.getTaxonChildren(node.getId() + "", new GenericCallback<List<Taxon>>() {
								public void onFailure(Throwable caught) {
									WindowUtils.infoAlert("Error", "There was an internal error while trying to "
											+ "fetch the children of " + node.getFullName());
								}
								public void onSuccess(List<Taxon> list) {
									boolean show = false;
									for (Taxon childNode : list) {
										if (childNode.getLevel() == TaxonLevel.INFRARANK) {
											show = true;
											break;
										}
									}
									if (show) {
										popupChooser(new MergeUpInfrarank());
									} else {
										WindowUtils.infoAlert("Not Allowed", node.getFullName()
												+ " does not have any subspecies to promote.  " 
												+ "You can only merge subspecies with their parent.");
									}
	
								}
							});
						}
	
					}
				});
				mainMenu.add(mItem);
	
				mItem = new MenuItem();
				mItem.setIconStyle("icon-split");
				mItem.setText("Split Taxon");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						// TODO:
						//Taxon node = TaxonomyCache.impl.getCurrentTaxon();
						// if( !node.isDeprecatedStatus() )
						popupChooser(new SplitNodePanel());
						// else
						// WindowUtils.errorAlert("Error",
						// "Taxon selected for merging is not a valid taxon" +
						// " (i.e. status is not A or U).");
	
					}
				});
				mainMenu.add(mItem);
	
				mItem = new MenuItem();
				mItem.setIconStyle("icon-remove");
				mItem.setText("Remove Taxon");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						final Taxon node = getTaxon();
						TaxonomyCache.impl.fetchChildren(node, new GenericCallback<List<TaxonListElement>>() {
							public void onFailure(Throwable caught) {
								String msg = "If this taxon has assessments, these will be moved to the trash as well. Move"
											+ node.generateFullName() + " to the trash?";
								
								WindowUtils.confirmAlert("Confirm Delete", msg, new WindowUtils.SimpleMessageBoxListener() {
									public void onYes() {
										TaxomaticUtils.impl.deleteTaxon(node, new GenericCallback<String>() {
											public void onSuccess(String dresult) {
												TaxonomyCache.impl.evictNodes(node.getParentId() + "," + node.getId());
												updateSelection(node.getParentId());
												
												/*TaxonomyCache.impl.fetchTaxon(node.getParentId(), true,
														new GenericCallback<Taxon>() {
													public void onFailure(Throwable caught) {
														//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(null);
														//FIXME: panelManager.recentAssessmentsPanel.update();
													};
													public void onSuccess(Taxon result) {
														updateSelection(result.getId());
														ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
															.update(TaxonomyCache.impl.getCurrentTaxon().getId());
														//FIXME: panelManager.recentAssessmentsPanel.update();
													};
												});*/
											}
											public void onFailure(Throwable caught) {
												//close();
											}
										});
									}
	
								});
							}
	
							public void onSuccess(List<TaxonListElement> result) {
								WindowUtils.infoAlert("You cannot remove this Taxa without first removing its children.");
								// ((List<ModelData>) result).size();
	
							}
						});
					}
				});
				mainMenu.add(mItem);
	
				/* FIXME: make this work
				mItem = new MenuItem();
				mItem.setIconStyle("icon-undo");
				mItem.setText("Undo Taxomatic Operation");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					@Override
					public void componentSelected(MenuEvent ce) {
						final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
						doc.getAsText(UriBase.getInstance().getSISBase() + "/taxomatic/undo", new GenericCallback<String>() {
	
							public void onFailure(Throwable caught) {
								WindowUtils.infoAlert("Unable to undo last operation", "SIS is unable to undo the last "
										+ "taxomatic operation "
										+ "because you are not the last user to perform a taxomatic "
										+ "change, or there has not been a taxomatic operation to undo.");
	
							}
	
							public void onSuccess(String result) {
								WindowUtils.confirmAlert("Undo Last Taxomatic Operation", doc.getText()
										+ "  Are you sure you want to undo this operation?",
										new WindowUtils.SimpleMessageBoxListener() {
									public void onYes() {
										final NativeDocument postDoc = SimpleSISClient.getHttpBasicNativeDocument();
										postDoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/undo", "", new GenericCallback<String>() {
											public void onFailure(Throwable caught) {
												WindowUtils.errorAlert(
													"Unable to undo the last operation.  " +
													"Please undo the operation manually."
												);
											}
											public void onSuccess(String result) {
												final Taxon currentNode = TaxonomyCache.impl.getCurrentTaxon();
												TaxonomyCache.impl.clear();
												TaxonomyCache.impl.fetchTaxon(currentNode.getId(), true,
														new GenericCallback<Taxon>() {
													public void onFailure(Throwable caught) {
														WindowUtils	.infoAlert("Success",
														"Successfully undid the last taxomatic operation, " +
														"but was unable to refresh the current taxon.");
													}
													public void onSuccess(Taxon result) {
														//panelManager.taxonomicSummaryPanel.update(currentNode.getId());
														ClientUIContainer.bodyContainer.refreshBody();
														WindowUtils.infoAlert("Success",
														"Successfully undid the last taxomatic operation.");
													}
												});
											}
										});
									}
								});
							}
						});
					}
				});
			
				mainMenu.add(mItem);*/
				
				mItem = new MenuItem();
				mItem.setText("View Taxomatic History");
				mItem.setIconStyle("icon-taxomatic-history");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						TaxomaticHistoryPanel panel = new TaxomaticHistoryPanel(getTaxon());
						panel.show();
					}
				});
				mainMenu.add(mItem);
		
				mItem = new MenuItem();
				mItem.setText("Move Assessments");
				mItem.setIconStyle("icon-document-move");
				mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent ce) {
						popupChooser(new TaxomaticAssessmentMover(TaxonomyCache.impl.getCurrentTaxon()));
					}
				});
				
		
				mainMenu.add(mItem);
			}
		}
		
		Button taxomaticToolItem = new Button();
		taxomaticToolItem.setText("Taxomatic Tools");
		taxomaticToolItem.setIconStyle("icon-preferences-wrench-green");
		taxomaticToolItem.setMenu(mainMenu);
		
		toolbar.add(taxomaticToolItem);

		return toolbar;
	}
	
	public void buildNotePopup() {
		final Taxon taxon = getTaxon(); 
		final NotesWindow window = new NotesWindow(new TaxonNoteAPI(taxon));
		window.setHeading("Notes for " + taxon.getFullName());
		window.show();	
	}

	public void buildReferencePopup() {
		final Taxon taxon = getTaxon();
		SimpleSISClient.getInstance().onShowReferenceEditor(
			"Manage References for " + taxon.getFullName(), 
			new ReferenceableTaxon(taxon, new SimpleListener() {
				public void handleEvent() {
					//update(taxon.getId());
					//TaxonomyCache.impl.setCurrentTaxon(getSelectedItem());
					ClientUIContainer.bodyContainer.refreshTaxonPage();
				}
			}), 
			null, null
		);
	}

	private void popupChooser(TaxomaticWindow chooser) {
		chooser.show();
		//chooser.center();
	}
	
	public static class ReferenceableTaxon implements Referenceable {
		
		private final Taxon taxon;
		private final SimpleListener afterChangeListener;
		
		public ReferenceableTaxon(Taxon taxon, SimpleListener afterChangeListener) {
			this.taxon = taxon;
			this.afterChangeListener = afterChangeListener;
		}
		
		public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
			taxon.getReference().addAll(references);
			persist(callback);
		}
		
		public Set<Reference> getReferencesAsList() {
			return new HashSet<Reference>(taxon.getReference());
		}

		public void onReferenceChanged(GenericCallback<Object> callback) {

		}
		
		@Override
		public ReferenceGroup groupBy() {
			return ReferenceGroup.Taxon;
		}

		public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
			taxon.getReference().removeAll(references);
			persist(callback);
		}
		
		private void persist(final GenericCallback<Object> callback) {
			TaxonomyCache.impl.saveReferences(taxon, new GenericCallback<String>() {
				public void onSuccess(String result) {
					if (afterChangeListener != null)
						afterChangeListener.handleEvent();
					
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
		
	}
	
	public static class TaxonNoteAPI implements NoteAPI {
		
		private final Taxon taxon;
		
		public TaxonNoteAPI(Taxon taxon) {
			this.taxon = taxon;
		}
		
		@Override
		public void addNote(final Notes note, final GenericCallback<Object> callback) {
			final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			String url = UriBase.getInstance().getNotesBase() + "/notes/taxon/"+ taxon.getId();
			
			doc.post(url, note.toXML(), new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);							
				};

				public void onSuccess(String result) {
					Notes note = Notes.fromXML(doc.getDocumentElement());
					taxon.getNotes().add(note);
					callback.onSuccess(result);
				};
			});
		}
		
		@Override
		public void deleteNote(final Notes note, final GenericCallback<Object> callback) {
			NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			String url = UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId();

			doc.delete(url, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				};

				public void onSuccess(String result) {
					taxon.getNotes().remove(note);
					callback.onSuccess(result);
				};
			});
		}
		
		@Override
		public void loadNotes(ComplexListener<Collection<Notes>> listener) {
			listener.handleEvent(taxon.getNotes());
		}
		
		@Override
		public void onClose() {
			ClientUIContainer.bodyContainer.refreshBody();
		}
		
	}

}
