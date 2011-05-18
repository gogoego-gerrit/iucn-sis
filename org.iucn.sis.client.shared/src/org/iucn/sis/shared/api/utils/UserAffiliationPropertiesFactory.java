package org.iucn.sis.shared.api.utils;

import org.iucn.sis.shared.api.models.User;

public class UserAffiliationPropertiesFactory {
	
	public static UserAffiliationProperties get(User user) {
		return get(user.getAffiliation());
	}
	
	public static UserAffiliationProperties get(String affiliation) {
		if ("birdlife".equalsIgnoreCase(affiliation))
			return new BirdLifeUserAffiliationProperties();
		else
			return new DefaultUserAffiliationProperties();
	}

}
