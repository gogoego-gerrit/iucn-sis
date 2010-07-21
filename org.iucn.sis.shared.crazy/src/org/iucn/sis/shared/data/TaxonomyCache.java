package org.iucn.sis.shared.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.LongUtils;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Node;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxonomyCache {

	public static final TaxonomyCache impl = new TaxonomyCache();

	private TaxonNode currentNode = null;

	/**
	 * ArrayList<TaxonNode>
	 */
	private ArrayList<TaxonNode> recentlyAccessed;

	/**
	 * HashMap<String, TaxonNode>();
	 */
	private HashMap<String, TaxonNode> cache;

	/**
	 * HashMap<String, NativeDocument>();
	 */
	private HashMap<String, NativeDocument> pathCache;

	/**
	 * HashMap<String, ArrayList> - NodeID request, and list of callbacks
	 * waiting for its return
	 */
	private HashMap<String, ArrayList<GenericCallback<TaxonNode>>> requested;

	private TaxonomyCache() {
		cache = new HashMap<String, TaxonNode>();
		pathCache = new HashMap<String, NativeDocument>();
		recentlyAccessed = new ArrayList<TaxonNode>();
		requested = new HashMap<String, ArrayList<GenericCallback<TaxonNode>>>();
	}

	public void clear() {
		cache.clear();
		pathCache.clear();
	}

	public boolean containsNode(TaxonNode node) {
		return cache.containsValue(node);
	}

	public boolean containsNodeByID(String id) {
		return cache.containsKey(id);
	}

	public void createNewNode(final TaxonNode node, final GenericCallback<TaxonNode> wayback) {
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.putAsText("/taxomatic/new", TaxonNodeFactory.nodeToDetailedXML(node), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				if( doc.getStatusText().equals("423") )
					WindowUtils.errorAlert("Taxomatic In Use", "Sorry, but another " +
							"taxomatic operation is currently running. Please try " +
							"again later!");
				
				wayback.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				try {
					node.setId(LongUtils.safeParseLong(doc.getText()));
					putNode(node);
					setCurrentNode(node);
					invalidatePath(node.getParentId());
				} catch (Exception e) {
					invalidatePath(node.getParentId());
					setCurrentNode(getNode(node.getParentId()));
				}

				wayback.onSuccess(node);
			}
		});
	}

	private void doListFetch(final String ids, final String idsToFetch, final GenericCallback<String> wayBack) {
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.post("/browse/nodes/list", idsToFetch, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				wayBack.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				WindowUtils.loadingBox.updateText("Fetch successful. Processing...");
				processDocumentOfNodes(doc);
				WindowUtils.hideLoadingAlert();
				wayBack.onSuccess(ids);
			}
		});
	}

	public void doLogout() {
		cache.clear();
		pathCache.clear();
		recentlyAccessed.clear();

		currentNode = null;
	}

	/**
	 * Evicts the nodes from the cache, and then calls evictPaths();
	 * 
	 * @param csvIDs
	 */
	public void evict(String csvIDs) {
		String[] ids = csvIDs.split(",");
		for (int i = 0; i < ids.length; i++) {
			TaxonNode node = cache.remove(ids[i]);
			if (node != null) {
				recentlyAccessed.remove(node);
			}
		}
		evictPaths();
	}

	/**
	 * Evicts ALL taxonomic hierarchy paths from local cache.
	 * 
	 * @param csvIDs
	 */
	public void evictPaths() {
		pathCache.clear();
	}

	/**
	 * Returns taxon nodes of species or lower levels. Takes an xml document of
	 * the form: <br>
	 * (ids)<br>
	 * (id)1(/id)<br>
	 * (id)2(/id)<br>
	 * (/ids)<br>
	 * 
	 * 
	 * @param id
	 * @param wayback
	 *            - returns the ids of the lowest level taxa
	 */
	public void fetchAllLowestLevelNodes(final String ids, final GenericCallback<String> wayback) {
		if (ids != null) {

			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.post("/browse/lowestTaxa", ids, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					wayback.onFailure(caught);
				}

				public void onSuccess(String arg0) {
					if (!arg0.equals("204")) {

						NativeElement docElement = ndoc.getDocumentElement();
						if (docElement.getNodeName().equalsIgnoreCase("nodes")) {
							NativeNodeList nodes = docElement.getChildNodes();
							String ids = "";

							for (int i = 0; i < nodes.getLength(); i++) {
								if (nodes.item(i).getNodeType() == Node.TEXT_NODE)
									continue;

								TaxonNode newNode = TaxonNodeFactory.createNode(nodes.elementAt(i));
								putNode(newNode);
								SysDebugger.getInstance()
										.println(
												"this is the node information " + newNode.getFullName() + " "
														+ newNode.getId());
								ids += newNode.getId() + ",";
							}

							if (ids.length() > 0)
								ids = ids.substring(0, ids.length() - 1);
							wayback.onSuccess(ids);
						} else {
							TaxonNode newNode = TaxonNodeFactory.createNode(docElement);
							putNode(newNode);
							SysDebugger.getInstance().println(
									"this is the node information " + newNode.getFullName() + " " + newNode.getId());
							wayback.onSuccess(newNode.getId() + "");
						}
					} else
						wayback.onSuccess("");
				}

			});

		} else {
			wayback.onSuccess("");
		}
	}

	public void fetchList(final String ids, final GenericCallback<String> wayBack) {
		final StringBuilder idsToFetch = new StringBuilder("<ids>");
		final String[] list;
		int toFetch = 0;
		boolean needToFetch = false;

		if (ids.indexOf(",") == -1) {
			if (!cache.containsKey(ids))
				fetchNode(ids, false, new GenericCallback<TaxonNode>() {
					public void onFailure(Throwable caught) {
						wayBack.onFailure(caught);
					}

					public void onSuccess(TaxonNode result) {
						wayBack.onSuccess(result.getId() + "");
					}
				});
			else
				wayBack.onSuccess("OK");
		} else {
			list = ids.split(",");

			for (int i = 0; i < list.length; i++) {
				if (!cache.containsKey(list[i])) {
					needToFetch = true;

					if (!list[i].equals("")) {
						idsToFetch.append("<id>");
						idsToFetch.append(list[i]);
						idsToFetch.append("</id>");
						toFetch++;
					}
				}
			}
			idsToFetch.append("</ids>");

			if (!needToFetch) {
				wayBack.onSuccess("OK");
				return;
			}

			WindowUtils.showLoadingAlert("Fetching " + toFetch + " taxa from the server. Please wait.");
			doListFetch(ids, idsToFetch.toString(), wayBack);
		}
	}

	public void fetchNode(final String id, final boolean asCurrent, GenericCallback<TaxonNode> wayback) {
		if (getNode(id) != null) {
			if (asCurrent)
				setCurrentNode(cache.get(id));

			wayback.onSuccess(cache.get(id));
		} else {
			if (requested.containsKey(id))
				requested.get(id).add(wayback);
			else {
				requested.put(id, new ArrayList<GenericCallback<TaxonNode>>());
				requested.get(id).add(wayback);

				final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
				ndoc.load("/browse/nodes/" + id, new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						invokeFailureCallbacks(id, caught);
					}

					public void onSuccess(String arg0) {
						TaxonNode defaultNode;

						// LOOKS LIKE I LOST TO A RACE CONDITION ... JUST
						// RETURNING THE NODE
						if (cache.containsKey(id)) {
							defaultNode = cache.get(id);
						} else {
							defaultNode = TaxonNodeFactory.createNode(ndoc);
							putNode(defaultNode);
						}

						if (asCurrent)
							setCurrentNode(defaultNode);

						invokeCallbacks(id, defaultNode);
					}
				});
			}
		}
	}

	/**
	 * 
	 * @param kingdom
	 *            - the kingdom of the node
	 * @param otherInfo
	 *            - the other information that you know, either phylum, class,
	 *            order ...
	 * @param asCurrent
	 * @param wayback
	 */
	public void fetchNodeWithKingdom(String kingdom, String otherInfo, final boolean asCurrent,
			final GenericCallback<TaxonNode> wayback) {
		if (!kingdom.trim().equals("") && !otherInfo.trim().equals("")) {
			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.load(URL.encode("/browse/taxonName/" + kingdom + "/" + otherInfo), new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					wayback.onFailure(caught);
				}

				public void onSuccess(String arg0) {
					TaxonNode defaultNode = TaxonNodeFactory.createNode(ndoc);
					if (!cache.containsKey(defaultNode.getId() + "")) {
						putNode(defaultNode);
						if (asCurrent)
							setCurrentNode(defaultNode);
					} else if (asCurrent) {
						setCurrentNode(cache.get(defaultNode.getId() + ""));
					}
					wayback.onSuccess(cache.get(defaultNode.getId() + ""));

				}
			});

		} else {
			wayback.onFailure(new Throwable("Nothing to fetch"));
		}
	}

	public void fetchPath(final String path, final GenericCallback<NativeDocument> wayback) {
		if (pathCache.containsKey(path)) {
			wayback.onSuccess(pathCache.get(path));
		} else {
			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.load("/browse/taxonomy/" + path, new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					wayback.onFailure(caught);
				}

				public void onSuccess(String result) {
					pathCache.put(path, ndoc);
					wayback.onSuccess(ndoc);
				}
			});
		}
	}

	public void fetchPathWithID(final String id, final GenericCallback<NativeDocument> wayback) {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.get("/browse/hierarchy/" + id, new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(String result) {
				try {
					String path = ndoc.getDocumentElement().getElementByTagName("footprint").getText();
					pathCache.put(path, ndoc);
					wayback.onSuccess(ndoc);
				} catch (Exception e) {
					wayback.onFailure(new Throwable());
				}

			}

		});
	}

	/**
	 * Gets all immediate children of the node referred to by nodeID, processes
	 * them and caches them locally, then returns as an argument in the
	 * GenericCallback<String>an ArrayList(TaxonNode) that contains the child
	 * nodes themselves.
	 * 
	 * @param nodeID
	 * @param wayback
	 */
	public void getChildrenOfNode(final String nodeID, final GenericCallback<List<TaxonNode>> wayback) {
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
		doc.get("/browse/children/" + nodeID, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				if (doc.getPeer() == null) {
					wayback.onSuccess(new ArrayList<TaxonNode>());
				} else
					wayback.onSuccess(processDocumentOfNodes(doc));
			}
		});
	}

	public TaxonNode getCurrentNode() {
		return currentNode;
	}

	public TaxonNode getNode(long id) {
		return getNode("" + id);
	}

	public TaxonNode getNode(String id) {
		return cache.get(id);
	}

	public ArrayList<TaxonNode> getRecentlyAccessed() {
		return recentlyAccessed;
	}

	public Object invalidatePath(String pathID) {
		return pathCache.remove(pathID);
	}

	private void invokeCallbacks(String id, TaxonNode defaultNode) {
		for (GenericCallback<TaxonNode> curCallback : requested.get(id))
			curCallback.onSuccess(defaultNode);

		requested.remove(id);
	}

	private void invokeFailureCallbacks(String id, Throwable caught) {
		for (GenericCallback<TaxonNode> curCallback : requested.get(id))
			curCallback.onFailure(caught);

		requested.remove(id);
	}

	private ArrayList<TaxonNode> processDocumentOfNodes(final NativeDocument doc) {
		ArrayList<TaxonNode> taxaList = new ArrayList<TaxonNode>();
		SysDebugger.getInstance().println("This is documentelement name " + doc.getDocumentElement().getNodeName());

		if (doc.getDocumentElement().getNodeName().equalsIgnoreCase("nodes")) {
			NativeNodeList nodes = doc.getDocumentElement().getChildNodes();
			int numNodes = nodes.getLength();

			for (int i = 0; i < numNodes; i++) {
				if (nodes.item(i).getNodeType() == Node.TEXT_NODE)
					continue;

				try {
					TaxonNode newNode = TaxonNodeFactory.createNode(nodes.elementAt(i));
					putNode(newNode);
					taxaList.add(newNode);
				} catch (Exception e) {
					SysDebugger.getInstance().println("Node is named " + nodes.elementAt(i).getNodeName());
					e.printStackTrace();
				}
			}
		} else if (doc.getDocumentElement().getNodeName().equalsIgnoreCase("parsererror")) {
			Window.alert("Error -- Someone probably modified an xml file by hand, and SIS "
					+ "is unable to parse the file.");
		} else {
			try {
				TaxonNode newNode = TaxonNodeFactory.createNode(doc.getDocumentElement());
				putNode(newNode);
				taxaList.add(newNode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return taxaList;
	}

	public void putNode(TaxonNode node) {
		cache.put(new Long(node.getId()).toString(), node);
	}

	public void resetCurrentNode() {
		setCurrentNode(null);
	}

	public void setCurrentNode(TaxonNode newCurrent) {
		currentNode = newCurrent;

		try {

			if (currentNode != null) {
				recentlyAccessed.remove(currentNode);
				recentlyAccessed.add(0, currentNode);

				ClientUIContainer.bodyContainer.tabManager.taxonHomePage.setAppropriateRights(TaxonomyCache.impl
						.getCurrentNode());
			}

			ClientUIContainer.headerContainer.taxonChanged();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
