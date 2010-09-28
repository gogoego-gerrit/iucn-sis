package org.iucn.sis.server.api.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.iucn.sis.shared.api.debug.Debug;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.solertium.vfs.BoundsException;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

/**
 * DocumentUtils.java
 * 
 * A utility class to do some of the more mundane operations.
 * 
 * @author carl.scott
 * @author adam.schwartz
 * 
 */
public class DocumentUtils {

	/**
	 * Performs the writeback of a Document to the specified URI.
	 */
	private static class WriteBackDocumentToVFSTask implements Runnable {
		private String uri;
		private VFS vfs;
		private boolean forceOverwrite;
		private Document document;

		public WriteBackDocumentToVFSTask(String uri, VFS vfs, boolean forceOverwrite, Document document) {
			this.uri = uri;
			this.vfs = vfs;
			this.forceOverwrite = forceOverwrite;
			this.document = document;
		}

		public void run() {
			if (!writeVFSFile(uri, vfs, forceOverwrite, document))
				throw new Error("Error writing back document to url " + uri);
		}
	}

	/**
	 * Performs the writeback of a String to the specified URI.
	 */
	private static class WriteBackStringTask implements Runnable {
		private String uri;
		private VFS vfs;
		private boolean forceOverwrite;
		private String fileContents;

		public WriteBackStringTask(String uri, VFS vfs, boolean forceOverwrite, String fileContents) {
			this.uri = uri;
			this.vfs = vfs;
			this.forceOverwrite = forceOverwrite;
			this.fileContents = fileContents;
		}

		public void run() {
			if (!writeVFSFile(uri, vfs, forceOverwrite, fileContents))
				throw new Error("Error writing back document to url " + uri);
		}
	}

	private static final int BACKOFF_PERIOD_MSECS = 4000;

	/**
	 * Writes a document that sends a simple confirmation message
	 * 
	 * @param confirm
	 *            the message
	 * @return the Document <response> <status>@param confirm</status>
	 *         </response>
	 */
	public static Document createConfirmDocument(String confirm) {
		if (confirm == null)
			confirm = "true";
		return createDocumentFromString("<response>\r\n<status>" + confirm + "</status>\r\n</response>");
	}

	public static Document createDocumentFromString(String xml) {
		try {
			return newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			return null;
		}

		/*
		 * Alternate code try { return
		 * DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new
		 * File()); } catch (Exception e) { return null; }
		 */
	}

	/**
	 * Creates an element with text :)
	 * 
	 * @param document
	 *            the document for which the element is to be created
	 * @param elementName
	 *            the name of the new element
	 * @param text
	 *            the text to put in the element
	 * @return a new element <@param elementName>@param text</@param
	 *         elementName>
	 * 
	 */
	public static Element createElementWithText(Document document, String elementName, String text) {
		Element element = document.createElement(elementName);
		element.appendChild(document.createTextNode(text));
		return element;
	}

	public static Document createErrorDocument(String error) {
		return createConfirmDocument(error);
	}

	/**
	 * this fail-safe method will get an attribute from a node
	 * 
	 * @param node
	 *            the node
	 * @param attribute
	 *            the name of the attribute
	 * @return the attribute, or "" if nothing there
	 */
	public static String getAttribute(Node node, String attribute) {
		try {
			String ret = node.getAttributes().getNamedItem(attribute).getTextContent();
			if (ret != null)
				return ret;
			else
				return "";
		} catch (Exception e) {
			return "";
		}
	}

	public static String getElementValue(int item, String valueToExtract, Element elementGroup) {
		return elementGroup.getElementsByTagName(valueToExtract).item(item).getFirstChild().getNodeValue();
	}

	public static String getElementValue(String valueToExtract, Element elementGroup) {
		return getElementValue(0, valueToExtract, elementGroup);
	}

