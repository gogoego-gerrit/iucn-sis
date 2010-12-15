package org.iucn.sis.client.api.utils;

public class JiffyUtils {
	public native static void mark(String markerName) /*-{
		if( $wnd.Jiffy )
			$wnd.Jiffy.mark(markerName);
	}-*/;

	public native static void measure(String measurementName, String markerName) /*-{
		  if( $wnd.Jiffy )
			$wnd.Jiffy.measure(measurementName, markerName);
	}-*/;
}
