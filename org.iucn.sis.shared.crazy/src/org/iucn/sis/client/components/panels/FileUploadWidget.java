package org.iucn.sis.client.components.panels;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.util.extjs.client.WindowUtils;

public class FileUploadWidget extends LayoutContainer {

	protected FileUpload uploader;
	protected FormPanel uploadForm;
	protected Button submitUpload;
	protected Button cancelUpload;
	protected Button completed;
	protected DockPanel uploadPanel;

	protected ClickListener extraAction;

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

		uploadForm.addFormHandler(new FormHandler() {

			public void onSubmit(FormSubmitEvent event) {
			//	submitUpload.setEnabled(false);
				submitUpload.setText("Uploading file...");
			}

			public void onSubmitComplete(FormSubmitCompleteEvent event) {
				onSuccess(event);

			}
		});

		HorizontalPanel buttonPanel = new HorizontalPanel();
		{
			submitUpload = new Button("Upload File");
			submitUpload.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					submitUpload.setEnabled(false);
					submit();
				}
			});
			cancelUpload = new Button("Cancel");
			cancelUpload.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
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

	protected void onSuccess(FormSubmitCompleteEvent event) {

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
