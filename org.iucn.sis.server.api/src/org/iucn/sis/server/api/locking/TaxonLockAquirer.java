package org.iucn.sis.server.api.locking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.shared.api.models.Taxon;

public class TaxonLockAquirer {
	private ArrayList<String> locks;
	private String[] ids;
	private ArrayList<String> retry;
	private int MAXTRIES = 5;
	private boolean success;

	public TaxonLockAquirer(Collection<Taxon> taxa) {
		retry = new ArrayList<String>();
		locks = new ArrayList<String>();
		ids = new String[taxa.size()];
		int i = 0;
		for (Taxon taxon : taxa) {
			ids[i] = taxon.getId()+"";
			i++;
		}
		success = false;
	}
	
	public TaxonLockAquirer(Taxon taxon) {
		this(Arrays.asList(taxon));
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
			String url = ServerPaths.getTaxonURL(ids[i]);
			if (!locks.contains(url)) {
				if (SIS.get().getLocker().aquireLock(url)) {
					locks.add(url);
				} else {
					retry.add(url);
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
				if (SIS.get().getLocker().aquireLock(retry.get(i))) {
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
		String url = ServerPaths.getTaxonURL(id);
		SIS.get().getLocker().releaseLock(url);
		locks.remove(url);
	}

	public void releaseLocks() {
		for (int i = 0; i < locks.size(); i++) {
			SIS.get().getLocker().releaseLock(locks.get(i));
		}
		locks.clear();
	}

}
