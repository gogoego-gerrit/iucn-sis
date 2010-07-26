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

package org.gogoego.util.db.satools;

import java.sql.ResultSet;

import org.gogoego.util.db.DBProcessor;
import org.gogoego.util.db.DBSession;
import org.gogoego.util.db.ExecutionContext;
import org.gogoego.util.getout.GetOut;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class SABoolean implements DBProcessor {
	public boolean b = false;

	public void process(final ResultSet rs, final ExecutionContext ec) {
		try {
			rs.next();
			b = rs.getBoolean(1);
		} catch (final Exception weaklyHandled) {
			GetOut.log(weaklyHandled);
		}
	}

	public void setDBSess(final DBSession dbsess) {
	}
}
