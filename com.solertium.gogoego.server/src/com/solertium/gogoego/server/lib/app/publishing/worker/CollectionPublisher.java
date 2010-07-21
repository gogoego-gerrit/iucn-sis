/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.gogoego.server.lib.app.publishing.worker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.gogoego.api.collections.CategoryData;
import org.gogoego.api.collections.CollectionCache;
import org.gogoego.api.collections.CollectionResourceBuilder;
import org.gogoego.api.collections.CustomFieldData;
import org.gogoego.api.collections.GenericCollection;
import org.gogoego.api.collections.GenericItem;
import org.gogoego.api.collections.GoGoEgoCollection;
import org.gogoego.api.collections.GoGoEgoItem;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.app.importer.worker.ImportMode;
import com.solertium.gogoego.server.lib.app.importer.worker.ZipImporter;
import com.solertium.gogoego.server.lib.caching.MemoryCache;
import com.solertium.util.BaseTagListener;
import com.solertium.util.SysDebugger;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.TagFilter.Tag;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;

public class CollectionPublisher {
	
	private static final VFSPath INSTRUCTIONS_PATH = new VFSPath("/(SYSTEM)/publish/instructions.properties");
	
	private final Context context;
	private final VFS vfs;
	private final CollectionResourceBuilder builder;
	
	private ZipOutputStream os;
	private Properties instructions;
	
	public CollectionPublisher(Context context) {
		this.context = context; 
		vfs = GoGoEgo.get().getFromContext(context).getVFS();
		builder = new CollectionResourceBuilder(context, true);
	}
	
	public Document importCollection(final InputStream zip) throws ResourceException {
		final ZipInputStream zis;
		try {
			zis = new ZipInputStream(zip);
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		final Properties instructions = new Properties();
		
		/**
		 * This will only put the files there, but it will not 
		 */
		final ZipImporter importer = new ZipImporter(vfs);
		importer.restrict(INSTRUCTIONS_PATH);
		importer.setRestrictionListener(new ZipImporter.InputStreamListener() {
			public void handle(VFSPath uri, InputStream is) {
				try {
					instructions.load(is);
					System.out.println("Loading instructions from " + uri);
					instructions.list(System.out);
					System.out.println("Loaded instructions from " + uri);
					instructions.setProperty("loaded", "true");
				} catch (IOException e) {
					TrivialExceptionHandler.ignore(this, e);
					e.printStackTrace();
				}
			}
		});
		final Document results = importer.doImport(zis, ImportMode.OVERWRITE);
		
		if (!"true".equals(instructions.getProperty("loaded", "false")))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, 
					"Files imported, but instructions not loaded.  Will not append collections.");
		
		if (instructions.getProperty("uri") == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, 
					"Invalid properties loaded, no uri specified, stopping...");
		
		final VFSPath path = new VFSPath(instructions.getProperty("path"));
		final String id = instructions.getProperty("id");
		final VFSPath parentURI = new VFSPath(instructions.getProperty("parent"));
		final String protocol = instructions.getProperty("protocol");
		
		final CategoryData parent = builder.getCurrentCategory(parentURI);
		if (parent == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, 
				"Parent category does not exist, can not add " + protocol);
		
		if ("category".equals(protocol) && !parent.getCategory().getSubCollections().containsKey(id)) {
			try {
				builder.createSubcollectionInCollection(parent, vfs.getDocument(path));
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed due to unexpected server error trying to add collection category.", e);
			}
		}
		else if ("item".equals(protocol) && !parent.getCategory().getItems().containsKey(id)) {
			try {
				builder.createItemInCollection(vfs.getDocument(path), parent, true);
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed due to unexpected server error trying to add collection item.", e);
			}
		}
		
		handleImportResults(results, instructions);
		
		return results;
	}
	
	/**
	 * Remove information that may be cached.
	 * @param document
	 * @param instructions
	 */
	private void handleImportResults(Document document, Properties instructions) {
		CollectionCache.getInstance().invalidateInstance(context);
		MemoryCache.getInstance().clear(context);
	}
	
