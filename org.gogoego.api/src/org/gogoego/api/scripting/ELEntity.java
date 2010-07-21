/*
 * Dedicated to the public domain by the author, Rob Heittman,
 * Solertium Corporation, December 2007
 * 
 * http://creativecommons.org/licenses/publicdomain/
 */

package org.gogoego.api.scripting;

/**
 * A trivial interface representing an expression language entity resolver.
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public interface ELEntity {
	String resolveEL(String key);
}
