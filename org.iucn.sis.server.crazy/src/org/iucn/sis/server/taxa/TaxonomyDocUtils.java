package org.iucn.sis.server.taxa;

import java.io.File;
import java.util.Date;

import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.Mutex;
import com.solertium.util.NodeCollection;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPathToken;

public class TaxonomyDocUtils {
	private static class WriteBackTask implements Runnable {
		long delay = 5000;

		WriteBackTask() {
		}

		WriteBackTask(long delay) {
			this.delay = delay;
		}

		public void run() {
			try {
				if (lock.attempt(0)) {
					Thread.sleep(delay);

					writeTaxonomyDoc();
					writeTaxonomyDocByName();

					lock.release();
				} else
					return;
			} catch (Exception e) {
			}
		}
	}

	private static Document taxonomyDocByID = null;

	private static Document taxonomyDocByName = null;

	private static VFS vfs = null;

	private static Mutex lock = new Mutex();

	private static boolean writeBack = true;;

	private synchronized static void addNodeToTaxonomyDocByID(long id, String name, String parentID, VFS vfs) {
		readTaxonomyDocs();

		Element parent = null;

		if (!parentID.equals(""))
			parent = (Element) taxonomyDocByID.getElementsByTagName("node" + parentID).item(0);
		else
			parent = taxonomyDocByID.getDocumentElement();

		Element newNode = taxonomyDocByID.createElement("node" + id);
		newNode.setAttribute("name", name);

		if (parent == null)
			System.out.println("Parent is null. Looking for parent with ID " + parentID);

		parent.appendChild(newNode);
	}

	private synchronized static void addNodeToTaxonomyDocByName(long id, String name, String parentName,
			String parentID, String kingdomName, VFS vfs) {
		readTaxonomyDocs();

		Element parent = null;

		if (parentName != null && !parentName.equals("")) {
			NodeCollection list = new NodeCollection(taxonomyDocByName.getElementsByTagName(cleanName(parentName)));
			if( list.size() > 0 ) {
				for (Node curNode : list) {
					if (((Element) curNode).getAttribute("id").equals(parentID)) {
						parent = (Element) curNode;
						break;
					}
				}
			} else if( list.size() == 0 ) {
				System.out.println("******* Could NOT FIND ANY TRACE OF PARENT IN HIERARCHY -- Looking uppercased!!");
				list = new NodeCollection(taxonomyDocByName.getElementsByTagName(cleanName(parentName.toUpperCase())));
				if( list.size() > 0 ) {
					for (Node curNode : list) {
						if (((Element) curNode).getAttribute("id").equals(parentID)) {
							parent = (Element) curNode;
							break;
						}
					}
				}
			} else {
				parent = (Element)list.get(0);
			}
			
			if( parent == null )
				System.out.println("COULD NOT FIND PARENT " + parentID + ":" + parentName + " " +
						"FOR TAXON " + id + ":" + name + " IN KINGDOM " + kingdomName);
		} else
			parent = taxonomyDocByName.getDocumentElement();

		Element newNode = taxonomyDocByName.createElement(cleanName(name));
		newNode.setAttribute("id", "" + id);
		parent.appendChild(newNode);

		// TODO: Figure this OUT
		// Element parent = null;
		//		
		// if( !parentName.equals("") )
		// {
		// Element kingdom =
		// (Element)taxonomyDocByName.getDocumentElement().getElementsByTagName(
		// cleanName(kingdomName) ).item(0);
		//			
		// NodeCollection list = new NodeCollection(
		// taxonomyDocByName.getElementsByTagName( cleanName( parentName ) ) );
		// for( Node curParent : list )
		// {
		// System.out.println("Testing " + curParent.getNodeName());
		// Node curTest = curParent.getParentNode();
		//				
		// if( curTest != null && curTest.isSameNode( kingdom ) )
		// parent = (Element)curParent;
		// else
		// {
		// while( curTest != null && parent == null )
		// {
		// System.out.println("Testing " + curTest.getNodeName());
		// if( curTest.isSameNode( kingdom ) )
		// parent = (Element)curParent;
		// else
		// curTest = curTest.getParentNode();
		// }
		// }
		//				
		// if( parent != null )
		// break;
		// }
		// }
		// else
		// parent = taxonomyDocByName.getDocumentElement();
		//		
		// Element newNode = taxonomyDocByName.createElement( cleanName( name )
		// );
		// newNode.setAttribute("id", ""+id);
		// parent.appendChild(newNode);
	}

