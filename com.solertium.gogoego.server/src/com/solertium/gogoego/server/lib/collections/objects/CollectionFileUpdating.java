/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.gogoego.server.lib.collections.objects;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gogoego.api.collections.CollectionCache;
import org.gogoego.api.collections.Constants;
import org.gogoego.api.collections.GenericItem;
import org.gogoego.api.collections.TextData;
import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.BaseTagListener;
import com.solertium.util.SysDebugger;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.TagFilter.Tag;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.events.VFSEvent;

/**
 * CollectionFileUpdating.java
 * 
 * When files get updated, run this to update collections.
 * 
 * @author carl.scott
 *
 */
public class CollectionFileUpdating implements Runnable {
	
	private final VFS vfs;
	private final VFSEvent event;
	private final Context context;
		
	public CollectionFileUpdating(VFS vfs, VFSEvent event, Context context) {
		this.vfs = vfs;
		this.event = event;
		this.context = context;
	}
	
	public void run() {
		log("Updating collections on event {0} for {1}", event.getClass().getSimpleName(), Arrays.asList(event.getURIs()));
		try {
			parse(new VFSPath("/(SYSTEM)/collections/" + Constants.COLLECTION_ROOT_FILENAME));
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
	}
	
	private void parse(VFSPath path) throws IOException {
		final TagFilter tf = new TagFilter(vfs.getReader(path));
		tf.shortCircuitClosingTags = true;
		tf.registerListener(new BaseTagListener() {
			public List<String> interestingTagNames() {
				final List<String> l = new ArrayList<String>();
				l.add("subcollection");
				l.add("item");
				return l;
			}
			public void process(Tag t) throws IOException {
				if ("subcollection".equals(t.name)) {
					try {
						parse(new VFSPath("/(SYSTEM)" + t.getAttribute("uri") + "/" + Constants.COLLECTION_ROOT_FILENAME));
					} catch (IOException e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}
				else if ("item".equals(t.name)) {
					try {
						parseItem(new VFSPath("/(SYSTEM)" + t.getAttribute("uri") + ".xml"));
					} catch (IOException e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}					
			}
		});
		tf.parse();
	}
	
	private void parseItem(VFSPath path) throws IOException {
		final FileFinder finder = new FileFinder(event.getURIs()[0]);
		final TagFilter tf = new TagFilter(vfs.getReader(path));
		tf.shortCircuitClosingTags = false;
		tf.registerListener(finder);
		tf.parse();
		
		if (finder.containsTarget())
			update(path, finder.getFields());
	}
	
	private void update(VFSPath path, List<String> fields) {
		final GenericItem item;
		try {
			item = new GenericItem(vfs.getDocument(path));
		} catch (IOException e) {
			return;
		}
		
		final String text = event.getURIs().length == 2 ? event.getURIs()[1].toString() : "";			
		
		for (String field : fields)
			((TextData)item.getAllData().get(field)).setText(text);
		
		if (context != null) {
			DocumentUtils.writeVFSFile(path.toString(), vfs, BaseDocumentUtils.impl.createDocumentFromString(item.toXML()));
			try {
				CollectionCache.getInstance().invalidate(context, path.toString().substring(9, path.toString().length()-4));
			} catch (IndexOutOfBoundsException impossible) {
				TrivialExceptionHandler.impossible(this, impossible);
			}
		}
		
		log("Wrote {0}", path);
	}
	
	private void log(String template, Object... variables) {
		if (context == null)
			SysDebugger.out.println(template, variables);
		else
			GoGoEgo.debug("fine").println(template, variables);
	}
	
	public static class FileFinder extends BaseTagListener {
		private AtomicBoolean b;
		private String current;
		private StringWriter writer;
		private List<String> fields;
		private final VFSPath target;
		
		public FileFinder(VFSPath target) {
			b = new AtomicBoolean(false);
			fields = new ArrayList<String>();
			this.target = target;
		}
		
		public List<String> interestingTagNames() {
			final List<String> l = new ArrayList<String>();
			l.add("custom");
			l.add("text");
			l.add("/text");
			return l;
		}
		public void process(Tag t) throws IOException {
			if ("custom".equals(t.name) && ("file".equals(t.getAttribute("type")) || "image".equals(t.getAttribute("type")))) {
				current = t.getAttribute("name");
				b.set(true);
			}
			else if (b.get()) {
				if ("/text".equals(t.name)) {
					parent.stopWritingBeforeTag();
					parent.stopDiverting();
					b.set(false);
					
					String content = writer.toString();
					if (content.startsWith("<text>"))
						content = content.substring(6);
					if (content.startsWith("<![CDATA["))
						content = content.substring(9, content.length()-3);
					//log("Found {0} for {1}\r\n--", content, current);
					try {
						if (target.equals(new VFSPath(content)))
							fields.add(current);
					} catch (IllegalArgumentException e) {
						TrivialExceptionHandler.ignore(this, e);
					}
				}
				else if ("text".equals(t.name)) {
					writer = new StringWriter();
					parent.startWritingAfterTag();
					parent.divert(writer);
				}
			}
		}
		
		public boolean containsTarget() {
			return !fields.isEmpty();
		}
		
		public List<String> getFields() {
			return fields;
		}
		
	}

}
