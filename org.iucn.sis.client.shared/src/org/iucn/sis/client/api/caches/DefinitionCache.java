package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Definition;

import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class DefinitionCache {
	public static final DefinitionCache impl = new DefinitionCache();
	private static Map<String, Definition> hoverDefinitions;

	private DefinitionCache() {
		hoverDefinitions = new HashMap<String, Definition>();
		fetchDefinitions();
	}

	private void addDefinition(String name, String value) {
		Definition definition = new Definition(name, value);
		hoverDefinitions.put(clean(name), definition);
	}

	private void buildHardcodedDefinitions() {
		addDefinition(
						"population",
						"The term \"population\" is used in a specific sense in the Red List Criteria that is different to its common biological usage. Population is here defined as the total number of individuals of the taxon. For functional reasons, primarily owing to differences between life forms, population size is measured as numbers of mature individuals only.");
		addDefinition(
						"subpopulation",
						"Subpopulations are defined as geographically or otherwise distinct groups in the population between which there is little demographic or genetic exchange (typically one successful migrant individual or gamete per year or less).");
		addDefinition(
						"mature individuals",
						"The number of mature individuals is the number of individuals known, estimated or inferred to be capable of reproduction.");
		addDefinition(
						"generation",
						"Generation length is the average age of parents of the current cohort (i.e. newborn individuals in the population). Where generation length varies under threat, the more natural, i.e. pre-disturbance, generation length should be used.");
		addDefinition(
						"reduction",
						" A reduction is a decline in the number of mature individuals of at least the amount (%) stated under the criterion over the time period (years) specified, although the decline need not be continuing. A reduction should not be interpreted as part of a fluctuation unless there is good evidence for this.");
		addDefinition(
						"continuing decline",
						"A continuing decline is a recent, current or projected future decline (which may be smooth, irregular or sporadic) which is liable to continue unless remedial measures are taken. Fluctuations will not normally count as continuing declines, but an observed decline should not be considered as a fluctuation unless there is evidence for this.");
		addDefinition(
						"extreme fluctuation",
						"Extreme fluctuations can be said to occur in a number of taxa when population size or distribution area varies widely, rapidly and frequently, typically with a variation greater than one order of magnitude (i.e. a tenfold increase or decrease).");
		addDefinition(
						"severely fragmented",
						"The phrase \"severely fragmented\" refers to the situation in which increased extinction risk to the taxon results from the fact that most of its individuals are found in small and relatively isolated subpopulations.");
		addDefinition(
						"extent of occurrence",
						"Extent of occurrence is defined as the area contained within the shortest continuous imaginary boundary which can be drawn to encompass all the known, inferred or projected sites of present occurrence of a taxon, excluding cases of vagrancy. This measure may exclude large areas of obviously unsuitable habitat.");
		addDefinition(
						"eoo",
						"Extent of occurrence is defined as the area contained within the shortest continuous imaginary boundary which can be drawn to encompass all the known, inferred or projected sites of present occurrence of a taxon, excluding cases of vagrancy. This measure may exclude large areas of obviously unsuitable habitat.");
		addDefinition(
						"area of occupancy",
						"Area of occupancy is defined as the area within its \"extent of occurrence\" which is occupied by a taxon, excluding cases of vagrancy. The size of the area of occupancy will be a function of the scale at which it is measured, and should be at a scale appropriate to relevant biological aspects of the taxon, the nature of threats and the available data. However, we believe that in many cases a grid size of 2 km (a cell area of 4 km<sup>2</sup>) is an appropriate scale.");
		addDefinition(
						"aoo",
						"Area of occupancy is defined as the area within its \"extent of occurrence\" which is occupied by a taxon, excluding cases of vagrancy. The size of the area of occupancy will be a function of the scale at which it is measured, and should be at a scale appropriate to relevant biological aspects of the taxon, the nature of threats and the available data. However, we believe that in many cases a grid size of 2 km (a cell area of 4 km<sup>2</sup>) is an appropriate scale.");
		addDefinition(
						"location",
						"The term \"location\" defines a geographically or ecologically distinct area in which a single threatening event can rapidly affect all individuals of the taxon present. (Consider the most serious plausible threat if there are multiple threats.)");
		addDefinition(
						"quantitative analysis",
						"A quantitative analysis is defined here as any form of analysis which estimates the extinction probability of a taxon based on known life history, habitat requirements, threats and any specified management options. Population viability analysis (PVA) is one such technique.");
	}

	private void fetchDefinitions() {
		hoverDefinitions.clear();
		
		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
		doc.get(UriBase.getInstance().getDefinitionBase() + "/definitions",
				new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				buildHardcodedDefinitions();
				Debug.println("Could not load definitions from server...");
			}
			public void onSuccess(String result) {
				NativeNodeList list = doc.getDocumentElement().getElementsByTagName("definition");
				for (int i = 0; i < list.getLength(); i++) {
					NativeElement cur = list.elementAt(i);
					Definition definition = Definition.fromXML(cur);
					
					hoverDefinitions.put(clean(definition.getName()), definition);
				}
			}
		});
	}

	private String clean(String name) {
		return name.toLowerCase();
	}

	public Set<String> getDefinables() {
		return hoverDefinitions.keySet();
	}
	
	public List<Definition> getDefinitions() {
		List<Definition> list = new ArrayList<Definition>(hoverDefinitions.values());
		Collections.sort(list, new DefinitionComparator());
		return list;
	}

	public Definition getDefinition(String word) {
		return hoverDefinitions.get(clean(word));
	}

	public void setDefinition(String word, String definition) {
		if (!hoverDefinitions.containsKey(clean(word)))
			addDefinition(word, definition);
	}

	public void saveDefinitions(final Map<String, Definition> definitionsMap,
			final GenericCallback<String> callback) {
		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
		ndoc.post(UriBase.getInstance().getDefinitionBase() + "/definitions",
				toXML(definitionsMap), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			public void onSuccess(String result) {
				hoverDefinitions = definitionsMap;
				callback.onSuccess(result);
			};
		});
	}

	protected String toXML(Map<String, Definition> map) {
		StringBuilder xml = new StringBuilder("<definitions>");
		for (Entry<String, Definition> entry : map.entrySet())
			xml.append(entry.getValue().toXML() + "\r\n");
		xml.append("</definitions>");
		return xml.toString();
	}

	public String toXML() {
		return toXML(hoverDefinitions);
	}
	
	public static class DefinitionComparator implements Comparator<Definition> {
		
		private final PortableAlphanumericComparator comparator = 
			new PortableAlphanumericComparator();
		
		@Override
		public int compare(Definition o1, Definition o2) {
			return comparator.compare(o1.getName().toLowerCase(), o2.getName().toLowerCase());
		}
		
	}

}
