/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */

package com.solertium.db.query;

import java.util.ArrayList;
import java.util.HashMap;

import com.solertium.db.CanonicalColumnName;

/**
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public abstract class BaseQuery {

	protected QConstraintGroup constraints = new QConstraintGroup();

	protected HashMap<String, QConstraint> explicitJoins = new HashMap<String, QConstraint>();

	protected ArrayList<QIdiom> idioms = new ArrayList<QIdiom>();

	public BaseQuery() {
		super();
	}

	public void constrain(final CanonicalColumnName tfspec,
			final int comparisonType, final Object compareValue) {
		constraints.addConstraint(new QComparisonConstraint(tfspec,
				comparisonType, compareValue));
	}

	public void constrain(final int operator, final CanonicalColumnName tfspec,
			final int comparisonType, final Object compareValue) {
		constraints.addConstraint(operator, new QComparisonConstraint(tfspec,
				comparisonType, compareValue));
	}

	public void constrain(final int operator, final QConstraint constraint) {
		constraints.addConstraint(operator, constraint);
	}

	public void constrain(final int operator, final String arbitrary) {
		constraints
				.addConstraint(operator, new QArbitraryConstraint(arbitrary));
	}

	public void constrain(final QConstraint constraint) {
		constraints.addConstraint(constraint);
	}

	public void idiom(final QIdiom idiom) {
		idioms.add(idiom);
	}

	public void join(final String table, final QConstraint constraint) {
		explicitJoins.put(table, constraint);
	}

}