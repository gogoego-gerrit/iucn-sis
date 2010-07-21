package org.iucn.sis.server.ref;

import java.io.IOException;

import javax.naming.NamingException;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.util.ClasspathResources;
import com.solertium.util.SysDebugger;

public class ReferenceApplication extends Application {

	public static final String DBNAME = "ref_lookup";
	public static final String DBDEFINITION = "org.iucn.sis.server.ref.structure";

	private static boolean isInit = false;

	public static void createDebugger() {
		// Set up default logger
		SysDebugger.getInstance().setLogLevel(SysDebugger.INFO);

		// Set overriding system debug level
		SysDebugger.getInstance().setSystemDebugLevel(SysDebugger.OFF);

		final SysDebugger prepend = new SysDebugger();
		prepend.setPrepend("## ");

		final SysDebugger force = new SysDebugger();
		force.setForcePrint(true);

		SysDebugger.getInstance().addNamedInstance("##", prepend);
		SysDebugger.getInstance().addNamedInstance("force", force);
		SysDebugger.getInstance().addNamedInstance("fine", new SysDebugger(SysDebugger.FINE));
		SysDebugger.getInstance().addNamedInstance("status", new SysDebugger(SysDebugger.CONFIG));
		SysDebugger.getInstance().addNamedInstance("error", new SysDebugger(SysDebugger.SEVERE));
	}

	public static void initializeDatabase() throws DBException {
		if (isInit)
			return;
		SysDebugger.getInstance().println("Injecting/creating reference database structure");
		try {
			ExecutionContext ec = new SystemExecutionContext(DBNAME);
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.createStructure(ClasspathResources.getDocument(ReferenceApplication.class, "refstruct.xml"));
		} catch (IOException iox) {
			throw new DBException("Database could not be initialized with reference structure", iox);
		} catch (NamingException nx) {
			throw new DBException("Database " + DBNAME + " was not defined", nx);
		}
		isInit = true;
	}

	public ReferenceApplication(final Context context) throws DBException {
		super(context);
		if (!SysDebugger.isInit())
			createDebugger();
		initializeDatabase();
	}

	@Override
	public Restlet createRoot() {
		new ReferenceLabels().saveTo(getContext());
		final Router root = new Router(getContext());
		root.attach("/types", TypesResource.class);
		root.attach("/type/{type}", TypeResource.class);
		root.attach("/reference/{refid}", ReferenceResource.class);
		root.attach("/submit", SubmissionResource.class);
		root.attach("/search/reference", ReferenceSearchResource.class);
		return root;
	}

}
