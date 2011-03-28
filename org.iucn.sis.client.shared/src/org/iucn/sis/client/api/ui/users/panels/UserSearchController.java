package org.iucn.sis.client.api.ui.users.panels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.models.ClientUser;
import org.iucn.sis.client.api.utils.UriBase;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;

public class UserSearchController {
	
	public static final String SEARCH_KEY_FIRST_NAME = "firstname";
	public static final String SEARCH_KEY_LAST_NAME = "lastname";
	public static final String SEARCH_KEY_NICKNAME = "nickname";
	public static final String SEARCH_KEY_AFFILIATION = "affiliation";
	public static final String SEARCH_KEY_USER_ID = "userid";
	
	public static void search(Map<String, List<String>> params,
			final GenericCallback<List<SearchResults>> callback) {
		search(params, "or", callback);
	}
	
	public static void search(Map<String, List<String>> params, String mode, 
			final GenericCallback<List<SearchResults>> callback) {
		StringBuilder query = new StringBuilder();
		if (!params.isEmpty()) {
			query.append('?');
			if (!"or".equals(mode))
				query.append("mode=and&");
			final Iterator<Map.Entry<String, List<String>>> iter = params.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, List<String>> entry = iter.next();
				for (Iterator<String> valIter = entry.getValue().iterator(); valIter.hasNext(); ) {
					query.append(entry.getKey() + "=" + valIter.next());
					if (valIter.hasNext())
						query.append('&');
				}
				if (iter.hasNext())
					query.append('&');
			}
		}

		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getUserBase()
				+ "/browse/profile" + query, new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				final List<SearchResults> results = new ArrayList<SearchResults>();
				
				for (RowData rowData : parser.getRows())
					results.add(new SearchResults(userFromRowData(rowData)));
				
				callback.onSuccess(results);
			}
		});
	}
	
	public static ClientUser userFromRowData(RowData rowData) {
		final ClientUser user = new ClientUser();
		final Iterator<String> iterator = rowData.keySet().iterator();
		while (iterator.hasNext()) {
			final String property = iterator.next().toLowerCase();
			user.setProperty(property, rowData.getField(property));
			if (property.equalsIgnoreCase("firstname"))
				user.setFirstName(rowData.getField(property));
			else if (property.equalsIgnoreCase("lastname"))
				user.setLastName(rowData.getField(property));
			else if (property.equalsIgnoreCase("nickname"))
				user.setNickname(rowData.getField("nickname"));
			else if (property.equalsIgnoreCase("initials"))
				user.setInitials(rowData.getField(property));
			else if (property.equalsIgnoreCase("affiliation"))
				user.setAffiliation(rowData.getField(property));
			else if (property.equalsIgnoreCase("email"))
				user.setEmail(rowData.getField(property));
			else if (property.equalsIgnoreCase("userid"))
				user.setId(Integer.parseInt(rowData.getField(property)));
			else if (property.equalsIgnoreCase("quickgroup"))
				user.setProperty("quickGroup", rowData.getField(property));
			else if (property.equalsIgnoreCase("username"))
				user.setUsername(rowData.getField(property));
			else
				user.setProperty(property, rowData.getField(property));
		}
		
		return user;
	}
	
	/**
	 * Wrapper for the RowData results returned from a search query.
	 * 
	 * @author carl.scott <carl.scott@solertium.com>
	 * 
	 */
	public static class SearchResults extends BaseModelData {
		private static final long serialVersionUID = 1L;

		protected final ClientUser user;

		public SearchResults(ClientUser user) {
			this.user = user;
			set("name", user.getDisplayableName());
			set("userid", user.getId());
			
			for (Map.Entry<String, String> entry : user.properties.entrySet())
				set(entry.getKey().toLowerCase(), entry.getValue());
			
			if ("".equals(user.getFirstName()) && !"".equals(user.getInitials()))
				set("firstname", user.getInitials());
		}

		public ClientUser getUser() {
			return user;
		}

		public String toString() {
			return user.getDisplayableName();
		}
		
		@Override
		public boolean equals(Object obj) {
			if( obj instanceof SearchResults ) {
				return ((SearchResults)obj).getUser().getId() == user.getId();
			} else
				return super.equals(obj);
		}
	}

}
