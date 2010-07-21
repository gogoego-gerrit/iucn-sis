/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.solertium.util.querybuilder.utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Snippets of code were taken from GWT's DateTimeFormat and placed here. Want
 * this project to remain portable.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class SQLDateTimeFormat {
	
	public static final String DATE_FORMAT = "yyyy-MM-dd"; 

	private static final String NUMERIC_FORMAT_CHARS = "MydhHmsSDkK";
	private static final String PATTERN_CHARS = "GyMdkHmsSEDahKzZv";
	private static final String WHITE_SPACE = " \t\r\n";
	private static final String GMT = "GMT";
	private static final int MINUTES_PER_HOUR = 60;

	private static final int NUMBER_BASE = 10;
	private static final int JS_START_YEAR = 1900;

	private final ArrayList<PatternPart> patternParts = new ArrayList<PatternPart>();

	private final String pattern;
	
	public static SQLDateTimeFormat getInstance() {
		return new SQLDateTimeFormat(DATE_FORMAT);
	}
	
	public static SQLDateTimeFormat createInstance() {
		return createInstance(DATE_FORMAT);
	}
	
	/**
	 * Note that you are very limited in the patterns you 
	 * can create here; right now only year, month, and day 
	 * are supported.  If you want more, feel free to add 
	 * it to the format() function
	 * @param pattern
	 * @return
	 */
	public static SQLDateTimeFormat createInstance(String pattern) {
		return new SQLDateTimeFormat(pattern);
	}

	protected SQLDateTimeFormat(String pattern) {
		this.pattern = pattern;

		/*
		 * Even though the pattern is only compiled for use in parsing and
		 * parsing is far less common than formatting, the pattern is still
		 * parsed eagerly here to fail fast in case the pattern itself is
		 * malformed.
		 */
		parsePattern(pattern);
	}

	/**
	 * Format a date object.
	 * 
	 * @param date
	 *            the date object being formatted
	 * 
	 * @return formatted date representation
	 */
	public String format(Date date) {
		StringBuffer toAppendTo = new StringBuffer(64);
		int j, n = pattern.length();
		for (int i = 0; i < n;) {
			char ch = pattern.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
				// ch is a date-time pattern character to be interpreted by
				// subFormat().
				// Count the number of times it is repeated.
				for (j = i + 1; j < n && pattern.charAt(j) == ch; ++j) {
				}
				subFormat(toAppendTo, ch, j - i, date);
				i = j;
			} else if (ch == '\'') {
				// Handle an entire quoted string, included embedded
				// doubled apostrophes (as in 'o''clock').

				// i points after '.
				++i;

				// If start with '', just add ' and continue.
				if (i < n && pattern.charAt(i) == '\'') {
					toAppendTo.append('\'');
					++i;
					continue;
				}

				// Otherwise add the quoted string.
				boolean trailQuote = false;
				while (!trailQuote) {
					// j points to next ' or EOS.
					j = i;
					while (j < n && pattern.charAt(j) != '\'') {
						++j;
					}

					if (j >= n) {
						// Trailing ' (pathological).
						throw new IllegalArgumentException(
								"Missing trailing \'");
					}

					// Look ahead to detect '' within quotes.
					if (j + 1 < n && pattern.charAt(j + 1) == '\'') {
						++j;
					} else {
						trailQuote = true;
					}
					toAppendTo.append(pattern.substring(i, j));
					i = j + 1;
				}
			} else {
				// Append unquoted literal characters.
				toAppendTo.append(ch);
				++i;
			}
		}

		return toAppendTo.toString();
	}

	/**
	 * Formats a single field according to pattern specified.
	 * 
	 * @param buf
	 *            where formatted string will be appended to
	 * @param ch
	 *            pattern for this field
	 * @param count
	 *            number of time pattern char repeats; this controls how a field
	 *            should be formatted
	 * @param date
	 *            hold the date object to be formatted
	 * 
	 * @return <code>true</code> if pattern valid, otherwise <code>false</code>
	 */
	private boolean subFormat(StringBuffer buf, char ch, int count, Date date) {
		switch (ch) {
		case 'y':
			formatYear(buf, count, date);
			break;
		case 'M':
			formatMonth(buf, count, date);
			break;
		case 'd':
			formatDate(buf, count, date);
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * Formats Month field according to pattern specified.
	 * 
	 * @param buf
	 *            where formatted string will be appended to
	 * @param count
	 *            number of time pattern char repeats; this controls how a field
	 *            should be formatted
	 * @param date
	 *            hold the date object to be formatted
	 */
	private void formatMonth(StringBuffer buf, int count, Date date) {
		int value = date.getMonth();
		zeroPaddingNumber(buf, value + 1, count);

	}

	/**
	 * Formats Date field according to pattern specified.
	 * 
	 * @param buf
	 *            where formatted string will be appended to
	 * @param count
	 *            number of time pattern char repeats; this controls how a field
	 *            should be formatted
	 * @param date
	 *            hold the date object to be formatted
	 */
	private void formatDate(StringBuffer buf, int count, Date date) {
		int value = date.getDate();
		zeroPaddingNumber(buf, value, count);
	}

	/**
	 * Formats Year field according to pattern specified. Javascript Date object
	 * seems incapable handling 1BC and year before. It can show you year 0
	 * which does not exists. following we just keep consistent with
	 * javascript's toString method. But keep in mind those things should be
	 * unsupported.
	 * 
	 * @param buf
	 *            where formatted string will be appended to
	 * @param count
	 *            number of time pattern char repeats; this controls how a field
	 *            should be formatted
	 * @param date
	 *            hold the date object to be formatted
	 */
	private void formatYear(StringBuffer buf, int count, Date date) {
		int value = date.getYear() + JS_START_YEAR;
		if (value < 0) {
			value = -value;
		}
		if (count == 2) {
			zeroPaddingNumber(buf, value % 100, 2);
		} else {
			// count != 2
			buf.append(Integer.toString(value));
		}
	}

	/**
	 * Formats a number with the specified minimum number of digits, using zero
	 * to fill the gap.
	 * 
	 * @param buf
	 *            where zero padded string will be written to
	 * @param value
	 *            the number value being formatted
	 * @param minWidth
	 *            minimum width of the formatted string; zero will be padded to
	 *            reach this width
	 */
	private void zeroPaddingNumber(StringBuffer buf, int value, int minWidth) {
		int b = NUMBER_BASE;
		for (int i = 0; i < minWidth - 1; i++) {
			if (value < b) {
				buf.append('0');
			}
			b *= NUMBER_BASE;
		}
		buf.append(Integer.toString(value));
	}

	/**
	 * Parses text to produce a {@link Date} value. An
	 * {@link IllegalArgumentException} is thrown if either the text is empty or
	 * if the parse does not consume all characters of the text.
	 * 
	 * Dates are parsed leniently, so invalid dates will be wrapped around as
	 * needed. For example, February 30 will wrap to March 2.
	 * 
	 * @param text
	 *            the string being parsed
	 * @return a parsed date/time value
	 * @throws IllegalArgumentException
	 *             if the entire text could not be converted into a number
	 */
	public Date parse(String text) throws IllegalArgumentException {
		return parse(text, false);
	}

	/**
	 * Parses text to produce a {@link Date} value. An
	 * {@link IllegalArgumentException} is thrown if either the text is empty or
	 * if the parse does not consume all characters of the text.
	 * 
	 * If using lenient parsing, certain invalid dates and times will be parsed.
	 * For example, February 32nd would be parsed as March 4th in lenient mode,
	 * but would throw an exception in non-lenient mode.
	 * 
	 * @param text
	 *            the string being parsed
	 * @param strict
	 *            true to be strict when parsing, false to be lenient
	 * @return a parsed date/time value
	 * @throws IllegalArgumentException
	 *             if the entire text could not be converted into a number
	 */
	private Date parse(String text, boolean strict) {
		Date curDate = new Date();
		Date date = new Date(curDate.getYear(), curDate.getMonth(), curDate
				.getDate());
		int charsConsumed = parse(text, 0, date, strict);
		if (charsConsumed == 0 || charsConsumed < text.length()) {
			throw new IllegalArgumentException(text);
		}
		return date;
	}

	/**
	 * This method parses the input string and fills its value into a
	 * {@link Date}.
	 * 
	 * If using lenient parsing, certain invalid dates and times will be parsed.
	 * For example, February 32nd would be parsed as March 4th in lenient mode,
	 * but would return 0 in non-lenient mode.
	 * 
	 * @param text
	 *            the string that need to be parsed
	 * @param start
	 *            the character position in "text" where parsing should start
	 * @param date
	 *            the date object that will hold parsed value
	 * @param strict
	 *            true to be strict when parsingm false to be lenient
	 * 
	 * @return 0 if parsing failed, otherwise the number of characters advanced
	 */
	private int parse(String text, int start, Date date, boolean strict) {
		DateRecord cal = new DateRecord();
		int[] parsePos = { start };

		// For parsing abutting numeric fields. 'abutPat' is the
		// offset into 'pattern' of the first of 2 or more abutting
		// numeric fields. 'abutStart' is the offset into 'text'
		// where parsing the fields begins. 'abutPass' starts off as 0
		// and increments each time we try to parse the fields.
		int abutPat = -1; // If >=0, we are in a run of abutting numeric fields.
		int abutStart = 0;
		int abutPass = 0;

		for (int i = 0; i < patternParts.size(); ++i) {
			PatternPart part = patternParts.get(i);

			if (part.count > 0) {
				if (abutPat < 0 && part.abutStart) {
					abutPat = i;
					abutStart = start;
					abutPass = 0;
				}

				// Handle fields within a run of abutting numeric fields. Take
				// the pattern "HHmmss" as an example. We will try to parse
				// 2/2/2 characters of the input text, then if that fails,
				// 1/2/2. We only adjust the width of the leftmost field; the
				// others remain fixed. This allows "123456" => 12:34:56, but
				// "12345" => 1:23:45. Likewise, for the pattern "yyyyMMdd" we
				// try 4/2/2, 3/2/2, 2/2/2, and finally 1/2/2.
				if (abutPat >= 0) {
					// If we are at the start of a run of abutting fields, then
					// shorten this field in each pass. If we can't shorten
					// this field any more, then the parse of this set of
					// abutting numeric fields has failed.
					int count = part.count;
					if (i == abutPat) {
						count -= abutPass++;
						if (count == 0) {
							return 0;
						}
					}

					if (!subParse(text, parsePos, part, count, cal)) {
						// If the parse fails anywhere in the run, back up to
						// the
						// start of the run and retry.
						i = abutPat - 1;
						parsePos[0] = abutStart;
						continue;
					}
				} else {
					// Handle non-numeric fields and non-abutting numeric
					// fields.
					abutPat = -1;
					if (!subParse(text, parsePos, part, 0, cal)) {
						return 0;
					}
				}
			} else {
				// Handle literal pattern characters. These are any
				// quoted characters and non-alphabetic unquoted characters.
				abutPat = -1;
				// A run of white space in the pattern matches a run
				// of white space in the input text.
				if (part.text.charAt(0) == ' ') {
					// Advance over run in input text.
					int s = parsePos[0];
					skipSpace(text, parsePos);

					// Must see at least one white space char in input.
					if (parsePos[0] > s) {
						continue;
					}
				} else if (text.startsWith(part.text, parsePos[0])) {
					parsePos[0] += part.text.length();
					continue;
				}

				// We fall through to this point if the match fails.
				return 0;
			}
		}

		// Calculate the date from the parts
		if (!cal.calcDate(date, strict)) {
			return 0;
		}

		// Return progress.
		return parsePos[0] - start;
	}

	/**
	 * Method parses the input pattern string a generate a vector of pattern
	 * parts.
	 * 
	 * @param pattern
	 *            describe the format of date string that need to be parsed
	 */
	private void parsePattern(String pattern) {
		StringBuffer buf = new StringBuffer(32);
		boolean inQuote = false;

		for (int i = 0; i < pattern.length(); i++) {
			char ch = pattern.charAt(i);

			// Handle space, add literal part (if exist), and add space part.
			if (ch == ' ') {
				addPart(buf, 0);
				buf.append(' ');
				addPart(buf, 0);
				while (i + 1 < pattern.length() && pattern.charAt(i + 1) == ' ') {
					i++;
				}
				continue;
			}

			// If inside quote, except two quote connected, just copy or exit.
			if (inQuote) {
				if (ch == '\'') {
					if (i + 1 < pattern.length()
							&& pattern.charAt(i + 1) == '\'') {
						// Quote appeared twice continuously, interpret as one
						// quote.
						buf.append(ch);
						++i;
					} else {
						inQuote = false;
					}
				} else {
					// Literal.
					buf.append(ch);
				}
				continue;
			}

			// Outside quote now.
			if (PATTERN_CHARS.indexOf(ch) > 0) {
				addPart(buf, 0);
				buf.append(ch);
				int count = getNextCharCountInPattern(pattern, i);
				addPart(buf, count);
				i += count - 1;
				continue;
			}

			// Two consecutive quotes is a quote literal, inside or outside of
			// quotes.
			if (ch == '\'') {
				if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '\'') {
					buf.append('\'');
					i++;
				} else {
					inQuote = true;
				}
			} else {
				buf.append(ch);
			}
		}

		addPart(buf, 0);

		identifyAbutStart();
	}

	/**
	 * Converts one field of the input string into a numeric field value.
	 * Returns <code>false</code> if failed.
	 * 
	 * @param text
	 *            the time text to be parsed
	 * @param pos
	 *            Parse position
	 * @param part
	 *            the pattern part for this field
	 * @param digitCount
	 *            when greater than 0, numeric parsing must obey the count
	 * @param cal
	 *            DateRecord object that will hold parsed value
	 * 
	 * @return <code>true</code> if parsing successful
	 */
	@SuppressWarnings("fallthrough")
	private boolean subParse(String text, int[] pos, PatternPart part,
			int digitCount, DateRecord cal) {

		skipSpace(text, pos);

		int start = pos[0];
		char ch = part.text.charAt(0);

		// Parse integer value if it is a numeric field.
		int value = -1; // initialize value to be -1,
		if (isNumeric(part)) {
			if (digitCount > 0) {
				if ((start + digitCount) > text.length()) {
					return false;
				}
				value = parseInt(text.substring(0, start + digitCount), pos);
			} else {
				value = parseInt(text, pos);
			}
		}

		switch (ch) {
		case 'G': // 'G' - ERA
			value = matchString(text, start, new String[] { "BC", "AD" }, pos);
			cal.setEra(value);
			return true;
		case 'M': // 'M' - MONTH
			return subParseMonth(text, pos, cal, value, start);
		case 'E':
			return subParseDayOfWeek(text, pos, start, cal);
		case 'a': // 'a' - AM_PM
			value = matchString(text, start, new String[] { "AM", "PM" }, pos);
			cal.setAmpm(value);
			return true;
		case 'y': // 'y' - YEAR
			return subParseYear(text, pos, start, value, part, cal);
		case 'd': // 'd' - DATE
			cal.setDayOfMonth(value);
			return true;
		case 'S': // 'S' - FRACTIONAL_SECOND
			return subParseFractionalSeconds(value, start, pos[0], cal);
		case 'h': // 'h' - HOUR (1..12)
			if (value == 12) {
				value = 0;
			}
			// fall through
		case 'K': // 'K' - HOUR (0..11)
		case 'H': // 'H' - HOUR_OF_DAY (0..23)
			cal.setHours(value);
			return true;
		case 'k': // 'k' - HOUR_OF_DAY (1..24)
			cal.setHours(value);
			return true;
		case 'm': // 'm' - MINUTE
			cal.setMinutes(value);
			return true;
		case 's': // 's' - SECOND
			cal.setSeconds(value);
			return true;

		case 'z': // 'z' - ZONE_OFFSET
		case 'Z': // 'Z' - TIMEZONE_RFC
		case 'v': // 'v' - TIMEZONE_GENERIC
			return subParseTimeZoneInGMT(text, start, pos, cal);
		default:
			return false;
		}
	}

	/**
	 * Method attempts to match the text at a given position against an array of
	 * strings. Since multiple strings in the array may match (for example, if
	 * the array contains "a", "ab", and "abc", all will match the input string
	 * "abcd") the longest match is returned.
	 * 
	 * @param text
	 *            the time text being parsed
	 * @param start
	 *            where to start parsing
	 * @param data
	 *            the string array to parsed
	 * @param pos
	 *            to receive where the match stopped
	 * @return the new start position if matching succeeded; a negative number
	 *         indicating matching failure
	 */
	private int matchString(String text, int start, String[] data, int[] pos) {
		int count = data.length;

		// There may be multiple strings in the data[] array which begin with
		// the same prefix (e.g., Cerven and Cervenec (June and July) in Czech).
		// We keep track of the longest match, and return that. Note that this
		// unfortunately requires us to test all array elements.
		int bestMatchLength = 0, bestMatch = -1;
		String textInLowerCase = text.substring(start).toLowerCase();
		for (int i = 0; i < count; ++i) {
			int length = data[i].length();
			// Always compare if we have no match yet; otherwise only compare
			// against potentially better matches (longer strings).
			if (length > bestMatchLength
					&& textInLowerCase.startsWith(data[i].toLowerCase())) {
				bestMatch = i;
				bestMatchLength = length;
			}
		}
		if (bestMatch >= 0) {
			pos[0] = start + bestMatchLength;
		}
		return bestMatch;
	}

	/**
	 * Method subParseDayOfWeek parses day of the week field.
	 * 
	 * @param text
	 *            the time text to be parsed
	 * @param pos
	 *            Parse position
	 * @param start
	 *            from where parse start
	 * @param cal
	 *            DateRecord object that holds parsed value
	 * 
	 * @return <code>true</code> if parsing successful, otherwise
	 *         <code>false</code>
	 */
	private boolean subParseDayOfWeek(String text, int[] pos, int start,
			DateRecord cal) {
		int value;
		// 'E' - DAY_OF_WEEK
		// Want to be able to parse both short and long forms.
		// Try count == 4 (DDDD) first:
		value = matchString(text, start, new String[] { "Sunday", "Monday",
				"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" }, pos);
		if (value < 0) {
			value = matchString(text, start, new String[] { "Sun", "Mon",
					"Tue", "Wed", "Thu", "Fri", "Sat" }, pos);
		}
		if (value < 0) {
			return false;
		}
		cal.setDayOfWeek(value);
		return true;
	}

	/**
	 * Method subParseMonth parses Month field.
	 * 
	 * @param text
	 *            the time text to be parsed
	 * @param pos
	 *            Parse position
	 * @param cal
	 *            DateRecord object that will hold parsed value
	 * @param value
	 *            numeric value if this field is expressed using numberic
	 *            pattern
	 * @param start
	 *            from where parse start
	 * 
	 * @return <code>true</code> if parsing successful
	 */
	private boolean subParseMonth(String text, int[] pos, DateRecord cal,
			int value, int start) {
		// When month is symbols, i.e., MMM or MMMM, value will be -1.
		if (value < 0) {
			// Want to be able to parse both short and long forms.
			// Try count == 4 first:
			value = matchString(text, start, new String[] { "January",
					"February", "March", "April", "May", "June", "July",
					"August", "September", "October", "November", "December" },
					pos);
			if (value < 0) { // count == 4 failed, now try count == 3.
				value = matchString(text, start, new String[] { "Jan", "Feb",
						"Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
						"Nov", "Dec" }, pos);
			}
			if (value < 0) {
				return false;
			}
			cal.setMonth(value);
			return true;
		} else {
			cal.setMonth(value - 1);
			return true;
		}
	}

	/**
	 * Method subParseYear parse year field. Year field is special because 1,
	 * two digit year need to be resolved. 2, we allow year to take a sign. 3,
	 * year field participate in abut processing. In my testing, negative year
	 * does not seem working due to JDK (or redpill implementation) limitation.
	 * It is not a big deal so we don't worry about it. But keep the logic here
	 * so that we might want to replace DateRecord with our a calendar class.
	 * 
	 * @param text
	 *            the time text to be parsed
	 * @param pos
	 *            parse position
	 * @param start
	 *            where this field star
	 * @param value
	 *            integer value of yea
	 * @param part
	 *            the pattern part for this field
	 * @param cal
	 *            DateRecord object that will hold parsed value
	 * 
	 * @return <code>true</code> if successful
	 */
	private boolean subParseYear(String text, int[] pos, int start, int value,
			PatternPart part, DateRecord cal) {
		char ch = ' ';
		if (value < 0) {
			ch = text.charAt(pos[0]);
			// Check if it is a sign.
			if (ch != '+' && ch != '-') {
				return false;
			}
			++(pos[0]);
			value = parseInt(text, pos);
			if (value < 0) {
				return false;
			}
			if (ch == '-') {
				value = -value;
			}
		}

		// no sign, only 2 digit was actually parsed, pattern say it has 2
		// digit.
		if (ch == ' ' && (pos[0] - start) == 2 && part.count == 2) {
			// Assume for example that the defaultCenturyStart is 6/18/1903.
			// This means that two-digit years will be forced into the range
			// 6/18/1903 to 6/17/2003. As a result, years 00, 01, and 02
			// correspond to 2000, 2001, and 2002. Years 04, 05, etc. correspond
			// to 1904, 1905, etc. If the year is 03, then it is 2003 if the
			// other fields specify a date before 6/18, or 1903 if they specify
			// a
			// date afterwards. As a result, 03 is an ambiguous year. All other
			// two-digit years are unambiguous.
			Date date = new Date();
			int defaultCenturyStartYear = date.getYear() + 1900 - 80;
			int ambiguousTwoDigitYear = defaultCenturyStartYear % 100;
			cal.setAmbiguousYear(value == ambiguousTwoDigitYear);
			value += (defaultCenturyStartYear / 100) * 100
					+ (value < ambiguousTwoDigitYear ? 100 : 0);
		}
		cal.setYear(value);
		return true;
	}

	/**
	 * Method subParseFractionalSeconds parses fractional seconds field.
	 * 
	 * @param value
	 *            parsed numberic value
	 * @param start
	 * @param end
	 *            parse position
	 * @param cal
	 *            DateRecord object that holds parsed value
	 * @return <code>true</code> if parsing successful, otherwise
	 *         <code>false</code>
	 */
	private boolean subParseFractionalSeconds(int value, int start, int end,
			DateRecord cal) {
		// Fractional seconds left-justify.
		int i = end - start;
		if (i < 3) {
			while (i < 3) {
				value *= 10;
				i++;
			}
		} else {
			int a = 1;
			while (i > 3) {
				a *= 10;
				i--;
			}
			value = (value + (a >> 1)) / a;
		}
		cal.setMilliseconds(value);
		return true;
	}

	/**
	 * Method parses GMT type timezone.
	 * 
	 * @param text
	 *            the time text to be parsed
	 * @param start
	 *            from where parse start
	 * @param pos
	 *            Parse position
	 * @param cal
	 *            DateRecord object that holds parsed value
	 * 
	 * @return <code>true</code> if parsing successful, otherwise
	 *         <code>false</code>
	 */
	private boolean subParseTimeZoneInGMT(String text, int start, int[] pos,
			DateRecord cal) {
		// First try to parse generic forms such as GMT-07:00. Do this first
		// in case localized DateFormatZoneData contains the string "GMT"
		// for a zone; in that case, we don't want to match the first three
		// characters of GMT+/-HH:MM etc.

		// For time zones that have no known names, look for strings
		// of the form:
		// GMT[+-]hours:minutes or
		// GMT[+-]hhmm or
		// GMT.
		if (text.startsWith(GMT, start)) {
			pos[0] = start + GMT.length();
			return parseTimeZoneOffset(text, pos, cal);
		}

		// At this point, check for named time zones by looking through
		// the locale data from the DateFormatZoneData strings.
		// Want to be able to parse both short and long forms.
		/*
		 * i = subParseZoneString(text, start, cal); if (i != 0) return i;
		 */

		// As a last resort, look for numeric timezones of the form
		// [+-]hhmm as specified by RFC 822. This code is actually
		// a little more permissive than RFC 822. It will try to do
		// its best with numbers that aren't strictly 4 digits long.
		return parseTimeZoneOffset(text, pos, cal);
	}

	/**
	 * Method parses time zone offset.
	 * 
	 * @param text
	 *            the time text to be parsed
	 * @param pos
	 *            Parse position
	 * @param cal
	 *            DateRecord object that holds parsed value
	 * 
	 * @return <code>true</code> if parsing successful, otherwise
	 *         <code>false</code>
	 */
	private boolean parseTimeZoneOffset(String text, int[] pos, DateRecord cal) {
		if (pos[0] >= text.length()) {
			cal.setTzOffset(0);
			return true;
		}

		int sign;
		switch (text.charAt(pos[0])) {
		case '+':
			sign = 1;
			break;
		case '-':
			sign = -1;
			break;
		default:
			cal.setTzOffset(0);
			return true;
		}
		++(pos[0]);

		// Look for hours:minutes or hhmm.
		int st = pos[0];
		int value = parseInt(text, pos);
		if (value == 0 && pos[0] == st) {
			return false;
		}

		int offset;
		if (pos[0] < text.length() && text.charAt(pos[0]) == ':') {
			// This is the hours:minutes case.
			offset = value * MINUTES_PER_HOUR;
			++(pos[0]);
			st = pos[0];
			value = parseInt(text, pos);
			if (value == 0 && pos[0] == st) {
				return false;
			}
			offset += value;
		} else {
			// This is the hhmm case.
			offset = value;
			// Assume "-23".."+23" refers to hours.
			if (offset < 24 && (pos[0] - st) <= 2) {
				offset *= MINUTES_PER_HOUR;
			} else {
				offset = offset % 100 + offset / 100 * MINUTES_PER_HOUR;
			}
		}

		offset *= sign;
		cal.setTzOffset(-offset);
		return true;
	}

	/**
	 * Method parses a integer string and return integer value.
	 * 
	 * @param text
	 *            string being parsed
	 * @param pos
	 *            parse position
	 * 
	 * @return integer value
	 */
	private int parseInt(String text, int[] pos) {
		int ret = 0;
		int ind = pos[0];
		char ch = text.charAt(ind);
		while (ch >= '0' && ch <= '9') {
			ret = ret * 10 + (ch - '0');
			ind++;
			if (ind >= text.length()) {
				break;
			}
			ch = text.charAt(ind);
		}
		if (ind > pos[0]) {
			pos[0] = ind;
		} else {
			ret = -1;
		}
		return ret;
	}

	/**
	 * Method getNextCharCountInPattern calculate character repeat count in
	 * pattern.
	 * 
	 * @param pattern
	 *            describe the format of date string that need to be parsed
	 * @param start
	 *            the position of pattern character
	 * @return repeat count
	 */
	private int getNextCharCountInPattern(String pattern, int start) {
		char ch = pattern.charAt(start);
		int next = start + 1;
		while (next < pattern.length() && pattern.charAt(next) == ch) {
			++next;
		}
		return next - start;
	}

	/**
	 * Method append current content in buf as pattern part if there is any, and
	 * clear buf for next part.
	 * 
	 * @param buf
	 *            pattern part text specification
	 * @param count
	 *            pattern part repeat count
	 */
	private void addPart(StringBuffer buf, int count) {
		if (buf.length() > 0) {
			patternParts.add((new PatternPart(buf.toString(), count)));
			buf.setLength(0);
		}
	}

	/**
	 * Method identifies the start of a run of abutting numeric fields. Take the
	 * pattern "HHmmss" as an example. We will try to parse 2/2/2 characters of
	 * the input text, then if that fails, 1/2/2. We only adjust the width of
	 * the leftmost field; the others remain fixed. This allows "123456" =>
	 * 12:34:56, but "12345" => 1:23:45. Likewise, for the pattern "yyyyMMdd" we
	 * try 4/2/2, 3/2/2, 2/2/2, and finally 1/2/2. The first field of connected
	 * numeric fields will be marked as abutStart, its width can be reduced to
	 * accomodate others.
	 */
	private void identifyAbutStart() {
		// 'abut' parts are continuous numeric parts. abutStart is the switch
		// point from non-abut to abut.
		boolean abut = false;

		int len = patternParts.size();
		for (int i = 0; i < len; i++) {
			if (isNumeric(patternParts.get(i))) {
				// If next part is not following abut sequence, and isNumeric.
				if (!abut && i + 1 < len && isNumeric(patternParts.get(i + 1))) {
					abut = true;
					patternParts.get(i).abutStart = true;
				}
			} else {
				abut = false;
			}
		}
	}

	/**
	 * Method skips space in the string as pointed by pos.
	 * 
	 * @param text
	 *            input string
	 * @param pos
	 *            where skip start, and return back where skip stop
	 */
	private void skipSpace(String text, int[] pos) {
		while (pos[0] < text.length()
				&& WHITE_SPACE.indexOf(text.charAt(pos[0])) >= 0) {
			++(pos[0]);
		}
	}

	/**
	 * Method checks if the pattern part is a numeric field.
	 * 
	 * @param part
	 *            pattern part to be examined
	 * @return <code>true</code> if the pattern part is numberic field
	 */
	private boolean isNumeric(PatternPart part) {
		if (part.count <= 0) {
			return false;
		}
		int i = NUMERIC_FORMAT_CHARS.indexOf(part.text.charAt(0));
		return (i > 0 || (i == 0 && part.count < 3));
	}

	private class PatternPart {
		public String text;
		public int count; // 0 has a special meaning, it stands for literal
		public boolean abutStart;

		public PatternPart(String txt, int cnt) {
			text = txt;
			count = cnt;
			abutStart = false;
		}
	}

}
