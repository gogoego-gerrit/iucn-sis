/**
 *
 */
package org.iucn.sis.server.users.container;

import java.io.File;
import java.util.Properties;

import javax.naming.NamingException;

import org.iucn.sis.server.users.resources.CustomFieldManager;
import org.iucn.sis.server.users.resources.DumpResource;
import org.iucn.sis.server.users.resources.ProfileSearchResource;
import org.iucn.sis.server.users.resources.UserResource;
import org.iucn.sis.server.users.utils.ImportFromVFS;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Router;

import com.solertium.db.DBException;
import com.solertium.db.DBSession;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

/**
 * UserManagementApplication.java
 * 
 * User Management application. Controls the database of SIS users. Created for
 * SIS-216
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public final class UserManagementApplication extends Application {

	private ExecutionContext ec;
	private VFS vfs;

	private static final String INIT = "INIT_KEY";

	public static UserManagementApplication getFromContext(Context context) {
		UserManagementApplication app = (UserManagementApplication) context.getAttributes().get(INIT);
		if (app == null)
			app = (UserManagementApplication) Application.getCurrent();
		return app;
	}

	/**
	 * This constructor won't be called in normal use but GWT whines without it
	 * 
	 */
	public UserManagementApplication() {
	}

	public UserManagementApplication(Context context, String vfsroot) {
		super(context);
		VFS ivfs;
		try {
			ivfs = VFSFactory.getVFS(new File(vfsroot));
		} catch (NotFoundException nf) {
			throw new RuntimeException("The selected VFS " + vfsroot + " does not exist");
		}
		vfs = ivfs;
		initDatabase();
	}

	@Override
	public Restlet createRoot() {
		getContext().getAttributes().put(INIT, this);

		if (ec == null)
			throw new RuntimeException("Could not find user database, application can not start.");

		final Router router = new Router(getContext());

		// GWT-standalone only
		// router.attach("/Users", new
		// ClapCacheDirectory(getContext().createChildContext(),
		// "clap://thread/org/iucn/public"));

		router.attach("/list", UserResource.class);
		router.attach("/dump", DumpResource.class);

		router.attach("/manager/custom", CustomFieldManager.class);
		router.attach("/manager/custom/{id}", CustomFieldManager.class);

		router.attach("/browse/profile", ProfileSearchResource.class);

		router.attach("/import", new Restlet(getContext()) {
			@Override
			public void handle(Request request, Response response) {
				ImportFromVFS script = new ImportFromVFS();
				script.importUsers(vfs, ec);
			}
		});

		getContext().getAttributes().remove(INIT);

		return router;
	}

	public ExecutionContext getExecutionContext() {
		return ec;
	}

	private void initDatabase() {
		final Properties defaultProps = new Properties();
		defaultProps.setProperty("dbsession.users.uri", "jdbc:h2:file:h2_db/sisusers");
		defaultProps.setProperty("dbsession.users.driver", "org.h2.Driver");
		defaultProps.setProperty("dbsession.users.user", "sa");
		defaultProps.setProperty("dbsession.users.password", "");

		DBSession session;
		try {
			session = DBSessionFactory.getDBSession("users");
		} catch (NamingException e) {
			try {
				DBSessionFactory.registerDataSource("users", defaultProps);
				session = DBSessionFactory.getDBSession("users");
			} catch (NamingException f) {
				throw new RuntimeException("Could not create user database", f);
			}
		}

		session.setIdentifierCase(DBSession.CASE_UNCHECKED);

		final ExecutionContext ec = new SystemExecutionContext(session);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		try {
			ec.createStructure(BaseDocumentUtils.impl.getInputStreamFile(getClass().getResourceAsStream(
					"userstruct.xml")));
		} catch (DBException e) {
			throw new RuntimeException("Could not set up structure!", e);
		} catch (NullPointerException e) {
			throw new RuntimeException("Could not set up structure!", e);
		}

		this.ec = ec;
	}

}
