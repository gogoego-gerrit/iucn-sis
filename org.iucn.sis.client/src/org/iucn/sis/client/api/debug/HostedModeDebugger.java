package org.iucn.sis.client.api.debug;

import org.iucn.sis.shared.api.debug.Debugger;

import com.google.gwt.core.client.GWT;

public class HostedModeDebugger implements Debugger {
	
	public void println(Throwable e) {
		GWT.log(e.getMessage(), e);
		writeOutput(e == null ? "null" : serializeThrowable(e));
	}
	
	@Override
	public void println(Object obj) {
		writeOutput(obj == null ? "null" : obj instanceof Throwable ? serializeThrowable((Throwable)obj) : obj.toString());
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
		
		writeOutput(substitute(text, args));
	}
	
	private String substitute(String text, Object... params) {
		for (int i = 0; i < params.length; i++) {
			Object p = params[i];
			String toString;
			if (p == null)
				toString = "null";
			else if (p instanceof Throwable)
				toString = serializeThrowable((Throwable)p);
			else
				toString = p.toString();
			
			text = text.replaceAll("\\{" + i + "}", safeRegexReplacement(toString));
		}
		return text;
	}
	
	public static String serializeThrowable(Throwable e) {
		StringBuilder s = new StringBuilder();
		s.append(e + "\n");
        StackTraceElement[] trace = e.getStackTrace();
        for (int i=0; i < trace.length; i++)
            s.append("\tat " + trace[i] + "\n");

        Throwable ourCause = e.getCause();
        if (ourCause != null)
            printStackTraceAsCause(s, ourCause, trace);
        
        return s.toString();
	}
	
	private static void printStackTraceAsCause(StringBuilder s,
			Throwable caught, StackTraceElement[] causedTrace)
	{
		StackTraceElement[] trace = caught.getStackTrace();
		
		// Compute number of frames in common between this and caused
		int m = trace.length-1, n = causedTrace.length-1;
		while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
			m--; n--;
		}
		
		int framesInCommon = trace.length - 1 - m;
		s.append("Caused by: " + caught + "\n");
		for (int i=0; i <= m; i++)
			s.append("\tat " + trace[i]);
		if (framesInCommon != 0)
			s.append("\t... " + framesInCommon + " more\n");
	
		// Recurse if we have a cause
		Throwable ourCause = caught.getCause();
		if (ourCause != null)
			printStackTraceAsCause(s, ourCause, trace);
	}
	
	private String safeRegexReplacement(String replacement) {
		if (replacement == null)
			return replacement;

		return replacement.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
	}
	
	protected void writeOutput(String output) {
		System.out.println(output);
	}

}
