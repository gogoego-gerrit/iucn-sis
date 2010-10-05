package org.iucn.sis.client.panels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.fx.Fx;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.util.extjs.client.WindowUtils;

/**
 * 
 * @author adam
 */
public class DockingScrollableList extends VerticalPanel {
	public interface EntryClickListener {
		public void onEntryClicked(ScrollableListEntry entry);
	}

	public interface EntryListPopulator {
		public void populateList();
	}

	private class ScrollableListEntry extends HTML {
		String name;
		String value;
		Fx fx;

		public ScrollableListEntry(String entryName, String entryValue) {
			super(entryName);

			name = entryName;
			value = entryValue;
			fx = new Fx(new FxConfig());

			addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					if (selectedEntry != -1) {
						((ScrollableListEntry) entries.get(selectedEntry))
								.removeStyleName("scrollableListEntrySelected");
						((ScrollableListEntry) entries.get(selectedEntry)).addStyleName("scrollableListEntry");
					}

					addStyleName("scrollableListEntrySelected");
					selectedEntry = entries.indexOf(this);

					if (entryClickListener != null)
						entryClickListener.onEntryClicked((ScrollableListEntry) entries.get(selectedEntry));
				}
			});
			addStyleName("scrollableListEntry");
			setHeight(entryHeight + "px");
		}

		public void fadeIn() {
			setVisible(true);
			// fx.fadeIn();
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public void slideDown() {
			// fx.move( getAbsoluteLeft(), getAbsoluteTop()-entryHeight*2 );
		}

		public void slideOffBottom() {
			// fx.slideOut(LayoutRegion.SOUTH);
			// removeFromParent();
		}

		public void slideOffTop() {
			// fx.slideOut(LayoutRegion.NORTH);
			// removeFromParent();
		}

		public void slideUp() {
			// fx.move( getAbsoluteLeft(), getAbsoluteTop()+entryHeight );
		}
	}

	/**
	 * ArrayList<ScrollableListEntry> displayable entries in the list
	 */
	private ArrayList entries;
	private Image downButton;

	private Image upButton;
	/**
	 * Index of the entry displayed at the top of the panel. Will be 0 until the
	 * number of entries is greater than the number displayed
	 */
	private int topIndex = 0;

	private int bottomIndex = -1;
	private int numToDisplay = 5;
	private int entryHeight = 20;
	private Window popupWindow;

	private VerticalPanel undockedContainer;

	private VerticalPanel listContainer;

	// private int style;
	private int selectedEntry = -1;

	private EntryListPopulator populator;

	private EntryClickListener entryClickListener = null;

	public DockingScrollableList(String title) {
		this(title, null);
	}

	public DockingScrollableList(String title, EntryListPopulator listPopulator) {
		setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		populator = listPopulator;

		popupWindow = WindowUtils.getWindow(false, false, "");
		popupWindow.setSize(250, 250);
		popupWindow.addWindowListener(new WindowListener() {
			public void shellActivated(BaseEvent be) {
				WindowManager.get().bringToFront(popupWindow);
				draw(0);
			}

			public void shellClosed(BaseEvent be) {
			}

			public void shellDeactivated(BaseEvent be) {
				try {
					popupWindow.hide();
				} catch (Exception ignored) {
				}
			}
		});
		popupWindow.addListener(Events.Resize, new Listener() {
			public void handleEvent(BaseEvent be) {
				draw(0);
			}
		});

		popupWindow.setLayout(new FillLayout());
		undockedContainer = new VerticalPanel();
		popupWindow.add(undockedContainer);
		undockedContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		RowLayout layout = new RowLayout(Orientation.VERTICAL);
		// layout.setMargin( 0 );
		// undockedContainer.setLayout(layout);

		addStyleName("scrollableList");

		entries = new ArrayList();
		downButton = new Image("images/icon-bullet-arrow-down.png");
		downButton.addStyleName("pointerCursor");
		downButton.setSize("16px", "16px");
		downButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				scrollDown();
			}
		});
		upButton = new Image("images/icon-bullet-arrow-up.png");
		upButton.addStyleName("pointerCursor");
		upButton.setSize("16px", "16px");
		upButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				scrollUp();
			}
		});

		listContainer = new VerticalPanel();
		listContainer.addStyleName("transparentBG");
		listContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		listContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

		HTML titleHTML = new HTML(title);
		titleHTML.addStyleName("pointerCursor");
		titleHTML.addMouseListener(new MouseListenerAdapter() {
			@Override
			public void onMouseEnter(Widget sender) {
				if (!popupWindow.isAttached()) {
					// popupWindow.setPagePosition( sender.getAbsoluteLeft(),
					// sender.getAbsoluteTop() );
					// popupWindow.setLocation( sender.getAbsoluteLeft(),
					// sender.getAbsoluteTop() );
					popupWindow.show();
				}
			}
		});
		add(titleHTML);
	}

	/**
	 * Adds an entry to the list
	 * 
	 * @param name
	 *            - the name of the entry ... is displayed in the list
	 */
	public void addEntry(String name) {
		addEntry(name, "");
	}

	/**
	 * Adds an entry to the list
	 * 
	 * @param name
	 *            - the name of the entry ... is displayed in the list
	 * @param value
	 *            - an associated value.
	 */
	public void addEntry(String name, String value) {
		if (value == null)
			value = "";

		ScrollableListEntry entry = new ScrollableListEntry(name, value);
		entries.add(entry);
	}

	/**
	 * This listener will fire when an entry is clicked.
	 * 
	 * @param listener
	 */
	public void addEntryClickListener(EntryClickListener listener) {
		entryClickListener = listener;
	}

	public void clearEntryList() {
		entries.clear();
	}

	public boolean containsEntry(String name) {
		for (Iterator iter = entries.listIterator(); iter.hasNext();) {
			ScrollableListEntry cur = (ScrollableListEntry) iter.next();
			if (cur.getName().equals(name))
				return true;
		}

		return false;
	}

	public void draw(int startTopIndex) {
		if (populator != null)
			populator.populateList();

		bottomIndex = 0;
		topIndex = startTopIndex;
		listContainer.clear();
		listContainer.setSize(popupWindow.getWidth() - 100 + "px", popupWindow.getHeight() - 100 + "px");

		numToDisplay = listContainer.getOffsetHeight() / entryHeight;
		if (numToDisplay > entries.size())
			numToDisplay = entries.size();

		if (numToDisplay == 0)
			listContainer.add(new HTML("N/A"));

		for (int i = 0; i < topIndex; i++) {
			listContainer.add((ScrollableListEntry) entries.get(i));
			((ScrollableListEntry) entries.get(i)).setVisible(false);
		}

		for (int i = topIndex; i < numToDisplay; i++) {
			listContainer.add((ScrollableListEntry) entries.get(i));
			((ScrollableListEntry) entries.get(i)).setVisible(true);
			bottomIndex++;
		}

		for (int i = numToDisplay; i < entries.size(); i++) {
			listContainer.add((ScrollableListEntry) entries.get(i));
			((ScrollableListEntry) entries.get(i)).setVisible(false);
		}

		// undockedContainer.removeAll();
		undockedContainer.clear();
		undockedContainer.add(upButton);
		undockedContainer.add(listContainer);
		undockedContainer.add(downButton);
		// undockedContainer.layout();
		// popupWindow;
	}

	/**
	 * Removes an entry from the list based on the name. If duplicate names
	 * exist in the list, it removes the first it finds.
	 * 
	 * @param name
	 *            of entry
	 * @return value associated with the entry, or null if entry is not found
	 * @throws NoSuchElementException
	 *             if the entry is not found in the list
	 */
	public String removeEntry(String name) throws NoSuchElementException {
		for (Iterator iter = entries.listIterator(); iter.hasNext();) {
			ScrollableListEntry cur = (ScrollableListEntry) iter.next();
			if (cur.getName().equals(name)) {
				entries.remove(cur);
				return cur.getValue();
			}
		}

		throw new NoSuchElementException("Entry " + name + " not found.");
	}

	private void scrollDown() {
		if (bottomIndex < entries.size() - 1 && entries.size() > numToDisplay) {
			((ScrollableListEntry) entries.get(topIndex)).slideOffTop();

			bottomIndex++;
			topIndex++;

			listContainer.add((ScrollableListEntry) entries.get(topIndex));
			((ScrollableListEntry) entries.get(topIndex)).fadeIn();
		}
	}

	private void scrollUp() {
		if (topIndex > 0 && entries.size() > numToDisplay) {
			((ScrollableListEntry) entries.get(bottomIndex)).slideOffBottom();

			topIndex--;
			bottomIndex--;

			listContainer.add((ScrollableListEntry) entries.get(bottomIndex));
			((ScrollableListEntry) entries.get(bottomIndex)).fadeIn();
		}
	}

	/**
	 * Updates an entry in the list based on the oldName, assigning it a new
	 * name/value pair.
	 * 
	 * @param oldName
	 *            of entry
	 * @param newName
	 *            to give entry
	 * @param newValue
	 *            to give entry
	 * 
	 * @throws NoSuchElementException
	 *             if the entry is not found in the list
	 */
	public void updateEntry(String oldName, String newName, String newValue) throws NoSuchElementException {
		for (Iterator iter = entries.listIterator(); iter.hasNext();) {
			ScrollableListEntry cur = (ScrollableListEntry) iter.next();
			if (cur.getName().equals(oldName)) {
				cur.name = newName;
				cur.value = newValue;
				cur.setHTML(newName);

				return;
			}
		}

		throw new NoSuchElementException("Entry " + oldName + " not found.");
	}
}
