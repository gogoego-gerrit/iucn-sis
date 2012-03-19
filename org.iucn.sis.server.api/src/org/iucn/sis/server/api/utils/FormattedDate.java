package org.iucn.sis.server.api.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattedDate {

	public static final FormattedDate impl = new FormattedDate();

	private String format;
	private String formatWithTime;
	private DateFormat formatter;
	private DateFormat formatterWithTime;

	private FormattedDate() {
		format = "yyyy-MM-dd";
		formatWithTime = "yyyy-MM-dd HH:mm:ss";
		formatter = new SimpleDateFormat(format);
		formatterWithTime = new SimpleDateFormat(formatWithTime);
	}

	public String getDate() {
		
		return formatter.format(new Date());
	}

	public String getDate(Date date) {
		return formatter.format(date);
	}

	public String getDateWithTime() {
		return formatterWithTime.format(new Date());
	}

	public String getDateWithTime(Date date) {
		return formatterWithTime.format(date);
	}
	
	
	public Date getDate(String formattedDate) {
		try {
			return formatter.parse(formattedDate);
		} catch (ParseException e) {
			try {
				return formatterWithTime.parse(formattedDate);
			} catch (ParseException f) {
				return null;
			}
		}
	}
}
