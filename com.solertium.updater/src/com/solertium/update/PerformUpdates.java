package com.solertium.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.solertium.util.ElementCollection;
import com.solertium.util.restlet.StandardServerComponent;

/**
 * Initiates update downloads for an offline application.
 * 
 * Every instance of an application using this updater should have a version document 
 * that contains the following information - (component name) : (version), e.g.:
 * 
 * <ul>
 * <li>application.jar: 5</li>
 * <li>SIS_Toolkit.exe: 5</li>
 * <li>field specifications: 2</li>
 * <li>views: 1</li>
 * <li>scripts: 1</li>
 * <li>lib: 7</li>
 * </ul>
 * 
 * This clearly tracks versions of each module; when this updater contacts
 * the "fetch updater resource" online, it sends its version document. The
 * updater resource then prepares the proper updates, if anything is out of
 * date, and this updater will write out the proper update components then
 * exit the application with the proper "update" code, to be caught by the
 * wrapper. 
 * 
 * @author adam.schwartz
 */
public class PerformUpdates {

	public static PerformUpdates impl = new PerformUpdates();

	private Document myComponents;

	public boolean updateBookkeeping(Document bookEntry) {
		Element curComponent = (Element)bookEntry.getElementsByTagName("component").item(0).cloneNode(true);
		for( Element curEl : new ElementCollection(myComponents.getElementsByTagName("component")) )
			if( curEl.getAttribute("id").equals(curComponent.getAttribute("id")) )
				curEl.getParentNode().removeChild(curEl);
			
		myComponents.adoptNode(curComponent);
		myComponents.getDocumentElement().appendChild(curComponent);
		return writebackMyComponents();
	}
	
	public boolean writebackMyComponents() {
		String myComponentsPath = StandardServerComponent.getInitProperties().getProperty("MY_COMPONENTS_PATH");
		if( myComponentsPath == null )
			myComponentsPath = "my_components.properties";

		try {
			Transformer dt = getTransformer();
			FileWriter writer = new FileWriter(new File(myComponentsPath));
			dt.transform(new DOMSource(myComponents), new StreamResult(writer));
			writer.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Transformer getTransformer() {
		try {
			Transformer dt = TransformerFactory.newInstance().newTransformer();
			dt.setOutputProperty(OutputKeys.METHOD, "xml");
			dt.setOutputProperty(OutputKeys.INDENT, "yes");
			dt.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			return dt;
		} catch (TransformerConfigurationException tcx) {
			throw new RuntimeException(tcx);
		}
	}

	public Representation getUpdateSummary(Uniform uniform) {
		String myComponentsPath = StandardServerComponent.getInitProperties().getProperty("MY_COMPONENTS_PATH");
		if( myComponentsPath == null )
			myComponentsPath = "my_components.properties";

		String updateURL = StandardServerComponent.getInitProperties().getProperty("UPDATE_URL");
		if( updateURL == null )
			return null;
		updateURL = updateURL.concat("/summary");
		
		System.out.println("Update summary URL is: " + updateURL);
		
		try {
			if( new File(myComponentsPath).exists() )
				myComponents = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
						new InputSource(new FileInputStream(new File(myComponentsPath))));
			else
				myComponents = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
						new InputSource(new StringReader("<components/>")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		if( myComponents == null )
			return null;

		Request updateRequest = new Request(Method.POST, updateURL);
		updateRequest.setEntity( new DomRepresentation(MediaType.TEXT_XML, myComponents));

		Response updateResponse = new Response(updateRequest);
		uniform.handle(updateRequest, updateResponse);
		if( updateResponse.getStatus().isSuccess() && (updateResponse.getStatus().getCode() != 204) ) {
			return updateResponse.getEntity();
		} else {
			return null;
		}
	}
	
	public Representation checkForUpdates(Uniform uniform) {
		String myComponentsPath = StandardServerComponent.getInitProperties().getProperty("MY_COMPONENTS_PATH");
		if( myComponentsPath == null )
			myComponentsPath = "my_components.properties";

		String updateURL = StandardServerComponent.getInitProperties().getProperty("UPDATE_URL");
		if( updateURL == null ) {
			return new StringRepresentation("UPDATE_URL not specified! Cannot perform updates.", MediaType.TEXT_PLAIN);
		}

		System.out.println("Update URL is: " + updateURL);
		
		try {
			if( new File(myComponentsPath).exists() )
				myComponents = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
						new InputSource(new FileInputStream(new File(myComponentsPath))));
			else
				myComponents = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
						new InputSource(new StringReader("<components/>")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		if( myComponents == null ) {
			System.out.println("Unable to parse in my_components.properites. Must not have one.");
			return new StringRepresentation("Unable to parse in my_components.properties.", MediaType.TEXT_PLAIN);
		}

		Request updateRequest = new Request(Method.POST, updateURL);
		updateRequest.setEntity( new DomRepresentation(MediaType.TEXT_XML, myComponents));

		Response updateResponse = new Response(updateRequest);
		uniform.handle(updateRequest, updateResponse);
		if( updateResponse.getStatus().isSuccess() ) {
			
			if( updateResponse.getStatus() == Status.SUCCESS_NO_CONTENT ) {
				return null;
			}
			
			try {
				File updatesDir = new File("updates/");
				File updatesZip = new File(updatesDir, "updates.zip");

				if( updatesZip.exists() )
					updatesZip.delete();

				updatesDir.mkdir();
				updatesZip.createNewFile();

				FileOutputStream out = new FileOutputStream(updatesZip);
				Representation rep = updateResponse.getEntity();
				rep.write(out);
				out.flush();
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
				return new StringRepresentation(e.toString() + "\r\n" + Arrays.toString(e.getStackTrace()), MediaType.TEXT_PLAIN);
			}

			return null;
		} else {
			System.out.println( "ERROR communicating with update server! Status code: " + updateResponse.getStatus().getCode() );
			return updateResponse.getEntity();
		}
	}
}
