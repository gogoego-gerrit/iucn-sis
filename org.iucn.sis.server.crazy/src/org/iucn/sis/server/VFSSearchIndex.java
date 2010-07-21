package org.iucn.sis.server;

import net.jcip.annotations.ThreadSafe;

import org.iucn.sis.server.utils.DocumentUtils;
import org.w3c.dom.Document;

import com.solertium.vfs.VFS;

@ThreadSafe
public class VFSSearchIndex {

	private final SearchIndexType index;
	private final VFS vfs;

	public VFSSearchIndex(final SearchIndexType index, final VFS vfs) {
		this.index = index;
		this.vfs = vfs;
	}

	public void add(final String uri) {
		index(uri);
	}

	public SearchIndexType getIndex() {
		return index;
	}

	private synchronized void index(final String uri) {
		if (uri.endsWith(".xml")
				&& ((uri.startsWith("drafts")) || (uri.startsWith("/drafts")) || (uri.startsWith("/browse/nodes"))
						|| (uri.startsWith("browse/nodes")) || (uri.startsWith("browse/assessments")) || (uri
						.startsWith("/browse/assessments")))) {
			final Document docToIndex = DocumentUtils.getVFSFileAsDocument(uri, vfs);
			try {
				index.index(docToIndex, vfs.getLastModified(uri));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * try{ Thread.sleep(25); // give back some CPU } catch
		 * (InterruptedException interrupted) {
		 * TrivialExceptionHandler.ignore(this,interrupted); }
		 */
	}

	public boolean isCurrent(final String uri) {
		try {
			final String sub = uri.substring(uri.lastIndexOf('/') + 1, uri.lastIndexOf("."));
			if (vfs.getLastModified(uri) > index.getLastModified(sub))
				return false;
		} catch (final Exception e) {
			// SysDebugger.getInstance().println("not current");
			return false;
		}
		// SysDebugger.getInstance().println("current");
		return true;
	}

}
