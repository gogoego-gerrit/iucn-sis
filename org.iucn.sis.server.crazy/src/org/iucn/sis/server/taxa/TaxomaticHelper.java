package org.iucn.sis.server.taxa;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.users.utils.Arrays16Emulation;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.VFS;

public class TaxomaticHelper {

	/**
	 * WARNING -- OLD PARENT AND NEW PARENT SHOULD BE OF THE SAME
	 * LEVEL!!!!!!!!!!!!!!!
	 * 
	 * Takes all the children under oldParent and updates their internals to say
	 * they are now children of the newParent.
	 * 
	 * @param oldParent
	 * @param newParent
	 * @param addSynonym
	 *            TODO
	 * @return a list of taxa that were changed, or null if old and new parent
	 *         aren't on the same level
	 */
	public static ArrayList<TaxonNode> moveChildren(TaxonNode oldParent, TaxonNode newParent, VFS vfs,
			boolean addSynonym) {
		ArrayList<TaxonNode> changed = new ArrayList<TaxonNode>();
		ArrayList<TaxonNode> footprints = new ArrayList<TaxonNode>();

		footprints.add(oldParent);

		while (!footprints.isEmpty()) {
			TaxonNode currentNode = footprints.remove(0);
			if (currentNode.getLevel() != TaxonNode.INFRARANK
					&& currentNode.getLevel() != TaxonNode.INFRARANK_SUBPOPULATION) {
				String[] children = TaxonomyDocUtils.getChildrenTaxaByID(currentNode.getId() + "").split(",");
				for (int i = 0; i < children.length; i++) {
					if (!children[i].equalsIgnoreCase("") && vfs.exists(ServerPaths.getURLForTaxa(children[i]))) {
						TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
								.getURLForTaxa(children[i]), vfs), null, false);
						SysDebugger.getNamedInstance(SysDebugger.FINEST + "").println(
								"added in footprint " + node.getId());
						footprints.add(node);
					}
				}
			}

