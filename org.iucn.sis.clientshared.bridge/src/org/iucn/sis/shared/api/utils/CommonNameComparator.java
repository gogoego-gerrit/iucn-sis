package org.iucn.sis.shared.api.utils;

import java.util.Comparator;

import org.iucn.sis.shared.api.models.CommonName;

import com.solertium.util.portable.PortableAlphanumericComparator;

public class CommonNameComparator implements Comparator<CommonName>{
	
	protected PortableAlphanumericComparator pac;
	
	public CommonNameComparator() {
		pac = new PortableAlphanumericComparator();
	}
	
	@Override
	public int compare(CommonName o1, CommonName o2) {
		if (o1 == o2)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1.isPrimary())
			return -1;
		if (o2.isPrimary())
			return 1;
		
		return pac.compare(o1.getName(), o2.getName());
	}
	
}
