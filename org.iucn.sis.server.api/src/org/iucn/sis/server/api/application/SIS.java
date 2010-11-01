package org.iucn.sis.server.api.application;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.naming.NamingException;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.EditIO;
import org.iucn.sis.server.api.io.FieldIO;
import org.iucn.sis.server.api.io.InfratypeIO;
import org.iucn.sis.server.api.io.IsoLanguageIO;
import org.iucn.sis.server.api.io.NoteIO;
import org.iucn.sis.server.api.io.PermissionIO;
import org.iucn.sis.server.api.io.PrimitiveFieldIO;
import org.iucn.sis.server.api.io.ReferenceIO;
import org.iucn.sis.server.api.io.RegionIO;
import org.iucn.sis.server.api.io.RelationshipIO;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.locking.FileLocker;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.OnlineUtil;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.debug.Debugger;
import org.iucn.sis.shared.api.models.User;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.representation.Representation;

import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.authentication.AuthnGuard;
import com.solertium.util.restlet.authentication.Authenticator.AccountNotFoundException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.provider.VersionedFileVFS;

public class SIS {

	private static SIS impl;

	protected final AssessmentIO assessmentIO;
	protected final TaxonIO taxonIO;
	protected final UserIO userIO;
	protected final PermissionIO permissionIO;
	protected final WorkingSetIO workingSetIO;
	protected final InfratypeIO infratypeIO;
	protected final FileLocker locker;
	protected final TaxomaticIO taxomaticIO;
	protected final VFS vfs;
	protected final String vfsroot;
	protected final IsoLanguageIO isoLanguageIO;
	protected final RelationshipIO relationshipIO;
	protected final RegionIO regionIO;
	protected final ReferenceIO referenceIO;
	protected final PrimitiveFieldIO primitiveFieldIO;
	protected final FieldIO fieldIO;
	protected final EditIO editIO;
	protected final NoteIO noteIO;
	protected final ExecutionContext ec, lookups;
	protected Properties settings;

