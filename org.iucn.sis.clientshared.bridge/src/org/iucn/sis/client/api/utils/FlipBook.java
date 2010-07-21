package org.iucn.sis.client.api.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Holds a collection of FlipBookPages, iterating over them, ensuring the data
 * entered is valid (non-empty), and saving the data to a HashMap for the
 * calling function
 * 
 * The form can only be processed once the data entered is valid and the user
 * has gone through the entire flipbook. They can only go from page to page
 * after entering valid data
 * 
 * @author carl.scott
 */
public class FlipBook {

	// Book-related variables
	private Widget title; // Title of the book
	private ScrollPanel pagePanel; // The panel the current page is shown in
	private HorizontalPanel footerScroll; // The footer & scrolling buttons
	private Button cancelButton; // The button the calling function uses to
	// return to a state of normalcy if the user
	// decides to cancel.
	private Button footerButton; // The button the calling function uses to
	// determine when the flipbook form is done
	private Button next, prev; // Scrolls through the book
	private VerticalPanel flipbook; // The panel the whole flipbook is shown in

	// Page-related variables
	private ArrayList pages; // Has all the FlipBookPages in this book
	private HTML currentPageNum; // Shows Page x of y
	private HTML currentPageTitle; // Shows the title of the current page
	private HTML currentPageStatusBar; // Show the validity status (if data is
	// invalid)
	private int currentPage = 0; // The current page being used

	// Confirmation page
	private Widget confirmationPage; // This widget is shown on the confirmation
	// page
	// when the data has been entered. Defaults to
	// a message if not set.

	// Options
	private boolean viewOnly = false; // Is this a book users just view data in?

	/**
	 * Creates a new Flipbook
	 */
	public FlipBook() {
		pages = new ArrayList();
		confirmationPage = null;
		cancelButton = null;
		footerButton = new Button();
	}

	/**
	 * Adds a FlipBookPage to the FlipBook
	 * 
	 * @param page
	 *            the page to add
	 */
	public void addPage(FlipBookPage page) {
		pages.add(page);
	}

	/**
	 * Deconstructs the book
	 */
	public void closeBook() {
		flipbook.setVisible(false);
		flipbook.removeFromParent();
		flipbook = null;
	}

	/**
	 * Shows a confirmations screen once the page has been finished.
	 */
	private void complete() {
		if (confirmationPage == null) {
			confirmationPage = new HTML(
					"You have completed the form.  Please review your entries and, when ready, click the "
							+ footerButton.getText() + " button to submit your data");
		}
		pagePanel.clear();
		pagePanel.setVisible(false);
		pagePanel.add(confirmationPage);
		pagePanel.setVisible(true);

		currentPage++;
		currentPageTitle.setText("Form Complete!");
		currentPageNum.setText("End");
		footerButton.setEnabled(true);
		next.setEnabled(false);
	}

	/**
	 * Checks to see what page has been requested. If in bounds, go to it. If
	 * you're at the end of the book, show this.
	 */
	private void doStatusCheck() {
		currentPageNum.setText("Page " + (currentPage + 1) + " of " + pages.size());
		currentPageTitle.setText(((FlipBookPage) pages.get(currentPage)).getTitle());

		if (currentPage < pages.size() - 1) {
			prev.setEnabled(!(currentPage == 0));
			next.setEnabled((!(pages.size() == 1)));
			if (next.getText().equalsIgnoreCase("Finish"))
				footerButton.setEnabled(true);
			else
				footerButton.setEnabled(pages.size() == 1);
			next.setText("-->");
		}

		else if (currentPage == pages.size() - 1) {
			prev.setEnabled((pages.size() != 1));
			if (viewOnly) {
				next.setEnabled(false);
			} else {
				next.setText("Finish");
				next.setEnabled(true);
			}
		}

		if (viewOnly)
			footerButton.setEnabled(true);
	}

	public FlipBookPage getCurrentPage() {
		return (FlipBookPage) pages.get(currentPage);
	}

