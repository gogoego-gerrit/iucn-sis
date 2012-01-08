package org.iucn.sis.shared.conversions;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.shared.api.models.RecentlyAccessed;
import org.iucn.sis.shared.api.models.User;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class RecentlyViewedConverter extends GenericConverter<VFSInfo> {
	
	public RecentlyViewedConverter() {
		super();
		setClearSessionAfterTransaction(true);
	}

	/**
	 * Assumes that:
	 *  - Users have already been converted
	 *  - Assessments have already been converted
	 *  - SIS 1 Assessments IDs are the SIS 2 Assessment internal IDs.
	 */
	@SuppressWarnings("unchecked")
	protected void run() throws Exception {
		final UserIO userIO = new UserIO(session);
		
		final VFSPath root = new VFSPath("/users");
		final VFSPathToken file = new VFSPathToken("recentlyViewed.xml");
		
		for (VFSPathToken token : data.getOldVFS().list(root)) {
			final User user = userIO.getUserFromUsername(token.toString());
			if (user == null)
				printf("## No user exists for data directory %s ==", token);
			else {
				final Document document;
				try {
					document = BaseDocumentUtils.impl.getInputStreamFile(
						data.getOldVFS().getInputStream(root.child(token).child(file))
					);
				} catch (IOException e) {
					printf("No recently viewed file for %s, continuing...", token);
					continue;
				}
				
				printf("Processing recent assessments for %s...", token);
				
				final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
				for (Node node : nodes) {
					final String oldAsmID = node.getTextContent();
					
					/*
					 * I prefer not to wait for the entire assessment objec to be created 
					 * just to get the ID if the assessment exists, so...
					 */
					final List<Object[]> results;
					try {
						results = session.createSQLQuery(String.format(
								"SELECT id, internal_id FROM assessment WHERE internal_id = '%s'", oldAsmID))
								.list();
					} catch (Exception e) {
						continue;
					}
					
					if (results.size() == 1) {
						Object[] data = results.get(0);
						Integer id = (Integer)data[0];
						
						RecentlyAccessed accessed = new RecentlyAccessed();
						accessed.setDate(Calendar.getInstance().getTime());
						accessed.setUser(user);
						accessed.setType(RecentlyAccessed.ASSESSMENT);
						accessed.setObjectid(id);
						
						session.save(accessed);
						
						printf(" - Added Assessment %s (internal_id: %s)", id, oldAsmID);
					}
				}
			}
			
			commitAndStartTransaction();
		}
	}

}
