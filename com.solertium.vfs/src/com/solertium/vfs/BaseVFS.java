/*
 * Copyright (C) 2004-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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

package com.solertium.vfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.dom.readonly.RONode;
import com.solertium.vfs.events.VFSEvent;

/**
 * Supplies general assistance for implementers of the VFS interface, e.g.
 * listeners, wrappers that implement deprecated methods.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public abstract class BaseVFS implements VFS {

	protected static class ListenerEntry {
		public WeakReference<VFSListener> listener;
		public VFSPath uri;

		public ListenerEntry(final VFSPath uri, final VFSListener listener) {
			this.uri = uri;
			this.listener = new WeakReference<VFSListener>(listener);
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof ListenerEntry){
				ListenerEntry other = (ListenerEntry) o;
				return (other.uri.equals(uri) && other.listener.equals(listener));
			}
			return false;
		}
		
		public int hashCode(){
			return uri.hashCode() ^ listener.hashCode();
		}

	}
	
	protected boolean usingAccessListeners = false;
	
	protected final CacheManager cacheManager;
	
	protected BaseVFS(){
		cacheManager = new CacheManager();
		Cache domCache = new Cache("dom",200,MemoryStoreEvictionPolicy.LFU,false,"",false,86400,86400,false,120,null);
		cacheManager.addCache(domCache);
	}
	
	protected Cache getDomCache(){
		return cacheManager.getCache("dom");
	}

	private static final int BUFFER_SIZE = 65536;

	public static void copyStream(final InputStream is, final OutputStream os)
			throws IOException {
		final byte[] buf = new byte[BUFFER_SIZE];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
	}

	protected final ConcurrentLinkedQueue<ListenerEntry> listeners = new ConcurrentLinkedQueue<ListenerEntry>();

	protected final ConcurrentLinkedQueue<ListenerEntry> accessListeners = new ConcurrentLinkedQueue<ListenerEntry>();

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void addListener(final String uri, final VFSListener listener) {
		addListener(new VFSPath(uri), listener);
	}

	public void addListener(final VFSListener listener) {
		addListener(VFSPath.ROOT, listener);
	}

	public void addListener(final VFSPath uri, final VFSListener listener) {
		if(listener instanceof VFSAccessListener){
			accessListeners.add(new ListenerEntry(uri, listener));
			usingAccessListeners = true;
		} else {
			ListenerEntry le = new ListenerEntry(uri, listener);
			if(!listeners.contains(le)){
				listeners.add(le);
			}
		}
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void copy(final String from, final String to)
			throws NotFoundException, ConflictException {
		copy(new VFSPath(from), new VFSPath(to));
	}
	
	protected URL getJavaUrlForPath(VFSPath uri){
		if(!exists(uri)) return null;
		try{
			System.err.println("Warning: using an expensive temp-file approach to access "+uri+"\n"+
					"The VFS provider "+this.getClass().getName()+" should implement an optimized version of getJavaUrlForPath.");
			File f = File.createTempFile("tempjar", "jar");
			InputStream is = getInputStream(uri);
			OutputStream os = new FileOutputStream(f);
			copyStream(is,os);
			is.close();
			os.close();
			f.deleteOnExit();
			return f.toURL();
		} catch (IOException iox) {
			iox.printStackTrace();
			return null;
		}
	}

	public ClassLoader createClassLoader(List<VFSPath> uris, ClassLoader parent) {
		ArrayList<URL> urls = new ArrayList<URL>();
		for(VFSPath uri:uris){
			if(exists(uri)){
				URL url = getJavaUrlForPath(uri);
				if(url!=null) urls.add(url);
			} else {
				System.err.println("Location "+uri+" does not exist, classloader ignoring it");
			}
		}
		return new URLClassLoader(urls.toArray(new URL[0]),parent);
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void delete(final String uri) throws NotFoundException,
			ConflictException {
		delete(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public boolean exists(final String uri) {
		return exists(new VFSPath(uri));
	}
	
	public void generateCollection(VFSPath uri) throws NotFoundException, ConflictException {
		throw new UnsupportedOperationException("Creating generated collections is not supported.");
	}
	
	public void generateCollections(VFSPath uri) throws NotFoundException, ConflictException {
		throw new UnsupportedOperationException("Creating generated collections is not supported.");
	}

	/**
	 * This method returns a read-only DOM Document, possibly from cache.
	 */
	public Document getDocument(final VFSPath uri) throws IOException {
		try {
			Cache cache = getDomCache();
			Element e = cache.get(uri);
			if(e!=null){
				// It appears that mtimes can only be trusted to resolve to
				// the second, and not to the millisecond
				final long ct = e.getCreationTime() / 1000;
				final long lm = getLastModified(uri) / 1000;
				if(ct>lm){
					Document cachedDocument = (Document) e.getObjectValue();
					return cachedDocument;
				}
			}
			final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			try {
				Document roDocument = (Document)
					RONode.representing(builder.parse(new InputSource(getReader(uri))));
				if(roDocument==null){
					return null;
				}
				cache.put(new Element(uri,roDocument));
				return roDocument;
			} catch (final SAXException sax) {
				throw new VFSException("XML Parse failed", sax);
			}
		} catch (final ParserConfigurationException unlikely) {
			throw new RuntimeException("Bad XML Parser configuration", unlikely);
		}
	}

	/**
	 * This method returns a freshly parsed, unique, read-write Document object.
	 * It is much slower under load than getDocument(...) and should only be used
	 * when the caller explicitly needs to modify the underlying Document.
	 */
	public Document getMutableDocument(final VFSPath uri) throws IOException {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			try {
				return builder.parse(new InputSource(getReader(uri)));
			} catch (final SAXException sax) {
				throw new VFSException("XML Parse failed", sax);
			}
		} catch (final ParserConfigurationException unlikely) {
			throw new RuntimeException("Bad XML Parser configuration", unlikely);
		}
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public String getETag(final String uri) throws NotFoundException {
		return getETag(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public InputStream getInputStream(final String uri)
			throws NotFoundException {
		return getInputStream(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLastModified(final String uri) throws NotFoundException {
		return getLastModified(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public long getLength(final String uri) throws NotFoundException {
		return getLength(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public VFSMetadata getMetadata(final String uri) {
		return getMetadata(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public String getName(final String uri) {
		return getName(new VFSPath(uri));
	}

	public String getName(final VFSPath uri) {
		return uri.getName();
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public OutputStream getOutputStream(final String uri)
			throws NotFoundException, ConflictException {
		return getOutputStream(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public Reader getReader(final String uri) throws NotFoundException {
		return getReader(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public String getString(final String uri) throws IOException,
			NotFoundException, BoundsException {
		return getString(new VFSPath(uri));
	}
	
	/**
	 * Returns a temporary file for the given uri.  It is 
	 * obviously not connected to the VFS at this point.
	 * @param uri
	 * @return
	 * @throws IOException
	 * @throws NotFoundException
	 */
	public File getTempFile(VFSPath uri) throws IOException, NotFoundException {
		if (isCollection(uri))
			throw new NotFoundException();
		if (!exists(uri))
			throw new NotFoundException();
		
		final int index = uri.getName().lastIndexOf('.');
		final String suffix = index == -1 || uri.getName().endsWith(".") ? 
			".tmp" : uri.getName().substring(index+1);
		
		final File tmp = File.createTempFile(
			uri.getName()+new Date().getTime(), suffix
		);
		
		final OutputStream os = new FileOutputStream(tmp);
		final InputStream is = getInputStream(uri);
		
		final byte[] buf = new byte[65536];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
		
		try {
			os.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		try {
			is.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		return tmp;
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public Writer getWriter(final String uri) throws NotFoundException,
			ConflictException {
		return getWriter(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public boolean isCollection(final String uri) throws NotFoundException {
		return isCollection(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public String[] list(final String uri) throws NotFoundException {
		final VFSPathToken[] list = list(new VFSPath(uri));
		final String[] stringList = new String[list.length];
		for (int i = 0; i < list.length; i++)
			stringList[i] = list[i].toString();
		return stringList;
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void makeCollection(final String uri) throws NotFoundException,
			ConflictException {
		makeCollection(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void makeCollections(final String uri) throws NotFoundException,
			ConflictException {
		makeCollections(new VFSPath(uri));
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void move(final String from, final String to)
			throws NotFoundException, ConflictException {
		move(new VFSPath(from), new VFSPath(to));
	}

	protected void notifyAccessListeners(final VFSEvent vfsEvent) {
		ArrayList<ListenerEntry> toRemove = new ArrayList<ListenerEntry>();
		for (final ListenerEntry entry : accessListeners)
			for (final VFSPath uri : vfsEvent.getURIs())
				if ((uri != null) && uri.isIn(entry.uri)) {
					VFSListener l = entry.listener.get();
					if(l==null){
						toRemove.add(entry);
					} else {
						l.notifyEvent(vfsEvent);
					}
					break;
				}
		for (final ListenerEntry le : toRemove)
			listeners.remove(le);
	}

	protected void notifyListeners(final VFSEvent vfsEvent) {
		new Thread(new Runnable() {
			public void run() {
				ArrayList<ListenerEntry> toRemove = new ArrayList<ListenerEntry>();
				ArrayList<ListenerEntry> toNotify = new ArrayList<ListenerEntry>();
				toNotify.addAll(listeners); // fix the listener list at this point in time
				for (final ListenerEntry entry : toNotify)
					for (final VFSPath uri : vfsEvent.getURIs())
						if ((uri != null) && uri.isIn(entry.uri)) {
							VFSListener l = entry.listener.get();
							if(l==null){
								toRemove.add(entry);
							} else {
								l.notifyEvent(vfsEvent);
							}
							break;
						}
				for (final ListenerEntry le : toRemove)
					listeners.remove(le);
			}
		}).start();
	}

	public void removeListener(final VFSListener listener) {
		Queue<ListenerEntry> target;
		if(listener instanceof VFSAccessListener){
			target = accessListeners;
		} else {
			target = listeners;
		}
		ArrayList<ListenerEntry> toRemove = new ArrayList<ListenerEntry>();
		for (final ListenerEntry le : target){
			VFSListener l = le.listener.get();
			if(l==null){
				toRemove.add(le);
			} else {
				if(l.equals(listener)) toRemove.add(le);
			}
		}
		for (final ListenerEntry le : toRemove){
			target.remove(le);
		}
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void setLastModified(final String uri, final Date modified)
			throws NotFoundException {
		setLastModified(new VFSPath(uri), modified);
	}

	/**
	 * @deprecated Pass VFSPath instead of String for uri
	 */
	@Deprecated
	public void setMetadata(final String uri, final VFSMetadata metadata)
			throws ConflictException {
		setMetadata(new VFSPath(uri), metadata);
	}

}
