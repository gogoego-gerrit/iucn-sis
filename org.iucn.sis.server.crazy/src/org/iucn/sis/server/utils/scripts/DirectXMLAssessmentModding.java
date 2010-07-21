package org.iucn.sis.server.utils.scripts;

import org.iucn.sis.server.utils.DocumentUtils;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSRevisionUtils;
import com.solertium.vfs.VersionedVFS;

public class DirectXMLAssessmentModding {
	public static int count = 0;

	private static void removeBadlyCleanedXMLFromFile(VFS vfs, String path) throws NotFoundException {
		boolean writeback = false;
		String file = DocumentUtils.getVFSFileAsString(path, vfs);
		{
			if (file.matches("\\Q&amp;lt;\\E")) {
				file = file.replaceAll("\\Q&amp;lt;\\E", "&lt;");
				writeback = true;
			}
			if (file.matches("\\Q&amp;gt;\\E")) {
				file = file.replaceAll("\\Q&amp;gt;\\E", "&gt;");
				writeback = true;
			}
			if (file.matches("\\Q&amp;quot;\\E")) {
				file = file.replaceAll("\\Q&amp;quot;\\E", "&quot;");
				writeback = true;
			}
			if (file.matches("\\Q&amp;amp;\\E")) {
				file = file.replaceAll("\\Q&amp;amp;\\E", "&amp;");
				writeback = true;
			}
		}

		if (writeback)
			DocumentUtils.writeVFSFile(path, vfs, file);
	}

	public static void removedBadlyCleanedXML(VFS vfs, String rootPath) throws NotFoundException {
		try {
			for (String url : vfs.list(rootPath)) {
				String curURL = rootPath + url;

				SysDebugger.getInstance().println("Checking " + curURL);

				if (vfs.isCollection(curURL))
					removedBadlyCleanedXML(vfs, curURL + "/");
				else
					removeBadlyCleanedXMLFromFile(vfs, curURL);
			}
		} catch (NotFoundException e) {
			System.out.println("Could not find path " + rootPath);
		}
	}

	public static void revertToLastVersion(VersionedVFS vfs, String rootPath) throws NotFoundException,
			ConflictException {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}

		for (String url : vfs.list(rootPath)) {
			if (url.charAt(0) == '.')
				continue;

			String curURL = rootPath + url;

			if (vfs.isCollection(curURL)) {
				try {
					revertToLastVersion(vfs, curURL + "/");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error recursing into url " + curURL);
				}
			}

			else {
				try {
					VFSRevisionUtils.revertToLastUndo(vfs, new VFSPath(curURL));
					count++;

					if (count % 2000 == 0)
						System.out.println("Through " + count + " assessments.");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error reverting to last UNDO for url " + curURL);
				}

			}
		}
	}
}
