package org.iucn.sis.shared.taxonomyTree;

import java.util.HashMap;

/**
 * "Stores" a bunch of TaxonNodes.
 * 
 * @Todo A node is only unique within a Kingdom ... we need a new storage scheme
 *       that will allow this separation inside a Kingdom.
 * 
 * @author adam.schwartz
 * 
 */
public class TaxonomyTree {

	public class Kingdom {
		HashMap[] levels;

		TaxonNode theKingdom;

		HashMap phylums;
		HashMap classes;
		HashMap orders;
		HashMap family;
		HashMap genus;
		HashMap species;
		HashMap infraranks;
		HashMap subpopulations;
		HashMap infrasubpopulations;

		public Kingdom(TaxonNode theKingdom) {
			this.theKingdom = theKingdom;

			phylums = new HashMap();
			classes = new HashMap();
			orders = new HashMap();
			family = new HashMap();
			genus = new HashMap();
			species = new HashMap();
			infraranks = new HashMap();
			subpopulations = new HashMap();
			infrasubpopulations = new HashMap();

			levels = new HashMap[] { phylums, classes, orders, family, genus, species, infraranks, subpopulations,
					infrasubpopulations };
		}

		public HashMap getClasses() {
			return classes;
		}

		public HashMap getFamily() {
			return family;
		}

		public HashMap getGenus() {
			return genus;
		}

		public HashMap getInfraranks() {
			return infraranks;
		}

		public HashMap getInfrasubpopulations() {
			return infrasubpopulations;
		}

		public HashMap[] getLevels() {
			return levels;
		}

		public HashMap getOrders() {
			return orders;
		}

		public HashMap getPhylums() {
			return phylums;
		}

		public HashMap getSpecies() {
			return species;
		}

		public HashMap getSubpopulations() {
			return subpopulations;
		}

		public TaxonNode getTheKingdom() {
			return theKingdom;
		}

	}

	private HashMap kingdoms;

	private int[] counts;

	public TaxonomyTree() {
		kingdoms = new HashMap();
	}

	// public int getNumberOfLevels()
	// {
	// return levels.length;
	// }

	public void addNode(String kingdom, TaxonNode nodeToAdd) {
		if (nodeToAdd.getLevel() == TaxonNode.KINGDOM)
			kingdoms.put(kingdom, new Kingdom(nodeToAdd));
		else
			((Kingdom) kingdoms.get(kingdom)).levels[nodeToAdd.getLevel() - 1].put(nodeToAdd.getFullName(), nodeToAdd);
	}

	public int[] getCounts() {
		return counts;
	}

	public HashMap getKingdoms() {
		return kingdoms;
	}

	// public boolean isTreeFull()
	// {
	// for (int i = 0; i < levels.length; i++)
	// if (!isLevelFull(i))
	// return false;
	//
	// return true;
	// }

	// public boolean isLevelFull(int level)
	// {
	// if (levels[level].keySet().size() >= counts[level] - 1)
	// return true;
	//
	// return false;
	// }
	//
	// public int getLevelStatus(int level)
	// {
	// return (int) (((double) levels[level].keySet().size() / (double)
	// counts[level]) * 100.0);
	// }
	//
	// public int getOverallStatus()
	// {
	// double loaded = 0;
	//
	// for (int i = 0; i < levels.length; i++)
	// loaded += levels[i].keySet().size();
	//
	// return (int) (loaded / (double) counts[counts.length - 1] * 100.0);
	// }
	//
	// public String getStats()
	// {
	// String stats = "There are " + kingdoms.keySet().size() + " kingdoms.\n";
	// stats += "There are " + phylums.keySet().size() + " phylums.\n";
	// stats += "There are " + classes.keySet().size() + " classes.\n";
	// stats += "There are " + orders.keySet().size() + " orders.\n";
	// stats += "There are " + family.keySet().size() + " family.\n";
	// stats += "There are " + genus.keySet().size() + " genus.\n";
	// stats += "There are " + species.keySet().size() + " species.\n";
	// stats += "There are " + infraranks.keySet().size() + " infraranks.\n";
	// stats += "There are " + subpopulations.keySet().size() +
	// " subpopulations.\n";
	// stats += "There are " + infrasubpopulations.keySet().size() +
	// " infrasubpopulations.\n";
	//
	// return stats;
	// }

	// public void buildRelationships(int levelToBuild)
	// {
	// if (levelToBuild == 0)
	// return;
	//
	// for (Iterator iter = levels[levelToBuild].values().iterator();
	// iter.hasNext();)
	// {
	// // TaxonNode curNode = (TaxonNode)iter.next();
	//
	// /*
	// * if( ! (curNode.getParent() != null &&
	// * curNode.getParent().getName().equalsIgnoreCase(
	// * curNode.getParentName() ) ) ) { if( getNode( curNode.level-1,
	// * curNode.getParentName() ) != null ) getNode( curNode.level-1,
	// * curNode.getParentName() ).addChild( curNode ); }
	// */
	// }
	// }
	//
	// public TaxonNode searchForNode(int level, String id)
	// {
	// for (Iterator iter = levels[level].values().iterator(); iter.hasNext();)
	// {
	// TaxonNode curNode = (TaxonNode) iter.next();
	// if (curNode.matches(id))
	// return curNode;
	// }
	//
	// return null;
	// }
	//
	// public boolean contains(int level, String id)
	// {
	// return levels[level].containsKey(id);
	// }

	public TaxonNode getNode(int level, String kingdom, String name) {
		Kingdom curKingdom = (Kingdom) kingdoms.get(kingdom);

		if (curKingdom == null)
			return null;

		if (level == TaxonNode.KINGDOM)
			return curKingdom.getTheKingdom();
		else
			return (TaxonNode) curKingdom.levels[level - 1].get(name);
	}

	// public TaxonNode findNode( String id )
	// {
	// for( int i = 0; i < getNumberOfLevels(); i++ )
	// if( getNode( i, id ) != null )
	// return getNode( i, id );
	//		
	// return null;
	// }

	// public HashMap getLevel(int level)
	// {
	// return levels[level];
	// }

	public void removeNode(String kingdom, TaxonNode nodeToRemove) {
		if (nodeToRemove.getLevel() != TaxonNode.KINGDOM)
			((Kingdom) kingdoms.get(kingdom)).levels[nodeToRemove.getLevel() - 1].remove(nodeToRemove.getFullName());
		else
			kingdoms.remove(kingdom);
	}

	public void setCounts(int[] counts) {
		this.counts = counts;
	}

}
