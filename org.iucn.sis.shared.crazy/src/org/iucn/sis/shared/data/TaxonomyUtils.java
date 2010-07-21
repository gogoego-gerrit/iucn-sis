package org.iucn.sis.shared.data;

import java.util.ArrayList;

import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class TaxonomyUtils {
	private static class ComparableTaxon implements Comparable {
		private String id;
		private String fullName;

		public ComparableTaxon(String id, String fullName) {
			super();
			this.id = id;
			this.fullName = fullName;
		}

		public int compareTo(Object arg0) {
			return comparator.compare(fullName, arg0);
		}

		@Override
		public String toString() {
			return fullName;
		}
	}

	private static PortableAlphanumericComparator comparator = new PortableAlphanumericComparator();

	/**
	 * Will sort an array of ids properly, based on the user's preferred format.
	 * 
	 * @param array
	 *            of taxaIDs - these MUST HAVE already been fetched and exist in
	 *            TaxonomyCache
	 * @return sorted array of taxaIDs as array
	 * @throws Exception
	 *             if a taxa does not exist in the cache
	 */
	public static String[] sortTaxaIDsProperly(Object[] taxaIDs) throws Exception {
		ComparableTaxon[] comparables = new ComparableTaxon[taxaIDs.length];
		String[] newIDs = new String[taxaIDs.length];

		for (int i = 0; i < taxaIDs.length; i++) {
			TaxonNode curNode = TaxonomyCache.impl.getNode(taxaIDs[i].toString());
			comparables[i] = new ComparableTaxon(taxaIDs[i].toString(), curNode.getFootprintAsString() + " "
					+ curNode.getFullName());
		}

		ArrayUtils.quicksort(comparables);

		for (int i = 0; i < comparables.length; i++)
			newIDs[i] = comparables[i].id;

		return newIDs;
	}

	/**
	 * Will sort an array of ids properly, based on the user's preferred format.
	 * 
	 * @param array
	 *            of taxaIDs - these MUST HAVE already been fetched and exist in
	 *            TaxonomyCache
	 * @return sorted array of taxaIDs as ArrayList
	 * @throws Exception
	 *             if a taxa does not exist in the cache
	 */
	public static ArrayList sortTaxaIDsProperlyAsArrayList(Object[] taxaIDs) throws Exception {
		ComparableTaxon[] comparables = new ComparableTaxon[taxaIDs.length];
		ArrayList newIDs = new ArrayList();

		for (int i = 0; i < taxaIDs.length; i++) {
			TaxonNode curNode = TaxonomyCache.impl.getNode(taxaIDs[i].toString());
			comparables[i] = new ComparableTaxon(taxaIDs[i].toString(), curNode.getFootprintAsString(TaxonNode.FAMILY)
					+ " " + curNode.getFullName());
		}

		ArrayUtils.quicksort(comparables);

		for (int i = 0; i < comparables.length; i++)
			newIDs.add(comparables[i].id);

		return newIDs;
	}
}
