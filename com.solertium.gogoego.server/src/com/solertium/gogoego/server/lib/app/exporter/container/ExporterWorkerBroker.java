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
package com.solertium.gogoego.server.lib.app.exporter.container;


import org.gogoego.api.utils.PluginBroker;

import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorker;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerManagement;
import com.solertium.gogoego.server.lib.app.exporter.workers.ExporterWorkerMetadata;
import com.solertium.gogoego.server.lib.app.exporter.workers.appengine.AppEngineExporterManager;
import com.solertium.gogoego.server.lib.app.exporter.workers.appengine.AppEngineWorker;
import com.solertium.gogoego.server.lib.app.exporter.workers.flat.StaticSiteWorker;
import com.solertium.gogoego.server.lib.app.exporter.workers.flat.StaticSiteWorkerManager;
import com.solertium.gogoego.server.lib.app.exporter.workers.gge.GGEWorker;
import com.solertium.gogoego.server.lib.app.exporter.workers.gge.GGEWorkerManager;
import com.solertium.vfs.VFS;

/**
 * ExporterWorkerBroker.java
 * 
 * Brokers for the ExporterWorkers
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class ExporterWorkerBroker extends PluginBroker<ExporterWorkerFactory> {
	
	public ExporterWorkerBroker() {
		super(
			PluginAgent.getGoGoEgoApplicationBroker().getBundleContext(), 
			ExporterWorkerFactory.class.getName()
		);
		
		addLocalReference("com.solertium.gogoego.server.lib.app.exporter.workers.flat", new ExporterWorkerFactory() {
			public ExporterWorkerManagement getManagement() {
				return new StaticSiteWorkerManager();
			}
			public ExporterWorkerMetadata getMetadata() {
				return new StaticSiteWorkerManager();
			}
			public ExporterWorker newInstance(VFS vfs) {
				return new StaticSiteWorker(vfs);
			}
		});
		addLocalReference("com.solertium.gogoego.server.lib.app.exporter.workers.gge", new ExporterWorkerFactory() {
			public ExporterWorkerManagement getManagement() {
				return new GGEWorkerManager();
			}
			public ExporterWorkerMetadata getMetadata() {
				return new GGEWorkerManager();
			}
			public ExporterWorker newInstance(VFS vfs) {
				return new GGEWorker(vfs);
			}
		});
		addLocalReference("com.solertium.gogoego.server.lib.app.exporter.workers.appengine", new ExporterWorkerFactory() {
			public ExporterWorkerManagement getManagement() {
				return new AppEngineExporterManager();
			}
			public ExporterWorkerMetadata getMetadata() {
				return new AppEngineExporterManager();
			}
			public ExporterWorker newInstance(VFS vfs) {
				return new AppEngineWorker(vfs);
			}
		});
	}

}
