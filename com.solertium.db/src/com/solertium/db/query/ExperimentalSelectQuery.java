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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
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
public class ExperimentalSelectQuery extends BaseQuery implements Query, XMLConfigurable {

	final protected ArrayList<QColumn> columnList = new ArrayList<QColumn>();
	protected String checkStatus = "LOOSE";
	protected int maxChase = 64;
	
	protected ArrayList<String> unjoinedTables = new ArrayList<String>();
	
	public QConstraint findJoin(final String hi, final String lo,
			final DBSession dbsess, final boolean isRecursive) throws Exception {
		if (explicitJoins.containsKey(hi))
			return explicitJoins.get(hi);

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
		for (final Column c : rowLo.getColumns()) {
			final CanonicalColumnName related = c.getRelatedColumn();
			if (related == null)
				continue;

			if (hiComp.equals(related.getTable())) {
				final String ref_id = related.getField();
				QRelationConstraint qrc = new QRelationConstraint(
						new CanonicalColumnName(hiComp,ref_id),
						new CanonicalColumnName(loComp,c.getLocalName())
				);
				write("Relationship is " + qrc);
				return qrc;
			}
		}

		// try inverted order
		return isRecursive ? null : findJoin(lo, hi, dbsess, true);
	}

	public QConstraintGroup getConstraints() {
		return constraints;
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

	public boolean resolve(List<String> tables, DBSession dbsess, HashMap<String,List<QConstraint>> tableRelationships, HashMap<String,QConstraint> crossRelationships) throws Exception {
		boolean resolved = true;
		String append = null;
		write("\nResolving");
		for(String t : tables) write("  # "+t);
		for(String a : tables){
			final ArrayList<String> foundRelationships = new ArrayList<String>();
			write("Examining "+a);
			for(String b : tables){
				if(b.equals(a)) continue;
				QConstraint join = crossRelationships.get(a+":"+b);
				if(join == null) join = findJoin(a, b, dbsess, false);
				if(join == null) continue;
				foundRelationships.add(b);
				crossRelationships.put(a+":"+b, join);
				crossRelationships.put(b+":"+a, join);

				List<QConstraint> lq = null;

				lq = tableRelationships.get(a);
				if(lq==null){
					lq = new ArrayList<QConstraint>();
					lq.add(join);
					tableRelationships.put(a,lq);
				} else {
					if(!lq.contains(join)){
						lq.add(join);
					}
				}

				lq = tableRelationships.get(b);
				if(lq==null){
					lq = new ArrayList<QConstraint>();
					lq.add(join);
					tableRelationships.put(b,lq);
				} else {
					if(!lq.contains(join)){
						lq.add(join);
					}
				}
			}
			
			final ArrayList<String> remainingTables = new ArrayList<String>();
			remainingTables.addAll(tables);
			remainingTables.removeAll(foundRelationships);
			
			//if(tableRelationships.get(a)==null){ // look for hints
				for(String b : remainingTables){
					if(b.equals(a)) continue;
					write("looking for hint between "+a+" and "+b);
					String hint = dbsess.getHint(a, b);
					if ((hint != null) && (!"".equals(hint))
							&& (!tables.contains(hint))) {
						write(" Got hint to use " + hint);
						append = hint;
						resolved = false;
						//break;
					} else {
						hint = dbsess.getHint(b, a);
						if ((hint != null) && (!"".equals(hint))
								&& (!tables.contains(hint))) {
							write(" Got (reverse) hint to use " + hint);
							resolved = false;
							append = hint;
							//break;
						}
					}
				}
			//}
		}
		if(!resolved){
			tables.add(append);
			tableRelationships.clear();
		}
		return resolved;
	}

	public String findPath(final ArrayList<String> tables,
			final DBSession dbsess) throws Exception {
		if(tables.size()==0)
			throw new Exception("No tables in query");
		if(tables.size()==1)
			return dbsess.formatIdentifier(tables.get(0));
		final HashMap<String,List<QConstraint>> tableRelationships = new HashMap<String,List<QConstraint>>();
		final HashMap<String,QConstraint> crossRelationships = new HashMap<String,QConstraint>();
		boolean fullyResolved = false;
		write("-- Initial Resolve --");
		while(!fullyResolved){
			fullyResolved = resolve(tables,dbsess,tableRelationships,crossRelationships);
		}

		StringBuffer fpath = new StringBuffer();

		// count relationships for tables
		int singleRelations = 0;
		int multiRelations = 0;
		for (String a : tables) {
			List<QConstraint> lc = tableRelationships.get(a);
			if(lc==null)
				throw new Exception("Unjoinable table in query: " + a);
			int sz = lc.size();
			if(sz==1){
				singleRelations++;
				write("  "+a+" has a single relationship.");
			}
			if(sz>1){
				multiRelations++;
				write("  "+a+" has multiple relationships ("+sz+")");
			}
		}
		if("STRICT".equals(checkStatus) && singleRelations!=2)
			throw new Exception("Wrong number of endpoints in join path: "+singleRelations+" should be 2");

		// pick one of the single relationships
		for (String a : tables) {
			List<QConstraint> lc = tableRelationships.get(a);
			if(lc.size()==1){
				write("Starting with "+a+" because it has a single relationship");
				fpath.append("\n ");
				fpath.append(dbsess.formatIdentifier(a));
				fpath.append("\n");
				chase(a,fpath,dbsess,tableRelationships,(QRelationConstraint) lc.get(0), 0);
				break;
			}
		}

		return fpath.toString();
	}

	public void chase(String a, StringBuffer fpath, DBSession dbsess,
			HashMap<String,List<QConstraint>> tableRelationships,
			QRelationConstraint currentRelationship, final int chaseCount) throws Exception {
		if (chaseCount == maxChase)
			throw new DBException("Could not resolve query, please simplify.");
		String b = currentRelationship.t_tfspec.getTable();
		if(b.equals(a)) b = currentRelationship.f_tfspec.getTable();
		List<QConstraint> ql = tableRelationships.get(b);
		fpath.append("" + findJoinType(b));
		fpath.append("JOIN\n " + dbsess.formatIdentifier(b)
				+ "\nON\n ");
		fpath.append(currentRelationship.getSQL(dbsess));
		fpath.append("\n");
		if(ql.size()==1){
			write(b+" only has one relationship, done");
			return;
		}
		for(QConstraint qc : ql){
			if(!qc.equals(currentRelationship)){
				write("Chasing "+b);
				chase(b, fpath, dbsess, tableRelationships, (QRelationConstraint) qc, chaseCount+1);
			}
		}
	}

	public ArrayList<QColumn> getColumnList() {
		return columnList;
	}

	public String getSQL(final DBSession ds) {
		final StringBuffer buf = new StringBuffer(1024);
		buf.append("SELECT\n");
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
				buf.append(", \n");
			else buf.append(" \n");
		}
		for (final String explicitJoinTable : explicitJoins.keySet())
			if (!tables.contains(explicitJoinTable))
				tables.add(explicitJoinTable);
		for (final String neededTable : unjoinedTables)
			if (!tables.contains(neededTable))
				tables.add(neededTable);
		buf.append("FROM ");
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
		write(buf.toString());
		return buf.toString();
	}
	
