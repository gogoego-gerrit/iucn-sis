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

package com.solertium.util.gwt.ui;

/*
 * Simple Calendar Widget for GWT Copyright (C) 2006 Alexei Sokolov
 * http://gwt.components.googlepages.com/
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * GWT 1.5 emulates the old deprecated Date API.  The SuppressWarnings flags
 * here ignore the deprecation warnings since we must use the deprecated API.
 * 
 */

import java.util.Date;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ChangeListenerCollection;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.gwt.api.DatePicker;

public class CalendarWidget extends Composite implements ClickListener,
		SourcesChangeEvents {
	private static class CellHTML extends HTML {
		private final int day;

		public CellHTML(final String text, final int day) {
			super(text);
			this.day = day;
		}

		public int getDay() {
			return day;
		}
	}

	private class NavBar extends Composite implements ClickListener {
		public final DockPanel bar = new DockPanel();
		private final CalendarWidget calendar;
		public final Button nextMonth = new Button("&gt;", this);
		public final Button nextYear = new Button("&gt;&gt;", this);
		public final Button prevMonth = new Button("&lt;", this);
		public final Button prevYear = new Button("&lt;&lt;", this);

		public final HTML title = new HTML();

		public NavBar(final CalendarWidget calendar, boolean withMonth) {
			this.calendar = calendar;

			// setWidget(bar);
			initWidget(bar);
			bar.setStyleName("navbar");
			title.setStyleName("header");

			prevYear.setTitle("Previous Year");
			prevMonth.setTitle("Previous Month");

			nextYear.setTitle("Next Year");
			nextMonth.setTitle("Next Year");

			final HorizontalPanel prevButtons = new HorizontalPanel();
			prevButtons.add(prevYear);
			if (withMonth)
				prevButtons.add(prevMonth);

			final HorizontalPanel nextButtons = new HorizontalPanel();
			if (withMonth)
				nextButtons.add(nextMonth);
			nextButtons.add(nextYear);

			bar.add(prevButtons, DockPanel.WEST);
			bar.setCellHorizontalAlignment(prevButtons,
					HasHorizontalAlignment.ALIGN_LEFT);
			bar.add(nextButtons, DockPanel.EAST);
			bar.setCellHorizontalAlignment(nextButtons,
					HasHorizontalAlignment.ALIGN_RIGHT);
			bar.add(title, DockPanel.CENTER);
			bar.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			bar.setCellHorizontalAlignment(title,
					HasHorizontalAlignment.ALIGN_CENTER);
			bar.setCellVerticalAlignment(title,
					HasVerticalAlignment.ALIGN_MIDDLE);
			bar.setCellWidth(title, "100%");
		}

		public void onClick(final Widget sender) {
			if (sender == prevMonth)
				calendar.prevMonth();
			else if (sender == prevYear)
				calendar.prevYear();
			else if (sender == nextYear)
				calendar.nextYear();
			else if (sender == nextMonth)
				calendar.nextMonth();
		}
	}

	private ChangeListenerCollection changeListeners;
	private Date date = new Date();
	private int yearEntered = 0;
	private int dayEntered = 0;
	private int monthEntered = 0;
	private String dateFormat;
	private boolean monthSelector;
	private boolean selectWeek;
	private final String[] days = new String[] { "S", "M", "T", "W", "T", "F",
			"S" };

	private FlexTable flexGrid = new FlexTable();
	private FlexTable monthGrid;
	private final String[] months = new String[] { "Jan", "Feb", "Mar", "Apr",
			"May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec" };

//	private final NavBar navbar = new NavBar(this, !monthSelector);
	private final NavBar navbar;
	private final DockPanel outer = new DockPanel();
	
	private String separator = "/";
	
	/**
	 * 
	 * @param dateFormat --DatePicker.InternationalDate, DatePicker.AmericanDate, DatePicker.EuropeanDate
	 */
	public CalendarWidget(String dateFormat, boolean monthSelector) {
		this(dateFormat,monthSelector,false);
	}
	public CalendarWidget(String dateFormat, boolean monthSelector, boolean selectWeek) {
		this.monthSelector = monthSelector;
		this.dateFormat = dateFormat;
		this.selectWeek=selectWeek;
		this.navbar = new NavBar(this, !monthSelector);
		initWidget(outer);
		drawCalendar();
		outer.add(navbar, DockPanel.NORTH);
		drawMonthPicker();			
		setStyleName("CalendarWidget");
	}
	
	/**
	 * Use for when a date is already entered
	 * @param dateFormat
	 * @param monthSelector
	 * @param day
	 * @param month
	 * @param year
	 */
	public CalendarWidget(String dateFormat, boolean monthSelector, String day, String month, String year) {
		this(dateFormat,monthSelector, day, month, year, false);
	}
	public CalendarWidget(String dateFormat, boolean monthSelector, String day, String month, String year, boolean selectWeek) {
		this.monthSelector = monthSelector;
		this.dateFormat = dateFormat;
		this.selectWeek=selectWeek;
		this.navbar = new NavBar(this, !monthSelector);
		initWidget(outer);
		try 
		{
			setDate(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day));
			setDateEntered(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day));
		}
		catch (NumberFormatException e)
		{	}
		drawCalendar();
		outer.add(navbar, DockPanel.NORTH);
		drawMonthPicker();			
		setStyleName("CalendarWidget");
	}
	

	/**
	 * 
	 * @param dateFormat --DatePicker.InternationalDate, DatePicker.AmericanDate, DatePicker.EuropeanDate
	 */
	public CalendarWidget(String dateFormat) {
		this(dateFormat, false);
	}
	
	public CalendarWidget() {
		this(DatePicker.AmericanDate);
	}

	public String getSeparator()
	{
		return separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}
	
	public void addChangeListener(final ChangeListener listener) {
		if (changeListeners == null)
			changeListeners = new ChangeListenerCollection();
		changeListeners.add(listener);
	}

	private void drawMonthPicker()
	{
		if (monthSelector)
		{
			monthGrid = new FlexTable();
			monthGrid.addStyleName("calendar-month");
			monthGrid.setCellSpacing(0);
			int numberPerRow = 7;
			for (int i = 0; i < numberPerRow; i++)
			{
				final Button button = new Button(months[i]);
				button.addClickListener(new ClickListener() {
				
					public void onClick(Widget sender) {
						setMonth(button.getText());
					}
				
				});
				button.addStyleName("calendar-month-button");
				button.setWidth("100%");
				monthGrid.setWidget(0, i, button );
				monthGrid.getColumnFormatter().setWidth(i, "33px");
//				monthGrid.getCellFormatter().setAlignment(0, i, 
//						HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
			}
			for (int i = 0; i < months.length-numberPerRow; i++)
			{
				final Button button = new Button(months[i+numberPerRow]);
				button.addClickListener(new ClickListener() {
					
					public void onClick(Widget sender) {
						setMonth(button.getText());
					}
				
				});
				button.addStyleName("calendar-month-button");
				button.setWidth("100%");
				monthGrid.setWidget(1, i, button);
			}
			
			outer.add(monthGrid, DockPanel.SOUTH);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void drawCalendar()
	{
		final int year = getYear();
		final int month = getMonth();
		setHeaderText(year, month);

		outer.remove(flexGrid);

		flexGrid = new FlexTable() {
			public boolean clearCell(final int row, final int column) {
				final boolean retValue = super.clearCell(row, column);
				final Element td = getCellFormatter().getElement(row, column);
				DOM.setInnerHTML(td, "");
				return retValue;
			}
		};

		flexGrid.setStyleName("table");
		flexGrid.setCellSpacing(0);

		for (int i = 0; i < days.length; i++) {
			flexGrid.setText(0, i, days[i]);
			flexGrid.getCellFormatter().setStyleName(0, i, "days");
		}
		flexGrid.getRowFormatter().setStyleName(0, "weekheader");

		
		int today = 0;
		final Date now = new Date();
		int sameDay = now.getDate();
		if (yearEntered == 0)
		{
			today = (now.getMonth() == month && now.getYear() + 1900 == year) ? sameDay
					: 0;
		}
		else
		{
			sameDay = dayEntered;
			today = (monthEntered == month && yearEntered + 1900 == year) ? dayEntered
					: 0;
		}
		
		final int firstDay = new Date(year - 1900, month, 1).getDay();
		final int numOfDays = getDaysInMonth(year, month);

		int j = 0;
		for (int i = 1; i < 7; i++){
			boolean continueToColor=true;
			if(selectWeek){
				for(int io=0;io<7;io++){
					flexGrid.getCellFormatter().addStyleName(i, io, "today");
					if(j+io-firstDay+1==today && today!=0){
						today = j-firstDay+1;
						continueToColor=false;
					}
				}
				
			}
			for (int k = 0; k < 7; k++, j++) {
				
				final int displayNum = (j - firstDay + 1);
				
				if (j < firstDay) {
					flexGrid.setHTML(i, k, "&nbsp;");
					if(continueToColor)flexGrid.getCellFormatter().setStyleName(i, k, "empty");
				} else if (displayNum > numOfDays)
					try {
						if (!flexGrid.getHTML(i, 0).equalsIgnoreCase("&nbsp;")) {
							flexGrid.setHTML(i, k, "&nbsp;");
							if(continueToColor)flexGrid.getCellFormatter().setStyleName(i, k,
									"empty");
						}
					} catch (final Exception e) {

					}
				else {
					int setNum=displayNum;
					if(selectWeek) setNum=j-firstDay+1-k;
					final HTML html = new CellHTML("<span>"
							+ String.valueOf(displayNum) + "</span>",
							setNum);
					html.addClickListener(this);

					flexGrid.setWidget(i, k, html);
					if(continueToColor)flexGrid.getCellFormatter().setStyleName(i, k, "cell");
					
					if (displayNum == today)
						flexGrid.getCellFormatter().addStyleName(i, k, "today");
					else if (displayNum == sameDay)
						if(continueToColor && !selectWeek)flexGrid.getCellFormatter().addStyleName(i, k, "day");
				}
				
			}
		}
		outer.add(flexGrid, DockPanel.CENTER);
	}
	
	

	public Date getDate() {
		return date;
	}

	@SuppressWarnings("deprecation")
	public int getDay() {
		return date.getDate();
	}

	private int getDaysInMonth(final int year, final int month) {
		switch (month) {
		case 1:
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				return 29; // leap year
			else
				return 28;
		case 3:
			return 30;
		case 5:
			return 30;
		case 8:
			return 30;
		case 10:
			return 30;
		default:
			return 31;
		}
	}

	public String getFormattedDate() {
		String day, month, year;

		final int theMonth = getMonth() + 1;

		day = getDay() < 10 ? "0" + getDay() : "" + getDay();
		month = theMonth < 10 ? "0" + theMonth : "" + theMonth;
		year = "" + getYear();

		if (dateFormat.equalsIgnoreCase(DatePicker.InternationalDate))
			return year + separator + month + separator + day;
		else if (dateFormat.equalsIgnoreCase(DatePicker.EuropeanDate))
		{
			return day + separator + month + separator + year;
		}
		else
		{
			return month + separator + day + separator + year;
		}
	}

	
	@SuppressWarnings("deprecation")
	public int getMonth() {
		return date.getMonth();
	}

	@SuppressWarnings("deprecation")
	public int getYear() {
		return 1900 + date.getYear();
	}

	public void nextMonth() {
		final int month = getMonth() + 1;
		if (month > 11)
			setDate(getYear() + 1, 0, getDay());
		else
			setMonth(month);
		drawCalendar();
	}

	public void nextYear() {
		setYear(getYear() + 1);
		drawCalendar();
	}

	public void onClick(final Widget sender) {
		final CellHTML cell = (CellHTML) sender;
		setDate(getYear(), getMonth(), cell.getDay());
		drawCalendar();
		if (changeListeners != null)
			changeListeners.fireChange(this);
	}

	public void prevMonth() {
		final int month = getMonth() - 1;
		if (month < 0)
			setDate(getYear() - 1, 11, getDay());
		else
			setMonth(month);
		drawCalendar();
	}

	public void prevYear() {
		setYear(getYear() - 1);
		drawCalendar();
	}
	
	public void setMonth(String month) 
	{
		boolean found = false;
		for (int i = 0; i < months.length; i++)
		{
			if (months[i].equalsIgnoreCase(month))
			{
				found = true;
				setMonth(i);
			}
		}
		if (found)
			drawCalendar();
	}
	

	public void removeChangeListener(final ChangeListener listener) {
		if (changeListeners != null)
			changeListeners.remove(listener);
	}

	@SuppressWarnings("deprecation")
	private void setDate(final int year, final int month, final int day) {
		date = new Date(year - 1900, month, day);
	}
	
	private void setDateEntered(final int year, final int month, final int day)
	{
		yearEntered = year - 1900;
		monthEntered = month;
		dayEntered = day;
	}

	protected void setHeaderText(final int year, final int month) {
		navbar.title.setText(months[month] + ", " + year);
	}
	
	@SuppressWarnings("deprecation")
	private void setMonth(final int month) {
		date.setMonth(month);
	}

	@SuppressWarnings("deprecation")
	private void setYear(final int year) {
		date.setYear(year - 1900);
	}
}