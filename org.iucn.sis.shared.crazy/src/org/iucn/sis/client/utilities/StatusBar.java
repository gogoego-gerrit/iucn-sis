package org.iucn.sis.client.utilities;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StatusBar extends HorizontalPanel {

	private HTML message, percentage;
	private HorizontalPanel status;
	private SimplePanel movingStatus;

	public StatusBar() {
		super();
		addStyleName("SIS_statusBar");

		message = new HTML();
		status = new HorizontalPanel();
		status.setWidth("500px");
		status.add(movingStatus = new SimplePanel());

		movingStatus.setWidget(percentage = new HTML());

		status.addStyleName("SIS_statusMovingBg");
		movingStatus.addStyleName("SIS_statusMovingFg");
		percentage.addStyleName("SIS_statusMovingPercentage");
		message.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(status);

		add(panel);

		setVisible(false);
	}

	public void clearMessage() {
		message.setText("");
		setVisible(false);
	}

	public void clearPercentage() {
		// percentage.setText("");
		// movingStatus.setWidth(0+"px");
		status.setVisible(false);
	}

	public HTML getMessage() {
		return message;
	}

	public Timer setClearStatusBarTimer(int millis) {
		Timer timer = new Timer() {
			@Override
			public void run() {
				clearMessage();
			}
		};
		timer.schedule(millis);

		return timer;
	}

	public void setMessage(String message) {
		this.message.setHTML(message);
		setVisible(true);
	}

	public void setPercentage(int widthPercentage) {
		setPercentage(widthPercentage + "%");
	}

	public void setPercentage(String widthPercentage) {
		setVisible(false);
		movingStatus.setWidth(widthPercentage);
		percentage.setText(widthPercentage + " complete");
		setVisible(true);
	}

	public void setTimedMessage(String message, int seconds) {
		this.message.setHTML(message);
		setVisible(true);
		Timer clearTimer = new Timer() {
			@Override
			public void run() {
				clearMessage();
			}
		};
		clearTimer.schedule(seconds * 1000);
	}

}
