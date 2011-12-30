package org.iucn.sis.shared.conversions;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.naming.NamingException;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.shared.conversions.AssessmentConverter.ConversionMode;
import org.iucn.sis.shared.helpers.ServerPaths;
import org.restlet.data.Form;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

public class ConverterWorker implements Runnable {
	
	private static final String OLD_VFS_PROPERTY = "sis_old_vfs";
	private static final String OLD_VFS_PATH_PROPERTY = "sis_old_vfs_path";
	private static final String NEW_VFS_PROPERTY = "sis_vfs";
	private static final String NEW_VFS_PATH_PROPERTY = "sis_vfs_path";
	
	public static boolean running = false;
	
	private final String step;
	private final boolean proceed;
	private final PrintWriter writer;
	private final Form parameters;
	
	protected VFS oldVFS;
	protected VFS newVFS;
	
	public ConverterWorker(PrintWriter writer, String step, boolean proceed, Form parameters) {
		this.step = step;
		this.proceed = proceed;
		this.writer = writer;
		this.parameters = parameters;
	}
	
	@Override
	public void run() {
		running = true;
		
		if (!init(proceed, writer)) {
			//arg1.setEntity(writer.toString(), MediaType.TEXT_PLAIN);
			running = false;
			return;
		}
		
		boolean success;
		if ("libraries".equals(step))
			success = convertLibrary(proceed, writer);
		else if ("definitions".equals(step))
			success = convertDefinitions(proceed, writer);
		else if ("permissions".equals(step))
			success = convertPermissions(proceed, writer);
		else if ("users".equals(step))
			success = convertUsers(proceed, writer);
		else if ("references".equals(step))
			success = convertReferences(proceed, writer);
		else if ("regions".equals(step))
			success = convertRegions(proceed, writer);
		else if ("taxa".equals(step))
			success = convertTaxa(proceed, writer);
		else if ("images".equals(step))
			success = convertImages(proceed, writer);
		else if ("draft".equals(step))
			success = convertDrafts(proceed, writer);
		else if ("published".equals(step))
			success = convertPublished(proceed, writer);
		else if ("attachments".equals(step))
			success = convertAttachments(proceed, writer);
		else if ("workingsets".equalsIgnoreCase(step))
			success = convertWorkingSets(proceed, writer);
		else if ("userworkingsets".equals(step))
			success = convertUserWorkingSets(proceed, writer);
		else {
			success = true;
			writer.write("Conversion for " + step + " complete, cascade was " + proceed);
		}
			
		if (success) {
			writer.write("++ DONE, SUCCESS");
		}
		else {
			writer.write("-- Failed, see statements for more...");
		}
		
		writer.close();
		
		running = false;
	}

	private boolean init(boolean proceed, Writer writer) {
		try {
			if (oldVFS == null) {
				newVFS = VFSFactory.getVFS(GoGoEgo.getInitProperties().getProperty(NEW_VFS_PROPERTY));
				oldVFS = VFSFactory.getVFS(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PROPERTY));
			}
		} catch (NotFoundException e) {
			die("Couldn't instantiate VFS's", e, writer);
			return false;
		}
		
		makeDirs();
		
