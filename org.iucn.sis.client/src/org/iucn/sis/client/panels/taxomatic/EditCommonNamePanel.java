package org.iucn.sis.client.panels.taxomatic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.client.api.caches.LanguageCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class EditCommonNamePanel extends Window implements DrawsLazily {

	protected final CommonName cn;
	protected final Taxon taxon;
	private final ComplexListener<CommonName> callback;
	protected ListBox language;
	protected CheckBox isPrimary;
	protected TextBox nameBox;
	
	public EditCommonNamePanel(CommonName cn, Taxon taxon, ComplexListener<CommonName> callback) {
		super();
		setSize(550, 250);
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
		if (cn == null)
			setHeading("Add New Common Name");
		else
			setHeading("Edit Common Name");

		nameBox = new TextBox();
		nameBox.setWidth("300px");
		isPrimary = new CheckBox();
		if (taxon.getCommonNames().size() == 0) {
			isPrimary.setValue(true);
			isPrimary.setEnabled(false);
		}

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
			
			nameBox.setText(cn.getName());
			isPrimary.setValue(cn.isPrimary());
		}

		Grid grid = new Grid(3, 2);
		grid.setCellSpacing(8);
		grid.getColumnFormatter().setWidth(0, "150px");
		grid.setHTML(0, 0, "Name: ");
		grid.setWidget(0, 1, nameBox);
		grid.setHTML(1, 0, "Language: ");
		grid.setWidget(1, 1, language);
		grid.setHTML(2, 0, "Primary? ");
		grid.setWidget(2, 1, isPrimary);

		add(grid);
		
		addButton(new Button(cn == null ? "Add Common Name" : "Save", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		}));
		
		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
	}
	
	protected void save() {
		int languageIndex = language.getSelectedIndex();
		String name = nameBox.getText();
		if (languageIndex == 0) {
			WindowUtils.errorAlert("You must first select a language for the common name.");
			return;
		}
		else if (name == null || "".equals(name)) {
			WindowUtils.errorAlert("Please provide a common name.");
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
		copy.setName(name);;
		copy.setIso(new IsoLanguage(language.getItemText(languageIndex), language.getValue(languageIndex)));
		copy.setPrincipal(isPrimary.getValue());
		
		if (cn == null) {
			TaxonomyCache.impl.addCommonName(taxon, copy, new GenericCallback<String>() {
				public void onSuccess(String result) {
					WindowUtils.infoAlert("Saved", "Common name " + copy.getName() + " was saved.");
					
					callback.handleEvent(copy);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Error", "An error occurred when trying to save the common name data related to "
							+ taxon.getFullName() + ".");
				}
			});
		}
		else {
			TaxonomyCache.impl.editCommonName(taxon, copy, new GenericCallback<String>() {
				public void onSuccess(String result) {
					WindowUtils.infoAlert("Saved", "Common name " + copy.getName() + " was saved.");
					
					callback.handleEvent(copy);
				}
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Error", "An error occurred when trying to save the common name data related to "
							+ taxon.getFullName() + ".");
				}
			});
		}
	}
	
	public static class IsoLanguageComparator implements Comparator<IsoLanguage> {
		
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
