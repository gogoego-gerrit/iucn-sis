package org.iucn.sis.client.utilities;

import java.util.Date;

import org.iucn.sis.client.components.panels.TaxonomyBrowserPanel.TaxonListElement;

import com.extjs.gxt.ui.client.util.DefaultComparator;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class TaxonNodeComparator extends DefaultComparator<TaxonListElement> {

	private static TaxonNodeComparator instance = null;

	public final static String ALPHA_NUM = "name";
	public final static String SEQ_CODE = "sequence";

	public static TaxonNodeComparator getInstance() {
		if (instance == null) {
			instance = new TaxonNodeComparator();
		}
		return instance;
	}

	public static TaxonNodeComparator getInstance(String compareProperty) {
		if (instance == null) {
			instance = new TaxonNodeComparator(compareProperty);
		}
		return instance;
	}

	private String compareProperty;

	private PortableAlphanumericComparator comparator;

	protected TaxonNodeComparator() {
		this(ALPHA_NUM);
		comparator = new PortableAlphanumericComparator();
	}

	protected TaxonNodeComparator(String compareProperty) {
		this.compareProperty = compareProperty;
		comparator = new PortableAlphanumericComparator();
	}

	@Override
	public int compare(Object o1, Object o2) {
		if( o1 instanceof Date )
			return compareDates((Date)o1, (Date)o2);
		else
			return super.compare(o1, o2);
	}
	
	protected int compareDates(Date d1, Date d2) {
		return comparator.compare(FormattedDate.impl.getDate(d1), FormattedDate.impl.getDate(d2));
	}

	public void compareOn(String comparable) {
		compareProperty = comparable;
	}

	@Override
	protected int compareStrings(String s1, String s2) {
		return comparator.compare(s1, s2);
	}

	public String getCompareOn() {
		return compareProperty;
	}

}
