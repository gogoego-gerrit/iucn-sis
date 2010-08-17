package org.iucn.sis.client.panels.header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.extjs.gxt.ui.client.widget.table.TableSelectionModel;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.extjs.client.WindowUtils.MessageBoxListener;

public class TrashBinPanel extends LayoutContainer {

	private LayoutContainer centerPanel;
	private LayoutContainer westPanel;

	private BorderLayoutData centerData;
	private BorderLayoutData westData;

	private ContentPanel status;
	private Table trashTable;
	private DataList folders;

	private int total = 0;
	private HashMap<String, Integer> folderCount;

	public TrashBinPanel() {
		setLayout(new BorderLayout());
		setLayoutOnChange(true);
		folderCount = new HashMap<String, Integer>();
		build();
	}

	public void build() {

		centerPanel = new LayoutContainer();
		westPanel = new LayoutContainer();
		centerData = new BorderLayoutData(LayoutRegion.CENTER, .75f);
		westData = new BorderLayoutData(LayoutRegion.WEST, .25f);

		trashTable = new Table();
		trashTable.setWidth(585);
		trashTable.setHeight(475);

		folders = new DataList();
		folders.addStyleName("gwt-background");
		folders.setBorders(true);

		status = new ContentPanel();
		status.setLayoutOnChange(true);

		buildWestPanel();
		buildCenterPanel();

		fillTrash();

		add(centerPanel, centerData);
		add(westPanel, westData);
	}

	private void buildCenterPanel() {

		TableColumn[] columns = new TableColumn[6];

		columns[0] = new TableColumn("Date Removed", .30f);
		columns[0].setMaxWidth(210);
		columns[0].setMaxWidth(250);
		columns[0].setAlignment(HorizontalAlignment.LEFT);

		columns[1] = new TableColumn("Type", .15f);
		columns[1].setMinWidth(50);
		columns[1].setMaxWidth(100);

		columns[2] = new TableColumn("ID", .15f);
		columns[2].setMinWidth(50);
		columns[2].setMaxWidth(100);

		columns[3] = new TableColumn("Taxon", .15f);
		columns[3].setMaxWidth(200);
		columns[3].setMinWidth(75);
		columns[3].setAlignment(HorizontalAlignment.LEFT);

		columns[4] = new TableColumn("Status", .1f);
		columns[4].setMaxWidth(50);
		columns[4].setMaxWidth(100);

		columns[5] = new TableColumn("Removed By", .15f);
		columns[5].setMaxWidth(150);
		columns[5].setMaxWidth(150);
		columns[5].setAlignment(HorizontalAlignment.LEFT);

		TableColumnModel cm = new TableColumnModel(columns);
		trashTable.setColumnModel(cm);
		trashTable.setSelectionModel(new TableSelectionModel(SelectionMode.SINGLE));

		centerPanel.add(buildToolBar());
		centerPanel.add(trashTable);

	}

