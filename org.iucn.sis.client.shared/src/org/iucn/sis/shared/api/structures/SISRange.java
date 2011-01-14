package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;

import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.RangePrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.extjs.client.WindowUtils;

public class SISRange extends SISPrimitiveStructure<String> implements DominantStructure<PrimitiveField<String>> {

	private TextBox range;

	public SISRange(String struct, String descript, String structID) {
		super(struct, descript, structID);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
	}
	
	@Override
	protected PrimitiveField<String> getNewPrimitiveField() {
		return new RangePrimitiveField(getId(), null);
	}

	@Override
	public void addListenerToActiveStructure(ChangeHandler changeListener, ClickHandler clickListener,
			KeyUpHandler keyboardListener) {
		range.addKeyUpHandler(keyboardListener);
		DOM.setEventListener(range.getElement(), range);
	}

	public static boolean validateRange(String rangeText) {
		// CHECK TO MAKE SURE THE RANGE IS IN CORRECT FORM
		if (!(rangeText.equals("")
				|| rangeText.matches("(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(\\d)*(\\.)?(\\d)+))?") || rangeText
				.matches("(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*(-)(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*"
						+ "(,)(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(\\d)*(\\.)?(\\d)+))?"))) {
			// MessageBox alert = new MessageBox(Style.ICON_ERROR, Style.MODAL |
			// Style.OK);
			// alert.setMessage(
			// "Invalid range. Must fit one of the following forms:\r\n" +
			// "{0-100} {0-100,50} {0-100, 25-75} or simply {50}. Please re-enter your data."
			// );
			// alert.setText("INVALID DATA");
			// alert.setMinimumWidth(400);
			// alert.show();
			
			new SISRangeError();
			return false;
		}

		// MAKE SURE THE RANGE IS VALID
		else if (!rangeText.equals("")) {
			String[] ranges = rangeText.split(",");
			boolean sendError = false;
			float firstNum = 0;
			float secondNum = 0;
			float first = 0;
			float second = 0;

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

				if ((i > 0)
						&& ((Float.compare(firstNum, first) > 0) || (Float.compare(first, secondNum) > 0)
								|| (Float.compare(second, firstNum) < 0) || (Float.compare(second, secondNum) > 0))) {
					sendError = true;
				} else if (first > second) {
					sendError = true;
				}

			}
			if (sendError) {
				WindowUtils
						.errorAlert("You must enter a valid range, with the first number less than the second number");
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void clearData() {
		range.setText("");
	}

	@Override
	public Widget createLabel() {
		clearDisplayPanel();
		displayPanel.add(descriptionLabel);
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

		range = new TextBox();
		range.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (Character.isLetter(event.getCharCode()))
					range.cancelKey();
			}
		});
		range.addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				if (!validateRange(range.getText()))
					range.setText("");
			}
		});
	}

	@Override
	public String getData() {
		String ret = range.getText().replaceAll("\\s*", "");
		if( ret.equals(""))
			return null;
		else
			return ret;
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
		prettyData.add(offset, rawData.get(offset));
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
		//super.setData(data);
		String datum = field != null ? field.getValue() : "";
		range.setText(datum.replaceAll("\\s*", ""));
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		range.setEnabled(isEnabled);
	}

}
