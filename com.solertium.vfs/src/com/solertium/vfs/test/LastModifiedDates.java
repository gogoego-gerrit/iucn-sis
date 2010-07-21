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
package com.solertium.vfs.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.solertium.vfs.ConflictException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.provider.VersionedFileVFS;

public class LastModifiedDates {

	VFS vfs = null;
	VFSPath testPath = new VFSPath("/test.txt");
	int seq = 0;
	
	@Before
	public void setUp() throws Exception {
		File tempDir = File.createTempFile("foo", "bar").getParentFile();
		try{
			vfs = VersionedFileVFS.create(new File(tempDir,"testvfs"));
		} catch (ConflictException cx) {
			vfs = VFSFactory.getVFS(new File(tempDir,"testvfs"));
		}
	}
	
	public void writeSomething() throws IOException {
		Writer w = vfs.getWriter(testPath);	
		w.write("testing testing 1,2,3 ... "+seq);
		w.close();
		seq++;
	}

	public long getMTime() throws IOException {
		return vfs.getLastModified(testPath);
	}

	@Test
	public void lastModifiedDates() throws IOException {
		for(int i=0;i<50;i++){
			long in = getMTime();
			writeSomething();
			long out = getMTime();
			System.out.println("mtime in:  " +in);
			System.out.println("mtime out: " +out);
			assertTrue(out>in);
		}
	}

	@After
	public void tearDown() throws Exception {
	}

}