	public synchronized static void addTaxonToHierarchy(long id, String name, String fullName, String parentID,
			String parentName, VFS vfs, String kingdomName) {
		synchronized (taxonomyDocByID) {
			addNodeToTaxonomyDocByID(id, name, parentID, vfs);
		}
		synchronized (taxonomyDocByName) {
			addNodeToTaxonomyDocByName(id, fullName, parentName, parentID, kingdomName, vfs);
		}

		doWriteBack(5000);
	}

	public static void buildFromScratch(int numberOfThreads) {
		toggleWriteBack(false);
		try {
			taxonomyDocByID = DocumentUtils.createDocumentFromString("<taxonomicHierarchy></taxonomicHierarchy>");
			taxonomyDocByName = DocumentUtils.createDocumentFromString("<taxonomicHierarchy></taxonomicHierarchy>");

			SysDebugger.getInstance().println("Starting to build!");

			Date start = new Date();
			TaxonomyHierarchyBuilder builder = new TaxonomyHierarchyBuilder(vfs, numberOfThreads);
			builder.buildDocuments((VFSPathToken[]) null, false);
			Date end = new Date();

			SysDebugger.getInstance().println("Hierarchy build time: " + (end.getTime() - start.getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Taxa can contain white space, periods and apparently the ' character in
	 * their name, so we must strip them out when creating these nodes.
	 */
	public static String cleanName(String name) {
		if (name != null)
			return name.replaceAll("\\s", "").replaceAll("\\&", "AND").replaceAll("[\\W&&[^\\.\\-\\_\\:]]", "");
		else
			return null;
	}

	public static void doWriteBack(long delay) {
		if (writeBack)
			new Thread(new WriteBackTask()).start();
	}

	public static String getChildrenTaxaByID(String id) {
		readTaxonomyDocs();
		StringBuffer string = new StringBuffer();

		if (id != null && !id.trim().equals("")) {
			Element parent = getElementByID(id);
			if (parent != null) {
				NodeList nodeList = parent.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getNodeType() != Node.TEXT_NODE) {
						String childID = node.getNodeName().replace("node", "");
						string.append(childID + ",");
					}
				}
			}

		}
		if (string.length() > 0)
			return string.substring(0, string.length() - 1);
		else
			return string.toString();
	}

	private static Element getElementByID(String nodeID) {
		return ((Element) taxonomyDocByID.getElementsByTagName("node" + nodeID).item(0));
	}

	private static Element getElementByName(String kingdom, String nodeName, String id) {
		nodeName = cleanName(nodeName);

		if (nodeName == null || kingdom.equalsIgnoreCase(nodeName))
			return (Element) taxonomyDocByName.getElementsByTagName(kingdom).item(0);

		NodeList list = taxonomyDocByName.getElementsByTagName(nodeName);
		
		for( Node curNode : new NodeCollection(list) ) {
			if( getKingdomName(curNode, taxonomyDocByName.getDocumentElement().getNodeName() ).equals(kingdom) ) {
				if (id != null && !((Element)curNode).getAttribute("id").equals(id) )
					System.out.println(" ---- WARNING ---- Could not match " + nodeName + " in kingdom " + kingdom
							+ " with id " + id + ". Returning node anyway.");
				
				return (Element)curNode;
			}
		}
		
		return null;
	}

	private static String getKingdomName(Node node, String docElementName) {
		Node cur = node.getParentNode();
		while( !cur.getParentNode().getNodeName().equals(docElementName) )
			cur = cur.getParentNode();
		
		return cur.getNodeName();
	}
	
	public static String getHierarchyAndChildrenByID(String id) {
		readTaxonomyDoc();
		NodeList children;
		String idFootprint;

		if (id != null) {
			Node node = taxonomyDocByID.getDocumentElement().getElementsByTagName("node" + id).item(0);
			children = node.getChildNodes();

			idFootprint = id;

			node = node.getParentNode();
			while (!node.equals(taxonomyDocByID.getDocumentElement())) {
				String temp = node.getNodeName().replaceFirst("node", "");
				idFootprint = temp + "-" + idFootprint;
				node = node.getParentNode();
			}

		} else {
			children = TaxonomyDocUtils.taxonomyDocByID.getDocumentElement().getChildNodes();
			idFootprint = "";
		}

		return getHierarchyFootprintXML(children, idFootprint);

	}

	private static String getHierarchyFootprintXML(NodeList children, String footprint) {
		String[] options = new String[children.getLength()];
		for (int i = 0; i < options.length; i++)
			options[i] = children.item(i).getNodeName();

		String xml = "<hierarchy>\r\n";
		xml += "<footprint>" + (footprint == null ? "" : footprint) + "</footprint>\r\n";

		xml += "<options>\r\n";
		for (int i = 0; i < options.length; i++)
			if (options[i].startsWith("node"))
				xml += "<option>" + options[i].substring(4) + "</option>\r\n";
		xml += "</options>\r\n";

		xml += "</hierarchy>";

		return xml;
	}

	public static String getHierarchyXML(String hierarchy) {
		readTaxonomyDocs();

		NodeList children;

		if (hierarchy != null) {
			if (hierarchy.indexOf("-") < 0)
				children = taxonomyDocByID.getDocumentElement().getElementsByTagName("node" + hierarchy).item(0)
						.getChildNodes();
			else {
				String[] split = hierarchy.split("-");
				Element cur = taxonomyDocByID.getDocumentElement();

				for (int i = 0; i < split.length; i++)
					cur = (Element) cur.getElementsByTagName("node" + split[i]).item(0);

				children = cur.getChildNodes();
			}
		} else
			children = TaxonomyDocUtils.taxonomyDocByID.getDocumentElement().getChildNodes();

		return getHierarchyFootprintXML(children, hierarchy);
	}

	public static String getIDByName(String kingdomName, String nodeName) {
		readTaxonomyDocs();

		try {
			return getElementByName(kingdomName, nodeName, null).getAttribute("id");
		} catch (Exception notFound) {
			return null;
		}
	}

	public static String getNameByID(String nodeID) {
		readTaxonomyDocs();

		try {
			return getElementByID(nodeID).getAttribute("name");
		} catch (Exception e) {
			return null;
		}
	}

	public static Document getTaxonomyDocByID() {
		readTaxonomyDocs(); // just in case
		return taxonomyDocByID;
	}

	public static Document getTaxonomyDocByName() {
		readTaxonomyDocs(); // just in case
		return taxonomyDocByName;
	}

	public static void init(String vfsroot) {
		final File spec = new File(vfsroot);
		try {
			vfs = VFSFactory.getVFS(spec);
			readTaxonomyDoc();
			readTaxonomyDocByName();

		} catch (final NotFoundException nf) {
			throw new RuntimeException("VFS " + spec.getPath() + " could not be opened.");
		}
	}

	private static boolean readTaxonomyDoc() {
		boolean success = false;
		if (FileLocker.impl.aquireLock(ServerPaths.getTaxonomyDocURL())) {
			if (!vfs.exists(ServerPaths.getTaxonomyDocURL()))
				taxonomyDocByID = DocumentUtils.createDocumentFromString("<taxonomicHierarchy></taxonomicHierarchy>");
			else
				taxonomyDocByID = DocumentUtils.getVFSFileAsDocument(ServerPaths.getTaxonomyDocURL(), vfs);
			DocumentUtils.unversion(ServerPaths.getTaxonomyDocURL(), vfs);
			FileLocker.impl.releaseLock(ServerPaths.getTaxonomyDocURL());
		}
		return success;
	}

	private static boolean readTaxonomyDocByName() {
		boolean success = false;
		if (FileLocker.impl.aquireLock(ServerPaths.getTaxonomyByNameURL())) {
			if (!vfs.exists(ServerPaths.getTaxonomyByNameURL()))
				taxonomyDocByName = DocumentUtils.createDocumentFromString("<taxonomicHierarchy></taxonomicHierarchy>");
			else
				taxonomyDocByName = DocumentUtils.getVFSFileAsDocument(ServerPaths.getTaxonomyByNameURL(), vfs);
			DocumentUtils.unversion(ServerPaths.getTaxonomyByNameURL(), vfs);
			FileLocker.impl.releaseLock(ServerPaths.getTaxonomyDocURL());
		}
		return success;
	}

	private static void readTaxonomyDocs() {
		if (taxonomyDocByID == null) {
			readTaxonomyDoc();
			readTaxonomyDocByName();
		}
	}

	public static boolean removeTaxonFromHierarchy(long id, String kingdom, String fullName) throws Exception {
		Element idNode = null;
		Element nameNode = null;

		synchronized (taxonomyDocByID) {
			idNode = getElementByID(id + "");
		}

		synchronized (taxonomyDocByName) {
			nameNode = getElementByName(kingdom, fullName, id + "");
		}

		if (idNode == null || nameNode == null) {
			System.out.println("BOTH nodes not found for removal of taxon " + id + " w/full name " + fullName);
			return false;
		} else {
			boolean ret = true;

			synchronized (taxonomyDocByID) {
				if (nameNode.getParentNode().removeChild(nameNode) == null)
					ret = false;
			}
			synchronized (taxonomyDocByName) {
				if (idNode.getParentNode().removeChild(idNode) == null)
					ret = false;
			}

			toggleWriteBack(true); // Make sure it's going to write back...
			doWriteBack(0);
			
			return ret;
		}
	}

	public static void renameTaxon(String kingdom, String id, String oldFullName, String newName, String newFullName) {
		Element idDocElement = getElementByID(id);
		Element nameDocElement = getElementByName(kingdom, oldFullName, id);

		synchronized (taxonomyDocByID) {
			idDocElement.setAttribute("name", newName);
			// System.out.println("In TaxonomyDocUtils.renameTaxon(): renamed name "
			// +
			// "attribute for taxon " + id + " to " + newName);
		}
		synchronized (taxonomyDocByName) {
			// System.out.println("In TaxonomyDocUtils.renameTaxon(): renaming node "
			// +
			// nameDocElement.getNodeName() + " to " + cleanName(newFullName));
			taxonomyDocByName.renameNode(nameDocElement, "", cleanName(newFullName));
			// System.out.println(" to " + nameDocElement.getNodeName());
		}

		toggleWriteBack(true); // Make sure it's going to write back...
		doWriteBack(0);
	}

	public static void renameTaxon(TaxonNode oldNode, TaxonNode newNode) {
		renameTaxon(oldNode.getFootprint()[0], oldNode.getId() + "", oldNode.getFullName(), newNode.getName(), newNode
				.getFullName());
	}

	public static void toggleWriteBack(boolean doWriteBack) {
		writeBack = doWriteBack;
	}

	private static boolean writeTaxonomyDoc() {
		boolean success = false;
		if (FileLocker.impl.aquireLock(ServerPaths.getTaxonomyDocURL())) {
			DocumentUtils.writeVFSFile(ServerPaths.getTaxonomyDocURL(), vfs, taxonomyDocByID);
			FileLocker.impl.releaseLock(ServerPaths.getTaxonomyDocURL());
			success = true;
		}
		return success;
	}

	private static boolean writeTaxonomyDocByName() {
		boolean success = false;
		if (FileLocker.impl.aquireLock(ServerPaths.getTaxonomyByNameURL())) {
			DocumentUtils.writeVFSFile(ServerPaths.getTaxonomyByNameURL(), vfs, taxonomyDocByName);
			FileLocker.impl.releaseLock(ServerPaths.getTaxonomyByNameURL());
			success = true;
		}
		return success;
	}
}
