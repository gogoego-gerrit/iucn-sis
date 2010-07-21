package com.solertium.gogoego.server.cm;

import org.gogoego.api.errors.ErrorHandler;
import org.gogoego.api.errors.ErrorHandlerFactory;
import org.gogoego.api.utils.PluginBroker;
import org.osgi.framework.BundleContext;

import com.solertium.gogoego.server.lib.settings.shortcuts.ShortcutErrorHandler;

public class ErrorHandlerBroker extends PluginBroker<ErrorHandlerFactory> {
	
	public ErrorHandlerBroker(BundleContext bundleContext) {
		super(bundleContext, ErrorHandlerFactory.class.getName());
		
		addLocalReference(ShortcutErrorHandler.BUNDLE_KEY, new ErrorHandlerFactory() {
			public ErrorHandler newInstance() {
				return new ShortcutErrorHandler();
			}
		});
	}

}
