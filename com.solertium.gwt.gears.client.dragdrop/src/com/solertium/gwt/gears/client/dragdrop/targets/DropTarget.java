/*
 * Copyright 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package com.solertium.gwt.gears.client.dragdrop.targets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.gears.client.desktop.File;
import com.solertium.gwt.gears.client.dragdrop.processors.PackageProcessor;

/**
 * 
 * @author david.fritz
 *
 */

public class DropTarget{

  private final PackageProcessor processor;
  private final String id;
  
  /**
   * 
   * @param id id of the DOM element to be converted to drop target
   * @param processor PackageProcessor to handle drop events
   * @throws GearsException Thrown if Google Gears is not installed
   */
  public DropTarget(String id, PackageProcessor processor) throws GearsException{
	 if(isInstalled()){
		this.processor=processor;
		this.id = id;
	    init();
	 }
	 else{
		 throw new GearsException("Gears Not Installed");
	 }
  }
	
  private void init(){
    _init();
  }
	
  private native void _init() /*-{ 
  	var obj = this; 
    
     var dropZone = $wnd.document.getElementById(this.@com.solertium.gwt.gears.client.dragdrop.targets.DropTarget::id);
	dropZone.addEventListener('dragenter', 
	    this.@com.solertium.gwt.gears.client.dragdrop.targets.DropTarget::handleDragEnter(Lcom/google/gwt/core/client/JavaScriptObject;), false);
	dropZone.addEventListener('dragover',  
	    this.@com.solertium.gwt.gears.client.dragdrop.targets.DropTarget::handleDragOver(Lcom/google/gwt/core/client/JavaScriptObject;), false);
	dropZone.addEventListener('dragexit',  
	    this.@com.solertium.gwt.gears.client.dragdrop.targets.DropTarget::handleDragLeave(Lcom/google/gwt/core/client/JavaScriptObject;), false);
	dropZone.addEventListener('dragdrop',  
	    this.@com.solertium.gwt.gears.client.dragdrop.targets.DropTarget::handleDrop(Lcom/google/gwt/core/client/JavaScriptObject;), false);
	    
	$wnd.updateEventOutput = function(event) {
      obj.@com.solertium.gwt.gears.client.dragdrop.targets.DropTarget::updateEventOutput(Lcom/google/gwt/core/client/JavaScriptObject;)(event);
    }
  }-*/;
	
  private native void _finishDrag(JavaScriptObject event, boolean isDrop) /*-{
	var isFirefox = $wnd.google.gears.factory.getBuildInfo().indexOf(';firefox') > -1;
    if (isFirefox) {
      if (isDrop) {
        event.stopPropagation();
      }
    } else {
      if (!isDrop) {
        event.returnValue = false;
      }
    }
  }-*/;
	
  private native void _handleDragEnter(JavaScriptObject event) /*-{
    $wnd.updateEventOutput(event);
  }-*/;

  private native void _handleDragOver(JavaScriptObject event) /*-{
    $wnd.updateEventOutput(event);
  }-*/;

  private native void _handleDragLeave(JavaScriptObject event) /*-{
    $wnd.updateEventOutput(event);
  }-*/;

  private native File[]  _handleDrop(JavaScriptObject event) /*-{
    var desktop = $wnd.google.gears.factory.create('beta.desktop');
	var data = desktop.getDragData(event, 'application/x-gears-files');
	var files = data && data.files;
	return files;
  }-*/;

  /**
   * 
   * @return boolean specifying the state of Google gears installation
   */
  public static native boolean isInstalled() /*-{
    var available = $wnd.google && $wnd.google.gears;
    return available != null;
  }-*/;

  
  /**
   * 
   * @param event JavaScript event that is fired when a file is dropped in the drop target
   */
  public void handleDrop(JavaScriptObject event){
    processor.processDropEvent(_handleDrop(event));
	_finishDrag(event, true);
  }

  /**
   * 
   * @param event JavaScript event that is fired when a file is dragged into the drop target
   */
  public void handleDragEnter(JavaScriptObject event){
    _handleDragEnter(event);
    _finishDrag(event, false);
  }
	
  /**
   * 
   * @param event JavaScript event that is fired when a file is dragged over the drop target
   */
  public void handleDragOver(JavaScriptObject event){
    _handleDragOver(event);
    _finishDrag(event, false);
  }
  
  /**
   * 
   * @param event JavaScript event that is fired when a file is dragged out of the drop target
   */
  public void handleDragLeave(JavaScriptObject event){
    _handleDragLeave(event);
    _finishDrag(event, false);
  }

  /**
   * This function will call the updateEventOutput function that is implemented in the 
   * PackageProcessor. This function will be called on any drag and drop event that takes
   * place in this drop target. This will allow for the developer to update UI displays 
   * or handle external calls during the drag drop process.
   * 
   * @param event JavaScript event that is fired during any drag and drop event 
   */
  public void updateEventOutput(JavaScriptObject event){
	  processor.updateEventOutput(event);
  }
  
}
