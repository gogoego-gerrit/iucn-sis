package org.iucn.sis.client.api.debug;

import org.iucn.sis.shared.api.debug.Debugger;

import com.extjs.gxt.ui.client.util.Format;

public class HostedModeDebugger implements Debugger {
	
	@Override
	public void println(Object obj) {
		System.out.println(obj);
	}
	
	@Override
	public void println(String template, Object... args) {
		/*
		 * Since we can't use String.format style templating, which is 
		 * what GetOut uses, we will instead translate this format to 
		 * be Ext's format operation and use that instead.  Can spruce 
		 * this up later.
		 */
		
		int count = 0, limit = 50;
		String text = template;
		
		while (text.indexOf("%s") != -1 && count < limit)
			text = text.replaceFirst("%s", "{" + (count++) + "}");
		
		System.out.println(Format.substitute(text, args));
	}

}
