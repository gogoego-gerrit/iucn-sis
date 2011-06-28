package org.iucn.sis.shared.api.criteriacalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

import com.solertium.util.portable.PortableAlphanumericComparator;

public class CriteriaLevel implements Comparable<CriteriaLevel> {
	
	public static final String L1 = "ABCDE";
	public static final String L2 = "12345";
	public static final String L3 = "abcde";
	public static final String L4 = "iiiv"; //i,ii,iii,iv,v
	
	private static final String[] SEPARATORS = new String[] { "+", "", ",", "" };
	
	private final PortableAlphanumericComparator comparator;
	
	private final String code;
	private final int level;
	private final String separator;
	private final List<CriteriaLevel> children;
	
	public CriteriaLevel(String code) {
		this.code = code;
		this.level = calculateLevel();
		this.separator = level > 0 ? SEPARATORS[level-1] : ",";
		this.children = new ArrayList<CriteriaLevel>();
		this.comparator = new PortableAlphanumericComparator();
	}
	
	public void add(CriteriaLevel level) {
		children.add(level);
	}
	
	public int getLevel() {
		return level;
	}
	
	public String getCode() {
		return code;
	}
	
	private int calculateLevel() {
		if ("".equals(code))
			return -1;
		
		int level = 1;
		for (String L : new String[] {L1, L2, L3, L4})
			if (L.contains(code))
				return level;
			else
				level++;
		return -1;
	}
	
	@Override
	public String toString() {
		Collections.sort(children);
		
		final StringBuilder out = new StringBuilder();
		out.append(code);
		if (!children.isEmpty()) {
			boolean paren = level == 3;
			if (paren)
				out.append('(');
			for (Iterator<CriteriaLevel> iter = children.iterator(); iter.hasNext(); )
				out.append(iter.next() + (iter.hasNext() ? separator : ""));
			if (paren)
				out.append(')');
		}
		return out.toString();
	}
	
	@Override
	public int compareTo(CriteriaLevel o) {
		if (o == null)
			return 1;
		else
			return comparator.compare(code, o.getCode());
	}
	
	public static CriteriaLevel parse(Field field, String category) {
		Map<String, Map<String, CriteriaLevel>> graphs = new HashMap<String, Map<String,CriteriaLevel>>();
		
		Map<String,CriteriaLevel> graph = null;
		
		CriteriaLevel head = new CriteriaLevel("");
		CriteriaLevel current = head;
		
		for (PrimitiveField<String> prim : field.getPrimitiveField()) {
			String name = prim.getName();
			if (!CriteriaLevel.L1.contains(name.charAt(0)+"") || !category.equals(prim.getValue()))
				continue;
			
			graph = graphs.get("" + name.charAt(0));
			if (graph == null) {
				graph = new HashMap<String, CriteriaLevel>();
				graphs.put("" + name.charAt(0), graph);
			}
			
			StringBuilder buf = new StringBuilder(3);
			
			char[] chars = name.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				buf.append(c);
				if (CriteriaLevel.L4.contains(buf.toString()) && i+1 < chars.length) {
					continue;
				}
				else {
					CriteriaLevel level = graph.get(buf.toString());
					if (level == null) {
						level = new CriteriaLevel(buf.toString());
					
						graph.put(buf.toString(), level);
					
						current.add(level);
					}
					current = level;
				}
				buf = new StringBuilder(3);
			}
			
			current = head;
		}
		
		return head;
	}

}
