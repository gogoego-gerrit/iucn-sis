/*
 * Dedicated to the public domain by the author, Rob Heittman,
 * Solertium Corporation, 2008
 * 
 * http://creativecommons.org/licenses/publicdomain/
 */

package com.solertium.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Local scratch implementation of Mutex to avoid Sun dependency.
 * 
 * @author rob.heittman
 */
public class Mutex {

	final private AtomicBoolean bool = new AtomicBoolean();
	
	public boolean attempt(long wait) throws InterruptedException {
		if(bool.get() == true){
			Thread.sleep(wait);
		}
		synchronized(bool){
			if(bool.get() == true) return false; // no lock obtained during window
			bool.set(true); // lock obtained
			return true;
		}
	}
	
	public boolean attempt() {
		synchronized(bool){
			if(bool.get() == true) return false; // no lock obtained during window
			bool.set(true); // lock obtained
			return true;
		}
	}
	
	public void release(){
		bool.set(false);
	}
}
