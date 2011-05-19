package org.iucn.sis.client.tabs.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.client.panels.utils.RefreshPortlet;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * The Working Set panel displayed on the home page. Contains list of all
 * current working sets, sorted by name. The refresh function should be invoked
 * when the home page is displayed
 * 
 * @author liz.schwartz
 * 
 */
public class WorkingSetPortlet extends RefreshPortlet {
	private List<WorkingSet> workingSets = null;
	private boolean isBuilt;
	private LayoutContainer content = null;
	private HashMap<String, Integer> nameToID = null;

	public WorkingSetPortlet() {
		super("x-panel");
		setHeading("My Working Sets");
		isBuilt = false;
		workingSets = new ArrayList<WorkingSet>();
		nameToID = new HashMap<String, Integer>();
		
		refresh();
	}

	protected void build() {
		if (!isBuilt) {
			content = new LayoutContainer();
			content.setStyleName("x-panel");
			isBuilt = true;
			add(content);
			layout();

			Timer timer = new Timer() {
				@Override
				public void run() {
					refresh();
				}
			};
			timer.schedule(2000);
		} else {
			refresh();
		}
	}

	private void createPopup(final WorkingSet ws) {
		final Window s = WindowUtils.newWindow(ws.getWorkingSetName(), null, true, false);
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
					s.layout();

				};
			});
		}
		// ELSE LOAD NO SPECIES
		else {
			table.setHTML(0, 0, "There are no species in the " + ws.getWorkingSetName() + " working set.");
		}

		s.setLayout(new FillLayout());
		s.setSize(600, 400);
		s.add(table);
		s.show();
	}

	@Override
	public void refresh() {
		if (isBuilt) {
			content.removeAll();
			updateWorkingSets();
			if (workingSets.size() > 0) {
				Image infoIcon;
				HTML htmlName;
				Image goIcon;

				for (final WorkingSet data : workingSets) {
					HorizontalPanel panel = new HorizontalPanel();
					String name = data.getWorkingSetName();
					nameToID.put(name, data.getId());

					infoIcon = new Image("images/icon-information.png");
					infoIcon.addStyleName("pointerCursor");
					infoIcon.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							createPopup(data);
						}
					});

					htmlName = new HTML(name);
					htmlName.setTitle(name);
					htmlName.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							HTML sender = (HTML)event.getSource();
							//WorkingSetCache.impl.setCurrentWorkingSet(nameToID.get(sender.getTitle()));
						}
					});

					htmlName.addStyleName("pointerCursor");

					goIcon = new Image("tango/actions/go-jump.png");
					goIcon.setTitle(name);
					goIcon.addStyleName("pointerCursor");
					goIcon.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							Image sender = (Image)event.getSource();
							/*WorkingSetCache.impl.setCurrentWorkingSet(nameToID.get(sender.getTitle()), true, new SimpleListener() {
								public void handleEvent() {
									ClientUIContainer.bodyContainer
										.setSelection(ClientUIContainer.bodyContainer.tabManager.workingSetPage);
								}
							});*/
						}
					});

					panel.setSpacing(2);
					panel.add(infoIcon);
					panel.add(htmlName);
					panel.setSpacing(6);
					panel.add(goIcon);
					content.add(panel);
					content.layout();
				}
			} else {
				content.add(new HTML("No working sets to display."));
				content.layout();
			}
		} else {
			build();
		}

	}

	protected void updateWorkingSets() {
		workingSets.clear();
		workingSets.addAll(WorkingSetCache.impl.getWorkingSets().values());
		ArrayUtils.insertionSort(workingSets, new WorkingSetCache.WorkingSetComparator());
	}

}