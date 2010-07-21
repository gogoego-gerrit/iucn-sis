package org.iucn.sis.shared.acl;

import java.util.ArrayList;

public class UserScope extends Scope {

	private ArrayList<Scope> scopeSet;

	public UserScope() {
		this(new ArrayList<Scope>());
	}

	public UserScope(ArrayList<Scope> scopeSet) {
		this.scopeSet = new ArrayList<Scope>();
		this.scopeSet.addAll(scopeSet);
	}

	public void addScope(Scope scope) {
		scopeSet.add(scope);
	}

	@Override
	public boolean matches(Object requirement) {
		for (int i = 0; i < scopeSet.size(); i++) {
			if ((scopeSet.get(i)).matches(requirement)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String retString = "";
		for (int i = 0; i < scopeSet.size(); i++) {
			retString += scopeSet.get(i);
			if ((i + 1) != scopeSet.size())
				retString += ", ";
		}
		return retString;
	}

}
