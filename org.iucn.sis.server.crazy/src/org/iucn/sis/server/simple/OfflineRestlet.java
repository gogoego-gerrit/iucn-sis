package org.iucn.sis.server.simple;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

/**
 * Handles functionality that can only be done offline
 * 
 * @author liz.schwartz
 * 
 */
public class OfflineRestlet extends ServiceRestlet {

	public OfflineRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
	}

	@Override
	public void definePaths() {
		paths.add("/offline/{action}");
	}

	@Override
	public void performService(Request request, Response response) {
		if (SISContainerApp.amIOnline) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			response
					.setEntity(
							"This operation can only be performed on offline copies of SIS.",
							MediaType.TEXT_PLAIN);
			return;
		}

		String action = (String) request.getAttributes().get("action");
		if ("clear".equalsIgnoreCase(action)) {
			performDataDeletion(request, response);
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}

	}

	protected void performDataDeletion(Request request, Response response) {
		try{

		String[] pathsToDelete = new String[] {
				ServerPaths.getPublicWorkingSetFolderURL(),
				ServerPaths.getPublishedAssessmentURL(),
				ServerPaths.getDraftAssessmentRootURL(),
				ServerPaths.getSpeciesReportPath(),
				ServerPaths.getTaxonomyByNameURL(),
				ServerPaths.getTaxonomyDocURL(),
				ServerPaths.getURLForTaxaFolder() };
			
//		String[] pathsToDelete = new String[] {
//					ServerPaths.getPublicWorkingSetFolderURL() };

		List<VFSPath> pathsToExclude = new ArrayList<VFSPath>();
		try {
			pathsToExclude.add(VFSUtils.parseVFSPath(ServerPaths
					.getURLForTaxa(TaxonomyDocUtils.getIDByName("PROTISTA",
							null))));
			pathsToExclude.add(VFSUtils
					.parseVFSPath(ServerPaths.getURLForTaxa(TaxonomyDocUtils
							.getIDByName("FUNGI", null))));
			pathsToExclude.add(VFSUtils.parseVFSPath(ServerPaths
					.getURLForTaxa(TaxonomyDocUtils
							.getIDByName("PLANTAE", null))));
			pathsToExclude.add(VFSUtils.parseVFSPath(ServerPaths
					.getURLForTaxa(TaxonomyDocUtils.getIDByName("ANIMALIA",
							null))));
			
//			pathsToExclude.add(VFSUtils.parseVFSPath(ServerPaths
//					.getURLForTaxa("100001")));
//			pathsToExclude.add(VFSUtils.parseVFSPath(ServerPaths
//					.getURLForTaxa("100002")));
//			pathsToExclude.add(VFSUtils.parseVFSPath(ServerPaths
//					.getURLForTaxa("100003")));
//			pathsToExclude.add(VFSUtils.parseVFSPath(ServerPaths
//					.getURLForTaxa("100004")));
			
			
		} catch (VFSPathParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(
					"Error occurred -- please report the following error -- "
							+ e.getLocalizedMessage(), MediaType.TEXT_PLAIN);
			return;
		}

		for (String path : pathsToDelete) {
			try {
				deleteEverything(VFSUtils.parseVFSPath(path), pathsToExclude);
			} catch (VFSPathParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				response
						.setEntity(
								"Error occurred -- please report the following error -- "
										+ e.getLocalizedMessage(),
								MediaType.TEXT_PLAIN);
				return;
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				response
						.setEntity(
								"Error occurred -- please report the following error -- "
										+ e.getLocalizedMessage(),
								MediaType.TEXT_PLAIN);
				return;
			} catch (ConflictException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				response
						.setEntity(
								"Error occurred -- please report the following error -- "
										+ e.getLocalizedMessage(),
								MediaType.TEXT_PLAIN);
				return;
			}
		}
		TaxonomyDocUtils.buildFromScratch(1);

		response.setStatus(Status.SUCCESS_OK);
		response.setEntity("All local data was deleted.", MediaType.TEXT_PLAIN);

		return;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void deleteEverything(VFSPath root, List<VFSPath> exclude)
			throws VFSPathParseException, NotFoundException, ConflictException {

		System.out.println("looking at path " + root.toString());
		if (vfs.exists(root)) {
			if (vfs.isCollection(root) && vfs.list(root).length > 0) {
				for (VFSPathToken token : vfs.list(root)) {
					VFSPath url = root.child(token);
					System.out.println("looking at " + url);
					if (!exclude.contains(url)) {
						try {
							if (vfs.isCollection(url)) {
								deleteEverything(url, exclude);
							} else {
								System.out.println("deleting url " + url);
								vfs.delete(url);
							}

						} catch (NotFoundException e) {
							System.out.println("Could not find path " + url);
							throw e;
						}
					}
				}
			} else {
				System.out.println("deleting url " + root);
				vfs.delete(root);
			}
		}
	}
}
