package org.iucn.sis.server.api.io.taxomatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;

/**
 * For the lateral move, (for a species or any other rank) you choose the
 * new genus (or rank directly higher) where you would like to move the
 * species to. All of the children of the moved taxa are also moved. Besides
 * this, the information about the the moved species is unchanged.
 * 
 * Class moves the children taxa from current parent to new parent. It's 
 * important that the new parent and the old parent are of the same taxon level
 */
public class LateralMoveOperation extends TaxomaticWorker {
	
	private final TaxomaticOperation operation;
	private final Taxon parent;
	private final Collection<Taxon> children;
	
	public LateralMoveOperation(Session session, TaxomaticOperation operation) {
		super(session, TaxomaticIO.LATERAL_MOVE);
		
		this.operation = operation;
		this.parent = loadParent(operation.getInstructions());
		this.children = loadChildren(operation.getInstructions());
	}
	
	public LateralMoveOperation(Session session, User user, Taxon parent, Collection<Taxon> children) {
		super(session, TaxomaticIO.LATERAL_MOVE);
		
		TaxomaticOperation operation = new TaxomaticOperation();
		operation.setDate(new Date());
		operation.setInstructions(generateInstructions(parent, children));
		operation.setDetails(generateDetails(parent, children));
		operation.setOperation(TaxomaticIO.LATERAL_MOVE);
		operation.setUser(user);
		
		this.operation = operation;
		this.parent = parent;
		this.children = children;
	}
	
	private String generateInstructions(Taxon parent, Collection<Taxon> children) {
		StringBuilder builder = new StringBuilder();
		builder.append(parent.getId());
		for (Taxon taxon : children) {
			builder.append(',');
			builder.append(taxon.getId());
		}
		return builder.toString();
	}
	
	private String generateDetails(Taxon parent, Collection<Taxon> children) {
		StringBuilder builder = new StringBuilder();
		builder.append("Laterally moved the following taxa to " + parent.getFullName() + ": ");
		for (Iterator<Taxon> iter = children.iterator(); iter.hasNext(); ) {
			builder.append(iter.next().getFullName());
			if (iter.hasNext())
				builder.append(", ");
		}
		return builder.toString();
	}
	
	private Taxon loadParent(String instructions) {
		return taxonIO.getTaxon(Integer.valueOf(instructions.split(",")[0]));
	}
	
	private Collection<Taxon> loadChildren(String instructions) {
		Collection<Taxon> children = new ArrayList<Taxon>();
		String[] split = instructions.split(",");
		for (int i = 1; i < split.length; i++)
			children.add(taxonIO.getTaxon(Integer.valueOf(split[i])));
		
		return children;
	}

	@Override
	public String getUndoOperationName() {
		return TaxomaticIO.LATERAL_MOVE;
	}

	@Override
	public void run() throws TaxomaticException {
		Set<Taxon> changed = new HashSet<Taxon>();
		for (Taxon child : children)
			changed.addAll(taxomaticIO.moveTaxon(parent, child, operation.getUser(), Synonym.NEW, true));
		
		taxomaticIO.updateAndSave(changed, operation.getUser(), true);
		
		recordOperationHistory(changed, operation);
	}

}
