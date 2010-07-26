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

import java.util.UUID;

import org.gogoego.util.db.DBSession;
import org.gogoego.util.db.SQLDateHelper;
import org.gogoego.util.db.shared.CanonicalColumnName;
import org.gogoego.util.db.shared.NumericLiteral;
import org.gogoego.util.db.shared.StringLiteral;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class QComparisonConstraint implements QConstraint {

	public static final int NUMERIC_ARGUMENT = 1;
	public static final int STRING_ARGUMENT = 1;
	private int argumentType = STRING_ARGUMENT;
	public Boolean ask;

	public Object compareValue;
	public int comparisonType;

	public String id = UUID.randomUUID().toString();

	public CanonicalColumnName tfspec;

	public QComparisonConstraint() {
	}

	public QComparisonConstraint(final CanonicalColumnName tfspec,
			final int comparisonType, final Object compareValue) {
		this.tfspec = tfspec;
		this.comparisonType = comparisonType;
		this.compareValue = compareValue;
	}

	public QComparisonConstraint(final CanonicalColumnName tfspec,
			final int comparisonType, final Object compareValue,
			final int argumentType) {
		this.argumentType = argumentType;
		this.tfspec = tfspec;
		this.comparisonType = comparisonType;
		this.compareValue = compareValue;
	}

	public QConstraint findByID(final String id) {
		if (id == null)
			return null;
		if (id.equals(getID()))
			return this;
		return null;
	}

	public String getID() {
		if ((id == null) || ("".equals(id)))
			id = UUID.randomUUID().toString();
		return id;
	}

	public String getSQL(final DBSession ds) {
		final StringBuffer sb = new StringBuffer(512);
		final String fqfn = ds.formatCanonicalColumnName(tfspec);
		if (comparisonType == CT_CONTAINS_IGNORE_CASE)
			sb.append(ds.fnLowerCase(fqfn));
		else
			sb.append(fqfn);
		// TODO: this compiles, but doesn't work -- need to refactor to use
		// Column objects
		String fqval = null;
		if (compareValue instanceof CanonicalColumnName)
			fqval = ds
					.formatCanonicalColumnName((CanonicalColumnName) compareValue);
		else if ((argumentType == NUMERIC_ARGUMENT)
				&& (compareValue instanceof Number))
			fqval = ds.formatLiteral(new NumericLiteral((Number) compareValue));
		else
			fqval = ds
					.formatLiteral(new StringLiteral(compareValue == null ? null : compareValue.toString()));
		if (comparisonType == CT_EQUALS)
			if (compareValue == null)
				sb.append(" IS NULL");
			else {
				sb.append("=");
				sb.append(fqval);
			}
		if (comparisonType == CT_NOT)
			if (compareValue == null)
				sb.append(" IS NOT NULL");
			else {
				sb.append("!=");
				sb.append(fqval);
			}
		if (comparisonType == CT_GT) {
			sb.append(">");
			sb.append(fqval);
		}
		if (comparisonType == CT_LT) {
			sb.append("<");
			sb.append(fqval);
		}
		if (comparisonType == CT_CONTAINS) {
			sb.append(" LIKE ");
			final String ncv = "%" + compareValue + "%";
			fqval = ds.formatLiteral(new StringLiteral(ncv));
			sb.append(fqval);
		}
		if (comparisonType == CT_CONTAINS_IGNORE_CASE) {
			sb.append(" LIKE ");
			
			final String ncv = "%" + compareValue.toString().toLowerCase() + "%";
			fqval = ds.formatLiteral(new StringLiteral(ncv));
			sb.append(fqval.toLowerCase());
		}
		if (comparisonType == CT_STARTS_WITH) {
			sb.append(" LIKE ");
			final String ncv = "" + compareValue + "%";
			fqval = ds.formatLiteral(new StringLiteral(ncv));
			sb.append(fqval);
		}
		if (comparisonType == CT_ENDS_WITH) {
			sb.append(" LIKE ");
			final String ncv = "%" + compareValue;
			fqval = ds.formatLiteral(new StringLiteral(ncv));
			sb.append(fqval);
		}
		return sb.toString();
	}

	public void loadConfig(final Element config) {
		id = config.getAttribute("id");
		try {
			tfspec = new CanonicalColumnName(config.getAttribute("fieldspec"));
		} catch (final Exception poorly_handled) {
			tfspec = null;
		}
		comparisonType = Integer
				.parseInt(config.getAttribute("comparisonType"));
		final String a = config.getAttribute("ask");
		if ((a != null) && (!"".equals(a)))
			ask = Boolean.parseBoolean(a);
		final String valueClass = config.getAttribute("valueClass");
		if ((valueClass != null) && (!"".equals(valueClass)))
			if (valueClass.equals("java.lang.Integer"))
				compareValue = Integer.parseInt(config.getAttribute("value"));
			else if (valueClass.equals("java.lang.Float"))
				compareValue = Float.parseFloat(config.getAttribute("value"));
			else if (valueClass.equals("java.lang.Double"))
				compareValue = Double.parseDouble(config.getAttribute("value"));
			else if (valueClass.equals("java.lang.Boolean"))
				compareValue = Boolean.parseBoolean(config.getAttribute("value"));
			else if (valueClass.equals("java.util.Date"))
				compareValue = SQLDateHelper.parse(SQLDateHelper.sqlDateFormat, config.getAttribute("value"));
			else
				compareValue = config.getAttribute("value");
	}

	public Element saveConfig(final Document doc) {
		final Element el = doc.createElement("constraint");
		if (ask != null)
			el.setAttribute("ask", ask.toString());
		el.setAttribute("id", getID());
		el.setAttribute("class", this.getClass().getName());
		el.setAttribute("fieldspec", tfspec.toString());
		el.setAttribute("comparisonType", Integer.toString(comparisonType));
		if ((compareValue != null) && (!"".equals(compareValue))) {
			el.setAttribute("value", compareValue.toString());
			el.setAttribute("valueClass", compareValue.getClass().getName());
		}
		return el;
	}
}
