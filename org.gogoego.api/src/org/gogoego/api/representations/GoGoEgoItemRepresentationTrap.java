/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package org.gogoego.api.representations;


/**
 * GoGoEgoItemRepresentationTrap.java
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoEgoItemRepresentationTrap extends BaseRepresentationTrap<GoGoEgoItemRepresentation> implements HasURI {
	
	public GoGoEgoItemRepresentationTrap(GoGoEgoItemRepresentation item) {
		super(item);
	}
	
	public GoGoEgoCollectionRepresentation getParent() {
		return getWrapped().getParent();
	}
	
	public String getItemID() {
		return getWrapped().getItemID();
	}
	
	public String getItemName() {
		return getWrapped().getItemName();
	}
	
	public String getURI() {
		return getWrapped().getURI();
	}
	
	public String getValue(final String key) {
		return getWrapped().getValue(key);
	}

	public String keySetCSV() {
		return getWrapped().keySetCSV();
	}

	public String resolveEL(final String key) {
		return getWrapped().resolveEL(key);
	}
	
	public String resolveConditionalEL(String template, String keyCSV) {
		return getWrapped().resolveConditionalEL(template, keyCSV);
	}

}
