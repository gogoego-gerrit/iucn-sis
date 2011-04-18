package org.iucn.sis.shared.api.models.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.User;

import com.solertium.util.portable.PortableAlphanumericComparator;

public class RedListCreditedUserField extends ProxyField {
	
	public RedListCreditedUserField(Field field) {
		super(field);
	}

	public void setUsers(List<Integer> users) {
		setForeignKeyListPrimitiveField("value", users, "user");
	}
	
	public List<Integer> getUsers() {
		return getForeignKeyListPrimitiveField("value");
	}
	
	public void setOrder(String order) {
		setStringPrimitiveField("order", order);
	}
	
	public String getOrder() {
		String value = getStringPrimitiveField("order");
		if ("".equals(value))
			value = null;
		return value;
	}
	
	public void setText(String text) {
		setStringPrimitiveField("text", text);
	}
	
	public String getText() {
		return getStringPrimitiveField("text");
	}
	
	public static String generateText(List<? extends User> users) {
		return generateText(users, null);
	}
	
	public static String generateText(List<? extends User> users, String order) {
		Collections.sort(users, new CreditedUserComparator(order));

		StringBuilder text = new StringBuilder();
		for (int i = 0; i < users.size(); i++) {
			text.append(users.get(i).getCitationName());
			
			if (i + 1 < users.size() - 1)
				text.append(", ");

			else if (i + 1 == users.size() - 1)
				text.append(" & ");
		}
		
		return text.toString();
	}
	
	private static class CreditedUserComparator implements Comparator<User> {

		private static final long serialVersionUID = 1L;
		private final PortableAlphanumericComparator c = new PortableAlphanumericComparator(); 

		private final List<Integer> order;

		public CreditedUserComparator(String order) {
			if (order == null || "".equals(order))
				this.order = null;
			else {
				this.order = new ArrayList<Integer>();
				for (String s : order.split(",")) {
					try {
						this.order.add(Integer.valueOf(s));
					} catch (Exception how) { }
				}
			}
		}

		@Override
		public int compare(User arg0, User arg1) {
			if (order == null)
				return sortByName(arg0, arg1);
		
			int m1Index = arg0 == null ? -1 : order.indexOf(arg0.getId());
			int m2Index = arg1 == null ? -1 : order.indexOf(arg1.getId());
		
			if (m1Index == -1)
				return 1;
			else if (m2Index == -1)
				return -1;
			else
				return Integer.valueOf(m1Index).compareTo(Integer.valueOf(m2Index));
		}
		
		private int sortByName(User m1, User m2) {
			for (String current : new String[]{ "lastname", "firstname" }) {
				int value;
				if ("lastname".equals(current))
					value = c.compare(m1.getLastName(), m2.getLastName());
				else
					value = c.compare(m1.getFirstName(), m2.getFirstName());
				if (value != 0)
					return value;
			}
			return 0;
		}
	}
	
}
