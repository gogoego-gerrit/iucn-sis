package org.iucn.sis.server.extensions.integrity;

import com.solertium.util.restlet.StandardServerComponent;

/**
 * IntegrityBootstrap.java
 * 
 * For testing purposes, to run the IntegrityApplication. Mounts it at
 * /integrity, just like SISBootstrap
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public class IntegrityBootstrap extends StandardServerComponent {

	public static void main(String[] args) {
		try {
			IntegrityBootstrap b = new IntegrityBootstrap();
			b.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public IntegrityBootstrap() {
		super(11001, 11002);
	}

	@Override
	protected void setupDefaultVirtualHost() {
		super.setupDefaultVirtualHost();
		getDefaultHost().attach(
				"/integrity",
				new IntegrityApplication(getContext().createChildContext(),
						"/var/sis/vfs"));
	}

}
