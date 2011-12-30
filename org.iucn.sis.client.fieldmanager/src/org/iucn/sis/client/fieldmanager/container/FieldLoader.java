package org.iucn.sis.client.fieldmanager.container;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.iucn.sis.client.fieldmanager.container.LookupData.LookupDataValue;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldType;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableReplacer;

public class FieldLoader extends SimplePanel implements DrawsLazily {
	
	private final String urlPrefix;
	
	private final ListBox box;
	private final SimplePanel content;
	private final VerticalPanel sql;
	
	public FieldLoader() {
		super();
		box = new ListBox();
		content = new SimplePanel();
		sql = new VerticalPanel();
		
		urlPrefix = Window.Location.getParameter("gwt.codesvr") == null ? "" : "/proxy-service";
	}
	
	private String getCurrentSchema() {
		return box.getValue(box.getSelectedIndex());
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		final NativeDocument document = NativeDocumentFactory.newNativeDocument();
		document.get(url("/application/manager"), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				final Map<String, String> idToName = new LinkedHashMap<String, String>();
				for (RowData row : parser.getRows()) {
					idToName.put(row.getField("id"), row.getField("name"));
				}
				
				draw(idToName);
				
				callback.isDrawn();
			}
			public void onFailure(Throwable caught) {
				Window.alert("Could not load schemas.");
			}
		});
	}
	
	private void draw(Map<String, String> schemas) {
		for (Map.Entry<String, String> entry : schemas.entrySet()) {
			box.addItem(entry.getValue(), entry.getKey());
		}
		if (!schemas.isEmpty())
			box.setSelectedIndex(0);
		
		final TextBox field = newCharOnlyTextBox();
		Button loadButton = new Button("Load", new ClickHandler() {
			public void onClick(ClickEvent event) {
				loadField(field.getText());
			}
		});
		
		Button createButton = new Button("Create", new ClickHandler() {
			public void onClick(ClickEvent event) {
				createField(field.getText());
			}
		});
		
		Button viewButton = new Button("Manage Views", new ClickHandler() {
			public void onClick(ClickEvent event) {
				openTextEditor(url("/application/manager/" + getCurrentSchema() + "/views"));
			}
		});
		
		final VerticalPanel container = new VerticalPanel();
		container.add(horizontal(new HTML("Select Schema: "), box, viewButton));
		container.add(horizontal(new HTML("Enter field name: "), field, loadButton, createButton));
		container.add(content);
		
		content.setWidget(new HTML("Please enter a field name above."));
		
		final HorizontalPanel wrapper = horizontal(container, sql);
		wrapper.setCellWidth(container, "50%");
		wrapper.setCellWidth(sql, "50%");
		
		setWidget(wrapper);
	}
	
	private void createField(final String fieldName) {
		final NativeDocument document = NativeDocumentFactory.newNativeDocument();
		document.putAsText(url("/application/manager/" + getCurrentSchema() + "/field/" + fieldName), "<root/>", new GenericCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof GWTConflictException)
					Window.alert("Could not create field " + fieldName + ".");
				else
					Window.alert("Could not create field " + fieldName + " because it already exists.");
			}
			@Override
			public void onSuccess(String result) {
				updateSQL(document);
				
				/*
				 * <Hack>
				 */
				String prefix, curSchema = getCurrentSchema();
				if (curSchema.contains("birdlife"))
					prefix = "BL";
				else if (curSchema.contains("usetrade"))
					prefix = "UT";
				else
					prefix = "";
				
				Field field = new Field();
				field.setName(prefix + fieldName);
				
				LookupDataContainer container = new LookupDataContainer();
				container.setFieldName(field.getName());
				
				openField(field, container);
			}
		});
	}
	
	private void loadField(final String fieldName) {
		final NativeDocument document = NativeDocumentFactory.newNativeDocument();
		document.get(url("/application/manager/" + getCurrentSchema() + "/field/" + fieldName), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
				Field obj = new Field();
				LookupDataContainer container = new LookupDataContainer();
				for (int i = 0; i < nodes.getLength(); i++) {
					NativeNode current = nodes.item(i);
					if (current.getNodeName().endsWith(fieldName)) {
						obj = parse((NativeElement)current);
						container.setFieldName(obj.getName());
					}
					else if ("lookup".equals(current.getNodeName())) {
						LookupData data = new LookupData();
						NativeNodeList options = current.getChildNodes();
						for (int k = 0; k < options.getLength(); k++) {
							NativeNode option = options.item(k);
							if ("option".equals(option.getNodeName())) {
								data.addValue(((NativeElement)option).getAttribute("id"), option.getTextContent());
							}
						}
						container.put(((NativeElement)current).getAttribute("id"), data);
					}
				}
				openField(obj, container);
			}
			public void onFailure(Throwable caught) {
				Window.alert("Could not find field " + fieldName);
			}
		});
	}
			 
	
	private void openField(final Field field, final LookupDataContainer lookups) {
		final VerticalPanel listing = new VerticalPanel();
		
		final VerticalPanel panel = new VerticalPanel();
		panel.add(horizontal(new HTML("Field: "), new HTML(field.getName())));
		
		for (final PrimitiveField<?> prim : field.getPrimitiveField()) {
			Button delete = new Button("Delete", new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (Window.confirm("You sure??")) {
						final NativeDocument document = NativeDocumentFactory.newNativeDocument();
						document.delete(url("/application/manager/" + getCurrentSchema() + "/field/" + field.getName() + "/" + prim.getName()), new GenericCallback<String>() {
							public void onSuccess(String result) {
								field.getPrimitiveField().remove(prim);
								openField(field, lookups);
							}
							public void onFailure(Throwable caught) {
								Window.alert("Delete failed");
							}
						});
					}
				}
			});
			
			Button lookup = new Button("Manage Lookups", new ClickHandler() {
				public void onClick(ClickEvent event) {
					final String tbl = field.getName() + "_" + prim.getName() + "Lookup";
					
					LookupData data = lookups.get(tbl);
					if (data == null) {
						if (Window.confirm("Lookup table doesn't exist, create?")) {
							final NativeDocument document = NativeDocumentFactory.newNativeDocument();
							document.putAsText(url("/application/manager/" + getCurrentSchema() + "/lookup/" + tbl), "<root/>", new GenericCallback<String>() {
								public void onSuccess(String result) {
									updateSQL(document);
									
									LookupData data = new LookupData();
									
									showLookupEditor(tbl, data);
								}
								public void onFailure(Throwable caught) {
									Window.alert("Failed to create lookup table.");
								}
							});
						}
					}
					else {
						showLookupEditor(tbl, data);
					}
				}
			});
			
			if (prim instanceof ForeignKeyListPrimitiveField || prim instanceof ForeignKeyPrimitiveField)
				listing.add(horizontal(new HTML(prim.getName() + " -- " + prim.getSimpleName()), delete, lookup));
			else
				listing.add(horizontal(new HTML(prim.getName() + " -- " + prim.getSimpleName()), delete));
		}
		
		panel.add(listing);
		panel.add(new Button("Add Another Data Field",new ClickHandler() {
			public void onClick(ClickEvent event) {
				showFieldEditor(field, new ComplexListener<PrimitiveField<?>>() {
					public void handleEvent(PrimitiveField<?> eventData) {
						field.addPrimitiveField(eventData);
						openField(field, lookups);
					}
				});
			}
		}));
		panel.add(new Button("Manage Field Structure", new ClickHandler() {
			public void onClick(ClickEvent event) {
				openTextEditor(url("/application/manager/" + getCurrentSchema() + "/field/" + field.getName() + "/structure"));
			}
		}));
		
		content.setWidget(panel);
	}
	
	private void generateListing(final VerticalPanel listing, final String lookupTableName, final LookupData lookups) {
		listing.clear();
		for (final LookupDataValue value : lookups.getValues()) {
			listing.add(horizontal(new HTML(value.getLabel()), new Button("Delete", new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (Window.confirm("You sure??")) {
						final NativeDocument document = NativeDocumentFactory.newNativeDocument();
						document.delete(url("/application/manager/" + getCurrentSchema() + "/lookup/" + lookupTableName + "/" + value.getLabel()), new GenericCallback<String>() {
							public void onSuccess(String result) {
								lookups.getValues().remove(value);
								
								generateListing(listing, lookupTableName, lookups);
							}
							public void onFailure(Throwable caught) {
								Window.alert("Delete failed");
							}
						});
					}
				}
			})));
		}
	}
	
	private void showLookupEditor(final String lookupTableName, final LookupData lookups) {
		final DialogBox panel = new DialogBox();
		panel.setText("Create Data Field");
		
		final VerticalPanel listing = new VerticalPanel();
		generateListing(listing, lookupTableName, lookups);
		
		final ClickHandler handler;
		
		final TextBox box = newCharOnlyTextBox();
			
		final VerticalPanel wrapper = new VerticalPanel();
		wrapper.add(listing);
		wrapper.add(horizontal(new HTML("Add Value"), box, new Button("Add", handler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				String xml = "<root><lookup><label><![CDATA[" + box.getText() + "]]></label></lookup></root>";
				
				final NativeDocument document = NativeDocumentFactory.newNativeDocument();
				document.postAsText(url("/application/manager/" + getCurrentSchema() + "/lookup/" + lookupTableName), xml, new GenericCallback<String>() {
					public void onSuccess(String result) {
						updateSQL(document);
						
						LookupDataValue value = new LookupDataValue("none", box.getText());
						lookups.getValues().add(value);
						
						box.setText("");
						
						generateListing(listing, lookupTableName, lookups);
					}
					public void onFailure(Throwable caught) {
						Window.alert("Failed");
					}
				});
			}
		})));
		wrapper.add(new Button("Close", new ClickHandler() {
			public void onClick(ClickEvent event) {
				panel.hide();
			}
		}));
		
		box.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					handler.onClick(null);
			}
		});
		
		panel.setWidget(wrapper);
		panel.center();
	}
	
	private void showFieldEditor(final Field parent, final ComplexListener<PrimitiveField<?>> callback) {
		final DialogBox panel = new DialogBox();
		panel.setText("Create Data Field");
		
		final TextBox name = newCharOnlyTextBox();
		final ListBox type = getPrimitiveFieldTypes();
		
		final VerticalPanel container = new VerticalPanel();
		container.add(horizontal(new HTML("Data Field Name: "), name));
		container.add(horizontal(new HTML("Data Field Type: "), type));
		container.add(horizontal(new Button("Create", new ClickHandler() {
			public void onClick(ClickEvent event) {
				StringBuilder xml = new StringBuilder();
				xml.append("<root>");
				xml.append("<field>");
				xml.append("<name><![CDATA[" + name.getText() + "]]></name>");
				xml.append("<type><![CDATA[" + type.getItemText(type.getSelectedIndex()) + "]]></type>");
				xml.append("</field>");
				xml.append("</root>");
				
				final NativeDocument document = NativeDocumentFactory.newNativeDocument();
				document.postAsText(url("/application/manager/" + getCurrentSchema() + "/field/" + parent.getName()), xml.toString(), 
						new GenericCallback<String>() {
					public void onSuccess(String result) {
						panel.hide();
						
						updateSQL(document);
						
						PrimitiveField<?> prim = 
							PrimitiveFieldFactory.generatePrimitiveField(type.getItemText(type.getSelectedIndex()));
						prim.setName(name.getText());
						
						if (prim instanceof ForeignKeyListPrimitiveField)
							((ForeignKeyListPrimitiveField)prim).setTableID(parent.getName() + "_" + prim.getName() + "Lookup");
						else if (prim instanceof ForeignKeyPrimitiveField)
							((ForeignKeyPrimitiveField)prim).setTableID(parent.getName() + "_" + prim.getName() + "Lookup");
						
						callback.handleEvent(prim);
					}
					public void onFailure(Throwable caught) {
						Window.alert("Operation failed.");
					}
				});
			}
		}), new Button("Cancel", new ClickHandler() {
			public void onClick(ClickEvent event) {
				panel.hide();
			}
		})));
		
		panel.setWidget(container);
		panel.center();
	}
	
	private void openTextEditor(final String uri) {
		final NativeDocument document = NativeDocumentFactory.newNativeDocument();
		document.getAsText(uri, new GenericCallback<String>() {
			public void onSuccess(String result) {
				final DialogBox popup = new DialogBox();
				popup.setText("Structure");
				
				final TextArea text = new TextArea();
				text.setSize("500px", "500px");
				text.setValue(document.getText());
				
				final VerticalPanel container = new VerticalPanel();
				container.add(text);
				container.add(horizontal(new Button("Save", new ClickHandler() {
					public void onClick(ClickEvent event) {
						final NativeDocument save = NativeDocumentFactory.newNativeDocument();
						save.put(uri, text.getValue(), new GenericCallback<String>() {
							public void onSuccess(String result) {
								popup.hide();
								Window.alert("Changes saved.");
							}
							public void onFailure(Throwable caught) {
								Window.alert("Changes NOT saved.");
							}
						});
					}
				}), new Button("Cancel", new ClickHandler() {
					public void onClick(ClickEvent event) {
						popup.hide();
					}
				})));
				
				popup.setWidget(container);
				popup.center();
			}
			public void onFailure(Throwable caught) {
				Window.alert("Could not load.");
			}
		});
	}
	
	private ListBox getPrimitiveFieldTypes() {
		ListBox box = new ListBox();
		for (PrimitiveFieldType type : PrimitiveFieldType.values()) {
			box.addItem(type.getName());
		}
		box.setSelectedIndex(0);
		return box;
	}
	
	private HorizontalPanel horizontal(Widget... widgets) {
		HorizontalPanel panel = new HorizontalPanel();
		for (Widget widget : widgets)
			panel.add(widget);
		
		return panel;
	}
	
	private TextBox newCharOnlyTextBox() {
		final TextBox box = new TextBox();
		box.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				char ch = event.getCharCode();
				
				if (Character.isDigit(ch))
					box.cancelKey();
				else if (' ' == ch)
					box.cancelKey();
					
			}
		});
		box.addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				box.setText(PortableReplacer.stripNonword(box.getText()));
			}
		});
		
		return box;
	}
	
	private void updateSQL(NativeDocument document) {
		sql.add(new HTML(document.getText()));
	}
	
	private String url(String url) {
		return urlPrefix + "/apps/org.iucn.sis.server.extensions.fieldmanager" + url;
	}
	
	@SuppressWarnings("unchecked")
	private Field parse(NativeElement element) {
		String id = element.getAttribute("id");
		String name = element.getNodeName();
		
		Field field = new Field();
		field.setName(name);
		field.setFields(new HashSet<Field>());
		field.setPrimitiveField(new HashSet<PrimitiveField>());
		field.setNotes(new HashSet<Notes>());
		field.setReference(new HashSet<Reference>());
		
		try {
			field.setId(Integer.valueOf(id).intValue());
		} catch (NumberFormatException e) {
			Debug.println("ERROR - FIELD " + name + " DOES NOT HAVE AN ID!!! trying to parse " + id + " with name " + name);
		}
		
		final NativeNodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			NativeNode current = children.item(i);
			if ("subfields".equals(current.getNodeName())) {
				NativeNodeList subfields = current.getChildNodes();
				for (int k = 0; k < subfields.getLength(); k++) {
					NativeNode subfield = subfields.item(k);
					Field cur;
					try {
						cur = parse((NativeElement)subfield);
					} catch (ClassCastException e) {
						continue;
					} catch (Throwable e) {
						e.printStackTrace();
						continue;
					}
					
					cur.setParent(field);
					field.getFields().add(cur);
				}
			}
			else if (current instanceof NativeElement) {
				NativeElement el = (NativeElement)current;
				String type = el.getAttribute("type");
				if (type != null && type.endsWith("PrimitiveField")) {
					PrimitiveField cur = PrimitiveFieldFactory.generatePrimitiveField(type);
					String prim_id = el.getAttribute("id");
					String prim_name = el.getNodeName();
					if ("prim".equals(name))
						name = el.getAttribute("name");
					
					cur.setId(Integer.valueOf(prim_id));
					cur.setName(prim_name);
					cur.setField(field);
					field.getPrimitiveField().add(cur);
				}
			}
		}
		
		return field;
	}
	
	public static class LookupDataContainer extends HashMap<String, LookupData> {
		
		private static final long serialVersionUID = 1L;
		
		private String fieldName;
		
		public LookupDataContainer() {
			super();
		}
		       
		@Override
		public LookupData put(String key, LookupData value) {
		       return super.put(key.toLowerCase(), value);
		}
		
		@Override
		public LookupData get(Object key) {
			if (key instanceof String)
				return super.get(((String)key).toLowerCase());
			else
				return null;
		}
       
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		   
		public String getFieldName() {
			return fieldName;
		}
		
		public LookupData find(String structureID) {
			String probableKey = fieldName + "_" + structureID + "lookup";
		 
			return get(probableKey);
		}
	}

}
