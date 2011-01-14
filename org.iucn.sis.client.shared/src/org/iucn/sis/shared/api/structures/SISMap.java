package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings({"deprecation", "unchecked", "unused"})
public class SISMap extends Structure<Field> {

	public static final String MAP = "map";
	// public static GMarkerOptions MAP_OPTIONS;

	private ArrayList myMapData;
	private SISMapData current;

	// private GMap2Widget mapWidget;
	private SimplePanel myMapPanel;
	private VerticalPanel points;

	private ArrayList textLats;
	private ArrayList textLngs;

	private VerticalPanel fullNewPointPanel;
	private HorizontalPanel latLong;
	private HorizontalPanel id;
	private HorizontalPanel desc;

	public SISMap(String struct, String descript, String structID, Object data) {
		// descript should be blank
		// data = list of SISMapData
		super(struct, "", structID, data);
		// displayPanel = new ContentPanel();
		buildContentPanel(Orientation.HORIZONTAL);
		// GMarkerOptions options = new GMarkerOptions();
		// options.setBouncy(true);
		// options.setDraggable(true);
		// MAP_OPTIONS = options;
	}

	@Override
	public boolean hasChanged(Field field) {
		// TODO Auto-generated method stub
	
		return false;
	}
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		return new ArrayList<ClassificationInfo>();
	}
	
	@Override
	public void clearData() {
		// TODO Auto-generated method stub

	}

	@Override
	public Widget createLabel() {
		// Map Points (markers)
		createMapPoints();

		// Map Functionality
		// this.mapWidget = new GMap2Widget("400", "400");
		// this.mapWidget.getGmap().enableAutomatedCheckResizeOnMoveEnd();
		// this.mapWidget.getGmap().enableContinuousZoom();
		// this.mapWidget.getGmap().enableDoubleClickZoom();
		// this.mapWidget.getGmap().enableInfoWindow();
		// this.mapWidget.getGmap().setZoom( 2 );

		// myMapPanel.setWidget(mapWidget);

		// Add the panels
		clearDisplayPanel();
		displayPanel.add(myMapPanel);
		displayPanel.add(points);
		displayPanel.add(createNewPointInterface());

		return displayPanel;
	}

	private void createMapPoints() {
		/*
		 * try { GMarkerEventManager eventManager =
		 * GMarkerEventManager.getInstance();
		 * mapWidget.getGmap().clearOverlays();
		 * 
		 * for (int i = 0; i < myMapData.size(); i++) { current =
		 * (SISMapData)myMapData.get(i);
		 * eventManager.addOnClickHandler(current, new
		 * GMarkerEventClickHandler() { public void onClick(ClickEvent event) {
		 * marker.openInfoWindowTabs(openTabs()); } public void
		 * onDblClick(GMarker marker) {} });
		 * 
		 * eventManager.addOnDragEndListener(current, new
		 * GMarkerEventDragListener() { public void onDragEnd(GMarker marker) {
		 * ((SISMapData)marker).setLatitude(marker.getPoint().lat());
		 * ((SISMapData)marker).setLongitude(marker.getPoint().lng());
		 * createTextInputArea(); } public void onDragStart(GMarker marker) {}
		 * }); //current.enableDragging();
		 * mapWidget.getGmap().addOverlay(current); } createTextInputArea(); }
		 * catch (Exception e) { myMapData = new ArrayList(); }
		 */
	}

	private Widget createNewPointInterface() {
		if (fullNewPointPanel != null)
			return fullNewPointPanel;

		fullNewPointPanel = new VerticalPanel();
		VerticalPanel inputInterface = new VerticalPanel();
		latLong = new HorizontalPanel();
		id = new HorizontalPanel();
		fullNewPointPanel.add(new HTML("Add Point"));

		id.add(new HTML("Point Name: "));
		id.add(new TextBox());

		latLong.add(new HTML("Latitude: "));
		latLong.add(new TextBox());
		latLong.add(new HTML("Longitude: "));
		latLong.add(new TextBox());

		desc = new HorizontalPanel();
		desc.add(new HTML("Description: "));
		desc.add(new TextBox());

		Button createPoint = new Button("Create");
		createPoint.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				doPointCreate();
			}
		});

		inputInterface.add(id);
		inputInterface.add(latLong);
		inputInterface.add(desc);

		fullNewPointPanel.add(inputInterface);
		fullNewPointPanel.add(createPoint);

		return fullNewPointPanel;
	}

	private void createTextInputArea() {
		createTextInputArea("Edit Map Points", true);
	}

	private void createTextInputArea(String title, boolean canSave) {
		// TextBoxes to edit map points
		points.clear();

		HTML editBanner = new HTML("<center>" + title + "</center>");
		editBanner.addStyleName("SIS_Map_EditBanner");
		points.add(editBanner);

		int i;

		for (i = 0; i < myMapData.size(); i++) {
			textLats.add(new TextBox());
			textLngs.add(new TextBox());

			SISMapData currentData = (SISMapData) myMapData.get(i);
			HorizontalPanel header = new HorizontalPanel();
			HorizontalPanel textEntry = new HorizontalPanel();

			header.add(new HTML("<b><center>" + currentData.getId() + "</center></b>"));
			HTML removeMe = ((SISMapData) myMapData.get(i)).getRemoveHTML();
			removeMe.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					removeMapData();
				}
			});
			header.add(removeMe);

			textEntry.add(new HTML("Latitude: "));
			((TextBox) textLats.get(i)).setText("" + currentData.getLatitude());
			((TextBox) textLats.get(i)).addKeyboardListener(new KeyboardListenerAdapter() {
				@Override
				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
					doPointSave();
				}
			});
			textEntry.add(((TextBox) textLats.get(i)));

			textEntry.add(new HTML("Longitude: "));
			((TextBox) textLngs.get(i)).setText("" + currentData.getLongitude());
			((TextBox) textLngs.get(i)).addKeyboardListener(new KeyboardListenerAdapter() {
				@Override
				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
					doPointSave();
				}
			});
			textEntry.add(((TextBox) textLngs.get(i)));

			points.add(header);
			points.add(textEntry);
		}

		// Save button
		if (canSave) {
			Button save = new Button("Save");
			save.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					doPointSave();
				}
			});
			save.addStyleName("SIS_longButton");
			points.add(save);
		}
	}

	// Private helper functions

	@Override
	public Widget createViewOnlyLabel() {
		createTextInputArea("View Map Points", false);
		clearDisplayPanel();
		displayPanel.add(points);
		return displayPanel;
	}

	@Override
	public void createWidget() {
		textLats = new ArrayList();
		textLngs = new ArrayList();

		myMapPanel = new SimplePanel();
		myMapPanel.setHeight("400");
		myMapPanel.setWidth("400");

		points = new VerticalPanel();
		points.setWidth("400px");
		points.setHeight("100%");
	}

	private void doPointCreate() {
		double lat = Double.parseDouble(((TextBox) latLong.getWidget(1)).getText());
		double lng = Double.parseDouble(((TextBox) latLong.getWidget(3)).getText());
		String id = ((TextBox) this.id.getWidget(1)).getText();
		String desc = ((TextBox) this.desc.getWidget(1)).getText();

		// SysDebugger.getInstance().println("Lat: " + lat + "; Lng: " + lng +
		// "; ID: " + id + "; Desc: " + desc);

		SISMapData newPoint = new SISMapData(id, lat, lng, "", desc);
		myMapData.add(newPoint);
		// displayPanel.clear();
		// createLabel();
		createMapPoints();
		// createTextInputArea();
	}

	private void doPointSave() {
		for (int i = 0; i < myMapData.size(); i++) {
			try {
				((SISMapData) myMapData.get(i)).updatePoint(Double.parseDouble(((TextBox) textLats.get(i)).getText()),
						Double.parseDouble(((TextBox) textLngs.get(i)).getText()));
			} catch (Exception e) {
			}
			// ((SISMapData)myMapData.get(i)).show();
		}
	}

	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
		ret.add(description);
		return ret;
	}

	@Override
	public String getData() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Pass in the raw data from an Assessment object, and this will return
	 * it in happy, displayable String form
	 * 
	 * @return ArrayList of Strings, having converted the rawData to nicely
	 *         displayable String data. Happy days!
	 */
	@Override
	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
		prettyData.add(offset, rawData.get(offset));
		return ++offset;
	}

	/*
	 * private GInfoWindowTab[] openTabs() { if (current != null) {
	 * GInfoWindowTab[] tabs = { new GInfoWindowTab(current.getId(),
	 * current.getDescription()) }; return tabs; } else { return null; } }
	 */

	public ArrayList getMyMapData() {
		return myMapData;
	}

	private void removeMapData() {
		for (int i = 0; i < myMapData.size(); i++) {
			if (((SISMapData) myMapData.get(i)).isSetToDelete()) {
				myMapData.remove(i);
			}
		}

		createMapPoints();
	}
	
	@Override
	public void setData(Field field) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void save(Field parent, Field field) {
		// TODO Auto-generated method stub
		
	}

	
	// Structure functions
	@Override
	public void setEnabled(boolean isEnabled) {
		this.displayPanel.setVisible(isEnabled);
	}

}
