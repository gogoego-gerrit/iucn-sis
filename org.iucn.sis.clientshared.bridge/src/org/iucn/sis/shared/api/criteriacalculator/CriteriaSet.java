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