	private ToolBar buildToolBar() {
		ToolBar bar = new ToolBar();

		Button tItem = new Button();
		tItem.setText("Restore");
		tItem.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				final TrashedObject trashed = ((TrashedObject) trashTable.getSelectionModel().getSelectedItem());
				String id = trashed.getID();
				String type = trashed.getType();
				
				// **************************
				// check is assessments exist to restore with taxa
				boolean recurse = false;
				if (type.equals("TAXON")) {
					Iterator<TableItem> iter = trashTable.iterator();
					while (iter.hasNext()) {
						TrashedObject obj = (TrashedObject) iter.next();
						if (obj.getNodeID().equals(id) && obj.getType().equals("ASSESSMENT"))
							recurse = true;
					}
					if (recurse == true) {
						WindowUtils.confirmAlert("Restore Assessments",
								"This taxa has related assessments in the trash bin. Do you wish to restore these?",
								new MessageBoxListener() {
									@Override
									public void onNo() {
										restore(false, trashed);

									}

									@Override
									public void onYes() {
										restore(true, trashed);

									}
								});
					}
				} else {
					restore(false, trashed);
				}

				// ***************************

			}
		});
		tItem.setIconStyle("icon-undo");
		bar.add(tItem);

		tItem = new Button();
		tItem.setText("Delete");
		tItem.setIconStyle("icon-remove");
		tItem.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				final TrashedObject trashed = ((TrashedObject) trashTable.getSelectionModel().getSelectedItem());
				trashed.delete(new GenericCallback<String>() {
					public void onFailure(Throwable arg0) {
					};

					public void onSuccess(String arg0) {
						trashTable.remove(trashed);
						refresh();
					}
				});
			}
		});
		bar.add(tItem);

		tItem = new Button();
		tItem.setText("Empty Trash");
		tItem.setIconStyle("icon-bomb");
		tItem.addListener(Events.Select, new Listener() {
			public void handleEvent(BaseEvent be) {
				final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
				doc.post(UriBase.getInstance().getSISBase() +"/trash/deleteall", "", new GenericCallback<String>() {
					public void onFailure(Throwable arg0) {

					}

					public void onSuccess(String arg0) {
						trashTable.removeAll();
						refresh();

					}
				});
			}
		});
		bar.add(tItem);

		return bar;
	}

	private void buildWestPanel() {
		DataListItem all = new DataListItem("All");
		all.setIconStyle("tree-folder");
		folders.add(all);

		DataListItem published = new DataListItem("Published Assessments");
		published.setIconStyle("tree-folder");
		folders.add(published);

		DataListItem draft = new DataListItem("Draft Assessments");
		draft.setIconStyle("tree-folder");
		folders.add(draft);

		DataListItem user = new DataListItem("User Assessments");
		user.setIconStyle("tree-folder");
		folders.add(user);

		DataListItem taxon = new DataListItem("Taxa");
		taxon.setIconStyle("tree-folder");
		folders.add(taxon);

		folders.addListener(Events.SelectionChange, new Listener() {
			public void handleEvent(BaseEvent be) {
				refresh();
			}
		});
		folders.setHeight(300);

		westPanel.add(folders);
		refreshStatus();
		westPanel.add(status);
		// tree.getItem(i).setIconStyle("tree-folder-open");
	}

	private void fillTrash() {

		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();

		doc.get(UriBase.getInstance().getSISBase() + "/trash/list", new GenericCallback<String>() {
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub

			}

			public void onSuccess(String arg0) {
				// TODO Auto-generated method stub
				trashTable.removeAll();
				NativeNodeList list = doc.getDocumentElement().getElementsByTagName("data");
				total = list.getLength();
				folderCount.put("ASSESSMENT", 0);
				folderCount.put("TAXON", 0);

				for (int i = 0; i < list.getLength(); i++) {
					TrashedObject ti = new TrashedObject((NativeElement) list.item(i));
					folderCount.put(ti.getType(), folderCount.get(ti.getType()) + 1);
					if (folders.getSelectedItem() != null
							&& (folders.getSelectedItem().getText().equals("All")
									|| ti.getStatus().toLowerCase().startsWith(
											folders.getSelectedItem().getText().toLowerCase().substring(0, 4)) || (ti
									.getType().equals("TAXON") && folders.getSelectedItem().getText().equals("Taxa"))))
						trashTable.add(ti);
				}
				refreshStatus();

			}
		});
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		folders.getSelectionModel().select(0, false);
	}

	public void refresh() {
		fillTrash();
		refreshStatus();
		// build();

		// layout();
	}

	private void refreshStatus() {
		status.removeAll();
		status.setHeading("Statistics");
		status.setHeight(200);

		status.add(new HTML("Total Items: " + total));
		status.add(new HTML("Total Assesmenets: " + folderCount.get("ASSESSMENT")));
		status.add(new HTML("Total Taxa: " + folderCount.get("TAXON")));
		status.add(new HTML("Current View:" + trashTable.getItemCount()));
	}

	private void restore(boolean recurse, final TrashedObject trashed) {
		trashed.restore(recurse, new GenericCallback<String>() {
			public void onFailure(Throwable arg0) {
				WindowUtils.errorAlert("Unable to restore", "Unable to restore this object." + 
						(trashed.getType().startsWith("draft") ? " Ensure a draft assessment with " +
								"the same regions does not already exist for this taxon." : "" ));
			}

			public void onSuccess(String arg0) {
				trashTable.remove(trashed);
				TaxonomyCache.impl.fetchTaxon(Integer.valueOf(trashed.getNodeID()), false, new GenericCallback<Taxon >() {
					public void onFailure(Throwable arg0) {
						arg0.printStackTrace();
						// TODO Auto-generated method stub

					}

					public void onSuccess(Taxon  arg0) {
						TaxonomyCache.impl.getTaxon(trashed.getNodeID());
						Taxon  node = TaxonomyCache.impl.getTaxon(trashed.getNodeID());

						if (trashed.getStatus().equalsIgnoreCase("published")) {
							// node.addAssessment(trashed.getID());
							// TaxomaticUtils.impl.writeNodeToFS(node, new
							// GenericCallback<String>() {
							// public void onFailure(Throwable arg0) {
							// }
							//
							// public void onSuccess(String arg0) {
							WindowManager.get().hideAll();
							if (TaxonomyCache.impl.getCurrentTaxon() != null)
								ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
										.update(TaxonomyCache.impl.getCurrentTaxon().getId());
							ClientUIContainer.bodyContainer.tabManager.panelManager.recentAssessmentsPanel.refresh();
							// }
							// });
						}
						if (trashed.getStatus().equalsIgnoreCase("draft")
								|| trashed.getStatus().equalsIgnoreCase("draft_regional")) {
							AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest( 
									Integer.valueOf(trashed.getID())),
									new GenericCallback<String>() {
										public void onFailure(Throwable arg0) {
											arg0.printStackTrace();
										};

										public void onSuccess(String result) {
											WindowManager.get().hideAll();
											if (TaxonomyCache.impl.getCurrentTaxon() != null)
												ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
														.update(TaxonomyCache.impl.getCurrentTaxon()
																.getId());
											ClientUIContainer.bodyContainer.tabManager.panelManager.recentAssessmentsPanel
													.refresh();
										};
									});
						} else {
							WindowManager.get().hideAll();

							List<Integer> list = new ArrayList<Integer>();
							list.add(node.getId());
							list.add(node.getParentID());
							TaxonomyCache.impl.evict(node.getParentID() + "," + node.getId());
							TaxonomyCache.impl.fetchList(list, new GenericCallback<String>() {
										public void onFailure(Throwable caught) {
										};

										public void onSuccess(String result) {
											if (TaxonomyCache.impl.getCurrentTaxon() != null) {
												ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
														.update(TaxonomyCache.impl.getCurrentTaxon()
																.getId());
												ClientUIContainer.bodyContainer.tabManager.panelManager.recentAssessmentsPanel
														.refresh();
											}
										};
									});

						}
					}
				});

			}
		});

	}
}
