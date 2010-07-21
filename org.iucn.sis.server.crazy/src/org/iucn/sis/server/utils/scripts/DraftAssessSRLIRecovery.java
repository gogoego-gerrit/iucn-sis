package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.HashMap;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.vfs.VFS;
import com.solertium.vfs.provider.VersionedFileVFS;

public class DraftAssessSRLIRecovery extends BaseDraftAssessmentModder {

	public static class DraftAssessSRLIRecoveryResource extends Resource {

		public DraftAssessSRLIRecoveryResource() {
		}

		public DraftAssessSRLIRecoveryResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			if (!BaseDraftAssessmentModder.running) {
				String wsID = (String) getRequest().getAttributes().get("wsID");
				String writeback = (String)getRequest().getAttributes().get("writeback");
				String wsURL = null;

				if (wsID != null) {
					wsURL = "/workingsets/" + wsID + ".xml";
					new Thread(new DraftAssessSRLIRecovery(SISContainerApp.getStaticVFS(), wsURL,
							writeback != null && writeback.equalsIgnoreCase("true") )).run();
					System.out.println("Started a new draft publisher!!");
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
					sb.append("You must supply a working set ID.");
					sb.append("</body></html>");

					return new StringRepresentation(sb, MediaType.TEXT_HTML);
				}

			} else
				System.out.println("A draft assessment script is already running!");

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("DraftAssessFinalCarolineMods is running...");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	protected boolean doWriteback = false;
	
	private String [] nameArray = new String [] {"Aciagrion tillyardi", "Agriocnemis lacteola", "Anax nigrofasciatus", "Ceriagrion auranticum", "Coeliccia flavicauda", "Crocothemis servilia", "Heliocypha perforata", "Matrona basilaris", "Neurobasis chinensis", "Onychargia atrocyana", "Onychothemis testacea", "Tramea basilaris", "Tramea virginia", "Trithemis festiva", "Aeshna petalura", "Anisopleura lestoides", "Bayadera indica", "Brachydiplax sobrina", "Calicnemia mukherjee", "Caliphaea confusa", "Cercion malayanum", "Ceriagrion azureum", "Ceriagrion coromandelianum", "Coeliccia didyma", "Coeliccia rotundata ", "Coeliccia sarbottama", "Coeliccia schmidti", "Cratilla metallica", "Davidius zallorensis", "Elattoneura atkinsoni", "Gomphidia williamsoni", "Gynacanthaeschna sikkima", "Indolestes cyaneus", "Lestes garoensis", "Lyriothemis acigastra", "Lyriothemis bivittata", "Macrogomphus seductus", "Megalestes lieftincki", "Megalestes raychoudhurii", "Megalogomphus flavicolor", "Neallogaster hermionae", "Neurothemis tullia", "Oligoaeschna martini", "Onychogomphus cacharicus", "Onychogomphus grammicus", "Orolestes durga", "Orthetrum glaucum", "Palpopleura sexmaculata", "Periaeschna magdalena", "Periaeschna unifasciata", "Philoganga montana", "Polycanthagyna erythromelas", "Pseudagrion australasiae", "Rhodothemis rufa", "Schmidtiphaea schmidi", "Vestalis smaragdina", "Aciagrion hisopa", "Calicnemia miles", "Calicnemia uenoi", "Caliphaea thailandica", "Cephalaeschna aritai", "Chlorogomphus auratus", "Coeliccia satoi", "Coeliccia uenoi", "Heliocypha biforata", "Macrogomphus annulatus", "Microgomphus jurzitzai", "Mnais andersoni", "Orientogomphus circularis", "Planaeschna tomokunii", "Protosticta khaosoidaoensis", "Rhinocypha seducta", "Tetrathemis platyptera", "Vestalis gracilis"};
	private HashMap<String, String> names;
	
	public DraftAssessSRLIRecovery(File vfsRoot, String workingSetURL, boolean writeback) {
		super(vfsRoot, null, false);
		names = new HashMap<String, String>();
		for( String cur : nameArray )
			names.put( cur.trim().toLowerCase(), cur );
	}

	public DraftAssessSRLIRecovery(VFS vfs, String workingSetURL, boolean writeback) {
		super(vfs, null, false);
		this.doWriteback = writeback;
		System.out.println("WRite back is " + doWriteback);
		
		names = new HashMap<String, String>();
		for( String cur : nameArray )
			names.put( cur.trim().toLowerCase(), cur );
	}

	@Override
	protected void workOnAssessment(AssessmentData data, TaxonNode node) {
		VersionedFileVFS vvfs = (VersionedFileVFS)vfs; 
		
		if( names.containsKey(node.getFullName().trim().toLowerCase()) && node.getFootprint()[0].equalsIgnoreCase("ANIMALIA") ) {
			System.out.println("***" + node.getId());
//			VFSPath uri = new VFSPath(ServerPaths.getPathForGlobalDraftAssessment(data.getAssessmentID()));
//			List<String> revIDs = vvfs.getRevisionIDsBefore(uri, null, 5);
//			if( revIDs.size() == 0 )
//				System.out.println("No revisions for assessment " + node.getFullName());
//			
//			for( String curRevID : revIDs ) {
//				try {
//				String assessment = ByteUtils.toString(vvfs.getInputStream(uri, curRevID));
//				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
//				ndoc.parse(assessment);
//				
//				AssessmentData curAssessment = new AssessmentParser(ndoc).getAssessment();
//				if( curAssessment.getDataMap().size() > data.getDataMap().size() ) {
//					if( revIDs.get(0).equals(curRevID) )
//						System.out.println("Restoring latest version " + curRevID + " of the draft assessment " + 
//								curAssessment.getAssessmentID() + ".");
//					else
//						System.out.println("Restoring prior version " + curRevID + " of the draft assessment " + 
//							curAssessment.getAssessmentID() + ".");
//					
//					if( doWriteback )
//						writeBackDraftAssessment(curAssessment);
//					
//					break;
//				} else {
//					System.out.println("The prior version " + curRevID + " of the draft assessment " + 
//						curAssessment.getAssessmentID() + " doesn't have more data than the most recent.");
//				}
//				} catch (NotFoundException e) {
//					e.printStackTrace();
//				}
//			}
		}
			
	}
}
