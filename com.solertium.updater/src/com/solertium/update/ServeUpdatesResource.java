package com.solertium.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.ElementCollection;
import com.solertium.util.restlet.RestletUtils;

/**
 * Faciliates auto-updating behavior for a Restlet-based offline application, including
 * preparing patches and updates for baseline.zip, data-repair scripts that might 
 * need to be run and application updates.
 * 
 * Every instance of an Application has a version document that follows the general 
 * organization of (Component) : (version #), e.g.
 * <ul>
 * <li>application.jar : 5</li>
 * <li>App_Launcher.exe : 5</li>
 * <li>field specifications : 2</li>
 * <li>views : 1</li>
 * <li>scripts : 1</li>
 * <li>lib : 7</li>
 * </ul>
 * 
 * This clearly tracks versions of each module; when this resource receives an
 * update request, it must receive this version document as the Entity. This
 * resource then prepares the proper updates, if anything is out of date, to
 * send back to the client. 
 * 
 * An file will be maintained on the server that provides the location and 
 * version of each component that follows the general organization of
 * (Component) : (latest version #, if applicable) : (path - a directory or file) : 
 * (version to upgrade from - * for default), e.g.
 * <ul>
 * <li>application.jar : 5 : /updates/application.jar : *</li>
 * <li>App_Launcher.exe : 5 : /updates/App_Launcher.exe : *</li>
 * <li>field specifications : 2 : /browse/docs/fields/ : *</li>
 * <li>views : 1 : /browse/docs/views.xml : *</li>
 * <li>scripts : 1 : /updates/scripts/occurrenceMigrator.jar : *</li>
 * <li>lib : 7 : /updates/lib/ : *</li>
 * </li>
 * <li>application.jar : ? : /updates/v3updates/application.jar : 3</li>
 * <li>lib : ? : /updates/v3updates/lib/ : 3</li>
 * </ul> 
 * 
 * @author adam.schwartz
 */
public class ServeUpdatesResource extends Resource {
	
