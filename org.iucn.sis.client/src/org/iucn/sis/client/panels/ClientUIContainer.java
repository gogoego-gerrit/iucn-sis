package org.iucn.sis.client.panels;

import org.iucn.sis.client.panels.login.LoginPanel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

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

	private boolean loggedIn;

	public ClientUIContainer() {
		container = this;

		setLayout(new FitLayout());
		setLayoutOnChange(true);
		
		loginPanel = new LoginPanel();
		buildLogin(null);
	}

	public void buildLogin(String message) {
		loggedIn = false;
		
		removeAll();

		add(loginPanel);
		loginPanel.update(message);

		bodyContainer = null;
		headerContainer = null;
		footerContainer = null;
	}

	public void buildPostLogin(String first, String last, String affiliation) {
		loggedIn = true;

		removeAll();

		bodyContainer = new BodyContainer();
		headerContainer = new HeaderContainer(first, last, affiliation);

		BorderLayoutData headerData = new BorderLayoutData(LayoutRegion.NORTH);
		headerData.setFloatable(true);
		headerData.setCollapsible(true);
		headerData.setSplit(true);
		headerData.setMinSize(200);
		headerData.setMaxSize(400);
		
		BorderLayoutData bodyData =new BorderLayoutData(LayoutRegion.CENTER);
		bodyData.setMinSize(500);
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());

		container.add(headerContainer, headerData);
		container.add(bodyContainer, bodyData);
		
		add(container);
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
}
