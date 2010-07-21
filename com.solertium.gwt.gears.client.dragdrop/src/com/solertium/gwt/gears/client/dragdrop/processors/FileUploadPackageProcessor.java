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

package com.solertium.gwt.gears.client.dragdrop.processors;

import com.google.gwt.gears.client.desktop.File;

public abstract class FileUploadPackageProcessor implements PackageProcessor {

  private static String url;
	
  public native void setCallback() /*-{
    var obj = this; 
    $wnd.myFunc = function() {
      obj.@com.solertium.gwt.gears.client.dragdrop.processors.FileUploadPackageProcessor::postCallback()();
    }
  }-*/; 
	
  /**
  * 
  * @param postUrl
  */
  public FileUploadPackageProcessor(String postUrl) {
    url = postUrl;
	setCallback();
  }
	
  /**
  * @param files Array of Files to post to external server
  */
  public void processDropEvent(final File[] files) {
	  for(File file: files)
		  postFile(file, url);
  }

  /**
  * Callback to execute following a successful upload
  */
  public abstract void postCallback();
    
  /**
  * 
  * @param file File to upload
  * @param url Url to upload to
  */
  protected native void postFile(File file, String url)/*-{
    try{
      var boundary = '------multipartformboundary' + 'AaB03x';
      var dashDash = '--';
      var crlf = '\r\n';
      var dashDashBoundaryCrlf = dashDash + boundary + crlf;
   
      var builder = $wnd.google.gears.factory.create('beta.blobbuilder');
      builder.append(dashDashBoundaryCrlf);
	  builder.append('Content-Disposition: form-data; name="' + 'uploader' + '"');
	  if (file.name) {
	    builder.append('; filename="' + file.name + '"');
	  }
	           
	  builder.append(crlf);
	  builder.append('Content-Type: application/octet-stream');
	  builder.append(crlf);
	  builder.append(crlf);
	  builder.append(file.blob);
	  builder.append(crlf);
	  builder.append(dashDash);
	  builder.append(boundary);
	  builder.append(dashDash);
	  builder.append(crlf);
         
      var request = $wnd.google.gears.factory.create('beta.httprequest');
            
      request.open('POST', url);
      request.setRequestHeader('content-type', 'multipart/form-data;boundary=' + boundary);
      request.onreadystatechange = function() {
        if (request.readyState == 4) {
          if(request.status==200){
            $wnd.myFunc();
          }
        }
      };
      request.send(builder.getAsBlob());           
    }catch(err){
      alert(err);
    }
  }-*/;



}
