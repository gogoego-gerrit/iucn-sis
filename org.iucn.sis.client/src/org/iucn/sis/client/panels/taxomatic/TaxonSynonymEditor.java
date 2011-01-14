package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.Set;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxonSynonymEditor extends TaxomaticWindow {

	private final Taxon node;
	private final VerticalPanel synonymInfo;
	private final HorizontalPanel upperLevelPanel;
	private final HorizontalPanel genusPanel;
	private final HorizontalPanel speciePanel;
	private final HorizontalPanel infraPanel;
	private final HorizontalPanel stockPanel;
	private final ArrayList<Synonym> allSynonyms;

//	private final HorizontalPanel authorPanel;
	private final HorizontalPanel speciesAuthorPanel;
	private final HorizontalPanel infraAuthorPanel;

	// private final TextBox authorTextBox;
	private final TextBox speciesAuthorTextBox;
	private final TextBox infraAuthorTextBox;

	private Synonym currentSynonym;
	private ButtonBar bar;
	private Button delete;

	private HTML name;
	private TextBox upperLevelName;
	private TextBox genusName;
	private TextBox specieName;
	private TextBox infrarankName;
	private TextBox stockName;

	private ListBox synonymList;

	private ListBox status;
	private ListBox level;
	private int numberAdded;

	public TaxonSynonymEditor() {
		super();
		setHeading("Synonym Editor");
		setIconStyle("icon-note-edit");
		
		
		this.node = TaxonomyCache.impl.getCurrentTaxon();
		synonymInfo = new VerticalPanel();
		allSynonyms = new ArrayList<Synonym>();
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

		BlurHandler blurHandler = new BlurHandler() {
			public void onBlur(BlurEvent event) {
				if (currentSynonym != null)
					name.setText(currentSynonym.getFriendlyName());
			}
		};
		upperLevelName.addBlurHandler(blurHandler);
		genusName.addBlurHandler(blurHandler);
		specieName.addBlurHandler(blurHandler);
		infrarankName.addBlurHandler(blurHandler);
		stockName.addBlurHandler(blurHandler);

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
		status.addItem(Synonym.ACCEPTED);
		status.addItem(Synonym.ADDED);
		status.addItem(Synonym.CHANGED);
		status.addItem(Synonym.DELETED);

		level = new ListBox();
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.KINGDOM), String.valueOf(TaxonLevel.KINGDOM));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.PHYLUM), String.valueOf(TaxonLevel.PHYLUM));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.CLASS), String.valueOf(TaxonLevel.CLASS));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.ORDER), String.valueOf(TaxonLevel.ORDER));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.FAMILY), String.valueOf(TaxonLevel.FAMILY));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.GENUS), String.valueOf(TaxonLevel.GENUS));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.SPECIES), String.valueOf(TaxonLevel.SPECIES));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_SUBSPECIES),
				String.valueOf(TaxonLevel.INFRARANK));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK, Infratype.INFRARANK_TYPE_VARIETY),
				String.valueOf(TaxonLevel.INFRARANK));
		level.addItem(TaxonLevel.getDisplayableLevel(TaxonLevel.SUBPOPULATION),
				String.valueOf(TaxonLevel.SUBPOPULATION));

		// level.addItem("Infrarank " +
		// TaxonLevel.getDisplayableLevel(TaxonLevel.INFRARANK_SUBPOPULATION),
		// String
		// .valueOf(TaxonLevel.INFRARANK_SUBPOPULATION));
		// authorities.put(TaxonLevel.INFRARANK_SUBPOPULATION, new TextBox());

		// authorPanel = new HorizontalPanel();
		// authorTextBox = new TextBox();
		// HTML html = new HTML("Authority: ");
		// authorPanel.add(html);
		// authorPanel.add(authorTextBox);

		speciesAuthorPanel = new HorizontalPanel();
		speciesAuthorTextBox = new TextBox();
		HTML html = new HTML("Species Authority: ");
		speciesAuthorPanel.add(html);
		speciesAuthorPanel.add(speciesAuthorTextBox);

		infraAuthorPanel = new HorizontalPanel();
		infraAuthorTextBox = new TextBox();
		html = new HTML("Infrarank Authority: ");
		infraAuthorPanel.add(html);
		infraAuthorPanel.add(infraAuthorTextBox);

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

		infraAuthorTextBox.setText("");
		speciesAuthorTextBox.setText("");
		// authorTextBox.setText("");
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

		Synonym data = new Synonym();
		data.setName("genus");
		data.setSpeciesName("species " + numberAdded);
		data.setTaxon_level(node.getTaxonLevel());
		allSynonyms.add(data);
		refreshSynonym(data);
		return data.getFriendlyName();

	}

	private void doShowHide() {
		int curLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()));
		
		if (curLevel < TaxonLevel.GENUS)
			upperLevelPanel.setVisible(true);
		else
			upperLevelPanel.setVisible(false);

		if (curLevel >= TaxonLevel.GENUS)
			genusPanel.setVisible(true);
		else
			genusPanel.setVisible(false);

		if (curLevel >= TaxonLevel.SPECIES)
			speciePanel.setVisible(true);
		else
			speciePanel.setVisible(false);

		if (curLevel == TaxonLevel.INFRARANK_SUBPOPULATION || curLevel == TaxonLevel.INFRARANK)
			infraPanel.setVisible(true);
		else
			infraPanel.setVisible(false);

		if (curLevel == TaxonLevel.INFRARANK_SUBPOPULATION || curLevel == TaxonLevel.SUBPOPULATION) {
			stockPanel.setVisible(true);
		} else
			stockPanel.setVisible(false);
		if (curLevel >= TaxonLevel.SPECIES ) {
//			authorPanel.setVisible(false);
			speciesAuthorPanel.setVisible(true);
			if (curLevel != TaxonLevel.SPECIES) {
				infraAuthorPanel.setVisible(true);
			} else {
				infraAuthorPanel.setVisible(false);
			}
		} else {
//			authorPanel.setVisible(true);
			speciesAuthorPanel.setVisible(false);
			infraAuthorPanel.setVisible(false);
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

		getSynonym();
		refreshListBox();
	}

	private void drawButtons(BorderLayoutData layoutData) {
		bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.CENTER);
		bar.setWidth("100%");
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
				hide();
			}

		});
		delete = new Button("Delete Synonym");
		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if (currentSynonym != null) {
					TaxonomyCache.impl.deleteSynonymn(node, currentSynonym, new GenericCallback<String>() {

						@Override
						public void onSuccess(String result) {
							currentSynonym = null;
							bar.enable();
							WindowUtils.infoAlert("Saved", "Synonym has been deleted.");
							ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node
									.getId());
							allSynonyms.clear();
							allSynonyms.addAll(node.getSynonyms());
							refreshListBox();
							refreshSynonym(currentSynonym);

						}

						@Override
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Unable to delete the synonym");

						}
					});
				} else {
					WindowUtils.infoAlert("Please select synonym to delete.");
				}

			}

		});
		bar.add(close);
		bar.add(save);
		bar.add(delete);
		bar.add(saveAndClose);

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
		synonymList.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				String value = synonymList.getValue(synonymList.getSelectedIndex());
				if (value.equalsIgnoreCase("")) {
					refreshSynonym(null);
				} else {
					refreshSynonym(allSynonyms.get(Integer.parseInt(value)));
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

	private void getSynonym() {
		allSynonyms.clear();
		Set<Synonym> temp = node.getSynonyms();
		for (Synonym cur : temp) {
			allSynonyms.add(cur);
		}
	}

	public void refreshListBox() {
		synonymList.clear();
		synonymList.addItem("", "");
		for (int i = 0; i < allSynonyms.size(); i++) {
			synonymList.addItem(allSynonyms.get(i).getFriendlyName(), i + "");
		}
	}

	public void refreshSynonym(Synonym newSynonym) {
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
			name.setText(getShowableData(currentSynonym.getFriendlyName()));
			hp.setSpacing(5);
			hp.add(html);
			hp.add(name);
			synonymInfo.add(hp);

			String highLevelName = currentSynonym.getTaxon_level().getLevel() == TaxonLevel.GENUS ? "" : currentSynonym
					.getName();

			upperLevelName.setText("high level " + getShowableData(highLevelName));
			upperLevelName.setName(highLevelName);
			synonymInfo.add(upperLevelPanel);

			genusName.setText(getShowableData(currentSynonym.getGenusName()));
			genusName.setName(currentSynonym.getGenusName());
			synonymInfo.add(genusPanel);

			specieName.setText(getShowableData(currentSynonym.getSpeciesName()));
			specieName.setName(currentSynonym.getSpeciesName());
			synonymInfo.add(speciePanel);

			infrarankName.setText(getShowableData(currentSynonym.getInfraName()));
			infrarankName.setName(currentSynonym.getInfraName());
			synonymInfo.add(infraPanel);

			stockName.setText(getShowableData(currentSynonym.getStockName()));
			stockName.setName(currentSynonym.getStockName());
			synonymInfo.add(stockPanel);

			hp = new HorizontalPanel();
			html = new HTML("Action Proposed: ");
			if (currentSynonym.getStatus() == null || currentSynonym.getStatus().equals(Synonym.ADDED)
					|| currentSynonym.getStatus().equalsIgnoreCase("New")) {
				status.setSelectedIndex(1);
			} else if (currentSynonym.getStatus().equals(Synonym.ACCEPTED)
					|| currentSynonym.getStatus().equalsIgnoreCase("S")) {
				status.setSelectedIndex(0);
			} else if (currentSynonym.getStatus().equals(Synonym.CHANGED)) {
				status.setSelectedIndex(2);
			} else if (currentSynonym.getStatus().equals(Synonym.DELETED)) {
				status.setSelectedIndex(3);
			}

			hp.setSpacing(5);
			hp.add(html);
			hp.add(status);
			synonymInfo.add(hp);

			hp = new HorizontalPanel();
			html = new HTML("Level: ");
			int taxonLevel = currentSynonym.getTaxon_level().getLevel();
			final int offset;
			if (taxonLevel < TaxonLevel.INFRARANK)
				offset = 0;
			else if (TaxonLevel.INFRARANK == taxonLevel) {
				String iName = currentSynonym.getInfraType();
				if (Infratype.SUBSPECIES_NAME.equals(iName))
					offset = 0;
				else if (Infratype.VARIETY_NAME.equals(iName))
					offset = 1;
				else {
					taxonLevel = -1;
					offset = 0;
				}
			}
			else
				offset = 1;
			
			level.setSelectedIndex(taxonLevel + offset);
			hp.setSpacing(5);
			hp.add(html);
			hp.add(level);
			synonymInfo.add(hp);

			level.addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					doShowHide();
				}
			});

			authorityVp.setSpacing(5);
			// authorTextBox.setValue(newSynonym.getAuthor());
			// authorityVp.add(authorPanel);
			speciesAuthorTextBox.setText(newSynonym.getSpeciesAuthor());
			authorityVp.add(speciesAuthorPanel);
			infraAuthorTextBox.setText(newSynonym.getInfrarankAuthor());
			authorityVp.add(infraAuthorPanel);

			synonymInfo.add(authorityVp);

			if (Integer.parseInt(level.getValue(level.getSelectedIndex())) < TaxonLevel.SPECIES)
				genusPanel.setVisible(false);
			if (Integer.parseInt(level.getValue(level.getSelectedIndex())) < TaxonLevel.INFRARANK)
				speciePanel.setVisible(false);
			if (Integer.parseInt(level.getValue(level.getSelectedIndex())) != TaxonLevel.INFRARANK_SUBPOPULATION)
				infraPanel.setVisible(false);
		}

		doShowHide();
	}

	private void save() {
		
		bar.disable();
		storePreviousData();
		
		if (currentSynonym != null) {
			
			if(validateEntry()){

				TaxonomyCache.impl.addOrEditSynonymn(node, currentSynonym, new GenericCallback<String>() {
	
					@Override
					public void onSuccess(String result) {
						allSynonyms.clear();
						allSynonyms.addAll(node.getSynonyms());
						bar.enable();
						WindowUtils.infoAlert("Saved", "Synonym " + currentSynonym.getFriendlyName() + " was saved.");
						ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
						refreshListBox();
						refreshSynonym(null);
	
					}
	
					@Override
					public void onFailure(Throwable caught) {
						bar.enable();
						WindowUtils.errorAlert("Error",
								"An error occurred when trying to save the synonym data related to " + node.getFullName()
										+ ".");
	
					}
				});
			}
		} else {
			WindowUtils.infoAlert("Please select synonym to save.");
			bar.enable();
		}

	}

	private void saveAndClose() {
		bar.disable();
		storePreviousData();
		if (currentSynonym != null) {
			
			if(validateEntry()){
				TaxonomyCache.impl.addOrEditSynonymn(node, currentSynonym, new GenericCallback<String>() {
	
					@Override
					public void onSuccess(String result) {
	
						bar.enable();
						WindowUtils.infoAlert("Saved", "Synonym " + currentSynonym.getFriendlyName() + " was saved.");
						ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
						hide();
	
					}
	
					@Override
					public void onFailure(Throwable caught) {
						bar.enable();
						WindowUtils.errorAlert("Error",
								"An error occurred when trying to save the synonym data related to " + node.getFullName()
										+ ".");
	
					}
				});
			}
		} else {
			WindowUtils.infoAlert("Please select synonym to save.");
			hide();
		}

	}
	
	private boolean validateEntry(){
		
		int curLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()));
		boolean flag = true;

		if (curLevel < TaxonLevel.GENUS && currentSynonym.getName().equals("")){
			WindowUtils.infoAlert("Please enter the Synonym name!");
			flag = false; 
		}else if (curLevel == TaxonLevel.GENUS && currentSynonym.getGenusName().equals("")) {
			WindowUtils.infoAlert("Please enter the Genus name!");
			flag = false; 
		} else if (curLevel == TaxonLevel.SPECIES) {
			if(currentSynonym.getGenusName().equals("")){
				WindowUtils.infoAlert("Please enter the Genus name!");
				flag = false; 
			}else if(currentSynonym.getSpeciesName().equals("")){
				WindowUtils.infoAlert("Please enter the Species name!"); flag = false; 
			}
		} else if (curLevel == TaxonLevel.INFRARANK) {
			if(currentSynonym.getGenusName().equals("")){
				WindowUtils.infoAlert("Please enter the Genus name!");
				flag = false; 
			}else if(currentSynonym.getSpeciesName().equals("")){
				WindowUtils.infoAlert("Please enter the Species name!");
				flag = false; 
			}else if(currentSynonym.getInfraName().equals("")){
				WindowUtils.infoAlert("Please enter the Infrarank name!");
				flag = false; 
			}
			
		} else if (curLevel == TaxonLevel.SUBPOPULATION) {
			if(currentSynonym.getGenusName().equals("")){
				WindowUtils.infoAlert("Please enter the Genus name!"); 
				flag = false; 
			}else if(currentSynonym.getSpeciesName().equals("")){
				WindowUtils.infoAlert("Please enter the Species name!");
				flag = false; 
			}else if(currentSynonym.getStockName().equals("")){
				WindowUtils.infoAlert("Please enter the Subpopulation name!");
				flag = false; 
			}				
		}
		bar.enable();
		return flag;
	}

	private void storePreviousData() {
		if (currentSynonym != null) {
			int curLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()));

			if (curLevel < TaxonLevel.GENUS)
				currentSynonym.setName(upperLevelName.getText());
			else if (curLevel == TaxonLevel.GENUS) {
				currentSynonym.setGenusName(genusName.getText());
			} else if (curLevel == TaxonLevel.SPECIES) {
				currentSynonym.setGenusName(genusName.getText());
				currentSynonym.setSpeciesName(specieName.getText());
			} else if (curLevel == TaxonLevel.SUBPOPULATION) {
				currentSynonym.setGenusName(genusName.getText());
				currentSynonym.setSpeciesName(specieName.getText());
				currentSynonym.setStockName(stockName.getText());
			} else if (curLevel == TaxonLevel.INFRARANK) {
				currentSynonym.setGenusName(genusName.getText());
				currentSynonym.setSpeciesName(specieName.getText());
				currentSynonym.setInfraName(infrarankName.getText());
				
				String text = level.getItemText(level.getSelectedIndex());
				if (text.equalsIgnoreCase(Infratype.VARIETY_NAME)) {
					currentSynonym.setInfraTypeObject(Infratype.getInfratype(Infratype.VARIETY_NAME));
					currentSynonym.setInfraType(Infratype.VARIETY_NAME);
				} else {
					currentSynonym.setInfraTypeObject(Infratype.getInfratype(Infratype.SUBSPECIES_NAME));
					currentSynonym.setInfraType(Infratype.SUBSPECIES_NAME);
				}

			} else if (curLevel == TaxonLevel.INFRARANK_SUBPOPULATION) {
				currentSynonym.setGenusName(genusName.getText());
				currentSynonym.setSpeciesName(specieName.getText());
				currentSynonym.setInfraName(infrarankName.getText());
				currentSynonym.setStockName(stockName.getText());
			}

			currentSynonym.setStatus(status.getItemText(status.getSelectedIndex()));

			currentSynonym.clearAuthorities();
			
			currentSynonym.setTaxon_level(TaxonLevel.getTaxonLevel(curLevel));
			currentSynonym.setSpeciesAuthor(speciesAuthorTextBox.getValue());
			currentSynonym.setInfrarankAuthor(infraAuthorTextBox.getValue());
			// currentSynonym.setAuthor(authorTextBox.getValue());
			currentSynonym.setFriendlyName(null);
			currentSynonym.getFriendlyName();
		}

	}

}
