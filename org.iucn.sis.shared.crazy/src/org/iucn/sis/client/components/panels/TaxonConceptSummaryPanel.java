package org.iucn.sis.client.components.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.components.panels.TaxonomyBrowserPanel.TaxonListElement;
import org.iucn.sis.client.components.panels.imagemanagement.ImageManagerPanel;
import org.iucn.sis.client.components.panels.imagemanagement.ManagedImage;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.data.assessments.AssessmentFetchRequest;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.taxomatic.CommonNameDisplay;
import org.iucn.sis.client.taxomatic.TaxomaticUtils;
import org.iucn.sis.client.utilities.TaxonPagingLoader;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.Note;
import org.iucn.sis.shared.data.assessments.RegionCache;
import org.iucn.sis.shared.taxonomyTree.CommonNameData;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TableEvent;
import com.extjs.gxt.ui.client.event.TableListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.table.CellRenderer;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.extjs.gxt.ui.client.widget.table.TableSelectionModel;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class TaxonConceptSummaryPanel extends LayoutContainer {
	class TaxonNodeDescriptionPanel extends LayoutContainer {

		DockPanel wrapper;

		private TaxonNodeDescriptionPanel() {
			addStyleName("padded");
		}

		private ContentPanel getAssessmentInformationPanel(final TaxonNode node) {
			final ContentPanel assessmentInformation = new ContentPanel();
			assessmentInformation.setStyleName("x-panel");
			assessmentInformation.setWidth(350);
			assessmentInformation.setHeight(200);
			assessmentInformation.setHeading("Most Recent Published Status");
			assessmentInformation.setLayoutOnChange(true);

			if( node.getAssessments().size() > 0 ) {
				VerticalPanel assessPanel = new VerticalPanel();
				AssessmentData curAssessment = null;
				ArrayList assess = TaxonomyCache.impl.getNode(node.getId()).getAssessments();
				for (Iterator iter = assess.listIterator(); iter.hasNext();) {
					AssessmentData cur = AssessmentCache.impl.getPublishedAssessment((String) iter.next(),
							false);

					if (!cur.isHistorical()) {
						curAssessment = cur;
						break;
					}
				}

				VerticalPanel assessInfoPanel = new VerticalPanel();

				// TEST THIS
				if (curAssessment == null) {
					assessInfoPanel.add(new HTML("This taxon does not have a non-historical assessment."));
				} else {
					assessInfoPanel.setStyleName("SIS_taxonSummaryHeader_assessHeader");

					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessment ID: "
							+ curAssessment.getAssessmentID()));

					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Category: "
							+ curAssessment.getProperCategoryAbbreviation()));
					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Criteria: "
							+ curAssessment.getProperCriteriaString()));
					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessed: " + curAssessment.getDateAssessed()));
					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessor: " + curAssessment.getAssessors()));
					assessPanel.add(assessInfoPanel);

					assessPanel.add(new HTML("&nbsp;&nbsp;Major Threats: " + ""));
					assessPanel.add(new HTML("&nbsp;&nbsp;Population Trend: " + ""));
					assessPanel.add(new HTML("&nbsp;&nbsp;Major Importance Habitats: " + ""));
					assessPanel.add(new HTML("&nbsp;&nbsp;Conservation Actions Needed: " + ""));
					headerAssess.setText(curAssessment.getProperCategoryAbbreviation() + " (v "
							+ curAssessment.getCritVersion() + ") " + curAssessment.getDateAssessed());
				}
				assessmentInformation.add(assessPanel);
			} else
				assessmentInformation.add(new HTML("There is no published data for this taxon."));
			return assessmentInformation;
		}

		private ContentPanel getAssessmentsPanel(final TaxonNode node) {
			final ContentPanel assessments = new ContentPanel();
			assessments.setStyleName("x-panel");

			TableColumn[] columns = new TableColumn[6];

			columns[0] = new TableColumn("Assessment Date", 150);
			columns[1] = new TableColumn("Category", 100);
			columns[2] = new TableColumn("Criteria", 100);
			columns[3] = new TableColumn("Status", 100);
			columns[4] = new TableColumn("Edit/View", 60);
			columns[4].setRenderer(new CellRenderer<Component>() {
				public String render(Component item, String property, Object value) {
					return "<img src =\"images/application_form_edit.png\" class=\"SIS_HyperlinkBehavior\"></img> "
					+ value;
				}
			});
			columns[5] = new TableColumn("Trash", 60);
			columns[5].setRenderer(new CellRenderer<Component>() {
				public String render(Component item, String property, Object value) {
					if (!((String) value).equals("n"))
						return "<img src =\"tango/places/user-trash.png\" class=\"SIS_HyperlinkBehavior\"></img> "
						+ value;

					return "";
				}
			});

			TableColumnModel cm = new TableColumnModel(columns);

			final Table tbl = new Table(cm);
			tbl.setSelectionModel(new TableSelectionModel());
			tbl.setBorders(false);
			assessments.setWidth(com.google.gwt.user.client.Window.getClientWidth() - 500);
			assessments.setHeight(panelHeight);
			tbl.setHeight(panelHeight - 25);
			tbl.setWidth("100%");

			assessments.setHeading("Assessment List");
			assessments.setLayoutOnChange(true);

			tbl.removeAllListeners();
			tbl.addTableListener(new TableListener() {

				public void tableCellClick(TableEvent be) {
					if (be.getItem() == null)
						return;

					final int columnIndex = be.getCellIndex();
					final String id = (String) be.getItem().getValue(6);
					final String status = (String) be.getItem().getValue(3);
					final String type = status.equals("Published") ? AssessmentData.PUBLISHED_ASSESSMENT_STATUS :
						status.startsWith("Draft") ? AssessmentData.DRAFT_ASSESSMENT_STATUS : AssessmentData.USER_ASSESSMENT_STATUS;

					if (columnIndex == 5) {
						if (type == AssessmentData.PUBLISHED_ASSESSMENT_STATUS && 
								!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.DELETE, AssessmentCache.impl.getPublishedAssessment(id, false))) {
							WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission " +
							"to perform this operation.");
						} else if (type == AssessmentData.DRAFT_ASSESSMENT_STATUS && 
								!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.DELETE, AssessmentCache.impl.getDraftAssessment(id, false))) {
							WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission " +
							"to perform this operation.");
						} else {
							WindowUtils.confirmAlert("Confirm Delete", "Are you sure you want to delete this assessment?",
									new WindowUtils.MessageBoxListener() {
								public void onNo() {
								}

								public void onYes() {
									if (AssessmentCache.impl.getCurrentAssessment() != null
											&& AssessmentCache.impl.getCurrentAssessment().getAssessmentID()
											.equals(id))
										AssessmentCache.impl.resetCurrentAssessment();
									NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
									doc.delete("/assessments/" + type + "/" + id, new GenericCallback<String>() {
										public void onFailure(Throwable arg0) {

										}

										public void onSuccess(String arg0) {
											TaxonomyCache.impl.evict(String.valueOf(node.getId()));
											TaxonomyCache.impl.fetchNode(String.valueOf(node.getId()), true,
													new GenericCallback<TaxonNode>() {
												public void onFailure(Throwable caught) {
												};

												public void onSuccess(TaxonNode result) {
													AssessmentCache.impl.clear();
													// AssessmentCache.impl.evictAssessments(String.valueOf(node.getId()),
													// assesType);
													update(String.valueOf(node.getId()));
													panelManager.recentAssessmentsPanel.update();
												};
											});
										}
									});
								}
							});
						}
					} else if (columnIndex == 4) {
						AssessmentData fetched = AssessmentCache.impl.getAssessment(type, id, false);

						// CHANGE
						if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, 
								fetched) ) {
							AssessmentCache.impl.setCurrentAssessment(fetched);
							ClientUIContainer.headerContainer.update();
							ClientUIContainer.bodyContainer.setSelection(
									ClientUIContainer.bodyContainer.tabManager.assessmentEditor);
						} else {
							WindowUtils.errorAlert("Sorry, you do not have permission to view this assessment.");
						}
					}
				}

			});

			for (String pubID : node.getAssessments()) {
				AssessmentData data = AssessmentCache.impl.getPublishedAssessment(pubID, false);

				String date = data.getDateAssessed();
				SysDebugger.getInstance().println("date " + date);

				Object[] values = new Object[7];
				values[0] = data.getDateAssessed() == null ? "(Not set)" : data.getDateAssessed();
				values[1] = data.getProperCategoryAbbreviation();
				values[2] = data.getProperCriteriaString();
				values[3] = "Published";
				values[4] = "";
				values[5] = "";
				values[6] = data.getAssessmentID();

				TableItem item = new TableItem(values);
				tbl.add(item);
			}

			List<AssessmentData> drafts = AssessmentCache.impl.getDraftAssessmentsForTaxon(String.valueOf(node.getId()));
			// final AssessmentData data =
			// AssessmentCache.impl.getDraftGlobalAssessment
			// (String.valueOf(node.getId()), false);
			for (int i = 0; i < drafts.size(); i++) {
				// SysDebugger.getInstance().println((String)drafts.get(i
				// ));
				AssessmentData data = (AssessmentData) drafts.get(i);

				if (data != null) {
					Object[] values = new Object[7];

					if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, data) ) {
						values[0] = data.getDateAssessed() == null ? "(Not set)" : data.getDateAssessed();
						values[1] = data.getProperCategoryAbbreviation();
						values[2] = data.getProperCriteriaString();
						if (data.isRegional())
							values[3] = "Draft - " + RegionCache.impl.getRegionName(data.getRegionIDs());
						else
							values[3] = "Draft";
						values[4] = "";
						values[5] = "";
						values[6] = data.getAssessmentID();
					} else {
						values[0] = "Sorry, you";
						values[1] = "do not have";
						values[2] = "permission";
						values[3] = "to view this";
						values[4] = "draft assessment.";
						values[5] = "";
						values[6] = data.getAssessmentID();
					}

					TableItem item = new TableItem(values);
					tbl.add(item);
				}
			}
			final AssessmentData userdata = AssessmentCache.impl.getUserAssessment(
					String.valueOf(node.getId()), false);
			if (userdata != null) {
				String date = userdata.getDateAssessed();
				SysDebugger.getInstance().println("date " + date);

				Object[] values = new Object[7];
				values[0] = userdata.getDateAssessed() == null ? "(Not set)" : userdata.getDateAssessed();
				values[1] = userdata.getProperCategoryAbbreviation();
				values[2] = userdata.getProperCriteriaString();
				values[3] = "User";
				values[4] = "";
				values[5] = "n";
				values[6] = userdata.getAssessmentID();

				TableItem item = new TableItem(values);

				tbl.add(item);

			}
			tbl.sort(0, SortDir.DESC);
			ClientUIContainer.headerContainer.update();

			assessments.add(tbl);
			return assessments;
		}

		private ContentPanel getChildrenPanel() {
			final ContentPanel children = new ContentPanel();
			children.setLayout(new RowLayout(Orientation.VERTICAL));
			children.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			children.setHeight(200);
			if (TaxonNode.getDisplayableLevelCount() > node.getLevel() + 1) {
				try {
					children.setHeading(TaxonNode.getDisplayableLevel(node.getLevel() + 1));
					// children.setLayoutOnChange(true);
					final TaxonPagingLoader loader = new TaxonPagingLoader();
					final PagingToolBar bar = new PagingToolBar(30);
					bar.bind(loader.getPagingLoader());
					final DataList list = new DataList();
					list.setSize((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2, 148);
					list.setScrollMode(Scroll.AUTOY);
					final ListStore<TaxonListElement> store = new ListStore<TaxonListElement>(loader.getPagingLoader());
					store.setStoreSorter(new StoreSorter<TaxonListElement>(new PortableAlphanumericComparator()));

					final DataListBinder<TaxonListElement> binder = new DataListBinder<TaxonListElement>(list, store);
					binder.setDisplayProperty("name");
					binder.init();

					TaxonTreePopup.fetchChildren(node, new GenericCallback<List<TaxonListElement>>() {

						public void onFailure(Throwable caught) {
							children.add(new HTML("No " + TaxonNode.getDisplayableLevel(node.getLevel() + 1) + "."));
						}

						public void onSuccess(List<TaxonListElement> result) {
							loader.getFullList().addAll(result);
							ArrayUtils.quicksort(loader.getFullList(), new Comparator<TaxonListElement>() {
								public int compare(TaxonListElement o1, TaxonListElement o2) {
									return ((String) o1.get("name")).compareTo((String) o2.get("name"));
								}
							});
							children.add(list);
							children.add(bar);

							loader.getPagingLoader().load(0, loader.getPagingLoader().getLimit());

							children.layout();
						}
					});

					binder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
						
						@Override
						public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
							if (se.getSelectedItem() != null) {
								update(String.valueOf(se.getSelectedItem().getNode().getId()));
							}
						}
					});
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else {
				children.setHeading("Not available.");
			}

			return children;
		}

		private ContentPanel getDistributionMapPanel(final TaxonNode node) {
			ContentPanel cp = new ContentPanel();
			cp.setHeading("Distribution Map");
			cp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			cp.setHeight(200);
			final LayoutContainer vp = new LayoutContainer();
			vp.setLayoutOnChange(true);
			vp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			vp.setHeight(200);
			cp.add(vp);
			vp.setStyleName("SIS_taxonSummaryHeader_mapPanel");

			googleMap = new Image("/raw/browse/spatial/noMapAvailable.jpg");
			NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			doc.get("/raw/browse/spatial/" + node.getId() + ".jpg", new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					Image map = new Image("images/noMapAvailable.jpg");
					map.setSize((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2 + "", "175");
					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.setMap(map);
					vp.add(googleMap);

				}

				public void onSuccess(String result) {
					Image map = new Image("/raw/browse/spatial/" + node.getId() + ".jpg");
					map.addClickListener(new ClickListener() {
						public void onClick(Widget w) {
							SysDebugger.getInstance().println("on click");
							Window s = WindowUtils.getWindow(false, false, "Map Distribution Viewer");
							LayoutContainer content = s;
							content
							.add(ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel);
							if (!ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel
									.isRendered())
								ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel
								.update(new ManagedImage(googleMap, ManagedImage.IMG_JPEG));
							s.setHeight(600);
							s.setWidth(800);
							s.show();
							s.center();

						}
					});

					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.setMap(map);
					vp.add(googleMap);
				}
			});

			return cp;
		}

		private ContentPanel getGeneralInformationPanel(final TaxonNode node) {
			final ContentPanel generalInformation = new ContentPanel();
			generalInformation.setStyleName("x-panel");
			panelHeight = 180;
			generalInformation.setWidth(350);

			generalInformation.setHeading("General Information");
			BorderLayout generalLayout = new BorderLayout();
			// generalLayout.setSpacing(5);
			generalInformation.setLayout(generalLayout);
			generalInformation.setLayoutOnChange(true);
			final BorderLayoutData image = new BorderLayoutData(LayoutRegion.WEST, 100);

			final BorderLayoutData info = new BorderLayoutData(LayoutRegion.CENTER);

			final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			doc.get("/images/" + node.getId(), new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					SysDebugger.getInstance().println("failed to fetch xml");
				}

				public void onSuccess(String result) {
					taxonImage = null;
					NativeNodeList list = doc.getDocumentElement().getElementsByTagName("image");
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

							ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
							.setImage(new Image("/raw/images/bin/"
									+ ((NativeElement) list.item(i)).getAttribute("id") + "." + ext));
						}
					}
					if (taxonImage == null) {
						SysDebugger.getInstance().println("null image set unavaiolable");
						taxonImage = new Image("images/unavailable.png");
						ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
						.setImage(taxonImage);
					}

					VerticalPanel vp = new VerticalPanel();
					vp.setSize("100px", "100px");

					taxonImage.setWidth("100px");
					taxonImage.setHeight("100px");
					taxonImage.setStyleName("SIS_taxonSummaryHeader_image");
					taxonImage.setTitle("Click for Image Viewer");
					vp.add(taxonImage);
					generalInformation.add(vp, image);

				}
			});

			// ADD GENERAL INFO
			LayoutContainer data = new LayoutContainer();
			data.setWidth(240);
			if (!node.isDeprecated())
				data.add(new HTML("Name: <i>" + node.getName() + "</i>"));
			else
				data.add(new HTML("Name: <s>" + node.getName() + "</s>"));

			data.add(new HTML("&nbsp;&nbsp;Taxon ID: "
					+ "<a target='_blank' href='http://www.iucnredlist.org/search/details.php/" + node.getId()
					+ "/summ'>" + node.getId() + "</a>"));

			if (node.getLevel() >= TaxonNode.SPECIES) {
				panelHeight += 10;
				data.add(new HTML("Full Name:  <i>" + node.getFullName() + "</i>"));
			}

			data.add(new HTML("Level: " + node.getDisplayableLevel()));
			if (node.getParentName() != null) {
				panelHeight += 10;
				HTML parentHTML = new HTML("Parent:  <i>" + node.getParentName() + "</i>"
						+ "<img src=\"images/icon-tree.png\"></img>");
				parentHTML.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						SysDebugger.getInstance().println("clicking");
						new TaxonTreePopup(node).show();

					}
				});
				data.add(parentHTML);
			}
			if (node.getTaxonomicAuthority() != null && !node.getTaxonomicAuthority().equalsIgnoreCase("")) {
				panelHeight += 20;
				data.add(new HTML("Taxonomic Authority: " + node.getTaxonomicAuthority()));
			}

			data.add(new HTML("Status: " + node.getStatus()));
			data.add(new HTML("Hybrid: " + node.isHybrid()));

			// ADD SYNONYMS
			if (node.getSynonyms().size() != 0) {
				panelHeight += 150;
				data.add(new HTML("<hr><br />"));
				data.add(new HTML("<b>Synonyms</b>"));
				int size = node.getSynonyms().size();
				if (size > 5)
					size = 5;

				for (int i = 0; i < size; i++) {
					final SynonymData curSyn = (SynonymData) node.getSynonyms().get(i);
					HorizontalPanel hp = new HorizontalPanel();

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
						final Image notesImage = new Image("images/icon-note.png");
						if (curSyn.getNotes().equals(""))
							notesImage.setUrl("images/icon-note-grey.png");
						notesImage.setTitle("Add/Remove Notes");
						notesImage.addClickListener(new ClickListener() {
							public void onClick(Widget arg0) {
								final Window s = WindowUtils.getWindow(false, false, "Notes for Synonym "
										+ curSyn.getName());
								final LayoutContainer container = s;
								container.setLayoutOnChange(true);
								FillLayout layout = new FillLayout(Orientation.VERTICAL);
								container.setLayout(layout);

								final TextArea area = new TextArea();
								area.setText(curSyn.getNotes());
								area.setSize("400", "75");
								container.add(area);
								HorizontalPanel buttonPanel = new HorizontalPanel();

								final Button cancel = new Button();
								cancel.setText("Cancel");
								cancel.addListener(Events.Select, new Listener() {
									public void handleEvent(BaseEvent be) {
										s.hide();

									}
								});
								final Button save = new Button();
								save.setText("Save");
								save.addListener(Events.Select, new Listener() {
									public void handleEvent(BaseEvent be) {
										curSyn.setNotes(area.getText());
										if (!curSyn.getNotes().equals(""))
											notesImage.setUrl("images/icon-note.png");
										else
											notesImage.setUrl("images/icon-note-grey.png");
										s.hide();
										TaxomaticUtils.impl.writeNodeToFS(node, new GenericCallback<Object>() {
											public void onFailure(Throwable caught) {

											};

											public void onSuccess(Object result) {

											};
										});
									}
								});
								buttonPanel.add(cancel);
								buttonPanel.add(save);
								container.add(buttonPanel);

								s.setSize(500, 400);
								s.show();
								s.center();

							}
						});
						hp.add(notesImage);
					}

					String value = curSyn.getName() + " - " + curSyn.getAuthority(node.getLevel()) + "";
					if (curSyn.getStatus().equals(SynonymData.ADDED) || curSyn.getStatus().equals(SynonymData.DELETED))
						value += "--" + curSyn.getStatus();

					hp.add(new HTML("&nbsp;&nbsp;" + value));

					if (curSyn.getTaxaID() != null && !curSyn.getTaxaID().equals("")) {
						Image jump = new Image("tango/actions/go-jump.png");
						jump.addStyleName("pointerCursor");
						jump.addClickListener(new ClickListener() {
							public void onClick(Widget sender) {
								TaxonomyCache.impl.fetchNode(curSyn.getTaxaID(), true,
										new GenericCallback<TaxonNode>() {
									public void onFailure(Throwable caught) {

									}

									public void onSuccess(TaxonNode result) {
										ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
										.update(curSyn.getTaxaID());
									}
								});
							}
						});
						hp.add(new HTML("&nbsp;"));
						hp.add(jump);
					}

					data.add(hp);
				}
				if (node.getSynonyms().size() > 5) {
					HTML viewAll = new HTML("View all...");
					viewAll.setStyleName("SIS_HyperlinkLookAlike");
					viewAll.addClickListener(new ClickListener() {
						public void onClick(Widget sender) {
							if (TaxonomyCache.impl.getCurrentNode() == null) {
								Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
								return;
							}

							final Window s = WindowUtils.getWindow(false, false, "Synonyms");
							s.setSize(400, 400);
							LayoutContainer data = s;
							data.setScrollMode(Scroll.AUTO);

							VerticalPanel currentSynPanel = new VerticalPanel();
							currentSynPanel.setSpacing(3);

							HTML curHTML = new HTML("Current Synonyms");
							curHTML.addStyleName("bold");
							currentSynPanel.add(curHTML);

							if (TaxonomyCache.impl.getCurrentNode().getSynonyms().size() == 0)
								currentSynPanel.add(new HTML("There are no synonyms for this taxon."));

							for (Iterator iter = TaxonomyCache.impl.getCurrentNode().getSynonyms().listIterator(); iter
							.hasNext();) {
								SynonymData curSyn = (SynonymData) iter.next();
								curHTML = new HTML(curSyn.getName() + " - " + curSyn.getAuthority(node.getLevel()));
								currentSynPanel.add(curHTML);
							}

							data.add(currentSynPanel);
							s.show();

						}
					});
					data.add(viewAll);
				}
			}

			// ADD COMMON NAMES
			Image addName = new Image("images/add.png");
			addName.setSize("14px", "14px");
			addName.setTitle("Add New Common Name");
			addName.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {

					Window addNameBox = CommonNameDisplay.getNewCommonNameDisplay(node, null,
							new GenericCallback<String>() {
						public void onFailure(Throwable arg0) {
							update(new Long(node.getId()).toString());
						}

						public void onSuccess(String arg0) {
							update(new Long(node.getId()).toString());
						}
					});

					addNameBox.show();
					addNameBox.center();
				}
			});

			HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");

			LayoutContainer commonNamePanel = new LayoutContainer();

			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node))
				commonNamePanel.add(addName);
			commonNamePanel.add(commonNamesHeader);

			data.add(new HTML("<hr><br />"));
			data.add(commonNamePanel);

			if (node.getCommonNames().size() != 0) {

				int loop = 5;
				if (node.getCommonNames().size() < 5)
					loop = node.getCommonNames().size();
				panelHeight += loop * 15 + 20;
				for (int i = 0; i < loop; i++) {
					CommonNameData curName = (CommonNameData) node.getCommonNames().get(i);
					data.add(new CommonNameDisplay(node, curName).show(new GenericCallback<String>() {
						public void onFailure(Throwable arg0) {
							update(new Long(node.getId()).toString());
						}

						public void onSuccess(String arg0) {
							update(new Long(node.getId()).toString());
						}
					}));
				}
				Html viewAll = new Html("View all...");
				viewAll.setStyleName("SIS_HyperlinkLookAlike");
				viewAll.addListener(Events.CellClick, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						final Window s = WindowUtils.getWindow(false, false, "Edit Common Names");
						LayoutContainer data = s;
						data.setScrollMode(Scroll.AUTO);

						ToolBar tBar = new ToolBar();
						Button item = new Button();
						item.setText("New Common Name");
						item.setIconStyle("icon-add");
						item.addSelectionListener(new SelectionListener<ButtonEvent>() {
							public void componentSelected(ButtonEvent ce) {
								s.hide();
								Window addNameBox = CommonNameDisplay.getNewCommonNameDisplay(TaxonomyCache.impl
										.getCurrentNode(), null, new GenericCallback<String>() {
									public void onFailure(Throwable arg0) {
										update(new Long(node.getId()).toString());
									}

									public void onSuccess(String arg0) {
										update(new Long(node.getId()).toString());
									}
								});
								addNameBox.show();
								addNameBox.center();
							}
						});

						tBar.add(item);

						if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node))
							data.add(tBar);

						HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");

						LayoutContainer commonNamePanel = new LayoutContainer();
						commonNamePanel.add(commonNamesHeader);

						data.add(new HTML("<hr><br />"));
						data.add(commonNamePanel);

						if (TaxonomyCache.impl.getCurrentNode().getCommonNames().size() != 0) {
							for (int i = 0; i < TaxonomyCache.impl.getCurrentNode().getCommonNames().size(); i++) {
								CommonNameData curName = (CommonNameData) TaxonomyCache.impl.getCurrentNode()
								.getCommonNames().get(i);
								data.add(new CommonNameDisplay(TaxonomyCache.impl.getCurrentNode(), curName)
								.show(new GenericCallback<String>() {
									public void onFailure(Throwable arg0) {
										update(new Long(node.getId()).toString());
									}

									public void onSuccess(String arg0) {
										update(new Long(node.getId()).toString());
									}
								}));
							}
						} else
							data.add(new HTML("No Common Names."));

						s.setSize(350, 550);
						s.show();
						s.center();

					}
				});
				if (node.getCommonNames().size() > 5)
					data.add(viewAll);
			} else
				data.add(new HTML("No Common Names."));

			generalInformation.setHeight(panelHeight);
			generalInformation.add(data, info);
			return generalInformation;
		}

		private ContentPanel getTaxonomicNotePanel(final TaxonNode node) {
			ContentPanel cp = new ContentPanel();
			cp.setHeading("Taxonomic Notes");
			cp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			cp.setHeight(200);
			final LayoutContainer vp = new LayoutContainer();
			vp.setLayoutOnChange(true);
			vp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			vp.setHeight(200);
			cp.add(vp);
			// vp.setStyleName("SIS_taxonSummaryHeader_mapPanel");

			if( node.getAssessments().size() > 0 ) {
				AssessmentData curAssessment = null;
				if (node.getAssessments().size() == 0) {
					vp.add(new HTML("No Taxonomic Notes Available."));

				} else {
					ArrayList<AssessmentData> pubAssessments = new ArrayList<AssessmentData>();
					for (String pubID : TaxonomyCache.impl.getNode(node.getId()).getAssessments())
						pubAssessments.add(AssessmentCache.impl.getPublishedAssessment(pubID, false));

					Collections.sort(pubAssessments, new Comparator<AssessmentData>() {
						public int compare(AssessmentData o1, AssessmentData o2) {
							String date2 = o2.getDateAssessed();
							String date1 = o1.getDateAssessed();

							if( date2 == null )
								return -1;
							else if( date1 == null )
								return 1;

							int ret = date2.compareTo(date1);

							if( ret == 0 ) {
								if( o2.isHistorical() )
									ret = -1;
								else
									ret = 1;
							}

							return ret;
						}
					});
					curAssessment = AssessmentCache.impl.getPublishedAssessment(
							((AssessmentData) pubAssessments.get(0)).getAssessmentID(), false);
					if (curAssessment.getDataMap().containsKey("TaxonomicNotes")) {
						ArrayList data = (ArrayList) curAssessment.getFieldData("TaxonomicNotes");
						vp.add(new Html(XMLUtils.cleanFromXML(data.get(0).toString().replaceAll("<em>", "<i>").replaceAll("</em>", "</i>"))));

					} else {
						vp.add(new Html("No Taxonomic Notes Available."));
					}

					vp.layout();
				}
			}

			return cp;
		}

		public void resize(int width, int height) {
			onResize(width, height);
		}

		public void updatePanel(final TaxonNode node) {
			removeAll();
			if( node != null ) {
				AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, node.getId()+""), new GenericCallback<String>() {
					public void onSuccess(String result) {
						drawPanel(node);
					}

					public void onFailure(Throwable caught) {};
				});
			} else
				drawPanel(node);
		}

		private void drawPanel(final TaxonNode node) {
			WindowUtils.hideLoadingAlert();
			removeAll();
			setLayoutOnChange(true);
			if (node == null) {
				add(new HTML("No summary available."));
				return;
			}

			RowLayout innerLayout = new RowLayout();
			innerLayout.setOrientation(Orientation.VERTICAL);
			// innerLayout.setSpacing(10);
			wrapper = new DockPanel();
			FillLayout layout = new FillLayout();
			// layout.setSpacing(10);

			String name = "";
			HorizontalPanel hPanel = new HorizontalPanel();

			hPanel.setStyleName("SIS_taxonSummaryHeader_panel");

			Image prevTaxon = new Image("tango/actions/go-previous.png");

			prevTaxon.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					TaxonomyCache.impl.fetchNode(node.getParentId(), true, new GenericCallback<TaxonNode>() {
						public void onFailure(Throwable caught) {
							SysDebugger.getInstance().println("fail" + node.getParentId());
							inner.updatePanel(node);
						}

						public void onSuccess(TaxonNode arg0) {
							SysDebugger.getInstance().println("suc" + node.getParentId());
							inner.updatePanel(arg0);
							ClientUIContainer.headerContainer.update();
							update(node.getParentId());
						}
					});
				}

			});

			if (!node.getParentId().equals("")) {
				hPanel.add(prevTaxon);
				hPanel.setCellWidth(prevTaxon, "30px");
				hPanel.setCellVerticalAlignment(prevTaxon, HasVerticalAlignment.ALIGN_MIDDLE);
			}
			if (node.getLevel() >= TaxonNode.SPECIES)
				name = node.getFullName();
			else
				name = node.getName();

			HTML header = new HTML(" <i>" + name + "</i>");
			headerAssess = new HTML("");

			hPanel.add(header);
			hPanel.add(headerAssess);

			headerAssess.setStyleName("SIS_taxonSummaryHeader");
			header.setStyleName("SIS_taxonSummaryHeader");

			wrapper.add(hPanel, DockPanel.NORTH);

			VerticalPanel westPanel = new VerticalPanel();
			westPanel.add(getGeneralInformationPanel(node));

			if (node.getLevel() >= TaxonNode.SPECIES) {

				westPanel.add(getAssessmentInformationPanel(node));

			} else {
				AssessmentCache.impl.resetCurrentAssessment();
			}

			wrapper.add(westPanel, DockPanel.WEST);

			HorizontalPanel hp = new HorizontalPanel();

			hp.add(getTaxonomicNotePanel(node));

			hp.add(getChildrenPanel());

			VerticalPanel vp = new VerticalPanel();
			vp.clear();
			if (node.getLevel() >= TaxonNode.SPECIES) {

				vp.add(getAssessmentsPanel(node));

			}

			vp.add(hp);
			wrapper.add(vp, DockPanel.CENTER);
			wrapper.setSize("100%", "100%");
			add(wrapper);
		}
	}

	private PanelManager panelManager = null;
	private Image taxonImage;
	private Window imagePopup = null;
	private Image googleMap;
	private TaxonNodeDescriptionPanel inner = null;
	private HTML headerAssess;
	private TaxonNode node;
	private int panelHeight;
	private ImageManagerPanel imageManager;

	private boolean allowDelete;

	public TaxonConceptSummaryPanel(PanelManager manager) {
		this.setScrollMode(Scroll.AUTO);
		addStyleName("gwt-background");
		inner = new TaxonNodeDescriptionPanel();
		setLayoutOnChange(true);
		panelManager = manager;

	}

	public void allowDelete(boolean delete) {
		allowDelete = delete;
		if (node != null)
			update(String.valueOf(node.getId()));
	}

	public void buildNotePopup() {
		final Window s = WindowUtils.getWindow(false, false, "Notes for " + node.getFullName());
		final LayoutContainer container = s;
		container.setLayoutOnChange(true);
		final VerticalPanel panelAdd = new VerticalPanel();
		panelAdd.setSpacing(3);
		panelAdd.add(new HTML("Add Note: "));

		final TextArea area = new TextArea();
		area.setSize("400", "75");
		panelAdd.add(area);

		Button save = new Button("Add Note");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (area.getText().equalsIgnoreCase("")) {
					WindowUtils.errorAlert("Must enter note body.");

				} else {
					Note currentNote = new Note();
					currentNote.setBody(area.getText());
					NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
					String url = "/notes/taxon";
					url += "/" + node.getId();

					doc.post(url, currentNote.toXML(), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
						};

						public void onSuccess(String result) {
						};
					});

					s.hide();
				}
			}
		});
		Button close = new Button("Close");
		close.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				s.hide();
			}
		});

		panelAdd.add(save);
		panelAdd.add(close);

		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		String type = AssessmentCache.impl.getCurrentAssessment().getType();
		String url = "/notes/taxon";
		url += "/" + node.getId();

		doc.get(url, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				container.add(new HTML("<b>There are no notes for this taxon.</b><br>"));
				container.add(panelAdd);
				s.setSize(500, 400);
				s.show();
				s.center();
			}

			public void onSuccess(String result) {
				final ArrayList notes = Note.notesFromXML(doc.getDocumentElement());
				if (notes == null || notes.size() == 0) {
					container
					.add(new HTML(
					"<div style='padding-top:10px';background-color:grey><b>There are no notes for this field.</b></div>"));
					container.add(panelAdd);
				} else {

					ContentPanel eBar = new ContentPanel();
					eBar.setHeight(200);

					RowLayout layout = new RowLayout(Orientation.VERTICAL);
					eBar.setLayout(layout);
					eBar.setLayoutOnChange(true);

					for (Iterator iter = notes.listIterator(); iter.hasNext();) {

						final Note current = (Note) iter.next();
						Image deleteNote = new Image("images/icon-note-delete.png");
						deleteNote.setTitle("Delete Note");
						deleteNote.addClickListener(new ClickListener() {
							public void onClick(Widget sender) {
								NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
								String url = "/notes/taxon";
								url += "/" + node.getId();

								doc.post(url + "?option=remove", current.toXML(), new GenericCallback<String>() {
									public void onFailure(Throwable caught) {
									};

									public void onSuccess(String result) {
									};
								});

							}
						});

						LayoutContainer a = new LayoutContainer();
						RowLayout innerLayout = new RowLayout(Orientation.HORIZONTAL);
						// innerLayout.setSpacing(10);
						a.setLayout(innerLayout);
						a.setLayoutOnChange(true);
						a.setWidth(400);
						a.add(deleteNote, new RowData());
						a.add(new HTML("<b>" + current.getUser() + " [" + current.getDate() + "]</b>  --"
								+ current.getBody()), new RowData(1d, 1d));// );

						eBar.add(a, new RowData(1d, 1d));
					}
					container.add(eBar);
					container.add(panelAdd);
				}

				s.setSize(500, 400);
				s.show();
				s.center();

			}

		});

	}

	public void buildReferencePopup() {
		final Window s = WindowUtils.getWindow(false, false, "Add a references to " + node.getFullName());
		s.setIconStyle("icon-book");
		LayoutContainer container = s;
		container.setLayout(new FillLayout());

		ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(node);

		container.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);

		s.setSize(850, 550);
		s.show();
		s.center();
	}

	public void onResize(int width, int height) {
		super.onResize(width, height);
		inner.resize(width, height);
	}

	public void setImage(Image image) {
		SysDebugger.getInstance().println("setting image");
		taxonImage = image;
		taxonImage.addClickListener(new ClickListener() {
			public void onClick(Widget w) {
				if (imagePopup == null) {
					imageManager = new ImageManagerPanel(String.valueOf(node.getId()));

					imagePopup = WindowUtils.getWindow(false, false, "Photo Station");
					imagePopup.setLayout(new FitLayout());
					//					imagePopup.setAutoHide(true);
					//					imagePopup.setShim(true);
					imagePopup.add(imageManager);
				}

				imageManager.setTaxonId(String.valueOf(node.getId()));
				imageManager.update();
				imagePopup.setScrollMode(Scroll.AUTO);
				imagePopup.show();
				imagePopup.setSize(600, 330);
				imagePopup.setPagePosition(w.getAbsoluteLeft(), w.getAbsoluteTop());
			}
		});
	}

	public void setMap(Image image) {
		googleMap = image;
	}

	public void update(final String nodeID) {
		WindowUtils.showLoadingAlert("Loading...");

		Timer timer = new Timer() {
			public void run() {
				updateDelayed(nodeID);

			}
		};
		timer.schedule(150);

	}

	private void updateDelayed(final String nodeID) {
		WindowUtils.hideLoadingAlert();
		if (imagePopup != null && imagePopup.isRendered())
			imageManager.setTaxonId(String.valueOf(node.getId()));
		removeAll();

		if (nodeID == null || nodeID.equalsIgnoreCase(""))
			inner.updatePanel(null);
		else {
			boolean resetAsCurrent = TaxonomyCache.impl.getCurrentNode() == null ? true : !nodeID
					.equals(TaxonomyCache.impl.getCurrentNode().getId() + "");
			TaxonomyCache.impl.fetchNode(nodeID, resetAsCurrent, new GenericCallback<TaxonNode>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Taxon ID " + nodeID + " does not exist.");
					inner.updatePanel(null);
					add(inner);
				}

				public void onSuccess(TaxonNode arg0) {
					node = (TaxonNode) arg0;
					if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
							ClientUIContainer.bodyContainer.tabManager.taxonHomePage))
						inner.updatePanel((TaxonNode) arg0);
					if (((TaxonNode) arg0).getLevel() < TaxonNode.SPECIES) {
						ClientUIContainer.headerContainer.update();
					}
					add(inner);
				}
			});
		}

	}
}
