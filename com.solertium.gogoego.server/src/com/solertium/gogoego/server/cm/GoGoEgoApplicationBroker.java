/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.cm;

import org.gogoego.api.applications.GoGoEgoApplication;
import org.gogoego.api.applications.GoGoEgoApplicationFactory;
import org.gogoego.api.applications.GoGoEgoApplicationManagement;
import org.gogoego.api.applications.GoGoEgoApplicationMetaData;
import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.BundleContext;

import com.solertium.gogoego.server.lib.app.exporter.container.ExporterApplication;
import com.solertium.gogoego.server.lib.app.exporter.container.ExporterMetaData;
import com.solertium.gogoego.server.lib.app.importer.container.ImporterApplication;
import com.solertium.gogoego.server.lib.app.importer.container.ImporterMetaData;
import com.solertium.gogoego.server.lib.app.publishing.container.PublishingApplication;
import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.gogoego.server.lib.app.tags.container.TagMetaData;
import com.solertium.gogoego.server.lib.app.writefilter.container.FileWritingFilterApplication;

/**
 * GoGoEgoApplicationBroker.java
 * 
 * Broker load/unload of GoGoEgo Applications.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoEgoApplicationBroker extends PluginBroker<GoGoEgoApplicationFactory> {

	private final BundleContext bundleContext;

	public GoGoEgoApplicationBroker(BundleContext bundleContext) {
		super(bundleContext, GoGoEgoApplicationFactory.class.getName());
		this.bundleContext = bundleContext;

		addLocalReference(TagApplication.class.getName(), new GoGoEgoApplicationFactory() {
			public GoGoEgoApplication newInstance() {
				return new TagApplication();
			}
			public GoGoEgoApplicationManagement getManagement() {
				return new TagApplication();
			}
			public GoGoEgoApplicationMetaData getMetaData() {
				return new TagMetaData();
			}
		});
		addLocalReference(ExporterApplication.class.getName(), new GoGoEgoApplicationFactory() {
			public GoGoEgoApplication newInstance() {
				return new ExporterApplication();
			}
			public GoGoEgoApplicationManagement getManagement() {
				return new ExporterApplication();
			}
			public GoGoEgoApplicationMetaData getMetaData() {
				return new ExporterMetaData();
			}
		});
		addLocalReference(ImporterApplication.class.getName(), new GoGoEgoApplicationFactory() {
			public GoGoEgoApplication newInstance() {
				return new ImporterApplication();
			}
			public GoGoEgoApplicationManagement getManagement() {
				return new ImporterApplication();
			}
			public GoGoEgoApplicationMetaData getMetaData() {
				return new ImporterMetaData();
			}
		});
		addLocalReference(FileWritingFilterApplication.REGISTRATION, new GoGoEgoApplicationFactory() {
			public GoGoEgoApplication newInstance() {
				return new FileWritingFilterApplication();
			}
			public GoGoEgoApplicationMetaData getMetaData() {
				return new FileWritingFilterApplication();
			}
			
			public GoGoEgoApplicationManagement getManagement() {
				return null;
			}
		});
		addLocalReference(PublishingApplication.class.getName(), new GoGoEgoApplicationFactory() {
			public GoGoEgoApplicationManagement getManagement() {
				return new PublishingApplication();
			}
			public GoGoEgoApplicationMetaData getMetaData() {
				return new PublishingApplication();
			}
			public GoGoEgoApplication newInstance() {
				return new PublishingApplication();
			}
		});
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

}
