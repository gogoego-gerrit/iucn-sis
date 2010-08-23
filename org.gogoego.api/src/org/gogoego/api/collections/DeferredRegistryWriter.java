package org.gogoego.api.collections;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.vfs.VFS;
import com.solertium.vfs.utils.VFSUtils;

/**
 * DeferredRegistryWriter.java
 * 
 * This writer will defer writing all updates to file until 
 * the persistDocument function is explicitly called.  Any 
 * intermediate updates will be stored in a local copy of 
 * the registry document, but not written to file.  This is 
 * useful when making batch changes or any situation where  
 * many updates are being done via script, as to avoid file 
 * collisions. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class DeferredRegistryWriter extends RegistryWriter {
	
	private final Document document;
	
	public DeferredRegistryWriter(VFS vfs, String path) {
		super(vfs, path);
		
		Document document;
		try {
			document = DocumentUtils.getReadWriteDocument(VFSUtils.parseVFSPath(path), vfs);
		} catch (IOException e) {
			document = null;
		}
		if (document == null)
			document = DocumentUtils.impl.createDocumentFromString("<registry></registry>");

		this.document = document;
	}
	
	/**
	 * Return the copy of the doc that we will be modifying
	 */
	public Document getDocument() {
		return document;
	}

	public boolean removeFromRegistry(String id, String protocol) {
		final Document document = getDocument();
		final NodeList items = document.getElementsByTagName(protocol);

		for (int i = 0; i < items.getLength(); i++) {
			final Node current = items.item(i);
			if (current.getNodeName().equalsIgnoreCase(protocol)
					&& DocumentUtils.impl.getAttribute(current, "id").equalsIgnoreCase(id)) {
				document.getFirstChild().removeChild(current);
				break;
			}
		}
		
		return updateDocument(document);
	}
	
	public boolean updateRegistry(final String id, final String uri, final String name, final String protocol) {
		final Document document = getDocument();
		boolean found = false;

		final NodeList registryNodes = document.getElementsByTagName("registry");
		for (int k = 0; k < registryNodes.getLength(); k++) {
			final Node currentRegistry = registryNodes.item(k);
			if (currentRegistry.getNodeName().equalsIgnoreCase("registry")) {
				final NodeList items = currentRegistry.getChildNodes();

				for (int i = 0; i < items.getLength(); i++) {
					final Node current = items.item(i);
					if (current.getNodeName().equalsIgnoreCase(protocol)
							&& DocumentUtils.impl.getAttribute(current, "id").equalsIgnoreCase(id)) {
						Attr uriAttr = document.createAttribute(Registry.DATA_URI);
						uriAttr.setTextContent(uri);
						current.getAttributes().setNamedItem(uriAttr);

						Attr nameAttr = document.createAttribute(Registry.DATA_NAME);
						nameAttr.setTextContent(name);
						current.getAttributes().setNamedItem(nameAttr);

						found = true;
						break;
					}
				}

				if (!found) {
					final Element newChild = document.createElement(protocol);
					newChild.setAttribute(Registry.DATA_NAME, name);
					newChild.setAttribute(Registry.DATA_URI, uri);
					newChild.setAttribute("id", id);
					currentRegistry.appendChild(newChild);
				}
				break;
			}
		}

		return updateDocument(document);
	}

	public boolean updateRegistryData(final String id, final String protocol, final HashMap<String, String> data) {
		final Document document = getDocument();
		boolean found = false;

		final NodeList registryNodes = document.getElementsByTagName("registry");
		for (int k = 0; k < registryNodes.getLength(); k++) {
			final Node currentRegistry = registryNodes.item(k);
			if (currentRegistry.getNodeName().equalsIgnoreCase("registry")) {
				final NodeList items = currentRegistry.getChildNodes();

				for (int i = 0; i < items.getLength(); i++) {
					final Node current = items.item(i);
					if (current.getNodeName().equalsIgnoreCase(protocol)
							&& DocumentUtils.impl.getAttribute(current, "id").equals(id)) {

						// These can not be changed here.
						data.remove(Registry.DATA_URI);
						data.remove(Registry.DATA_NAME);

						Iterator<Map.Entry<String, String>> iterator = data.entrySet().iterator();
						while (iterator.hasNext()) {
							Map.Entry<String, String> entry = iterator.next();
							((Element) current).setAttribute(entry.getKey(), entry.getValue());
						}
						found = true;
						break;
					}
				}
				if (!found)
					return false;
				break;
			}
		}

		return updateDocument(document);
	}
	
	/**
	 * Deferred writer will wait to write...
	 */
	protected boolean updateDocument(Document document) {
		return true;
	}
	
	/**
	 * Time to write for real...
	 */
	public boolean persistDocument(Document document) {
		return DocumentUtils.writeVFSFile(path, vfs, document);
	}

}
