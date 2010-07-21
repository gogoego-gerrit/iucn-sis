package org.iucn.sis.shared.acl;

public class GlobalScope extends Scope {

	public GlobalScope() {
	}

	@Override
	public boolean matches(Object requirement) {
		return true;
	}

	@Override
	public String toString() {
		return "Global Scope";
	}

}
