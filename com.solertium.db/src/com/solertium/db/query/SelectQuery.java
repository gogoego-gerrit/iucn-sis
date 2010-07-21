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

package com.solertium.db.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBSession;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SilentExecutionContext;
import com.solertium.db.XMLConfigurable;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
@NotThreadSafe
public class SelectQuery extends BaseQuery implements Query, XMLConfigurable {

	final protected ArrayList<QColumn> columnList = new ArrayList<QColumn>();

	public String findJoin(final String hi, final String lo,
			final DBSession dbsess, final boolean isRecursive) throws Exception {
		if (explicitJoins.containsKey(hi))
			return explicitJoins.get(hi).getSQL(dbsess);

		final ExecutionContext ec = new SilentExecutionContext(dbsess);

		/**
		 * This block attempts to eliminate case sensitivity via input by
		 * getting matching tables & using the first one. While most databases
		 * will only return one, if more than one is returned, you're on your
		 * own, DB will look up what you specified & assumes you know what
		 * you're doing. *CS*
		 */
		final List<String> loMatch = dbsess.getMatchingTables(ec, lo);
		final String loComp = loMatch.size() == 1 ? loMatch.get(0) : lo;
		final List<String> hiMatch = dbsess.getMatchingTables(ec, hi);
		final String hiComp = hiMatch.size() == 1 ? hiMatch.get(0) : hi;

		final Row rowLo = dbsess.getRow(loComp, new SilentExecutionContext(
				dbsess));
		final StringBuffer sb = new StringBuffer(2048);
		for (final Column c : rowLo.getColumns()) {
			final CanonicalColumnName related = c.getRelatedColumn();
			if (related == null)
				continue;

			if (hiComp.equals(related.getTable())) {
				final String ref_id = related.getField();
				sb.append(dbsess.formatIdentifier(hiComp));
				sb.append(".");
				sb.append(dbsess.formatIdentifier(ref_id));
				sb.append(" = ");
				sb.append(dbsess.formatIdentifier(loComp));
				sb.append(".");
				sb.append(dbsess.formatIdentifier(c.getLocalName()));
				return sb.toString();
			}
		}

		// try inverted order
		return isRecursive ? null : findJoin(lo, hi, dbsess, true);
	}

	public String findJoinType(final String tablename) {
		final Iterator<QColumn> it = columnList.iterator();
		boolean outer = false;
		while (it.hasNext()) {
			final QColumn col = it.next();
			final String tbl = col.getName().getTable();
			if (tablename.equals(tbl) && (col.getOuter() != null)
					&& (col.getOuter() == true))
				outer = true;
		}
		if (outer)
			return ("LEFT ");
		else
			return ("");
	}

	public String findPath(final ArrayList<String> tables,
			final DBSession dbsess) throws Exception {
		if(tables==null || tables.size()==0)
			throw new Exception("No tables to join");
		boolean irreconcilable = false;
		boolean unjoined_tables = true;
		StringBuffer fpath = null;
		while (unjoined_tables) {
			unjoined_tables = false;
			boolean swapped = true;
			while (swapped) {
				swapped = false;
				for (int i = 0; i < tables.size() - 1; i++) {
					final String a = tables.get(i);
					final String b = tables.get(i + 1);
					Integer ja = null;
					// TODO: get join priority for table a
					Integer jb = null;
					// TODO: get join priority for table b
					if (ja == null)
						ja = 0;
					if (jb == null)
						jb = 0;
					if (ja < jb) {
						tables.set(i, b);
						tables.set(i + 1, a);
						swapped = true;
					}
				}
			}
			fpath = new StringBuffer(2048);
			fpath.append(dbsess.formatIdentifier(tables.get(0)));
			fpath.append("\n");
			for (int i = 1; i < tables.size(); i++) {
				final String a = tables.get(i);
				boolean joined = false;
				for (int j = 0; j < i; j++) {
					final String b = tables.get(j);
					final String join = findJoin(a, b, dbsess, false);
					if (join != null) {
						fpath.append("" + findJoinType(a));
						fpath.append("JOIN\n " + dbsess.formatIdentifier(a)
								+ "\nON\n ");
						fpath.append(join);
						fpath.append("\n");
						joined = true;
						break;
					}
				}
				if (!joined)
					irreconcilable = true;
				/*
				 * TODO: get next-hop hint -- do not erase
				 *
				 * for (int j = i - 1; j >= 0; j--) { final String b =
				 * tables.get(j); final String hint = null; if ((hint != null) &&
				 * (!"".equals(hint)) && (!tables.contains(hint))) {
				 * System.out.println(" Got hint to use " + hint + "<br/>\n");
				 * tables.add(hint); unjoined_tables = true; irreconcilable =
				 * false; } else { System.out.println("No hint found<br/>\n"); } }
				 */
			}
			if (irreconcilable)
				return null;
		}
		return fpath.toString();
	}

