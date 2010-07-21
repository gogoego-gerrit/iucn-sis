package org.iucn.sis.server.simple;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.shared.xml.XMLUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.ElementCollection;

/**
 * Serves Region information. POST can handle multiple Region update/creation
 * requests and returns the entire list of Regions, PUT can handle one Region at
 * a time, and returns the new/existing ID. Incoming payload for these should be
 * an XML document with any document element name then serialized Region
 * object(s) as children.
 * 
 * @author adam.schwartz
 * 
 */
public class RegionRestlet extends ServiceRestlet {

	public static long addNewRegion(String regionName, String description) {
		Element newNode = DocumentUtils.createElementWithText(regionsDocument, "region", "");
		long id = nextID.getAndIncrement();
		newNode.setAttribute("id", id + "");
		newNode.setIdAttribute("id", true);
		newNode.appendChild(DocumentUtils.createElementWithText(regionsDocument, "name", XMLUtils.clean(regionName)));
		newNode.appendChild(DocumentUtils.createElementWithText(regionsDocument, "description", XMLUtils
				.clean(description)));

		regionsDocument.getDocumentElement().appendChild(newNode);
		nameToID.put(regionName, id + "");

		return id;
	}

	private final String baseUrl = "/regions/";

	private static final AtomicLong nextID = new AtomicLong(0);

	public static HashMap<String, String> nameToID;

	private static Document regionsDocument;

	public RegionRestlet(String vfsroot, Context context) {
		super(vfsroot, context);
		nameToID = new HashMap<String, String>();

		if (vfs.exists(baseUrl + "regions.xml")) {
			regionsDocument = DocumentUtils.getVFSFileAsDocument(baseUrl + "regions.xml", vfs);

			long largestID = 0;
			ElementCollection els = new ElementCollection(regionsDocument.getElementsByTagName("region"));
			for (Element el : els) {
				long myID = Long.parseLong(el.getAttribute("id"));
				largestID = Math.max(myID, largestID);
				el.setIdAttribute("id", true);

				String name = XMLUtils.clean(el.getElementsByTagName("name").item(0).getTextContent());
				nameToID.put(name, myID + "");
			}
			nextID.set(largestID + 1);
		} else {
			regionsDocument = DocumentUtils.createDocumentFromString("<regions></regions>");
		}
	}

	@Override
	public void definePaths() {
		paths.add("/regions");
		paths.add("/regions/{regionID}");
	}

	private void handleGet(Request request, Response response) {
		// String id = (String) request.getAttributes().get("regionID");

		response.setEntity(new DomRepresentation(MediaType.TEXT_XML, regionsDocument));
		response.setStatus(Status.SUCCESS_OK);
	}

	private void handlePost(Request request, Response response) {
		try {
			Document editedRegions = new DomRepresentation(request.getEntity()).getDocument();

			ElementCollection newRegions = new ElementCollection(editedRegions.getElementsByTagName("region"));
			for (Element el : newRegions) {
				final String id = el.getAttribute("id");
				final String name = XMLUtils.cleanFromXML(el.getElementsByTagName("name").item(0).getTextContent());
				final String description = XMLUtils.cleanFromXML(el.getElementsByTagName("description").item(0)
						.getTextContent());

				final Element existingById = regionsDocument.getElementById(id);

				if (existingById == null) {
					if (!nameToID.containsKey(name))
						addNewRegion(name, description);
				} else {
					updateRegion(id, name, description);
				}
			}

			writebackDocument();
			handleGet(request, response);

		} catch (IOException e) {
			response.setEntity("Error fetching edited regions out of the request object.", MediaType.TEXT_PLAIN);
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private void handlePut(Request request, Response response) {

		try {
			Document putRegion = new DomRepresentation(request.getEntity()).getDocument();
			Element el = (Element) putRegion.getDocumentElement().getElementsByTagName("region").item(0);

			String id = el.getAttribute("id");
			final String name = XMLUtils.cleanFromXML(el.getElementsByTagName("name").item(0).getTextContent());
			final String desc = XMLUtils.cleanFromXML(el.getElementsByTagName("description").item(0).getTextContent());

			if (id.matches("\\d+") && regionsDocument.getElementById(id) != null) {
				updateRegion(id, name, desc);
				writebackDocument();
			} else if (nameToID.containsKey(name))
				id = nameToID.get(name);
			else {
				id = addNewRegion(name, desc) + "";
				writebackDocument();
			}

			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(id, MediaType.TEXT_PLAIN);

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			response.setEntity("Region payload unreadable.", MediaType.TEXT_PLAIN);
		}

	}

	@Override
	public void performService(Request request, Response response) {
		if (request.getMethod().equals(Method.GET))
			handleGet(request, response);
		if (request.getMethod().equals(Method.POST))
			handlePost(request, response);
		if (request.getMethod().equals(Method.PUT))
			handlePut(request, response);

	}

	private void updateRegion(String id, String regionName, String description) {
		Element el = regionsDocument.getElementById(id);
		el.getElementsByTagName("name").item(0).setTextContent(XMLUtils.clean(regionName));
		el.getElementsByTagName("description").item(0).setTextContent(XMLUtils.clean(description));
	}

	private void writebackDocument() {
		DocumentUtils.writeVFSFile(baseUrl + "regions.xml", vfs, regionsDocument);
	}
}
