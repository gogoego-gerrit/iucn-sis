package org.iucn.sis.client.utilities;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class FormattedDate {

	public final static FormattedDate impl = new FormattedDate();

	private DateTimeFormat format;

	private FormattedDate() {
		format = DateTimeFormat.getFormat("yyyy-MM-dd");
	}

	public String getDate() {
		return format.format(new Date());
	}
	
	public Date getDate(String dateString) {
		return format.parse(dateString);
	}

	public String getDate(Date date) {
		return format.format(date);
	}
	
	public DateTimeFormat getDateTimeFormat() {
		return format;
	}

}
