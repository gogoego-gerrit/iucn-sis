package org.iucn.sis.shared.api.models.comparators;

import java.util.Comparator;

import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.solertium.util.portable.PortableAlphanumericComparator;

public class TaxonNavigationComparator implements Comparator<Taxon> {
	
	private final PortableAlphanumericComparator comparator;
	
	public TaxonNavigationComparator() {
		comparator = new PortableAlphanumericComparator();
	}
	
	@Override
	public int compare(Taxon o1, Taxon o2) {
		String f1 = o1.getFootprint()[TaxonLevel.FAMILY];
		String f2 = o2.getFootprint()[TaxonLevel.FAMILY];
		
		int result = comparator.compare(f1, f2);
		
		if (result == 0) {
			String n1 = o1.getFriendlyName();
			String n2 = o2.getFriendlyName();
			
			result = comparator.compare(n1, n2);
		}
		
		return result;
	}

}
