package org.iucn.sis.server.extensions.scripts.synonyms;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.SynonymCriteria;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;

public class SynonymFriendlyNameFixer {
	
	public static boolean start() {
		boolean cont = true;
		int startID = 0;
		try {
			while (cont) {
				Session session = SISPersistentManager.instance().openSession();
				session.beginTransaction();
				
				TaxonIO taxonIO = new TaxonIO(session);
				TaxonCriteria taxonCriteria = taxonIO.getCriteria();
				SynonymCriteria criteria = taxonCriteria.createSynonymsCriteria();
				criteria.id.gt(0);
				taxonCriteria.id.gt(startID);
				taxonCriteria.addOrder(Order.asc("id"));
				taxonCriteria.setMaxResults(20);
				taxonCriteria.setFirstResult(0);

				Taxon[] results = taxonIO.search(taxonCriteria);
				if (results.length == 0)
					cont = false;
				else {
					for (Taxon result : results) {
						if (result.getId() != startID) {
							
							for (Synonym s : result.getSynonyms()) {
								s.setFriendlyName(null);
								s.getFriendlyName();
							}
							
							/*String xml = result.toXML();
							String taxonPath = ServerPaths.getTaxonURL(result.getId());
							Debug.println("wrote file " + taxonPath);
							if (DocumentUtils.writeVFSFile(taxonPath, vfs, xml)) {
								startID = result.getId();
							} else {
								throw new Exception("unable to write file " + taxonPath + " : " + xml);
							}*/
						}
					}
					Debug.println("Scripted up to taxon " + startID);
					session.getTransaction().commit();
					session.beginTransaction();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Debug.println("ERROR -- " + e.getMessage());
			Debug.println("Failed, you can start again at id " + startID);
			return false;
		}
		return true;
	}

}
