package org.iucn.sis.server.utils;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VersionedVFS;

public class DemoVersionAPI {

	private static final String PATH = "/var/sis/sistest-copy/sis/vfs";

	public static void main(final String[] args) throws Exception {

		final VersionedVFS testvfs = VFSFactory.getVersionedVFS(new File(PATH));
		System.out.println("Type: " + testvfs.getClass().getName());

		final String uri = "/browse/assessments/10/1011.xml";
		final List<String> versions = testvfs.getRevisionIDsBefore(uri, null, -1);
		for (final String v : versions) {
			System.out.println("Version ID: " + v);
			System.out.println("Last modified " + new Date(testvfs.getLastModified(uri, v)));
			System.out.println("Length " + testvfs.getLength(uri, v));
		}

	}

}
