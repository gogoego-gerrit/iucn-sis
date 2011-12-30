package org.iucn.sis.server.extensions.migration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.restlets.TransactionResource;
import org.iucn.sis.shared.api.models.Taxon;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.util.AlphanumericComparator;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

@SuppressWarnings("deprecation")
public class ListingResource extends TransactionResource {
	
	public ListingResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);
		
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.TEXT_CSS));
	}
	
	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		if (getRequest().getResourceRef().getPath().endsWith("styles.css"))
			return new InputRepresentation(ListingResource.class.getResourceAsStream("styles.css"), MediaType.TEXT_CSS);
		
		String id = (String)getRequest().getAttributes().get("id");
		if (id == null)
			return listTaxa(variant, session);
		else
			return listAssessmentsForTaxa(id, variant, session);
	}
	
	private Representation listAssessmentsForTaxa(String id, Variant variant, Session session) throws ResourceException {
		final VFSPath root = new VFSPath("/migration/" + id);
		
		if (!SIS.get().getVFS().exists(root))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		final StringBuilder out = new StringBuilder();
		out.append("<html>");
		out.append("<head><title>Taxa with Migration Errors/Warnings</title></head>");
		out.append("<body>");
		
		final List<VFSPath> list = new ArrayList<VFSPath>();
		
		findFiles(root, list);
		
		if (!list.isEmpty()) {
			for (VFSPath uri : list) {
				String location = uri.toString().replace("migration", "reports");
				out.append("<a href=\"/apps/org.iucn.sis.server.extensions.migration/" + Application.VERSION +
						location + "\">Assessment #" + uri.getName().substring(0, uri.getName().lastIndexOf('.')) + "</a><br/>");
			}
		}
		else
			out.append("No assessments to display.");
		
		out.append("</body>");
		out.append("</html>");
		
		return new StringRepresentation(out.toString(), variant.getMediaType());
	}
	
	private void findFiles(VFSPath folder, List<VFSPath> files) throws ResourceException  {
		VFSPathToken[] tokens = null;
		try {
			tokens = SIS.get().getVFS().list(folder);
		} catch (IOException e) {
			return;
		}
		
		for (VFSPathToken token : tokens) {
			final VFSPath uri = folder.child(token);
			try {
				if (SIS.get().getVFS().isCollection(uri))
					findFiles(uri, files);
				else if (token.toString().endsWith(".html"))
					files.add(uri);
			} catch (IOException e) {
				continue;
			}
		}
	}
	
	private Representation listTaxa(Variant variant, Session session) throws ResourceException {
		final VFSPath path = new VFSPath("/migration");
		
		final StringBuilder out = new StringBuilder();
		out.append("<html>");
		out.append("<head><title>Taxa with Migration Errors/Warnings</title></head>");
		out.append("<body>");
		
		boolean empty = true;
		
		if (SIS.get().getVFS().exists(path)) {
			VFSPathToken[] tokens = null;
			try {
				tokens = SIS.get().getVFS().list(path);
			} catch (Exception e) {
				tokens = null;
			}
			
			List<Taxon> found = new ArrayList<Taxon>();
			
			for (VFSPathToken token : tokens) {
				Taxon taxon;
				try {
					taxon = SISPersistentManager.instance().getObject(session, Taxon.class, Integer.valueOf(token.toString()));
				} catch (Exception e) {
					continue;
				}
				
				if (taxon != null)
					found.add(taxon);
			}
			
			if (!found.isEmpty()) {
				empty = false;
				
				Collections.sort(found, new TaxonListComparator());
				
				for (Taxon taxon : found)
					out.append("<a href=\"/apps/org.iucn.sis.server.extensions.migration/" + Application.VERSION + "/list/" + 
						taxon.getId() + "\">" + taxon.getFullName() + " (" + taxon.getId() + ")</a><br/>");
			}
		}
		
		if (empty)
			out.append("No taxa to display.");
		
		out.append("</body>");
		out.append("</html>");
		
		return new StringRepresentation(out.toString(), variant.getMediaType());
	}
	
	private static class TaxonListComparator implements Comparator<Taxon> {
		
		private final AlphanumericComparator comparator;
		
		public TaxonListComparator() {
			comparator = new AlphanumericComparator();
		}
		
		public int compare(Taxon o1, Taxon o2) {
			return comparator.compare(o1.getFullName(), o2.getFullName());
		}
		
	}

}
