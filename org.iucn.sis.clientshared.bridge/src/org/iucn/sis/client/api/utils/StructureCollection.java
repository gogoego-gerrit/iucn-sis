package org.iucn.sis.client.api.utils;

import java.util.ArrayList;
import java.util.Iterator;

import org.iucn.sis.shared.api.structures.Structure;

public class StructureCollection extends ArrayList {

	public StructureCollection() {
		super();
	}

	public Structure getStructure(int index) {
		return (Structure) get(index);
	}

	@Override
	public Iterator iterator() {
		return listIterator();
	}

}
