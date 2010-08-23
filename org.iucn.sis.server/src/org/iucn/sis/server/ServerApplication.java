package org.iucn.sis.server;

import java.util.ArrayList;
import java.util.Iterator;

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
import org.iucn.sis.server.restlets.taxa.CommonNameRestlet;
import org.iucn.sis.server.restlets.taxa.SynonymRestlet;
import org.iucn.sis.server.restlets.taxa.TaxomaticRestlet;
import org.iucn.sis.server.restlets.taxa.TaxonByStatusRestlet;
import org.iucn.sis.server.restlets.taxa.TaxonRestlet;
import org.iucn.sis.server.restlets.users.PermissionGroupsRestlet;
import org.iucn.sis.server.restlets.users.ProfileRestlet;
import org.iucn.sis.server.restlets.utils.FieldRestlet;
import org.iucn.sis.server.restlets.utils.LockManagementRestlet;
import org.iucn.sis.server.restlets.utils.RegionRestlet;
import org.iucn.sis.server.restlets.utils.SearchRestlet;
import org.iucn.sis.server.restlets.utils.StatusRestlet;
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
	
	protected final ArrayList<ServiceRestlet> services;
	
	
	public ServerApplication() {
		super();
		SISPersistentManager.instance();
		services = new ArrayList<ServiceRestlet>();
		
		if (GoGoEgo.getInitProperties().containsKey("UPDATE_URL"))
			GoGoEgo.getInitProperties().put("UPDATE_URL", "http://sis.iucnsis.org/getUpdates");
	}
	
	@Override
	public void init() {
		initServiceRoutes();
		initRoutes();		
		addResource(SIS.get().getGuard(app.getContext()), "/authn", true, true, true);		
	}
	
	protected void initServiceRoutes() {
		services.add(new StatusRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new TaxonRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new SynonymRestlet(app.getContext()));
		services.add(new CommonNameRestlet(app.getContext()));
		services.add(new FieldRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new WorkingSetRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new AssessmentRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new TaxomaticRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new WorkingSetExportImportRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new WorkingsetLogBuilder(SIS.get().getVfsroot(), app.getContext()));
		services.add(new TrashRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new RegionRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new TaxonByStatusRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new PermissionGroupsRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new LockManagementRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new AuthzRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new ProfileRestlet(SIS.get().getVfsroot(), app.getContext()));
		services.add(new SearchRestlet(SIS.get().getVfsroot(), app.getContext()));
		
		
		for (Iterator<ServiceRestlet> iter = services.iterator(); iter.hasNext();)
			addServiceToRouter(iter.next());
		
	}
	
	private void addServiceToRouter(ServiceRestlet curService) {
		addResource(curService, curService.getPaths(), true, true, false);
	}
	
	protected void initRoutes() {
		addResource(GWTClientResource.class, "/SIS", true, true, true);
		addResource(new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				try {
					response.setEntity(new InputRepresentation(SIS.get().getVFS().getInputStream(new VFSPath("/images/favicon.ico")),
							MediaType.IMAGE_ICON));
					response.setStatus(Status.SUCCESS_OK);
				} catch (NotFoundException e) {
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}

			}
		}, "/favicon.ico", true, true, true);
		addResource(SISVFSResource.class, "/raw", true, true, true);
//		addResource(VFSVersionAccessResource.class, "/revisions", true, true, true);
		addResource(AsmChangesResource.class, "/asmchanges/{asm_id}", true, true, false);		
		addResource(ServeUpdatesResource.class, "/getUpdates", true, false, true);
		addResource(ServeUpdatesResource.class, "/getUpdates/summary", true, false, true);
		addResource(new Restlet(app.getContext()) {
			@Override
					public void handle(Request request, Response response) {
						response.setStatus(Status.SUCCESS_OK);
						response.setEntity("<html><body>The online environment does not "
								+ "support automatic updates.", MediaType.TEXT_HTML);
					}
		}, "/update", true, false, true);
		
		addResource(UpdateResource.class, "/update", false, true, true);
		addResource(UpdateResource.class, "/update/summary", false, true, true);
		addResource(new Restlet(app.getContext()) {
			@Override
			public void handle(Request request, Response response) {
				response.setStatus(Status.SUCCESS_OK);
				response.setEntity("<html><body>Serving updates can only be done in " + "the online environment.",
						MediaType.TEXT_HTML);
			}
		}, "/getUpdates", false, true, true);
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
