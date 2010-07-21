/*
 * Dedicated to the public domain by the author, Rob Heittman,
 * Solertium Corporation, December 2007
 * 
 * http://creativecommons.org/licenses/publicdomain/
 */

package com.solertium.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generally, checked exceptions should not be ignored; they should be handled
 * or thrown out of the method where they occur. When proper exception handling
 * has not yet been constructed, this class offers something useful to insert,
 * and implies the kind of handling that should be refactored into the code in
 * the future.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class TrivialExceptionHandler {

	public final static boolean ALLOW_IGNORE = true;

	/**
	 * This method trivially logs the exception at the FINE log level. In most
	 * cases the calling code should re-throw the exception instead.
	 */
	public static void handle(final Object caller, Throwable exception) {
		final Logger logger = Logger.getLogger(caller.getClass().getName());
		logger.log(Level.FINE, "Weakly handled exception", exception);
		while (exception.getCause() != null) {
			exception = exception.getCause();
			logger.log(Level.FINE, "Weakly handled exception", exception);
		}
	}

	/**
	 * This method may be used to bypass checked exceptions that are believed to
	 * be harmless. The method returns immediately in normal usage. By
	 * redefining the value of the static ALLOW_IGNORE, it is possible to have
	 * these exceptions logged. In most cases the calling code could and should
	 * be redesigned to handle or preclude the exception.
	 */
	public static void ignore(final Object caller, Throwable exception) {
		if (ALLOW_IGNORE)
			return;
		final Logger logger = Logger.getLogger(caller.getClass().getName());
		logger.log(Level.FINEST, "Normally ignored exception", exception);
		while (exception.getCause() != null) {
			exception = exception.getCause();
			logger.log(Level.FINEST, "Normally ignored exception", exception);
		}
	}

	/**
	 * This method may be used when the exception handling block is thought to
	 * be normally unreachable.  A better choice, if feasible, would be to throw
	 * a RuntimeException.
	 */
	public static void impossible(final Object caller, Throwable exception) {
		final Logger logger = Logger.getLogger(caller.getClass().getName());
		logger.log(Level.WARNING, "Unexpected exception", exception);
		while (exception.getCause() != null) {
			exception = exception.getCause();
			logger.log(Level.WARNING, "Unexpected exception", exception);
		}
	}
}
