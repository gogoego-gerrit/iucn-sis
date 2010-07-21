package org.iucn.sis.client.utilities;

import java.util.ArrayList;
import java.util.Iterator;

import org.iucn.sis.shared.structures.Structure;

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
