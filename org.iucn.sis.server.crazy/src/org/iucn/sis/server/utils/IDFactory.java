package org.iucn.sis.server.utils;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFS;

public class IDFactory {

	class IDSaver implements Runnable {
		public void run() {
			synchronized (saving) {
				SysDebugger.getInstance().println("I am saving");
				try {
					saving.set(true);
					java.io.Writer writer = vfs.getWriter(uri);
					writer.write(id.get() + "");
					writer.close();
					saving.set(false);
				} catch (Exception e) {
					saving.set(false);
					e.printStackTrace();
				}
			}
		}

	}

	public static IDFactory getIDFactory(VFS vfs, String uri) {
		synchronized (IDFactory.class) {
			String key = "" + vfs.hashCode() + ":" + uri;
			IDFactory idf = factories.get(key);
			if (idf == null)
				idf = new IDFactory(vfs, uri);
			return idf;
		}
	}

	AtomicLong id;
	AtomicBoolean saving;

	final VFS vfs;

	final String uri;

	private static HashMap<String, IDFactory> factories = new HashMap<String, IDFactory>();

	/**
	 * Given a uri, if the uri exists it opens and gets number out of it, and
	 * stores it as the last used id. Otherwise creates file at the given uri
	 * and stores the id as 0.
	 * 
	 * @param vfs
	 * @param uri
	 */
	private IDFactory(VFS vfs, String uri) throws NumberFormatException {

		this.vfs = vfs;
		this.uri = uri;
		saving = new AtomicBoolean(false);
		id = new AtomicLong();
		DocumentUtils.unversion(uri, vfs);
		if (vfs.exists(uri)) {
			String file = DocumentUtils.getVFSFileAsString(uri, vfs);
			if (file.trim().equals("0"))
				id.set(300000);
			else
				id.set(Long.valueOf(file.trim()));
		}

		else {
			DocumentUtils.writeVFSFile(uri, vfs, "300000");
			id.set(300000);
		}

	}

	public long getNextIDAsLong() {
		long idLong = id.incrementAndGet();
		new Thread(new IDSaver()).start();// run();
		return idLong;
	}

	public String getNextIDAsString() {
		String idString = id.incrementAndGet() + "";
		new Thread(new IDSaver()).start();// run();
		return idString;
	}

}
