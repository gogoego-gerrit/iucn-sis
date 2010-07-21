package org.iucn.sis.server.utils;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

public class FormattedDate {

	public static final FormattedDate impl = new FormattedDate();

	private String format;
	private String formatWithTime;

	private FormattedDate() {
		format = "yyyy-MM-dd";
		formatWithTime = "yyyy-MM-dd HH:mm:ss";
	}

	public String getDate() {
		return DateFormatUtils.format(new Date(), format);
	}

	public String getDate(Date date) {
		return DateFormatUtils.format(date, format);
	}

	public String getDateWithTime() {
		return DateFormatUtils.format(new Date(), formatWithTime);
	}

	public String getDateWithTime(Date date) {
		return DateFormatUtils.format(date, formatWithTime);
	}

}
