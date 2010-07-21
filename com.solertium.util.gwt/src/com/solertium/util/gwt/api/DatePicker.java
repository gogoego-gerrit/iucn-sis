/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.util.gwt.api;

import java.util.ArrayList;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.gwt.ui.CalendarWidget;

/**
 * NEEDS THE FOLLOWING STYLES
 * .CalendarWidget {
 *  border: 1px solid #00428C;
 *  background-color: #FFFFFF;
 *}
 *
 *.CalendarWidget .navbar {
 *    width: 100%;
 *    background-color: #DFE8F6;
 *    color: #00428C;
 *    font-weight: bold;
 *    vertical-align: middle;
 *}
 *
 *.CalendarWidget .navbar .gwt-Button {
 *   padding-left: 5px;
 *  padding-right: 5px;
 *   color: #00428C;
 *}
 *
 *.CalendarWidget .table {
 *    font: 10pt sans-serif;
 *    text-align: center;
 *}
 *
 *.CalendarWidget .weekheader {
 *    background-color:#DFE8F6;
 *}
 *
 *.CalendarWidget .weekheader .days {
 *    width: 3em;
 *}
 *
 *.CalendarWidget .cell {
 *    cursor: pointer;
 *}
 *
 *.CalendarWidget .cell .gwt-HTML {
 *    border: .5px solid #ACA899;
 *}
 *
 *.CalendarWidget .cell .gwt-HTML span {
 *  width: 100%;
 *  height: 100%;
 *  line-height: 2em;
 *}
 *
 *.CalendarWidget .today .gwt-HTML {
 *  background-color: #DFE8F6
 *}
 *
 *.CalendarWidget .day .gwt-HTML {
 *  border: .5px solid #C9EEFF;
 *}
 *
 *.CalendarWidget .day .gwt-HTML {
 *  border: .5px solid #ACA899;
 *}
 *
 *.calendar-month-button .gwt-Button {
 *	color: #00428C;
 *}
 *
 *.calendar-month {
 *  background-color: #DFE8F6;
 *}
 * @author liz.schwartz
 *
 */
public	class DatePicker extends HorizontalPanel {

	public final static String InternationalDate = "yyyy/mm/dd";
	public final static String AmericanDate = "mm/dd/yyyy";
	public final static String EuropeanDate = "dd/mm/yyyy";
	
	public boolean SELECT_DAY;
	public boolean SELECT_WEEK;
	public boolean SELECT_MONTH;
	public boolean SELECT_YEAR;
	
	private int earliestYear = 1800;
	private int lastYear = 3000;
	private boolean monthSelector;
	private boolean isShowing = false;
	private final PopupPanel popupPanel;	 
	private String dateFormat;
	private TextBox date;
	private Image img;
	private boolean resetDate;
	private boolean selectWeek;
	
	private ArrayList<ChangeListener> listeners;
	/**
	 * Used as the separator in the date format. Defaults to "/", so for example, 
	 * the InternationalDate format would be "yyyy/mm/dd".
	 */
	private String separator = "/";

	/**
	 * only choice for dateFormat right now is InternationalDate and AmericanDate and EuropeanDate
	 */
	public DatePicker(String imageURL, String dateFormat, boolean monthSelector, boolean resetDate){
		this(imageURL, dateFormat, monthSelector, resetDate, false);

	}
	public DatePicker(String imageURL, String dateFormat, boolean monthSelector, boolean resetDate, boolean selectWeek) 
	{
		
		this.monthSelector = monthSelector;
		this.dateFormat = dateFormat;
		this.resetDate = resetDate;
		this.selectWeek=selectWeek;
		
		listeners = new ArrayList<ChangeListener>();
		
		SELECT_DAY=true;
		SELECT_MONTH=true;
		SELECT_WEEK=false;
		SELECT_YEAR=true;
		
		buildCalendarImage(imageURL);
		buildDateTextbox();
		
		popupPanel = new PopupPanel(true, true);
		
		setSpacing(4);
		add(date);
		add(img);
		
		setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
	}
	
	/**
	 * only choice for dateFormat right now is InternationalDate and AmericanDate and EuropeanDate
	 */
	public DatePicker(String imageURL, String dateFormat) {
		this(imageURL, dateFormat, false);
	}
	
