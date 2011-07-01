package org.iucn.sis.shared.api.criteriacalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.solertium.util.portable.PortableAlphanumericComparator;

public class CriteriaLevel implements Comparable<CriteriaLevel> {
	
	public static final String L1 = "ABCDE";
	public static final String L2 = "12345";
	public static final String L3 = "abcde";
	public static final String L4 = "iiiv"; //i,ii,iii,iv,v
	
	public static final String[] SEPARATORS = new String[] { "+", "", ",", "" };
	
	private final PortableAlphanumericComparator comparator;
	
	private final String code;
	private final int level;
	private final String separator;
	private final Map<String, CriteriaLevel> children;
	
	public CriteriaLevel(String code) {
		this.code = code;
		this.level = calculateLevel();
		this.separator = level > 0 ? SEPARATORS[level-1] : ";";
		this.children = new HashMap<String, CriteriaLevel>();
		this.comparator = new PortableAlphanumericComparator();
	}
	
	public void add(CriteriaLevel level) {
		children.put(level.getCode(), level);
	}
	
	public boolean contains(String code) {
		return children.containsKey(code);
	}
	
	public CriteriaLevel getChild(String code) {
		return children.get(code);
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
		List<CriteriaLevel> children = new ArrayList<CriteriaLevel>(this.children.values());
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

}