	public ServeUpdatesResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.APPLICATION_ZIP));
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}
	
	@Override
	public void acceptRepresentation(Representation entity) throws ResourceException {
		ArrayList<UpdatableComponent> toBeUpdated = new ArrayList<UpdatableComponent>();
		Document doc = null;
		
		try {
			doc = new DomRepresentation(entity).getDocument();
		} catch (IOException e) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			getResponse().setEntity(new StringRepresentation("<html><head></head><body>Unable to parse " +
					"version document. Please supply a valid version document!</body></html>", MediaType.TEXT_HTML));
			RestletUtils.addHeaders(getResponse(), "Cache-control", "no-cache");
		}
		
		ArrayList<String> allComponents = ServerUpdateManager.getImpl().getAllAvailableComponents();
		ElementCollection els = new ElementCollection(doc.getElementsByTagName("component"));
		for( Element el : els ) {
			String id = el.getAttribute("id");
			String name = el.getAttribute("name");
			String version = el.getAttribute("version");

			try {
				UpdatableComponent comp = new UpdatableComponent(id, name, version);
				if( ServerUpdateManager.getImpl().updateNeeded(comp) ) {
					toBeUpdated.add(comp);
				}
				
				allComponents.remove(id);
			} catch (NumberFormatException e) {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				getResponse().setEntity(new StringRepresentation("<html><head></head><body>Version for component "
						+ name + " is not a valid integer, meaning your version document has most likely "
						+ "been corrupted. Please download a fresh copy of your Application to "
						+ "fix this issue.</body></html>", MediaType.TEXT_HTML));
				RestletUtils.addHeaders(getResponse(), "Cache-control", "no-cache");
			}
		}
		
		for( String id : allComponents )
			toBeUpdated.add(new UpdatableComponent(id, "N/A", "0") );
		
		if( toBeUpdated.isEmpty() ) {
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
			RestletUtils.addHeaders(getResponse(), "Cache-control", "no-cache");
		} else if( getRequest().getResourceRef().getPath().endsWith("summary") ) {
			getResponse().setEntity(new StringRepresentation(
					"There are " + toBeUpdated.size() + " updates available for download.", MediaType.TEXT_PLAIN));
			getResponse().setStatus(Status.SUCCESS_OK);
			RestletUtils.addHeaders(getResponse(), "Cache-control", "no-cache");
		} else {
			File temp;
			try {
				temp = File.createTempFile("update", "zip");
				if( prepareUpdate(temp, toBeUpdated) ) {
					getResponse().setEntity(new FileRepresentation(temp, MediaType.APPLICATION_ZIP));
					getResponse().setStatus(Status.SUCCESS_OK);
					RestletUtils.addHeaders(getResponse(), "Cache-Control", "no-cache");
				} else {
					getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
					RestletUtils.addHeaders(getResponse(), "Cache-Control", "no-cache");
				}
			} catch (IOException e) {
				e.printStackTrace();
				getResponse().setStatus(Status.SERVER_ERROR_INSUFFICIENT_STORAGE);
				getResponse().setEntity(new StringRepresentation("<html><head></head><body>An error has "
						+ "occurred when trying to create the update Zip file. Please report this error and "
						+ "the time to an administrator.</body></html>", MediaType.TEXT_HTML));
				RestletUtils.addHeaders(getResponse(), "Cache-Control", "no-cache");
			}
		}	
	}
	
	private boolean prepareUpdate(File temp, ArrayList<UpdatableComponent> components) throws IOException, FileNotFoundException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(temp));
		
		boolean addedSomething = false;
		
		for( UpdatableComponent curComponent : components ) {
			String targetPath = ServerUpdateManager.getImpl().getTargetPath(
					curComponent.getId(), curComponent.getCurrentVersion().toString() );
			String updatePath = ServerUpdateManager.getImpl().getUpdatePath(
					curComponent.getId(), curComponent.getCurrentVersion().toString() );
			
			if( !new File(updatePath).exists() ) {
				System.out.println("Update references the following non-existent path, so it " 
						+ "will be not be included: " + updatePath);
				continue;
			}
			
			HashMap<String, String> updatePathToTargetPath = new HashMap<String, String>();
			addFilesRecursively(updatePathToTargetPath, updatePath, targetPath);
			
			for( Entry<String, String> curPath : updatePathToTargetPath.entrySet() ) {
				File localFile = new File(curPath.getKey());

				ZipEntry entry = new ZipEntry(curPath.getValue());
				out.putNextEntry(entry);
				System.out.println("Adding " + curPath + " to the update package.");

				if( localFile.isFile() ) {
					InputStream in = new FileInputStream(localFile);
					// Transfer bytes from the file to the ZIP file
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
				}

				// Complete the entry
				out.closeEntry();
				addedSomething = true;
			}
			
			UpdatableComponent trueComponent = ServerUpdateManager.getImpl().getIdToUpdatableComponentMap().get(curComponent.getId());
			ZipEntry bookKeepingEntry = new ZipEntry((updatePath.endsWith(File.pathSeparator) ?
					updatePath.substring(0, updatePath.length()-2) : updatePath) + ".book");
			out.putNextEntry(bookKeepingEntry);
			out.write( ("<bookEntry><component id=\"" + trueComponent.getId() + "\" name=\"" 
					+ trueComponent.getComponentName() + "\" version=\"" 
					+ ServerUpdateManager.getImpl().getLatestVersion(trueComponent.getId()) + "\" /></bookEntry>").getBytes("utf8") );
			out.closeEntry();
		}
		
		if( addedSomething )
			out.close();
		
		return addedSomething;
	}
	
	private void addFilesRecursively(HashMap<String, String> files, String updatePath, String targetPath) {
		files.put(updatePath, targetPath);
		
		File file = new File(updatePath);
		String [] children = file.list();
		
		if( children != null )
			for( String curChild : children )
				addFilesRecursively(files, updatePath + "/" + curChild, 
						targetPath + "/" + curChild);
	}
}
