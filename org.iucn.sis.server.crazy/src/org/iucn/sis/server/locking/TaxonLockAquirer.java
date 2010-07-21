package org.iucn.sis.server.locking;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

public class TaxonLockAquirer {
	private ArrayList<String> locks;
	private String[] ids;
	private ArrayList<String> retry;
	private int MAXTRIES = 5;
	private boolean success;

	public TaxonLockAquirer(List<TaxonNode> nodes) {
		retry = new ArrayList<String>();
		locks = new ArrayList<String>();
		ids = new String[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			// SysDebugger.getNamedInstance(SysDebugger.FINEST +
			// "").println("added " + nodes.get(i).getId() + "");
			ids[i] = nodes.get(i).getId() + "";
		}
		success = false;
	}

	public TaxonLockAquirer(String ids) {
		this(ids.split(","));
	}

	public TaxonLockAquirer(String[] ids) {
		retry = new ArrayList<String>();
		locks = new ArrayList<String>();
		this.ids = ids;
		success = false;
	}

	public void aquireLocks() {
		for (int i = 0; i < ids.length; i++) {
			String url = ServerPaths.getURLForTaxa(ids[i]);
			if (!locks.contains(url)) {
				if (FileLocker.impl.aquireLock(url)) {
					locks.add(url);
					// SysDebugger.getInstance().println("I got lock for " +
					// url);
				} else {
					retry.add(url);
					// SysDebugger.getInstance().println(
					// "I failed to get lock for "
					// + url);
				}
			}
		}

		int counter = 0;
		while (retry.size() > 0 && counter < MAXTRIES) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {

			}
			for (int i = 0; i < retry.size(); i++) {
				if (FileLocker.impl.aquireLock(retry.get(i))) {
					locks.add(retry.remove(i));
				}
			}
			counter++;
		}

		if (retry.size() == 0) {
			success = true;
		} else {
			releaseLocks();
		}
	}

	public ArrayList<String> getLocks() {
		return locks;
	}

	public boolean isSuccess() {
		return success;
	}

	public void releaseLock(String id) {
		String url = ServerPaths.getURLForTaxa(id);
		FileLocker.impl.releaseLock(url);
		locks.remove(url);
	}

	public void releaseLocks() {
		for (int i = 0; i < locks.size(); i++) {
			FileLocker.impl.releaseLock(locks.get(i));
		}
		locks.clear();
	}

}
