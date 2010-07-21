package org.iucn.sis.client.utilities;

import com.google.gwt.user.client.ui.Widget;

public interface Securable {

	public void checkSecurity();

	public Widget getNoPermissionPanel();

	public void lock(Object data);

	public void unlock(Object data);

}
