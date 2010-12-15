package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.Map;

import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.BooleanRangePrimitiveField;
import org.iucn.sis.shared.api.utils.XMLUtils;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.extjs.client.WindowUtils;

public class SISBooleanRange extends SISPrimitiveStructure<String> implements DominantStructure<PrimitiveField<String>> {

	private ListBox options;
	private TextBox range;

	public SISBooleanRange(String struct, String descript, String structID) {
		super(struct, descript, structID);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}

	@Override
	protected BooleanRangePrimitiveField getNewPrimitiveField() {
		return new BooleanRangePrimitiveField(getId(), null);
	}
	
	@Override
	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
			KeyboardListener keyboardListener) {
		range.addKeyboardListener(keyboardListener);
		options.addChangeListener(changeListener);
		DOM.setEventListener(range.getElement(), range);
		DOM.setEventListener(options.getElement(), options);
	}

	public void checkValidText(String rangeText) {
		// CHECK TO MAKE SURE THE RANGE IS IN CORRECT FORM
		if (!(rangeText.equals("") || rangeText.matches("(\\s)*(0)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(0)*(\\.)?(\\d)+))?") || rangeText
				.matches("(\\s)*(0)*(\\.)?(\\d)+(\\s)*(-)(\\s)*(0)*(\\.)?(\\d)+(\\s)*"
						+ "(,)(\\s)*(0)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(0)*(\\.)?(\\d)+))?"))) {
			// MessageBox alert = new MessageBox(Style.ICON_ERROR, Style.MODAL |
			// Style.OK);
			// alert.setMessage(
			// "Invalid range. Must fit one of the following forms:\r\n" +
			// "{0-100} {0-100,50} {0-100, 25-75} or simply {50}. Please re-enter your data."
			// );
			// alert.setText("INVALID DATA");
			//			
			// alert.setMinimumWidth(400);
			// range.setText("");
			SISRangeError errorMessage = new SISRangeError();
			// alert.show();

		}

		// MAKE SURE THE RANGE IS VALID
		else if (!rangeText.equals("")) {
			String[] ranges = rangeText.split(",");
			boolean sendError = false;
			float firstNum = 0;
			float secondNum = 0;
			float first = 0;
			float second = 0;
			String message = null;

			for (int i = 0; i < ranges.length && !sendError; i++) {
				String[] numbers = ranges[i].split("-");

				if (numbers.length == 2) {
					first = Float.valueOf(numbers[0]).floatValue();
					second = Float.valueOf(numbers[1]).floatValue();
					if (i == 0) {
						firstNum = Float.valueOf(numbers[0]).floatValue();
						secondNum = Float.valueOf(numbers[1]).floatValue();

					}
				} else {
					first = Float.valueOf(numbers[0]).floatValue();
					second = first;
				}
				if (first > 1 || second > 1) {
					sendError = true;
					message = "Since this is a true or false, the number must be between 0 and 1.  Please re-enter your number";
				} else if ((i > 0)
						&& ((Float.compare(firstNum, first) > 0) || (Float.compare(first, secondNum) > 0)
								|| (Float.compare(second, firstNum) < 0) || (Float.compare(second, secondNum) > 0))) {
					sendError = true;
				} else if (first > second) {
					sendError = true;
				}

			}
			if (sendError) {

				if (message == null)
					WindowUtils
							.errorAlert("You must enter a valid range, with the first number less than the second number");
				else
					WindowUtils.errorAlert(message);
				range.setText("");
			} else {
				options.setSelectedIndex(3);
			}
		}
	}

	@Override
	public void clearData() {
		range.setText("");
		range.setVisible(true);
		options.setSelectedIndex(3);
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(options);
		displayPanel.add(new HTML("&nbsp&nbsp"));
		displayPanel.add(range);
		return displayPanel;
	}

	@Override
	public Widget createViewOnlyLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
		displayPanel.add(new HTML(range.getText()));
		return displayPanel;
	}

	@Override
	public void createWidget() {
		this.descriptionLabel = new HTML(this.description);

		KeyboardListenerAdapter listener = new KeyboardListenerAdapter() {
			@Override
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (Character.isLetter(keyCode)) {// || Character.isWhitespace(
					// keyCode)) {
					((TextBox) sender).cancelKey();
				}
			}
		};

		range = new TextBox();
		range.addKeyboardListener(listener);
		range.addFocusListener(new FocusListener() {
			public void onFocus(Widget sender) {
			}

			public void onLostFocus(Widget sender) {
				checkValidText(range.getText());
			}
		});
		range.setVisible(false);

		options = new ListBox();
		options.addItem("Unknown");
		options.addItem("Yes");
		options.addItem("No");
		options.addItem("Custom");
		options.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				int selected = options.getSelectedIndex();

				if (selected == 0) {
					range.setText(".5");
					range.setVisible(false);
				} else if (selected == 1) {
					range.setText("1");
					range.setVisible(false);
				} else if (selected == 2) {
					range.setText("0");
					range.setVisible(false);
				} else if (selected == 3) {
					range.setVisible(true);
				}
			}
		});

		options.setSelectedIndex(3);
		range.setVisible(true);
	}

	@Override
	public String getData() {
		String value = range.getText().replaceAll("\\s*", "");
		return ("".equals(value)) ? null : value;
	}

	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		String data = (String) rawData.get(offset);
		if (data.length() == 0) {
			prettyData.add(offset, "(Not Specified)");
		} else {
			if (data.equals(".5"))
				prettyData.add(offset, "Unknown");
			else if (data.equals("1"))
				prettyData.add(offset, "Yes");
			else if (data.equals("0"))
				prettyData.add(offset, "No");
			else
				prettyData.add(offset, "Custom range: " + data);
		}
		return ++offset;
	}

	@Override
	public boolean isActive(Rule activityRule) {
		try {
			return ((ContentRule) activityRule).matchesContent(range.getText());
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void setData(PrimitiveField<String> field) {
		String datum;
		if (field != null)
			datum = field.getValue();
		else
			datum = "";
		
		range.setText(XMLUtils.cleanFromXML(datum.replaceAll("\\s*", "")));

		if (range.getText() != null && range.getText() != "")
			if (range.getText().equals("1")) {
				options.setSelectedIndex(1);
				range.setVisible(false);
			} else if (range.getText().equals("0")) {
				options.setSelectedIndex(2);
				range.setVisible(false);
			} else if (range.getText().equals(".5")) {
				options.setSelectedIndex(0);
				range.setVisible(false);
			} else {
				options.setSelectedIndex(3);
				range.setVisible(true);
			}
		else {
			options.setSelectedIndex(3);
			range.setVisible(true);
		}
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		range.setEnabled(isEnabled);
	}

	public String toXML() {
		return StructureSerializer.toXML(this);
	}

}
