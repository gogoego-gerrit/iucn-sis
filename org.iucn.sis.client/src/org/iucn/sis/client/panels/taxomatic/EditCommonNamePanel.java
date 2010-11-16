package org.iucn.sis.client.panels.taxomatic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.client.api.caches.LanguageCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.panels.ClientUIContainer;
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
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class EditCommonNamePanel extends Window implements DrawsLazily {

	protected final CommonName cn;
	protected final Taxon taxon;
	protected final GenericCallback<CommonName> callback;
	protected ListBox language;
	protected CheckBox isPrimary;
	protected TextBox nameBox;
	
	public EditCommonNamePanel(CommonName cn, Taxon taxon, GenericCallback<CommonName> callback) {
		super();
		this.cn = cn;
		this.taxon = taxon;
		this.callback = callback;
	}
	
	@Override
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	private void open() {
		super.show();
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		LanguageCache.impl.list(new ComplexListener<List<IsoLanguage>>() {
			public void handleEvent(List<IsoLanguage> eventData) {
				draw(eventData);
				
				callback.isDrawn();
			}
		});
	}
	
	protected void draw(List<IsoLanguage> languages) {
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
			isPrimary.setValue(true);
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

		Collections.sort(languages, new IsoLanguageComparator());		
		for (IsoLanguage current : languages) {
			language.addItem(current.getName(), current.getCode());
		}
		
		if (cn != null) {
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

		if (cn != null) {
			nameBox.setText(cn.getName());
			isPrimary.setValue(cn.isPrimary());
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
		if (language.getSelectedIndex() == 0) {
			WindowUtils.errorAlert("You must first select a language for the common name.");
			return;
		}
		
		hide();
		final CommonName copy;
		if (cn == null) {
			copy = new CommonName();
			copy.setTaxon(taxon);
		} else {
			copy = cn.deepCopy();
		}
		copy.setName(nameBox.getText());;
		copy.setIso(new IsoLanguage(language.getItemText(language.getSelectedIndex()), language.getValue(language.getSelectedIndex())));
		copy.setPrincipal(isPrimary.getValue());
		
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
	
	private static class IsoLanguageComparator implements Comparator<IsoLanguage> {
		
		private final PortableAlphanumericComparator comparator;
		
		public IsoLanguageComparator() {
			comparator = new PortableAlphanumericComparator();
		}
		
		@Override
		public int compare(IsoLanguage o1, IsoLanguage o2) {
			return comparator.compare(o1.getName(), o2.getName());
		}
		
	}
	
}
