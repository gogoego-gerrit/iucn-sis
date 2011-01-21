package org.iucn.sis.server.extensions.recentasms;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.RecentlyAccessed;
import org.iucn.sis.shared.api.models.User;

public class RecentInfoFactory {
	
	public static Class<?> getClassForName(String type) {
		Class<?> clazz = null;
		if (RecentlyAccessed.ASSESSMENT.equals(type))
			clazz = Assessment.class;
		else if (RecentlyAccessed.USER.equals(type))
			clazz = User.class;
		
		return clazz;
	}
	
	public static <X> RecentInfo<X> load(RecentlyAccessed accessed) throws PersistentException {
		Class<?> clazz = getClassForName(accessed.getType());
		if (clazz == null)
			return null;
		
		try {
			return load(accessed.getType(), 
				SIS.get().getManager().loadObject(clazz, accessed.getObjectid()));
		} catch (PersistentException e) {
			//Object may have been deleted at some point
			SIS.get().getManager().deleteObject(accessed);
			return null;
		} catch (Exception e) {
			Debug.println(e);
			return null;
		}
	}
	
	public static <X> RecentInfo<X> load(String type, Object object) {
		RecentInfo parser;
		if (RecentlyAccessed.ASSESSMENT.equals(type))
			parser = new RecentAssessmentInfo();
		else if (RecentlyAccessed.USER.equals(type))
			parser = new RecentUserInfo();
		else
			return null;
		
		parser.parse(object);
		
		return parser;
	}

}