	public ArrayList<QColumn> getColumnList() {
		return columnList;
	}

	public String getSQL(final DBSession ds) {
		final StringBuffer buf = new StringBuffer(1024);
		buf.append("SELECT ");
		ArrayList<QColumn> cl = getColumnList();
		final ArrayList<String> tables = new ArrayList<String>();
		for (int i = 0; i < cl.size(); i++) {
			final QColumn curCol = cl.get(i);
			final CanonicalColumnName ccn = curCol.getName();
			buf.append(" ");
			buf.append(ds.formatCanonicalColumnName(ccn));
			if (curCol.getLabel() != null) {
				buf.append(" AS ");
				buf.append("\"" + curCol.getLabel() + "\"");
			}
			final String t = ccn.getTable();
			if (!tables.contains(t))
				tables.add(t);
			if (i < cl.size() - 1)
				buf.append(", ");
		}
		for (final String explicitJoinTable : explicitJoins.keySet())
			if (!tables.contains(explicitJoinTable))
				tables.add(explicitJoinTable);
		buf.append(" FROM ");
		try {
			final String fjoin = findPath(tables, ds);
			buf.append(fjoin);
		} catch (final Exception joiningException) {
			joiningException.printStackTrace();
		}
		if (!constraints.isEmpty()) {
			buf.append(" WHERE ");
			buf.append(constraints.getSQL(ds));
		}
		cl = getColumnList();
		boolean orderClause = false;
		boolean orderFirst = true;
		for (int i = 0; i < cl.size(); i++) {
			final QColumn qc = cl.get(i);
			final String sort = qc.getSort();
			if (sort != null) {
				if (orderFirst)
					orderFirst = false;
				else
					buf.append(", ");
				final CanonicalColumnName ccn = cl.get(i).getName();
				if (!orderClause) {
					orderClause = true;
					buf.append(" ORDER BY ");
				}
				buf.append(ds.formatCanonicalColumnName(ccn));
				buf.append(" ");
				buf.append(sort.toUpperCase());
			}
		}
		return buf.toString();
	}

	public void loadConfig(final Element el) throws Exception {
		final NodeList nl = el.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			final Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				final Element e = (Element) n;
				final String name = e.getNodeName();
				if ("select".equals(name)) {
					final QColumn qc = new QColumn();
					qc.loadConfig(e);
					select(qc);
				} else if ("idiom".equals(name)) {
					final QIdiom qi = (QIdiom) Class.forName(
							e.getAttribute("class")).newInstance();
					qi.loadConfig(e);
					idiom(qi);
				} else if ("constraint".equals(name)) {
					final QConstraint qc = (QConstraint) Class.forName(
							e.getAttribute("class")).newInstance();
					qc.loadConfig(e);
					constraints = (QConstraintGroup) qc;
				}
			}
		}
	}

	public Element saveConfig(final Document doc) throws Exception {
		final Element el = doc.createElement("query");
		el.setAttribute("version", "1.1");
		for (int j = 0; j < columnList.size(); j++) {
			final QColumn col = columnList.get(j);
			el.appendChild(col.saveConfig(doc));
		}
		saveIdioms(doc);
		saveExplicitJoins(doc);
		final Element con = constraints.saveConfig(doc);
		el.appendChild(con);
		return el;
	}

	private void saveExplicitJoins(final Document doc) throws Exception {
		final Element rootEl = doc.getDocumentElement();
		for (final String key : explicitJoins.keySet()) {
			final Element child = doc.createElement("explicitJoin");
			child.setAttribute("name", key);
			final Object o = explicitJoins.get(key);
			child.appendChild(((QConstraint) o).saveConfig(doc));
			rootEl.appendChild(child);
		}
	}

	private Element saveIdioms(final Document doc) {
		final Element rootEl = doc.getDocumentElement();
		for (final QIdiom i : idioms)
			rootEl.appendChild(i.saveConfig(doc));
		return rootEl;
	}

	public void select(final CanonicalColumnName tfspec) {
		final QColumn col = new QColumn();
		col.setName(tfspec);
		select(col);
	}

	public void select(final CanonicalColumnName tfspec, final String sort) {
		final QColumn col = new QColumn();
		col.setName(tfspec);
		col.setSort(sort);
		select(col);
	}

	public void select(final QColumn col) {
		columnList.add(col);
	}

	public void select(final String table, final String field) {
		select(new CanonicalColumnName(table, field));
	}

	public void select(final String table, final String field, final String sort) {
		select(new CanonicalColumnName(table, field), sort);
	}

}
