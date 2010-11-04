import java.io.File;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.shared.conversions.Activator;

import com.solertium.gogoego.server.extensions.testing.generic.GenericBootstrap;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;


public class SISConversionsBootstrap extends GenericBootstrap {
	
	public static void main(String[] args) {
		final SISConversionsBootstrap b;
		try {
			b = new SISConversionsBootstrap();
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
	
	@Override
	protected GoGoEgoApplication getGoGoEgoApplication() {
		return new Activator().getApplicationFactory().newInstance();
	}
	
	@Override
	protected String getRegistrationKey() {
		return "org.iucn.sis.shared.conversions";
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
