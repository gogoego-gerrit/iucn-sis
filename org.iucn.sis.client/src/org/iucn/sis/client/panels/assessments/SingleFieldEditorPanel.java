package org.iucn.sis.client.panels.assessments;

import org.iucn.sis.client.api.assessment.ReferenceableField;
import org.iucn.sis.client.api.caches.FieldWidgetCache;
import org.iucn.sis.client.api.caches.SchemaCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.citations.Referenceable;
import org.iucn.sis.shared.api.displays.Display;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Taxon;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class SingleFieldEditorPanel extends BasicWindow implements DrawsLazily {
	
	private final Field field;
	
	private ComplexListener<Field> saveListener;
	
	public SingleFieldEditorPanel(Field field) {
		this(field, null);
	}
	
	public SingleFieldEditorPanel(Field field, ComplexListener<Field> saveListener) {
		super(field.getName());
		this.field = field;
		
		setLayout(new FillLayout());
		setScrollMode(Scroll.AUTO);
		setSize(600, 450);
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		final String uri = UriBase.getInstance().getSISBase() + "/application/schema" 
			+ "/"+SchemaCache.impl.getDefaultSchema()+"/field/" + field.getName();
		doc.get(uri, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load field, please try again later.");
			}
			public void onSuccess(String arg0) {
				Display found = null;
				final NativeNodeList displays = doc.getDocumentElement().getChildNodes();
				for (int i = 0; i < displays.getLength(); i++) {
					final NativeNode current = displays.item(i);
					if (current.getNodeType() != NativeNode.TEXT_NODE && current instanceof NativeElement) {
						found = FieldWidgetCache.impl.getFieldParser().parseField((NativeElement)current);
					}
				}
				
				final Display display = found;
				if (display != null && display.getCanonicalName() != null && !display.getCanonicalName().equals("")) {
					display.setAssigner(new TaxonFieldIDAssigner());
					display.setReferenceableFieldFactory(new Display.ReferenceableFieldFactory() {
						public Referenceable newReferenceableField(Field field) {
							return new ReferenceableTaxonField(field);
						}
					});
					display.setData(field);
					
					final LayoutContainer wrapper = new LayoutContainer();
					wrapper.addStyleName("gwt-background");
					wrapper.add(display.showDisplay());
					
					add(wrapper);
					
					addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							if (!display.hasChanged())
								Info.display("Save not needed", "No changes were made.");
							else {
								display.save();
								
								if (saveListener != null)
									saveListener.handleEvent(field);
							}
						}
					}));
					addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							hide();
						}
					}));
					
					
					callback.isDrawn();
				} else
					WindowUtils.errorAlert("Unable to parse information for field, please try again later.");
			}
		});
	}
	
	public void setSaveListener(ComplexListener<Field> saveListener) {
		this.saveListener = saveListener;
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
	
	private static class ReferenceableTaxonField extends ReferenceableField {
		
		public ReferenceableTaxonField(Field field) {
			super(field);
		}
		
		@Override
		protected void persist(final GenericCallback<Object> callback) {
			TaxonomyCache.impl.saveTaxon(TaxonomyCache.impl.getCurrentTaxon(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
		
	}
	
	private static class TaxonFieldIDAssigner implements Display.IDAssigner {
	
		@Override
		public void assignID(Field field, final GenericCallback<Object> callback) {
			Taxon taxon = TaxonomyCache.impl.getCurrentTaxon();
			taxon.setTaxonomicNotes(field);
			
			TaxonomyCache.impl.saveTaxon(taxon, new GenericCallback<String>() {
				public void onSuccess(String result) {
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
		
	}

}
