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
package com.solertium.util.portable;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * IDValidator.java
 * 
 * Validates a given string based on a set of standards and requirements that
 * can be set on the object.
 * 
 * @author carl.scott
 * 
 */
public class IDValidator {

	public static final Integer SUCCESS = new Integer(0);
	public static final Integer FAILURE_CASE = new Integer(1);
	public static final Integer FAILURE_STARTS_WITH_LETTER = new Integer(2);
	public static final Integer FAILURE_NO_WHITESPACE = new Integer(3);
	public static final Integer FAILURE_ALPHANUMERIC = new Integer(4);
	public static final Integer FAILURE_NO_ID_GIVEN = new Integer(5);

	private boolean toLowerCase = false;
	private boolean startWithLetter = false;
	private boolean noWhiteSpace = false;
	private boolean mustBeAlphanumeric = false;

	private char[] alphanumericExceptions = new char[0];

	private HashMap<Integer, String> standardErrorMessages;

	public IDValidator() {
		standardErrorMessages = new HashMap<Integer, String>();
		standardErrorMessages.put(FAILURE_CASE, "Input is required to be lowercase.");
		standardErrorMessages.put(FAILURE_STARTS_WITH_LETTER, "Input is required to start with a letter.");
		standardErrorMessages.put(FAILURE_NO_WHITESPACE, "Input is required " + "to have no whitespace.");
		standardErrorMessages.put(FAILURE_ALPHANUMERIC, "Input is required " + "to contain only letters and numbers.");
		standardErrorMessages.put(FAILURE_NO_ID_GIVEN, "No input or invalid input given.");
	}

	public String getStandardErrorMessage(Integer key) {
		return standardErrorMessages.get(key);
	}

	public void setLowercaseRestriction(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}

	public void setMustBeginWithLetter(boolean startWithLetter) {
		this.startWithLetter = startWithLetter;
	}

	public void setNoWhiteSpace(boolean noWhiteSpace) {
		this.noWhiteSpace = noWhiteSpace;
	}

	public void setMustBeAlphaNumeric(boolean isAlphaNumeric) {
		setMustBeAlphaNumeric(isAlphaNumeric, false);
	}

	public void setMustBeAlphaNumeric(boolean isAlphaNumeric, boolean allowWhitespace) {
		setMustBeAlphaNumeric(isAlphaNumeric, allowWhitespace, new char[0]);
	}

	public void setMustBeAlphaNumeric(boolean isAlphaNumeric, boolean allowWhitespace, char[] allowedChars) {
		this.mustBeAlphanumeric = isAlphaNumeric;
		StringBuffer buffer = new StringBuffer();
		buffer.append(allowedChars);
		if (allowWhitespace)
			buffer.append(new char[] { ' ', '\n', '\r', '\t' });
		this.alphanumericExceptions = buffer.toString().toCharArray();
	}

	/**
	 * Validates the given string based on the settings, and returns any
	 * failures and such.
	 * 
	 * @param id
	 *            the id to validate
	 * @return validation information
	 */
	public ValidationInfo validate(String id) {
		String potentialID = id;
		ValidationInfo obj = new ValidationInfo();

		/**
		 * A null id is always bad, and no suggestions can be made
		 */
		if (id == null || id.equals("")) {
			obj.addError(FAILURE_NO_ID_GIVEN);
			return obj;
		}

		/**
		 * If doesn't start with letter, we can't help them, user must re-input.
		 */
		if (startWithLetter && !Character.isLetter(potentialID.charAt(0))) {
			obj.addError(FAILURE_STARTS_WITH_LETTER);
			return obj;
		}

		/**
		 * Lowercase can be fixed, so we suggest such
		 */
		if (toLowerCase && !potentialID.equals(id.toLowerCase())) {
			potentialID = id.toLowerCase();
			obj.addError(FAILURE_CASE);
			obj.setSuggested(potentialID);
		}

		/**
		 * Whitespace can be fixed, so we suggest such
		 */
		String withoutSpace = PortableReplacer.stripWhitespace(potentialID);
		if (noWhiteSpace && !potentialID.equals(withoutSpace)) {
			obj.addError(FAILURE_NO_WHITESPACE);
			obj.setSuggested(potentialID = withoutSpace);
		}

		/**
		 * Alphanumeric can be fixed, so we suggest such
		 */
		String alnum = alphanumericExceptions.length == 0 ? PortableReplacer.stripNonalphanumeric(potentialID) : PortableReplacer
				.stripNonalphanumeric(potentialID, alphanumericExceptions);
		if (mustBeAlphanumeric && !potentialID.equals(alnum)) {
			obj.addError(FAILURE_ALPHANUMERIC);
			obj.setSuggested(potentialID = alnum);
		}

		return obj;
	}

	/**
	 * ValidationInfo.java
	 * 
	 * Validation information. Contains a list of errors (empty if there are
	 * none), and possibly a suggested id where possible.
	 * 
	 * @author carl.scott
	 * 
	 */
	public static class ValidationInfo {
		private ArrayList<Integer> errors;
		private String suggestedID;

		public ValidationInfo() {
			errors = new ArrayList<Integer>();
		}

		public boolean isValid() {
			return errors.isEmpty();
		}

		public void addError(Integer error) {
			errors.add(error);
		}

		public ArrayList<Integer> getErrors() {
			return errors;
		}

		public void setSuggested(String suggestedID) {
			this.suggestedID = (suggestedID.equals("") ? null : suggestedID);
		}

		public String getSuggestedID() {
			return suggestedID;
		}

	}
}

/*
 * SAMPLE CODE BLOCK
 * 
 * final IDValidator validator = new IDValidator();
 * validator.setLowercaseRestriction(true);
 * validator.setMustBeAlphaNumeric(true);
 * validator.setMustBeginWithLetter(false); validator.setNoWhiteSpace(true);
 * 
 * final IDValidator.ValidationInfo response = validator.validate(newID);
 * 
 * if (response.isValid()) { ... do stuff ... } else { if
 * (response.getErrors().contains(IDValidator.FAILURE_NO_ID_GIVEN)) { ... no id
 * given ... } else { String error = "The following errors occured: <br/>"; for
 * (int i = 0; i < response.getErrors().size(); i++) { error += " - " +
 * validator. getStandardErrorMessage((Integer)response.getErrors().get(i)) + "<br/>"; }
 * if (response.getSuggestedID() == null) { error += "Please fix these errors
 * and try again."; WindowUtils.errorAlert("Error", error); } else { error +=
 * "If you would like to use the suggested ID, <b>\"" +
 * response.getSuggestedID() + "\"</b>, click \"Yes\". otherwise, " + "click
 * \"No\" and enter a new ID."; WindowUtils.MessageBoxListener listener = new
 * WindowUtils.MessageBoxListener() { public void onNo() { } public void onYes() {
 * ... do stuff ... } }; WindowUtils.confirmAlert("Confirm", error, listener); } } }
 * 
 */