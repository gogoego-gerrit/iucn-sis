package org.iucn.sis.shared;

public class LongUtils {
	
	public static long safeParseLong(String data) {
		try {
			return Long.parseLong(data);
		} catch (Throwable e) {
			e.printStackTrace();
			return -1;
		}
	}
}
