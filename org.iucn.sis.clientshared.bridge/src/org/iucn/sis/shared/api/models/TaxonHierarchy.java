package org.iucn.sis.shared.api.models;

import java.util.ArrayList;
import java.util.List;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.XMLWritingUtils;

public class TaxonHierarchy {
	
	/**
	 * This probably won't work client-side...
	 * @param taxon
	 * @return
	 */
	public static TaxonHierarchy fromTaxon(Taxon taxon) {
		final TaxonHierarchy hierarchy = new TaxonHierarchy();
		
		hierarchy.setFootprint(taxon.getIDFootprintAsString(0, "-"));
		
		final List<Integer> list = new ArrayList<Integer>();
		for (Taxon child : taxon.getChildren())
			if (Taxon.ACTIVE == child.getState())
				list.add(child.getId());
		hierarchy.setChildren(list);
		
		return hierarchy;
	}
	
	public static TaxonHierarchy fromXML(NativeDocument document) {
		final TaxonHierarchy hierarchy = new TaxonHierarchy();
		
		final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final NativeNode node = nodes.item(i);
			if ("footprint".equals(node.getNodeName()))
				hierarchy.setFootprint(node.getTextContent());
			else if ("options".equals(node.getNodeName())) {
				final List<Integer> list = new ArrayList<Integer>();
				
				final NativeNodeList children = node.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					final NativeNode child = children.item(k);
					if ("option".equals(child.getNodeName()))
						list.add(Integer.valueOf(child.getTextContent()));
				}
				
				hierarchy.setChildren(list);
			}
		}
		
		return hierarchy;
	}
	
	private List<Integer> children;
	
	private String[] idFootprint;
	private String footprint;
	
	public TaxonHierarchy() {
		children = new ArrayList<Integer>();
	}
	
	public List<Integer> getChildren() {
		return children;
	}
	
	public void setChildren(List<Integer> children) {
		this.children = children;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public String getFootprint() {
		return footprint == null ? "" : footprint;
	}
	
	public void setFootprint(String footprint) {
		this.footprint = footprint;
		this.idFootprint = footprint.split("-");
	}
	
	public String getFootprintAt(int level) {
		try {
			return idFootprint[level];
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public String toXML() {
		final StringBuilder out = new StringBuilder();
		out.append("<hierarchy>");
		out.append(XMLWritingUtils.writeTag("footprint", footprint, true));
		
		out.append("<options>");
		for (Integer child : children)
			out.append(XMLWritingUtils.writeTag("option", child.toString()));
		out.append("</options>");
		
		out.append("</hierarchy>");
		return out.toString();
	}

}
