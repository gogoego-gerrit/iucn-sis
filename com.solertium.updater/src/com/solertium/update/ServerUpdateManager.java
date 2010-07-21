package com.solertium.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.solertium.util.ElementCollection;
import com.solertium.util.restlet.StandardServerComponent;

public class ServerUpdateManager {

	private static ServerUpdateManager impl = new ServerUpdateManager();
	
	/**
	 * Maps the component name to the latest version ID 
	 */
	private HashMap<String, Long> componentLatestVersions;
	
	/**
	 * Maps the component name to the latest version ID 
	 */
	private HashMap<String, UpdatableComponent> idToUpdatableComponent;
	
	/**
	 * Maps an updatable component (name, version) to the location of path of the proper 
	 * patch to be downloaded
	 */
	private HashMap<String, String> updatePaths;
	
	/**
	 * Maps an updatable component (name, version) to the target path, or where it should
	 * unzipped to
	 */
	private HashMap<String, String> targetPaths;
	
	private long lastModified = 0;
	
	private ServerUpdateManager() {
		componentLatestVersions = new HashMap<String, Long>();
		updatePaths = new HashMap<String, String>();
		idToUpdatableComponent = new HashMap<String, UpdatableComponent>();
		targetPaths = new HashMap<String, String>();
		buildMaps();
	}
	
	public static ServerUpdateManager getImpl() {
		impl.buildMaps();
		return impl;
	}
	
	public Long getLatestVersion(String componentID) {
		return componentLatestVersions.get(componentID);
	}
	
	public ArrayList<String> getAllAvailableComponents() {
		ArrayList<String> all = new ArrayList<String>();
		for( Entry<String, Long> curEntry : componentLatestVersions.entrySet() )
			all.add( curEntry.getKey() );
		
		return all;
	}
	
	public HashMap<String, UpdatableComponent> getIdToUpdatableComponentMap() {
		return idToUpdatableComponent;
	}
	
	private void buildMaps() {
		String updateConfigURL = StandardServerComponent.getInitProperties().getProperty("UPDATE_CONFIGURE_PATH");
		if( updateConfigURL == null )
			updateConfigURL = "update_config.properties";
		
		Document config = null;
		
		try {
			if( lastModified == new File(updateConfigURL).lastModified() )
				return;
			
			config = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					new InputSource(new FileInputStream(new File(updateConfigURL))));
			lastModified = new File(updateConfigURL).lastModified();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		if( config == null ) {
			System.out.println("Unable to parse in update_config.properites. Must not be " +
					"an online version.");
			return;
		}
		
		ElementCollection els = new ElementCollection(config.getElementsByTagName("component"));
		for( Element el : els ) {
			String id = el.getAttribute("id");
			String name = el.getAttribute("name");
			String latestVersion = el.getAttribute("latestVersion");
			String path = el.getAttribute("path");
			String upgradeFromVersion = el.getAttribute("upgradeFromVersion");
			String targetPath = el.getAttribute("targetPath");
			
			if( latestVersion.matches("\\d+") )
				componentLatestVersions.put(id, Long.valueOf(latestVersion));
			
			if( targetPath != null && !targetPath.equals("") )
				putTargetPath(id, upgradeFromVersion, targetPath);
			
			putUpdatePath(id, upgradeFromVersion, path);
			
			idToUpdatableComponent.put(id, new UpdatableComponent(id, name, Long.valueOf(latestVersion)));
		}
	}
	
	/**
	 * Fetches the latest version of this component from the Map and checks to see
	 * if the client component needs updating.
	 *   
	 * @param client's component specs
	 * @return true if it needs updating, false if not
	 */
	public boolean updateNeeded(UpdatableComponent component) {
		Long mostRecent = componentLatestVersions.get(component.getId());
		if( mostRecent.longValue() > component.getCurrentVersion().longValue() )
			return true;
		else
			return false;
	}
	
	private void putTargetPath(String componentID, String fromVersion, String path) {
		targetPaths.put(componentID + fromVersion, path);
	}
	
	public String getTargetPath(String componentID, String fromVersion) {
		if( targetPaths.containsKey(componentID + fromVersion) ) //If not partiular version update
			return targetPaths.get(componentID + fromVersion);
		else if( targetPaths.containsKey(componentID + "*") )
			return targetPaths.get(componentID + "*"); //Get the general update path
		else
			return getUpdatePath(componentID, fromVersion);
	}
	
	private void putUpdatePath(String componentID, String fromVersion, String path) {
		updatePaths.put(componentID + fromVersion, path);
	}
	
	public String getUpdatePath(String componentID, String fromVersion) {
		if( updatePaths.containsKey(componentID + fromVersion) ) //If not partiular version update
			return updatePaths.get(componentID + fromVersion);
		else
			return updatePaths.get(componentID + "*"); //Get the general update path
	}
}