	public DatePicker(String imageURL, String dateFormat, boolean selectWeek) {
		this(imageURL, dateFormat, false, true, selectWeek);
	}
	
	private void buildCalendarImage(String imageURL)
	{
		img = new Image(imageURL) {
			public void onBrowserEvent(final Event event) {
				switch (DOM.eventGetType(event)) {
				case Event.ONCLICK:
					if (!isShowing)
					{
						showCalendar(
							DOM.eventGetClientX(event), 
							DOM.eventGetClientY(event));
					}
					break;
				default:
					break;
				}
			}		
		};
		img.addStyleName("cursor-pointer");
	}
	
	private void buildDateTextbox()
	{
		date = new TextBox();
		date.setText(dateFormat);
		date.setMaxLength(dateFormat.length());
		date.setWidth("80px");
		date.addFocusListener(new FocusListener() {

			public void onLostFocus(Widget sender) 
			{
				if (!getUncheckedText().equalsIgnoreCase(dateFormat))
				{
					boolean valid = checkDateValidity();
					if (!valid && !getUncheckedText().equalsIgnoreCase(""))
					{
						Window.alert("The date " + getUncheckedText() + " is not a valid date.  " +
								"Date must be of the form " + dateFormat.replaceAll("\\/", "\\" + separator));
						if (resetDate)
							setText(dateFormat);
					}
					else if (!valid)
					{
						date.setText(dateFormat);
					}
				}

			}

			public void onFocus(Widget sender) {	}
		
		});
	}
	
	public String getSeparator()
	{
		return separator;
	}
	
	public void setSeparator(String separator)
	{
		this.separator = separator;
	}
	
