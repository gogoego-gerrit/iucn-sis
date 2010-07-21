package org.iucn.sis.server.simple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.iucn.sis.server.crossport.export.ExportApplication;
import org.iucn.sis.server.integrity.IntegrityApplication;
import org.iucn.sis.server.ref.ReferenceApplication;
import org.iucn.sis.server.users.container.UserManagementApplication;
import org.iucn.sis.server.workflow.WorkflowApplication;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.routing.VirtualHost;

import com.solertium.db.DBException;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.java.JavaNativeDocumentFactory;
import com.solertium.util.CurrentBinary;
import com.solertium.util.SysDebugger;
import com.solertium.util.restlet.DesktopIntegration;
import com.solertium.util.restlet.StandardServerComponent;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

public class SISBootstrap extends StandardServerComponent {

	private static String sisInstance = "offline";

	public static String getSISInstance() {
		return sisInstance;
	}

	public static void main(final String args[]) {
		try {
			final SISBootstrap component = new SISBootstrap();
			
			if( !SISContainerApp.isHostedMode() )
				DesktopIntegration.launch("SIS Toolkit", "/SIS/index.html", component);
			else
				try{
					component.start();
				} catch (Exception exception) {
					System.err.println("Component failed to start.");
					exception.printStackTrace();
					System.exit(0);
				}
		} catch (DBException dbx) {
			dbx.printStackTrace();
		}
	}

	public SISBootstrap() throws DBException {
		this(10000, 10001);
	}

	public SISBootstrap(int defaultHttpPort, int defaultSslPort) throws DBException {
		super(defaultHttpPort, defaultSslPort);
		NativeDocumentFactory.setDefaultInstance(new JavaNativeDocumentFactory(getContext().getClientDispatcher()));
	}

	@Override
	protected void setupDefaultVirtualHost() {
		final Context context = getContext().createChildContext();
		
		String iinstance = StandardServerComponent.getInitProperties().getProperty("INSTANCE");
		if (iinstance != null)
			sisInstance = iinstance;
		String ivfsroot = StandardServerComponent.getInitProperties().getProperty("VFSROOT");
		if (ivfsroot == null) {
			try {
				File propfile = new File(CurrentBinary.getDirectory(this), "data/layout.properties");
				if (propfile.exists()) {
					SysDebugger.getInstance().println("Using data/layout.properties for VFS structure");
					ivfsroot = propfile.getCanonicalPath();
				} else {
					SysDebugger.getInstance().println("Using data/My_Assessments folder as VFS root.");
					ivfsroot = new File(CurrentBinary.getDirectory(this), "data/My_Assessments").getCanonicalPath();
				}
			} catch (IOException unreadable) {
				ivfsroot = ".";
				SysDebugger.getInstance().println("Using working directory as VFS root.");
			}
		}
		System.out.println("VFS Root is " + ivfsroot);

		List<Reference> mirrors = new ArrayList<Reference>();

		for( Entry<Object, Object> cur : StandardServerComponent.getInitProperties().entrySet() ) {
			if( cur.getKey().toString().startsWith("MIRROR") ) {
				try {
					mirrors.add(new Reference(cur.getValue().toString()));
					System.out.println("Adding mirror at " + cur.getValue());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					System.out.println("Error parsing References to mirroring servers. Not using ref " + cur.getValue());
				}
			}
		}


		VFS ivfs = null;
		if( ivfs == null ) {
			try {
				ivfs = VFSFactory.getVFS(new File(ivfsroot));
			} catch (IOException nf) {
				throw new RuntimeException("The selected VFS " + ivfsroot + " does not exist");
			} catch (Exception nf) {
				nf.printStackTrace();
				throw new RuntimeException("The provided HDFS URI is invalid.");
			}  
		}

		final VirtualHost vhost = new VirtualHost(getContext());
		try {
			vhost.attach(new SISContainerApp(context, ivfsroot));
			vhost.attach("/refsvr", new ReferenceApplication(context));
			vhost.attach("/export", new ExportApplication(context));
			vhost.attach("/manageusers", new UserManagementApplication(context, ivfsroot));
			/*
			 * Comment in below to include the integrity application.  Note that if the 
			 * "assess" dbsession is not found it will throw a RuntimeException.
			 */
			if( SISContainerApp.amIOnline ) {
				vhost.attach("/integrity", new IntegrityApplication(context, ivfsroot));
				vhost.attach("/workflow", new WorkflowApplication(context, ivfsroot));
			}
		} catch (DBException dbx) {
			throw new RuntimeException("Failure to initialize databases at startup", dbx);
		}
		setHosts(Arrays.asList(new VirtualHost[] { vhost }));
		try {
			updateHosts();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		setDefaultHost(vhost);
	}

}
