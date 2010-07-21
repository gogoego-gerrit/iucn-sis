package org.iucn.sis.client.displays;

import java.util.ArrayList;
import java.util.Iterator;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.referenceui.ReferenceCache;
import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.client.referenceui.Referenceable;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.utilities.clipboard.Clipboard;
import org.iucn.sis.client.utilities.clipboard.UsesClipboard;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.DisplayData;
import org.iucn.sis.shared.acl.InsufficientRightsException;
import org.iucn.sis.shared.data.DefinitionCache;
import org.iucn.sis.shared.data.DefinitionPanel;
import org.iucn.sis.shared.data.assessments.AssessmentUtilFactory;
import org.iucn.sis.shared.data.assessments.Note;
import org.iucn.sis.shared.data.assessments.NotesCache;
import org.iucn.sis.shared.structures.SISRelatedStructures;
import org.iucn.sis.shared.structures.SISStructureCollection;
import org.iucn.sis.shared.structures.Structure;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * Display.java
 * 
 * Holds references to data Structure objects, with this abstraction allowing
 * for a consistent way to store data and get their values. Instantiating
 * classes differ by how they display their data to the user, and implement this
 * into the show() function
 * 
 * @author carl.scott
 */
public abstract class Display implements Referenceable {

	static class DisplayStructureCollection extends ArrayList {

		public DisplayStructureCollection() {
			super();
		}

		public Structure getStructure(int index) {
			return (Structure) get(index);
		}

		@Override
		public Iterator iterator() {
			return listIterator();
		}

	}

	protected Panel dockPanel;
	// UI Display Vars
	protected DisplayStructureCollection myStructures; // List of Structures
	protected ComplexPanel displayPanel; // The panel to add a display to
	protected HorizontalPanel iconPanel;
	protected Image infoIcon = null;
	protected Image helpIcon = null;
	protected Image notesIcon = null;
	protected Image refIcon = null;
	protected Image viewRefIcon = null;

	protected Menu optionsMenu = null;
	// Data Vars
	protected String structure = ""; // what type of input element is this?
	protected String description; // the input prompt
	protected String canonicalName; // the canonical name - unique identifier!
	protected String classOfService; // the class of service
	protected Object data; // data specific to the field (string, hashmap,
	// arraylist)
	protected String groupName; // for radio buttons, group 'em, depricated
	protected String location; // for complex structures
	protected String displayID; // id num for the field
	protected String associatedFieldId; // id this field is associated with

	protected ArrayList referenceIds;

	protected int dominantStructureIndex = 0;

	/**
	 * Creates a new Display by instantiating an ArrayList of Structures
	 */
	public Display() {
		this("", "", null, "", "", "", "", "");
	}

	public Display(DisplayData displayData) {
		this(displayData.getStructure(), displayData.getDescription(), displayData.getData(), "", displayData
				.getDisplayId(), displayData.getCanonicalName(), displayData.getClassOfService(), "");

		this.location = displayData.getLocation();
		this.referenceIds = displayData.getReferences();
	}

	public Display(String struct, String descript, Object data, String group, String displayID, String canonicalName,
			String classOfService, String associate) {
		myStructures = new DisplayStructureCollection();

		this.structure = struct;
		this.description = descript;
		this.data = data;
		this.groupName = group;
		this.displayID = displayID;
		this.canonicalName = canonicalName;
		this.classOfService = classOfService;
		this.associatedFieldId = associate;
		this.referenceIds = new ArrayList();
	}

	public void addReferenceId(String id) {
		referenceIds.add(id);
	}

	public void addReferences(ArrayList<ReferenceUI> references, GenericCallback<Object> callback) {
		ReferenceCache.getInstance().addReferences(AssessmentCache.impl.getCurrentAssessment().getAssessmentID(),
				references);

		ReferenceCache.getInstance().addReferencesToAssessmentAndSave(references, canonicalName, callback);
	}

	public void addStructure(Structure structureToAdd) {
		myStructures.add(structureToAdd);
	}

