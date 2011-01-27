package org.iucn.sis.server.extensions.recentasms;

import org.iucn.sis.shared.api.models.User;

public class RecentUserInfo extends RecentInfo<User> {
	
	@Override
	protected void parse(User user) {
		if (user.getState() != User.DELETED) {
			addField("firstName", user.getFirstName());
			addField("lastName", user.getLastName());
			addField("nickname", user.getNickname());
			addField("initials", user.getInitials());
			addField("email", user.getEmail());
			addField("userid", user.getId()+"");
			addField("username", user.getUsername());
			addField("affiliation", user.getAffiliation());
			addField("quickGroup", user.getQuickGroupString());
		}
	}

}