package org.iucn.sis.server.utils;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VersionedVFS;

public class DemoVersionAPI {

	private static final String PATH = "/var/sis/sistest-copy/sis/vfs";

	public static void main(final String[] args) throws Exception {

		final VersionedVFS testvfs = VFSFactory.getVersionedVFS(new File(PATH));
		SysDebugger.getInstance().println("Type: " + testvfs.getClass().getName());

		final String uri = "/browse/assessments/10/1011.xml";
		final List<String> versions = testvfs.getRevisionIDsBefore(uri, null, -1);
		for (final String v : versions) {
			SysDebugger.getInstance().println("Version ID: " + v);
			SysDebugger.getInstance().println("Last modified " + new Date(testvfs.getLastModified(uri, v)));
			SysDebugger.getInstance().println("Length " + testvfs.getLength(uri, v));
		}

	}

}