	private VerticalPanel buildDefintionPanel() {
		VerticalPanel panelContainer = new VerticalPanel();
		panelContainer.setSpacing(3);

		ArrayList<String> found = new ArrayList<String>();

		String lowerCaseDesc = description.toLowerCase();

		for (String curDefinable : DefinitionCache.impl.getDefinables()) {
			if (lowerCaseDesc.indexOf(curDefinable) >= 0 && !found.contains(curDefinable))
				found.add(curDefinable);
		}

		if (found.size() == 0)
			panelContainer.add(new HTML("No definable terms were found."));
		else {
			final DefinitionPanel dPanel = new DefinitionPanel();

			panelContainer.add(new HTML("Definable terms:"));
			for (final String curDef : found) {
				HTML curDefHTML = new HTML(curDef);
				curDefHTML.addStyleName("SIS_HyperlinkLookAlike");
				curDefHTML.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						dPanel.updateContent(curDef);
					}
				});

				panelContainer.add(curDefHTML);
			}

			panelContainer.add(dPanel);
		}
		return panelContainer;
	}

	public void checkSecurity() {
		// ModuleController.getSecurityManager().setUserDefaultDisplayVisibility(
		// this, SecurityManager.DISPLAY_LEVEL);
	}

	public void disableStructures() {
		for (int i = 0; i < myStructures.size(); i++)
			myStructures.getStructure(i).disable();
	}

	public void enableStructures() {
		for (int i = 0; i < myStructures.size(); i++)
			myStructures.getStructure(i).enable();
	}

	protected abstract Widget generateContent(boolean viewOnly);

	public String getCanonicalName() {
		return canonicalName;
	}

	public String getClassOfService() {
		return classOfService;
	}

	/**
	 * Builds the clipboard context menu.
	 * 
	 * @param clipList
	 *            the list of structures *that implement UsesClipboard*. You
	 *            should NEVER put an object in this list that does not
	 *            implement UsesClipboard.
	 * @see getStructuresThatUseClipboard()
	 * @return the menu the context menu
	 */
	private Menu getClipboardMenu(ArrayList clipList) {
		Menu menu = new Menu();

		MenuItem open = new MenuItem();
		open.setText("Open Clipboard");
		open.setIconStyle("icon-open-folder");
		open.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				Clipboard.getInstance().show();
			}
		});

		MenuItem copyFrom = new MenuItem();
		copyFrom.setIconStyle("icon-copy");
		copyFrom.setText("Copy text from...");
		{
			Menu subMenu = new Menu();
			for (int i = 0; i < clipList.size(); i++) {
				try {
					final UsesClipboard struct = (UsesClipboard) clipList.get(i);
					MenuItem item = new MenuItem();
					item.setText(((Structure) clipList.get(i)).getDescription());
					item.addSelectionListener(new SelectionListener<MenuEvent>() {
						@Override
						public void componentSelected(MenuEvent ce) {
							struct.copyToClipboard();
						}
					});
					subMenu.add(item);
				} catch (Exception e) {
					continue;
				}
			}

			if (subMenu.getItemCount() == 0) {
				MenuItem empty = new MenuItem();
				empty.setText("[none available]");
				subMenu.add(empty);
			}

			copyFrom.setSubMenu(subMenu);
		}

		MenuItem pasteTo = new MenuItem();
		pasteTo.setIconStyle("icon-paste");
		pasteTo.setText("Paste text to...");
		{
			Menu subMenu = new Menu();

			if (Clipboard.getInstance().isEmpty()) {
				MenuItem empty = new MenuItem();
				empty.setText("[clipboard empty]");
				subMenu.add(empty);
			} else {
				for (int i = 0; i < clipList.size(); i++) {
					try {
						final UsesClipboard struct = (UsesClipboard) clipList.get(i);
						MenuItem item = new MenuItem();
						item.setText(((Structure) clipList.get(i)).getDescription());
						item.addSelectionListener(new SelectionListener<MenuEvent>() {
							@Override
							public void componentSelected(MenuEvent ce) {
								Clipboard.getInstance().pasteConditions(new Clipboard.ClipboardPasteCallback() {
									@Override
									public void onPaste(ArrayList items) {
										struct.pasteFromClipboard(items);
									}
								});
							}
						});
						subMenu.add(item);
					} catch (Exception e) {
						continue;
					}
				}
			}

			if (subMenu.getItemCount() == 0) {
				MenuItem empty = new MenuItem();
				empty.setText("[none available]");
				subMenu.add(empty);
			}

			pasteTo.setSubMenu(subMenu);
		}

		menu.add(open);
		menu.add(copyFrom);
		menu.add(pasteTo);

		return menu;
	}

	public String getDescription() {
		return description;
	}

	public int getDominantStructureIndex() {
		if (dominantStructureIndex < 0 || dominantStructureIndex > myStructures.size())
			return this.dominantStructureIndex;
		else
			return 0;
	}

	/**
	 * Returns the ID of ths Trackable object implements Trackable
	 * 
	 * @return the id
	 */
	public String getID() {
		return displayID;
	}

	public ArrayList getMyWidgets() {
		ArrayList retWidgets = new ArrayList();
		for (int i = 0; i < this.myStructures.size(); i++) {
			retWidgets.add(this.myStructures.getStructure(i).generate());
		}
		SysDebugger.getInstance().println(
				"Returned " + myStructures.size() + " widgets to show for " + this.description);
		return retWidgets;
	}

	public Widget getNoPermissionPanel() {
		return new SimplePanel();
	}

	public ArrayList getReferenceIds() {
		return referenceIds;
	}

	public ArrayList<ReferenceUI> getReferencesAsList() {
		return AssessmentCache.impl.getCurrentAssessment().getReferences(canonicalName);
	}

	public ArrayList getStructures() {
		return this.myStructures;
	}

	/**
	 * Looks in the display's structures and locates structures that implement
	 * UsesClipboard
	 * 
	 * @return the list
	 */
	private ArrayList getStructuresThatUseClipboard(ArrayList structures) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < structures.size(); i++) {
			if (structures.get(i) instanceof SISRelatedStructures) {
				if (((SISRelatedStructures) structures.get(i)).getDominantStructure() instanceof UsesClipboard)
					list.add(((SISRelatedStructures) structures.get(i)).getDominantStructure());

				list.addAll(getStructuresThatUseClipboard(((SISRelatedStructures) structures.get(i))
						.getDependantStructures()));
			} else if (structures.get(i) instanceof SISStructureCollection) {
				list
						.addAll(getStructuresThatUseClipboard(((SISStructureCollection) structures.get(i))
								.getStructures()));
			} else if (structures.get(i) instanceof UsesClipboard)
				list.add(structures.get(i));
		}
		return list;
	}

	/********* DISPLAY VISIBILITY ************/

	public void hideStructures() {
		iconPanel.setVisible(false);
		for (int i = 0; i < myStructures.size(); i++)
			myStructures.getStructure(i).hide();
	}

	public void onReferenceChanged(GenericCallback<Object> callback) {
		try {
			AssessmentUtilFactory.getSaveUtils().saveAssessment(AssessmentCache.impl.getCurrentAssessment(), callback);
		} catch (InsufficientRightsException e) {
			WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
					+ "permission to modify this assessment. The changes you " + "just made will not be saved.");
		}
	}

	protected void openEditViewNotesPopup(final String cName, final AsyncCallback<String> wayback) {
		final Window s = WindowUtils.getWindow(true, true, "Notes for " + cName);
		s.setLayoutOnChange(true);

		FillLayout layout = new FillLayout(Orientation.VERTICAL);

		final VerticalPanel panelAdd = new VerticalPanel();
		panelAdd.setSpacing(3);

		panelAdd.add(new HTML("Add Note: "));

		final TextArea area = new TextArea();
		area.setSize("400", "75");
		panelAdd.add(area);

		Button save = new Button("Add Note");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (area.getText().equalsIgnoreCase("")) {
					WindowUtils.errorAlert("Data Error", "Must enter note body.");
				} else {
					Note currentNote = new Note();
					currentNote.setBody(area.getText());
					currentNote.setCanonicalName(cName);

					NotesCache.impl.addNote(cName, currentNote, AssessmentCache.impl.getCurrentAssessment(),
							new GenericCallback<String>() {
								public void onFailure(Throwable caught) {
								}

								public void onSuccess(String result) {
									wayback.onSuccess(result);
									s.hide();
								}
							});
				}

			}
		});
		Button close = new Button("Close");
		close.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				s.hide();
			}
		});

		panelAdd.add(save);
		panelAdd.add(close);

		// final NativeDocument doc =
		// SimpleSISClient.getHttpBasicNativeDocument();
		String type = AssessmentCache.impl.getCurrentAssessment().getType();
		String url = "/notes/" + type;
		url += "/" + AssessmentCache.impl.getCurrentAssessment().getAssessmentID();
		url += "/" + cName;

		if (type.equals(BaseAssessment.USER_ASSESSMENT_STATUS))
			url += "/" + SimpleSISClient.currentUser.username;

		final ArrayList<Note> notes = NotesCache.impl.getNotesForCurrentAssessment(cName);
		if (notes == null || notes.size() == 0) {
			s.add(new HTML("<div style='padding-top:10px';background-color:grey>"
					+ "<b>There are no notes for this field.</b></div>"));
			s.add(panelAdd);
		} else {

			ContentPanel eBar = new ContentPanel();
			eBar.setHeight(200);

			FillLayout fl = new FillLayout();
			fl.setOrientation(Orientation.VERTICAL);
			eBar.setLayout(fl);
			eBar.setLayoutOnChange(true);
			eBar.setScrollMode(Scroll.AUTO);

			for (Iterator iter = notes.listIterator(); iter.hasNext();) {

				final Note current = (Note) iter.next();
				Image deleteNote = new Image("images/icon-note-delete.png");
				deleteNote.setTitle("Delete Note");
				deleteNote.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						NotesCache.impl.deleteNote(cName, current, AssessmentCache.impl.getCurrentAssessment(),
								new GenericCallback<String>() {
									public void onFailure(Throwable caught) {

									};

									public void onSuccess(String result) {
										s.hide();
										if (notes.size() - 1 == 0)
											notesIcon.setUrl("images/icon-note-grey.png");
									};
								});
					}
				});

				LayoutContainer a = new LayoutContainer();
				RowLayout innerLayout = new RowLayout();
				innerLayout.setOrientation(Orientation.HORIZONTAL);
				// innerLayout.setSpacing(10);
				a.setLayout(innerLayout);
				a.setLayoutOnChange(true);
				// a.setWidth(400);
				a.add(deleteNote, new RowData());
				a.add(new HTML("<b>" + current.getUser() + " [" + current.getDate() + "]</b>  --" + current.getBody()),
						new RowData(1d, 1d));// );

				eBar.add(a, new RowData(1d, 1d));
			}
			s.add(eBar);
			s.add(panelAdd);
		}

		s.setSize(500, 400);
		s.show();
		s.center();
	}

	private void rebuildIconPanel() {
		iconPanel.clear();
		iconPanel.setSpacing(5);

		/* This code adds the Clipboard icon to the display mini-menuCS */
		final ArrayList clipList = getStructuresThatUseClipboard(myStructures);
		if (!clipList.isEmpty()) {
			Button iconButton = new Button();
			iconButton.setIconStyle("icon-paste");
			iconButton.setToolTip(new ToolTipConfig("Clipboard",
					"You can use the clipboard to copy and paste text <br/>"
							+ "from one field to another in SIS, where supported. <br/>"
							+ "The clipboard can hold multiple pieces of text at a time."));
			iconButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					getClipboardMenu(clipList).show(ce.getComponent());
				}
			});

			iconPanel.add(iconButton);
		}
		iconPanel.add(notesIcon);
		// iconPanel.add(new Label("Notes"));

		if (AssessmentCache.impl.getCurrentAssessment().getReferences(canonicalName).size() == 0)
			refIcon.setUrl("images/icon-book-grey.png");
		else
			refIcon.setUrl("images/icon-book.png");

		iconPanel.add(refIcon);
		// iconPanel.add(new Label("References"));
		iconPanel.add(helpIcon);
		// iconPanel.add(new Label("Help"));
	}

	public void removeReferences(ArrayList<ReferenceUI> references, GenericCallback<Object> callback) {
		int removed = 0;
		for (int i = 0; i < references.size(); i++)
			if (AssessmentCache.impl.getCurrentAssessment().removeReference(references.get(i), canonicalName))
				removed++;

		if (removed > 0) {
			try {
				AssessmentUtilFactory.getSaveUtils().saveAssessment(AssessmentCache.impl.getCurrentAssessment(), callback);
			} catch (InsufficientRightsException e) {
				WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
						+ "permission to modify this assessment. The changes you " + "just made will not be saved.");
			}
		}
	}

	public void removeStructures() {
		for (int i = 0; i < displayPanel.getWidgetCount(); i++)
			displayPanel.getWidget(i).removeFromParent();
	}

	/*
	 * public void revert() { for (int i = 0; i < myStructures.size(); i++) {
	 * ((Structure)myStructures.get(i)).revert(); } }
	 * 
	 * public void save() { for (int i = 0; i < myStructures.size(); i++) {
	 * ((Structure)myStructures.get(i)).save(); } }
	 */

	public void setCanonicalName(String canonicalName) {
		this.canonicalName = canonicalName;
	}

	public void setClassOfService(String classOfService) {
		this.classOfService = classOfService;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDisplayID(String displayID) {
		this.displayID = displayID;
	}

	public void setDominantStructureIndex(int index) {
		this.dominantStructureIndex = index;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setReferenceIds(ArrayList referenceIds) {
		this.referenceIds = referenceIds;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	protected void setupIconPanel() {
		if (iconPanel != null)
			return;

		iconPanel = new HorizontalPanel();
		iconPanel.setStylePrimaryName("SIS_iconPanel");
		iconPanel.setSpacing(1);

		if (canonicalName == null || canonicalName.equals(""))
			return;

		final ClickListener noteListener = new ClickListener() {
			public void onClick(Widget sender) {
				openEditViewNotesPopup(canonicalName, new AsyncCallback<String>() {
					public void onSuccess(String result) {
						notesIcon.setUrl("images/icon-book.png");
					}
					public void onFailure(Throwable caught) {
					}
				});
			}
		};

		final ArrayList refs = AssessmentCache.impl.getCurrentAssessment().getReferences(canonicalName);

		if (refIcon == null)
			refIcon = new Image();

		refIcon.setStyleName("SIS_iconPanelIcon");

		refIcon.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				final Window s = WindowUtils.getWindow(true, true, "Add a references to " + canonicalName);
				s.setIconStyle("icon-book");
				s.setLayout(new FillLayout());

				GenericCallback<Object> callback = new GenericCallback<Object>() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Error!", "Error committing changes to the "
								+ "server. Ensure you are connected to the server, then try " + "the process again.");
					}

					public void onSuccess(Object result) {
						rebuildIconPanel();
					}
				};
				ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(Display.this,
						callback, callback);

				s.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);

				s.setSize(850, 550);
				s.show();
				s.center();
			}
		});
		helpIcon = new Image("images/icon-help.png");
		helpIcon.setStyleName("SIS_iconPanelIcon");
		helpIcon.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				Window s = WindowUtils.getWindow(true, false, "Definitions for " + canonicalName);
				s.setScrollMode(Scroll.AUTO);
				s.add(buildDefintionPanel());
				s.setSize(400, 400);

				s.show();
				s.center();
			}
		});

		ArrayList<Note> notes = NotesCache.impl.getNotesForCurrentAssessment(canonicalName);

		if (notes == null || notes.size() == 0) {
			notesIcon = new Image("images/icon-note-grey.png");
		} else {
			notesIcon = new Image("images/icon-note.png");
		}

		notesIcon.setStyleName("SIS_iconPanelIcon");
		notesIcon.addClickListener(noteListener);
		rebuildIconPanel();
		
		if (refIcon != null) {
			if (AssessmentCache.impl.getCurrentAssessment().getReferences(canonicalName).size() == 0)
				refIcon.setUrl("images/icon-book-grey.png");
			else
				refIcon.setUrl("images/icon-book.png");
		}
	}

	/**
	 * Display in the proper form
	 * 
	 * @return the panel
	 */
	public Widget showDisplay() {
		return this.showDisplay(false);
	}

	/**
	 * Sets the protected class member dockPanel to be the desired Panel. This method is
	 * expected to return the dockPanel object.
	 *
	 * NOTE: If you override this method, MAKE SURE YOU INVOKE setupIconPanel() before you
	 * add the iconPanel.
	 *  
	 * @param viewOnly - whether or not to build the Panel in view only mode
	 * @return the dockPanel class member
	 */
	protected Widget showDisplay(boolean viewOnly) {
		try {
			setupIconPanel();
			
			DockPanel myPanel = new DockPanel();
			myPanel.setSize("100%", "100%");
			myPanel.clear();

			myPanel.add(iconPanel, DockPanel.NORTH);
			myPanel.add(generateContent(viewOnly), DockPanel.CENTER);
			dockPanel = myPanel;
		} catch (Exception e) {
			dockPanel = new VerticalPanel();
			e.printStackTrace();
		}

		return dockPanel;
	}

	public void showStructures() {
		iconPanel.setVisible(true);
		for (int i = 0; i < myStructures.size(); i++)
			myStructures.getStructure(i).show();
	}

	public Widget showViewOnly() {
		return showDisplay(true);
	}

	public abstract String toThinXML();

	/**
	 * Return the XML version of a Display
	 * 
	 * @return xml representation of a display
	 */
	public abstract String toXML();

}// class Display
