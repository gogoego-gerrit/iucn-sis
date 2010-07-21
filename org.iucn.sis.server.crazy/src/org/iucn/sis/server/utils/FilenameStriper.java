package org.iucn.sis.server.utils;

public class FilenameStriper {
	private static final int PATH_CHAR_LENGTH = 3;

	public static String getIDAsStripedPath(long ID) {
		return getIDAsStripedPath("" + ID);
	}

	/**
	 * Passing in an ID for a document, e.g. 22467, will result in retrieving a
	 * path that inserts a slash between every two characters, e.g.
	 * 22/46/7/22467.
	 * 
	 * @param ID
	 * @return
	 */
	public static String getIDAsStripedPath(String ID) {
		String fullID = ID;

		// STRIP .xml SUFFIX
		if (ID.indexOf(".xml") >= 0)
			ID = ID.substring(0, ID.length() - ".xml".length() - 1);

		if (ID.length() <= PATH_CHAR_LENGTH)
			return ID;
		else {
			String ret = ID.substring(0, PATH_CHAR_LENGTH);
			ID = ID.substring(PATH_CHAR_LENGTH, ID.length());

			while (ID.length() > PATH_CHAR_LENGTH) {
				ret += "/" + ID.substring(0, PATH_CHAR_LENGTH);
				ID = ID.substring(PATH_CHAR_LENGTH, ID.length());
			}

			ret += "/" + fullID;

			return ret;
		}
	}
}
