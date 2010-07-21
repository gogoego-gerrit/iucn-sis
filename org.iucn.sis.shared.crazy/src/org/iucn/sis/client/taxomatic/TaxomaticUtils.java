package org.iucn.sis.client.taxomatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.data.assessments.AssessmentCache;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.client.utilities.FormattedDate;
import org.iucn.sis.shared.data.TaxonomyCache;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.api.XMLWritingUtils;

public class TaxomaticUtils {

	public static int CLEAR_DESCRIPTION_DELAY = 10000;

	public static TaxomaticUtils impl = new TaxomaticUtils();

	private GenericCallback<String> getDefaultCallback(final NativeDocument ndoc, 
			final GenericCallback<String> wayback, final String idToReturn) {
		return new GenericCallback<String>() {

			public void onFailure(Throwable arg0) {
				if( ndoc.getStatusText().equals("423") )
					WindowUtils.errorAlert("Taxomatic In Use", "Sorry, but another " +
							"taxomatic operation is currently running. Please try " +
							"again later!");
				
				wayback.onFailure(arg0);
			}

			public void onSuccess(String arg0) {
				afterTaxomaticOperation(idToReturn, new GenericCallback<String>() {

					public void onFailure(Throwable arg0) {
						wayback.onSuccess(null);
					}

					public void onSuccess(String arg0) {
						wayback.onSuccess(arg0);
					}

				});
			}

		};
	}
	
