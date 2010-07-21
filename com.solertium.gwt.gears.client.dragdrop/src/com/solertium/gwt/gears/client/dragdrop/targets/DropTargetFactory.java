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

import com.google.gwt.gears.client.GearsException;
import com.google.gwt.user.client.ui.Widget;
import com.solertium.gwt.gears.client.dragdrop.processors.PackageProcessor;

/**
 * 
 * @author david.fritz
 *
 */

public class DropTargetFactory {

	
  private static DropTargetFactory impl = new DropTargetFactory();
  
  private DropTargetFactory() {
  }
  
  /**
   * 
   * @return An instance of the DropTargetFactory
   */
  public static DropTargetFactory getInstance(){
	  return impl;
  }
  
  /**
   * 
   * @param w Widget to be converted to drop target
   * @param p PackageProcessor that will handle the drop target events
   * @return A new DropTarget 
   * @throws GearsException Thrown if Google Gears is not installed 
   */
  public DropTarget createDropTarget(Widget w, PackageProcessor p) throws GearsException{
	  return new DropTarget(w.getElement().getAttribute("id"), p);
  }
  
}
