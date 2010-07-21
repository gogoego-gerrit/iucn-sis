/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.gogoego.server.lib.app.exporter.workers.appengine;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;

import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.util.TagFilter;
import com.solertium.util.TagFilter.Tag;

/**
 * SiteWalker.java
 * 
 * Walks the site
 * 
 * @author rob.heittman
 * 
 */
public class SiteWalker {

	public class HrefListener implements TagFilter.Listener {

		private final List<String> interestingTagNames = Arrays.asList(new String[] { "a", "A" });

		private final Reference baseReference;

		public HrefListener(final Reference baseReference) {
			this.baseReference = baseReference;
		}

		public List<String> interestingTagNames() {
			return interestingTagNames;
		}

		public void process(final Tag t) throws IOException {
			final String href = t.getAttribute("href");
			if (href != null) {
				if (href.contains("://"))
					walk(new Reference(href));
				else
					walk(new Reference(baseReference, href).getTargetRef());
			}
		}

		public void setTagFilter(final TagFilter tf) {
			// stub to complete interface
		}

	}

	public static final void main(final String[] args) {

		final SiteWalker siteWalker = new SiteWalker("http://www.protectplanetocean.org", 3600);
		siteWalker.walk(new Reference("http://www.protectplanetocean.org/index.html"));
		while (siteWalker.processRetries()) {
		}

	}

	private final Client client = new Client(Protocol.HTTP);
	private final String scope;
	private final HashMap<String, Integer> seen = new HashMap<String, Integer>();
	private final int delay;

	public SiteWalker(final String scope, final int delay) {
		this.scope = scope;
		this.delay = delay;
	}

	boolean processRetries() {
		boolean retried = false;
		List<String> toWalk = new ArrayList<String>();
		for (final Map.Entry<String, Integer> targetEntry : seen.entrySet()) {
			final Integer i = targetEntry.getValue();
			final String k = targetEntry.getKey();
			if (i > 0) {
				GoGoDebug.get("debug").println("Retries remaining: " + i + " for " + k);
				toWalk.add(k);
			}
		}
		for (String k : toWalk) {
			seen.put(k, Integer.valueOf(seen.get(k) - 1));
			walk(new Reference(k));
			retried = true;
		}
		return retried;
	}

	public void walk(final Reference targetRef) {
		String target = targetRef.toString();
		if (target.contains("#")) {
			target = target.substring(0, target.lastIndexOf("#"));
		}

		if (!target.startsWith(scope)) {
			// debug.println("??? "+target+" is not in scope "+scope);
			return;
		}

		if (seen.containsKey(target)) {
			// debug.println("??? "+target+" already successfully
			// retrieved");
			return;
		}

		try {
			Thread.sleep(delay);
		} catch (final InterruptedException ix) {
			GoGoDebug.get("debug").println("Interrupted while waiting between fetches");
		}

		GoGoDebug.get("debug").println("Fetching " + target);

		final Response response = client.get(targetRef);
		if (response.getStatus().isSuccess()) {
			if (response.getEntity().getMediaType().equals(MediaType.TEXT_HTML)) {
				seen.put(target, Integer.valueOf(0));
				try {
					final String entityString = response.getEntity().getText();
					response.getEntity().release();
					final Reader reader = new StringReader(entityString);
					final TagFilter tf = new TagFilter(reader);
					tf.registerListener(new HrefListener(targetRef));
					tf.parse();
				} catch (final IOException iox) {
					GoGoDebug.get("debug").println("!!! Error reading " + target);
					GoGoDebug.get("debug").println("    " + iox.getClass().getName() + " " + iox.getMessage());
				}
			} else {
				seen.put(target, Integer.valueOf(0));
				GoGoDebug.get("debug").println("??? Not HTML: " + target);
				GoGoDebug.get("debug").println("    Media Type: " + response.getEntity().getMediaType().toString());
				response.getEntity().release();
			}
		} else {
			response.getEntity().release();
			GoGoDebug.get("debug").println("??? Unsuccessful response for: " + target);
			GoGoDebug.get("debug").println("    Status: " + response.getStatus().getCode() + " " + response.getStatus().getName());
		}
	}

}
