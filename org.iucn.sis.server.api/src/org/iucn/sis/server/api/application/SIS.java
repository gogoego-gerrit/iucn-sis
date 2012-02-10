package org.iucn.sis.server.api.application;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.naming.NamingException;

import org.gogoego.api.mail.InstanceMailer;
import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.locking.FileLocker;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.queries.CannedQueries;
import org.iucn.sis.server.api.queries.H2CannedQueries;
import org.iucn.sis.server.api.queries.PostgreSQLCannedQueries;
import org.iucn.sis.server.api.schema.AssessmentSchemaBroker;
import org.iucn.sis.server.api.utils.SISGlobalSettings;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.debug.Debugger;
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

import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.vendor.H2DBSession;
import com.solertium.db.vendor.PostgreSQLDBSession;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.mail.Mailer;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.restlet.authentication.AuthnGuard;
import com.solertium.util.restlet.authentication.Authenticator.AccountNotFoundException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

public class SIS {

	private static SIS impl;
	
	public static SIS get() {
		if (impl == null) {
			impl = new SIS();
			SISPersistentManager.instance();
		}
		return impl;
	}
	
	private final ExecutionContext lookups;
	private final String vfsroot;
	private final VFS vfs;
	
	private AssessmentSchemaBroker broker;
	private Properties settings;
	private ExecutionContext ec;
	private CannedQueries queries;
	
	private FileLocker locker;

	private SIS() {
		Debug.setInstance(new SISDebugger());
		
		final Properties properties = getSettings(null);
		Debug.println("map of properties is: {0}", properties);
		
		try {
			vfsroot = properties.getProperty(SISGlobalSettings.VFS, 
					properties.getProperty("sis_vfs"));
			vfs = VFSFactory.getVFS(new File(vfsroot));
			
			refreshDatabase();
			
			lookups = new SystemExecutionContext("sis_lookups");
			lookups.setAPILevel(ExecutionContext.SQL_ALLOWED);
			lookups.setExecutionLevel(ExecutionContext.ADMIN);
			if (lookups.getDBSession() instanceof PostgreSQLDBSession)
				lookups.getDBSession().setIdentifierCase(DBSession.CASE_UPPER);
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
		
		try {
			broker = new AssessmentSchemaBroker();
		} catch (Exception e) {
			//OK.
		}
	}

	public void refreshDatabase() {
		Properties settings = getSettings(null);
		Properties dbsession = new Properties();
		
		for (Object name : settings.keySet()) {
			String key = (String) name;
			if (key.startsWith("dbsession."))
				dbsession.setProperty(key, settings.getProperty(key));
		}
		
		try {
			DBSessionFactory.registerDataSources(dbsession);
			
			ec = new SystemExecutionContext(DBSessionFactory.getDBSession("sis"));
			ec.setExecutionLevel(ExecutionContext.READ_WRITE);
			ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
			
			if (ec.getDBSession() instanceof H2DBSession)
				queries = new H2CannedQueries(); 
			else
				queries = new PostgreSQLCannedQueries();
		} catch (NamingException e) {
			Debug.println(e);
			throw new RuntimeException(e);
		}
	}
	
	public AssessmentSchemaBroker getAssessmentSchemaBroker() {
		return broker;
	}

	public FileLocker getLocker() {
		if (locker == null)
			locker = new FileLocker();
		return locker;
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
	
	public void saveSettings(Context context) throws IOException {
		final Properties properties = getSettings(context);
		final String rootFolder = 
			GoGoEgo.getInitProperties().getProperty("sis_settings", 
					"/ebs/sis/test/files/settings");
		
		File folder = new File(rootFolder);
		if (!folder.exists())
			folder.mkdirs();
		
		properties.store(new FileWriter(new File(folder, "global.properties")), null);
	}
	
	public String getDefaultSchema() {
		return getSettings(null).getProperty(SISGlobalSettings.SCHEMA, "org.iucn.sis.server.schemas.redlist");
	}
	
	public Mailer getMailer() {
		return InstanceMailer.getInstance().getMailer();
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

			@SuppressWarnings("deprecation")
			protected void setDefaultAuthenticator() {
				authenticators.put(getRealm(), new SISDBAuthenticator(ec));
			}

			@Override
			protected void doRemoveUser(final Request request, final Response response, final String domain) {
				String currentUser = null;
				if (!bypassAuth(request)) {
					currentUser = request.getChallengeResponse().getIdentifier();
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

	public boolean isHostedMode() {
		return GoGoEgo.getInitProperties().containsKey("HOSTED_MODE");
	}

	public VFS getVFS() {
		return vfs;
	}
	
	public CannedQueries getQueries() {
		return queries;
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

	/**
	 * SIS is running online unless otherwise 
	 * stated via property.
	 * @return
	 */
	public static boolean amIOnline() {
		//return OnlineUtil.amIOnline();
		return !"false".equals(SIS.get().getSettings(null).getProperty(SISGlobalSettings.ONLINE, "true"));
	}
	
	/**
	 * SIS does NOT force users to use HTTPS 
	 * unless otherwise stated via property 
	 * @return
	 */
	public static boolean forceHTTPS() {
		return "true".equals(SIS.get().getSettings(null).getProperty(SISGlobalSettings.FORCE_HTTPS, "false"));
	}

	boolean isEncodeableEntity(Representation entity) {
		return entity.getMediaType().equals(MediaType.TEXT_XML) && entity.getSize() > 2048;
	}

	public SISPersistentManager getManager() throws PersistentException {
		return SISPersistentManager.instance();
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
