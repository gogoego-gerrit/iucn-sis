import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.ServerActivator;

import com.solertium.gogoego.server.extensions.testing.generic.GenericBootstrap;
import com.solertium.gogoego.server.extensions.testing.generic.MultiAppGenericBootstrap;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

/**
 * Use this to run the entire SIS universe.  If you 
 * just want to run a particular application, make 
 * a new bootstrap that extends {@link GenericBootstrap} 
 * 
 * 
 * @author carl.scott@solertium.com
 *
 */
public class SISTestBootstrap extends MultiAppGenericBootstrap {
	
	public static void main(String[] args) {
		final SISTestBootstrap b;
		try {
			b = new SISTestBootstrap();
		} catch (Exception e) {
			System.out.println("Could not create test bootstrap");
			throw new RuntimeException(e);
		}
		
		try {
			b.start();
		} catch (Exception e) {
			System.out.println("Could not start test bootstrap");
			throw new RuntimeException(e);
		}
	}
	
	protected Map<String, GoGoEgoApplication> getGoGoEgoApplications() {
		final Map<String, GoGoEgoApplication> map = 
			new LinkedHashMap<String, GoGoEgoApplication>();
		
		map.put("org.iucn.sis.server", new ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.recentasms", 
			new org.iucn.sis.server.extensions.recentasms.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.references", 
			new org.iucn.sis.server.extensions.references.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.tags", 
			new org.iucn.sis.server.extensions.tags.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.definitions", 
			new org.iucn.sis.server.extensions.definitions.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.images", 
			new org.iucn.sis.server.extensions.images.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.integrity", 
			new org.iucn.sis.server.extensions.integrity.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.workflow", 
			new org.iucn.sis.server.extensions.workflow.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.user", 
			new org.iucn.sis.server.extensions.user.application.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.zendesk", 
			new org.iucn.sis.server.extensions.zendesk.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.notes", 
			new org.iucn.sis.server.extensions.notes.ServerActivator().getApplicationFactory().newInstance());
		map.put("org.iucn.sis.server.extensions.scripts", 
			new org.iucn.sis.server.extensions.scripts.ServerActivator().getApplicationFactory().newInstance());
		
		//TODO add additional plugins
		
		return map;
	}
	
	@Override
	protected VFS getVFS() {
		File file = new File(GoGoEgo.getInitProperties().getProperty("sis_vfs", "/var/sis/newest_vfs"));
		if (file.exists())
			try {
				return VFSFactory.getVersionedVFS(file);
			} catch (NotFoundException e) {
				return null;
			}
		else
			return null;
	}

}
