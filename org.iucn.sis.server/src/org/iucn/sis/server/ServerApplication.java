package org.iucn.sis.server;

import java.util.Arrays;
import java.util.Collection;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.SISGlobalSettings;
import org.iucn.sis.server.restlets.assessments.AssessmentChangesRestlet;
import org.iucn.sis.server.restlets.assessments.AssessmentRestlet;
import org.iucn.sis.server.restlets.publication.PublicationRestlet;
import org.iucn.sis.server.restlets.publication.PublicationTargetRestlet;
import org.iucn.sis.server.restlets.schema.AssessmentSchemaRestlet;
import org.iucn.sis.server.restlets.taxa.CommonNameRestlet;
import org.iucn.sis.server.restlets.taxa.SynonymRestlet;
import org.iucn.sis.server.restlets.taxa.TaxomaticRestlet;
import org.iucn.sis.server.restlets.taxa.TaxonByStatusRestlet;
import org.iucn.sis.server.restlets.taxa.TaxonRestlet;
import org.iucn.sis.server.restlets.users.PermissionGroupsRestlet;
import org.iucn.sis.server.restlets.users.ProfileRestlet;
import org.iucn.sis.server.restlets.utils.FieldRestlet;
import org.iucn.sis.server.restlets.utils.LanguageRestlet;
import org.iucn.sis.server.restlets.utils.LockManagementRestlet;
import org.iucn.sis.server.restlets.utils.MultiDocumentRestlet;
import org.iucn.sis.server.restlets.utils.RegionRestlet;
import org.iucn.sis.server.restlets.utils.SearchRestlet;
import org.iucn.sis.server.restlets.utils.StatusRestlet;
import org.iucn.sis.server.restlets.utils.TaxaTaggingResource;
import org.iucn.sis.server.restlets.utils.TrashRestlet;
import org.iucn.sis.server.restlets.workingsets.WorkingSetExportImportRestlet;
import org.iucn.sis.server.restlets.workingsets.WorkingSetRestlet;
import org.iucn.sis.server.utils.SISVFSResource;
import org.iucn.sis.server.utils.logging.WorkingsetLogBuilder;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class ServerApplication extends SISApplication { 
	
	@Override
	protected Collection<String> getSettingsKeys() {
		return Arrays.asList(SISGlobalSettings.ALL);
	}
	
	/**
	 * Attached whether online or offline.
	 */
	public void initDual() {
		SISPersistentManager.instance();
		
		initServiceRoutes();
		initDualRoutes();
	}
	
	protected void initServiceRoutes() {
		addServiceToRouter(new StatusRestlet(app.getContext()));
		addServiceToRouter(new TaxonRestlet(app.getContext()));
		addServiceToRouter(new SynonymRestlet(app.getContext()));
		addServiceToRouter(new CommonNameRestlet(app.getContext()));
		addServiceToRouter(new FieldRestlet(app.getContext()));
		addServiceToRouter(new AssessmentSchemaRestlet(app.getContext()));
		addServiceToRouter(new WorkingSetRestlet(app.getContext()));
		addServiceToRouter(new AssessmentRestlet(app.getContext()));
		addServiceToRouter(new AssessmentChangesRestlet(app.getContext()));
		addServiceToRouter(new WorkingSetExportImportRestlet(app.getContext()));
		addServiceToRouter(new WorkingsetLogBuilder(app.getContext()));
		addServiceToRouter(new TrashRestlet(app.getContext()));
		addServiceToRouter(new RegionRestlet(app.getContext()));
		addServiceToRouter(new TaxonByStatusRestlet(app.getContext()));
		addServiceToRouter(new PermissionGroupsRestlet(app.getContext()));
		addServiceToRouter(new LockManagementRestlet(app.getContext()));
		addServiceToRouter(new ProfileRestlet(app.getContext()));
		addServiceToRouter(new SearchRestlet(app.getContext()));
		addServiceToRouter(new LanguageRestlet(app.getContext()));
		addServiceToRouter(new MultiDocumentRestlet(app.getContext()));
		addServiceToRouter(new PublicationRestlet(app.getContext()));
		addServiceToRouter(new PublicationTargetRestlet(app.getContext()));
	}
	
	private void addServiceToRouter(BaseServiceRestlet curService) {
		addResource(curService, curService.getPaths(), false);
	}
	
	protected void initDualRoutes() {
		addResource(SIS.get().getGuard(app.getContext()), "/authn", true);
		addResource(LatestGWTClientResource.class, "/SIS", true);
		addResource(NamedGWTClientResource.class, "/builds/SIS/{version}", true);
		addResource(TaxaTaggingResource.class, TaxaTaggingResource.getPaths(), false);
		addResource(SISVFSResource.class, "/raw", true);
	}
	
	@Override
	protected void initOnline() {
		initDual();
		
		addServiceToRouter(new TaxomaticRestlet(app.getContext()));
	}
	
	@Override
	protected void initOffline() {
		initDual();
	}
	
	public static class LatestGWTClientResource extends VersionedGWTClientResource {

		public LatestGWTClientResource(Context context, Request request,
				Response response) {
			super(context, request, response);
		}
		
		public String getVersion() {
			return null;
		}
		
	}
	
	public static class NamedGWTClientResource extends VersionedGWTClientResource {
		
		public NamedGWTClientResource(Context context, Request request,
				Response response) {
			super(context, request, response);
		}
		
		@SuppressWarnings("deprecation")
		public String getVersion() {
			return (String)getRequest().getAttributes().get("version");
		}
		
	}

}
