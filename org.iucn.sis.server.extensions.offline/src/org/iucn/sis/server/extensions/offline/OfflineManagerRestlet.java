package org.iucn.sis.server.extensions.offline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.server.extensions.offline.manager.Resources;
import org.iucn.sis.shared.api.models.OfflineMetadata;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.util.Replacer;

public class OfflineManagerRestlet extends Restlet {
	
	public OfflineManagerRestlet(Context context) {
		super(context);
	}
	
	@Override
	public void handle(Request arg0, Response arg1) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(Resources.get("manager.html")));
		final StringBuilder in = new StringBuilder();
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null)
				in.append(line);
		} catch (Exception e) {
			arg1.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			return;
		}
		
		String value = in.toString();
		value = Replacer.replace(value, "$database", getDatabaseInfo());
		
		List<OfflineMetadata> list = OfflineBackupWorker.listBackups();
		StringBuilder sel = new StringBuilder();
		if (list.isEmpty())
			sel.append("No backups available.");
		else {
			Collections.sort(list, new OfflineMetadataComparator());
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm");
			sel.append("<select size=10 name=\"database\">");
			for (OfflineMetadata md : list) {
				sel.append("<option value=\"" + md.getLocation() + "\">" + md.getName() + " - " + fmt.format(md.getLastModified()) + "</option>");
			}
			sel.append("</select>");
		}
		value = Replacer.replace(value, "$backups", sel.toString());
		
		arg1.setStatus(Status.SUCCESS_OK);
		arg1.setEntity(value, MediaType.TEXT_HTML);
	}

	private String getDatabaseInfo() {
		OfflineMetadata m = OfflineBackupWorker.get();
		if (m == null)
			return "No database installed.";
		else
			return m.getName() + " last modified " + 
				new SimpleDateFormat("yyyy-MM-dd, hh:mm aa").format(m.getLastModified());
	}
	
	private static class OfflineMetadataComparator implements Comparator<OfflineMetadata> {
		
		@Override
		public int compare(OfflineMetadata o1, OfflineMetadata o2) {
			return o1.getLastModified().compareTo(o2.getLastModified()) * -1;
		}
		
	}
	
}
