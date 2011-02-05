package org.iucn.sis.server.extensions.images;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.data.ManagedImageData;
import org.iucn.sis.shared.api.debug.Debug;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.CSVTokenizer;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class ImageRestlet extends BaseServiceRestlet {

	private AtomicBoolean running;
	private final VFS vfs;
	
	public ImageRestlet(Context context) {
		super(context);
		running = new AtomicBoolean(false);
		vfs = SIS.get().getVFS();
	}
	
	@Override
	public void definePaths() {
		paths.add("/images");
		paths.add("/images/{taxonId}");
	}
	
	@Override
	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
		final String taxonId = (String) request.getAttributes().get("taxonId");
		if (taxonId == null)
			return buildUploadHTML();
		
		final VFSPath stripedPath = 
			new VFSPath("/images/" + FilenameStriper.getIDAsStripedPath(taxonId) + ".xml");
		
		final Document document;
		if (vfs.exists(stripedPath)) {
			try {
				document = vfs.getDocument(stripedPath);
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		else
			document = 
				BaseDocumentUtils.impl.createDocumentFromString("<images id=\"" + taxonId + "\"></images>");
		
		return new DomRepresentation(MediaType.TEXT_XML, document);
	}
	
	@Override
	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final String taxonId = (String) request.getAttributes().get("taxonId");
		
		final RestletFileUpload fileUploaded = new RestletFileUpload(new DiskFileItemFactory());
		
		final List<FileItem> list;
		try {
			list = fileUploaded.parseRequest(request);
		} catch (FileUploadException e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		FileItem file = null;
		
		for (int i = 0; i < list.size() && file == null; i++) {
			FileItem item = list.get(i);
			if (!item.isFormField()) {
				file = item;
			}
		}

		if (file == null) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No file uploaded.");
		}
		else {
			String encoding = file.getContentType();
			Debug.println("Uploading image {0} encoded as {1}", file.getName(), file.getContentType());
			String extension = "";
			if (encoding.equals("image/jpeg"))
				extension = "jpg";
			else if (encoding.equals("image/gif"))
				extension = "gif";
			else if (encoding.equals("image/png"))
				extension = "png";
			else if (encoding.equals("image/tiff"))
				extension = "tif";
			else if (encoding.equals("image/bmp"))
				extension = "bmp";
			else if (taxonId == null || taxonId.equals("batch"))
				extension = "zip";
			else
				throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "Encoding of type " + encoding + " is not supported.");

			final int id;
			try {
				id = writeFile(file.get(), extension);
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
				
			if (taxonId == null || taxonId.equals("batch")) {
				if (!running.getAndSet(true)) {
					try {
						handleBatchUpload(id, response);
					} finally {
						running.set(false);
					}
				} else {
					response.setEntity(buildUploadHTML());
					response.setStatus(Status.SUCCESS_OK);
				}
			} else {
				writeXML(taxonId, String.valueOf(id), encoding, null);
			}
		}
	}

	private int writeFile(byte[] data, String encoding) throws IOException {
		Random r = new Random(new Date().getTime());
		
		int id;
		VFSPath randomImagePath;
		
		// check for file already existing
		while (vfs.exists(randomImagePath = 
			new VFSPath("/images/bin/" + (id = r.nextInt(Integer.MAX_VALUE)) + "." + encoding)));
		
		final OutputStream outStream = vfs.getOutputStream(randomImagePath);
		
		try {
			outStream.write(data);
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				outStream.close();
			} catch (Exception f) {
				TrivialExceptionHandler.ignore(this, f);
			}
		}
			
		return id;
	}

	private int writeFile(InputStream is, String encoding) throws IOException {
		Random r = new Random(new Date().getTime());
		
		int id;
		VFSPath randomImagePath;
		
		// check for file already existing
		while (vfs.exists(randomImagePath = 
			new VFSPath("/images/bin/" + (id = r.nextInt(Integer.MAX_VALUE)) + "." + encoding)));
		
		final OutputStream outStream = vfs.getOutputStream(randomImagePath);
		try {
			while(is.available()>0)
				outStream.write(is.read());
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				outStream.close();
			} catch (IOException f) {
				TrivialExceptionHandler.ignore(this, f);
			}
		}
			
		return id;
	}
	
	@Override
	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
		final String taxonId = (String) request.getAttributes().get("taxonId");
		
		final Document document;
		try {
			document = new DomRepresentation(entity).getDocument();
		} catch (Exception e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
		}
		
		try {
			DocumentUtils.writeVFSFile("/images/" + FilenameStriper.getIDAsStripedPath(taxonId) + 
				".xml", vfs, true, document);
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		response.setStatus(Status.SUCCESS_CREATED);
	}

	@SuppressWarnings("unchecked")
	private void handleBatchUpload(int id, Response response) throws ResourceException {
		StringBuilder xml = new StringBuilder();
		xml.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=cp1252\">" +
				"</head><body><div>");
		
		File tempFile;
		try {
			tempFile = vfs.getTempFile(new VFSPath("/images/bin/" + id + ".zip"));
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
			
		final ZipFile zip;
		try {
			zip = new org.apache.commons.compress.archivers.zip.ZipFile(tempFile, "Cp1252");
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final Enumeration entries = zip.getEntries();
			
		ZipArchiveEntry csv = null;
		
		final HashMap<String, Integer> filenames = new HashMap<String, Integer>();
		final HashMap<String, String> encodings = new HashMap<String, String>();
			
		while (entries.hasMoreElements()) {
			ZipArchiveEntry entry = (ZipArchiveEntry)entries.nextElement();
			if (entry.getName().endsWith(".csv")) 
				csv = entry;
			else {
				String filename = entry.getName().toLowerCase();
				String ext = filename.substring(filename.lastIndexOf(".")+1);
				
				final InputStream stream;
				try {
					stream = zip.getInputStream(entry);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				
				String encoding = "image/"+ext;
				if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
					encoding = "image/jpeg";
					ext = "jpg"; //Force it to the three letter extension
				} else if (ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")) {
					encoding="image/tiff";
					ext = "tif";
				}
				
				try {
					filenames.put(filename.replaceAll("[^a-zA-Z0-9]", ""), writeFile(stream, ext));
					encodings.put(filename.replaceAll("[^a-zA-Z0-9]", ""), encoding);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
			
		if (!csv.equals(null)) {
			final HashMap<String, ManagedImageData> map;
			try {
				map = parseCSV(zip.getInputStream(csv));
			} catch (IOException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
				return;
			}
			
			for (String key : map.keySet()) {
				if (writeXML(map.get(key).getField("sp_id"), String.valueOf(filenames.get(key)), encodings.get(key), map.get(key))){
					xml.append("<div>"+map.get(key).getField("filename")+": Success</div><br/>");
				}
				else{
					xml.append("<div>"+map.get(key).getField("filename")+": Failure</div><br/>");
				}
			}
				
			xml.append("</div></body></html>");
		
			response.setEntity(new StringRepresentation(xml.toString(), MediaType.TEXT_HTML, Language.DEFAULT, CharacterSet.UTF_8));
			response.setStatus(Status.SUCCESS_OK);
		}
	}
	
	private HashMap<String, ManagedImageData> parseCSV(InputStream csvStream) throws IOException {
		HashMap<String, ManagedImageData> data = new HashMap<String, ManagedImageData>();
		
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(csvStream, Charset.forName("Cp1252")));
		while(lineReader.ready()){
			String line = lineReader.readLine();
			
			if (line.contains("sp_id")){
				continue; //ignore xsl export headers
			}
			CSVTokenizer tokenizer = new CSVTokenizer(line);
			
			String filename = tokenizer.nextToken().toLowerCase();
			ManagedImageData img = new ManagedImageData();
			img.setField("sp_id", tokenizer.nextToken());
			
			img.setField("credit", tokenizer.nextToken());
			img.setField("source", tokenizer.nextToken());
			img.setField("caption", tokenizer.nextToken());
			
			img.setField("genus", tokenizer.nextToken());
			img.setField("species", tokenizer.nextToken());
			
			img.setField("showRedlist", "true");
			img.setField("showSIS", "true");
			img.setField("filename", filename);
			
			data.put(filename.replaceAll("[^a-zA-Z0-9]", ""), img);
		}	
		
		return data;
	}
	
	private Representation buildUploadHTML() {
		StringBuilder sb = new StringBuilder();
		if (running.get()) {
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>" +
					"A batch upload is currently running. Please try again later.</body></html>");
		} else {
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'><form method=\"post\" enctype=\"multipart/form-data\">");
			sb.append("Select .zip to upload: ");
			sb.append("<input type=\"file\" name=\"dem\" size=\"60\" style='font-family:Verdana; font-size:x-small'/>");
			sb.append("<p><input type=\"submit\" onclick=\"this.disabled=true;\" style='font-family:Verdana; font-size:x-small'/>");
			sb.append("<div>After selecting Submit, do NOT close or navigate away from this tab until you see a ");
			sb.append("final report. Depending on the size of your zip file, this could take significant time.</div>");
			sb.append("</form>");
			sb.append("</body></html>");
		}
		
		return new StringRepresentation(sb, MediaType.TEXT_HTML);
	}

	private boolean writeXML(String taxonID, String id, String encoding, ManagedImageData data) {
		if (taxonID == null || id == null || id.equals("null")) 
			return false;

		final String stripedID = FilenameStriper.getIDAsStripedPath(taxonID);
		final VFSPath stripedPath = new VFSPath("/images/" + stripedID + ".xml");
		
		boolean primary = false;
		Document xml = null;
		if (vfs.exists(stripedPath)) {
			try {
				xml = vfs.getDocument(stripedPath);
			} catch (IOException e) {
				return false;
			}
		}
		else {
			xml = DocumentUtils.createDocumentFromString("<images id=\"" + taxonID + "\"></images>");
			primary = true;
		}

		Element el = xml.createElement("image");
		el.setAttribute("id", id);
		el.setAttribute("encoding", encoding);
		el.setAttribute("primary", Boolean.toString(primary));
		
		if (data != null) {
			/*
			 * FIXME: using attributes to store captions or 
			 * any other user-entered data doesn't sound 
			 * like a recipe for success.  Should probably  
			 * wrap this information in a child CDATA node.
			 */
			if (data.containsField("caption")) 
				el.setAttribute("caption", data.getField("caption"));
			if (data.containsField("credit")) 
				el.setAttribute("credit", data.getField("credit"));
			if (data.containsField("source")) 
				el.setAttribute("source", data.getField("source"));
			if (data.containsField("showRedlist")) 
				el.setAttribute("showRedlist", data.getField("showRedlist"));
			if (data.containsField("showSIS"))
				el.setAttribute("showSIS", data.getField("showSIS"));
		}
			
		xml.getDocumentElement().appendChild(el);

		return DocumentUtils.writeVFSFile(stripedPath.toString(), vfs, true, xml);
	}
}