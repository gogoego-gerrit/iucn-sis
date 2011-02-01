package org.iucn.sis.client.panels.workingsets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
import org.iucn.sis.client.panels.workingsets.WorkingSetTaxaList.TaxaData;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

@SuppressWarnings("deprecation")
public class WorkingSetMoveTaxaPanel extends RefreshLayoutContainer {

	public static final boolean MOVE = false;
	public static final boolean COPY = true;

	private HTML title = null;
	private Button move = null;
	private Button copy = null;
	private DataList list = null;

	public WorkingSetMoveTaxaPanel() {
		super();
		build();
	}

	private Collection<Taxon> addWithHigherIDs(List<TaxaData> checkedTaxa, WorkingSet workingSetToGetTaxaFrom) {
		HashSet<Taxon> taxa = new HashSet<Taxon>();
		Iterator<TaxaData> iter = checkedTaxa.iterator();
		Map<Integer, Taxon> map = workingSetToGetTaxaFrom.getTaxaMap();
		while (iter.hasNext()) {
			TaxaData data = (TaxaData) iter.next();
			for (String id : data.getChildIDS().split(",")) {
				taxa.add(map.get(Integer.valueOf(id)));
			}
			
		}
		return taxa;


	}

	private Collection<Taxon> addWithSpeciesIDS(List<TaxaData> checkedTaxa, WorkingSet workingSetToGetTaxaFrom) {
		HashSet<Taxon> taxa = new HashSet<Taxon>();
		Iterator<TaxaData> iter = checkedTaxa.iterator();
		Map<Integer, Taxon> map = workingSetToGetTaxaFrom.getTaxaMap();
		while (iter.hasNext()) {
			TaxaData data = (TaxaData) iter.next();
			taxa.add(map.get(Integer.valueOf(data.getID())));

		}
		return taxa;

	}

