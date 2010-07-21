package com.solertium.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.solertium.util.CurrentBinary;

public class UpdateResource extends Resource {

	public UpdateResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	private boolean updateBookkeeping(Document bookEntry) {
		return PerformUpdates.impl.updateBookkeeping(bookEntry);
	}

	@Override
	public Representation represent(final Variant variant) {
		if( getRequest().getResourceRef().getPath().endsWith("summary") ) {
			Representation rep = PerformUpdates.impl.getUpdateSummary(getContext().getClientDispatcher());
			if( rep == null ) {
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return new StringRepresentation("No updates available.", MediaType.TEXT_PLAIN);
			}
			else {
				getResponse().setStatus(Status.SUCCESS_OK);
				return rep;
			}
		} else {
			try {
				Representation rep = PerformUpdates.impl.checkForUpdates(getContext().getClientDispatcher());
				if( rep == null ) {
					File updates = new File(CurrentBinary.getDirectory(this), "updates");
					File updatesZip = new File(updates, "updates.zip");

					if( !updatesZip.exists() ) {
						getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
						return new StringRepresentation(
								"<html><head><body>No updates required!</body></html>",
								MediaType.TEXT_HTML);
					}

					ZipInputStream in = new ZipInputStream(new FileInputStream(updatesZip));
					ZipEntry curEntry = in.getNextEntry();

					while( curEntry != null ) {
						if( curEntry.getName().endsWith(".book") ) {
							System.out.println("Found a bookkeeping entry: " + curEntry.getName());

							try {
								BufferedReader b = new BufferedReader(new InputStreamReader(in, "utf8"));
								StringBuffer bookEntryStr = new StringBuffer();
								String temp;
								while ((temp = b.readLine()) != null) {
									bookEntryStr.append(temp);
								}

								Document bookEntry = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
										new InputSource(new StringReader(bookEntryStr.toString())));
								if( !updateBookkeeping(bookEntry) )
									throw new Exception("OH NO! FAIL!");
							} catch (Exception e) {
								e.printStackTrace();
								if( e.getCause() != null )
									e.getCause().printStackTrace();

								getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
								return new StringRepresentation(
										"<html><head><body>Update unsuccessful!<p/>" +
										"Failure to update my_configuration.properties. " +
										"Please ensure you have proper permissions to write " +
										"to your file system.</body></html>",
										MediaType.TEXT_HTML);
							}

							in.closeEntry();
						} else {
							//write out file!
							try {
								File outFile = new File(curEntry.getName());
								outFile.mkdirs();

								if( !curEntry.isDirectory() ) {
									if( outFile.exists() )
										outFile.delete();

									outFile.createNewFile();

									FileOutputStream out = new FileOutputStream(outFile);

									// Transfer bytes from the file to the ZIP file
									byte[] buf = new byte[1024];
									int len;
									while ((len = in.read(buf)) > 0) {
										out.write(buf, 0, len);
									}
									in.closeEntry();
									out.close();
								}
							} catch (Throwable e) {
								e.printStackTrace();
								System.out.println("ERROR trying to write file " + curEntry.getName());
							}
						}

						curEntry = in.getNextEntry();
					}

					in.close();
					updatesZip.delete();
					updates.delete();

					try {
						new Thread(new Runnable() {
							public void run() {
								try {
									Thread.sleep(5000);
								} catch (InterruptedException unlikely) {
								}
								// crash out of SIS and let wrapper restart it
								System.exit(42);
							}
						}).start();
					} catch (Throwable e) {
						System.out.println("ERROR spawning thread to exit the system.");
						getResponse().setStatus(Status.SUCCESS_OK);
						return new StringRepresentation(
								"<html><head><body>Update successful!<p/>SIS was unable to "
								+ "restart itself. Please exit the Toolkit via the SIS icon "
								+ "in your toolbar, then re-launch the "
								+ "application as usual.</body></html>",
								MediaType.TEXT_HTML);
					}					

					getResponse().setStatus(Status.SUCCESS_OK);
					return new StringRepresentation(
							"<html><head><body>Update successful!<p/>Restarting SIS.... You MUST close this browser window.</body></html>",
							MediaType.TEXT_HTML);
				} else {
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
					return rep;
				}
			} catch (IOException e) {
				e.printStackTrace();
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return new StringRepresentation(
						"<html><head><body>Update unsuccessful!<p/>Please check your Internet connection.</body></html>",
						MediaType.TEXT_HTML);
			}
		}
	}
}