	public FileRepresentation publish(final VFSPath uri) throws ResourceException {
		final File zipFile; 
		try {
			final Calendar date = Calendar.getInstance();
			os = new ZipOutputStream(new FileOutputStream(zipFile = File.createTempFile("site-"
					+ new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()), ".zip")));
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final CategoryData categoryData = builder.getCurrentCategory(uri);
		if (categoryData == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		final Properties instructions = new Properties();
		instructions.setProperty("uri", uri.toString());
		instructions.setProperty("file", zipFile.getAbsolutePath());
		
		final GenericCollection category = (GenericCollection)categoryData.getCategory();
		if (categoryData.getItemID() != null) {
			instructions.setProperty("protocol", "item");
			instructions.setProperty("parent", category.getCollectionAccessURI());
			instructions.setProperty("id", categoryData.getItemID());
			instructions.setProperty("path", category.getCollectionFileLocation().getCollection() + 
					"/" + categoryData.getItemID() + ".xml");
			processItem(category, categoryData.getItemID());
		}
		else {
			instructions.setProperty("protocol", "category");
			instructions.setProperty("parent", category.getParent().getCollectionAccessURI());
			instructions.setProperty("id", category.getCollectionID());
			instructions.setProperty("path", category.getCollectionFileLocation().toString());
			processCategory(category);
		}
		
		final ZipEntry entry = new ZipEntry(INSTRUCTIONS_PATH.toString());
		try {
			os.putNextEntry(entry);
			instructions.store(os, null);
			os.closeEntry();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		try {
			os.close();
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		if (instructions.getProperty("file") == null)
			instructions.setProperty("file", zipFile.getAbsolutePath());
		
		this.instructions = instructions;
		
		return new FileRepresentation(zipFile, MediaType.APPLICATION_ZIP);
	}
	
	public Properties getInstructions() {
		return instructions;
	}
	
	private void processCategory(GoGoEgoCollection category) throws ResourceException {
		write("Adding " + category.getCollectionAccessURI());
		try {
			addToZip(category.getCollectionFileLocation());
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not export " + category.getCollectionAccessURI(), e);
		}
		for (String itemID : category.getItems().keySet()) {
			processItem(category, itemID);
		}
		for (String categoryID : category.getSubCollections().keySet()) {
			CategoryData subcollection = 
				builder.getCurrentCategory(new VFSPath(category.getCollectionAccessURI() + "/" + categoryID));
			processCategory(subcollection.getCategory());
		}
	}
	
	private void processItem(GoGoEgoCollection parent, String itemID) throws ResourceException {
		final GoGoEgoItem possibleItem = parent.getItems().get(itemID);
		if (!(possibleItem instanceof GenericItem))
			return;
		
		final VFSPath itemPath =
			parent.getCollectionFileLocation().getCollection().child(new VFSPathToken(itemID + ".xml"));
		
		try {
			possibleItem.convertFromXMLDocument(vfs.getDocument(itemPath));
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not find item that was reported to be there.");
		}
		
		write("- Adding " + possibleItem.getItemID());
		try {
			addToZip(itemPath);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not export " + itemPath, e);		
		}
		final GenericItem item = (GenericItem)possibleItem;
		for (CustomFieldData customFieldData : item.getAllData().values()) {
			if ("file".equalsIgnoreCase(customFieldData.getType()) || 
					"image".equalsIgnoreCase(customFieldData.getType())) {
				final VFSPath uri;
				try {
					uri = VFSUtils.parseVFSPath(customFieldData.getValue());
				} catch (VFSUtils.VFSPathParseException e) {
					continue;
				}
				
				try {
					addToZip(uri);
				} catch (IOException e) {
					continue;
				}
			}
            if ("html".equalsIgnoreCase(customFieldData.getType())){
                String html = customFieldData.getValue();
                System.out.println("Examining HTML: [["+html+"]]");
                TagFilter tf = new TagFilter(new StringReader(html));
                TagFilter.Listener imgListener = new BaseTagListener(){
                    public void process(Tag t) throws IOException {
                        if("img".equalsIgnoreCase(t.name)){
                                String src = t.getAttribute("src");
                                if(src!=null && src.startsWith("/")){
                                        final VFSPath uri = VFSUtils.parseVFSPath(src);
                                        if(uri!=null){
                                            System.out.println("Found img src="+uri);
                                        	addToZip(uri);
                                        }
                                }
                        }
                    }

	                public List<String> interestingTagNames() {
	                        // TODO Auto-generated method stub
	                        List<String> interestingTagNames = new ArrayList<String>();
	                        interestingTagNames.add("img");
	                        return interestingTagNames;
	                }
                };
                tf.registerListener(imgListener);
                try{
                        tf.parse();
                } catch (IOException iox) {}
        }
      }
	}
	
	private void write(String out) {
		SysDebugger.out.println(out);
	}
	
	private void addToZip(VFSPath uri) throws IOException {
		if (!vfs.exists(uri))
			return;
		
		byte[] buff = new byte[18024];
		
		VFSMetadata md = null;
		try {
			md = vfs.getMetadata(uri);
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}

		final InputStream in = vfs.getInputStream(uri);
		final ZipEntry entry = new ZipEntry(uri.toString());
		if (md != null) {
			try {
				entry.setTime(md.getLastModified());
				entry.setSize(md.getLength());
			} catch (Exception e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		os.putNextEntry(entry);
		int len;
		while ((len = in.read(buff)) > 0)
			os.write(buff, 0, len);
		os.closeEntry();
		in.close();
		
		write("Add successful for " + uri);
	}
	
}
