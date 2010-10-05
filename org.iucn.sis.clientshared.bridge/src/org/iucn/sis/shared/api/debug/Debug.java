package org.iucn.sis.shared.api.debug;

public class Debug {
	
	public static boolean isDefaultInstance() {
		return instance.debugger instanceof SystemDebugger;
	}
	
	public static void setInstance(Debugger debugger) {
		instance = new Debug(debugger); 
	}
	
	/*
	 * The default debugger does not support the entire 
	 * API, but it will not cause a NullPointer exception 
	 * if you use this without setting the instance. 
	 * 
	 */
	private static Debug instance = new Debug(new SystemDebugger());
	
	public static void println(String string) {
		instance.debugger.println(string);
	}
	
	public static void println(Object obj) {
		instance.debugger.println(obj);
	}
	
	public static void println(String template, Object... objects) {
		instance.debugger.println(template, objects);
	}
	
	public static void println(Throwable e) {
		instance.debugger.println(e);
	}
	
	private final Debugger debugger;
	
	private Debug(Debugger debugger) {
		this.debugger = debugger;
	}
	
	private static class SystemDebugger implements Debugger {
		
		public void println(Throwable e) {
			System.err.println("Using default debugger, please supply a Debugger instance.");
			e.printStackTrace();
		}
		
		@Override
		public void println(Object obj) {
			System.err.println("Using default debugger, please supply a Debugger instance.");
			System.out.println(obj);
		}
		
		@Override
		public void println(String template, Object... args) {
			System.err.println("Using default debugger, please supply a Debugger instance.");
			System.out.println(template);
		}
		
	}

}