	protected SIS() {
		Debug.setInstance(new SISDebugger());
		Debug.println("map of properties is: {0}", GoGoEgo.getInitProperties());
		
		try {
			vfsroot = GoGoEgo.getInitProperties().getProperty("sis_vfs");
			vfs = VFSFactory.getVFS(new File(vfsroot));
			taxomaticIO = new TaxomaticIO((VersionedFileVFS) vfs);
			assessmentIO = new AssessmentIO((VersionedFileVFS) vfs);
			taxonIO = new TaxonIO((VersionedFileVFS) vfs);
			userIO = new UserIO((VersionedFileVFS) vfs);
			permissionIO = new PermissionIO();
			workingSetIO = new WorkingSetIO((VersionedFileVFS) vfs);
			locker = new FileLocker();
			infratypeIO = new InfratypeIO();
			isoLanguageIO = new IsoLanguageIO();
			relationshipIO = new RelationshipIO();
			primitiveFieldIO = new PrimitiveFieldIO();
			regionIO = new RegionIO();
			referenceIO = new ReferenceIO();
			editIO = new EditIO();
			noteIO = new NoteIO();
			fieldIO = new FieldIO();
			
			ec = new SystemExecutionContext(DBSessionFactory.getDBSession(getDBSessionName()));
			ec.setExecutionLevel(ExecutionContext.READ_WRITE);
			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
			
			lookups = new SystemExecutionContext("sis_lookups");
			lookups.setAPILevel(ExecutionContext.SQL_ALLOWED);
			lookups.setExecutionLevel(ExecutionContext.ADMIN);
		} catch (NotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NamingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		SISPersistentManager manager = (SISPersistentManager) SISPersistentManager.instance();
	}

	public static SIS get() {
		if (impl == null)
			impl = new SIS();
		return impl;
	}

	public AssessmentIO getAssessmentIO() {
		return assessmentIO;
	}

	public PermissionIO getPermissionIO() {
		return permissionIO;
	}

	public UserIO getUserIO() {
		return userIO;
	}

	public TaxonIO getTaxonIO() {
		return taxonIO;
	}

	public WorkingSetIO getWorkingSetIO() {
		return workingSetIO;
	}

	public FileLocker getLocker() {
		return locker;
	}
	
	public PrimitiveFieldIO getPrimitiveFieldIO() {
		return primitiveFieldIO;
	}
	
	public EditIO getEditIO() {
		return editIO;
	}
	
	public Properties getSettings(Context context) {
		if (settings != null)
			return settings;
		
		final String rootFolder = 
			GoGoEgo.getInitProperties().getProperty("sis_settings", 
					"/ebs/sis/test/files/settings");
		
		Properties settings = new Properties();
		try {
			settings = new Properties();
			settings.load(new FileReader(new File(rootFolder + "/global.properties")));
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
			return new Properties();
		}
		
		this.settings = settings;
		
		return settings;
	}

	public AuthnGuard getGuard(Context context) {
		
		return new AuthnGuard(context, ChallengeScheme.HTTP_BASIC, "User Service") {
			@Override
			protected boolean bypassAuth(Request request) {
				String path = request.getResourceRef().getPath();
				if (path.equals("/") || path.equalsIgnoreCase("/favicon.ico"))
					return true;
				else if (request.getProtocol() == Protocol.RIAP)
					return true;
				else if (path.startsWith("/reports"))
					return true;
				else if (path.startsWith("/workingSetImporter"))
					return true;

				return false;
			}

			@Override
			protected void setDefaultAuthenticator() {
				authenticators.put(getRealm(), new SISDBAuthenticator(ec));
			}

			@Override
			protected void doRemoveUser(final Request request, final Response response, final String domain) {
				String currentUser = null;
				if (!bypassAuth(request)) {
					currentUser = SIS.get().getUsername(request);
				}

				String userToDelete = null;
				try {
					NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
					ndoc.parse(request.getEntityAsText());
					userToDelete = ndoc.getDocumentElement().getElementByTagName("u").getText();
				} catch (NullPointerException e1) {
					e1.printStackTrace();
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return;
				}
				if (userToDelete == null || (currentUser == null && !bypassAuth(request))) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				} else if (currentUser != null && currentUser.equals(userToDelete)) {
					response.setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED, "You can not delete yourself.");
				} else {
					try {
						if (getAuthenticator(domain).deleteUser(userToDelete)) {
							response.setStatus(Status.SUCCESS_OK);
						} else {
							response.setStatus(Status.SERVER_ERROR_INTERNAL);
						}
					} catch (AccountNotFoundException e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL);
					}
				}
			}

		};
	}

	public User getUser(Request request) {
		String username = request.getChallengeResponse().getIdentifier();
		if( username != null ) {
			return getUserIO().getUserFromUsername(username);
		} else
			return null;
	}

	public String getUsername(Request request) {
		return request.getChallengeResponse().getIdentifier();
	}

	public boolean isHostedMode() {
		return GoGoEgo.getInitProperties().containsKey("HOSTED_MODE");
	}

	public VFS getVFS() {
		return vfs;
	}

	public String getVfsroot() {
		return vfsroot;
	}

	public NativeDocument newNativeDocument(ChallengeResponse challengeResponse) {
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		String base64Encoded = "";
		if (challengeResponse != null)
			base64Encoded = challengeResponse.getIdentifier() + ":" + new String(challengeResponse.getSecret());
		else
			base64Encoded = "riapInternal:secretInternalCall";

		ndoc.setHeader("Authorization", "Basic " + Base64.encode(base64Encoded.getBytes(), false));
		return ndoc;
	}

	public static boolean amIOnline() {
		return OnlineUtil.amIOnline();
	}

	boolean isEncodeableEntity(Representation entity) {
		return entity.getMediaType().equals(MediaType.TEXT_XML) && entity.getSize() > 2048;
	}

	public InfratypeIO getInfratypeIO() {
		return infratypeIO;
	}

	public TaxomaticIO getTaxomaticIO() {
		return taxomaticIO;
	}

	public SISPersistentManager getManager() throws PersistentException {
		return SISPersistentManager.instance();
	}

	public IsoLanguageIO getIsoLanguageIO() {
		return isoLanguageIO;
	}

	public RelationshipIO getRelationshipIO() {
		return relationshipIO;
	}

	public RegionIO getRegionIO() {
		return regionIO;
	}
	
	public ReferenceIO getReferenceIO() {
		return referenceIO;
	}
	
	public NoteIO getNoteIO() {
		return noteIO;
	}
	
	public FieldIO getFieldIO() {
		return fieldIO;
	}
	
	public String getDBSessionName() {
		return "sis";
	}
	
	public ExecutionContext getExecutionContext() {
		return ec;
	}

	public ExecutionContext getLookupDatabase() {
		return lookups;
	}
	
	public static class SISDebugger implements Debugger {
		
		@Override
		public void println(Throwable e) {
			GoGoEgo.debug().println("{0}", e);
		}
		
		@Override
		public void println(Object obj) {
			GoGoEgo.debug().println(obj);
		}
		
		@Override
		public void println(String template, Object... args) {
			GoGoEgo.debug().println(template, args);
		}
		
	}
	
}
