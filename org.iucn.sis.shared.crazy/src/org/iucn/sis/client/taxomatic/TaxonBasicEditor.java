package org.iucn.sis.client.taxomatic;

import org.iucn.sis.client.components.panels.PanelManager;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxonBasicEditor extends LayoutContainer {
	private final VerticalPanel editor;
	private final PanelManager manager;
	private ButtonBar bar;
	private Button close;
	private Button save;
	private Button saveAndClose;
	private final TaxonNode node;
	private ListBox deprecated;
	private ListBox hybrid;
	private TextBox name;
	// private TextBox status;
	private ListBox status;
	private TextBox taxonomicAuthority;
	private ListBox level;

	public TaxonBasicEditor(PanelManager manager) {
		super();
		this.manager = manager;
		node = TaxonomyCache.impl.getCurrentNode();
		editor = new VerticalPanel();
		if (node != null)
			draw();
		else {
			add(new HTML("Please select a taxon first."));
		}

	}

	private void buildButtons() {
		bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		save = new Button("Save");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}

		});
		saveAndClose = new Button("Save and Close");
		saveAndClose.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				saveAndClose();
			}

		});
		close = new Button("Cancel");
		close.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				close();
			}

		});
		bar.add(save);
		bar.add(close);
		bar.add(saveAndClose);
	}

	private void close() {
		BaseEvent be = new BaseEvent(this);
		be.setCancelled(false);
		fireEvent(Events.Close, be);
	}

	private void draw() {
		setLayout(new FillLayout());
		addStyleName("gwt-background");
		add(editor);
		editor.setStyleName("gwt-background");

		buildButtons();
		drawInfo();

		editor.add(bar);
		editor.setCellVerticalAlignment(bar, HasVerticalAlignment.ALIGN_BOTTOM);
	}

	private void drawInfo() {
		String width = "150px";
		String htmlWidth = "80px";

		HTML html = new HTML("Name: ");
		html.setWidth(htmlWidth);
		name = new TextBox();
		name.setWidth(width);
		name.setText(node.getName());
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(4);
		panel.add(html);
		panel.add(name);
		editor.add(panel);
		int height = panel.getOffsetHeight() + 15;
		editor.setCellHeight(panel, height + "px");

		level = new ListBox();

		if (node.getLevel() == TaxonNode.SUBPOPULATION) {
			level.addItem(node.getLevelString(), node.getLevel() + "");
			level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_SUBSPECIES),
					TaxonNode.INFRARANK + "" + TaxonNode.INFRARANK_TYPE_SUBSPECIES);
			if (!node.getFootprintAsString().contains("ANIMALIA"))
				level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_VARIETY),
						TaxonNode.INFRARANK + "" + TaxonNode.INFRARANK_TYPE_VARIETY);
			level.setEnabled(true);
		} else if (node.getLevel() == TaxonNode.INFRARANK) {
			if (node.getInfrarankType() == TaxonNode.INFRARANK_TYPE_SUBSPECIES) {
				level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_SUBSPECIES),
						TaxonNode.INFRARANK + "" + TaxonNode.INFRARANK_TYPE_SUBSPECIES);
				if (!node.getFootprintAsString().contains("ANIMALIA"))
					level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_VARIETY),
							TaxonNode.INFRARANK + "" + TaxonNode.INFRARANK_TYPE_VARIETY);
			} else {
				if (!node.getFootprintAsString().contains("ANIMALIA"))
					level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_VARIETY),
							TaxonNode.INFRARANK + "" + TaxonNode.INFRARANK_TYPE_VARIETY);
				level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK, TaxonNode.INFRARANK_TYPE_SUBSPECIES),
						TaxonNode.INFRARANK + "" + TaxonNode.INFRARANK_TYPE_SUBSPECIES);
			}

			save.setEnabled(false);
			saveAndClose.setEnabled(false);

			TaxonomyCache.impl.fetchPathWithID(node.getId() + "", new GenericCallback<NativeDocument>() {
				public void onFailure(Throwable caught) {
					level.setEnabled(false);

					save.setEnabled(true);
					saveAndClose.setEnabled(true);
				}

				public void onSuccess(NativeDocument ndoc) {
					if (ndoc.getDocumentElement().getElementsByTagName("option").getLength() == 0) {
						// It has no children - it can be changed to a subpop.
						level.addItem(TaxonNode.getDisplayableLevel(TaxonNode.SUBPOPULATION), TaxonNode.SUBPOPULATION
								+ "");
						level.setEnabled(true);
					} else
						level.setEnabled(false);

					save.setEnabled(true);
					saveAndClose.setEnabled(true);
				}
			});
		} else {
			level.addItem(node.getLevelString(), node.getLevel() + "");
			level.setEnabled(false);
		}

		html = new HTML("Level: ");
		html.setWidth(htmlWidth);
		panel = new HorizontalPanel();
		panel.setSpacing(4);
		panel.add(html);
		panel.add(level);
		editor.add(panel);
		height = panel.getOffsetHeight() + 15;
		editor.setCellHeight(panel, height + "px");

		html = new HTML("Status: ");
		html.setWidth(htmlWidth);
		// status = new TextBox();
		// status.setWidth(width);
		// status.setText(node.getStatus());

		status = new ListBox();
		status.addItem("<Unset>", "");
		status.addItem("New", "N");
		status.addItem("Accepted", "A");
		status.addItem("Discarded", "D");
		status.addItem("Synonym", "S");
		if (node.getLevel() >= TaxonNode.SPECIES)
			status.addItem("Undescribed", "U");

		if (node.getStatus().equalsIgnoreCase(""))
			status.setSelectedIndex(0);
		else if (node.getStatus().equalsIgnoreCase("N"))
			status.setSelectedIndex(1);
		else if (node.getStatus().equalsIgnoreCase("A"))
			status.setSelectedIndex(2);
		else if (node.getStatus().equalsIgnoreCase("D"))
			status.setSelectedIndex(3);
		else if (node.getStatus().equalsIgnoreCase("S"))
			status.setSelectedIndex(4);
		else if (node.getStatus().equalsIgnoreCase("U"))
			status.setSelectedIndex(5);
		else {
			status.addItem(node.getStatus());
			status.setSelectedIndex(status.getItemCount() - 1);
		}

		panel = new HorizontalPanel();
		panel.setSpacing(4);
		panel.add(html);
		panel.add(status);
		editor.add(panel);
		height = panel.getOffsetHeight() + 15;
		editor.setCellHeight(panel, height + "px");

		html = new HTML("Deprecated: ");
		html.setWidth(htmlWidth);
		deprecated = new ListBox(false);
		deprecated.setWidth(width);
		deprecated.addItem("true", "1");
		deprecated.addItem("false", "0");
		deprecated.setSelectedIndex(node.isDeprecated() ? 0 : 1);
		panel = new HorizontalPanel();
		panel.setSpacing(4);
		panel.add(html);
		panel.add(deprecated);
		editor.add(panel);
		height = panel.getOffsetHeight() + 15;
		editor.setCellHeight(panel, height + "px");

		if (node.getLevel() >= TaxonNode.SPECIES) {
			html = new HTML("Hybrid: ");
			html.setWidth(htmlWidth);
			hybrid = new ListBox(false);
			hybrid.setWidth(width);
			hybrid.addItem("true", "1");
			hybrid.addItem("false", "0");
			hybrid.setSelectedIndex(node.isHybrid() ? 0 : 1);
			panel = new HorizontalPanel();
			panel.setSpacing(4);
			panel.add(html);
			panel.add(hybrid);
			editor.add(panel);
			height = panel.getOffsetHeight() + 15;
			editor.setCellHeight(panel, height + "px");
		}

		html = new HTML("Taxonomic Authority: ");
		html.setWidth(htmlWidth);
		taxonomicAuthority = new TextBox();
		taxonomicAuthority.setWidth(width);
		taxonomicAuthority.setText(node.getTaxonomicAuthority());
		panel = new HorizontalPanel();
		panel.setSpacing(4);
		panel.add(html);
		panel.add(taxonomicAuthority);
		editor.add(panel);
		height = panel.getOffsetHeight() + 15;
		editor.setCellHeight(panel, height + "px");
	}

	private void refresh() {
		manager.taxonomicSummaryPanel.update(node.getId() + "");
	}

	private void save() {
		saveInfo();
		TaxomaticUtils.impl.writeNodeToFS(node, new GenericCallback<Object>() {
			public void onFailure(Throwable caught) {
				if( caught instanceof GWTConflictException ) {
					WindowUtils.infoAlert("Error", node.getFullName() + " has not been saved. A taxon"
							+ " in the kingdom " + node.getKingdomName() + " already exists.");
					TaxonomyCache.impl.evict(node.getId()+"");
				} else
					WindowUtils.infoAlert("Error", node.getFullName() + " has not been saved.  "
							+ "Please try again later.");
				bar.setEnabled(true);
			}

			public void onSuccess(Object arg0) {
				WindowUtils.infoAlert("Success", node.getFullName() + " has been saved.");
				bar.setEnabled(true);
				refresh();
			}

		});
	}

	private void saveAndClose() {
		saveInfo();
		TaxomaticUtils.impl.writeNodeToFS(node, new GenericCallback<Object>() {

			public void onFailure(Throwable caught) {
				if( caught instanceof GWTConflictException ) {
					WindowUtils.infoAlert("Error", node.getFullName() + " has not been saved. A taxon"
							+ " in the kingdom " + node.getKingdomName() + " already exists.");
					TaxonomyCache.impl.evict(node.getId()+"");
				} else
					WindowUtils.infoAlert("Error", node.getFullName() + " has not been saved.  "
						+ "Please try again later.");
				bar.setEnabled(true);

			}

			public void onSuccess(Object arg0) {
				close();
				WindowUtils.infoAlert("Success", node.getFullName() + " has been saved.");
				refresh();
			}

		});
	}

	private void saveInfo() {
		bar.setEnabled(false);
		// node.setDeprecated(deprecated.getValue(deprecated.getSelectedIndex()).
		// equalsIgnoreCase("1")
		// ? true : false);

		if (node.getLevel() >= TaxonNode.SPECIES)
			node.setHybrid(hybrid.getValue(hybrid.getSelectedIndex()).equalsIgnoreCase("1") ? true : false);

		if (level.isEnabled()) {
			int newLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()));
			int infraType = TaxonNode.INFRARANK_TYPE_NA;
			if (newLevel > TaxonNode.INFRARANK_SUBPOPULATION) {
				infraType = newLevel % (TaxonNode.INFRARANK * 10);
				newLevel = Integer.parseInt(level.getValue(level.getSelectedIndex()).substring(0, 1));

			}
			node.setLevel(newLevel);
			node.setInfraType(infraType);
		}

		node.setName(name.getText());
		node.correctFullName();
		node.setStatus(status.getValue(status.getSelectedIndex()));
		node.setTaxonomicAuthority(taxonomicAuthority.getText());
		close.setText("Close");
	}
}
