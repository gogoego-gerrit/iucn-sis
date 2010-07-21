package org.iucn.sis.client.acl.ui;

import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.acl.base.PermissionSet;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.user.client.ui.ListBox;

public class PermissionSetUI extends HorizontalPanel {
	
	private PermissionSet set;
	
	private ListBox read;
	private ListBox write;
	private ListBox create;
	private ListBox delete;
	private ListBox grant;
	private ListBox use;
	
	public PermissionSetUI() {
		this(new PermissionSet());
	}
	
	public PermissionSetUI(PermissionSet set) {
		this.set = set;
		
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
	
	public void setPermissionSet(PermissionSet set) {
		this.set = set;
		resetData();
	}
	
	private void setIndex(ListBox box, String operation) {
		Boolean ret = set.check(operation);
		box.setSelectedIndex( ret == null ? 0 : ret.booleanValue() ? 1 : 2 );
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
	public PermissionSet getSet() {
		int r = read.getSelectedIndex();
		int w = write.getSelectedIndex();
		int c = create.getSelectedIndex();
		int d = delete.getSelectedIndex();
		int g = grant.getSelectedIndex();
		int u = use.getSelectedIndex();
		
		if( r == 0 && w == 0 && c == 0 && d == 0 && g == 0 && u == 0 )
			return null;
		
		set.set(AuthorizableObject.READ, r == 0 ? null : r == 1 ? true : false );
		set.set(AuthorizableObject.WRITE, w == 0 ? null : w == 1 ? true : false );
		set.set(AuthorizableObject.CREATE, c == 0 ? null : c == 1 ? true : false );
		set.set(AuthorizableObject.DELETE, d == 0 ? null : d == 1 ? true : false );
		set.set(AuthorizableObject.GRANT, g == 0 ? null : g == 1 ? true : false );
		set.set(AuthorizableObject.USE_FEATURE, u == 0 ? null : u == 1 ? true : false );
		
		return set;
	}
	
	/**
	 * Returns the PermissionSet; DOES NOT SET values based on the check boxes.
	 * 
	 * @return a PermissionSet
	 */
	public PermissionSet getRawSet() {
		return set;
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
