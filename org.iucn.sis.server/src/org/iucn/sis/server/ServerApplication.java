package org.iucn.sis.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gogoego.api.classloader.SimpleClasspathResource;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.restlets.assessments.AsmChangesResource;
import org.iucn.sis.server.restlets.assessments.AssessmentRestlet;
import org.iucn.sis.server.restlets.baserestlets.AuthzRestlet;
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
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.update.ServeUpdatesResource;
import com.solertium.update.UpdateResource;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;

public class ServerApplication extends SISApplication{ 
	
	public ServerApplication() {
		super();
		
		if (GoGoEgo.getInitProperties().containsKey("UPDATE_URL"))
			GoGoEgo.getInitProperties().put("UPDATE_URL", "http://sis.iucnsis.org/getUpdates");
	}
	
	@Override
	protected Collection<String> getSettingsKeys() {
		ArrayList<String> settings = new ArrayList<String>();
		settings.add("org.iucn.sis.schema");
		return settings;
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
		addServiceToRouter(new StatusRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new TaxonRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new SynonymRestlet(app.getContext()));
		addServiceToRouter(new CommonNameRestlet(app.getContext()));
		addServiceToRouter(new FieldRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new AssessmentSchemaRestlet(app.getContext()));
		addServiceToRouter(new WorkingSetRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new AssessmentRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new TaxomaticRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new WorkingSetExportImportRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new WorkingsetLogBuilder(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new TrashRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new RegionRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new TaxonByStatusRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new PermissionGroupsRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new LockManagementRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new AuthzRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new ProfileRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new SearchRestlet(SIS.get().getVfsroot(), app.getContext()));
		addServiceToRouter(new LanguageRestlet(SIS.get().getVfsroot(), app.getContext()));
	}
	
	private void addServiceToRouter(ServiceRestlet curService) {
		addResource(curService, curService.getPaths(), false);
	}
	
	protected void initDualRoutes() {
		addResource(SIS.get().getGuard(app.getContext()), "/authn", true);
		addResource(GWTClientResource.class, "/SIS", true);
		
		final List<String> taxaPaths = new ArrayList<String>();
		taxaPaths.add("/tagging/taxa/{tag}");
		taxaPaths.add("/tagging/taxa/{tag}/{mode}");
		addResource(TaxaTaggingResource.class, taxaPaths, false);
		
		addResource(new Restlet() {
			public void handle(Request request, Response response) {
				try {
					response.setEntity(new InputRepresentation(SIS.get().getVFS().getInputStream(new VFSPath("/images/favicon.ico")),
							MediaType.IMAGE_ICON));
					response.setStatus(Status.SUCCESS_OK);
				} catch (NotFoundException e) {
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}
			}
		}, "/favicon.ico",  true);
		
		addResource(SISVFSResource.class, "/raw", true);
		addResource(AsmChangesResource.class, "/asmchanges/{asm_id}", false);
	}
	
	@Override
	protected void initOnline() {
		initDual();
		
		addResource(ServeUpdatesResource.class, "/getUpdates", true);
		addResource(ServeUpdatesResource.class, "/getUpdates/summary", true);
		addResource(new Restlet(app.getContext()) {
			@Override
					public void handle(Request request, Response response) {
						response.setStatus(Status.SUCCESS_OK);
						response.setEntity("<html><body>The online environment does not "
								+ "support automatic updates.", MediaType.TEXT_HTML);
					}
		}, "/update", true);
	}
	
	@Override
	protected void initOffline() {
		initDual();
		
		addResource(UpdateResource.class, "/update", true);
		addResource(UpdateResource.class, "/update/summary", true);
		addResource(new Restlet(app.getContext()) {
			@Override
			public void handle(Request request, Response response) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity("<html><body>Serving updates can only be done in " + "the online environment.",
						MediaType.TEXT_HTML);
			}
		}, "/getUpdates", true);
	}
	
	public static class GWTClientResource extends SimpleClasspathResource {
		
		public GWTClientResource(Context context, Request request, Response response) {
			super(context, request, response);
			addGZIPHeader();
		}
		
		public String getBaseUri() {
			return "org/iucn/sis/client/compiled/public/SIS";
		}
		
		public ClassLoader getClassLoader() {
			return GoGoEgo.get().getClassLoaderPlugin("org.iucn.sis.client.compiled");
		}
		
		@Override
		public Representation represent(Variant variant) throws ResourceException {
			getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, true);
			Representation rep = super.represent(variant);
			if( rep.getMediaType().equals(MediaType.TEXT_HTML) || rep.getMediaType().equals(MediaType.TEXT_CSS)
					|| rep.getMediaType().equals(MediaType.TEXT_JAVASCRIPT))
				return new EncodeRepresentation(Encoding.GZIP, rep);
			else
				return rep;
		}
		
	}

}
