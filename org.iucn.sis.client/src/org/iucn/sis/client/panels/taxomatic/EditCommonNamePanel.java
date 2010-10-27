package org.iucn.sis.client.panels.taxomatic;

import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.taxomatic.CommonNameDisplay.AddCommonNameClickListener;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.extjs.client.WindowUtils;

public class EditCommonNamePanel extends Window {

	protected final CommonName cn;
	protected final Taxon taxon;
	protected final GenericCallback<CommonName> callback;
	protected ListBox language;
	protected CheckBox isPrimary;
	protected TextBox nameBox;
	
	public EditCommonNamePanel(CommonName cn, Taxon taxon, GenericCallback<CommonName> callback) {
		
		this.cn = cn;
		this.taxon = taxon;
		this.callback = callback;
		
		draw();
	}
	
	protected void draw() {
		VerticalPanel contentPanel = new VerticalPanel();
		contentPanel.setSpacing(5);

		VerticalPanel leftPanel = new VerticalPanel();
		leftPanel.setSpacing(5);
		leftPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		VerticalPanel rightPanel = new VerticalPanel();
		rightPanel.setSpacing(5);
		rightPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

		HorizontalPanel buttonPanel = new HorizontalPanel();
		if (cn == null)
			setHeading("Add New Common Name");
		else
			setHeading("Edit Common Name");

		nameBox = new TextBox();
		isPrimary = new CheckBox();
		if (taxon.getCommonNames().size() == 0) {
			isPrimary.setChecked(true);
			isPrimary.setEnabled(false);
		}
		isPrimary.setText("Primary Name");

		final Button add = new Button();
		if (cn == null)
			add.setText("Add Common Name");
		else
			add.setText("Save");
		add.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		});
		

		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		});

		language = new ListBox();
		language.addItem("", "");
		final NativeDocument isoDoc = SimpleSISClient.getHttpBasicNativeDocument();
		isoDoc.get(UriBase.getInstance().getSISBase() +"/raw/utils/ISO-639-2_utf-8.xml", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Error Loading Languages", "Could not load "
						+ "languages for the drop down. Please check your Internet "
						+ "connectivity if you are running online, or check your local "
						+ "server if you are running offline, then try again.");
				hide();
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

		if (cn != null) {
			nameBox.setText(cn.getName());
			isPrimary.setChecked(cn.isPrimary());
			if (cn.getIso() == null) {
				language.setSelectedIndex(0);
			} else {
				String iso = cn.getIsoCode();
				for (int i = 0; i < language.getItemCount(); i++) {
					if (language.getValue(i).equalsIgnoreCase(iso)) {
						language.setSelectedIndex(i);
						break;
					}
				}
			}
		}

		HTML nameLabel = new HTML("Name: ");
		HTML langLabel = new HTML("Language: ");

		leftPanel.add(nameLabel);
		rightPanel.add(nameBox);

		leftPanel.add(langLabel);
		rightPanel.add(language);
		rightPanel.add(isPrimary);

		buttonPanel.add(add);
		buttonPanel.add(cancel);

		HorizontalPanel wrap = new HorizontalPanel();
		wrap.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		wrap.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		wrap.add(leftPanel);
		wrap.add(rightPanel);

		contentPanel.add(wrap);
		contentPanel.add(buttonPanel);
		add(contentPanel);
	}
	
	protected void save() {
		hide();
		final CommonName copy;
		if (cn == null) {
			copy = new CommonName();
			copy.setTaxon(taxon);
		} else {
			copy = cn.deepCopy();
			copy.setId(cn.getId());
		}
		copy.setName(nameBox.getText());;
		copy.setIso(new IsoLanguage(language.getItemText(language.getSelectedIndex()), language.getValue(language.getSelectedIndex())));
		copy.setPrincipal(isPrimary.isChecked());
		
		TaxonomyCache.impl.addOrEditCommonName(taxon, copy, new GenericCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				
				WindowUtils.infoAlert("Saved", "Common name " + copy.getName() + " was saved.");
				ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());
				callback.onSuccess(copy);
				
			}
		
			@Override
			public void onFailure(Throwable caught) {
				
				WindowUtils.errorAlert("Error", "An error occurred when trying to save the common name data related to "
						+ taxon.getFullName() + ".");
				
			}
		});
	}
	
	
}
