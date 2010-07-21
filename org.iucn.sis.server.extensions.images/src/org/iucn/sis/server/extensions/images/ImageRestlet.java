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

import com.solertium.util.CSVTokenizer;
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
		try {
			DomRepresentation domRep = new DomRepresentation(MediaType.TEXT_XML);
			System.out.println(FilenameStriper.getIDAsStripedPath(taxonId));
			if (vfs.exists("/images/" + FilenameStriper.getIDAsStripedPath(taxonId) + ".xml")) {
				domRep.setDocument(DocumentUtils.getVFSFileAsDocument("/images/"
						+ FilenameStriper.getIDAsStripedPath(taxonId) + ".xml", vfs));
			} else {
				String xml = "<images id=\"" + taxonId + "\"></images>";
				domRep.setDocument(DocumentUtils.createDocumentFromString(xml));
			}
			response.setEntity(domRep);
			response.setStatus(Status.SUCCESS_OK);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void handlePost(Request request, Response response, String taxonId) {
		RestletFileUpload fileUploaded = new RestletFileUpload(new DiskFileItemFactory());
		
		try {
			List<FileItem> list = fileUploaded.parseRequest(request);
			FileItem file = null;
			String encoding = "";
			for (int i = 0; i < list.size() && file == null; i++) {
				FileItem item = list.get(i);

				encoding = item.getContentType();
				System.out.println(encoding);

				if (!item.isFormField()) {
					file = item;
				}
			}

			if (file == null) {
				System.out.println("file null");
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}

			else {
				String extension = "";
				if (encoding.equals("image/jpeg"))
					extension = "jpg";
				if (encoding.equals("image/gif"))
					extension = "gif";
				if (encoding.equals("image/png"))
					extension = "png";
				if (encoding.equals("image/tiff"))
					extension = "tif";
				if (encoding.equals("image/bmp"))
					extension = "bmp";
				if (taxonId == null || taxonId.equals("batch"))
					extension = "zip";

				int id = writeFile(file.get(), extension);
				
				if (taxonId == null || taxonId.equals("batch")) {
					if( !running.getAndSet(true) ) {
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

		} catch (FileUploadException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}

	}

	private int writeFile(byte[] data, String encoding){
		Random r = new Random(new Date().getTime());
		
		int id = r.nextInt(Integer.MAX_VALUE);

		// check for file already existing
		while (vfs.exists("/images/bin/" + id + "." + encoding))
			id = r.nextInt(Integer.MAX_VALUE);
		try{
			OutputStream outStream = vfs.getOutputStream("/images/bin/" + id + "." + encoding);
			outStream.write(data);
			outStream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
			
		return id;
		
	}

	private int writeFile(InputStream is, String encoding){
		Random r = new Random(new Date().getTime());
		
		int id = r.nextInt(Integer.MAX_VALUE);

		// check for file already existing
		while (vfs.exists("/images/bin/" + id + "." + encoding))
			id = r.nextInt(Integer.MAX_VALUE);
		try{
			OutputStream outStream = vfs.getOutputStream("/images/bin/" + id + "." + encoding);
			while(is.available()>0)
				outStream.write(is.read());
			outStream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
			
		return id;
		
	}
	
	private void handlePut(Request request, Response response, String taxonId) {
		try {
			DomRepresentation dom = new DomRepresentation(request.getEntity());
			DocumentUtils.writeVFSFile("/images/" + FilenameStriper.getIDAsStripedPath(taxonId) + ".xml", vfs, true,
					dom.getDocument());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleBatchUpload(int id, Response response){
		String xml="<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=cp1252\">" +
				"</head><body><div>";
		try{
			File tempFile= vfs.getTempFile(new VFSPath("/images/bin/" + id + ".zip"));
			ZipFile zip = new org.apache.commons.compress.archivers.zip.ZipFile(tempFile, "Cp1252");
			Enumeration entries;
			entries = zip.getEntries();
			ZipArchiveEntry csv = null;
			HashMap<String, Integer> filenames = new HashMap<String, Integer>();
			HashMap<String, String> encodings = new HashMap<String, String>();
			while(entries.hasMoreElements()) {
				ZipArchiveEntry entry = (ZipArchiveEntry)entries.nextElement();
				if(entry.getName().endsWith(".csv")) 
					csv = entry;
				else{
					String filename = entry.getName().toLowerCase();
					String ext = filename.substring(filename.lastIndexOf(".")+1);
					InputStream stream = zip.getInputStream(entry);
					String encoding = "image/"+ext;
					if(ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
						encoding = "image/jpeg";
						ext = "jpg"; //Force it to the three letter extension
					} else if(ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")) {
						encoding="image/tiff";
						ext = "tif";
					}
						
					filenames.put(filename.replaceAll("[^a-zA-Z0-9]", ""), writeFile(stream, ext));
					encodings.put(filename.replaceAll("[^a-zA-Z0-9]", ""), encoding);
					
					System.out.println("Putting into filenames " + filename.replaceAll("[^a-zA-Z0-9]", ""));
				}
			
			}
			
			if(!csv.equals(null)){
				HashMap<String, ManagedImageData> map = parseCSV(zip.getInputStream(csv));
				for(String key: map.keySet()){
					System.out.println("Working with key " + key + ": matching filename is " + filenames.get(key));
					if(writeXML(map.get(key).getField("sp_id"), String.valueOf(filenames.get(key)), encodings.get(key), map.get(key))){
						xml+="<div>"+map.get(key).getField("filename")+": Success</div><br/>";
					}
					else{
						xml+="<div>"+map.get(key).getField("filename")+": Failure</div><br/>";
					}
					System.out.println(xml);
				}
				
				xml+="</div></body></html>";
				response.setEntity(new StringRepresentation(xml, MediaType.TEXT_HTML, Language.DEFAULT, CharacterSet.UTF_8));
				response.setStatus(Status.SUCCESS_OK);
			}
				

			
		}
		catch (NotFoundException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		catch (IOException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}		
	}
	
	private HashMap<String, ManagedImageData> parseCSV(InputStream csvStream){
		HashMap<String, ManagedImageData> data = new HashMap<String, ManagedImageData>();
		try{
			BufferedReader lineReader = new BufferedReader(new InputStreamReader(csvStream, Charset.forName("Cp1252")));
			while(lineReader.ready()){
				String line = lineReader.readLine();
				
				if (line.contains("sp_id")){
					System.out.println("found header line. Ignoring...");
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
		}
		catch (IOException e) {
			e.printStackTrace();
			
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
		if( taxonID == null || id == null || id.equals("null") ) 
			return false;

		try {
			String stripedID = FilenameStriper.getIDAsStripedPath(taxonID);
			boolean primary = false;
			Document xml = null;
			if (vfs.exists("/images/" + stripedID + ".xml")) {
				xml = DocumentUtils.getVFSFileAsDocument("/images/" + stripedID + ".xml",
						vfs);
			} else {
				xml = DocumentUtils.createDocumentFromString("<images id=\"" + taxonID + "\"></images>");
				primary = true;
			}

			Element el = xml.createElement("image");
			el.setAttribute("id", id);
			el.setAttribute("encoding", encoding);
			el.setAttribute("primary", Boolean.toString(primary));
			if(data!=null){
				if(data.containsField("caption")) el.setAttribute("caption", data.getField("caption"));
				if(data.containsField("credit")) el.setAttribute("credit", data.getField("credit"));
				if(data.containsField("source")) el.setAttribute("source", data.getField("source"));
				if(data.containsField("showRedlist")) el.setAttribute("showRedlist", data.getField("showRedlist"));
				if(data.containsField("showSIS"))el.setAttribute("showSIS", data.getField("showSIS"));
			}
			xml.getDocumentElement().appendChild(el);

			DocumentUtils.writeVFSFile("/images/" + stripedID + ".xml", vfs, true, xml);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}