/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.lib.caching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gogoego.api.debugging.GoGoDebugger;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

import com.solertium.gogoego.server.GoGoDebuggingImpl;
import com.solertium.gogoego.server.ServerApplication;

/**
 * MemoryCacheLandlord.java
 * 
 * Holds information about cached data, what can be cached, and 
 * eviction policies.
 * 
 * @author carl.scott
 *
 */
public class MemoryCacheLandlord {
	
	public static void main(String[] args) {
		final GoGoDebuggingImpl log = new GoGoDebuggingImpl();
		log.setForcePrint(true);
		
		final MemoryCacheLandlord landlord = new MemoryCacheLandlord(log);
		landlord.add("/apps/commerce/cart", new EvictionPathTranslator() {
			public Collection<String> getEvictionPaths() {
				return Arrays.asList(new String[] {"/admin/apps/commerce/cart", "/admin/apps/commerce/checkout"});
			}
			public String translate(String arg0) {
				System.out.println("Cart translation");
				return "/apps/commerce/cart";
			}
		});
		log.println("Cache a file (should succeed)");
		landlord.cache("/file/index.html");		
		
		log.println(landlord);
		
		log.println("Cache checkout (should fail)");
		landlord.cache("/apps/commerce/checkout");
		
		log.println(landlord);
		
		log.println("Cache the cart (should succeed)");
		landlord.cache("/apps/commerce/cart");
		
		log.println(landlord);
		
		landlord.invalidate(null, "/admin/files/file/index.html");
		
		log.println(landlord);
		
		landlord.invalidate(null, "/admin/apps/commerce/checkout");
		
		log.println(landlord);
	}
	
	private final Router cacheRouter;
	private final MemoryCacheAccess access;
	private final Router invalidationRouter;
	
	private final GoGoDebugger log;
	
	public MemoryCacheLandlord() {
		this(GoGoEgo.debug("fine"));
	}
	
	private MemoryCacheLandlord(GoGoDebugger log) {
		this.log = log;
		
		access = new MemoryCacheAccess();
		access.denyPath("/apps");
		
		cacheRouter = new Router();
		invalidationRouter = new Router();
		
		add("", new EvictionPathTranslator() {
			public Collection<String> getEvictionPaths() {
				return Arrays.asList(new String[] {"/admin/files"});
			}
			public String translate(String arg0) {
				return arg0.replaceFirst("/admin/files", "");
			}
		});
		add("/collections", new EvictionPathTranslator()  {
			public Collection<String> getEvictionPaths() {
				return Arrays.asList(new String[] {"/admin/collections"});
			}
			public String translate(String arg0) {
				return arg0.replaceFirst("/admin", "");
			}
		});
	}
	
	/**
	 * The URI at the resourcePath is modified when a write 
	 * operation is performed at the eviction path.  When this 
	 * occurs, the eviction path with translate the URI hit to 
	 * its readable version (which is the same URI space of the 
	 * cached entity) and  
	 * 
	 * @param resourcePath
	 * @param evictionPath
	 */
	public void add(String resourcePath, EvictionPathTranslator evictionPath) {
		access.allowPath(resourcePath);
		
		final Apartment apartment = new Apartment(evictionPath);
		
		cacheRouter.attach(resourcePath, apartment);
		
		for (String path : evictionPath.getEvictionPaths()) {
			invalidationRouter.attach(path, apartment);
		}
	}
	
	public boolean canCache(String path) {
		return access.canCache(path);
	}
	
	public void cache(String originalPath) {
		cache(originalPath, Arrays.asList(originalPath));
	}
	
	public void cache(String originalPath, Collection<String> dependents) {
		final ArrayList<String> updatedList = new ArrayList<String>();
		for (String path : dependents)
			updatedList.add(path.replaceFirst(ServerApplication.PUBLIC_FILE_LOCATION, ""));
		
		for (String uri : updatedList) {
			if (!canCache(uri))
				continue;
			
			final Request fauxRequest = new Request(Method.GET, uri);
			final Route route = (Route)cacheRouter.getNext(fauxRequest, new Response(fauxRequest));
			
			if (route == null)
				continue;
			
			final Restlet restlet = route.getNext();			
			if (restlet != null && restlet instanceof Apartment) {
				Apartment apartment = (Apartment)restlet;
				//apartment.rentRoom(uri, updatedList);
				apartment.rentRoom(uri, originalPath);
			}
		}
	}
	
	public void invalidateAll(final Context context) {
		for (Route route : invalidationRouter.getRoutes()) {
			if (route.getNext() instanceof Apartment) {
				Apartment apartment = (Apartment)route.getNext();
				for (String r : apartment.getRoutes().keySet())
					MemoryCache.getInstance().invalidate(context, r);
				apartment.getRoutes().clear();				
			}
		}
	}
	
