package org.iucn.sis.server.api.utils;

import java.util.Comparator;

import org.iucn.sis.shared.api.models.Taxon;

public class TaxaComparators {
	
	public static Comparator<Taxon> getTaxonComparatorByLevel() {
		return new Comparator<Taxon>() {
			
			@Override
			public int compare(Taxon o1, Taxon o2) {
				if (o1.getTaxonLevel().compareTo(o2.getTaxonLevel()) == 0) {
					return o1.getFriendlyName().compareTo(o2.getFriendlyName());
				} else {
					return o1.getTaxonLevel().compareTo(o2.getTaxonLevel());
				}
			}
		};
	}
	
	public static Comparator<Taxon> getTaxonNameComparator() {
		return new Comparator<Taxon>() {
		
			@Override
			public int compare(Taxon o1, Taxon o2) {
				return o1.getFriendlyName().compareTo(o2.getFriendlyName());
			}
		};
	}

}
