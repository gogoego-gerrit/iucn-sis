package org.iucn.sis.server.api.queries;



public abstract class BaseCannedQueries implements CannedQueries {
	
	protected String format(String query, Object... params) {
		return String.format(query, params);
	}

}
