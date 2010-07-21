package org.iucn.sis.client.taxomatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxonSynonymEditor extends LayoutContainer {

	private final TaxonNode node;
	private final VerticalPanel synonymInfo;
	private final HorizontalPanel upperLevelPanel;
	private final HorizontalPanel genusPanel;
	private final HorizontalPanel speciePanel;
	private final HorizontalPanel infraPanel;
	private final HorizontalPanel stockPanel;
	private final ArrayList<SynonymData> allSynonyms;

	private HashMap<Integer, HorizontalPanel> authPanels;
	private SynonymData currentSynonym;
	private ButtonBar bar;
	private Button delete;

	private HTML name;
	private TextBox upperLevelName;
	private TextBox genusName;
	private TextBox specieName;
	private TextBox infrarankName;
	private TextBox stockName;

	private ListBox synonymList;
	private HashMap<Integer, TextBox> authorities;

	private ListBox status;
	private ListBox level;
	private int numberAdded;

	public TaxonSynonymEditor() {
		this.node = TaxonomyCache.impl.getCurrentNode();
		synonymInfo = new VerticalPanel();
		allSynonyms = new ArrayList<SynonymData>();
		numberAdded = 0;

		name = new HTML();

		upperLevelPanel = new HorizontalPanel();
		genusPanel = new HorizontalPanel();
		speciePanel = new HorizontalPanel();
		infraPanel = new HorizontalPanel();
		stockPanel = new HorizontalPanel();

		upperLevelName = new TextBox();
		genusName = new TextBox();
		specieName = new TextBox();
		infrarankName = new TextBox();
		stockName = new TextBox();

		FocusListener focusListener = new FocusListener() {
			public void onFocus(Widget sender) {
			}

			public void onLostFocus(Widget sender) {
				if (currentSynonym != null)
					name.setText(currentSynonym.getName());
			}
		};
		upperLevelName.addFocusListener(focusListener);
		genusName.addFocusListener(focusListener);
		specieName.addFocusListener(focusListener);
		infrarankName.addFocusListener(focusListener);
		stockName.addFocusListener(focusListener);

		upperLevelPanel.setSpacing(5);
		upperLevelPanel.add(new HTML("Name: "));
		upperLevelPanel.add(upperLevelName);
		genusPanel.setSpacing(5);
		genusPanel.add(new HTML("Genus Name: "));
		genusPanel.add(genusName);
		speciePanel.setSpacing(5);
		speciePanel.add(new HTML("Species Name: "));
		speciePanel.add(specieName);
		infraPanel.setSpacing(5);
		infraPanel.add(new HTML("Infrarank Name: "));
		infraPanel.add(infrarankName);
		stockPanel.setSpacing(5);
		stockPanel.add(new HTML("Subpopulation Name: "));
		stockPanel.add(stockName);

		status = new ListBox();
		status.addItem(SynonymData.ACCEPTED);
		status.addItem(SynonymData.ADDED);
		status.addItem(SynonymData.CHANGED);
		status.addItem(SynonymData.DELETED);
		authorities = new HashMap<Integer, TextBox>();
		authPanels = new HashMap<Integer, HorizontalPanel>();

		level = new ListBox();
		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.KINGDOM), String.valueOf(TaxonNode.KINGDOM));
		authorities.put(TaxonNode.KINGDOM, new TextBox());

		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.PHYLUM), String.valueOf(TaxonNode.PHYLUM));
		authorities.put(TaxonNode.PHYLUM, new TextBox());

		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.CLASS), String.valueOf(TaxonNode.CLASS));
		authorities.put(TaxonNode.CLASS, new TextBox());

		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.ORDER), String.valueOf(TaxonNode.ORDER));
		authorities.put(TaxonNode.ORDER, new TextBox());

		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.FAMILY), String.valueOf(TaxonNode.FAMILY));
		authorities.put(TaxonNode.FAMILY, new TextBox());

		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.GENUS), String.valueOf(TaxonNode.GENUS));
		authorities.put(TaxonNode.GENUS, new TextBox());

		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.SPECIES), String.valueOf(TaxonNode.SPECIES));
		authorities.put(TaxonNode.SPECIES, new TextBox());

		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_SUBSPECIES), String
				.valueOf(TaxonNode.INFRARANK));
		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_VARIETY), String
				.valueOf(TaxonNode.INFRARANK));
		authorities.put(TaxonNode.INFRARANK, new TextBox());

		level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.SUBPOPULATION), String.valueOf(TaxonNode.SUBPOPULATION));
		authorities.put(TaxonNode.SUBPOPULATION, new TextBox());

		// level.addItem("Infrarank " +
		// TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK_SUBPOPULATION),
		// String
		// .valueOf(TaxonNode.INFRARANK_SUBPOPULATION));
		// authorities.put(TaxonNode.INFRARANK_SUBPOPULATION, new TextBox());

		for (int h = 0; h < TaxonNode.SUBPOPULATION; h++) {
			HorizontalPanel hp = new HorizontalPanel();
			HTML html = new HTML(TaxonNode.getDisplayableLevel(h) + " Authority: ");
			TextBox cur = authorities.get(h);
			hp.add(html);
			hp.add(cur);
			authPanels.put(h, hp);
		}

		draw();
	}

	private void clearData() {
		name.setText("");
		// status.setText("");
		status.setSelectedIndex(0);
		upperLevelName.setText("");
		genusName.setText("");
		specieName.setText("");
		infrarankName.setText("");
		stockName.setText("");

		Iterator<TextBox> iter = authorities.values().iterator();
		while (iter.hasNext())
			iter.next().setText("");

		// infrarankAuthority.setText("");
		// speciesAuthority.setText("");
		// redListCategory.setText("");
		// criteria.setText("");
		// assessors.setText("");
		// notes.setText("");
		// date.setText("");
	}

	private void close() {
		BaseEvent event = new BaseEvent(this);
		event.setCancelled(false);
		fireEvent(Events.Close, event);
	}

	public String createNewSynonym() {

		String name = null;
		do {
			numberAdded++;
			name = "genus species " + numberAdded;
			boolean found = false;
			for (int i = 1; i < synonymList.getItemCount() && !found; i++) {
				if (synonymList.getItemText(i).equalsIgnoreCase(name)) {
					found = true;
					name = null;
				}
			}

		} while (name == null);

		SynonymData data = new SynonymData("genus", "species " + numberAdded, "", TaxonNode.INFRARANK_TYPE_NA, node
				.getLevel(), node.getId() + "");
		allSynonyms.add(data);
		refreshSynonymData(data);
		return name;

	}

	private void doShowHide() {
		Iterator<Integer> cur = authorities.keySet().iterator();
		int curLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()));

		if (curLevel < TaxonNode.GENUS)
			upperLevelPanel.setVisible(true);
		else
			upperLevelPanel.setVisible(false);

		if (curLevel >= TaxonNode.GENUS)
			genusPanel.setVisible(true);
		else
			genusPanel.setVisible(false);

		if (curLevel >= TaxonNode.SPECIES)
			speciePanel.setVisible(true);
		else
			speciePanel.setVisible(false);

		if (curLevel == TaxonNode.INFRARANK_SUBPOPULATION || curLevel == TaxonNode.INFRARANK)
			infraPanel.setVisible(true);
		else
			infraPanel.setVisible(false);

		if (curLevel == TaxonNode.INFRARANK_SUBPOPULATION || curLevel == TaxonNode.SUBPOPULATION) {
			stockPanel.setVisible(true);
		} else
			stockPanel.setVisible(false);

		while (cur.hasNext()) {
			int key = cur.next();
			if (key < TaxonNode.SUBPOPULATION)
				if (key <= curLevel && key >= TaxonNode.SPECIES)
					authPanels.get(new Integer(key)).setVisible(true);
				else
					authPanels.get(new Integer(key)).setVisible(false);
		}
	}

	private void draw() {
		addStyleName("gwt-background");

		if (node == null) {
			add(new HTML("Please first select a taxon to add edit the synonym data for."));
			return;
		}

		setLayout(new BorderLayout());
		BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, .1f);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER, .8f);
		BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, .1f);

		drawInstructions(north);
		drawSynonymChooser(center);
		drawButtons(south);

		getSynonymData();
		refreshListBox();
	}

	private void drawButtons(BorderLayoutData layoutData) {
		bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		Button save = new Button("Save");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}

		});
		Button saveAndClose = new Button("Save and Close");
		saveAndClose.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				saveAndClose();
			}

		});
		Button close = new Button("Cancel");
		close.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				close();
			}

		});
		delete = new Button("Delete Synonym");
		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				// String oldName = name.getName();
				// boolean found = false;
				// for (int i = 0; i < synonymList.getItemCount() && !found;
				// i++)
				// {
				// if (synonymList.getItemText(i).equalsIgnoreCase(oldName))
				// {
				// synonymList.removeItem(i);
				// synonymList.setSelectedIndex(0);
				// found = true;
				// }
				// }
				// if (found)
				allSynonyms.remove(currentSynonym);

				currentSynonym = null;
				refreshListBox();
				refreshSynonymData(currentSynonym);
			}

		});
		bar.add(close);
		bar.add(save);
		bar.add(saveAndClose);
		bar.add(delete);
		add(bar, layoutData);
	}

	private void drawInstructions(BorderLayoutData layoutData) {
		HTML html = new HTML("<b>Instructions:</b> Select the synonym from the list which"
				+ " you would like to edit, or chose to create a new synonym.", true);
		add(html, layoutData);
	}

	private void drawSynonymChooser(BorderLayoutData layoutData) {
		HorizontalPanel outterWraper = new HorizontalPanel();
		outterWraper.setSpacing(0);
		outterWraper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		HorizontalPanel panel = new HorizontalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		HTML html = new HTML("Synonyms: ");
		panel.add(html);
		panel.setSpacing(5);

		synonymList = new ListBox(false);
		synonymList.addItem("", "");
		synonymList.addChangeListener(new ChangeListener() {
			public void onChange(Widget arg0) {
				String value = synonymList.getValue(synonymList.getSelectedIndex());
				if (value.equalsIgnoreCase("")) {
					refreshSynonymData(null);
				} else {
					refreshSynonymData(allSynonyms.get(Integer.parseInt(value)));
				}
			}
		});
		synonymList.setWidth("200px");
		panel.add(synonymList);
		panel.setSpacing(10);
		html = new HTML(" or ");
		panel.add(html);

		Button createNew = new Button("Create New Synonym");
		createNew.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				String name = createNewSynonym();
				synonymList.addItem(name, allSynonyms.size() - 1 + "");
				synonymList.setSelectedIndex(synonymList.getItemCount() - 1);
			}

		});
		panel.add(createNew);
		outterWraper.add(panel);

		LayoutContainer container = new LayoutContainer();
		ScrollPanel scroller = new ScrollPanel(synonymInfo);
		synonymInfo.setSize("100%", "100%");
		container.setLayout(new BorderLayout());
		container.add(outterWraper, new BorderLayoutData(LayoutRegion.NORTH, 35));
		container.add(scroller, new BorderLayoutData(LayoutRegion.CENTER));
		synonymInfo.addStyleName("gwt-background");
		add(container, layoutData);

		layout();
	}

	private String getShowableData(String data) {
		if (data == null) {
			data = "";
		}
		return data;
	}

	private void getSynonymData() {
		allSynonyms.clear();
		ArrayList temp = node.getSynonyms();
		for (int i = 0; i < temp.size(); i++) {
			allSynonyms.add(((SynonymData) temp.get(i)).deepCopy());
		}
	}

	public void refreshListBox() {
		synonymList.clear();
		synonymList.addItem("", "");
		for (int i = 0; i < allSynonyms.size(); i++) {
			synonymList.addItem(allSynonyms.get(i).getName(), i + "");
		}
	}

	public void refreshSynonymData(SynonymData newSynonym) {
		storePreviousData();
		clearData();

		VerticalPanel authorityVp = new VerticalPanel();
		currentSynonym = newSynonym;

		synonymInfo.clear();
		if (currentSynonym == null) {
			synonymInfo.setBorderWidth(0);
			delete.setEnabled(false);
		} else {
			delete.setEnabled(true);
			synonymInfo.setBorderWidth(1);
			synonymInfo.setSpacing(10);

			HorizontalPanel hp = new HorizontalPanel();
			HTML html = new HTML("Full Name: ");
			name.setText(getShowableData(currentSynonym.getName()));
			if (currentSynonym.isOldVersion()) {

			}
			hp.setSpacing(5);
			hp.add(html);
			hp.add(name);
			synonymInfo.add(hp);

			upperLevelName.setText(getShowableData(currentSynonym.getUpperLevelName()));
			upperLevelName.setName(currentSynonym.getUpperLevelName());
			synonymInfo.add(upperLevelPanel);

			genusName.setText(getShowableData(currentSynonym.getGenus()));
			genusName.setName(currentSynonym.getGenus());
			synonymInfo.add(genusPanel);

			specieName.setText(getShowableData(currentSynonym.getSpecie()));
			specieName.setName(currentSynonym.getSpecie());
			synonymInfo.add(speciePanel);

			infrarankName.setText(getShowableData(currentSynonym.getInfrarank()));
			infrarankName.setName(currentSynonym.getInfrarank());
			synonymInfo.add(infraPanel);

			stockName.setText(getShowableData(currentSynonym.getStockName()));
			stockName.setName(currentSynonym.getStockName());
			synonymInfo.add(stockPanel);

			hp = new HorizontalPanel();
			html = new HTML("Action Proposed: ");
			if (currentSynonym.getStatus().equals(SynonymData.ACCEPTED)
					|| currentSynonym.getStatus().equalsIgnoreCase("S")) {
				status.setSelectedIndex(0);
			} else if (currentSynonym.getStatus().equals(SynonymData.ADDED)
					|| currentSynonym.getStatus().equalsIgnoreCase("New")) {
				status.setSelectedIndex(1);
			} else if (currentSynonym.getStatus().equals(SynonymData.CHANGED)) {
				status.setSelectedIndex(2);
			} else if (currentSynonym.getStatus().equals(SynonymData.DELETED)) {
				status.setSelectedIndex(3);
			}

			hp.setSpacing(5);
			hp.add(html);
			hp.add(status);
			synonymInfo.add(hp);

			hp = new HorizontalPanel();
			html = new HTML("Level: ");
			level.setSelectedIndex(currentSynonym.getLevel());
			hp.setSpacing(5);
			hp.add(html);
			hp.add(level);
			synonymInfo.add(hp);

			level.addChangeListener(new ChangeListener() {
				public void onChange(Widget sender) {
					doShowHide();
				}
			});

			authorityVp.setSpacing(5);

			for (int h = 0; h < TaxonNode.SUBPOPULATION; h++) {
				hp = authPanels.get(h);
				TextBox cur = authorities.get(h);
				cur.setText(getShowableData(currentSynonym.getAuthority(h)));
				authorityVp.add(hp);
			}
			synonymInfo.add(authorityVp);

			// if (Integer.parseInt(level.getValue(level.getSelectedIndex())) <
			// TaxonNode.SPECIES)
			// genusPanel.setVisible(false);
			// if (Integer.parseInt(level.getValue(level.getSelectedIndex())) <
			// TaxonNode.INFRARANK)
			// speciePanel.setVisible(false);
			// if (Integer.parseInt(level.getValue(level.getSelectedIndex())) !=
			// TaxonNode.INFRARANK_SUBPOPULATION)
			// infraPanel.setVisible(false);
		}

		doShowHide();
	}

	private void save() {
		bar.disable();
		storePreviousData();
		int index = 0;
		for (int i = 0; i < allSynonyms.size(); i++) {
			if (allSynonyms.get(index) == null) {
				SysDebugger.getInstance().println("I am removing index " + index);
				allSynonyms.remove(index);
			} else {
				SysDebugger.getInstance().println("In the loop with " + (allSynonyms.get(index)).getName());
				index++;
			}

		}
		SysDebugger.getInstance().println(
				"Before I just set the node to have all synonyms " + TaxonNodeFactory.nodeToDetailedXML(node));
		node.setSynonyms(allSynonyms);
		SysDebugger.getInstance().println(
				"I just set the node to have all synonyms " + TaxonNodeFactory.nodeToDetailedXML(node));
		TaxomaticUtils.impl.writeNodeToFS(node, new GenericCallback<Object>() {

			public void onFailure(Throwable arg0) {
				bar.enable();
				WindowUtils.errorAlert("Error", "An error occurred when trying to save the synonym data related to "
						+ node.getFullName() + ".");
			}

			public void onSuccess(Object arg0) {
				bar.enable();
				WindowUtils.infoAlert("Saved", "All the synonym data related to " + node.getFullName()
						+ " has been saved.");
				ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId() + "");
				refreshListBox();
				refreshSynonymData(null);
			}

		});
	}

	private void saveAndClose() {
		bar.disable();
		storePreviousData();
		int index = 0;
		for (int i = 0; i < allSynonyms.size(); i++) {
			if (allSynonyms.get(index) == null)
				allSynonyms.remove(index);
			else
				index++;
		}

		node.setSynonyms(allSynonyms);
		TaxomaticUtils.impl.writeNodeToFS(node, new GenericCallback<Object>() {

			public void onFailure(Throwable arg0) {
				bar.enable();
				WindowUtils.errorAlert("Error", "An error occurred when trying to save the synonym data related to "
						+ node.getFullName() + ".");
			}

			public void onSuccess(Object arg0) {
				bar.enable();
				WindowUtils.infoAlert("Saved", "All the synonym data related to " + node.getFullName()
						+ " has been saved.");
				ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId() + "");
				close();
			}

		});
	}

	private void storePreviousData() {
		if (currentSynonym != null) {
			int curLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()));

			if (curLevel < TaxonNode.GENUS)
				currentSynonym.setUpperLevelName(upperLevelName.getText());
			else if (curLevel == TaxonNode.GENUS) {
				currentSynonym.setGenus(genusName.getText());
			} else if (curLevel == TaxonNode.SPECIES) {
				currentSynonym.setGenus(genusName.getText());
				currentSynonym.setSpecie(specieName.getText());
			} else if (curLevel == TaxonNode.SUBPOPULATION) {
				currentSynonym.setGenus(genusName.getText());
				currentSynonym.setSpecie(specieName.getText());
				currentSynonym.setStockName(stockName.getText());
			} else if (curLevel == TaxonNode.INFRARANK) {
				currentSynonym.setGenus(genusName.getText());
				currentSynonym.setSpecie(specieName.getText());
				currentSynonym.setInfrarank(infrarankName.getText());

				if (level.getItemText(level.getSelectedIndex()).equals(
						TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_SUBSPECIES)))
					currentSynonym.setInfrarankType(TaxonNode.INFRARANK_TYPE_SUBSPECIES);
				if (level.getItemText(level.getSelectedIndex()).equals(
						TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_VARIETY)))
					currentSynonym.setInfrarankType(TaxonNode.INFRARANK_TYPE_VARIETY);
			} else if (curLevel == TaxonNode.INFRARANK_SUBPOPULATION) {
				currentSynonym.setGenus(genusName.getText());
				currentSynonym.setSpecie(specieName.getText());
				currentSynonym.setInfrarank(infrarankName.getText());
				currentSynonym.setStockName(stockName.getText());
			}

			currentSynonym.setStatus(status.getItemText(status.getSelectedIndex()));

			Iterator<Integer> cur = authorities.keySet().iterator();
			currentSynonym.clearAuthorities();
			while (cur.hasNext()) {
				int key = cur.next();
				if (key <= curLevel && key >= TaxonNode.SPECIES)
					currentSynonym.setAuthority(authorities.get(key).getText(), key);
			}
			currentSynonym.setLevel(curLevel);
		}

	}

}
