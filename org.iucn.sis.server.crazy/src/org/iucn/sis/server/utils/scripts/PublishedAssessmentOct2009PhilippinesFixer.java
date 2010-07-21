package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.server.io.WorkingSetIO;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.MostRecentFlagger;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.CanonicalNames;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.w3c.dom.Document;

import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class PublishedAssessmentOct2009PhilippinesFixer extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentOct2009PhilippinesFixerResource extends Resource {

		public PublishedAssessmentOct2009PhilippinesFixerResource() {
		}

		public PublishedAssessmentOct2009PhilippinesFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			boolean writeback = false;
			String wb = (String)getRequest().getAttributes().get("writeback");
			if( wb != null && wb.equalsIgnoreCase("true") )
				writeback = true;
			
			System.out.println("Writeback is " + writeback);
			try {
				if (!BasePublishedAssessmentModder.running) {
					new Thread(new PublishedAssessmentOct2009PhilippinesFixer(SISContainerApp.getStaticVFS(), writeback)).run();
					System.out.println("Started a new historian!");
				} else
					System.out.println("A published assessment script is already running!");

				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append(BasePublishedAssessmentModder.results.toString());
				sb.append("</body></html>");

				return new StringRepresentation(sb, MediaType.TEXT_HTML);
			} catch (Exception e) {
				
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append(DocumentUtils.getStackTraceAsString(e));
				sb.append("</body></html>");
				return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
			}
		}
	}

	private boolean writeback = false;
	private HashMap<String, Boolean> ids;

	public PublishedAssessmentOct2009PhilippinesFixer(File vfsRoot, boolean writeback) throws Exception {
		super(vfsRoot);
		this.writeback = writeback;
		buildIds();
	}

	public PublishedAssessmentOct2009PhilippinesFixer(VFS vfs, boolean writeback) throws Exception {
		super(vfs);
		this.writeback = writeback;
		buildIds();
	}

	private void buildIds() {
		try {
			ids = new HashMap<String, Boolean>();
			VFSPathToken [] tokens = vfs.list(new VFSPath(ServerPaths.getPublicWorkingSetFolderURL()));
			
			for( VFSPathToken curToken : tokens ) {
				if( curToken.toString().endsWith(".xml") ) {
					WorkingSetData ws = WorkingSetIO.readPublicWorkingSetAsWorkingSetData(vfs, curToken.toString().replace(".xml", ""));
					if( ws.getWorkingSetName().equals("FOR PUBLICATION_Nov 2009_Philippines Endemic Reptiles") ) {
						System.out.println("Using workingset " + ws.getWorkingSetName());
						for(String id : ws.getSpeciesIDs())
							ids.put(id, new Boolean(true));

						return;
					}
				}
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private boolean fix(AssessmentData data) {
		String evals = data.getEvaluators();
		
		if( evals != null && evals.equalsIgnoreCase("Cox, N. & Hoffmann, N. (Global Reptile Assessment Coordinating Team)") ||
				evals.equalsIgnoreCase("Cox, N. &amp; Hoffmann, N. (Global Reptile Assessment Coordinating Team)" )) {
		
			data.setFirstDataPiece(CanonicalNames.RedListEvaluators, "Cox, N. &amp; Hoffmann, M. (Global Reptile Assessment Coordinating Team)");
			return true;
		}
				
		return false;
	}
	
	@Override
	protected void parseNode(VFS vfs, Document node) {
		if( ids.remove(node.getDocumentElement().getAttribute("id")) != null )
			super.parseNode(vfs, node);
		
		if( ids.size() == 0 )
			return;
	}
	
	@Override
	protected void workOnFullList(List<AssessmentData> assessments) {
		
		List<AssessmentData> updatedHistorical = MostRecentFlagger.flagMostRecentInList(assessments);
		
		if( updatedHistorical.size() > 0 ) {
			System.out.println("Changed " + updatedHistorical.size() + " historical flags for " +
					"taxon " + updatedHistorical.get(0).getSpeciesID());
		}
		
		for( AssessmentData data : assessments ) {
			boolean changed = false;
			
			if( fix(data) )
				changed = true;
			
			if( changed ) {
				System.out.println("Assessment " + data.getAssessmentID() + " changed.");
				
				if( writeback )
					writeBackPublishedAssessment(data);
			}
		}
	}
	
	@Override
	protected void workOnHistorical(AssessmentData data) {
		//Nothing to do.
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		//Nothing to do.
	}
}
