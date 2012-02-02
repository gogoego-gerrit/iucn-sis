package org.iucn.sis.shared.api.models;

import java.util.ArrayList;
import java.util.List;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
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
		hierarchy.setTaxon(taxon);
		hierarchy.setFootprint(taxon.getIDFootprintAsString(0, "-"));
		
		final List<Taxon> list = new ArrayList<Taxon>();
		for (Taxon child : taxon.getChildren())
			if (Taxon.ACTIVE == child.getState())
				list.add(child);
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
			else if (Taxon.ROOT_TAG.equals(node.getNodeName()))
				hierarchy.setTaxon(Taxon.fromXML((NativeElement)node));
			else if ("children".equals(node.getNodeName())) {
				final List<Taxon> list = new ArrayList<Taxon>();
				
				final NativeNodeList children = node.getChildNodes();
				for (int k = 0; k < children.getLength(); k++) {
					final NativeNode child = children.item(k);
					if (Taxon.ROOT_TAG.equals(child.getNodeName()))
						list.add(Taxon.fromXMLminimal((NativeElement)child));
				}
				
				hierarchy.setChildren(list);
			}
		}
		
		return hierarchy;
	}
	
	private List<Taxon> children;
	private String footprint;
	private String[] idFootprint;
	private Taxon taxon;
	
	public TaxonHierarchy() {
		children = new ArrayList<Taxon>();
	}
	
	public List<Taxon> getChildren() {
		return children;
	}
	
	public void setChildren(List<Taxon> children) {
		this.children = children;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public Taxon getTaxon() {
		return taxon;
	}
	
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
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
		if (taxon != null)
			out.append(taxon.toXML());
		out.append("<children>");
		for (Taxon child : children)
			out.append(child.toXMLMinimal());
		out.append("</children>");
		
		out.append("</hierarchy>");
		return out.toString();
	}

}
