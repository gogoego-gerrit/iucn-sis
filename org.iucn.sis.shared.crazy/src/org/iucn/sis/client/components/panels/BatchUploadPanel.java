//package org.iucn.sis.client.components.panels;
//
//import com.extjs.gxt.ui.client.event.ComponentEvent;
//import com.extjs.gxt.ui.client.event.SelectionListener;
//import com.extjs.gxt.ui.client.widget.LayoutContainer;
//import com.extjs.gxt.ui.client.widget.Window;
//import com.extjs.gxt.ui.client.widget.button.Button;
//import com.extjs.gxt.ui.client.widget.layout.FillLayout;
//import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
//import com.google.gwt.user.client.ui.HTML;
//import com.solertium.lwxml.shared.NativeDocument;
//import com.solertium.util.extjs.client.WindowUtils;
//
//public class BatchUploadPanel extends LayoutContainer {
//
//	public BatchUploadPanel() {
//		super();
//		setLayout(new FillLayout());
//	}
//	
//	public void draw() {
//		removeAll();
////		FileUploadWidget fileupload = new FileUploadWidget("/images/batch"){
////			@Override
////			protected boolean validate() {
////				if(uploader.getFilename().endsWith(".zip")) return true;
////				return false;
////			}
////			@Override
////			protected void onSuccess(FormSubmitCompleteEvent event) {
////				final Window w= WindowUtils.getWindow(true, true, "Upload Complete");
////				w.add(new HTML(event.getResults()));
////				w.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
////					public void componentSelected(ButtonEvent ce) {
////						w.close();
////					};
////				}));
////				w.show();
////
////				draw();
////
////				submitUpload.setText("Upload");
////				submitUpload.setEnabled(true);
////				cancelUpload.setEnabled(true);
////			}
////		};
////		add(fileupload);
//		
//		layout();
//	}
//}