		return true;
	}
	
	private boolean convertLibrary(boolean proceed, Writer writer) {
		LibraryGenerator converter = new LibraryGenerator();
		initConverter(converter, writer);
		converter.setData(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY));
		
		return converter.start() && (!proceed || convertDefinitions(proceed, writer));
	}
	
	private boolean convertDefinitions(boolean proceed, Writer writer) {
		DefinitionConverter converter = new DefinitionConverter();
		initConverter(converter, writer);
		converter.setData(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY));
		
		return converter.start() && (!proceed || convertPermissions(proceed, writer));
	}
	
	private boolean convertPermissions(boolean proceed, Writer writer) {
		PermissionConverter converter = new PermissionConverter();
		initConverter(converter, writer);
		converter.setData(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY));
		
		return converter.start() && (!proceed || convertUsers(proceed, writer));
	}
	
	private boolean convertUsers(boolean proceed, Writer writer) {
		UserConvertor converter = new UserConvertor();
		initConverter(converter, writer);
		
		return converter.start() && (!proceed || convertReferences(proceed, writer));
	}
	
	private boolean convertReferences(boolean proceed, Writer writer) {
		ReferenceConverter converter = new ReferenceConverter();
		initConverter(converter, writer);
		
		return converter.start() && (!proceed || convertRegions(proceed, writer));
	}
	
	private boolean convertRegions(boolean proceed, Writer writer) {
		RegionConverter converter = new RegionConverter();
		initConverter(converter, writer);
		converter.setData(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY));
		
		return converter.start() && (!proceed || convertTaxa(proceed, writer));
	}
	
	private boolean convertTaxa(boolean proceed, Writer writer) {
		TaxonConverter converter = new TaxonConverter();
		initConverter(converter, writer);
		converter.setData(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY));
		
		return converter.start() && (!proceed || convertImages(proceed, writer));
	}
	
	private boolean convertImages(boolean proceed, Writer writer) {
		TaxonImageConverter converter = new TaxonImageConverter();
		initConverter(converter, writer);
		converter.setData(new VFSInfo(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY), oldVFS, newVFS));
		
		return converter.start() && (!proceed || convertDrafts(proceed, writer));
	}
	
	private boolean convertDrafts(boolean proceed, Writer writer) {
		AssessmentConverter converter;
		try {
			converter = new AssessmentConverter();
		} catch (NamingException e) {
			die("Failed to locate lookup database", e, writer);
			return false;
		}
		initConverter(converter, writer);
		converter.setData(new VFSInfo(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY), oldVFS, newVFS));
		converter.setConversionMode(ConversionMode.DRAFT);
		
		return converter.start() && (!proceed || convertPublished(proceed, writer));
	}
	
	private boolean convertPublished(boolean proceed, Writer writer) {
		AssessmentConverter converter;
		try {
			converter = new AssessmentConverter();
		} catch (NamingException e) {
			die("Failed to locate lookup database", e, writer);
			return false;
		}
		initConverter(converter, writer);
		converter.setData(new VFSInfo(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY), oldVFS, newVFS));
		converter.setConversionMode(ConversionMode.PUBLISHED);
		
		return converter.start() && (!proceed || convertAttachments(proceed, writer));
	}
	
	private boolean convertAttachments(boolean proceed, Writer writer) {
		AttachmentConverter converter = new AttachmentConverter();
		initConverter(converter, writer);
		converter.setData(new VFSInfo(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY), oldVFS, newVFS));
		
		return converter.start() && (!proceed || convertWorkingSets(proceed, writer));
	}
	
	private boolean convertWorkingSets(boolean proceed, Writer writer) {
		WorkingSetConverter converter = new WorkingSetConverter();
		initConverter(converter, writer);
		converter.setData(new VFSInfo(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY), oldVFS, newVFS));
		
		return converter.start() && (!proceed || convertUserWorkingSets(proceed, writer));
	}
	
	private boolean convertUserWorkingSets(boolean proceed, Writer writer) {
		UserWorkingSetConverter converter = new UserWorkingSetConverter();
		initConverter(converter, writer);
		converter.setData(new VFSInfo(GoGoEgo.getInitProperties().getProperty(OLD_VFS_PATH_PROPERTY), oldVFS, newVFS));
		
		return converter.start();
	}
	
	private void initConverter(Converter converter, Writer writer) {
		converter.setWriter(writer);
		converter.setLineBreakRule("\r\n");
		converter.setParameters(parameters);
	}
	
	private void die(String message, Throwable e, Writer writer) {
		String out = message;
		if (e != null)
			out += "\r\n" + e.getMessage();
		try {
			writer.write(out);
			writer.flush();
		} catch (IOException f) {
			TrivialExceptionHandler.ignore(this, f);
		}
	}

	protected void makeDirs() {
		String[] dirs = new String[] { ServerPaths.getAssessmentRootURL(), ServerPaths.getUserRootPath(),
				ServerPaths.getTaxonRootURL() };

		for (String dir : dirs) {
			File file = new File(GoGoEgo.getInitProperties().getProperty(NEW_VFS_PATH_PROPERTY) + "/HEAD" + dir);
			if (!file.exists())
				file.mkdirs();
		}
	}
	
}
