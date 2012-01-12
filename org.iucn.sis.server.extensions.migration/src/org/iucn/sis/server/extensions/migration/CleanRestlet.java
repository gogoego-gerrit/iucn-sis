package org.iucn.sis.server.extensions.migration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.solertium.util.Replacer;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class CleanRestlet extends BaseServiceRestlet {
	
	private static final int SIZE = 400, RANGE = 80;
	private static final VFSPath ROOT = new VFSPath("/migration");
	private static final VFSPath DIRTY = new VFSPath("/migration_dirty");
	
	private final Pattern match;
	
	public CleanRestlet(Context context) {
		super(context);
		String match;
		match = ".*<body><h1>SIS 2.0 Migration Report for " +
				".*</h1>" +
				"<h2>Errors</h2><ul><li>#### Level 4 Error: Found more data " +
				"in SIS 1 than can fit in SIS 2 for EOO.*]</li></ul></body></html>$";
		
		this.match = Pattern.compile(match);
	}
	
	@Override
	public void definePaths() {
		paths.add("/clean");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final VFS vfs = SIS.get().getVFS();
		final AtomicInteger count = new AtomicInteger(0);
		final StringBuilder out = new StringBuilder();
		
		try {
			final String queryRoot = request.getResourceRef().getQueryAsForm().getFirstValue("root");
			final VFSPath root;
			if (queryRoot == null)
				root = ROOT;
			else
				root = new VFSPath(ROOT + queryRoot);
			
			if (!vfs.exists(DIRTY))
				vfs.makeCollections(DIRTY);
			out.append("Starting move from " + root + "...\n");
			find(vfs, ROOT, out, count);
			out.append("Moved " + count + " files to dirty folder.\n");
			return new StringRepresentation(out.toString(), MediaType.TEXT_PLAIN);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	private void find(VFS vfs, VFSPath folder, StringBuilder out, AtomicInteger count) throws IOException {
		for (VFSPathToken token : vfs.list(folder)) {
			VFSPath uri = folder.child(token);
			if (vfs.isCollection(uri))
				find(vfs, uri, out, count);
			else
				process(vfs, uri, out, count);
		}
		if (vfs.list(folder).length == 0) {
			out.append("Deleting now empty folder " + folder + "\n");
			vfs.delete(folder);
		}
	}
	
	private void process(VFS vfs, VFSPath uri, StringBuilder out, AtomicInteger count) throws IOException {
		String value = Replacer.replace(vfs.getString(uri), "\n", "");
		int len = value.length();
		
		if (matches(value) && len < (SIZE + RANGE) && len > (SIZE - RANGE)) {
			vfs.move(uri, new VFSPath(DIRTY.toString() + uri.toString()));
			out.append("Moved dirty file " + uri + "\n");
			count.incrementAndGet();
		}
	}
	
	public boolean matches(String value) {
		return match.matcher(value).matches();
	}

}