	/**
	 * Creates the footer panel
	 * 
	 * @return the panel
	 */
	private Widget getFooterScroll() {
		footerScroll = new HorizontalPanel();
		footerScroll.addStyleName("SIS_FlipBookFooterScroll");
		footerScroll.setSpacing(4);

		prev = new Button("<--", new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (currentPage > 0) {
					pagePanel.setWidget((Widget) pages.get(--currentPage));
					doStatusCheck();
				}
			}
		});
		prev.addStyleName("SIS_FlipBookScrollPrev");

		next = new Button("-->", new ClickHandler() {
			public void onClick(ClickEvent event) {
				if ((((Button) event.getSource()).getText().equalsIgnoreCase("-->"))) {
					if (((FlipBookPage) pages.get(currentPage)).isValid()) {
						currentPageStatusBar.setHTML("<span style=\"color:#CCFFCC\">Status</span>");
						if (currentPage < pages.size() - 1) {
							pagePanel.setWidget((Widget) pages.get(++currentPage));
							doStatusCheck();
						}
					} else {
						currentPageStatusBar.setText("INVALID: You must fill out all fields on this page");
					}
				} else { // It must say "Finish"
					if (((FlipBookPage) pages.get(currentPage)).isValid()) {
						complete();
					} else {
						currentPageStatusBar.setText("INVALID: You must fill out all fields on this page");
					}
				}
			}
		});
		next.addStyleName("SIS_FlipBookScrollNext");

		currentPageNum = new HTML();
		currentPageNum.addStyleName("SIS_FlipBookPageNum");

		footerScroll.add(prev);
		footerScroll.add(currentPageNum);
		footerScroll.add(next);
		if (footerButton != null)
			footerScroll.add(footerButton);
		if (cancelButton == null) {
			footerScroll.add(new Button("Cancel", new ClickHandler() {
				public void onClick(ClickEvent event) {
					closeBook();
				}
			}));
		} else {
			footerScroll.add(cancelButton);
		}
		return footerScroll;
	}

	/**
	 * Get data from the book
	 * 
	 * @return hashmap with results
	 */
	public HashMap getResults() {
		HashMap results = new HashMap();
		for (int i = 0; i < pages.size(); i++) {
			results.putAll(((FlipBookPage) pages.get(i)).saveData());
		}
		return results;
	}

	/**
	 * Determines if the data in the book is valid. It should always be upon
	 * completion
	 * 
	 * @return true if valid, false otherwise
	 */
	public boolean isValid() {
		for (int i = 0; i < pages.size(); i++) {
			if (!((FlipBookPage) pages.get(i)).isValid()) {
				return false;
			}
		}
		return true;
	}

	public void setCancelButton(Button button, ClickHandler listener) {
		this.cancelButton = button;
		cancelButton.addClickHandler(listener);
	}

	public void setCancelButton(String buttonText, ClickHandler listener) {
		setCancelButton(new Button(buttonText), listener);
	}

	public void setConfirmationPage(Widget confirmationPage) {
		this.confirmationPage = confirmationPage;
	}

	/**
	 * Set the footer button for the calling function.
	 * 
	 * @param button
	 *            the button
	 * @param listener
	 *            a click listener for the button
	 */
	public void setFooterButton(Button button, ClickHandler listener) {
		this.footerButton = button;
		footerButton.addClickHandler(listener);
	}

	/************ PAGE RELATED FUNCTIONS *************/

	public void setFooterButton(String buttonText, ClickHandler listener) {
		setFooterButton(new Button(buttonText), listener);
	}

	// public void addViewAsPage(SISView view) {
	// addViewAsPages(view, true);
	// }

	/**
	 * Adds all the pages in a View to a FlipBook, each page being a different
	 * FlipBookPage
	 * 
	 * @param view
	 */
	// public void addViewAsPages(SISView view, boolean mustValidate) {
	// for (int i = 0; i < view.getPages().size(); i++) {
	// FlipBookPage pageToAdd = new FlipBookPage(
	// view.getPageAt(i).getPageTitle(),
	// view.getPageAt(i),
	// view.getDisplaySetToUse());
	// pageToAdd.setValidation(mustValidate);
	// addPage(pageToAdd);
	// }
	// }
	/************* SET BOOK OPTIONS ***************/

	public void setIsViewOnly(boolean isViewOnly) {
		viewOnly = isViewOnly;
	}

	/************* PRIVATE HELPER FUNCTIONS ****************/

	/************ BOOK-RELATED FUNCTIONS *************/

	public void setTitle(String title) {
		setTitle(new HTML(title));
	}

	/**
	 * Sets the title widget of the book to whatever widget you want shown.
	 * Given a string, the widget defaults to an HTML Widget
	 * 
	 * @param title
	 *            the title widget
	 */
	public void setTitle(Widget title) {
		this.title = title;
	}

	/**
	 * Displays the flipBook.
	 * 
	 * @return a panel containing a represenation of the book
	 */
	public Widget showFlipBook() {
		flipbook = new VerticalPanel();
		flipbook.addStyleName("SIS_FlipBook");

		pagePanel = new ScrollPanel();
		pagePanel.setWidget((Widget) pages.get(currentPage));

		title.addStyleName("SIS_FlipBookTitle");
		pagePanel.addStyleName("SIS_FlipBookPagePanel");

		currentPageTitle = new HTML();
		currentPageTitle.addStyleName("SIS_FlipBookPageTitle");

		currentPageStatusBar = new HTML("<span style=\"color:#CCFFCC\">Status</span>");
		currentPageStatusBar.addStyleName("SIS_FlipBookPageStatus");

		flipbook.add(title);
		flipbook.add(currentPageTitle);
		flipbook.add(pagePanel);
		flipbook.add(currentPageStatusBar);
		flipbook.add(getFooterScroll());

		doStatusCheck();

		return flipbook;
	}

}// class FlipBook
