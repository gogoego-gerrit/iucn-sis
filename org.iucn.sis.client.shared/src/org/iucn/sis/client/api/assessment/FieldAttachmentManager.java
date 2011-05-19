package org.iucn.sis.client.api.assessment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.FieldAttachment;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class FieldAttachmentManager extends BasicWindow implements DrawsLazily {
	
	private final Assessment assessment;
	private final ListStore<FieldAttachmentModelData> store;

	public FieldAttachmentManager() {
		this(AssessmentCache.impl.getCurrentAssessment());
	}
	
	public FieldAttachmentManager(Assessment assessment) {
		super("Assessment Attachment Manager", "icon-attachment");
		setLayout(new FillLayout());
		setLayoutOnChange(true);
		setSize(750, 500);
		
		this.assessment = assessment;
		this.store = new ListStore<FieldAttachmentModelData>();
		store.setStoreSorter(new StoreSorter<FieldAttachmentModelData>(new PortableAlphanumericComparator()));
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		removeAll();
		
		WindowUtils.showLoadingAlert("Finding attachments...");
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(getBaseUri(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("attachment");
				
				store.removeAll();
				for (int i = 0; i < nodes.getLength(); i++)
					store.add(new FieldAttachmentModelData(FieldAttachment.fromXML(nodes.elementAt(i))));
				
				if (store.getModels().isEmpty()) {
					WindowUtils.hideLoadingAlert();	
					WindowUtils.infoAlert("There are no attachments for this assessment.");
				}
				else {
					store.sort("name", SortDir.ASC);
					
					drawGrid();
					
					WindowUtils.hideLoadingAlert();
					
					callback.isDrawn();	
				}
			}
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				WindowUtils.errorAlert("Could not load attachments, please try again later.");
			}
		});
	}
	
	private void drawGrid() {
		final GridSelectionModel<FieldAttachmentModelData> sm = new GridSelectionModel<FieldAttachmentModelData>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		
		final ToolBar bar = new ToolBar();
		bar.add(new Label("<b>Double-click the rows below to edit.</b>") {
			protected void onDisable() {
				//Do nothing.
			}
		});
		bar.add(new FillToolItem());
		bar.add(new Button("View", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				FieldAttachmentModelData item = sm.getSelectedItem();
				if (item == null) 
					return;
				
				String url = getBaseUri() + "/" + item.getModel().getId();
				
				com.google.gwt.user.client.Window.open(url, item.getModel().getName(), "");
			}
		}));
		bar.add(new Button("Download", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				FieldAttachmentModelData item = sm.getSelectedItem();
				if (item == null) 
					return;
				
				String url = getBaseUri() + "/" + item.getModel().getId() + "?download=true";
				
				com.google.gwt.user.client.Window.open(url, item.getModel().getName(), "");
			}
		}));
		bar.add(new SeparatorToolItem());
		bar.add(new Button("Replace", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final FieldAttachmentModelData item = sm.getSelectedItem();
				if (item == null) 
					return;
				
				FieldAttachmentWindow window = new FieldAttachmentWindow(assessment, null, item.getModel());
				window.addListener(Events.Hide, new Listener<WindowEvent>() {
					public void handleEvent(WindowEvent be) {
						draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
					}
				});
				window.show();
			}
		}));
		bar.add(new SeparatorToolItem());
		bar.add(new Button("Delete", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				final FieldAttachmentModelData item = sm.getSelectedItem();
				if (item == null) 
					return;
				
				int size = item.getModel().getFields().size();
				
				StringBuilder msg = new StringBuilder();
				msg.append("Are you sure you want to delete this file?");
				if (size > 1)
					msg.append(" It will also be removed from the " + size + " fields it is attached to.");
				
				WindowUtils.confirmAlert("Confirm", msg.toString(), new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
						document.delete(getBaseUri() + "/" + item.getModel().getId(), new GenericCallback<String>() {
							public void onSuccess(String result) {
								Info.display("Success", "File deleted.");
								
								store.remove(item);
								
								if (store.getModels().isEmpty())
									hide();
								else
									draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
							}
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Could not delete, please try again later.");
							}
						});
					}
				});
			}
		}));
		bar.disable();
		
		final EditorGrid<FieldAttachmentModelData> grid = new EditorGrid<FieldAttachmentModelData>(store, createColumnModel());
		grid.setSelectionModel(sm);
		grid.setClicksToEdit(ClicksToEdit.TWO);
		grid.setAutoExpandColumn("name");
		grid.setBorders(false);
		grid.addListener(Events.RowClick, new Listener<GridEvent<FieldAttachmentModelData>>() {
			public void handleEvent(GridEvent<FieldAttachmentModelData> be) {
				FieldAttachmentModelData item = grid.getSelectionModel().getSelectedItem();
				if (item == null)
					bar.disable();
				else
					bar.enable();
			}
		});
		grid.addListener(Events.AfterEdit, new Listener<GridEvent<FieldAttachmentModelData>>() {
			public void handleEvent(GridEvent<FieldAttachmentModelData> be) {
				FieldAttachmentModelData data = be.getModel();
				
				HashSet<Field> fields = new HashSet<Field>();
				AttachableFieldModelData checked = data.get("fields");
				for (String fieldName : checked.toList()) {
					Field field = assessment.getField(fieldName);
					if (field != null)
						fields.add(field);
				}
				
				FieldAttachment model = data.getModel();
				model.setName((String)data.get("name"));
				model.setPublish((Boolean)data.get("publish"));
				model.setFields(fields);
			
				final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
				document.put(getBaseUri() + "/" + model.getId(), model.toXML(), new GenericCallback<String>() {
					public void onSuccess(String result) {
						grid.getStore().commitChanges();
						Info.display("Success", "Changes saved.");
					}
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Could not save, please try again later.");
					}
				});
			}
		});
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
	}
	
	private ColumnModel createColumnModel() {
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
		
		final ColumnConfig name = new ColumnConfig("name", "File Name", 200);
		final TextField<String> nameField = new TextField<String>();
		nameField.setAllowBlank(false);
		name.setEditor(new CellEditor(nameField));
		
		list.add(name);
		
		final ColumnConfig toPublish = new ColumnConfig("publish", "To Publish?", 100);
		toPublish.setEditor(new CellEditor(new CheckBox()));
		toPublish.setRenderer(new GridCellRenderer<FieldAttachmentModelData>() {
			public Object render(FieldAttachmentModelData model,
					String property, ColumnData config, int rowIndex,
					int colIndex, ListStore<FieldAttachmentModelData> store,
					Grid<FieldAttachmentModelData> grid) {
				return Boolean.TRUE.equals(model.get(property)) ? "Yes" : "No";
			}
		});
		list.add(toPublish);
		
		final ColumnConfig fieldColumn = new ColumnConfig("fields", "Fields Attached", 300);
		
		final ListStore<AttachableFieldModelData> store = new ListStore<AttachableFieldModelData>();
		for (String fieldName : CanonicalNames.attachable)
			if (assessment.getField(fieldName) != null)
				store.add(new AttachableFieldModelData(fieldName));
		
		final AttachableFieldComboBox box = new AttachableFieldComboBox();
		box.setStore(store);
			
		final CellEditor editor = new CellEditor(box);
		editor.setCompleteOnEnter(true);
		editor.setCancelOnEsc(true);
		
		fieldColumn.setEditor(editor);
			
		list.add(fieldColumn);
		
		list.add(new ColumnConfig("version", "Version", 200));
		
		return new ColumnModel(list);
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
	
	private String getBaseUri() {
		return UriBase.getInstance().getAttachmentBase() + "/browse/assessments/" + 
			assessment.getId();
	}
	
	private static class FieldAttachmentModelData extends BaseModelData {
	
		private static final long serialVersionUID = 1L;
		
		private final FieldAttachment model;
		
		public FieldAttachmentModelData(FieldAttachment model) {
			super();
			this.model = model;
			
			set("id", model.getId());
			set("name", model.getName());
			set("publish", model.getPublish());
			
			final List<String> fieldNames = new ArrayList<String>();
			for (Field field : model.getFields())
				fieldNames.add(field.getName());
			
			set("fields", new AttachableFieldModelData(fieldNames));
			
			String author = getAuthor();
			if (author != null)
				set("version", "Version " + model.getEdits().size() + " by " + author);
			else
				set("version", "N/A");
		}
		
		private String getAuthor() {
			List<Edit> edits = new ArrayList<Edit>(model.getEdits());
			Collections.sort(edits, Collections.reverseOrder());
			
			if (edits.isEmpty())
				return null;
			else
				return edits.get(0).getUser().getDisplayableName();
		}
		
		public FieldAttachment getModel() {
			return model;
		}
		
	}
	
	public static class AttachableFieldModelData extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		private final Collection<String> fields;
		
		public AttachableFieldModelData(Collection<String> fields) {
			this(toCSV(fields));
		}
		
		public AttachableFieldModelData(String csv) {
			this.fields = new HashSet<String>();
			if (csv != null) {
				for (String value : csv.split(","))
					fields.add(value);
			}
			set("text", csv);
			set("value", csv);
		}
		
		public boolean hasValue(String value) {
			return fields.contains(value);
		}
		
		public List<String> toList() {
			return new ArrayList<String>(fields);
		}
		
		public String toCSV() {
			return toCSV(fields);
		}
		
		private static String toCSV(Collection<String> permissions) {
			StringBuilder out = new StringBuilder();
			for (Iterator<String> iter = permissions.iterator(); iter.hasNext(); ) {
				out.append(iter.next());
				out.append(iter.hasNext() ? "," : "");
			}
			return out.toString();
		}
		
		public String toString() {
			return get("text");
		}
		
	}

}
