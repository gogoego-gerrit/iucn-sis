package org.iucn.sis.client.panels.references;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Reference;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.utils.ClientDocumentUtils;
import com.solertium.lwxml.shared.GWTResponseException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * ReferenceEditor.java
 * 
 * Used to view/edit a reference
 * 
 * @author carl.scott
 * 
 */
public class ReferenceEditor extends Window implements DrawsLazily {

	private ComboBox<ReferenceTypeModel> typeChooser;
	private Reference reference;

	private LayoutContainer formArea;
	private ArrayList<TextBox> registeredFields;

	private boolean canEdit;

	private boolean citationComplete;
	private String citation;
	private HTML citationHTML;
	private boolean changedType;
	private ListBox submissionType;

	/**
	 * Constructor. Takes a reference object, or null if you intend to create a
	 * new reference.
	 * 
	 * @param reference
	 *            the reference.
	 */
	public ReferenceEditor(final Reference reference) {
		this(reference, reference == null ? true : AuthorizationCache.impl.hasRight(
				SimpleSISClient.currentUser, AuthorizableObject.WRITE, reference));
	}

	public ReferenceEditor(final Reference reference, final boolean canEdit) {
		super();
		setLayout(new FillLayout());
		setClosable(true);
		setSize(600, 400);
		setIconStyle("icon-book");
		setScrollMode(Scroll.AUTO);

		this.reference = reference;
		this.canEdit = canEdit;
		changedType = false;

		registeredFields = new ArrayList<TextBox>();

		formArea = new LayoutContainer();
		formArea.setLayout(new FlowLayout(0));
		formArea.setScrollMode(Scroll.AUTO);

		typeChooser = new ComboBox<ReferenceTypeModel>();
		typeChooser.setForceSelection(true);
		
		show();
	}
	
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	private void open() {
		super.show();
		center();
	}
	
