package org.iucn.sis.server.extensions.migration;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.api.application.SimpleSISApplication;

public class Application extends SimpleSISApplication {
	
	public static final String VERSION = "2.0";
	
	public Application() {
		super(RunMode.ONLINE);
	}
	
	@Override
	public void init() {
		List<String> paths = new ArrayList<String>();
		paths.add("/" + VERSION + "/list");
		paths.add("/" + VERSION + "/list/{id}");
		paths.add("/reports/styles.css");
		
		addResource(ListingResource.class, paths, false);
		addResource(ReportResource.class, "/" + VERSION + "/reports", false);
	}

}
