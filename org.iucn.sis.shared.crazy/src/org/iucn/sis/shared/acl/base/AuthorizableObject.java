package org.iucn.sis.shared.acl.base;


public interface AuthorizableObject {

	public static final String READ = "READ";
	public static final String WRITE = "WRITE";
	public static final String CREATE = "CREATE";
	public static final String DELETE = "DELETE";
	public static final String GRANT = "GRANT";
	public static final String USE_FEATURE = "USE_FEATURE";

	/**
	 * This should be prepended to any AuthorizedObject's path that is a
	 * resource within the program.
	 */
	public static final String RESOURCE_TYPE_PATH = "resource";

	/**
	 * This should be prepended to any AuthorizedObject's path that is a
	 * function within the program.
	 */
	public static final String FEATURE_TYPE_PATH = "feature";

	/**
	 * Gets the object's authorization URI.
	 * 
	 * @return authorization URI
	 */
	public String getFullURI();

	/**
	 * Gets a property that does not fit into the hierarchical URI model that can be used 
	 * to further restrict permission scopes for this object type. For example, if the
	 * object to govern is a car and has a URI of make/model/year/componentName, 
	 * "foreign" would be a property, as it doesn't fit simply in the hierarchy;
	 * even using a wildcard is ambiguous, as it could mean either foreign or domestic, or
	 * all foreign origins. 
	 * 
	 * @param key - name of the property
	 * @return value associated with the key
	 */
	public String getProperty(String key);
}
