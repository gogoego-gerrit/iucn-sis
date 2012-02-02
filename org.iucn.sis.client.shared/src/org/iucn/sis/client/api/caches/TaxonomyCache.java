package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonHierarchy;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.models.interfaces.HasNotes;
import org.iucn.sis.shared.api.models.interfaces.HasReferences;

import com.google.gwt.http.client.URL;
import com.google.gwt.xml.client.Node;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class TaxonomyCache {

	public static final TaxonomyCache impl = new TaxonomyCache();

	private final Set<Integer> recentlyAccessed;
	private final HashMap<Integer, Taxon> cache;
	private final HashMap<String, CacheBackedTaxonHierarchy> pathCache;

	/**
	 * HashMap<String, ArrayList> - NodeID request, and list of callbacks
	 * waiting for its return
	 */
	private HashMap<Integer, List<GenericCallback<Taxon>>> requested;

	private TaxonomyCache() {
		cache = new HashMap<Integer, Taxon>();
		pathCache = new HashMap<String, CacheBackedTaxonHierarchy>();
		recentlyAccessed = new LinkedHashSet<Integer>();
		requested = new HashMap<Integer, List<GenericCallback<Taxon>>>();
	}

	public void clear() {
		cache.clear();
		pathCache.clear();
	}

	public boolean containsNode(Taxon node) {
		return contains(node.getId());
	}

	public boolean contains(int id) {
		return cache.containsKey(Integer.valueOf(id));
	}

	public boolean contains(Integer id) {
		return cache.containsKey(id);
	}

	private void doListFetch(final String payload, final GenericCallback<String> wayBack) {
		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		doc.post(UriBase.getInstance().getSISBase() + "/browse/nodes/list", payload, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.hideLoadingAlert();
				wayBack.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				try{
				WindowUtils.loadingBox.updateText("Fetch successful. Processing...");
				List<Taxon> taxa = processDocumentOfTaxa(doc);
				WindowUtils.hideLoadingAlert();
				wayBack.onSuccess(Arrays.toString(taxa.toArray()));
				} catch (Throwable e) {
					Debug.println(e);
				}
			}
		});
	}

	public void doLogout() {
		cache.clear();
		pathCache.clear();
		recentlyAccessed.clear();
	}
	
	public void evictNode(Integer id) {
		if (id == null)
			return;
		
		evictNode(id.toString());
	}
	
	public void evictNode(String id) {
		evict(id);
	}
	
	public void evictNodes(String csv) {
		evict(csv);
	}

	/**
	 * Evicts the nodes from the cache, and then calls evictPaths();
	 * 
	 * @param csvIDs
	 */
	private void evict(String csvIDs) {
		if (csvIDs == null)
			return;
		
		String[] ids = csvIDs.split(",");
		for (String idStr : ids) {
			Integer idInt = Integer.valueOf(idStr);
			cache.remove(idInt);
			pathCache.remove(idStr);
			recentlyAccessed.remove(idInt);
		}
		//evictPaths();
	}
	
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
	public void fetchLowestLevelTaxa(final String ids, final GenericCallback<List<Taxon>> wayback) {
		if (ids == null) {
			wayback.onSuccess(new ArrayList<Taxon>());
			return;
		}
		
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.post(UriBase.getInstance().getSISBase() + "/browse/lowestTaxa", ids, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}
			public void onSuccess(String arg0) {
				if (!arg0.equals("204")) {
					List<Taxon> ids = new ArrayList<Taxon>();
					NativeElement docElement = ndoc.getDocumentElement();
					if (docElement.getNodeName().equalsIgnoreCase("nodes")) {
						NativeNodeList nodes = docElement.getChildNodes();
						for (int i = 0; i < nodes.getLength(); i++) {
							if (nodes.item(i).getNodeType() == Node.TEXT_NODE)
								continue;
						
							Taxon newNode = Taxon.fromXML(nodes.elementAt(i));
							putTaxon(newNode);
							ids.add(newNode);
						}
					} else {
						Taxon newNode = Taxon.fromXML(docElement);
						putTaxon(newNode);
						ids.add(newNode);
					}
					wayback.onSuccess(ids);
				} else {
					//FIXME: Should this be a failure?
					wayback.onSuccess(new ArrayList<Taxon>());
				}
			}
		});
	}
	

	public void fetchList(final List<Integer> ids, final GenericCallback<String> wayBack) {
		if (ids == null || ids.isEmpty()) {
			wayBack.onSuccess("OK");
			return;
		}
		
		final StringBuilder idsToFetch = new StringBuilder("<ids>");
		int toFetch = 0;
		boolean needToFetch = false;

		if (ids.size() == 1) {
			if (!contains(ids.get(0)))
				fetchTaxon(ids.get(0), new GenericCallback<Taxon>() {
					public void onFailure(Throwable caught) {
						wayBack.onFailure(caught);
					}

					public void onSuccess(Taxon result) {
						wayBack.onSuccess(result.getId() + "");
					}
				});
			else
				wayBack.onSuccess("OK");
		} else {
			for (Integer curID : ids) {
				if (!contains(curID)) {
					needToFetch = true;

					idsToFetch.append("<id>");
					idsToFetch.append(curID);
					idsToFetch.append("</id>");
					toFetch++;
				}
			}
			idsToFetch.append("</ids>");

			if (!needToFetch) {
				wayBack.onSuccess("OK");
				return;
			}
			
			WindowUtils.showLoadingAlert("Fetching " + toFetch + " taxa from the server. Please wait.");
			doListFetch(idsToFetch.toString(), wayBack);
		}
	}

	public void fetchTaxon(final Integer id, GenericCallback<Taxon> wayback) {
		fetchTaxon(id, true, wayback);
	}
	
	public void fetchTaxon(final Integer id, final boolean saveIfNecessary, final GenericCallback<Taxon> wayback) {
		if (getTaxon(id) != null) {
			/*if (asCurrent)
				setCurrentTaxon(getTaxon(id), saveIfNecessary);*/

			wayback.onSuccess(getTaxon(id));
		} else {
			if (requested.containsKey(id))
				requested.get(id).add(wayback);
			else {
				requested.put(id, new ArrayList<GenericCallback<Taxon>>());
				requested.get(id).add(wayback);

				final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
				ndoc.load(UriBase.getInstance().getSISBase() + "/browse/nodes/" + id, new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						invokeFailureCallbacks(id, caught);
					}

					public void onSuccess(String arg0) {
						Taxon defaultNode;

						// ___ FIXME ___ LOOKS LIKE I LOST TO A RACE CONDITION ... JUST
						// RETURNING THE NODE
						if (contains(id)) {
							defaultNode = getTaxon(id);
						} else {
							List<Taxon> list = processDocumentOfTaxa(ndoc);
							defaultNode = list.get(0);
							putTaxon(defaultNode);
						}

						/*if (asCurrent) {
							setCurrentTaxon(defaultNode, saveIfNecessary);
						}*/

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
	public void fetchTaxonWithKingdom(String kingdom, String otherInfo,
			final GenericCallback<Taxon> wayback) {
		if (!kingdom.trim().equals("") && !otherInfo.trim().equals("")) {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.get(URL.encode(UriBase.getInstance().getSISBase() + "/browse/taxonName/" + kingdom + "/" + otherInfo),
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					wayback.onFailure(caught);
				}
				public void onSuccess(String arg0) {
					Taxon defaultNode = Taxon.fromXML(ndoc.getDocumentElement().getElementByTagName(Taxon.ROOT_TAG));
					if (!contains(defaultNode.getId())) {
						putTaxon(defaultNode);
						/*if (asCurrent)
							setCurrentTaxon(defaultNode);*/
					} /*else if (asCurrent) {
						setCurrentTaxon(getTaxon(defaultNode.getId()));
					}*/
					wayback.onSuccess(getTaxon(defaultNode.getId()));
				}
			});
		} else {
			wayback.onFailure(new Throwable("Nothing to fetch"));
		}
	}
	
	public void fetchChildren(final Taxon node, final GenericCallback<List<TaxonListElement>> wayback) {
		TaxonomyCache.impl.fetchPath(String.valueOf(node.getId()), new GenericCallback<TaxonHierarchy>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}
			public void onSuccess(final TaxonHierarchy result) {
				if (result.hasChildren()) {
					final ArrayList<TaxonListElement> childModel = new ArrayList<TaxonListElement>();
					for (Taxon taxon : result.getChildren())
						childModel.add(new TaxonListElement(taxon, ""));

					wayback.onSuccess(childModel);
					/*
					TaxonomyCache.impl.fetchList(result.getChildren(), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							wayback.onFailure(caught);
						}
						public void onSuccess(String useless) {
							final ArrayList<TaxonListElement> childModel = new ArrayList<TaxonListElement>();
							for (Integer id : result.getChildren())
								childModel.add(new TaxonListElement(getTaxon(id), ""));

							wayback.onSuccess(childModel);
						}
					});*/
				} else {
					wayback.onFailure(new Throwable());
				}
			}
		});
	}

	public void fetchPath(final String path, final GenericCallback<TaxonHierarchy> wayback) {
		if (containsPath(path)) {
			wayback.onSuccess(getHierarchy(path));
		} else {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.load(UriBase.getInstance().getSISBase() + "/browse/taxonomy/" + path, 
					getPathFetchCallback(ndoc, wayback));
		}
	}

	public void fetchPathWithID(final Integer id, final GenericCallback<TaxonHierarchy> wayback) {
		fetchPathWithID(id.toString(), wayback);
	}
	
	public void fetchPathWithID(final String id, final GenericCallback<TaxonHierarchy> wayback) {
		if (containsPath(id))
			wayback.onSuccess(getHierarchy(id));
		else {
			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
			ndoc.get(UriBase.getInstance().getSISBase() + "/browse/hierarchy/" + id, 
					getPathFetchCallback(ndoc, wayback));
		}
	}
	
	private GenericCallback<String> getPathFetchCallback(final NativeDocument document, final GenericCallback<TaxonHierarchy> wayback) {
		return new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}
			public void onSuccess(String result) {
				try {
					TaxonHierarchy hierarchy = TaxonHierarchy.fromXML(document);
					if (hierarchy.getTaxon() != null) {
						Integer id = hierarchy.getTaxon().getId();
						if (!contains(id))
							putTaxon(hierarchy.getTaxon());
						
						hierarchy.setTaxon(getTaxon(id));
					}
					CacheBackedTaxonHierarchy cached = new CacheBackedTaxonHierarchy(hierarchy);
					putHierarchy(cached);
					wayback.onSuccess(cached);
				} catch (Exception e) {
					wayback.onFailure(e);
				}
			}
		};
	}

	/**
	 * Gets all immediate children of the node referred to by nodeID, processes
	 * them and caches them locally, then returns as an argument in the
	 * GenericCallback<String>an ArrayList(Taxon) that contains the child nodes
	 * themselves.
	 * 
	 * @param nodeID
	 * @param wayback
	 */
	public void getTaxonChildren(final String nodeID, final GenericCallback<List<Taxon>> wayback) {
		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		doc.get(UriBase.getInstance().getSISBase() + "/browse/children/" + nodeID, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				if (doc.getPeer() == null) {
					wayback.onSuccess(new ArrayList<Taxon>());
				} else
					wayback.onSuccess(processDocumentOfTaxa(doc));
			}
		});
	}

	public Taxon getCurrentTaxon() {
		return StateManager.impl.getTaxon();
	}

	public Taxon getTaxon(String id) {
		return cache.get(Integer.valueOf(id));
	}

	public Taxon getTaxon(int id) {
		return cache.get(Integer.valueOf(id));
	}

	public Taxon getTaxon(Integer id) {
		return cache.get(id);
	}

	public Set<Taxon> getRecentlyAccessed() {
		Set<Taxon> set = new LinkedHashSet<Taxon>();
		for (Integer id : recentlyAccessed) {
			Taxon taxon = getTaxon(id);
			if (taxon != null)
				set.add(taxon);
		}
		return set;
	}
	
	public void updateRecentTaxa() {
		//TODO: should this be persisted?
		Taxon taxon = getCurrentTaxon();
		if (taxon != null)
			recentlyAccessed.add(getCurrentTaxon().getId());
	}

	private void invokeCallbacks(Integer id, Taxon defaultNode) {
		for (GenericCallback<Taxon> curCallback : requested.get(id))
			curCallback.onSuccess(defaultNode);

		requested.remove(id);
	}

	private void invokeFailureCallbacks(Integer id, Throwable caught) {
		for (GenericCallback<Taxon> curCallback : requested.get(id))
			curCallback.onFailure(caught);

		requested.remove(id);
	}

	private List<Taxon> processDocumentOfTaxa(final NativeDocument doc) {
		List<Taxon> taxaList = new ArrayList<Taxon>();
		
		if (doc.getDocumentElement().getNodeName().trim().equalsIgnoreCase("nodes")) {
			final NativeNodeList nodes = doc.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				final NativeNode node = nodes.item(i);
				if (Taxon.ROOT_TAG.equals(node.getNodeName())) {
					Taxon newNode = Taxon.fromXML((NativeElement)node);
					putTaxon(newNode);
					taxaList.add(newNode);
				}
			}
			
		} else if (doc.getDocumentElement().getNodeName().equalsIgnoreCase("parsererror")) {
			WindowUtils.errorAlert("Unknown error parsing taxon data.");
		} else {
			try {
				Taxon newNode = Taxon.fromXML(doc.getDocumentElement());
				putTaxon(newNode);
				taxaList.add(newNode);
			} catch (Exception e) {
				Debug.println(e);
			}
		}

		return taxaList;
	}

	public void putTaxon(Taxon node) {
		cache.put(Integer.valueOf(node.getId()), node);
	}
	
	private String getPathAsKey(String path) {
		final String key;
		int index;
		if ((index = path.lastIndexOf('-')) == -1)
			key = path;
		else if (!path.endsWith("-"))
			key = path.substring(index+1);
		else
			key = null;
		
		return key;
	}
	
	private boolean containsPath(String path) {
		final String key = getPathAsKey(path);
		if (key == null)
			return false;
		
		return pathCache.containsKey(key);
	}
	
	private void putHierarchy(CacheBackedTaxonHierarchy hierarchy) {
		String key = "";
		if (hierarchy.getTaxon() != null)
			key = String.valueOf(hierarchy.getTaxon().getId());
		
		putHierarchy(key, hierarchy);
	}
	
	private void putHierarchy(String path, CacheBackedTaxonHierarchy hierarchy) {
		final String key = getPathAsKey(path);
		if (key == null)
			return;
		
		pathCache.put(key, hierarchy);
	}
	
	private TaxonHierarchy getHierarchy(String path) {
		final String key = getPathAsKey(path);
		if (key == null)
			return null;
		
		return pathCache.get(key);
	}
	
	public void saveReferences(Taxon taxon, final GenericCallback<String> callback) {
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		for (Reference ref : taxon.getReference())
			out.append("<reference id=\"" + ref.getId() + "\" />");
		out.append("</root>");
		
		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		doc.post(UriBase.getInstance().getSISBase() + "/browse/nodes/" + taxon.getId() + "/references", 
				out.toString(), callback);
	}

	public void saveTaxon(final Taxon taxon, final GenericCallback<String> callback) {
		/*
		 * TODO: do we need to check permissions?
		 */
		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		doc.put(UriBase.getInstance().getSISBase() + "/browse/nodes/" + taxon.getId(), taxon.toXML(),
				new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			public void onSuccess(String result) {
				Taxon returned = Taxon.fromXML(doc.getDocumentElement());
				if (returned.getTaxonomicNotes() != null) {
					if (taxon.getTaxonomicNotes() == null)
						taxon.setTaxonomicNotes(returned.getTaxonomicNotes());
					else {
						taxon.getTaxonomicNotes().setId(returned.getTaxonomicNotes().getId());
						AssessmentClientSaveUtils.sink(returned.getTaxonomicNotes(), taxon.getTaxonomicNotes());
					}
				}
				else
					taxon.setTaxonomicNotes(null);
				callback.onSuccess(result);
			}
		});
	}

	/**
	 * Saves the taxon and makes it the current taxon
	 * 
	 * @param node
	 * @param callback
	 */
	/*public void saveTaxonAndMakeCurrent(final Taxon node, final GenericCallback<String> callback) {
		saveTaxon(node, new GenericCallback<String>() {
			public void onSuccess(String result) {
				TaxonomyCache.impl.setCurrentTaxon(node);
				//TODO: refresh the view.
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}*/

	/*public void setCurrentTaxon(Taxon newCurrent) {
		setCurrentTaxon(newCurrent, true);
	}
	
	public void setCurrentTaxon(Taxon newCurrent, boolean saveIfNecessary) {
		setCurrentTaxon(newCurrent, saveIfNecessary, null);
	}
	
	public void setCurrentTaxon(final Taxon newCurrent, boolean saveIfNecessary, final SimpleListener afterChange) {
		SimpleListener callback = new SimpleListener() {
			public void handleEvent() {
				StateManager.impl.setTaxon(newCurrent);
				if (newCurrent != null) {
					recentlyAccessed.remove(newCurrent);
					recentlyAccessed.add(0, newCurrent);
					SISClientBase.getInstance().onTaxonChanged();
					if (afterChange != null)
						afterChange.handleEvent();
				}
				else {
					AssessmentCache.impl.resetCurrentAssessment();
					SISClientBase.getInstance().onTaxonChanged();
				}
			}
		};
		if (saveIfNecessary)
			AssessmentClientSaveUtils.saveIfNecessary(callback);
		else
			callback.handleEvent();
	}*/
	
	public void getTaggedTaxa(final String tag, final GenericCallback<List<Taxon>> callback) {
		final String url = UriBase.getInstance().getSISBase() + "/tagging/taxa/" + tag;
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(url, new GenericCallback<String>() {
			public void onSuccess(String result) {
				final List<Taxon> results = new ArrayList<Taxon>();
				final NativeNodeList nodes = document.
					getDocumentElement().getElementsByTagName("taxon");
				for (int i = 0; i < nodes.getLength(); i++) {
					final Taxon taxon = Taxon.fromXML(nodes.elementAt(i));
					if (!containsNode(taxon))
						putTaxon(taxon);
					
					results.add(getTaxon(taxon.getId()));
				}
				callback.onSuccess(results);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void tagTaxa(final String tag, final List<Taxon> taxa, final GenericCallback<Object> callback) {
		doTaxaMarking(tag, taxa, true, callback);
	}
	
	public void untagTaxa(final String tag, final List<Taxon> taxa, final GenericCallback<Object> callback) {
		doTaxaMarking(tag, taxa, false, callback);
	}
	
	private boolean isTagged(final String tag, final Taxon taxon) {
		if ("invasive".equals(tag))
			return taxon.getInvasive();
		else if ("feral".equals(tag))
			return taxon.getFeral();
		else
			return false;
	}
	
	private void doTaxaMarking(final String tag, final List<Taxon> taxa, final boolean isMarked, 
			final GenericCallback<Object> callback) {
		boolean found = false;
		
		final StringBuilder builder = new StringBuilder();
		builder.append("<root>");
		for (Taxon taxon : taxa) {
			if (taxon.getId() != 0 && isTagged(tag, taxon) != isMarked) {
				found = true;
				builder.append("<taxon id=\"" + taxon.getId() + "\" />");
			}
		}
		builder.append("</root>");
		
		if (!found)
			callback.onSuccess(null);
		else {
			final String url = UriBase.getInstance().getSISBase() + 
				"/tagging/taxa/" + tag + (isMarked ? "/marked" : "/unmarked");
			final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
			document.post(url, builder.toString(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					for (Taxon taxon : taxa) {
						if ("feral".equals(tag))
							taxon.setFeral(isMarked);
						else if ("invasive".equals(tag))
							taxon.setInvasive(isMarked);
					}
					
					callback.onSuccess(null);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
	}
	
	public void deleteSynonymn(final Taxon taxon, final Synonym synonym, final GenericCallback<String> callback) {
		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.delete(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/synonym/" + synonym.getId(), new GenericCallback<String>() {
		
			@Override
			public void onSuccess(String result) {
				Synonym toRemove = null;
				for (Synonym s : taxon.getSynonyms())
					if (s.getId() == synonym.getId()) {
						toRemove = s;
						break;
					}
				taxon.getSynonyms().remove(toRemove);
				callback.onSuccess(result);
			}
		
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void addOrEditSynonymn(final Taxon taxon, final Synonym synonym, final GenericCallback<String> callback) {
		String uri = UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/synonym";
		if (synonym.getId() != 0) {
			uri += "/" + synonym.getId();
		}
		
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		if (synonym.getId() != 0) {
			ndoc.postAsText(uri, synonym.toXML(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
		else {
			ndoc.putAsText(uri, synonym.toXML(), new GenericCallback<String>() {
				public void onSuccess(String result) {
					String newId = ndoc.getText();
					synonym.setId(Integer.parseInt(newId));
					taxon.getSynonyms().add(synonym);
					
					callback.onSuccess(result);
				}
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);	
				}
			});
		}
		
	}
	
	public void deleteCommonName(final Taxon taxon, final CommonName commonName, final GenericCallback<String> callback) {
		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.delete(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/commonname/" + commonName.getId(), new GenericCallback<String>() {
		
			@Override
			public void onSuccess(String result) {
				taxon.getCommonNames().remove(commonName);
				callback.onSuccess(result);
			}
		
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void deleteNoteOnSynonym(final Taxon taxon, final Synonym synonym, final Notes note, final GenericCallback<String> callback) {
		deleteNoteOnNotable(taxon, synonym, note, callback);
	}
	
	public void deleteNoteOnCommonNames(final Taxon taxon, final CommonName commonName, final Notes note, final GenericCallback<String> callback) {
		deleteNoteOnNotable(taxon, commonName, note, callback);
	}
	
	private void deleteNoteOnNotable(final Taxon taxon, final HasNotes hasNotes, final Notes note, final GenericCallback<String> callback) {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.delete(UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				hasNotes.getNotes().remove(note);
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void addNoteToCommonName(final Taxon taxon, final CommonName commonName, final Notes note, final GenericCallback<String> callback) {
		addNoteToNotable(taxon, commonName, "commonName", commonName.getId(), note, new GenericCallback<String>() {
			public void onSuccess(String result) {
				note.setCommonName(commonName);
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void addNoteToSynonym(final Taxon taxon, final Synonym synonym, final Notes note, final GenericCallback<String> callback) {
		addNoteToNotable(taxon, synonym, "synonym", synonym.getId(), note, new GenericCallback<String>() {
			public void onSuccess(String result) {
				note.setSynonym(synonym);
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	private void addNoteToNotable(final Taxon taxon, final HasNotes hasNotes, final String type, final int id, final Notes note, final GenericCallback<String> callback) {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.put(UriBase.getInstance().getNotesBase() + "/notes/"+type+"/" + id, note.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				Notes newNote = Notes.fromXML(ndoc.getDocumentElement());
				
				note.setId(newNote.getId());
				note.setEdits(newNote.getEdits());
				
				hasNotes.getNotes().add(note);
				callback.onSuccess(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		
		});
	}
	
	public void addReferencesToCommonName(final Taxon taxon, final CommonName commonName, final Collection<Reference> refs, final GenericCallback<Object> callback) {
		addReferencesToHasReferences(taxon, commonName, "commonname", commonName.getId(), refs, callback);
	}
	
	public void addReferencesToSynonym(final Taxon taxon, final Synonym synonym, final Collection<Reference> refs, final GenericCallback<Object> callback) {
		addReferencesToHasReferences(taxon, synonym, "synonym", synonym.getId(), refs, callback);
	}
	
	private void addReferencesToHasReferences(final Taxon taxon, final HasReferences hasReferences, final String type, final int id, final Collection<Reference> refs, final GenericCallback<Object> callback) {
		StringBuilder xml = new StringBuilder("<references>");
		for (Reference ref : refs)
			xml.append("<action id=\"" + ref.getId() + "\">add</action>");
		xml.append("</references>");
		
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.post(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/"+type+"/" + id + "/reference", 
				xml.toString(), new GenericCallback<String>() {
			@Override
			public void onSuccess(String result) {
				hasReferences.getReference().addAll(refs);
				callback.onSuccess(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void removeReferencesFromCommonName(final Taxon taxon, final CommonName commonName, final Collection<Reference> refs, final GenericCallback<Object> callback) {
		removeReferencesFromHasReferences(taxon, commonName, "commonname", commonName.getId(), refs, callback);
	}
	
	public void removeReferencesFromSynonym(final Taxon taxon, final Synonym synonym, final Collection<Reference> refs, final GenericCallback<Object> callback) {
		removeReferencesFromHasReferences(taxon, synonym, "synonym", synonym.getId(), refs, callback);
	}
	
	private void removeReferencesFromHasReferences(final Taxon taxon, final HasReferences hasReferences, final String type, final int id, final Collection<Reference> refs, final GenericCallback<Object> callback) {
		StringBuilder xml = new StringBuilder("<references>");
		for (Reference ref : refs)
			xml.append("<action id=\"" + ref.getId() + "\">remove</action>");
		xml.append("</references>");
		
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.post(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/"+type+"/" + id + "/reference", xml.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				hasReferences.getReference().removeAll(refs);
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void setPrimaryCommonName(final Taxon taxon, final CommonName commonName, final GenericCallback<String> callback) {
		final String uri = UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + 
			"/commonname/primary";
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(uri, commonName.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				for (CommonName current : taxon.getCommonNames())
					current.setPrincipal(current.getId() == commonName.getId());
				
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void addOrEditCommonName(final Taxon taxon, final CommonName commonName, final GenericCallback<String> callback) {
		if (commonName.getId() == 0)
			addCommonName(taxon, commonName, callback);
		else
			editCommonName(taxon, commonName, callback);
	}
	
	public void editCommonName(final Taxon taxon, final CommonName commonName, final GenericCallback<String> callback) {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.postAsText(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/commonname/" + commonName.getId(), commonName.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				CommonName toRemove = null;
				for (CommonName c : taxon.getCommonNames()) {
					if (c.getId() == commonName.getId()) {
						toRemove = c;
						break;
					}
				}
				taxon.getCommonNames().remove(toRemove);
				taxon.getCommonNames().add(commonName);
				callback.onSuccess(null);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void addCommonName(final Taxon taxon, final CommonName commonName, final GenericCallback<String> callback) {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.putAsText(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/commonname", commonName.toXML(), new GenericCallback<String>() {
		
			@Override
			public void onSuccess(String result) {
				String newId = ndoc.getText();
				if (commonName.getId() == 0) {
					commonName.setId(Integer.parseInt(newId));
					
				} else {
					CommonName toRemove = null;
					for (CommonName c : taxon.getCommonNames()) {
						if (c.getId() == commonName.getId()) {
							toRemove = c;
							break;
						}
					}
					taxon.getCommonNames().remove(toRemove);
				}
				if (taxon.getCommonNames().isEmpty())
					commonName.setPrincipal(true);
				taxon.getCommonNames().add(commonName);
				callback.onSuccess(newId);
		
			}
		
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
		
			}
		});
		
	}
	
	/**
	 * Fetch Working sets for related to the taxon
	 * 
	 * @param taxon
	 * @param callback
	 */
	public void fetchWorkingSetsForTaxon(final Taxon taxon,final GenericCallback<List<WorkingSet>> wayBack) {
		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getSISBase() + "/browse/workingSets/" + taxon.getId(), new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				wayBack.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				List<WorkingSet> workingSets;
				workingSets = new ArrayList<WorkingSet>();
				
				NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName(WorkingSet.ROOT_TAG);
				for (int i = 0; i < list.getLength(); i++) {
					NativeElement element = (NativeElement) list.item(i);
					WorkingSet ws = WorkingSet.fromXMLMinimal(element);
					workingSets.add(ws);	
				}
				wayBack.onSuccess(workingSets);
			}

		});
	}
	
	public void fetchTaxomaticHistory(final Taxon taxon, final GenericCallback<List<TaxomaticOperation>> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getSISBase() + "/taxomatic/history/" + taxon.getId(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				final List<TaxomaticOperation> list = new ArrayList<TaxomaticOperation>();
				
				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("operation");
				for (int i = 0; i < nodes.getLength(); i++) {
					NativeElement node = nodes.elementAt(i);
					
					list.add(TaxomaticOperation.fromXML(node));
				}
				
				callback.onSuccess(list);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public static class CacheBackedTaxonHierarchy extends TaxonHierarchy {
		
		private final Integer taxonID;
		
		public CacheBackedTaxonHierarchy(TaxonHierarchy hierarchy) {
			super();
			this.taxonID = hierarchy.getTaxon() == null ? null : hierarchy.getTaxon().getId();
			setFootprint(hierarchy.getFootprint());
			setChildren(hierarchy.getChildren());
		}
		
		@Override
		public Taxon getTaxon() {
			return TaxonomyCache.impl.getTaxon(taxonID);
		}
		
		@Override
		public List<Taxon> getChildren() {
			List<Taxon> children = new ArrayList<Taxon>();
			for (Taxon thin : super.getChildren()) {
				Taxon full = TaxonomyCache.impl.getTaxon(thin.getId());
				if (full == null)
					children.add(thin);
				else
					children.add(full);
			}
			return children;
		}
		
	}

}
