package org.iucn.sis.server.extensions.zendesk;

public class Settings {
	
	public static final String ZENDESK_URL = "org.iucn.sis.server.extension.zendesk.url";
	public static final String ZENDESK_USER = "org.iucn.sis.server.extension.zendesk.user";
	public static final String ZENDESK_PASSWORD = "org.iucn.sis.server.extension.zendesk.password";
	
	public static final String ASSEMBLA_EMAIL = "org.iucn.sis.server.extensions.support.assembla.email";
	public static final String ASSEMBLA_ASSIGNED = "org.iucn.sis.server.extensions.support.assembla.assigned";
	
	public static final String[] ALL = new String[] {
		ZENDESK_URL, ZENDESK_USER, ZENDESK_PASSWORD, 
		ASSEMBLA_EMAIL, ASSEMBLA_ASSIGNED
	};

}