	public void draw(final DrawsLazily.DoneDrawingCallback callback) {
		final NativeDocument typesDoc = SimpleSISClient.getHttpBasicNativeDocument();
		typesDoc.get(UriBase.getInstance().getReferenceBase() +"/refsvr/types", new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load reference types, please try again later.");
			}
			public void onSuccess(String result) {
				if (reference != null)
					Debug.println("Loading reference of type {0}", reference.getType());
				
				int articleIndex = -1;
				
				ReferenceTypeModel selected = null;
				
				final ListStore<ReferenceTypeModel> store = new ListStore<ReferenceTypeModel>();
				final NativeNodeList types = typesDoc.getDocumentElement().getChildNodes();
				for (int i = 0; i < types.getLength(); i++) {
					final NativeNode current = types.item(i);
					if (current.getNodeName().equalsIgnoreCase("type")) {
						final String value = current.getTextContent();
						final ReferenceTypeModel model = new ReferenceTypeModel(value);
						
						if (reference != null && reference.getType().equalsIgnoreCase(value))
							selected = model;
						
						if ("journal article".equalsIgnoreCase(value))
							articleIndex = store.getModels().size();
						
						store.add(model);
					}
				}
				
				typeChooser.setStore(store);
				
				if (selected != null)
					typeChooser.setValue(selected);
				
				typeChooser.addSelectionChangedListener(new SelectionChangedListener<ReferenceTypeModel>() {
					public void selectionChanged(SelectionChangedEvent<ReferenceTypeModel> se) {
						changedType = true;
						setCitationWithNewType();
						updateFormArea(se.getSelectedItem().getValue());
					}
				});
				
				build(selected, callback);
			}
		});
	}
	
	public void build(ReferenceTypeModel initValue, DrawsLazily.DoneDrawingCallback callback) {
		Button save = new Button();
		save.setIconStyle("icon-save");
		save.setText("Save and Close");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				onSave();
			}
		});

		Button cancel = new Button();
		cancel.setIconStyle("icon-cancel");
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		});

		Button delete = new Button();
		delete.setIconStyle("icon-trash");
		delete.setText("Delete");
		delete.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				WindowUtils.confirmAlert("Confirm Delete", "Are you sure you want to delete this reference?",
						new WindowUtils.MessageBoxListener() {
							@Override
							public void onNo() {
							}

							@Override
							public void onYes() {
								onDelete();
							}
						});
			}
		});

		final ToolBar header = new ToolBar();
		header.add(new Button("Reference Type:"));
		header.add(typeChooser);
		header.add(new SeparatorToolItem());
		if (canEdit)
			header.add(save);
		header.add(cancel);

		if (reference != null && canEdit) {
			header.add(new SeparatorToolItem());
			header.add(delete);
		}

		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(header, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
		container.add(formArea, new BorderLayoutData(LayoutRegion.CENTER));
		
		add(container);
		
		if (initValue == null)
			callback.isDrawn();
		else
			updateFormArea(initValue.getValue(), callback);
	}

	private void addCitation(int row, FlexTable table) {
		HTML html = new HTML("Citation:&nbsp;");
		html.setWordWrap(false);

		if (citation == null)
			citation = "";

		citationHTML = new HTML(citation);
		citationHTML.setWidth("100%");
		html.setWordWrap(true);

		final HorizontalPanel hp = new HorizontalPanel();
		hp.add(html);
		hp.add(new HTML());
		hp.add(citationHTML);
		hp.setCellWidth(html, (table.getWidget(0, 0).getOffsetWidth() - 15) + "px");
		hp.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_RIGHT);
		hp.setCellHorizontalAlignment(citationHTML, HasHorizontalAlignment.ALIGN_LEFT);
		final TextArea area = new TextArea();
		VerticalPanel bar = new VerticalPanel();
		final VerticalPanel vp = new VerticalPanel();

		Button generate = new Button("Generate", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				if (reference == null)
					reference = new Reference();
				putFieldsIntoReference();

				reference.generateCitation();
				citationHTML.setHTML(reference.getCitation());
				area.setText(reference.getCitation());
				citation = reference.getCitation();
				if (reference.isCitationValid() && !citationComplete) {
					citationComplete = reference.isCitationValid();
					displayInvalid(hp, 1, vp);
				} else if (citationComplete && !reference.isCitationValid()) {
					citationComplete = reference.isCitationValid();
					displayInvalid(hp, 1, vp);
				}
			}
		});
		generate.setSize("65px", "17px");
		final Button edit = new Button("Edit");
		edit.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if (edit.getText().equalsIgnoreCase("edit")) {
					edit.setText("Done");
					area.setText(citationHTML.getHTML());
					area.setWidth("100%");
					hp.remove(citationHTML);
					hp.insert(area, 2);
					hp.setCellHorizontalAlignment(area, HasHorizontalAlignment.ALIGN_LEFT);
				} else {
					edit.setText("Edit");
					citationHTML.setHTML(area.getText());
					hp.remove(area);
					hp.insert(citationHTML, 2);
					hp.setCellHorizontalAlignment(citationHTML, HasHorizontalAlignment.ALIGN_LEFT);
				}

			}

		});
		area.addFocusListener(new FocusListener() {

			public void onFocus(Widget sender) {
			}

			public void onLostFocus(Widget sender) {
				citation = area.getText();
			}

		});
		edit.setSize("65px", "17px");
		bar.add(generate);
		bar.setSpacing(3);
		bar.add(edit);
		hp.add(bar);
		hp.setCellHorizontalAlignment(bar, HasHorizontalAlignment.ALIGN_RIGHT);
		hp.setWidth("100%");

		vp.setSpacing(15);
		vp.add(hp);
		vp.setBorderWidth(1);

		vp.setWidth("100%");

		if (!citationComplete)
			displayInvalid(hp, 1, vp);

		formArea.add(vp);
		formArea.layout();

	}

	/**
	 * Helper function that adds a field to the form area
	 * 
	 * @param label
	 *            the friendly name
	 * @param name
	 *            the field id
	 * @param value
	 *            the field value
	 * @param row
	 *            the row of the table to use
	 * @param table
	 *            the table to add to
	 */
	private void addField(String label, String name, String value, int row, FlexTable table) {

		if (name.equalsIgnoreCase("citation")) {
			citation = value;
		} else if (name.equalsIgnoreCase("citation_complete")) {
			citationComplete = Reference.isCitationValid(value);
		} else if (name.equalsIgnoreCase("submission_type")) {
			if (!changedType) {
				submissionType = new ListBox();
				for (int i = 0; i < ReferenceCitationGeneratorShared.SUBMISSION_TYPES.length; i++) {
					submissionType.addItem(ReferenceCitationGeneratorShared.SUBMISSION_TYPES[i]);
				}
				submissionType.setSelectedIndex(0);
			}
			HTML html = new HTML(label + ":&nbsp;");
			html.setWordWrap(false);

			table.setWidget(row, 0, html);
			table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);

			if (submissionType != null)
				table.setWidget(row, 1, submissionType);
		} else {
			HTML html = new HTML(label + ":&nbsp;");
			html.setWordWrap(false);

			TextBox input = new TextBox();
			input.setName(name);
			input.setText(value);
			input.setVisibleLength(50);

			registeredFields.add(input);

			table.setWidget(row, 0, html);
			table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);

			table.setWidget(row, 1, input);
		}

	}

	protected void afterDelete() {
		WindowUtils.infoAlert("Successfully Deleted", "This reference has been successfully deleted!");
		hide();
	}

	private void displayInvalid(HorizontalPanel hp, int index, VerticalPanel vp) {

		if (!citationComplete) {
			HTML html = new HTML("*");
			html.addStyleName("red-menu");
			hp.remove(index);
			hp.insert(html, index);
			hp.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_LEFT);
			hp.setCellWidth(html, "10px");
			html = new HTML("* Citation possibly incomplete");
			html.addStyleName("red-menu");
			vp.add(html);
		} else {
			hp.remove(index);
			HTML html = new HTML();
			hp.insert(html, index);
			hp.setCellWidth(html, "10px");
			vp.remove(vp.getWidgetCount() - 1);
		}
	}

	/**
	 * Deletes the reference.
	 */
	public void onDelete() {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.delete(UriBase.getInstance().getReferenceBase() +"/refsvr/reference/" + reference.getReferenceID(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				if (caught instanceof GWTResponseException && ((GWTResponseException)caught).getCode() == 417)
					WindowUtils.errorAlert("Reference In Use", "This reference cannot be deleted "
							+ "because it is being used in some assessments. Please remove it from "
							+ "these assessments before deleting it.");
				else
					WindowUtils.errorAlert("Error Deleting", "Could not delete this reference. "
							+ "Please check you are properly connected to the SIS server and try again.");
			}

			public void onSuccess(String result) {
				afterDelete();
			}
		});
	}

	/**
	 * Saves this references by POSTing it to the submit resource.
	 */
	private void onSave() {
		String validate = validate();
		if (validate != null) {
			WindowUtils.errorAlert("Error", "Please correct the following fields: <p> " + validate + "</p>");
			return;
		}

		if (reference == null)
			reference = new Reference();
		putFieldsIntoReference();

		reference.generateCitation();

		if (citation.equals("")) {
			citation = reference.getCitation();
			citationComplete = reference.isCitationValid();
			save();
		} else if (!citation.equalsIgnoreCase(reference.getCitation())) {
			String message = 
				"The citation does not accurately reflect all of the entered information.  "
				+ "Would you like to update the citation before saving?  The new citation would be: "
				+ "<br/><br/> " + reference.getCitation();
			WindowUtils.confirmAlert("Update Citation", message, new WindowUtils.MessageBoxListener() {
				public void onYes() {
					citation = reference.getCitation();
					citationComplete = reference.isCitationValid();
				}
				public void onNo() {
					save();
				}
			});
		} else {
			save();
		}

	}

	/**
	 * Called when saving is successful. Can be overridden. By default, it
	 * 
	 */
	public void onSaveSuccessful(Reference returnedRef) {
		if (reference != null)
			reference.setReferenceID(returnedRef.getReferenceID());
		else
			reference = returnedRef;

		hide();
	}

	/**
	 * places all text boxes, the submission type, and citation, and
	 * citation_complete into the reference which is currently displayed on the
	 * screen
	 */
	private void putFieldsIntoReference() {
		reference.setType(typeChooser.getValue().getValue());

		for (TextBox current : registeredFields)
			Reference.addField(reference, current.getName(), current.getValue());

		if (submissionType != null)
			Reference.addField(reference, "submission_type", submissionType.getItemText(submissionType.getSelectedIndex()));

		reference.setCitation(citationHTML.getHTML());
	}

	protected void save() {
		reference.setCitation(citation);
		reference.setCitationComplete(citationComplete);

		final String xml = "<references>" + reference.toXML() + "</references>";

		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getReferenceBase() +"/refsvr/submit", xml, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Failure", "Save Failed Unexpectedly.");
			}

			public void onSuccess(String result) {
				final Reference returnedRef = Reference.fromXML(document.getDocumentElement().getElementByTagName(
				"reference"));
				onSaveSuccessful(returnedRef);
			}
		});
	}

	private void setCitationWithNewType() {
		if (citationHTML != null)
			citation = citationHTML.getHTML();
		citationComplete = false;
	}

	/**
	 * Updates the form area based on the given type
	 * 
	 * @param type
	 *            the type of reference
	 */
	private void updateFormArea(String type) {
		updateFormArea(type, new DrawsLazily.DoneDrawingWithNothingToDoCallback());
	}
	
	private void updateFormArea(String type, final DrawsLazily.DoneDrawingCallback callback) {
		formArea.removeAll();
		
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getReferenceBase() + "/refsvr/type/" + type, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.isDrawn();
			}
			public void onSuccess(String result) {
				int currentRow = 0;
				
				final FlexTable table = new FlexTable();
				table.setCellSpacing(2);
				
				final Map<String, String> data;
				if (reference == null)
					data = new HashMap<String, String>();
				else
					data = reference.toMap();
				
				final NativeNodeList fields = document.getDocumentElement().getElementsByTagName("field");
				for (int i = 0; i < fields.getLength(); i++) {
					NativeElement current = fields.elementAt(i);
					if (current.getNodeName().equalsIgnoreCase("field")) {
						String fieldName = current.getAttribute("name");
						String value = "";
						if (data.get(fieldName) != null)
							value = data.get(fieldName);

						addField(current.getAttribute("label"), fieldName, value, currentRow++, table);
					}
				}
				if (changedType) {
					citationComplete = false;
					changedType = false;
				}

				formArea.add(table);
				addCitation(currentRow, table);
				formArea.layout();
				
				callback.isDrawn();
			}
		});
	}

	/**
	 * TODO: implement Determines if the user's input is valid for the given
	 * fields
	 * 
	 * @return null if input is valid, an error message otherwise, listing
	 *         fields that are invalid
	 */
	public String validate() {
		return null;
	}
	
	private static class ReferenceTypeModel extends BaseModelData {
		
		private static final long serialVersionUID = 1L;
		
		public ReferenceTypeModel(String value) {
			super();
			set("text", value);
			set("value", value);
		}
		
		public String getValue() {
			return get("value");
		}
		
	}

}
