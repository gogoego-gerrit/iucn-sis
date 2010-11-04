package org.iucn.sis.shared.conversions;

import com.solertium.vfs.VFS;

public class VFSInfo {
	
	private final String oldVFSPath;
	private final VFS oldVFS, newVFS;
	
	public VFSInfo(String oldVFSPath, VFS oldVFS, VFS newVFS) {
		this.oldVFSPath = oldVFSPath;
		this.oldVFS = oldVFS;
		this.newVFS = newVFS;
	}
	
	public VFS getOldVFS() {
		return oldVFS;
	}
	
	public VFS getNewVFS() {
		return newVFS;
	}
	
	public String getOldVFSPath() {
		return oldVFSPath;
	}

}
