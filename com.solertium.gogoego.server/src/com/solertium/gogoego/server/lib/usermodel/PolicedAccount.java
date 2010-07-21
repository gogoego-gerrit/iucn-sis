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

package com.solertium.gogoego.server.lib.usermodel;

import java.util.Date;

import com.solertium.gogoego.server.lib.usermodel.access.AccountKnowledgeBase;
import com.solertium.gogoego.server.lib.usermodel.resource.Resource;
import com.solertium.gogoego.server.lib.usermodel.resource.ResourceAccessType;

/**
 * Extending this class allows an account concept to include a collection of
 * rules about resources
 * 
 * @author adam.schwartz
 * 
 */
public abstract class PolicedAccount {
	private final AccountKnowledgeBase knowledgeBase;

	private Object signature;

	public PolicedAccount() {
		knowledgeBase = new AccountKnowledgeBase(this);
	}

	public PolicedAccount(final Object signature) {
		knowledgeBase = new AccountKnowledgeBase(this);
	}

	public void addRule(final Resource resource, final ResourceAccessType accessRule, final Date expires) {
		knowledgeBase.addRule(resource, accessRule, expires);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof PolicedAccount)
			if (((PolicedAccount) obj).matches(signature))
				return true;

		return false;
	}

	public AccountKnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public Object getSignature() {
		return signature;
	}

	@Override
	public int hashCode() {
		return signature.hashCode();
	}

	public abstract boolean matches(Object otherSignature);

	public void setSignature(final Object signature) {
		this.signature = signature;
	}
}
