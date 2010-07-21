package org.iucn.sis.client.panels.permissions;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.Permission;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.user.client.ui.ListBox;

public class PermissionSetUI extends HorizontalPanel {
	
	private Permission permission;
	
	private ListBox read;
	private ListBox write;
	private ListBox create;
	private ListBox delete;
	private ListBox grant;
	private ListBox use;
	
	public PermissionSetUI() {
		this(null);
	}
	
	public PermissionSetUI(Permission permission) {
		this.permission = permission;
		
		setSpacing(3);
		setVerticalAlign(VerticalAlignment.BOTTOM);
		
		read = getBox();
		write = getBox();
		create = getBox();
		delete = getBox();
		grant = getBox();
		use = getBox();
		
		draw();
	}
	
	private ListBox getBox() {
		ListBox l = new ListBox(false);
		l.addItem(" ");
		l.addItem("+");
		l.addItem("-");
		l.setWidth("36px");
		return l;
	}
	
	public Permission getPermission() {
		return permission;
	}
	
	public void setPermission(Permission permission) {
		this.permission = permission;
		resetData();
	}
	
	private void setIndex(ListBox box, String operation) {
		if( permission != null ) {
			Boolean ret = permission.check(operation);
			box.setSelectedIndex( ret == null ? 0 : ret.booleanValue() ? 1 : 2 );
		} else
			box.setSelectedIndex(0);
	}
	
	private void resetData() {
		setIndex(read, AuthorizableObject.READ);
		setIndex(write, AuthorizableObject.WRITE);
		setIndex(create, AuthorizableObject.CREATE);
		setIndex(delete, AuthorizableObject.DELETE);
		setIndex(grant, AuthorizableObject.GRANT);
		setIndex(use, AuthorizableObject.USE_FEATURE);
	}
	
	private void draw() {
		add(new Html("r"));
		add(read);
		
		add(new Html("w"));
		add(write);
		
		add(new Html("c"));
		add(create);
		
		add(new Html("d"));
		add(delete);
		
		add(new Html("g"));
		add(grant);
		
		add(new Html("u"));
		add(use);
		
		resetData();
	}
	
	/**
	 * Returns the PermissionSet, setting the appropriate values based on the check boxes
	 * before it's returned. If all options are Unset, this method will return a null
	 * permission set.
	 * 
	 * @return a PermissionSet reflecting the check box values, or null if nothing is set
	 */
	public void sinkToPermission() {
		int r = read.getSelectedIndex();
		int w = write.getSelectedIndex();
		int c = create.getSelectedIndex();
		int d = delete.getSelectedIndex();
		int g = grant.getSelectedIndex();
		int u = use.getSelectedIndex();
		
		if( permission != null ) {
			permission.setRead(r == 1 ? true : false );
			permission.setWrite(w == 1 ? true : false );
			permission.setCreate(c == 1 ? true : false );
			permission.setDelete(d == 1 ? true : false );
			permission.setGrant(g == 1 ? true : false );
			permission.setUse(u == 1 ? true : false );
		}
	}

	public ListBox getRead() {
		return read;
	}

	public ListBox getWrite() {
		return write;
	}

	public ListBox getCreate() {
		return create;
	}

	public ListBox getDelete() {
		return delete;
	}

	public ListBox getGrant() {
		return grant;
	}

	public ListBox getUse() {
		return use;
	}
}
