/**
 * 
 */
package com.solertium.lwxml.shared;

/**
 * NativeDocumentCallback.java
 *
 * @author carl.scott
 *
 */
public interface GenericCallback<T> {
	
	public void onSuccess(T result);
	
	public void onFailure(Throwable caught);

}
