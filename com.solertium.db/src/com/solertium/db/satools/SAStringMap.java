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

package com.solertium.db.satools;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import net.jcip.annotations.NotThreadSafe;

import com.solertium.db.DBProcessor;
import com.solertium.db.DBSession;
import com.solertium.db.ExecutionContext;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
@NotThreadSafe
public class SAStringMap implements DBProcessor {
	public static class SAStringDataMap extends HashMap<String, String> {
		private static final long serialVersionUID = 1;
		private final String id;
		private HashMap<String, String> map;

		public SAStringDataMap(final String id) {
			super();
			this.id = id;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final SAStringDataMap other = (SAStringDataMap) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		public String get(final String key) {
			return super.get(key);
		}

		public String getID() {
			return id;
		}

		public HashMap<String, String> getMap() {
			return map;
		}

		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		public void setMap(final HashMap<String, String> map) {
			this.map = map;
		}

	}

	private final HashMap<String, String> interestingColumnNames;
	public TreeMap<String, SAStringDataMap> m = new TreeMap<String, SAStringDataMap>();

	public SAStringMap() {
		this(new HashMap<String, String>());
	}

	public SAStringMap(final HashMap<String, String> interestingColumnNames) {
		this.interestingColumnNames = interestingColumnNames;
	}

	public void process(final ResultSet rs, final ExecutionContext ec) {
		try {
			while (rs.next()) {
				final SAStringDataMap data = new SAStringDataMap(rs
						.getString(2));
				data.setMap(interestingColumnNames);
				final Iterator<String> iterator = interestingColumnNames
						.values().iterator();
				while (iterator.hasNext()) {
					final String key = iterator.next();
					try {
						data.put(key, rs.getString(rs.findColumn(key)));
					} catch (final Exception r) {
					}
				}
				m.put(rs.getString(1), data);
			}
			/*
			 * while(rs.next()){ m.put(rs.getString(1),rs.getString(2)); }
			 */
		} catch (final Exception ignored) {
		}
	}

	public void setDBSess(final DBSession dbsess) {
	}
}
