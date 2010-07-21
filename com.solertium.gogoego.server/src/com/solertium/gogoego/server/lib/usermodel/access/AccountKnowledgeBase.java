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

import java.util.Date;
import java.util.Iterator;

import com.solertium.gogoego.server.lib.usermodel.PolicedAccount;
import com.solertium.gogoego.server.lib.usermodel.resource.Resource;
import com.solertium.gogoego.server.lib.usermodel.resource.ResourceAccessType;

/**
 * A UserKnowledgeBase is a set of Rules. Rules govern resources, so a user's
 * knowledge base is a collection of the resources it has some governing info
 * on.
 * 
 * @author adam.schwartz
 */
public class AccountKnowledgeBase {
	private final RuleSet myRules = new RuleSet();
	private final PolicedAccount owner;

	public AccountKnowledgeBase(final PolicedAccount user) {
		owner = user;
	}

	public void addRule(final Resource resource, final ResourceAccessType access, final Date expires) {
		myRules.addRule(new Rule(owner, resource, access, expires));
	}

	public boolean contains(final Resource var) {
		for (final Iterator<Rule> iter = myRules.iterator(); iter.hasNext();)
			if (iter.next().resolve(owner, var))
				return true;

		return false;
	}

	public Rule getRule(final Resource var) {
		for (final Iterator<Rule> iter = myRules.iterator(); iter.hasNext();) {
			final Rule curRule = iter.next();
			if (curRule.resolve(owner, var))
				return curRule;
		}

		return null;
	}
}
