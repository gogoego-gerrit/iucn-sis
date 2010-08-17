import java.util.LinkedHashMap;
import java.util.Map;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.iucn.sis.server.ServerActivator;

import com.solertium.gogoego.server.extensions.testing.generic.GenericBootstrap;
import com.solertium.gogoego.server.extensions.testing.generic.MultiAppGenericBootstrap;

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
		//TODO add additional plugins
		
		
		return map;
	}

}
