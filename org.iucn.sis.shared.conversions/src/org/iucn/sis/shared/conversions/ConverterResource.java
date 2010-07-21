package org.iucn.sis.shared.conversions;

import java.io.File;
import java.sql.BatchUpdateException;

import org.gogoego.api.plugins.GoGoEgo;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.classic.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISApplication;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.util.restlet.RestletUtils;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

public class ConverterResource extends Restlet {

	protected int step = -1;
	protected VFS oldVFS;
	protected VFS newVFS;

	@Override
	public void handle(Request arg0, Response arg1) {

		try {
			generateVFS();
			Session session = SIS.get().getManager().getSession();
			Transaction transaction = session.beginTransaction();
			if (step == -1) {
				makeDirs();
				LibraryGenerator.generateAssessmentTypes();
				LibraryGenerator.generateInfratypes();
				LibraryGenerator.generateRelationships();
				LibraryGenerator.generateTaxonLevel();
				LibraryGenerator.generateTaxonStatus();
				LibraryGenerator.generateIsoLanguages();
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else if (step == 0) {
				PermissionConverter.convertAllPermissions();
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else if (step == 1) {
				UserConvertor.convertAllUsers();
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else if (step == 2) {
				ReferenceConverter.convertAllReferences();
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else if (step == 3) {
				RegionConverter.generateRegions();
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else if (step == 4) {
				TaxonConverter.convertAllNodes();
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else if (step == 5) {
				WorkingSetConverter.convertAllWorkingSets(oldVFS);
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else if (step == 6) {
				AssessmentConverter.convertAllDrafts(oldVFS, newVFS);
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else if (step == 7) {
				AssessmentConverter.convertAllPublished(oldVFS, newVFS);
				arg1.setEntity("Success", MediaType.TEXT_PLAIN);
			} else {
				arg1.setEntity("ALL DONE =)", MediaType.TEXT_PLAIN);
			}
			System.out.println("about to commit transaction");
			try {
				transaction.commit();
			} catch (TransactionException e) {
				System.out.println("UNABLE TO CLOSE TRANSACTION -- WAS IT ALREADY CLOSED?");
			}
			System.out.println("about to close transaction");
			step++;			
			RestletUtils.addHeaders(arg1, SISApplication.NO_TRANSACTION_HANDLE, "true");
			System.out.println("all done");

		} catch (Throwable e) {
			if (e.getCause() instanceof BatchUpdateException) {
				((BatchUpdateException) e.getCause()).getNextException().printStackTrace();
			} else {
				e.printStackTrace();
				try {
					System.out.println("\n\n\n REALLY CAUSED BY:");
					e.getCause().printStackTrace();
					System.out.println("\n\n\n REALLY REALLY CAUSED BY:");
					e.getCause().getCause().printStackTrace();
				} catch (NullPointerException e1) {

				}

			}
			arg1.setEntity(e.getMessage(), MediaType.TEXT_PLAIN);
		}

	}

	protected void generateVFS() throws NotFoundException {
		if (oldVFS == null) {
			newVFS = VFSFactory.getVFS(GoGoEgo.getInitProperties().getProperty("sis_vfs"));
			oldVFS = VFSFactory.getVFS(GoGoEgo.getInitProperties().getProperty("sis_old_vfs"));
		}

	}

	protected void makeDirs() {
		String[] dirs = new String[] { ServerPaths.getAssessmentRootURL(), ServerPaths.getUserRootPath(),
				ServerPaths.getTaxonRootURL() };

		for (String dir : dirs) {
			File file = new File(GoGoEgo.getInitProperties().getProperty("sis_vfs") + "/HEAD" + dir);
			if (!file.exists())
				file.mkdirs();
		}
	}

}
