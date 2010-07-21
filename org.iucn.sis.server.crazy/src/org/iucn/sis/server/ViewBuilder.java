package org.iucn.sis.server;

import java.util.ArrayList;

public class ViewBuilder {
	public static String[] getViews() {
		ArrayList<String> views = new ArrayList<String>();

		views.add(ServerUtils.getFileContentsAsString("xmlAssessments/standardView.xml"));
		views.add(ServerUtils.getFileContentsAsString("xmlAssessments/campView.xml"));
		views.add(ServerUtils.getFileContentsAsString("xmlAssessments/JimTestView.xml"));
		views.add(ServerUtils.getFileContentsAsString("xmlAssessments/FullView.xml"));
		return views.toArray(new String[views.size()]);
	}

}
