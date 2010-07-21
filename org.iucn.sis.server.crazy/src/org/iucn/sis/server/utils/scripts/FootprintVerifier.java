package org.iucn.sis.server.utils.scripts;

import java.util.ArrayList;

import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.utils.SISMailer;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.mail.Mailer;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.VFS;

public class FootprintVerifier {

	private static StringBuilder builder;
	private static int count;
	private static Mailer mailer;

	public static void verifyFootprints(VFS vfs) {
		builder = new StringBuilder();
		count = 0;
		mailer = SISMailer.getGMailer();

		Document taxonomyDoc = TaxonomyDocUtils.getTaxonomyDocByID();

		NodeCollection kingdoms = new NodeCollection(taxonomyDoc.getDocumentElement().getChildNodes());
		for( Node kingdom : kingdoms ) {
			if( kingdom.getNodeType() != Node.ELEMENT_NODE )
				continue;
			Element el = (Element)kingdom;
			String id = el.getNodeName().replace("node", "");

			TaxonNode taxon = TaxaIO.readNode(id, vfs);
			ArrayList<String> footprint = new ArrayList<String>();
			footprint.add(taxon.getName());

			recurseCheckFootprint(el, footprint, vfs);
		}
		
		mailer.setBody("Results of footprint verifier run: " + builder.toString());
		mailer.setTo("adam.schwartz@solertium.com");
		mailer.setFrom("sis@iucnsis.org");
		mailer.setSubject("Footprint Verifier Run");
		try {
			mailer.send();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error sending footprint verifier results.");
		}
	}

	private static void recurseCheckFootprint(Element taxonEl, ArrayList<String> footprint, VFS vfs) {
		count++;
		if( count % 1000 == 0 )
			System.out.println("Through " + count + " taxa");

		NodeCollection children = new NodeCollection(taxonEl.getChildNodes());
		for( Node cur : children ) {
			if( cur.getNodeType() != Node.ELEMENT_NODE )
				continue;
			Element el = (Element)cur;
			String id = el.getNodeName().replace("node", "");

			try {
				TaxonNode child = TaxaIO.readNode(id, vfs);
				boolean writeback = false;
				for( int i = 0; i < child.getFootprint().length; i++ ) {
					if( !child.getFootprint()[i].equalsIgnoreCase(footprint.get(i)) ) {
//						builder.append("Taxon " + child.getId() + ": footprint is " + child.getFootprintAsString() + 
//								" and should be " + footprint.toString() + "\n");
						System.out.println("Taxon " + child.getId() + ": footprint is " + child.getFootprintAsString() + 
								" and should be " + footprint.toString() + "\n");
						child.getFootprint()[i] = footprint.get(i);
						writeback = true;
					}
				}
				if( writeback )
					TaxaIO.writeNode(child, vfs);

				ArrayList<String> fprint = new ArrayList<String>(footprint);
				fprint.add(child.getName());

				recurseCheckFootprint(el, fprint, vfs);
			} catch (NullPointerException e) {
				System.out.println("Taxon " + id + " doesn't actually exist.");
			}
		}
	}
}
