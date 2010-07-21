package org.iucn.sis.server.io;

import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.WorkingSetParser;
import org.w3c.dom.Document;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * Performs file system IO operations for Working Sets.
 * 
 * @author adam.schwartz
 *
 */
public class WorkingSetIO {

	public static WorkingSetData readPublicWorkingSetAsWorkingSetData(VFS vfs, String workingSetID) {
		String uri = ServerPaths.getPublicWorkingSetURL(workingSetID);
		String xml = DocumentUtils.getVFSFileAsString(uri, vfs);
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(xml);
		WorkingSetParser p = new WorkingSetParser();
		return p.parseSingleWorkingSet(ndoc.getDocumentElement());
	}
	
	public static WorkingSetData readPrivateWorkingSetAsWorkingSetData(VFS vfs, String workingSetID, String username) {
		String uri = ServerPaths.getPrivateWorkingSetURL(username, workingSetID);
		String xml = DocumentUtils.getVFSFileAsString(uri, vfs);
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(xml);
		WorkingSetParser p = new WorkingSetParser();
		return p.parseSingleWorkingSet(ndoc.getDocumentElement());
	}
	
	public static Document readPrivateWorkingSetAsDocument(VFS vfs, String workingSetID, String username) {
		String uri = ServerPaths.getPrivateWorkingSetURL(username, workingSetID);
		return DocumentUtils.getVFSFileAsDocument(uri, vfs);
	}
	
	public static String readPrivateWorkingSetAsString(VFS vfs, String workingSetID, String username) {
		String uri = ServerPaths.getPrivateWorkingSetURL(username, workingSetID);
		return DocumentUtils.getVFSFileAsString(uri, vfs);
	}

	public static Document readPublicWorkingSetAsDocument(VFS vfs, String workingSetID) {
		String uri = ServerPaths.getPublicWorkingSetURL(workingSetID);
		return DocumentUtils.getVFSFileAsDocument(uri, vfs);
	}

	public static String readPublicWorkingSetAsString(VFS vfs, String workingSetID) {
		String uri = ServerPaths.getPublicWorkingSetURL(workingSetID);
		return DocumentUtils.getVFSFileAsString(uri, vfs);
	}
	
	/**
	 * Deletes a working set. 
	 * Performs NO checks for extant permission groups or subscriptions first.
	 * 
	 * @param vfs
	 * @param workingSetID - public working set ID
	 * @return success or failure
	 */
	public static boolean deleteWorkingSet(VFS vfs, String workingSetID) {
		try {		
			VFSPath trashURI = new VFSPath("/trash/workingsets");
			if( !vfs.exists( trashURI ) )
				vfs.makeCollections(trashURI);
		
			VFSPath from = new VFSPath(ServerPaths.getPublicWorkingSetURL(workingSetID));
			VFSPath to = trashURI.child(new VFSPathToken(workingSetID + ".xml"));
			vfs.move(from, to);
			
			return vfs.exists(to);
		} catch (ConflictException e) {
			e.printStackTrace();
			return false;
		} catch (NotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
}