	/**
	 * Invalidates resource edited by the given path
	 * @param context
	 * @param path
	 * @return the public resource invalidated, or null if nothing was found.
	 */
	public String invalidate(final Context context, final String path) {
		final Request fauxRequest = new Request(Method.GET, path);
		final Route route = (Route)invalidationRouter.getNext(fauxRequest, new Response(fauxRequest));
		log.println("Attempting to invalidate {0}, found {1}", path, route == null ? "null" : route.toString());
		if (route == null)
			return null;
		
		final Restlet router = route.getNext();
		if (router != null && router instanceof Apartment) {
			log.println("+ Invalidation hit for {0}", path);
			final Apartment next = (Apartment)router;
			String evictionRoute = next.getEvictionRoute().translate(path);
			//final Request fauxEvictionRequest = new Request(Method.GET, next.getEvictionRoute().translate(path));
			
			log.println("translated {0} to {1}", path, evictionRoute);
			//log.println("Apartments has tenants at {0}", next.getRoutes().keySet());
			EvictionPolicyRestlet tenants = next.remove(evictionRoute);
			if (tenants != null) {
				log.println("Eviction Policy for {0} applies to {1}", evictionRoute, tenants.getPaths());
				for (String uri : tenants.getPaths()) {
					log.println("Invalidating {0}", uri);
					MemoryCache.getInstance().invalidate(context, uri);
				}
			}
			
			return evictionRoute;
		}
		else
			log.println("- Invalidation miss");
		
		return null;
	}
	
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		/*builder.append("Default Route\r\n");
		final EvictionPolicyRouter defaultRoute = (EvictionPolicyRouter)resourceRouter.getDefaultRoute().getNext();
		for (Route route : defaultRoute.getRoutes()) {
			EvictionPolicyRestlet next = (EvictionPolicyRestlet)route.getNext();
			builder.append("- " + route.getTemplate().getPattern() + " -> ");
			for (Iterator<String> iter = next.getPaths().listIterator(); iter.hasNext(); )
				builder.append(iter.next() + (iter.hasNext() ? "," : "\r\n"));
		}*/
		for (Route route : cacheRouter.getRoutes()) {
			Apartment next = (Apartment)route.getNext();
			builder.append("Route: " + route.getTemplate().getPattern());
			if (next.getRoutes().isEmpty())
				builder.append(" (0)\r\n");
			else
				for (EvictionPolicyRestlet target : next.getRoutes().values()) {
					builder.append(" ("  + target.getPaths().size() + ")\r\n");
					for (String path : target.getPaths())
						builder.append("- " + path + "\r\n");
				}
		}
		builder.append("Invalidation routes...\r\n");
		for (Route route : invalidationRouter.getRoutes()) {
			builder.append("- " + route.getTemplate().getPattern() + "\r\n");
		}
		return builder.toString();
	}

	/**
	 * This router holds all the cached assets at their readable URIs.
	 * Think "apartment" here...
	 *  
	 * @author user
	 *
	 */
	private static class Apartment extends Restlet {
		
		private final EvictionPathTranslator evictionRoute;
		private final Map<String, EvictionPolicyRestlet> routes;
		
		public Apartment(final EvictionPathTranslator evictionRoute) {
			super();
			this.evictionRoute = evictionRoute;
			
			routes = new HashMap<String, EvictionPolicyRestlet>();
		}
		
		public void rentRoom(String address, String tenant) {
			rentRoom(address, Arrays.asList(tenant));
		}
		
		public void rentRoom(String address, Collection<String> tenants) {
			if (routes.containsKey(address))
				routes.get(address).update(tenants);
			else
				routes.put(address, new EvictionPolicyRestlet(tenants));
		}
		
		public EvictionPathTranslator getEvictionRoute() {
			return evictionRoute;
		}
		
		public Map<String, EvictionPolicyRestlet> getRoutes() {
			return routes;
		}
		
		public EvictionPolicyRestlet getNext(Request request, Response response) {
			return routes.get(request.getResourceRef().getPath());
		}
		
		public EvictionPolicyRestlet remove(String path) {
			return routes.remove(path);
		}
		
	}
	
	private static class EvictionPolicyRestlet extends Restlet {
		
		private final Set<String> paths;
		
		public EvictionPolicyRestlet(Collection<String> paths) {
			this.paths = new HashSet<String>();
			this.paths.addAll(paths);
		}
		
		public Collection<String> getPaths() {
			return paths;
		}
		
		public void clear() {
			paths.clear();
		}
		
		public void update(Collection<String> updates) {
			paths.addAll(updates);
		}
	}
	
	public static interface EvictionPathTranslator {
		
		public Collection<String> getEvictionPaths();
		
		public String translate(String reference);
		
	}
}
