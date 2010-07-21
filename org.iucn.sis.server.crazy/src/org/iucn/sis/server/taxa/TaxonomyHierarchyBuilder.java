package org.iucn.sis.server.taxa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.users.utils.Arrays16Emulation;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.SysDebugger;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

public class TaxonomyHierarchyBuilder {
	class FileProcessor implements Runnable {
		VFSPathToken[] uris;
		int start;
		int end;

		public FileProcessor(VFSPathToken[] uris, int start, int end) {
			this.uris = uris;
			this.start = start;
			this.end = end;
		}

		public void run() {
			try {
				VFSPath root = VFSUtils.parseVFSPath("/browse/nodes/");

				for (int i = start; i <= end; i++) {
					if (vfs.isCollection(root.child(uris[i])))
						processURI(root.child(uris[i]));
					else
						processFile(root.child(uris[i]));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<ConcurrentHashMap<Long, TaxonNode>> levels;
	private ConcurrentHashMap<Long, TaxonNode> kingdoms;
	private ConcurrentHashMap<Long, TaxonNode> phylums;
	private ConcurrentHashMap<Long, TaxonNode> classes;
	private ConcurrentHashMap<Long, TaxonNode> orders;
	private ConcurrentHashMap<Long, TaxonNode> family;
	private ConcurrentHashMap<Long, TaxonNode> genus;
	private ConcurrentHashMap<Long, TaxonNode> species;
	private ConcurrentHashMap<Long, TaxonNode> infraranks;
	private ConcurrentHashMap<Long, TaxonNode> subpopulations;

	private ConcurrentHashMap<Long, TaxonNode> infrasubpopulations;

	private VFS vfs;

	private ArrayList<TaxonNode> failures;

	private final int NUMBER_OF_THREADS;

	/**
	 * Instantiate a new one for each use.
	 * 
	 * @param vfs
	 */
	public TaxonomyHierarchyBuilder(VFS vfs) {
		this(vfs, 40);
	}

	public TaxonomyHierarchyBuilder(VFS vfs, int numberOfThreads) {
		this.vfs = vfs;

		this.NUMBER_OF_THREADS = numberOfThreads;

		kingdoms = new ConcurrentHashMap<Long, TaxonNode>();
		phylums = new ConcurrentHashMap<Long, TaxonNode>();
		classes = new ConcurrentHashMap<Long, TaxonNode>();
		orders = new ConcurrentHashMap<Long, TaxonNode>();
		family = new ConcurrentHashMap<Long, TaxonNode>();
		genus = new ConcurrentHashMap<Long, TaxonNode>();
		species = new ConcurrentHashMap<Long, TaxonNode>();
		infraranks = new ConcurrentHashMap<Long, TaxonNode>();
		subpopulations = new ConcurrentHashMap<Long, TaxonNode>();
		infrasubpopulations = new ConcurrentHashMap<Long, TaxonNode>();

		levels = new ArrayList<ConcurrentHashMap<Long, TaxonNode>>();
		levels.add(0, kingdoms);
		levels.add(1, phylums);
		levels.add(2, classes);
		levels.add(3, orders);
		levels.add(4, family);
		levels.add(5, genus);
		levels.add(6, species);
		levels.add(7, infraranks);
		levels.add(8, subpopulations);
		levels.add(9, infrasubpopulations);
	}

	public void buildDocuments(ArrayList<TaxonNode> nodes, Map<Long, String> idToOldFullname, boolean failOnDupeFound)
			throws NotFoundException, IOException {
		clearLevel();
		for (TaxonNode cur : nodes) {
			levels.get(cur.getLevel()).put(new Long(cur.getId()), cur);
		}
		updateTaxonomyDoc(idToOldFullname, true, failOnDupeFound);

	}

	public void buildDocuments(VFSPathToken[] uris, boolean removeFirst) throws NotFoundException, IOException,
			VFSPathParseException {
		Thread[] threads = new Thread[NUMBER_OF_THREADS];

		if (uris == null)
			uris = vfs.list(VFSUtils.parseVFSPath("/browse/nodes"));

		readInNodes(uris, threads);
		updateTaxonomyDoc(null, removeFirst, false);
	}

	private void clearLevel() {
		for (ConcurrentHashMap<Long, TaxonNode> a : levels) {
			a.clear();
		}
	}

	private void processFile(VFSPath URI) throws NotFoundException, IOException {
		if (!URI.toString().endsWith(".xml")) {
			SysDebugger.getNamedInstance("info").println(
					"***** NOTE: URL does not represent " + "a valid XML file. The extension is wrong: " + URI);
			return;
		}

		String file = DocumentUtils.getVFSFileAsString(URI.toString(), vfs);
		NativeDocument doc = SISContainerApp.newNativeDocument(null);

		try {
			doc.parse(file);
			TaxonNode cur = TaxonNodeFactory.createNode(doc);

			levels.get(cur.getLevel()).put(Long.valueOf(cur.getId()), cur);
		} catch (Throwable e) {
			SysDebugger.getNamedInstance("info").println("***** ERROR PARSING: \n" + file);
		}
	}

	private void processURI(VFSPath URI) throws NotFoundException, IOException {
		VFSPathToken[] uris = vfs.list(URI);

		for (VFSPathToken curFile : uris) {
			if (vfs.isCollection(URI.child(curFile)))
				processURI(URI.child(curFile));
			else
				processFile(URI.child(curFile));
		}
	}

	private void readInNodes(VFSPathToken[] uris, Thread[] threads) {
		int base = uris.length / NUMBER_OF_THREADS;
		int start = 0;
		int end = 0;

		for (int i = 0; i < threads.length; i++) {
			start = i * base;

			if (i == threads.length - 1)
				end = uris.length - 1;
			else
				end = (i * base) + base - 1;

			threads[i] = new Thread(new FileProcessor(uris, start, end));

			SysDebugger.getInstance().println("Thread " + i + " processing " + start + ":" + end);
			threads[i].start();
		}

		boolean interrupted = true;
		while (interrupted) {
			try {
				interrupted = false;
				for (int i = 0; i < threads.length; i++)
					threads[i].join();
			} catch (Exception e) {
				e.printStackTrace();
				interrupted = true;
			}
		}
	}

	private void updateTaxonomyDoc(Map<Long, String> idToOldFullname, boolean removeFirst, boolean failOnDupeFound) {
		TaxonomyDocUtils.toggleWriteBack(false);
		failures = new ArrayList<TaxonNode>();

		int level = 0;
		int numTaxa = 0;
		for (int j = 0; j < levels.size(); j++) {
			ConcurrentHashMap<Long, TaxonNode> curMap = levels.get(j);

			for (Entry<Long, TaxonNode> curEntry : curMap.entrySet()) {
				Long curID = curEntry.getKey();
				TaxonNode info = curMap.get(curID);

				try {
					verifyParentName(info);
					// verifyParentNameByID( info );
				} catch (Exception e) {
					e.printStackTrace();
					SysDebugger.getInstance().println("Error validating parent name for: " + info.getFullName());
				}

				if (removeFirst) {
					try {
						if (idToOldFullname != null && idToOldFullname.containsKey(curID))
							TaxonomyDocUtils.removeTaxonFromHierarchy(curID.longValue(), info.getFootprint()[0],
									idToOldFullname.get(curID));
						else
							TaxonomyDocUtils.removeTaxonFromHierarchy(curID.longValue(), info.getFootprint()[0], info
									.getFullName());
					} catch (Exception e) {
						e.printStackTrace();
						SysDebugger.getInstance().println("Failed to remove from hierarchy: " + info.getFullName());
					}
				}

				try {
					if (info.getFootprint().length != 0) {
						String dupeID = TaxonomyDocUtils.getIDByName(info.getFootprint()[0], info.getFullName());
						if (dupeID != null)
							System.out.println("Found myself... " + info.getFullName() + ". Old/New IDs: " + dupeID
									+ "/" + info.getId());

						if (dupeID != null && failOnDupeFound)
							throw new Exception("Found a dupe " + info.getId());
					}

					try {
						TaxonomyDocUtils.addTaxonToHierarchy(curID.longValue(), info.getName(), info.getFullName(),
								info.getParentId(), info.getParentName(), vfs, info.getFootprint().length == 0 ? ""
										: info.getFootprint()[0]);
					} catch (Exception e) {
						System.out.println("Failed to add: " + info.getFullName() + ":" + info.getId() + " (curID="
								+ curID + ") to parent " + info.getParentName());
					}
				} catch (Exception e) {
					e.printStackTrace();
					failures.add(info);
					SysDebugger.getInstance().println(TaxonNodeFactory.nodeToDetailedXML(info));
					SysDebugger.getInstance().println("Failed to add this guy.");
				}

				numTaxa++;
			}
			SysDebugger.getInstance().println("Through level " + level);
			SysDebugger.getInstance().println("Through " + numTaxa + " taxa.");
			level++;
		}

		// for( TaxonNode failure : failures )
		// {
		// try
		// {
		// //Try resetting the parent's ID
		// failure.setParentId( TaxonomyDocUtils.getIDByName(
		// failure.getFootprint()[0], failure.getParentName() ) );
		//				
		// TaxonomyDocUtils.addTaxonToHierarchy(failure.getId(),
		// failure.getName(),
		// failure.getFullName(), failure.getParentId(),
		// failure.getParentName(), vfs);
		//				
		// NativeDocument ndoc = SISContainerApp.newNativeDocument(request);
		// ndoc.put("/browse/taxonomy/" + failure.getId(),
		// TaxonNodeFactory.nodeToDetailedXML(failure), new AsyncCallback() {
		// public void onSuccess(Object arg0) {}
		// public void onFailure(Throwable arg0) {}
		// });
		//				
		// SysDebugger.getInstance().println("Saved node with correct parent ID: "
		// + failure.getParentId() );
		// }
		// catch (Exception e)
		// {
		// SysDebugger.getInstance().println(
		// TaxonNodeFactory.nodeToDetailedXML( failure ) );
		// SysDebugger.getInstance().println("Failed to add this guy AGAIN.");
		// }
		// }

		TaxonomyDocUtils.toggleWriteBack(true);
		TaxonomyDocUtils.doWriteBack(0);
	}

	private void validateParentNames() {
		int level = 0;
		int numTaxa = 0;

		// Start above Kingdoms ... we don't care about them.
		for (int j = 1; j < levels.size(); j++) {
			ConcurrentHashMap<Long, TaxonNode> curMap = levels.get(j);

			for (Entry<Long, TaxonNode> curEntry : curMap.entrySet()) {
				Long curID = curEntry.getKey();
				TaxonNode info = curEntry.getValue();
				TaxonNode parent = levels.get(info.getLevel() + 1).get(Long.valueOf(info.getParentId()));

				if (!info.getParentName().equals(parent.getName())) {
					System.out.println("Taxon " + info.getId() + " has mismatching parent name: "
							+ info.getParentName() + " != " + parent.getName());
				}

				if (!Arrays.deepEquals(Arrays16Emulation.copyOfRange(info.getFootprint(), 0,
						info.getFootprint().length - 1), parent.getFootprint())) {
					System.out.println("Taxon " + info.getId() + " has mismatching footprint with parent: "
							+ info.getFootprint() + " != " + parent.getFootprint());
				}

				numTaxa++;
			}
			SysDebugger.getInstance().println("Through level " + level);
			SysDebugger.getInstance().println("Through " + numTaxa + " taxa.");
			level++;
		}
	}

	private void verifyParentName(TaxonNode node) {
		int parentLevel = node.getLevel() >= TaxonNode.SUBPOPULATION ? node.getLevel() - 2 : node.getLevel() - 1;

		if (parentLevel != -1) {
			String parentName = "";

			if (parentLevel > TaxonNode.GENUS) {
				for (int i = 5; i <= parentLevel; i++)
					parentName += node.getFootprint()[i] + " ";
			} else
				parentName = node.getFootprint()[parentLevel];

			node.setParentName(parentName);
		}
	}

	private void verifyParentNameByID(TaxonNode node) {
		if (node.getParentId() == null || node.getParentId().equals("")) {
			System.out.println("This must be a kingdom: " + node.getName());
			return;
		}

		TaxonNode parent = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths.getURLForTaxa(node
				.getParentId()), vfs), null, false);

		boolean writeback = false;

		if (!parent.getFullName().equals(node.getParentName())) {
			System.out.println("Parent name doesn't match for taxon " + node.getId() + "Current: "
					+ node.getParentName() + " - TaxonomyDoc: " + parent.getFullName());
			node.setParentName(parent.getFullName());
			writeback = true;
		}

		if (parent.getLevel() != TaxonNode.KINGDOM && !parent.getFootprint()[0].equals(node.getFootprint()[0])) {
			// I'm in the wrong kingdom! Go find my real family!!
			int len = node.getFootprint().length;
			String pFullName = "";
			for (int i = TaxonNode.GENUS; i < (len >= TaxonNode.SUBPOPULATION ? len - 1 : len); i++)
				pFullName += node.getFootprint()[i] + " ";

			String realParentID = TaxonomyDocUtils.getIDByName(node.getFootprint()[0], pFullName);
			System.out.println("I'm in the wrong Kingdom: " + node.getFullName() + ":" + node.getId()
					+ " and parent should be " + pFullName + ":" + realParentID);

			node.setParentId(realParentID);
			writeback = true;
		} else {
			// My footprint may be a bit off...
			String[] shouldBe = new String[node.getFootprint().length];

			for (int i = 0; i < parent.getFootprint().length; i++)
				shouldBe[i] = parent.getFootprint()[i];

			shouldBe[shouldBe.length - 1] = parent.getName();

			if (!Arrays.deepEquals(shouldBe, node.getFootprint())) {
				System.out.println("Taxon " + node.getId() + " has footprint " + Arrays.toString(node.getFootprint())
						+ " vs. " + Arrays.toString(shouldBe));
				node.setFootprint(shouldBe);
				writeback = true;
			}
		}

		if (writeback)
			TaxaIO.writeNode(node, vfs, false);
//			DocumentUtils.writeVFSFile(ServerPaths.getURLForTaxa(node.getId() + ""), vfs, true, TaxonNodeFactory
//					.nodeToDetailedXML(node));
	}
}
