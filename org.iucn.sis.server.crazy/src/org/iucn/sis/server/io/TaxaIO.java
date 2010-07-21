package org.iucn.sis.server.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.server.crossport.export.DBMirrorManager;
import org.iucn.sis.server.locking.TaxonLockAquirer;
import org.iucn.sis.server.taxa.TaxonomyHierarchyBuilder;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;

/**
 * Performs file system IO operations for Taxa.
 * 
 * @author adam.schwartz
 *
 */
public class TaxaIO {

	/**
	 * Reads a taxon in from the file system based on an ID and parses it using the 
	 * TaxonNodeFactory. 
	 * 
	 * @param id of the taxon
	 * @param vfs
	 * @return a fleshed out TaxonNode
	 */
	public static TaxonNode readNode(String id, VFS vfs) {
		String xml = DocumentUtils.getVFSFileAsString(ServerPaths.getURLForTaxa(id), vfs);
		if( xml != null )
			return TaxonNodeFactory.createNode(xml, null, false);
		else
			return null;
	}
	
	/**
	 * Reads taxa in from the file system based on an ID and parses it using the 
	 * TaxonNodeFactory. If a taxon cannot be read in, nullFail determines whether
	 * it immediately returns a null response or if it skips over it and returns
	 * what it can; if nullFail is true, null is returned.
	 * 
	 * @param ids of the taxon
	 * @param vfs
	 * @param nullFail - if a taxon cannot be read in, e.g. if it's missing, nullFail == true
	 * will cause the returned list to be null
	 * @return a list of TaxonNode objects
	 */
	public static List<TaxonNode> readNodes(String [] ids, boolean nullFail, VFS vfs) {
		List<TaxonNode> list = new ArrayList<TaxonNode>();
		for( String id : ids ) {
			TaxonNode taxon = readNode(id, vfs);
			if( taxon != null )
				list.add(taxon);
			else if( nullFail )
				return null;
		}
		
		return list;
	}
	
	/**
	 * Returns an xml document containing the ids of all the nodes that were
	 * written, null if the write was unsuccessful
	 * 
	 * @param nodeToSave
	 * @return summary
	 */
	public static String writeNode(TaxonNode nodeToSave, VFS vfs) {
		ArrayList<TaxonNode> list = new ArrayList<TaxonNode>();
		list.add(nodeToSave);
		return writeNodes(list, vfs, true);
	}
	
	/**
	 * Returns an xml document containing the ids of all the nodes that were
	 * written, null if the write was unsuccessful
	 * 
	 * @param nodeToSave
	 * @return summary
	 */
	public static String writeNode(TaxonNode nodeToSave, VFS vfs, boolean requireLocking) {
		ArrayList<TaxonNode> list = new ArrayList<TaxonNode>();
		list.add(nodeToSave);
		return writeNodes(list, vfs, requireLocking);
	}

	/**
	 * This function is a slight variation of writeNodeandDocument to facilitate
	 * the undo Taxomatic operation, which has different information available
	 * than
	 * 
	 * @param nodesToSave
	 * @param nodesWithDocumentChanges
	 * @param idsWithOldFullName
	 * @return
	 */
	public static boolean writeNodeandDocumentChanges(Map<String, TaxonNode> nodesToSave,
			Map<Long, String> idsWithOldFullName, List<String> docChanges, VFS vfs) {
		boolean success = true;
		TaxonomyHierarchyBuilder builder = new TaxonomyHierarchyBuilder(vfs);
		try {

			ArrayList<TaxonNode> nodesWithDocChanges = new ArrayList<TaxonNode>();
			for (String id : docChanges) {
				nodesWithDocChanges.add(nodesToSave.get(id));
			}

			builder.buildDocuments(nodesWithDocChanges, idsWithOldFullName, false);
			writeNodes(new ArrayList<TaxonNode>(nodesToSave.values()), vfs, true);
//			for (Entry<String, TaxonNode> entry : nodesToSave.entrySet()) {
//				DocumentUtils.writeVFSFile(ServerPaths.getURLForTaxa(entry.getKey()), vfs, TaxonNodeFactory
//						.nodeToDetailedXML(entry.getValue()));
//			}

		} catch (NotFoundException e) {
			e.printStackTrace();
			success = false;
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}

		return success;
	}

	/**
	 * Returns an xml document containing the ids of all the nodes that were
	 * written, null if the write was unsuccessful
	 * 
	 * @param nodesToSave
	 * @param requireLocking TODO
	 * @param nodesWithDocChanges
	 *            TODO
	 * @return
	 */
	public static String writeNodes(List<TaxonNode> nodesToSave, VFS vfs, boolean requireLocking) {
		StringBuffer xml = new StringBuffer("<nodes>");

		// TRY TO AQUIRE LOCKS
		TaxonLockAquirer aquirer = new TaxonLockAquirer(nodesToSave);
		if( requireLocking )
			aquirer.aquireLocks();
		
		if (!requireLocking || aquirer.isSuccess()) {
			try {
				for (int i = 0; i < nodesToSave.size(); i++) {
					DocumentUtils.writeVFSFile(ServerPaths.getURLForTaxa(nodesToSave.get(i).getId() + ""), vfs,
							TaxonNodeFactory.nodeToDetailedXML(nodesToSave.get(i)));
					xml.append(nodesToSave.get(i).getId() + ",");
				}

				// UNLOCK FILES
				if( requireLocking )
					aquirer.releaseLocks();
				
				xml.append("</nodes>");
				
				DBMirrorManager.impl.taxaChanged(nodesToSave);
				
				return xml.toString();
			} catch (Exception severeError) {
				// UNLOCK FILES
				severeError.printStackTrace();
				aquirer.releaseLocks();
				return null;
			}

		} else {
			SysDebugger.getInstance().println("Failed to acquire locks.");
			return null;
		}
	}

	/**
	 * Returns an xml document containing the ids of all the nodes that were
	 * written, null if the write was unsuccessful
	 * 
	 * @param nodesToSave
	 * @param nodesWithDocChanges
	 * @param failOnDupeFound
	 * @return
	 */
	public static String writeNodesAndDocument(ArrayList<TaxonNode> nodesToSave,
			ArrayList<TaxonNode> nodesWithDocChanges, HashMap<Long, String> idToOldFullName, VFS vfs,
			boolean failOnDupeFound) {
		StringBuffer xml = new StringBuffer("<nodes>");

		TaxonomyHierarchyBuilder builder = new TaxonomyHierarchyBuilder(vfs);

		try {
			builder.buildDocuments(nodesWithDocChanges, idToOldFullName, false);
		} catch (NotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return writeNodes(nodesToSave, vfs, true);
	}
}