	private void build() {
		BorderLayout layout = new BorderLayout();
		BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, 85f);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
		BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, 35f);
		setLayout(layout);

		buildInstructions(north);
		buildContent(center);
		buildButtons(south);
	}

	private void buildButtons(BorderLayoutData data) {
		ButtonBar buttons = new ButtonBar();
		buttons.setAlignment(HorizontalAlignment.LEFT);
		move = new Button("Move", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				performTaxaOperation(MOVE);
			}
		});
		move.setTitle("Moves the taxa out of the current " + "working set into the selected working set.");

		copy = new Button("Copy", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				performTaxaOperation(COPY);
			}
		});
		copy.setTitle("Copies the selected taxa and places the taxa in " + "the selected working.");

		Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				cancel();
			};
		});

		buttons.add(copy);
		buttons.add(move);
		buttons.add(cancel);
		add(buttons, data);

	}

	private void buildContent(BorderLayoutData data) {
		list = new DataList();
		list.setSelectionMode(SelectionMode.SINGLE);
		list.setCheckable(true);
		list.setBorders(true);
		list.addStyleName("gwt-background");
		list.setScrollMode(Scroll.AUTO);
		add(list, data);
	}

	private void buildInstructions(BorderLayoutData data) {
		title = new HTML();
		add(title, data);
	}

	private void cancel() {
		this.setVisible(false);
		//manager.workingSetOptionsPanel.clearImagePanel();
	}

	private void performTaxaOperation(final boolean mode) {
		if (WorkingSetCache.impl.getCurrentWorkingSet() != null) {
			final WorkingSet workingSet = WorkingSetCache.impl.getCurrentWorkingSet();
			DataListItem[] checkedList = list.getChecked().toArray(new DataListItem[0]);
			move.setEnabled(false);
			if (checkedList.length < 1) {
				WindowUtils.errorAlert("Please select a working set where you would like to "
						+ "move the selected taxa to.");
				move.setEnabled(true);
			} else if (checkedList.length > 1) {
				WindowUtils.errorAlert("Please select only 1 working set.");
				move.setEnabled(true);
			} else {
				List<TaxaData> checkedTaxa = null;//FIXME:  = manager.workingSetOptionsPanel.getChecked();
				if (checkedTaxa != null && !checkedTaxa.isEmpty()) {

					final WorkingSet workingSetToMoveTaxaInto = WorkingSetCache.impl.getWorkingSets().get(Integer.valueOf(checkedList[0].getId()));
					if (mode == MOVE
							&& !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, workingSetToMoveTaxaInto)) {
						WindowUtils.errorAlert("Insufficient Permissions",
								"You cannot modify a public working set you did not create. "
										+ "Check to ensure you created both source and destination working sets.");
					} else if (mode == COPY
							&& !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, workingSetToMoveTaxaInto)) {
						WindowUtils.errorAlert("Insufficient Permissions",
								"You cannot copy to a public working set you did not create.");
					} else {
						
						final Collection<Taxon> taxonToMove;
						if (checkedTaxa.get(0).getType().equalsIgnoreCase(TaxaData.FULLNAME)) {
							taxonToMove = addWithSpeciesIDS(checkedTaxa, workingSet);
						} else {
							taxonToMove = addWithHigherIDs(checkedTaxa, workingSet);
						}
							
						
						WorkingSetCache.impl.editTaxaInWorkingSet(workingSetToMoveTaxaInto, taxonToMove, null, new GenericCallback<String>() {
							
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Taxa failed to transfer to working set "
										+ workingSetToMoveTaxaInto.getWorkingSetName());
								move.setEnabled(true);
								removeChecks();
							}

							public void onSuccess(String arg0) {
								// REMOVE FROM CURRENT WORKING SET
								if (mode == MOVE) {

									WorkingSetCache.impl.editTaxaInWorkingSet(workingSet, null, taxonToMove,  new GenericCallback<String>() {
										public void onFailure(Throwable caught) {
											WindowUtils.errorAlert("Taxa failed to remove from working set "
													+ workingSet.getWorkingSetName() + " but was succesfully copied into " + workingSetToMoveTaxaInto.getName());
											move.setEnabled(true);
											removeChecks();
										};

										public void onSuccess(String arg0) {
											WindowUtils.infoAlert("Success", "Taxa successfully removed from "
													+ workingSet.getWorkingSetName() + " and added to working set "
													+ workingSetToMoveTaxaInto.getWorkingSetName());
											move.setEnabled(true);
											//manager.workingSetOptionsPanel.listChanged();
											removeChecks();
										};
									});
								} else {
									WindowUtils.infoAlert("Taxa successfully added to working set "
											+ workingSetToMoveTaxaInto.getWorkingSetName());
									move.setEnabled(true);
									removeChecks();
								}

							}
						});
					}
				} else {
					WindowUtils.errorAlert("Please select a taxa to move.");
					move.setEnabled(true);
				}
			}
		}
	}

	@Override
	public void refresh() {
		this.setVisible(true);
		refreshMode();
		refreshContent();
	}

	private void refreshContent() {
		list.removeAll();
		if (WorkingSetCache.impl.getCurrentWorkingSet() != null) {
			final Map<Integer, WorkingSet> workingSets = WorkingSetCache.impl.getWorkingSets();
			for( Entry<Integer, WorkingSet> curEntry : workingSets.entrySet() ) {
				WorkingSet ws = curEntry.getValue();
				if (!ws.equals(WorkingSetCache.impl.getCurrentWorkingSet())
						&& AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, ws)) {
					DataListItem item = new DataListItem(ws.getName() + " -- " + ws.getSpeciesIDs().size()
							+ " species");
					item.setId(ws.getId()+"");
					list.add(item);
				}

			}
		}

	}

	private void refreshMode() {
		String titleHTML;

		if (WorkingSetCache.impl.getCurrentWorkingSet() == null) {
			titleHTML = "<b>Instructions:<b> Please select a working set";

		} else {
			titleHTML = "<b>Instructions:</b> Select taxa to be copied or moved from the <b>"
					+ WorkingSetCache.impl.getCurrentWorkingSet().getWorkingSetName()
					+ "</b> working set in the opposite table.  Select the working set where the taxa should be copied into.  "
					+ "If you chose a family (or a genus), all of the taxa in the "
					+ WorkingSetCache.impl.getCurrentWorkingSet().getWorkingSetName()
					+ " working set in the selected family (genus) will be copied to the selected working sets. ";

			if ( !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.GRANT, WorkingSetCache.impl.getCurrentWorkingSet()) )
				move.setEnabled(false);
			else
				move.setEnabled(true);
		}
		title.setHTML(titleHTML);
	}

	private void removeChecks() {
		if (list.getChecked().toArray(new DataListItem[0]).length > 0) {
			DataListItem item = list.getChecked().toArray(new DataListItem[0])[0];
			item.setChecked(false);
			WorkingSet data = WorkingSetCache.impl.getWorkingSet(Integer.valueOf(item.getId()));
			item.setText(data.getWorkingSetName() + " -- " + data.getSpeciesIDs().size() + " species");
		}
	}

}
