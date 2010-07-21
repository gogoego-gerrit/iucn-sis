package com.solertium.update;

/**
 * A simple wrapper that associates a component with a version number. hashCode is 
 * SOLELY based upon the ID of this component.
 * 
 * @author adam.schwartz
 */
public class UpdatableComponent {
	
	private String id;
	private String componentName;
	private Long currentVersion;
	
	public UpdatableComponent(String id, String componentName, String currentVersion)
			throws NumberFormatException{
		this.id = id;
		this.componentName = componentName;
		this.currentVersion = Long.valueOf(currentVersion);
	}
	
	public UpdatableComponent(String id, String componentName, Long currentVersion) {
		this.id = id;
		this.componentName = componentName;
		this.currentVersion = currentVersion;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public Long getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(Long currentVersion) {
		this.currentVersion = currentVersion;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}