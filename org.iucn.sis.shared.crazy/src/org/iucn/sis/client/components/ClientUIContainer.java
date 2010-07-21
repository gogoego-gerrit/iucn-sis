package org.iucn.sis.client.components;

import org.iucn.sis.client.components.panels.LoginPanel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowCloseListener;

/**
 * This is the class on which everything else will lay. It will take up all of
 * the available real estate in the client's browser and arrange child widgets
 * vertically, by default, or by the specified style.
 * 
 * @author adam.schwartz
 * 
 */
public class ClientUIContainer extends Viewport {
	private LoginPanel loginPanel = null;

	public static BodyContainer bodyContainer = null;
	public static HeaderContainer headerContainer = null;
	public static FooterContainer footerContainer = null;
	public static ClientUIContainer container = null;

	private WindowCloseListener listener = new WindowCloseListener() {
		public void onWindowClosed() {
		}

		public String onWindowClosing() {
			return "This action will close SIS Toolkit - all unsaved changes will be lost.";
		}
	};

	public ClientUIContainer() {
		container = this;

		loginPanel = new LoginPanel();
		buildLogin(null);
	}

	public void buildLogin(String message) {
		Window.removeWindowCloseListener(listener);

		removeAll();

		setLayout(new FitLayout());

		add(loginPanel);
		loginPanel.update(message);

		bodyContainer = null;
		headerContainer = null;
		footerContainer = null;

		layout();
	}

	public void buildPostLogin(String first, String last, String affiliation) {
		Window.addWindowCloseListener(listener);

		remove(loginPanel);

		setLayout(new BorderLayout());

		bodyContainer = new BodyContainer();
		headerContainer = new HeaderContainer(first, last, affiliation);
		footerContainer = new FooterContainer();

		BorderLayoutData bodyData = new BorderLayoutData(LayoutRegion.CENTER, .9f, 5, 2000);
		BorderLayoutData headerData = new BorderLayoutData(LayoutRegion.NORTH, .1f, 5, 2000);
		BorderLayoutData footerData = new BorderLayoutData(LayoutRegion.SOUTH, .05f, 5, 2000);

		headerData.setSize(HeaderContainer.defaultHeight);

		add(bodyContainer, bodyData);
		add(headerContainer, headerData);
		add(footerContainer, footerData);

		layout();
	}
}