	/**
	 * Gets a document from input stream
	 * 
	 * @param uri
	 *            where the file is
	 * @return the Document
	 */
	public static Document getInputStreamFile(InputStream stream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line = "";
			String xml = "";
			while ((line = reader.readLine()) != null)
				xml += line;
			return createDocumentFromString(xml);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getStackTraceAsString(Throwable e) {
		StringWriter w = new StringWriter();
		e.printStackTrace(new PrintWriter(w));
		return w.toString();
	}

	public static Transformer getTransformer() {
		try {
			Transformer dt = TransformerFactory.newInstance().newTransformer();
			dt.setOutputProperty(OutputKeys.ENCODING, Charset.forName("UTF-8").name());
			dt.setOutputProperty(OutputKeys.METHOD, "xml");
			dt.setOutputProperty(OutputKeys.INDENT, "yes");
			dt.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			return dt;
		} catch (TransformerConfigurationException tcx) {
			throw new RuntimeException(tcx);
		}
	}

	/**
	 * Gets a document from the VFS
	 * 
	 * @param uri
	 *            where the file is
	 * @return the Document
	 */
	public static Document getVFSFileAsDocument(String uri, VFS vfs) {
		try {
			if( vfs.exists(new VFSPath(uri)) )
				return newDocumentBuilder().parse(new InputSource(vfs.getInputStream(new VFSPath(uri))));
			else
				return null;
		} catch (Exception e) {
			Debug.println("DocumentUtils ERROR: getVFSFile (" + uri + "): " + e.getMessage());
//			e.printStackTrace();
			return null;
		}
	}

	public static String getVFSFileAsString(String uri, VFS vfs) {
		String content = null;
		try {
			content = vfs.getString(VFSUtils.parseVFSPath(uri));
		} catch (NotFoundException nf) {
			Debug.println("  --" + uri + " not found (probably this is ok)");
		} catch (BoundsException retryAsStream) {
			try {
				Reader in = vfs.getReader(new VFSPath(uri));
				BufferedReader buf = new BufferedReader(in);
				String line = "";
				String xml = "";
				while ((line = buf.readLine()) != null)
					xml += line;
				buf.close();
				return xml;
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException serious) {
			serious.printStackTrace();
		}

		return content;
	}

	public static Document newDocument() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (Exception e) {
			return null;
		}
	}

	public static DocumentBuilder newDocumentBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException pcx) {
			throw new RuntimeException(pcx);
		}
	}

	/**
	 * @deprecated use serializeNodeToString();
	 */
	@Deprecated
	public static String serializeDocumentToString(Document document) {
		return serializeNodeToString(document);
	}

	/**
	 * @deprecated use serializeNodeToString();
	 */
	@Deprecated
	public static String serializeElementToString(Element element) {
		return serializeNodeToString(element);
	}

	public static String serializeNodeToString(Node node) {
		try {
			StringWriter writer = new StringWriter();

			Transformer dt = getTransformer();
			dt.transform(new DOMSource(node), new StreamResult(writer));
			writer.flush();

			return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static void unversion(String uri, VFS vfs) {
		VFSMetadata md = vfs.getMetadata(new VFSPath(uri));
		if (!md.isVersioned())
			return; // already not tracking versions
		md.setVersioned(false);
		try {
			vfs.setMetadata(new VFSPath(uri), md);
		} catch (ConflictException loggedOnly) {
			System.err.println("Attempt to unversion " + uri + " blocked with ConflictException");
		}
	}

	/**
	 * Writes a document to the store's VFS, forcing an overwrite
	 * 
	 * @param uri
	 *            where the file goes
	 * @param document
	 *            the document
	 * @return true if successful, false otherwise
	 */
	public static boolean writeVFSFile(String uri, VFS vfs, boolean forceOverwrite, Document document) {
		Writer writer = null;

		try {
			Transformer dt = getTransformer();
			writer = vfs.getWriter(new VFSPath(uri));
			dt.transform(new DOMSource(document), new StreamResult(writer));
			writer.close();

			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("DocumentUtils ERROR: writeVFSFile (" + uri + ").");
			e.printStackTrace();
			return false;
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (Exception ignored) {
					ignored.printStackTrace();
				}
		}
	}

	/**
	 * Writes a document to the store's VFS, forcing an overwrite
	 * 
	 * @param uri
	 *            where the file goes
	 * @param String
	 *            the string
	 * @return true if successful, false otherwise
	 * 
	 * @author adam.schwartz
	 */
	public static boolean writeVFSFile(String uri, VFS vfs, boolean forceOverwrite, String string) {

		Writer writer = null;

		try {
			writer = vfs.getWriter(new VFSPath(uri));
			writer.write(string);
			writer.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Debug.println("DocumentUtils ERROR: writeVFSFile (" + uri + "): " + e.getMessage());
			return false;
		} finally {
			try {
				writer.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}
	}

	/**
	 * Writes a document to the store's VFS
	 * 
	 * @param uri
	 *            where the file goes
	 * @param document
	 *            the document
	 * @return true if successful, false otherwise
	 */
	public static boolean writeVFSFile(String uri, VFS vfs, Document document) {
		return writeVFSFile(uri, vfs, false, document);
	}

	/**
	 * Writes a document to the store's VFS
	 * 
	 * @param uri
	 *            where the file goes
	 * @param document
	 *            the document
	 * @return true if successful, false otherwise
	 * 
	 * @author adam.schwartz
	 */
	public static boolean writeVFSFile(String uri, VFS vfs, String string) {
		return writeVFSFile(uri, vfs, false, string);
	}

	/**
	 * Invokes writeback of a Document to the specified URI inside a Thread, so
	 * the program can return immediately, without waiting for the write to
	 * finish.
	 * 
	 * WARNING: This function DOES NOT perform locking, or concurrent
	 * modification detection. Assume that calling this function in rapid
	 * succession with the same target uri WILL result in non-deterministic
	 * output.
	 * 
	 * @param uri
	 * @param vfs
	 * @param forceOverwrite
	 * @param document
	 */
	public static void writeVFSFileLazily(String uri, VFS vfs, boolean forceOverwrite, Document document) {
		new Thread(new WriteBackDocumentToVFSTask(uri, vfs, forceOverwrite, document)).start();
	}

	/**
	 * Invokes writeback of a string to the specified URI inside a Thread, so
	 * the program can return immediately, without waiting for the write to
	 * finish.
	 * 
	 * WARNING: This function DOES NOT perform locking, or concurrent
	 * modification detection. Assume that calling this function in rapid
	 * succession with the same target uri WILL result in non-deterministic
	 * output.
	 * 
	 * @param uri
	 * @param vfs
	 * @param forceOverwrite
	 * @param document
	 */
	public static void writeVFSFileLazily(String uri, VFS vfs, boolean forceOverwrite, String fileContents) {
		try {
			new Thread(new WriteBackStringTask(uri, vfs, forceOverwrite, fileContents)).start();
		} catch (Exception e) {
			try {
				Debug.println(
						"Detected congestion ... backing off for writing " + BACKOFF_PERIOD_MSECS);
				Thread.sleep(BACKOFF_PERIOD_MSECS);
				new Thread(new WriteBackStringTask(uri, vfs, forceOverwrite, fileContents)).start();
			} catch (Exception e1) {
				try {
					Debug.println(
							"Detected congestion again ... backing off for writing " + BACKOFF_PERIOD_MSECS);
					Thread.sleep(BACKOFF_PERIOD_MSECS);
					new Thread(new WriteBackStringTask(uri, vfs, forceOverwrite, fileContents)).start();
				} catch (InterruptedException e2) {
					Debug.println("No luck with the writeback. Bailing.");
				}
			}
		}
	}

	/**
	 * Invokes writeback of a Document to the specified URI inside a Thread, so
	 * the program can return immediately, without waiting for the write to
	 * finish.
	 * 
	 * WARNING: This function DOES NOT perform locking, or concurrent
	 * modification detection. Assume that calling this function in rapid
	 * succession with the same target uri WILL result in non-deterministic
	 * output.
	 * 
	 * @param uri
	 * @param vfs
	 * @param document
	 */
	public static void writeVFSFileLazily(String uri, VFS vfs, Document document) {
		writeVFSFileLazily(uri, vfs, false, document);
	}

	/**
	 * Invokes writeback of a string to the specified URI inside a Thread, so
	 * the program can return immediately, without waiting for the write to
	 * finish.
	 * 
	 * WARNING: This function DOES NOT perform locking, or concurrent
	 * modification detection. Assume that calling this function in rapid
	 * succession with the same target uri WILL result in non-deterministic
	 * output.
	 * 
	 * @param uri
	 * @param vfs
	 * @param fileContents
	 *            as a String
	 */
	public static void writeVFSFileLazily(String uri, VFS vfs, String fileContents) {
		writeVFSFileLazily(uri, vfs, false, fileContents);
	}
	
	public static String getDisplayString(String text) {
		if (text == null)
			return "";
		text = text.replaceAll("\\Q\\n\\E", " ");
		return text.trim();
	}

	public DocumentUtils() {
	}
}
