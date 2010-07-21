package org.iucn.sis.server.api.utils;

public class OnlineUtil {
	
	//FIXME
	public static boolean amIOnline() {
		try {
			Class clazz = Class.forName("com.solertium.util.VerifyOnline");
			java.lang.reflect.Method online = clazz.getMethod("amIOnline", (Class[]) null);
			return ((Boolean) online.invoke((Object) null, (Object[]) null)).booleanValue();
		} catch (Exception e) {
			return "true".equals(System.getProperty("HOSTED_MODE"));
		}
	}

}
