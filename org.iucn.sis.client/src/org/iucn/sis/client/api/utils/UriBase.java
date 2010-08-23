package org.iucn.sis.client.api.utils;

import com.google.gwt.user.client.Window;

public class UriBase {

	private static UriBase base;
	
	public static final UriBase getInstance() {
		if (base == null)
			base = new UriBase();
		return base;
	}
	
	private final String baseUrl;
	
	private UriBase() {
		final boolean hostedMode = Window.Location.getParameter("gwt.codesvr") != null;
		baseUrl = hostedMode ? "/proxy-service/apps" : "/apps";
	}
	
	@Override
	public String toString() {
		return getSISBase();
	}

	public String getRecentAssessmentsBase() {
		return getBase() + "/org.iucn.sis.server.extensions.recentasms";
	}
	
	public String getNotesBase() {
		return getBase() + "/org.iucn.sis.server.extensions.notes";
	}
	
	public String getZendeskBase() {
		return getBase() + "/org.iucn.sis.server.extensions.zendesk";
	}

	public String getSISBase() {
		return getBase() + "/org.iucn.sis.server";
	}
	
	public String getWorkflowBase() {
		return getBase() + "/org.iucn.sis.server.extensions.workflow";
	}
	
	public String getFindReplaceBase() {
		return getBase() + "/org.iucn.sis.server.extensions.findreplace";
	}
	
	public String getReferenceBase() {
		return getBase() + "/org.iucn.sis.server.extensions.references";
	}

	public String getAttachmentBase() {
		return getBase() + "/org.iucn.sis.server.extensions.attachments";
	}
	
	public String getBatchChangeBase() {
		return getBase() + "/org.iucn.sis.server.extensions.batchchanges";
	}
	
	public String getCommentsBase() {
		return getBase() + "/org.iucn.sis.server.extensions.comments";
	}
	
	public String getDefinitionBase() {
		return getBase() + "/org.iucn.sis.server.extensions.definitions";
	}
	
	public String getDEMBase() {
		return getBase() + "/org.iucn.sis.server.extensions.demimport";
	}
	
	public String getExportBase() {
		return getBase() + "/org.iucn.sis.server.extensions.export";
	}
	
	public String getImageBase() {
		return getBase() + "/org.iucn.sis.server.extensions.images";
	}
	
	public String getIntegrityBase() {
		return getBase() + "/org.iucn.sis.server.extensions.integrity";
	}
	
	public String getMessagingBase() {
		return getBase() + "/org.iucn.sis.server.extensions.messaging";
	}
	
	public String getOfflineBase() {
		return getBase() + "/org.iucn.sis.server.extensions.offline";
	}
	
	public String getRedlistBase() {
		return getBase() + "/org.iucn.sis.server.extensions.redlist";
	}
	
	public String getReportBase() {
		return getBase() + "/org.iucn.sis.server.extensions.reports";
	}
	
	public String getSpatialBase() {
		return getBase() + "/org.iucn.sis.server.extensions.spatial";
	}
	
	public String getTagBase() {
		return getBase() + "/org.iucn.sis.server.extensions.tags";
	}
	
	public String getUserBase() {
		return getBase() + "/org.iucn.sis.server.extensions.user";
	}
	
	
	private String getBase() {
		return baseUrl;
	}

}
