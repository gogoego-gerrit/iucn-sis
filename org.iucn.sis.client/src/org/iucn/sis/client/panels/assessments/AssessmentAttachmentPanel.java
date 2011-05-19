package org.iucn.sis.client.panels.assessments;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.shared.api.models.FieldAttachment;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;

public class AssessmentAttachmentPanel extends LayoutContainer {

	protected final static String desc = "<b>Add another attachment</b><br/><br/>"
			+ "Choose a file that will be associated with this assessment, and "
			+ "whether to publish this file if and/or when the assessment is published.";
	protected final static String url = "/browse/assessments";
	protected final static String filenameSize = "200px";
	protected final static String publishSize = "200px";
	protected final static String deleteSize = "150px";

	protected FileUpload uploadField;
	protected HorizontalPanel radioPublish;
	protected FormPanel form;
	protected LayoutContainer table;
	protected LayoutContainer tableContents;

	protected final String assessmentID;
	protected Listener<BaseEvent> closeListener;
	protected List<FieldAttachment> attachments;
	protected FieldAttachment tempAssessmentAttach;
	protected RadioButton noPublish;
	protected RadioButton publish;

	public AssessmentAttachmentPanel(String assessmentID) {
		form = new FormPanel();
		uploadField = new FileUpload();
		this.assessmentID = assessmentID;
		this.attachments = new ArrayList<FieldAttachment>();
		table = new LayoutContainer();
		tableContents = new LayoutContainer();

		initForm();
		initTable();
	}

