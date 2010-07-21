/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */
package com.solertium.util.restlet.usermodel.test;

import org.restlet.Context;

import com.solertium.db.DBException;
import com.solertium.util.restlet.StandardServerComponent;

/**
 * ExampleBootstrap.java
 * 
 * Bootstrap for the example application
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ExampleBootstrap extends StandardServerComponent {
	
	public static void main(String[] args) {
		try {
			final ExampleBootstrap component = new ExampleBootstrap();
			component.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
				 		
	}

	public ExampleBootstrap() throws DBException {
		super(11001, 11002);
	}
	
	@Override
	protected void setupDefaultVirtualHost() {
		final Context childContext = getContext().createChildContext();
		getDefaultHost().attach(new TestApplication(childContext));

		
	}

	
}