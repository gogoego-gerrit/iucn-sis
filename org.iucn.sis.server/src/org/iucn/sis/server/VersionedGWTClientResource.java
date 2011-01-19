package org.iucn.sis.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.gogoego.api.classloader.SimpleClasspathResource;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.gogoego.api.utils.PluginBroker;
import org.restlet.Context;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

@SuppressWarnings("deprecation")
public abstract class VersionedGWTClientResource extends SimpleClasspathResource {
	
	public VersionedGWTClientResource(Context context, Request request, Response response) {
		super(context, request, response);
	}
	
	@Override
	public Representation represent(Variant variant) throws ResourceException {
		getRequest().getAttributes().put(MagicDisablingFilter.MAGIC_DISABLING_KEY, true);
		
		final GoGoEgoBaseRepresentation representation = 
			(GoGoEgoBaseRepresentation)super.represent(variant);
		
		String encodedUri = getRequest().getResourceRef().getRemainingPart();
		if ("".equals(encodedUri) || "/".equals(encodedUri))
			encodedUri = "/index.html";
		
		int qindex = encodedUri.indexOf('?');
		if (qindex != -1)
			encodedUri = encodedUri.substring(0, qindex);
		try {
			encodedUri = URLDecoder.decode(encodedUri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		if (!encodedUri.endsWith(".html"))
			return gzip(representation);
		
		representation.setModificationDate(Calendar.getInstance().getTime());
		
		if ("/index.html".equals(encodedUri))
			return gzip(GoGoEgo.get().applyTemplating(representation, new ClientTemplateRepresentation(getPluginName())));
		else
			return gzip(representation);
	}
	
	public abstract String getVersion();
	
	private Representation gzip(Representation rep) {
		if (MediaType.TEXT_ALL.includes(rep.getMediaType()))
			return new EncodeRepresentation(Encoding.GZIP, rep);
		else
			return rep;
	}
	
	public String getPluginName() {
		return "org.iucn.sis.client.compiled";
	}

	@Override
	public String getBaseUri() {
		return "org/iucn/sis/client/compiled/public/SIS";
	}

	@Override
	public ClassLoader getClassLoader() {
		String latestVersion = null;
		{
			Map<String, Map<String, String>> version = 
				GoGoEgo.get().getClassLoaderPluginMetadata(getPluginName());
			if (!version.isEmpty()) {
				List<String> list = new ArrayList<String>(version.keySet());
				Collections.sort(list, Collections.reverseOrder(new PluginBroker.VersionNumberComparator()));
				
				latestVersion = list.get(0);
			}
		}
		String version = getVersion();
		if (version == null)
			version = latestVersion;
		
		if (version == null)
			return GoGoEgo.get().getClassLoaderPlugin(getPluginName());
		else
			return GoGoEgo.get().getClassLoaderPlugin(getPluginName(), version);
	}

	public static class ClientTemplateRepresentation extends GoGoEgoStringRepresentation {
		
		private final String plugin;
		
		public ClientTemplateRepresentation(String bundleName) {
			super("template");
			this.plugin = bundleName;
			setModificationDate(Calendar.getInstance().getTime());
		}
		
		@Override
		public String resolveEL(String key) {
			if ("version".equals(key)) {
				Map<String, Map<String, String>> version = GoGoEgo.get().getClassLoaderPluginMetadata(plugin);
				if (version.isEmpty())
					return "2.0.0";
				else {
					List<String> list = new ArrayList<String>(version.keySet());
					Collections.sort(list, Collections.reverseOrder(new PluginBroker.VersionNumberComparator()));
					return version.get(list.get(0)).get("Bundle-Version");
				}
			}
			else
				return super.resolveEL(key);
		}
		
	}
	
}
