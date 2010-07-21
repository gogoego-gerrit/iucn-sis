package com.solertium.gwt.gears.client.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.solertium.gwt.gears.client.dragdrop.processors.PackageProcessor;
import com.solertium.gwt.gears.client.dragdrop.targets.DropTargetFactory;
import com.solertium.gwt.gears.client.widgets.DropTargetWidget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Com_solertium_gwt_gears_client implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		DropTargetWidget widget = new DropTargetWidget();
		RootPanel.get().add(widget);
		try{
			DropTargetFactory.getInstance().createDropTarget(widget, new PackageProcessor(){
				public void processDropEvent(File[] files) {
					for(File f:files){
						Window.alert("Oops! You Dropped your file! " + f.getName() + "\nsize: "+String.valueOf(f.getBlob().getLength())+"Kb");
					}
				}
				public void updateEventOutput(JavaScriptObject event) {
					/* Only put something here if you want to do 
					   something during other drag and drop events*/
					
				}
			});
			
//			FILE UPLOAD TEST
//			DropTargetFactory.getInstance().createDropTarget(widget, new FileUploadPackageProcessor("/test"){
//				
//				public void updateEventOutput(JavaScriptObject event) {
//					/* Only put something here if you want to do 
//					   something during other drag and drop events*/
//					
//				}
//				
//				public void postCallback() {
//					Window.alert("test success");
//					
//				}
//			});
		}
		catch (GearsException e) {
			Window.alert("Gears is not installed, Silly.");
		}
	}
}
