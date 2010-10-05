package org.iucn.sis.shared.api.structures;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

public class SISMapData {

	private String id;
	private double latitude;
	private double longitude;
	private String iconUrl;
	private String description;
	// public GMarkerOptions options;

	private boolean setToDelete = false;

	public SISMapData() {
		this("", 0, 0, "", "");
	}

	public SISMapData(double latitude, double longitude, String icon) {
		this("", latitude, longitude, icon, "");
	}

	public SISMapData(String id, double latitude, double longitude, String icon, String description) {
		// this(id, latitude, longitude, icon, description, SISMap.MAP_OPTIONS);
	}

	/*
	 * public SISMapData(String id, double latitude, double longitude, String
	 * icon, String description, GMarkerOptions opts) { super(new
	 * GLatLng(latitude, longitude), opts); this.id = id; this.latitude =
	 * latitude; this.longitude = longitude; this.iconUrl = icon;
	 * this.description = description; this.options = opts;
	 * options.setTitle(id); try { if (!iconUrl.equalsIgnoreCase(""))
	 * this.setImage(iconUrl); } catch (Exception e) {} }
	 */

	/*
	 * public SISMapData(SISMapData data) { this(data.getId(),
	 * data.getLatitude(), data.getLongitude(), data.getIconUrl(),
	 * data.getDescription(), data.getOptions()); super(new
	 * GLatLng(data.getLatitude(), data.getLongitude()), data.getOptions());
	 * this.id = data.getId(); this.latitude = data.getLatitude();
	 * this.longitude = data.getLongitude(); this.iconUrl = data.getIconUrl();
	 * this.description = data.getDescription(); this.options =
	 * data.getOptions(); options.setTitle(id); if
	 * (!this.iconUrl.equalsIgnoreCase("")) { this.setImage(iconUrl); } }
	 */

	/*
	 * public GMarkerOptions getOptions() { return options; }
	 */

	public boolean equals(SISMapData otherData) {
		return (this.latitude == otherData.latitude && this.longitude == otherData.longitude);
	}

	public String getDescription() {
		return description;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getId() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public HTML getRemoveHTML() {
		HTML removeMe = new HTML("  (Remove)");
		removeMe.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setToDelete = true;
			}
		});
		return removeMe;
	}

	public boolean isSetToDelete() {
		return setToDelete;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setIconUrl(String icon) {
		this.iconUrl = icon;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "(" + this.latitude + ", " + this.longitude + ")";
	}

	public void updatePoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		// this.setPoint(new GLatLng(this.latitude, this.longitude));
	}

}
