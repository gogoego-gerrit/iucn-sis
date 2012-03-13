package org.iucn.sis.shared.helpers;

import java.util.HashMap;

import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

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
		HashMap<String, Taxon>[] levels;

		Taxon theKingdom;

		HashMap<String, Taxon> phylums;
		HashMap<String, Taxon> classes;
		HashMap<String, Taxon> orders;
		HashMap<String, Taxon> family;
		HashMap<String, Taxon> genus;
		HashMap<String, Taxon> species;
		HashMap<String, Taxon> infraranks;
		HashMap<String, Taxon> subpopulations;
		HashMap<String, Taxon> infrasubpopulations;

		@SuppressWarnings("unchecked")
		public Kingdom(Taxon theKingdom) {
			this.theKingdom = theKingdom;

			phylums = new HashMap<String, Taxon>();
			classes = new HashMap<String, Taxon>();
			orders = new HashMap<String, Taxon>();
			family = new HashMap<String, Taxon>();
			genus = new HashMap<String, Taxon>();
			species = new HashMap<String, Taxon>();
			infraranks = new HashMap<String, Taxon>();
			subpopulations = new HashMap<String, Taxon>();
			infrasubpopulations = new HashMap<String, Taxon>();

			levels = new HashMap[] { phylums, classes, orders, family, genus, species, infraranks, subpopulations,
					infrasubpopulations };
		}

		public HashMap<String, Taxon> getClasses() {
			return classes;
		}

		public HashMap<String, Taxon> getFamily() {
			return family;
		}

		public HashMap<String, Taxon> getGenus() {
			return genus;
		}

		public HashMap<String, Taxon> getInfraranks() {
			return infraranks;
		}

		public HashMap<String, Taxon> getInfrasubpopulations() {
			return infrasubpopulations;
		}

		public HashMap<String, Taxon>[] getLevels() {
			return levels;
		}

		public HashMap<String, Taxon> getOrders() {
			return orders;
		}

		public HashMap<String, Taxon> getPhylums() {
			return phylums;
		}

		public HashMap<String, Taxon> getSpecies() {
			return species;
		}

		public HashMap<String, Taxon> getSubpopulations() {
			return subpopulations;
		}

		public Taxon getTheKingdom() {
			return theKingdom;
		}

	}

	private HashMap<String, Kingdom> kingdoms;

	private int[] counts;

	public TaxonomyTree() {
		kingdoms = new HashMap<String, Kingdom>();
	}

	// public int getNumberOfLevels()
	// {
	// return levels.length;
	// }

	public void addNode(String kingdom, Taxon nodeToAdd) {
		if (nodeToAdd.getLevel() == TaxonNode.KINGDOM)
			kingdoms.put(kingdom, new Kingdom(nodeToAdd));
		else
			kingdoms.get(kingdom).levels[nodeToAdd.getLevel() - 1].put(nodeToAdd.getFullName(), nodeToAdd);
	}

	public int[] getCounts() {
		return counts;
	}

	public HashMap<String, Kingdom> getKingdoms() {
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

	public Taxon getNode(int level, String kingdom, String name) {
		Kingdom curKingdom = kingdoms.get(kingdom);
		if (curKingdom == null)
			return null;

		if (level == TaxonLevel.KINGDOM)
			return curKingdom.getTheKingdom();
		else
			return curKingdom.levels[level - 1].get(name);
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

	public void removeNode(String kingdom, Taxon nodeToRemove) {
		if (nodeToRemove.getLevel() != TaxonLevel.KINGDOM)
			kingdoms.get(kingdom).levels[nodeToRemove.getLevel() - 1].remove(nodeToRemove.getFullName());
		else
			kingdoms.remove(kingdom);
	}

	public void setCounts(int[] counts) {
		this.counts = counts;
	}

}
