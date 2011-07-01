package org.iucn.sis.shared.api.criteriacalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;

public class CriteriaSet {
	
	public static CriteriaSet fromString(ResultCategory category, String criteria) {
		List<String> list = new ArrayList<String>();
		
		String[] palette = new String[] {
			CriteriaLevel.L1, CriteriaLevel.L2, CriteriaLevel.L3, CriteriaLevel.L4
		};
		
		int level = 0;
		
		StringBuilder buf = new StringBuilder();
		for (char c : criteria.toCharArray()) {
			if ('(' == c || ')' == c)
				continue;
			
			if (palette[level].indexOf(c) != -1) {
				if ("".equals(buf.toString()) || level == 3) //Level 3 can have multiple tokens
					buf.append(c);
				else {
					if (level == 2) //The delimiter is a space
						list.add(buf.toString());
					
					buf = new StringBuilder(buf.toString().subSequence(0, level));
					buf.append(c);
				}
			}
			else if (c == ';') {
				list.add(buf.toString());
				level = 0;
				buf = new StringBuilder(buf.toString().subSequence(0, level));
			}
			else if (c == '+') {
				list.add(buf.toString());
				level = 1;
				buf = new StringBuilder(buf.toString().subSequence(0, level));
			}
			else if (c == ',') {
				list.add(buf.toString());
				level = 3;
				buf = new StringBuilder(buf.toString().subSequence(0, level));
			}
			else if (level+1 < 4 && palette[level+1].indexOf(c) != -1) {
				buf.append(c);
				
				level++;
			}
			else {
				list.add(buf.toString());
				
				int newLevel = 0;
				int i;
				for (i = 0; i < palette.length; i++) {
					if (palette[i].indexOf(c) == -1)
						newLevel = i;
				}
				
				buf = new StringBuilder(buf.toString().substring(0, newLevel-1));
				buf.append(c);
			}
		}
		
		if (!"".equals(buf.toString()))
			list.add(buf.toString());
		
		return new CriteriaSet(category, list);
	}
	
	private static List<String> extractFromField(ResultCategory category, Field field) {
		List<String> list = new ArrayList<String>();
		for (PrimitiveField<?> prim : field.getPrimitiveField()) {
			if (prim instanceof StringPrimitiveField && 
					CriteriaLevel.L1.contains("" + prim.getName().charAt(0)) && 
					category.includes(((StringPrimitiveField)prim).getValue()))
				list.add(prim.getName());
		}
		return list;
	}
	
	private static List<String> toList(String criterion) {
		List<String> list = new ArrayList<String>();
		list.add(criterion);
		return list;
	}
	
	private ResultCategory category;
	private List<String> criteria;
	private CriteriaLevel head;
	
	public CriteriaSet(ResultCategory category) {
		this(category, new ArrayList<String>());
	}
	
	public CriteriaSet(ResultCategory category, Field field) {
		this(category, extractFromField(category, field));
	}
	
	public CriteriaSet(ResultCategory category, String criterion) {
		this(category, toList(criterion));
	}
	
	public CriteriaSet(ResultCategory category, List<String> criteria) {
		this.category = category;
		this.criteria = criteria;
		this.head = new CriteriaLevel("");
		
		parse();
	}
	
	private void parse() {
		CriteriaLevel current = head;
		
		for (String name : criteria) {
			StringBuilder buf = new StringBuilder(3);
			
			char[] chars = name.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				buf.append(c);
				if (CriteriaLevel.L4.contains(buf.toString()) && i+1 < chars.length) {
					continue;
				}
				else {
					CriteriaLevel level = current.getChild(buf.toString()); 
					if (level == null) {
						level = new CriteriaLevel(buf.toString());
						current.add(level);
					}
					current = level;
				}
				buf = new StringBuilder(3);	
			}
			
			current = head;
		}
	}
	
	public void merge(CriteriaSet... others) {
		HashSet<String> criteria = new HashSet<String>();
		criteria.addAll(getCriteria());
		
		for (CriteriaSet other : others)
			criteria.addAll(other.getCriteria());
		
		this.criteria = new ArrayList<String>(criteria);
		this.head = new CriteriaLevel("");
		parse();
	}
	
	public void merge(Collection<CriteriaSet> others) {
		merge(others.toArray(new CriteriaSet[others.size()]));
	}
	
	public ResultCategory getCategory() {
		return category;
	}
	
	public List<String> getCriteria() {
		return criteria;
	}
	
	public CriteriaLevel getLevels() {
		return head;
	}
	
	public boolean hasCriteria() {
		return !criteria.isEmpty();
	}
	
	@Override
	public String toString() {
		return head.toString();
	}

}
