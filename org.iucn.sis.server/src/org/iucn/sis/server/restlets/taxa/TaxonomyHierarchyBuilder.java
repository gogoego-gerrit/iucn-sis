package org.iucn.sis.server.restlets.taxa;


public class TaxonomyHierarchyBuilder {

//	private ArrayList<ConcurrentHashMap<Long, Taxon>> levels;
//	private ConcurrentHashMap<Long, Taxon> kingdoms;
//	private ConcurrentHashMap<Long, Taxon> phylums;
//	private ConcurrentHashMap<Long, Taxon> classes;
//	private ConcurrentHashMap<Long, Taxon> orders;
//	private ConcurrentHashMap<Long, Taxon> family;
//	private ConcurrentHashMap<Long, Taxon> genus;
//	private ConcurrentHashMap<Long, Taxon> species;
//	private ConcurrentHashMap<Long, Taxon> infraranks;
//	private ConcurrentHashMap<Long, Taxon> subpopulations;
//	private ConcurrentHashMap<Long, Taxon> infrasubpopulations;
//
//	private VFS vfs;
//
//	private ArrayList<Taxon> failures;
//
//	private final int NUMBER_OF_THREADS;
//
//	/**
//	 * Instantiate a new one for each use.
//	 * 
//	 * @param vfs
//	 */
//	public TaxonomyHierarchyBuilder(VFS vfs) {
//		this(vfs, 40);
//	}
//
//	public TaxonomyHierarchyBuilder(VFS vfs, int numberOfThreads) {
//		this.vfs = vfs;
//
//		this.NUMBER_OF_THREADS = numberOfThreads;
//
//		kingdoms = new ConcurrentHashMap<Long, Taxon>();
//		phylums = new ConcurrentHashMap<Long, Taxon>();
//		classes = new ConcurrentHashMap<Long, Taxon>();
//		orders = new ConcurrentHashMap<Long, Taxon>();
//		family = new ConcurrentHashMap<Long, Taxon>();
//		genus = new ConcurrentHashMap<Long, Taxon>();
//		species = new ConcurrentHashMap<Long, Taxon>();
//		infraranks = new ConcurrentHashMap<Long, Taxon>();
//		subpopulations = new ConcurrentHashMap<Long, Taxon>();
//		infrasubpopulations = new ConcurrentHashMap<Long, Taxon>();
//
//		levels = new ArrayList<ConcurrentHashMap<Long, Taxon>>();
//		levels.add(0, kingdoms);
//		levels.add(1, phylums);
//		levels.add(2, classes);
//		levels.add(3, orders);
//		levels.add(4, family);
//		levels.add(5, genus);
//		levels.add(6, species);
//		levels.add(7, infraranks);
//		levels.add(8, subpopulations);
//		levels.add(9, infrasubpopulations);
//	}
//
//	public void buildDocuments(ArrayList<Taxon> nodes, Map<Long, String> idToOldFullname, boolean failOnDupeFound)
//			throws NotFoundException, IOException {
//		clearLevel();
//		for (Taxon cur : nodes) {
//			levels.get(cur.getLevel()).put(new Long(cur.getId()), cur);
//		}
//		updateTaxonomyDoc(idToOldFullname, true, failOnDupeFound);
//
//	}
//
//	public void buildDocuments(VFSPathToken[] uris, boolean removeFirst) throws NotFoundException, IOException,
//			VFSPathParseException {
//		Thread[] threads = new Thread[NUMBER_OF_THREADS];
//
//		if (uris == null)
//			uris = vfs.list(VFSUtils.parseVFSPath("/browse/nodes"));
//
//		readInNodes(uris, threads);
//		updateTaxonomyDoc(null, removeFirst, false);
//	}
//
//	private void clearLevel() {
//		for (ConcurrentHashMap<Long, Taxon> a : levels) {
//			a.clear();
//		}
//	}
//
//	private void processFile(VFSPath URI) throws NotFoundException, IOException {
//		if (!URI.toString().endsWith(".xml")) {
//			SysDebugger.getNamedInstance("info").println(
//					"***** NOTE: URL does not represent " + "a valid XML file. The extension is wrong: " + URI);
//			return;
//		}
//
//		String file = DocumentUtils.getVFSFileAsString(URI.toString(), vfs);
//		NativeDocument doc = SIS.newNativeDocument(null);
//
//		try {
//			doc.parse(file);
//			Taxon cur = TaxonFactory.createNode(doc);
//
//			levels.get(cur.getLevel()).put(Long.valueOf(cur.getId()), cur);
//		} catch (Throwable e) {
//			SysDebugger.getNamedInstance("info").println("***** ERROR PARSING: \n" + file);
//		}
//	}
//
//	private void processURI(VFSPath URI) throws NotFoundException, IOException {
//		VFSPathToken[] uris = vfs.list(URI);
//
//		for (VFSPathToken curFile : uris) {
//			if (vfs.isCollection(URI.child(curFile)))
//				processURI(URI.child(curFile));
//			else
//				processFile(URI.child(curFile));
//		}
//	}
//
//	private void readInNodes(VFSPathToken[] uris, Thread[] threads) {
//		int base = uris.length / NUMBER_OF_THREADS;
//		int start = 0;
//		int end = 0;
//
//		for (int i = 0; i < threads.length; i++) {
//			start = i * base;
//
//			if (i == threads.length - 1)
//				end = uris.length - 1;
//			else
//				end = (i * base) + base - 1;
//
//			threads[i] = new Thread(new FileProcessor(uris, start, end));
//
//			System.out.println("Thread " + i + " processing " + start + ":" + end);
//			threads[i].start();
//		}
//
//		boolean interrupted = true;
//		while (interrupted) {
//			try {
//				interrupted = false;
//				for (int i = 0; i < threads.length; i++)
//					threads[i].join();
//			} catch (Exception e) {
//				e.printStackTrace();
//				interrupted = true;
//			}
//		}
//	}
//
//	private void updateTaxonomyDoc(Map<Long, String> idToOldFullname, boolean removeFirst, boolean failOnDupeFound) {
//		TaxonomyDocUtils.toggleWriteBack(false);
//		failures = new ArrayList<Taxon>();
//
//		int level = 0;
//		int numTaxa = 0;
//		for (int j = 0; j < levels.size(); j++) {
//			ConcurrentHashMap<Long, Taxon> curMap = levels.get(j);
//
//			for (Entry<Long, Taxon> curEntry : curMap.entrySet()) {
//				Long curID = curEntry.getKey();
//				Taxon info = curMap.get(curID);
//
//				try {
//					verifyParentName(info);
//					// verifyParentNameByID( info );
//				} catch (Exception e) {
//					e.printStackTrace();
//					System.out.println("Error validating parent name for: " + info.getFullName());
//				}
//
//				if (removeFirst) {
//					try {
//						if (idToOldFullname != null && idToOldFullname.containsKey(curID))
//							TaxonomyDocUtils.removeTaxonFromHierarchy(curID.longValue(), info.getFootprint()[0],
//									idToOldFullname.get(curID));
//						else
//							TaxonomyDocUtils.removeTaxonFromHierarchy(curID.longValue(), info.getFootprint()[0], info
//									.getFullName());
//					} catch (Exception e) {
//						e.printStackTrace();
//						System.out.println("Failed to remove from hierarchy: " + info.getFullName());
//					}
//				}
//
//				try {
//					if (info.getFootprint().length != 0) {
//						String dupeID = TaxonomyDocUtils.getIDByName(info.getFootprint()[0], info.getFullName());
//						if (dupeID != null)
//							System.out.println("Found myself... " + info.getFullName() + ". Old/New IDs: " + dupeID
//									+ "/" + info.getId());
//
//						if (dupeID != null && failOnDupeFound)
//							throw new Exception("Found a dupe " + info.getId());
//					}
//
//					try {
//						TaxonomyDocUtils.addTaxonToHierarchy(curID.longValue(), info.getName(), info.getFullName(),
//								info.getParentId(), info.getParentName(), vfs, info.getFootprint().length == 0 ? ""
//										: info.getFootprint()[0]);
//					} catch (Exception e) {
//						System.out.println("Failed to add: " + info.getFullName() + ":" + info.getId() + " (curID="
//								+ curID + ") to parent " + info.getParentName());
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//					failures.add(info);
//					System.out.println(TaxonFactory.nodeToDetailedXML(info));
//					System.out.println("Failed to add this guy.");
//				}
//
//				numTaxa++;
//			}
//			System.out.println("Through level " + level);
//			System.out.println("Through " + numTaxa + " taxa.");
//			level++;
//		}
//
//		// for( Taxon failure : failures )
//		// {
//		// try
//		// {
//		// //Try resetting the parent's ID
//		// failure.setParentId( TaxonomyDocUtils.getIDByName(
//		// failure.getFootprint()[0], failure.getParentName() ) );
//		//				
//		// TaxonomyDocUtils.addTaxonToHierarchy(failure.getId(),
//		// failure.getName(),
//		// failure.getFullName(), failure.getParentId(),
//		// failure.getParentName(), vfs);
//		//				
//		// NativeDocument ndoc = ServerApplication.newNativeDocument(request);
//		// ndoc.put("/browse/taxonomy/" + failure.getId(),
//		// TaxonFactory.nodeToDetailedXML(failure), new AsyncCallback() {
//		// public void onSuccess(Object arg0) {}
//		// public void onFailure(Throwable arg0) {}
//		// });
//		//				
//		// System.out.println("Saved node with correct parent ID: "
//		// + failure.getParentId() );
//		// }
//		// catch (Exception e)
//		// {
//		// System.out.println(
//		// TaxonFactory.nodeToDetailedXML( failure ) );
//		// System.out.println("Failed to add this guy AGAIN.");
//		// }
//		// }
//
//		TaxonomyDocUtils.toggleWriteBack(true);
//		TaxonomyDocUtils.doWriteBack(0);
//	}
//
//	private void validateParentNames() {
//		int level = 0;
//		int numTaxa = 0;
//
//		// Start above Kingdoms ... we don't care about them.
//		for (int j = 1; j < levels.size(); j++) {
//			ConcurrentHashMap<Long, Taxon> curMap = levels.get(j);
//
//			for (Entry<Long, Taxon> curEntry : curMap.entrySet()) {
//				Long curID = curEntry.getKey();
//				Taxon info = curEntry.getValue();
//				Taxon parent = levels.get(info.getLevel() + 1).get(Long.valueOf(info.getParentId()));
//
//				if (!info.getParentName().equals(parent.getName())) {
//					System.out.println("Taxon " + info.getId() + " has mismatching parent name: "
//							+ info.getParentName() + " != " + parent.getName());
//				}
//
//				if (!Arrays.deepEquals(Arrays16Emulation.copyOfRange(info.getFootprint(), 0,
//						info.getFootprint().length - 1), parent.getFootprint())) {
//					System.out.println("Taxon " + info.getId() + " has mismatching footprint with parent: "
//							+ info.getFootprint() + " != " + parent.getFootprint());
//				}
//
//				numTaxa++;
//			}
//			System.out.println("Through level " + level);
//			System.out.println("Through " + numTaxa + " taxa.");
//			level++;
//		}
//	}
//
//	private void verifyParentName(Taxon node) {
//		int parentLevel = node.getLevel() >= Taxon.SUBPOPULATION ? node.getLevel() - 2 : node.getLevel() - 1;
//
//		if (parentLevel != -1) {
//			String parentName = "";
//
//			if (parentLevel > Taxon.GENUS) {
//				for (int i = 5; i <= parentLevel; i++)
//					parentName += node.getFootprint()[i] + " ";
//			} else
//				parentName = node.getFootprint()[parentLevel];
//
//			node.setParentName(parentName);
//		}
//	}
//
//	private void verifyParentNameByID(Taxon node) {
//		if (node.getParentId() == null || node.getParentId().equals("")) {
//			System.out.println("This must be a kingdom: " + node.getName());
//			return;
//		}
//
//		Taxon parent = TaxonFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths.getURLForTaxa(node
//				.getParentId()), vfs), null, false);
//
//		boolean writeback = false;
//
//		if (!parent.getFullName().equals(node.getParentName())) {
//			System.out.println("Parent name doesn't match for taxon " + node.getId() + "Current: "
//					+ node.getParentName() + " - TaxonomyDoc: " + parent.getFullName());
//			node.setParentName(parent.getFullName());
//			writeback = true;
//		}
//
//		if (parent.getLevel() != Taxon.KINGDOM && !parent.getFootprint()[0].equals(node.getFootprint()[0])) {
//			// I'm in the wrong kingdom! Go find my real family!!
//			int len = node.getFootprint().length;
//			String pFullName = "";
//			for (int i = Taxon.GENUS; i < (len >= Taxon.SUBPOPULATION ? len - 1 : len); i++)
//				pFullName += node.getFootprint()[i] + " ";
//
//			String realParentID = TaxonomyDocUtils.getIDByName(node.getFootprint()[0], pFullName);
//			System.out.println("I'm in the wrong Kingdom: " + node.getFullName() + ":" + node.getId()
//					+ " and parent should be " + pFullName + ":" + realParentID);
//
//			node.setParentId(realParentID);
//			writeback = true;
//		} else {
//			// My footprint may be a bit off...
//			String[] shouldBe = new String[node.getFootprint().length];
//
//			for (int i = 0; i < parent.getFootprint().length; i++)
//				shouldBe[i] = parent.getFootprint()[i];
//
//			shouldBe[shouldBe.length - 1] = parent.getName();
//
//			if (!Arrays.deepEquals(shouldBe, node.getFootprint())) {
//				System.out.println("Taxon " + node.getId() + " has footprint " + Arrays.toString(node.getFootprint())
//						+ " vs. " + Arrays.toString(shouldBe));
//				node.setFootprint(shouldBe);
//				writeback = true;
//			}
//		}
//
//		if (writeback)
//			SIS.get().getTaxonIO().writeNode(node, vfs, false);
////			DocumentUtils.writeVFSFile(ServerPaths.getURLForTaxa(node.getId() + ""), vfs, true, TaxonFactory
////					.nodeToDetailedXML(node));
//	}
}
