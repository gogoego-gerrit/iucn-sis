package org.iucn.sis.shared.api.criteriacalculator;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

public class CriteriaSet {
	
	private CriteriaLevel head;
	
	public void parse(Field field) {
		for (PrimitiveField<String> prim : field.getPrimitiveField()) {
			String name = prim.getName();
			if (!CriteriaLevel.L1.contains(name.charAt(0)+""))
				continue;
			
			StringBuilder buf = new StringBuilder();
			
			char[] chars = name.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				buf.append(c);
				if (CriteriaLevel.L4.contains(buf.toString()) && i+1 < chars.length) {
					continue;
				}
				else {
					CriteriaLevel level = new CriteriaLevel(buf.toString());
					if (head == null)
						head = level;
					else {
						head.add(level);
					}
				}
			}
		}
	}

}
