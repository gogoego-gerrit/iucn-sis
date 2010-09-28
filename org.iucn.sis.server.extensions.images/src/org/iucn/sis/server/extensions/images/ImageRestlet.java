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
import org.iucn.sis.server.api.restlets.ServiceRestlet;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.data.ManagedImageData;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.CSVTokenizer;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;

public class ImageRestlet extends ServiceRestlet {

	private AtomicBoolean running;
	
	public ImageRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		running = new AtomicBoolean(false);
	}
	
	@Override
	public void definePaths() {
		paths.add("/images");
		paths.add("/images/{taxonId}");
	}
	
	private void handleGet(Request request, Response response, String taxonId) {
		final VFSPath stripedPath = 
			new VFSPath("/images/" + FilenameStriper.getIDAsStripedPath(taxonId) + ".xml");
		
		final Document document;
		if (vfs.exists(stripedPath)) {
			try {
				document = vfs.getDocument(stripedPath);
			} catch (IOException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
				return;
			}
		}
		else
			document = 
				BaseDocumentUtils.impl.createDocumentFromString("<images id=\"" + taxonId + "\"></images>");
		
		
		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, document));
		response.setStatus(Status.SUCCESS_OK);
	}

	private void handlePost(Request request, Response response, String taxonId) {
		final RestletFileUpload fileUploaded = new RestletFileUpload(new DiskFileItemFactory());
		
		final List<FileItem> list;
		try {
			list = fileUploaded.parseRequest(request);
		} catch (FileUploadException e) {
			response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
			return;
		}
		
		FileItem file = null;
		String encoding = "";
		
		for (int i = 0; i < list.size() && file == null; i++) {
			FileItem item = list.get(i);
			if (!item.isFormField()) {
				file = item;
			}
		}

		if (file == null) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		else {
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

			int id;
			try {
				id = writeFile(file.get(), extension);
			} catch (IOException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
				return;
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
	
	private void handlePut(Request request, Response response, String taxonId) {
		final Document document;
		try {
			document = new DomRepresentation(request.getEntity()).getDocument();
		} catch (Exception e) {
			response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
			return;
		}
		
		try {
			DocumentUtils.writeVFSFile("/images/" + FilenameStriper.getIDAsStripedPath(taxonId) + ".xml", vfs, true,
					document);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		response.setStatus(Status.SUCCESS_CREATED);
	}

	@SuppressWarnings("unchecked")
	private void handleBatchUpload(int id, Response response) {
		StringBuilder xml = new StringBuilder();
		xml.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=cp1252\">" +
				"</head><body><div>");
		
		File tempFile;
		try {
			tempFile = vfs.getTempFile(new VFSPath("/images/bin/" + id + ".zip"));
		} catch (NotFoundException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			return;
		}	
			
		final ZipFile zip;
		try {
			zip = new org.apache.commons.compress.archivers.zip.ZipFile(tempFile, "Cp1252");
		} catch (IOException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			return;
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
	
	@Override
	public void performService(Request request, Response response) {
		String taxonId = (String) request.getAttributes().get("taxonId");
		if (request.getMethod() == Method.GET) {
			if( taxonId != null ) {
				handleGet(request, response, taxonId);
			} else {
				response.setEntity(buildUploadHTML());
				response.setStatus(Status.SUCCESS_OK);
			}
		} else if (request.getMethod() == Method.POST) {
			try {
				handlePost(request, response, taxonId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (request.getMethod() == Method.PUT) {
			handlePut(request, response, taxonId);
		}

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