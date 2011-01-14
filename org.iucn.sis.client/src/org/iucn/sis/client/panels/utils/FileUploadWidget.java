package org.iucn.sis.client.panels.utils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.solertium.util.extjs.client.WindowUtils;

public class FileUploadWidget extends LayoutContainer {

	protected FileUpload uploader;
	protected FormPanel uploadForm;
	protected Button submitUpload;
	protected Button cancelUpload;
	protected Button completed;
	protected DockPanel uploadPanel;

	protected ClickHandler extraAction;

	public FileUploadWidget(String postUrl) {

		init();
		add(createPanel(postUrl));
	}

	public void addHiddenValue(String name, String value) {
		Hidden hiddenValue = new Hidden();
		hiddenValue.setName(name);
		hiddenValue.setValue(value);
		uploadPanel.add(hiddenValue, DockPanel.SOUTH);
	}

	protected Widget createPanel(String url) {

		// Setup uploadForm
		uploadForm = new FormPanel();
		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.setMethod(FormPanel.METHOD_POST);
		uploadForm.setAction(url);

		uploadPanel = new DockPanel();
		uploadPanel.setSpacing(5);

		uploader = new FileUpload();
		uploader.setTitle((url.hashCode() + Random.nextInt())+"");
		uploader.setName((url.hashCode() + Random.nextInt())+"");

		uploadPanel.add(uploader, DockPanel.CENTER);

		uploadForm.addSubmitHandler(new FormPanel.SubmitHandler() {
			public void onSubmit(SubmitEvent event) {
				submitUpload.setText("Uploading file...");
			}
		});
		uploadForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				onSuccess(event);
			}
		});

		HorizontalPanel buttonPanel = new HorizontalPanel();
		{
			submitUpload = new Button("Upload File");
			submitUpload.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					submitUpload.setEnabled(false);
					submit();
				}
			});
			cancelUpload = new Button("Cancel");
			cancelUpload.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					onClose();
				}
			});

			buttonPanel.add(submitUpload);
			buttonPanel.add(cancelUpload);
			buttonPanel.setSpacing(4);
		}

		uploadPanel.add(buttonPanel, DockPanel.SOUTH);
		uploadForm.setWidget(uploadPanel);
		uploadForm.addStyleName("RapidList-TableCell");

		return uploadForm;
	}

	protected void init() {

	}

	protected void onClose() {
		hide();
	}

	protected void onSuccess(SubmitCompleteEvent event) {

		WindowUtils.infoAlert("Upload Successful", "Upload Complete.");

		onClose();

		submitUpload.setText("Upload");
		submitUpload.setEnabled(true);
		cancelUpload.setEnabled(true);

	}
	

	public void submit() {
		if (validate()) {
			uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
			uploadForm.submit();
		}
		else{
			submitUpload.setEnabled(true);
		}

	}

	protected boolean validate() {
		return true;
	}
}
