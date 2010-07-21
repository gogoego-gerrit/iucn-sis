package org.iucn.sis.server;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSListener;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.events.VFSEvent;
import com.solertium.vfs.utils.VFSUtils;

public class VFSSearchCrawler implements Runnable, VFSListener {

	private VFSSearchIndex index;
	private boolean init = false;
	private String path = "/";
	private VFS vfs = null;
	private String vfsroot = null;

	private AtomicBoolean crawling = new AtomicBoolean(false);
	private AtomicInteger files = new AtomicInteger(0);

	public VFSSearchCrawler(final String vfsroot) {
		this.vfsroot = vfsroot;
		initVFS();
	}

	public VFSSearchCrawler(final String vfsroot, final SearchIndexType indexer) {
		this(vfsroot);
		index = new VFSSearchIndex(indexer, vfs);
	}

	public void addToUpdateList(final String path) {
		if (!index.isCurrent(path)) {
			index.add(path);
		}
	}

	public void bindToVFS() {
		final File spec = new File(vfsroot);
		try {
			vfs = VFSFactory.getVFS(spec);
			vfs.addListener(this);
		} catch (final NotFoundException nf) {
			throw new RuntimeException("VFS " + spec.getPath() + " could not be opened.");

		}
	}

	private void crawl(final VFSPathToken[] fileList, final String path) {
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].toString().endsWith(".xml")) {
				if (!index.isCurrent(path + fileList[i])) {
					files.addAndGet(1);
					index.add(path + fileList[i]);
				}
			} else {
				try {
					final VFSPathToken[] recurFileList = vfs.list(VFSUtils.parseVFSPath(path + fileList[i] + "/"));
					crawl(recurFileList, path + fileList[i] + "/");
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public SearchIndexType getIndex() {
		return index.getIndex();
	}

	public String getStatus() {
		return ("crawling: " + crawling.get() + "\n" + "crawled files: " + files.get());
	}

	private void initVFS() {
		bindToVFS();
	}

	public void notifyEvent(final VFSEvent vfsEvent) {
		for (final VFSPath element : vfsEvent.getURIs()) {
			index.add(element.toString());
		}
	}

	public void run() {

		if (crawling.get() == true)
			return; // do not crawl if already crawling

		crawling.set(true);

		try {

			final VFSPathToken[] fileList = vfs.list(VFSUtils.parseVFSPath(path));
			if (!init) {
				crawl(fileList, path);
				init = true;
			}
		} catch (final Exception e) {
			System.out.println("Offending path: " + path);
			e.printStackTrace();
		}

		crawling.set(false);

	}

	public void setIndexer(final SearchIndexType indexer) {
		index = new VFSSearchIndex(indexer, vfs);
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public void unbindFromVFS() {
		vfs.removeListener(this);
	}

}