	/**
	 * Takes in a newNode and attaches it to the parent, if the parent is an
	 * instanceof either a TaxonNode or the TaxonomyTree.
	 * 
	 * @param newNode
	 * @param parent
	 */
	public static void newTaxonNode(final TaxonNode newNode, TaxonNode parent, final GenericCallback<TaxonNode> wayback) {
		// Update the parent
		if (parent != null) {
			parent.setAsParent(newNode);

			if (newNode.getFullName() == null || newNode.getFullName().equals("")) {
				String fullName = "";
				String[] footprint = parent.getFootprint();

				if (newNode.getLevel() >= TaxonNode.SPECIES)
					for (int i = 5; i < footprint.length; i++)
						fullName += footprint[i] + " ";

				fullName += newNode.getName();
				newNode.setFullName(fullName);
			}
		}

		TaxonomyCache.impl.createNewNode(newNode, new GenericCallback<TaxonNode>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(TaxonNode arg0) {
				ClientUIContainer.bodyContainer.refreshBody();
				wayback.onSuccess(newNode);
			}
		});
	}

	private TaxomaticUtils() {

	}

	protected void afterTaxomaticOperation(final String currentTaxaID, final GenericCallback<String> callback) {
		TaxonomyCache.impl.clear();
		AssessmentCache.impl.clear();
		AssessmentCache.impl.resetCurrentAssessment();
		AssessmentCache.impl.loadRecentAssessments(new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(String arg) {
				// Silently be happy.

				TaxonomyCache.impl.fetchNode(currentTaxaID, true, new GenericCallback<TaxonNode>() {
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(TaxonNode result) {
						ClientUIContainer.bodyContainer.refreshBody();
						callback.onSuccess(result.getId() + "");
					}
				});
			}
		});
	}

	/**
	 * Sends a request to the taxomatic restlet to handle lateral moves, where
	 * you are moving all of the childrenIDs to be under the parentID
	 * 
	 * @param parentID
	 * @param childrenIDs
	 * @param wayback
	 */
	public void lateralMove(final String parentID, List<String> childrenIDs, final GenericCallback<String> wayback) {
		if (parentID != null && childrenIDs != null && !parentID.equalsIgnoreCase("") && childrenIDs.size() > 0) {
			StringBuilder xml = new StringBuilder("<xml>\r\n<parent id=\"" + parentID + "\"/>\r\n");
			for (String childID : childrenIDs) {
				xml.append("<child id=\"" + childID + "\"/>\r\n");
			}
			xml.append("</xml>");

			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.post("/taxomatic/move", xml.toString(), getDefaultCallback(ndoc, wayback, parentID));
		} else
			wayback.onFailure(new Throwable(""));
	}

	public void moveAssessments(final String idMoveAssessmentsOutOF, final String idMoveAssessmentsInto,
			final List<String> assessmentIDs, final GenericCallback<String> wayback) {
		StringBuilder xml = new StringBuilder("<moveAssessments>\r\n");
		xml.append("<oldNode>" + idMoveAssessmentsOutOF + "</oldNode>\r\n");
		xml.append("<nodeToMoveInto>" + idMoveAssessmentsInto + "</nodeToMoveInto>\r\n");
		for (String ids : assessmentIDs) {
			xml.append("<assessmentID>" + ids + "</assessmentID>\r\n");
		}
		xml.append("</moveAssessments>");
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.put("/taxomatic/moveAssessments", xml.toString(), getDefaultCallback(ndoc, wayback, idMoveAssessmentsOutOF));
	}

	public void performDemotion(final TaxonNode demoteMe, String newParent, final GenericCallback<String> wayback) {
		String xml = "<xml>\r\n<demoted id=\"" + demoteMe.getId() + "\" >" + newParent + "</demoted>\r\n</xml>";
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.post("/taxomatic/demote", xml, getDefaultCallback(ndoc, wayback, demoteMe.getId()+""));
	}

	/**
	 * Merges nodes contained in the ArrayList nodes into newNode, placing it
	 * under newParent.
	 * 
	 * @param nodes
	 * @param newNode
	 * @param newParent
	 */
	public void performMerge(final ArrayList nodes, final TaxonNode newNode, final GenericCallback<String> wayback) {
		final StringBuffer ret = new StringBuffer("<u>Performed Merge into new Node <b>" + newNode.getName()
				+ "</b></u>");
		String mergedNodes = "";
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			TaxonNode curMerger = (TaxonNode) iter.next();
			mergedNodes += curMerger.getId() + ",";
		}

		if (mergedNodes.length() > 0) {
			mergedNodes = mergedNodes.substring(0, mergedNodes.length() - 1);
		}

		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		String xml = "<xml>\r\n" + "<merged>" + mergedNodes + "</merged>\r\n";
		xml += "<main>" + newNode.getId() + "</main>\r\n</xml>";
		ndoc.post("/taxomatic/merge", xml, getDefaultCallback(ndoc, wayback, newNode.getId()+""));
	}

	/**
	 * Posts to the taxonomy restlet where the taxomatic will merge the given
	 * infranks with the given species. The subpopulations of the infraranks
	 * will become subpopulations of the species.
	 * 
	 * @param infrarankIDS
	 * @param speciesID
	 * @param callback
	 */
	public void performMergeUpInfrarank(final List<String> infrarankIDS, final long speciesID, final String name,
			final GenericCallback<String> wayback) {

		if (infrarankIDS.size() > 0) {
			final StringBuffer ret = new StringBuffer("<u>The infraranks are now merged into <b>" + name + "</b></u>");

			StringBuilder infraIds = new StringBuilder();
			for (String id : infrarankIDS) {
				infraIds.append(id + ",");
			}

			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			String xml = "<xml>\r\n" + "<infrarank>" + infraIds.substring(0, infraIds.length() - 1)
					+ "</infrarank>\r\n";
			xml += "<species>" + speciesID + "</species>\r\n</xml>";
			ndoc.post("/taxomatic/mergeupinfrarank", xml, getDefaultCallback(ndoc, wayback, speciesID+""));
		} else {
			wayback.onFailure(new Throwable("You must merge at least one infrarank with the species"));
		}

	}

	/**
	 * Posts to the taxonomy restlet with the taxomatic will move the
	 * assessments from the oldNode into the newNode
	 * 
	 * @param assessmentIDs
	 * @param oldNodeID
	 * @param newNodeID
	 * @param wayback
	 */
	public void performMoveAssessments(final List<String> assessmentIDs, final String oldNodeID,
			final String newNodeID, final GenericCallback<String> wayback) {
		StringBuilder builder = new StringBuilder();
		builder.append("<moveAssessments>\r\n");
		builder.append("<oldNode>" + oldNodeID + "</oldNode>\r\n");
		builder.append("<nodeToMoveInto>" + newNodeID + "</nodeToMoveInto>r\n");
		for (String item : assessmentIDs) {
			builder.append("<assessmentID>" + item + "</assessmentID>r\n");
		}
		builder.append("</moveAssessments>");

		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.post("/taxomatic/moveAssessments", builder.toString(), getDefaultCallback(ndoc, wayback, oldNodeID));
	}

	/**
	 * Promotes a node to reside under the newParent node. Returns a String of
	 * HTML telling what actions it took to perform the promotion.
	 * 
	 * @param promoteMe
	 * @param newParent
	 * @return
	 */
	public void performPromotion(final TaxonNode promoteMe, final GenericCallback<String> wayback) {
		SysDebugger.getInstance().println("I am in perform promotion");
		String xml = "<xml>\r\n<promoted id=\"" + promoteMe.getId() + "\" />\r\n</xml>";
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.post("/taxomatic/promote", xml, getDefaultCallback(ndoc, wayback, promoteMe.getId()+""));
	}

	/**
	 * This will assign to each new node a deprecated synonym that is
	 * oldNode.getId(), and will give each node the proper new parent.
	 * 
	 * @param oldNode
	 * @param newNodes
	 * @param numSplits
	 */
	public void performSplit(final TaxonNode oldNode, HashMap<String, ArrayList<String>> parentToChild,
			final GenericCallback<String> wayback) {
		String xml = "<root>";
		xml += XMLWritingUtils.writeTag("current", "" + oldNode.getId());

		Iterator iterator = parentToChild.keySet().iterator();
		while (iterator.hasNext()) {
			String curID = (String) iterator.next();
			xml += "<parent id=\"" + curID + "\">";

			ArrayList values = parentToChild.get(curID);
			for (int i = 0; i < values.size(); i++) {
				TaxonNode curChild = (TaxonNode) values.get(i);
				xml += XMLWritingUtils.writeTag("child", curChild.getId() + "");
			}
			xml += "</parent>";
		}

		xml += "</root>";

		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.post("/taxomatic/split", xml, getDefaultCallback(ndoc, wayback, oldNode.getId()+""));
	}
	
	/**
	 * Writes the node to the file system and makes it the current node on success
	 * 
	 * @param node
	 * @param callback
	 */
	public void writeNodeToFSAndMakeCurrent(final TaxonNode node, final GenericCallback<Object> callback) {
		writeNodeToFS(node, new GenericCallback<Object>() {
		
			public void onSuccess(Object result) {
				TaxonomyCache.impl.setCurrentNode(node);
				callback.onSuccess(result);
		
			}
		
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);		
			}
		});
		
	}

	public void writeNodeToFS(TaxonNode node, final GenericCallback<Object> callback) {
		node.setLastUpdated(FormattedDate.impl.getDate());
		node.setUpdatedBy(SimpleSISClient.currentUser.getUsername());
		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();

		SysDebugger.getInstance().println("This is what I am sending: " + TaxonNodeFactory.nodeToDetailedXML(node));
		doc.put("/browse/nodes/" + node.getId(), TaxonNodeFactory.nodeToDetailedXML(node),
				new GenericCallback<String>() {

					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(String result) {
						TaxonomyCache.impl.clear();
						callback.onSuccess(result);
					}
				});
	}
}
