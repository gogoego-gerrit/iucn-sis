/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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

package org.gogoego.util.db.query;

import org.gogoego.util.db.SQLAware;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public interface QConstraint extends XMLConfigurable, SQLAware {

	public final static int CG_AND = 1;
	public final static int CG_OR = 2;
	public final static int CT_CONTAINS = 10;
	public final static int CT_ENDS_WITH = 12;
	public final static int CT_CONTAINS_IGNORE_CASE = 13;
	public final static int CT_EQUALS = 1;
	public final static int CT_GT = 2;
	public final static int CT_LT = 3;
	public final static int CT_NOT = 90;
	public final static int CT_STARTS_WITH = 11;

	public QConstraint findByID(String id);

	public String getID();

}
