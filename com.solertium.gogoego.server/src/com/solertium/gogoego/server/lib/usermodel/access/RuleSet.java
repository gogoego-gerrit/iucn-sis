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

package com.solertium.gogoego.server.lib.usermodel.access;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.solertium.gogoego.server.lib.usermodel.PolicedAccount;
import com.solertium.gogoego.server.lib.usermodel.resource.Resource;

/**
 * Contains a set of Rules. Is Resolveable, based on a logical OR of the result
 * of resolve() for each of the rules in the set.
 * 
 * @author adam.schwartz
 * 
 */
public class RuleSet implements Resolveable {
	List<Rule> rules;

	public RuleSet() {
		rules = new ArrayList<Rule>();
	}

	/**
	 * Adds a Rule to the rule set if one does not exist that already governs
	 * the particular resource.
	 * 
	 * @param rule
	 * @return true if the rule was added, false if one already existed
	 */
	public boolean addRule(final Rule rule) {
		if (rules.contains(rule))
			return false;

		rules.add(rule);
		return true;
	}

	/**
	 * Adds a Rule to the rule set. If a rule already existed that governs the
	 * particular resource, it is overwritten.
	 * 
	 * @param rule
	 * @return success(true) or failure(false)
	 */
	public boolean forceAddRule(final Rule rule) {
		if (rules.contains(rule))
			rules.remove(rule);

		return rules.add(rule);
	}

	public List<Rule> getRules() {
		return rules;
	}

	public Iterator<Rule> iterator() {
		return rules.iterator();
	}

	public boolean resolve(final PolicedAccount user, final Resource variable) {
		for (final Rule rule : rules)
			if (rule.resolve(user, variable))
				return true;

		return false;
	}
}
