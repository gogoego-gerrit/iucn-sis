package org.iucn.sis.shared.helpers;

import java.util.HashMap;

public class PermissionSet {
	/**
	 * A HashMap that maps an operation as a string (Read, Write, etc.) to a Boolean
	 * value representing if that operation is allowed. 
	 */
	private HashMap<String, Boolean> operationToAllowed;

	public PermissionSet() {
		operationToAllowed = new HashMap<String, Boolean>();
		operationToAllowed.put(AuthorizableObject.READ, null);
		operationToAllowed.put(AuthorizableObject.WRITE, null);
		operationToAllowed.put(AuthorizableObject.CREATE, null);
		operationToAllowed.put(AuthorizableObject.DELETE, null);
		operationToAllowed.put(AuthorizableObject.GRANT, null);
		operationToAllowed.put(AuthorizableObject.USE_FEATURE, null);
	}

	PermissionSet(HashMap<String, Boolean> opToAllowed) {
		operationToAllowed = opToAllowed;
	}

	public Boolean check(String operation) {
		return operationToAllowed.get(operation);
	}

	HashMap<String, Boolean> getOperationToAllowed() {
		return operationToAllowed;
	}

	void setOperationToAllowed(HashMap<String, Boolean> operationToAllowed) {
		this.operationToAllowed = operationToAllowed;
	}

	public void set(String operation, Boolean value) {
		operationToAllowed.put(operation, value);
	}

	@Override
	public String toString() {
		Boolean cur = check(AuthorizableObject.READ);
		StringBuilder str = new StringBuilder();
		if( cur != null ) {
			str.append("r");
			str.append(cur ? "+" : "-");
		}

		cur = check(AuthorizableObject.WRITE);
		if( cur != null ) {
			str.append(str.length() > 0 ? ",w" : "w");
			str.append(cur ? "+" : "-");
		}

		cur = check(AuthorizableObject.CREATE);
		if( cur != null ) {
			str.append(str.length() > 0 ? ",c" : "c");
			str.append(cur ? "+" : "-");
		}

		cur = check(AuthorizableObject.DELETE);
		if( cur != null ) {
			str.append(str.length() > 0 ? ",d" : "d");
			str.append(cur ? "+" : "-");
		}

		cur = check(AuthorizableObject.GRANT);
		if( cur != null ) {
			str.append(str.length() > 0 ? ",g" : "g");
			str.append(cur ? "+" : "-");
		}

		cur = check(AuthorizableObject.USE_FEATURE);
		if( cur != null ) {
			str.append(str.length() > 0 ? ",u" : "u");
			str.append(cur ? "+" : "-");
		}

		return str.toString();
	}
}
