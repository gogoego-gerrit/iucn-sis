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
package org.gogoego.api.utils;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.restlet.routing.Filter;

/**
 * Put this in front of something to disable magic post-processing by GoGoEgo
 */
public class MagicDisablingFilter extends Filter {

	public final static String MAGIC_DISABLING_KEY = "com.solertium.gogoego.server.disableMagic";

	public MagicDisablingFilter(final Context context) {
		super(context);
	}

	public MagicDisablingFilter(final Context context, final Restlet next) {
		super(context);
		setNext(next);
	}

	public MagicDisablingFilter(final Context context, final Class<? extends Resource> next) {
		super(context);
		setNext(next);
	}

	@Override
	public int beforeHandle(final Request request, final Response response) {
		request.getAttributes().put(MAGIC_DISABLING_KEY, Boolean.TRUE);
		return Filter.CONTINUE;
	}

}
