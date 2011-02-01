package org.iucn.sis.client.api.utils;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class FormattedDate {
	
	public final static FormattedDate SHORT = new FormattedDate(DateTimeFormat.getFormat("yyyy-MM-dd"));
	public final static FormattedDate FULL = new FormattedDate(DateTimeFormat.getFormat("dd MMM yyyy, h:mm aa zzz"));
	
	public final static FormattedDate impl = SHORT;

	private DateTimeFormat format;

	private FormattedDate(DateTimeFormat format) {
		this.format = format;
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