			if (currentNode != oldParent) {
				SynonymData syn = TaxonNodeFactory.synonymizeNode(currentNode);
				for (int i = 0; i < newParent.getLevel(); i++) {
					currentNode.setFootprintAtLevel(i, newParent.getFootprint()[i]);
				}
				currentNode.setFootprintAtLevel(newParent.getLevel(), newParent.getName());

				if (currentNode.getParentId().equalsIgnoreCase(oldParent.getId() + "")) {
					currentNode.setParentId(newParent.getId() + "");
				}
				currentNode.setParentName(newParent.getFullName());

				if (addSynonym && !currentNode.generateFullName().trim().equalsIgnoreCase(syn.getName().trim())) {					
					currentNode.addSynonym(syn);			
				}

				changed.add(currentNode);
			}

		}

		return changed;
	}

	/**
	 * Given an infrarank and the species that the infrarnk has been merged
	 * into, moves all of the subpopulations of the infrarnk into the
	 * subpopulations of the species. Sets the footprint correctly, and changes
	 * the level of the subpopulations. (Does not add synonyms.)
	 * 
	 * @param infrarank
	 * @param species
	 * @param vfs
	 * @return
	 */
	public static ArrayList<TaxonNode> moveChildrenOfInfrarankToSpeciesSubpopulation(final TaxonNode infrarank,
			final TaxonNode species, final VFS vfs) {
		ArrayList<TaxonNode> movedNodes = new ArrayList<TaxonNode>();
		String[] childrenIDS = TaxonomyDocUtils.getChildrenTaxaByID(infrarank.getId() + "").split(",");
		System.out.println("for infrarank " + infrarank.getFullName() + " the childrenIDS are "
				+ childrenIDS.toString());

		for (String childID : childrenIDS) {
			System.out.println("looking at childID " + childID + " at " + ServerPaths.getURLForTaxa(childID));
			if (!childID.equalsIgnoreCase("") && vfs.exists(ServerPaths.getURLForTaxa(childID))) {
				try {
					TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
							.getURLForTaxa(childID), vfs), null, false);
					System.out.println("node existed " + node.getFullName());

					for (int i = 0; i < species.getLevel(); i++) {
						node.setFootprintAtLevel(i, species.getFootprint()[i]);
					}
					node.setFootprint(Arrays16Emulation.copyOf(node.getFootprint(), node.getFootprint().length - 1));
					node.setParentName(species.getFullName());
					node.setParentId(species.getId() + "");
					node.setLevel(TaxonNode.SUBPOPULATION);

					movedNodes.add(node);

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("FAILED to create taxa from ID " + childID);
				}

			} else {
				System.out.println("childID did not exist");
			}
		}

		return movedNodes;

	}

	/**
	 * WARNING -- oldParent and newParent must be the same level, or things will
	 * be messed up.
	 * 
	 * @param newParent
	 * @param child
	 * @param addSynonym
	 *            - if true, this will add a synonym to the moved taxon if its
	 *            name ended up being changed
	 */
	public static ArrayList<TaxonNode> moveNode(TaxonNode newParent, TaxonNode child, VFS vfs, boolean addSynonym) {
		SysDebugger.getNamedInstance(SysDebugger.FINEST + "").println("in movenode");
		newParent.setAsParent(child);

		// CHANGE THE FOOTPRINT OF CHILD, AND ALL CHILDREN OF CHILD
		ArrayList<TaxonNode> footprints = new ArrayList<TaxonNode>();
		ArrayList<TaxonNode> changed = new ArrayList<TaxonNode>();
		footprints.add(child);
		SysDebugger.getNamedInstance(SysDebugger.FINEST + "").println("added in footprint " + child.getId());

		while (!footprints.isEmpty()) {
			TaxonNode current = footprints.remove(0);
			if (current.getLevel() != TaxonNode.INFRARANK && current.getLevel() != TaxonNode.INFRARANK_SUBPOPULATION) {
				String[] children = TaxonomyDocUtils.getChildrenTaxaByID(current.getId() + "").split(",");
				for (int i = 0; i < children.length; i++) {
					if (!children[i].equalsIgnoreCase("") && vfs.exists(ServerPaths.getURLForTaxa(children[i]))) {
						try {
							TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
									.getURLForTaxa(children[i]), vfs), null, false);
							SysDebugger.getNamedInstance(SysDebugger.FINEST + "").println(
									"added in footprint " + node.getId());
							footprints.add(node);
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("FAILED to create taxa from ID " + children[i]);
						}
					}
				}
			}

			SynonymData syn = TaxonNodeFactory.synonymizeNode(current);
			String oldName = current.getFullName();
			for (int i = 0; i < newParent.getLevel(); i++) {
				current.setFootprintAtLevel(i, newParent.getFootprint()[i]);
			}
			current.setFootprintAtLevel(newParent.getLevel(), newParent.getName());
			current.setParentName(newParent.getFullName());

			if (addSynonym && !current.generateFullName().equalsIgnoreCase(oldName))
				current.addSynonym(syn);

			changed.add(current);
		}

		return changed;
	}

	/**
	 * WARNING -- OLD PARENT AND NEW PARENT SHOULD BE THE SAME TAXON, JUST WITH
	 * DIFFERENT NAMES!
	 * 
	 * Takes all the children under oldParent and updates their internals to say
	 * their parent is newParent.
	 * 
	 * @param oldParent
	 * @param newParent
	 * 
	 * @return a list of taxa that were changed, or null if old and new parent
	 *         aren't on the same level
	 */
	public static ArrayList<TaxonNode> renameChildren(TaxonNode oldParent, TaxonNode newParent, VFS vfs) {
		ArrayList<TaxonNode> changed = new ArrayList<TaxonNode>();
		ArrayList<TaxonNode> footprints = new ArrayList<TaxonNode>();

		footprints.add(oldParent);

		while (!footprints.isEmpty()) {
			TaxonNode currentNode = footprints.remove(0);
			if (currentNode.getLevel() != TaxonNode.INFRARANK
					&& currentNode.getLevel() != TaxonNode.INFRARANK_SUBPOPULATION) {
				String[] children = TaxonomyDocUtils.getChildrenTaxaByID(currentNode.getId() + "").split(",");
				for (int i = 0; i < children.length; i++) {
					if (!children[i].equalsIgnoreCase("") && vfs.exists(ServerPaths.getURLForTaxa(children[i]))) {
						TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(ServerPaths
								.getURLForTaxa(children[i]), vfs), null, false);
						SysDebugger.getNamedInstance(SysDebugger.FINEST + "").println(
								"added in footprint " + node.getId());
						footprints.add(node);
					}
				}
			}

			if (currentNode != oldParent) {
				String oldFullName = currentNode.generateFullName();

				for (int i = 0; i < newParent.getLevel(); i++) {
					currentNode.setFootprintAtLevel(i, newParent.getFootprint()[i]);
				}
				currentNode.setFootprintAtLevel(newParent.getLevel(), newParent.getName());
				String newFullName = currentNode.generateFullName();

				if (currentNode.getParentId().equalsIgnoreCase(oldParent.getId() + "")) {
					currentNode.setParentId(newParent.getId() + "");
				}
				currentNode.setParentName(newParent.getFullName());

				if (!newFullName.equals(oldFullName))
					TaxonomyDocUtils.renameTaxon(currentNode.getFootprint()[0], currentNode.getId() + "", oldFullName,
							currentNode.getName(), newFullName);

				changed.add(currentNode);
			}

		}

		return changed;
	}

	/**
	 * This operation will rename child nodes (if need be) from the name of
	 * oldNode to the name of newNode. It will also rename the entries in the
	 * taxonomy hierarchy documents. If any "child" nodes were changed it will
	 * return those.
	 * 
	 * @param newNode
	 * @param oldNode
	 * @param vfs
	 * @return child nodes that were changed
	 */
	public static ArrayList<TaxonNode> taxonNameChanged(TaxonNode newNode, TaxonNode oldNode, VFS vfs) {
		ArrayList<TaxonNode> taxaChanged = new ArrayList<TaxonNode>();

		if (!newNode.getFullName().equalsIgnoreCase(oldNode.getFullName())) {
			taxaChanged.addAll(TaxomaticHelper.renameChildren(oldNode, newNode, vfs));
			TaxonomyDocUtils.renameTaxon(oldNode, newNode);
		}

		return taxaChanged;
	}
	/**
	 * This operation will rename child nodes (if need be) from the name of
	 * oldNode to the name of newNode. It will also rename the entries in the
	 * taxonomy hierarchy documents. If any "child" nodes were changed it will
	 * return those.
	 * 
	 * @param newNode
	 * @param oldNode
	 * @param vfs
	 * @return child nodes that were changed
	 */
	public static ArrayList<AssessmentData> updateAssessmentTaxonName(TaxonNode newNode, TaxonNode oldNode, VFS vfs) {
		ArrayList<AssessmentData> changed = new ArrayList<AssessmentData>();

		if (!newNode.getFullName().equalsIgnoreCase(oldNode.getFullName())) {
			for( String curPub : newNode.getAssessments() ) {
				AssessmentData cur = AssessmentIO.readAssessment(vfs, curPub, BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, null);
				cur.setSpeciesName(newNode.getFullName());
				changed.add(cur);
			}

			List<AssessmentData> drafts = AssessmentIO.readAllDraftAssessments(vfs, newNode.getId()+"");
			for( AssessmentData cur : drafts ) {
				cur.setSpeciesName(newNode.getFullName());
				changed.add(cur);
			}
		}

		return changed;
	}

}