	private String getYear(String dateEntered) 
	{
		String yearString = null;
		try {
			if (dateFormat.equalsIgnoreCase(InternationalDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				yearString = dateEntered.substring(0, firstIndex);
			}
			else if (dateFormat.equalsIgnoreCase(AmericanDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				int secondIndex = dateEntered.indexOf(separator, firstIndex+1);
				yearString = dateEntered.substring(secondIndex+1);
			}
			
			else if (dateFormat.equalsIgnoreCase(EuropeanDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				int secondIndex = dateEntered.indexOf(separator, firstIndex+1);
				yearString = dateEntered.substring(secondIndex+1);
			}
		}
		catch (Exception e){}
		return yearString;
		
	}
	
	private String getMonth(String dateEntered)
	{
		String monthString = null;
		try {
			if (dateFormat.equalsIgnoreCase(InternationalDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				int secondIndex = dateEntered.indexOf(separator, firstIndex+1);
				monthString = dateEntered.substring(firstIndex + 1, secondIndex);
			}
			else if (dateFormat.equalsIgnoreCase(AmericanDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				monthString = dateEntered.substring(0, firstIndex);
			}
			else if (dateFormat.equalsIgnoreCase(EuropeanDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				int secondIndex = dateEntered.indexOf(separator, firstIndex+1);
				monthString = dateEntered.substring(firstIndex + 1, secondIndex);
			}
		}
		catch (Exception e){}
		return monthString;
	}
	
	private String getDay(String dateEntered)
	{
		String dayString = null;
		try {
			if (dateFormat.equalsIgnoreCase(InternationalDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				int secondIndex = dateEntered.indexOf(separator, firstIndex+1);
				dayString = dateEntered.substring(secondIndex+1);
			}
			else if (dateFormat.equalsIgnoreCase(AmericanDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				int secondIndex = dateEntered.indexOf(separator, firstIndex+1);
				dayString = dateEntered.substring(firstIndex + 1, secondIndex);
			}
			else if (dateFormat.equalsIgnoreCase(EuropeanDate))
			{
				int firstIndex = dateEntered.indexOf(separator);
				dayString = dateEntered.substring(0, firstIndex);
			}
		}
		catch (Exception e){}
		return dayString;
	}
		
	private boolean checkDateValidity()
	{
		boolean validDate = true;

		String dateEntered = getUncheckedText();

		String yearString = getYear(dateEntered);
		String monthString = getMonth(dateEntered);
		String dayString = getDay(dateEntered);
		try
		{
			int year = Integer.parseInt(yearString);
			int month = Integer.parseInt(monthString);
			int day = Integer.parseInt(dayString);

			if (year >= earliestYear && year <= lastYear  )
			{
				if (month == 1 || month == 3 || month == 5 || month == 7 || 
						month == 8 || month == 10 || month == 12)
				{
					if (day < 1 || day > 31)
					{
						validDate = false;
					}
				}
				else if ( month == 4 || month == 6 || month == 9 || 
						month == 11)
				{
					if (day < 1 || day > 30)
					{
						validDate = false;
					}
				}
				else if ( month == 2)
				{
					if (isLeapYear(year))
					{
						if (day < 1 || day > 29)
						{
							validDate = false;
						}
					}
					else if (day < 1 || day > 28)
					{
						validDate = false;
					}

				}
				else
					validDate = false;
			}
			else
			{
				validDate = false;
			}
		}
		catch (Exception e)
		{
			validDate = false;
		}
	
		if (validDate)
		{
			formatDate(yearString, monthString, dayString);
		}
		return validDate;
	}

	private void formatDate(String year, String month, String day)
	{
		
		day = day.length() < 2 ? "0" + day : day;
		month = month.length() < 2 ? "0" + month : month;
		
		if (dateFormat.equalsIgnoreCase(InternationalDate))
		{
			setText(year + separator + month + separator + day);
		}
		else if (dateFormat.equalsIgnoreCase(AmericanDate))
		{
			setText( month + separator + day + separator + year);
		}
		
		else if (dateFormat.equalsIgnoreCase(EuropeanDate))
		{
			setText( day + separator + month + separator + year);
		}
	}
	
	private void showCalendar(final int left, final int top) {
		if (getText().equalsIgnoreCase("") || !checkDateValidity())
		{
			final CalendarWidget calendar = new CalendarWidget(dateFormat, monthSelector, selectWeek);
			calendar.setSeparator(separator);
			calendar.addChangeListener(new ChangeListener() {
				public void onChange(final Widget sender) {
					date.setText(calendar.getFormattedDate());
					fireListeners();
					popupPanel.hide();
				}
			});
			popupPanel.setWidget(calendar);
			popupPanel.setPopupPosition(left, top);
			popupPanel.show();

		}
		else
		{
			String dateText = getUncheckedText();
			final CalendarWidget calendar = new CalendarWidget(dateFormat, monthSelector, 
					getDay(dateText), getMonth(dateText), getYear(dateText), selectWeek);
			calendar.setSeparator(separator);
			calendar.addChangeListener(new ChangeListener() {
				public void onChange(final Widget sender) {
					date.setText(calendar.getFormattedDate());
					fireListeners();
					popupPanel.hide();
				}
			});
			popupPanel.setWidget(calendar);
			popupPanel.setPopupPosition(left, top);
			popupPanel.show();
		}
		
		
	}
	
	/**
	 * if a value has been entered, returns the date, otherwise 
	 * returns an empty string
	 * @return
	 */
	public String getText() {
		String returnText = date.getText();
		if (returnText.equalsIgnoreCase(dateFormat))
			returnText = "";
		return returnText;
	}
	
	private String getUncheckedText() {
		return date.getText();
	}
	
	
	/**
	 * Warning -- does not check for validity of date first.
	 * @param text
	 */
	public void setText(String text) {
		if (text.trim().equalsIgnoreCase(""))
		{
			clearText();
		}
		else{
			date.setText(text);
			fireListeners();
		}
	}
	
	public void setEnabled(boolean enable)
	{
		date.setEnabled(enable);
		img.setVisible(enable);
	}
	
	public void clearText(){
		date.setText(dateFormat);
		fireListeners();
	}
	
	public void setFirstAllowableYear(int year)
	{
		earliestYear = year;
	}
	
	public void setLastAllowableYear(int year)
	{
		lastYear = year;
	}
	
	
	private boolean isLeapYear(int year)
	{
		boolean isLeapYear = false;
		if (year % 100 == 0)
		{
			if (year % 400 == 0)
				isLeapYear = true;
		}
		else if (year % 4 == 0)
		{
			isLeapYear = true;
		}
		return isLeapYear;
	}
	
	public void addChangeListener(ChangeListener listener){
		listeners.add(listener);
	}
	private void fireListeners(){
		for(int i=0;i<listeners.size();i++){
			((ChangeListener)listeners.get(i)).onChange(this);
		}
	}
}
