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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.gogoego.util.db.DBSession;
import org.gogoego.util.getout.GetOut;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class QConstraintGroup implements QConstraint {

	public ArrayList<Object> constraints = new ArrayList<Object>();

	public String id = UUID.randomUUID().toString();

	public QConstraintGroup() {
	}

	public void addConstraint(final int operator, final QConstraint c) {
		if (!constraints.isEmpty())
			constraints.add(Integer.valueOf(operator));
		constraints.add(c);
	}

	public void addConstraint(final QConstraint c) {
		if (constraints.size() > 0)
			constraints.add(Integer.valueOf(QConstraint.CG_AND));
		constraints.add(c);
	}

	public QConstraint findByID(final String id) {
		if (id == null)
			return null;
		if (id.equals(getID()))
			return this;
		final Iterator<Object> it = constraints.iterator();
		while (it.hasNext()) {
			final Object in = it.next();
			if (in instanceof QConstraint) {
				final QConstraint c = (QConstraint) in;
				final QConstraint ret = c.findByID(id);
				if (ret != null)
					return ret;
			}
		}
		return null;
	}

	public String getID() {
		if ((id == null) || ("".equals(id)))
			id = UUID.randomUUID().toString();
		return id;
	}

	public String getSQL(final DBSession ds) {
		final StringBuffer sb = new StringBuffer(512);
		final Iterator<Object> it = constraints.iterator();
		if (constraints.size() > 1)
			sb.append("(");
		boolean first = true;
		while (it.hasNext()) {
			final Object in = it.next();
			// write("QC is " + in.getClass().getSimpleName());
			if (in instanceof Integer) {
				if (!first)
					if ((int) (Integer) in == CG_AND)
						sb.append(" AND ");
					else
						sb.append(" OR ");
			} else {
				sb.append(" ");
				sb.append(((QConstraint) in).getSQL(ds));
			}
			first = false;
		}
		if (constraints.size() > 1)
			sb.append(")");
		return sb.toString();
	}

	public boolean isEmpty() {
		return (constraints.size() < 1);
	}

	public void loadConfig(final Element config) throws Exception {
		id = config.getAttribute("id");
		write("There are " + config.getChildNodes().getLength() + " nodes");
		final NodeList children = config.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final Node n = children.item(i);
			
			if (n.getNodeName().equals("operator")) {
				final Node attr = n.getAttributes().getNamedItem("mode");
				String mode = null;
				if (attr != null)
					mode = attr.getTextContent();
				if ((mode != null) && (!"".equals(mode)))
					constraints.add(Integer.parseInt(mode));
				
				write("Mode: " + mode);
				
				NodeList opChildNodes = n.getChildNodes();
				Element child = null;
				for (int r = 0; r < opChildNodes.getLength(); r++) {
					write("- " + opChildNodes.item(r).getNodeName());
					if (opChildNodes.item(r).getNodeName().equals("constraint")) {
						child = (Element)opChildNodes.item(r);
						break;
					}
				}
				
				QConstraint c = null;
				try {
					write("Gotta make: " + child.getAttribute("class"));
					c = (QConstraint) Class.forName(child.getAttribute("class"))
							.newInstance();
				} catch (final Exception weaklyHandled) {
					GetOut.log(weaklyHandled);
				}
				if (c != null) {
					c.loadConfig(child);
					constraints.add(c);
				}
			}
		}
	}

	public void remove(final int index) {
		final Object a = constraints.get(index);
		if (a instanceof Integer)
			return; // mistake; don't remove an operator
		// directly
		if (index > 0) {
			final Object operator = constraints.get(index - 1);
			if (operator instanceof Integer) {
				constraints.remove(index - 1); // remove the preceding operator
				constraints.remove(index - 1); // remove the desired item
			}
		} else {
			constraints.remove(index); // remove the desired (first) item
			constraints.remove(index); // remove the subsequent operator
		}
	}

	public void removeByID(final String id) {
		for (int i = 0; i < constraints.size(); i++) {
			final Object in = constraints.get(i);
			if (in instanceof QConstraintGroup) {
				final QConstraintGroup g = (QConstraintGroup) in;
				if (id.equals(g.getID())) {
					remove(i);
					return;
				}
				g.removeByID(id);
			} else if (in instanceof QConstraint) {
				final QConstraint c = (QConstraint) in;
				if (id.equals(c.getID())) {
					remove(i);
					return;
				}
			}
		}
	}

	public Element saveConfig(final Document doc) throws Exception {
		final Element el = doc.createElement("constraint");
		el.setAttribute("id", getID());
		el.setAttribute("class", this.getClass().getName());
		final Iterator<Object> it = constraints.iterator();
		while (it.hasNext()) {
			final Object in = it.next();
			if (in instanceof Integer) {
				final Integer operator = (Integer) in;
				final Element opEl = doc.createElement("operator");
				opEl.setAttribute("mode", "" + operator);
				final QConstraint c = (QConstraint) it.next();
				opEl.appendChild(c.saveConfig(doc));
				el.appendChild(opEl);
			} else {
				final Element opEl = doc.createElement("operator");
				final QConstraint c = (QConstraint) in;
				opEl.appendChild(c.saveConfig(doc));
				el.appendChild(opEl);
			}
		}
		return el;
	}
	
    public void getFieldsWithAskValues(ArrayList<QComparisonConstraint> list) {
    	for (int i = 0; i < constraints.size(); i++) {
    		Object cur = constraints.get(i);
    		if (cur instanceof QComparisonConstraint) {
    			QComparisonConstraint current = (QComparisonConstraint)cur;
    			if (current.ask != null && current.ask.booleanValue())
    				list.add(current);
    		}
    		else if (cur instanceof QConstraintGroup)
    			((QConstraintGroup)cur).getFieldsWithAskValues(list);
    	}
    }

	public int size() {
		return constraints.size();
	}
	
	private void write(String out) {
		//GetOut.log(out);
	}
}
