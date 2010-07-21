package org.iucn.sis.client.panels.workingsets;

import org.iucn.sis.client.api.caches.AssessmentCache;
import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
import org.iucn.sis.client.api.ui.models.workingset.WSStore;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.PanelManager;

import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;

public class WorkingsetImportWidget extends HorizontalPanel {

	protected FileUpload uploader;
	protected FormPanel uploadForm;
	protected Button submitUpload;
	protected Button cancelUpload;
	protected Button completed;
	protected DockPanel uploadPanel;
	private PanelManager manager;
	protected ClickListener extraAction;

	public WorkingsetImportWidget(PanelManager manager) {
		this.manager = manager;
		add(createPanel());
	}

	public Widget createPanel() {

		// Setup uploadForm
		uploadForm = new FormPanel();
		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.setMethod(FormPanel.METHOD_POST);
		uploadForm.setAction("/workingSetImporter/" + SimpleSISClient.currentUser.getUsername());

		uploadPanel = new DockPanel();
		uploadPanel.setSpacing(5);

		uploader = new FileUpload();
		uploader.setTitle("Hey");
		uploader.setName("HEy");
		uploadPanel.add(uploader, DockPanel.CENTER);

		uploadForm.addFormHandler(new FormHandler() {

			public void onSubmit(FormSubmitEvent event) {
				submitUpload.setEnabled(false);
				submitUpload.setText("Uploading file...");
			}

			public void onSubmitComplete(final FormSubmitCompleteEvent event) {
				WorkingSetCache.impl.update(new GenericCallback<String>() {

					public void onFailure(Throwable caught) {
						WindowUtils.infoAlert("Failed refresh", "This working set may have imported properly, but SIS "
								+ "failed to update its contents. Please log out and log "
								+ "back in and check for the working set. If this file "
								+ "continues to fail to import, please report the issue "
								+ "and include the file for testing.");

						submitUpload.setEnabled(true);
						cancelUpload.setEnabled(true);
						AssessmentCache.impl.clear();
						TaxonomyCache.impl.clear();
						manager.workingSetBrowser.refresh();
					}

					public void onSuccess(String arg0) {
						WSStore.getStore().update();
						submitUpload.setText("Import Working Set");
						submitUpload.setEnabled(true);
						cancelUpload.setEnabled(true);

						if( event.getResults() == null || event.getResults().equals("") || event.getResults().indexOf("<div>") > -1 ) {
							com.extjs.gxt.ui.client.widget.Window w = WindowUtils.getWindow(true, false, "Successful Import");
							w.add( new Html("The working set was successfully imported.<br/>" + event.getResults()));
							w.setSize(300, 500);
							w.show();
							w.center();
							w.setClosable(true);
AssessmentCache.impl.clear();
							TaxonomyCache.impl.clear();
							manager.workingSetBrowser.refresh();
						} else {
							com.extjs.gxt.ui.client.widget.Window w = WindowUtils.getWindow(true, false, "Import Failed!");
							w.add( new Html("Import failed. Please report the following error:" + event.getResults()) );
							w.setSize(300, 500);
							w.show();
							w.setClosable(true);
}

					}

				});

			}
		});

		HorizontalPanel buttonPanel = new HorizontalPanel();
		{
			submitUpload = new Button("Import Working Set");
			submitUpload.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					submit();
				}
			});
			cancelUpload = new Button("Cancel");
			cancelUpload.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					manager.workingSetBrowser.setManagerTab();
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

	public void onSubmitCompleteOverload(FormSubmitCompleteEvent event) {
		Window.alert("Successful upload ... refresh page still needed");
	}

	public void submit() {
		if (validate()) {
			uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
			uploadForm.submit();
		}

	}

	protected boolean validate() {
		if (uploader.getFilename().trim().equalsIgnoreCase("")) {
			WindowUtils.errorAlert("Please select a .zip file to import.");
			return false;
		} else if (!uploader.getFilename().endsWith(".zip")) {
			WindowUtils.errorAlert("You must choose a .zip file that contains a working set.");
			return false;
		}
		return true;
	}

}