	/**
	 * Programatically set the check status
	 * @param checkStatus LOOSE | STRICT
	 */
	public void setCheckStatus(final String checkStatus) {
		this.checkStatus = checkStatus;
	}

	public void loadConfig(final Element el) throws Exception {
		final NodeList nl = el.getChildNodes();
		String checkStatus = el.getAttribute("check");
		if (checkStatus != null)
			this.checkStatus = checkStatus;
		String maxChase = el.getAttribute("maxChase");
		ArrayList<String> implicitTables = new ArrayList<String>();
		if (maxChase != null)
			try {
				this.maxChase = Integer.parseInt(maxChase);
			} catch (NumberFormatException e) {
				this.maxChase = Integer.MAX_VALUE;
			}
		for (int i = 0; i < nl.getLength(); i++) {
			final Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				final Element e = (Element) n;
				final String name = e.getNodeName();
				if ("select".equals(name)) {
					final QColumn qc = new QColumn();
					qc.loadConfig(e);
					write("Created " + qc);
					implicitTables.add(qc.getName().getTable());
					select(qc);
				} else if ("idiom".equals(name)) {
					final QIdiom qi = (QIdiom) Class.forName(
							e.getAttribute("class")).newInstance();
					qi.loadConfig(e);
					idiom(qi);
				} else if ("constraint".equals(name)) {
					write("Gotta build: " + e.getAttribute("class"));
					final QConstraint qc = (QConstraint) Class.forName(
							e.getAttribute("class")).newInstance();
					qc.loadConfig(e);
					constraints = (QConstraintGroup) qc;
				}
			}
		}
		for (int i = 0; i < nl.getLength(); i++) {
			final Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("table")) {
				if (!implicitTables.contains(n.getTextContent()))
					unjoinedTables.add(n.getTextContent());
			}
		}
	}

	public Element saveConfig(final Document doc) throws Exception {
		final Element el = doc.createElement("query");
		el.setAttribute("version", "1.1");
		if (!"STRICT".equals(checkStatus))
			el.setAttribute("check", checkStatus);
		if (maxChase != Integer.MAX_VALUE)
			el.setAttribute("maxChase", maxChase+"");
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
	
	private void write(String out) {
		//System.out.println(out);
	}

}