	public void draw(final AsyncCallback<String> callback) {

		final NativeDocument ndoc = SimpleSISClient
				.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getAttachmentBase() + url + "/" + assessmentID, new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(String result) {
				NativeElement element = ndoc.getDocumentElement();
				NativeNodeList attachments = element
						.getElementsByTagName("attachment");
				for (int i = 0; i < attachments.getLength(); i++)
					AssessmentAttachmentPanel.this.attachments
							.add(FieldAttachment.fromXML(attachments
									.elementAt(i)));

				try {
					refreshTable();
					LayoutContainer formHolder = new LayoutContainer();
					formHolder.addStyleName("attachmentForm");
					formHolder.add(form);

					addStyleName("attachmentWindow");
					setLayout(new BorderLayout());
					add(table, new BorderLayoutData(LayoutRegion.CENTER, 400,
							400, 400));
					add(formHolder, new BorderLayoutData(LayoutRegion.WEST,
							300, 300, 300));
					// add(form, new BorderLayoutData(LayoutRegion.WEST, 300));

					layout();
					callback.onSuccess(result);
				} catch (Throwable e) {
					e.printStackTrace();
				}

			}
		});

	}

	protected HorizontalPanel getRow(final FieldAttachment att) {

		HTML filenameHTML = new HTML("<a href=\"" + UriBase.getInstance().getAttachmentBase() + url + "/" + assessmentID + "/" + att.getId()
				+ "\" target=\"_blank\">" + att.getName() + "</a>");
		final ListBox isPublished = new ListBox(false);
		isPublished.addItem("yes", "true");
		isPublished.addItem("no", "false");
		int index = att.getPublish() ? 0 : 1;
		isPublished.setSelectedIndex(index);
		isPublished.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				att.setPublish(Boolean.parseBoolean(isPublished
						.getValue(isPublished.getSelectedIndex())));
				NativeDocument ndoc = SimpleSISClient
						.getHttpBasicNativeDocument();
				ndoc.put(UriBase.getInstance().getAttachmentBase() +url + "/" + assessmentID + "/" + att.getId(), att.toXML(),
						new GenericCallback<String>() {

							public void onFailure(Throwable caught) {
								if (isPublished.getSelectedIndex() == 0) {
									isPublished.setSelectedIndex(1);
								} else {
									isPublished.setSelectedIndex(0);
								}
								WindowUtils.errorAlert("Failed to save file attachment publish status");
							}

							public void onSuccess(String result) {
								Info.display("", "File attachment saved");

							}
						});
			}
		});

		Button deleteButton = new Button("Delete", new ClickHandler() {
			public void onClick(ClickEvent event) {
				WindowUtils.confirmAlert("Confirm", "Are you sure you want to delete this file? this can not be undone.", new WindowUtils.SimpleMessageBoxListener() {
					public void onYes() {
						NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
						ndoc.delete(UriBase.getInstance().getAttachmentBase() +url + "/"+assessmentID+"/" + att.getId(),
								new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Failed to delete file attachment");
							}
							public void onSuccess(String result) {
								Info.display("", "File attachment deleted");
								attachments.remove(att);
								refreshTable();
							}
						});	
					}
				});
			}
		});

		HorizontalPanel header = new HorizontalPanel();
		header.add(filenameHTML);
		header.add(isPublished);
		header.add(deleteButton);
		header.setCellWidth(filenameHTML, filenameSize);
		header.setCellWidth(isPublished, publishSize);
		header.setCellWidth(deleteButton, deleteSize);
		header.addStyleName("attachmentRow");
		return header;
	}

	protected void initForm() {
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		form.setAction(url + "/" + assessmentID);

		uploadField.setTitle("file");
		uploadField.setName("file");
		uploadField.setWidth("200px");

		publish = new RadioButton("publish", "true");
		publish.setText("yes");
		noPublish = new RadioButton("publish", "false");
		noPublish.setText("no");
		noPublish.setValue(true);
		HorizontalPanel radioPanel = new HorizontalPanel();
		radioPanel.add(new HTML("Publish file? "));
		radioPanel.setSpacing(5);
		radioPanel.add(noPublish);
		radioPanel.add(publish);

		Button submitButton = new Button("Submit", new ClickHandler() {
			public void onClick(ClickEvent event) {
				submitForm();
			}
		});

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				if (event.getResults() != null) {
					/*tempAssessmentAttach.setId(Integer.valueOf(event.getResults().replaceAll(
							"(<pre>)|(</pre>)", "")));*/
					attachments.add(tempAssessmentAttach);
					tempAssessmentAttach = null;
					refreshTable();
					WindowUtils.infoAlert("Success uploading file");
				} else {
					WindowUtils.errorAlert("Error uploading file");
				}
			}
		});

		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(10);
		panel.add(new Html(desc));
		panel.setSpacing(15);
		panel.add(uploadField);
		panel.setSpacing(10);
		panel.add(radioPanel);
		panel.add(submitButton);
		form.setWidget(panel);

	}

	protected void initTable() {
		RowLayout layout = new RowLayout();
		table.setLayout(layout);
		table.addStyleName("attachmentTable");
		table.setWidth(350);

		HTML filenameHeader = new HTML("Filename");
		HTML shouldPublish = new HTML("Should Publish?");
		HTML delete = new HTML("");
		HorizontalPanel header = new HorizontalPanel();
		header.add(filenameHeader);
		header.add(shouldPublish);
		header.add(delete);
		header.setCellWidth(filenameHeader, filenameSize);
		header.setCellWidth(shouldPublish, publishSize);
		header.setCellWidth(delete, deleteSize);
		header.addStyleName("attachmentTableHeader");

		tableContents.setScrollMode(Scroll.AUTO);
		table.add(header, new RowData(1, -1));
		table.add(tableContents, new RowData(1, 1));

	}

	protected void refreshTable() {

		tableContents.removeAll();
		for (FieldAttachment attach : attachments) {
			tableContents.add(getRow(attach));
		}
		tableContents.layout();

	}

	protected void submitForm() {

		if (uploadField.getFilename() != null
				&& !uploadField.getFilename().equalsIgnoreCase("")) {
			String filename = uploadField.getFilename();
			if (filename.contains("/")) {
				filename = filename.substring(filename.lastIndexOf("/") + 1);
			}

			if (filename.contains("_")) {
				WindowUtils.errorAlert("Filename can not contain \"_\"");
				return;
			}

			for (FieldAttachment att : attachments) {
				if (att.getName().equals(filename)) {
					WindowUtils
							.errorAlert("Unable to upload "
									+ filename
									+ ", as there is already a file named "
									+ filename
									+ " attached to this assessment.  Either rename this attachment, or remove the old attachment");
					return;
				}
			}

			tempAssessmentAttach = new FieldAttachment();
			tempAssessmentAttach.setName(uploadField.getFilename());
			tempAssessmentAttach.setPublish(publish.getValue());
			//tempAssessmentAttach.assessmentID = assessmentID;
			form.submit();
		}

		else
			WindowUtils.errorAlert("You must first select a file");
	}

}
