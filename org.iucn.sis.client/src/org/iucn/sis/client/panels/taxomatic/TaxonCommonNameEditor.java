package org.iucn.sis.client.panels.taxomatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.PanelManager;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.iucn.sis.shared.api.models.Taxon;

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
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxonCommonNameEditor extends LayoutContainer {

	private final PanelManager manager;
	private final Taxon  node;
	private final VerticalPanel commonNameInfo;
	private final List<CommonName> allCommonNames;
	private CommonName currentCommonName;
	private ButtonBar bar;
	private Button delete;
	private ListBox commonNameList;

	private ListBox validated;
	private ListBox status;
	private TextBox name;
	private ListBox language;
	private int numberAdded;
	private boolean loaded;

	public TaxonCommonNameEditor(PanelManager manager) {
		this.manager = manager;
		this.node = TaxonomyCache.impl.getCurrentTaxon();
		commonNameInfo = new VerticalPanel();
		allCommonNames = new ArrayList<CommonName>();
		numberAdded = 0;

		name = new TextBox();
		name.addFocusListener(new FocusListener() {

			public void onFocus(Widget arg0) {
				// TODO Auto-generated method stub

			}

			public void onLostFocus(Widget arg0) {
				String oldName = name.getName();
				boolean found = false;
				for (int i = 0; i < commonNameList.getItemCount() && !found; i++) {
					if (commonNameList.getItemText(i).equalsIgnoreCase(oldName)) {
						commonNameList.setItemText(i, name.getText());
						name.setName(name.getText());
						found = true;
					}
				}
			}

		});
		validated = new ListBox();
		validated.addItem("true", "true");
		validated.addItem("false", "false");

		language = new ListBox();
		language.addItem("", "");
		final NativeDocument isoDoc = SimpleSISClient.getHttpBasicNativeDocument();
		isoDoc.get(UriBase.getInstance().getSISBase() +"/raw/utils/ISO-639-2_utf-8.xml", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Error Loading Languages", "Could not load "
						+ "languages for the drop down. Please check your Internet "
						+ "connectivity if you are running online, or check your local "
						+ "server if you are running offline, then try again.");
			}

			public void onSuccess(String result) {
				NativeNodeList isolist = isoDoc.getDocumentElement().getElementsByTagName("language");
				Map<String, String> nameToCode = new HashMap<String, String>();
				String[] names = new String[isolist.getLength()];
				for (int i = 0; i < isolist.getLength(); i++) {
					NativeElement cur = isolist.elementAt(i);
					
					String isoCode = cur.getElementByTagName("bibliographic").getText();
					String lang = cur.getElementByTagName("english").getText();
					names[i] = lang;
					nameToCode.put(lang, isoCode);
				}
				ArrayUtils.quicksort(names);
				
				for (String name : names) {
					language.addItem(name, nameToCode.get(name));
				}
			}
		});
		
		status = new ListBox();
		for (int i = 0; i < CommonName.reasons.length; i++)
			status.addItem(CommonName.reasons[i], i + "");

		
		draw();
	}

	private void clearData() {
		name.setText("");
		language.setSelectedIndex(0);
		status.setSelectedIndex(0);
		validated.setSelectedIndex(0);

	}

	private void close() {
		BaseEvent event = new BaseEvent(this);
		event.setCancelled(false);
		fireEvent(Events.Close, event);
	}
	
	public String createNewCommonName() {

		String name = null;
		do {
			numberAdded++;
			name = "new commonname " + numberAdded;
			boolean found = false;
			for (int i = 1; i < commonNameList.getItemCount() && !found; i++) {
				if (commonNameList.getItemText(i).equalsIgnoreCase(name)) {
					found = true;
					name = null;
				}
			}

		} while (name == null);

		CommonName data = new CommonName();
		data.setName(name);
		refreshCommonName(data);
		return data.getName();

	}

	private void draw() {
		addStyleName("gwt-background");

		if (node == null) {
			add(new HTML("Please first select a taxon to add edit the commonName data for."));
			return;
		}

		setLayout(new BorderLayout());
		BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, .1f);
		BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER, .8f);
		BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, .1f);

		drawInstructions(north);
		drawCommonNameChooser(center);
		drawButtons(south);

		getCommonName();
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
		delete = new Button("Delete CommonName");
		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				TaxonomyCache.impl.deleteCommonName(node, currentCommonName, new GenericCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						allCommonNames.remove(currentCommonName);
						
						bar.enable();
						WindowUtils.infoAlert("Deleted", "Common name " + currentCommonName.getName() + " has been deleted");
						ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
						currentCommonName = null;
						refreshListBox();
						refreshCommonName(null);
				
					}
				
					@Override
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Unable to delete the synonym");
				
					}
				} );
			}

		});
		bar.add(close);
		bar.add(save);
		bar.add(saveAndClose);
		bar.add(delete);
		add(bar, layoutData);
	}

	private void drawCommonNameChooser(BorderLayoutData layoutData) {
		HorizontalPanel outterWraper = new HorizontalPanel();
		outterWraper.setSpacing(0);
		outterWraper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		HorizontalPanel panel = new HorizontalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		HTML html = new HTML("Common Names: ");
		panel.add(html);
		panel.setSpacing(5);

		commonNameList = new ListBox(false);
		commonNameList.addItem("", "");
		commonNameList.addChangeListener(new ChangeListener() {
			public void onChange(Widget arg0) {
				String value = commonNameList.getValue(commonNameList.getSelectedIndex());
				if (value.equalsIgnoreCase("")) {
					refreshCommonName(null);
				} else {
					refreshCommonName((CommonName) allCommonNames.get(Integer.parseInt(value)));
				}
			}
		});
		commonNameList.setWidth("200px");
		panel.add(commonNameList);
		panel.setSpacing(10);
		html = new HTML(" or ");
		panel.add(html);

		Button createNew = new Button("Create New Common Name");
		createNew.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				String name = createNewCommonName();
				commonNameList.addItem(name, allCommonNames.size() - 1 + "");
				commonNameList.setSelectedIndex(commonNameList.getItemCount() - 1);
			}

		});
		panel.add(createNew);
		
		
		
		outterWraper.add(panel);
		

		LayoutContainer container = new LayoutContainer();
		ScrollPanel scroller = new ScrollPanel(commonNameInfo);
		commonNameInfo.setSize("100%", "100%");
		container.setLayout(new BorderLayout());
		container.add(outterWraper, new BorderLayoutData(LayoutRegion.NORTH, 35));
		container.add(scroller, new BorderLayoutData(LayoutRegion.CENTER));
		commonNameInfo.addStyleName("gwt-background");
		add(container, layoutData);

		layout();
	}

	private void drawInstructions(BorderLayoutData layoutData) {
		HTML html = new HTML("<b>Instructions:</b> Select the commonName from the list which"
				+ " you would like to edit, or chose to create a new commonName.", true);
		add(html, layoutData);
	}

	private void getCommonName() {
		allCommonNames.clear();
		Set<CommonName> temp = node.getCommonNames();
		for (CommonName cur : temp) {
			CommonName copy = cur.deepCopy();
			allCommonNames.add(copy);
		}
	}

	private String getShowableData(String data) {
		if (data == null) {
			data = "";
		}
		return data;
	}

	public void refreshCommonName(CommonName newCommonName) {
		storePreviousData();
		clearData();

		currentCommonName = newCommonName;

		commonNameInfo.clear();
		if (currentCommonName == null) {
			commonNameInfo.setBorderWidth(0);
			delete.setEnabled(false);
		} else {
			delete.setEnabled(true);
			commonNameInfo.setBorderWidth(1);
			commonNameInfo.setSpacing(10);

			HorizontalPanel hp = new HorizontalPanel();
			HTML html = new HTML("Name: ");
			name.setText(getShowableData(currentCommonName.getName()));
			name.setName(currentCommonName.getName());
			hp.setSpacing(5);
			hp.add(html);
			hp.add(name);
			commonNameInfo.add(hp);

			hp = new HorizontalPanel();
			html = new HTML("Language / ISO: ");
			
			
			if (currentCommonName.getIso() == null) {
				language.setSelectedIndex(0);
			} else {
				String iso = currentCommonName.getIsoCode();
				for (int i = 0; i < language.getItemCount(); i++) {
					if (language.getValue(i).equalsIgnoreCase(iso)) {
						language.setSelectedIndex(i);
						break;
					}
				}
			}
			
			hp.setSpacing(5);
			hp.add(html);
			hp.add(language);
			commonNameInfo.add(hp);

			hp = new HorizontalPanel();
			html = new HTML("Validated: ");
			validated.setSelectedIndex(currentCommonName.getValidated() ? 0 : 1);
			hp.setSpacing(5);
			hp.add(html);
			hp.add(validated);
			commonNameInfo.add(hp);

			hp = new HorizontalPanel();
			html = new HTML("Status: ");
			status.setSelectedIndex(currentCommonName.getChangeReason());
			hp.setSpacing(5);
			hp.add(html);
			hp.add(status);
			commonNameInfo.add(hp);
		}

	}

	public void refreshListBox() {
		commonNameList.clear();
		commonNameList.addItem("", "");
		for (int i = 0; i < allCommonNames.size(); i++) {
			commonNameList.addItem(((CommonName) allCommonNames.get(i)).getName(), i + "");
		}
	}

	private void save() {
		if (language.getSelectedIndex() == 0) {
			WindowUtils.errorAlert("You must first select a language for the common name.");
			return;
		}
		bar.disable();
		storePreviousData();
		
		if (currentCommonName != null) {
		
		TaxonomyCache.impl.addOrEditCommonName(node, currentCommonName, new GenericCallback<String>() {
		
			@Override
			public void onSuccess(String result) {
				allCommonNames.clear();
				allCommonNames.addAll(node.getCommonNames());
				bar.enable();
				WindowUtils.infoAlert("Saved", "Common name " + currentCommonName.getName() + " was saved.");
				ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
				refreshListBox();
				refreshCommonName(null);
			}
		
			@Override
			public void onFailure(Throwable caught) {
				bar.enable();
				WindowUtils.errorAlert("Error", "An error occurred when trying to save the common name data related to "
						+ node.getFullName() + ".");
		
			}
		});
		} else {
			WindowUtils.infoAlert("Please select a common name to save.");
		}
	}

	private void saveAndClose() {
		if (language.getSelectedIndex() == 0) {
			WindowUtils.errorAlert("You must first select a language for the common name.");
			return;
		}
		bar.disable();
		storePreviousData();
		if (currentCommonName != null) {
			TaxonomyCache.impl.addOrEditCommonName(node, currentCommonName, new GenericCallback<String>() {

				@Override
				public void onSuccess(String result) {

					bar.enable();
					WindowUtils.infoAlert("Saved", "Common name " + currentCommonName.getName() + " was saved.");
					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
					close();

				}

				@Override
				public void onFailure(Throwable caught) {
					bar.enable();
					WindowUtils.errorAlert(
							"Error",
							"An error occurred when trying to save the common name data related to "
									+ node.getFullName() + ".");
				}
			});
		} else {
			close();
		}
		
	}

	private void storePreviousData() {
		if (currentCommonName != null) {
			currentCommonName.setName(name.getText());
			currentCommonName.setChangeReason(status.getSelectedIndex());
			currentCommonName.setValidated(validated.getItemText(validated.getSelectedIndex()).equalsIgnoreCase("true"));
			currentCommonName.setIso(new IsoLanguage(language.getItemText(language.getSelectedIndex()), language.getValue(language.getSelectedIndex())));
		}
	}
	

}
