package org.iucn.sis.shared.acl;

public class InsufficientRightsException extends Exception {
	public InsufficientRightsException() {
		super("You do not have sufficient rights to perform this action.");
	}
}
