package org.iucn.sis.client.panels.images;

import java.util.ArrayList;

import org.iucn.sis.client.api.ui.models.image.ManagedImage;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.container.SimpleSISClient;
import org.iucn.sis.client.panels.ClientUIContainer;
import org.iucn.sis.client.panels.utils.FileUploadWidget;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonImage;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

public class ImageManagerPanel extends LayoutContainer {

	private Taxon taxon;

	protected ToolBar toolbar;
	protected ToolBar viewbar;
	protected int viewType;
	protected int selected;
	protected ArrayList<HorizontalPanel> selectedList;
	protected ArrayList<TaxonImage> imageList;

	final protected String RATING_FILLED = "images/star.png";
	final protected String RATING_OUTLINE = "images/star_outline.png";;

	protected LayoutContainer imagesContainer;

	protected TableLayout detailLayout;
	protected TableLayout galleryLayout;

	public static final int GALLERY_VIEW = 0;
	public static final int DETAIL_VIEW = 1;
	
	private boolean allowRating;

	public ImageManagerPanel(Taxon taxon) {
		super();
		this.taxon = taxon;
		this.allowRating = false;
		
		toolbar = new ToolBar();
		viewbar = new ToolBar();
		selectedList = new ArrayList<HorizontalPanel>();
		imageList = new ArrayList<TaxonImage>();

		imagesContainer = new LayoutContainer();
		imagesContainer.setBorders(false);
		imagesContainer.setStyleName("gwt-background");
		imagesContainer.setHeight(290);
		imagesContainer.setWidth(595);
		imagesContainer.setScrollMode(Scroll.AUTO);

		detailLayout = new TableLayout(2);
		detailLayout.setWidth("590px");
		detailLayout.setHeight("290px");
		galleryLayout = new TableLayout(2);
		galleryLayout.setWidth("590px");
		galleryLayout.setHeight("290px");

		setStyleName("gwt-background");
		setWidth(600);
		setHeight(300);
		setBorders(true);
		setLayout(new RowLayout(Orientation.VERTICAL));

		init();
	}
	
	public void setAllowRating(boolean allowRating) {
		this.allowRating = allowRating;
	}

	private void addImage(final TaxonImage image) {
		imageList.add(image);

		Image representation = new Image();
		
		if (viewType == DETAIL_VIEW) {
			// imageWrapper.setWidth("450");
			representation.setUrl(UriBase.getInstance().getImageBase() + 
					"/images/view/thumb/" + taxon.getId() + "/" + image.getFileName() + "?size=100");
		} else {
			// imageWrapper.setWidth("50");
			representation.setUrl(UriBase.getInstance().getImageBase() + 
					"/images/view/thumb/" + taxon.getId() + "/" + image.getFileName() + "?size=50");
		}

		representation.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				selected = imageList.indexOf(image);
				selectImage(selected);
			}
		});

		HorizontalPanel imageWrapper = new HorizontalPanel();
		imageWrapper.setBorderWidth(0);
		imageWrapper.add(representation);
		if (viewType == DETAIL_VIEW) {
			imageWrapper.add(buildDetailView(image));
		}

		if (viewType == DETAIL_VIEW) {
			imagesContainer.add(imageWrapper);
		} else {
			imagesContainer.add(imageWrapper);
		}

		selectedList.add(imageWrapper);
	}

	protected VerticalPanel buildDetailView(final TaxonImage image) {
		VerticalPanel details = new VerticalPanel();
		details.add(new HTML("<b>Description: </b>" + image.getCaption()));
		details.add(new HTML("<b>Credit: </b>" + image.getCredit()));
		details.add(new HTML("<b>Source: </b>" + image.getSource()));
		
		if (allowRating) {
			HorizontalPanel rating = new HorizontalPanel();
			double ratingNum = image.getRating(0.0F);
			for (int i = 0; i < 5; i++) {
				if (i < ratingNum) {
					Image star = new Image(RATING_FILLED);
					star.setSize("25", "25");
					rating.add(star);
				} else {
					Image star = new Image(RATING_OUTLINE);
					star.setSize("25", "25");
					rating.add(star);
				}
			}
			
			rating.add(new Button("Rate It!", new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					displayRateItPopup(image);
				}
			}));
			
			HTML values = new HTML("Votes: " + image.getWeight() + " Value: " + image.getRating(0.0F));
			values.setStyleName("SIS_hasSmallerHTML");
			
			details.add(rating);
			details.add(values);
		}
		
		return details;
	}

	protected void displayRateItPopup(final TaxonImage image) {
		final Window window = WindowUtils.newWindow("Rate It!", null, false, false);
		window.setSize(300, 100);
		window.setLayoutOnChange(true);
		
		HorizontalPanel hz = new HorizontalPanel();

		final int weight = image.getWeight();
		final double curRating = image.getRating(0.0F);
		final double weightedRate = weight * curRating;

		final Image[] stars = new Image[5];
		for (int i = 0; i < 5; i++) {
			final int w = i;
			stars[i] = new Image(RATING_OUTLINE);
			stars[i].setSize("25", "25");
			stars[i].addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					for (int j = 0; j < 5; j++) {
						if (j <= w)
							stars[j].setUrl(RATING_FILLED);
						else
							stars[j].setUrl(RATING_OUTLINE);
					}
					image.setRating((float) (int) ((((w + 1) + weightedRate) / (weight + 1)) * 100) / 100);
					image.setWeight(weight + 1);
					window.layout();
				};
			});
			hz.add(stars[i]);

		}
		window.add(hz);
		
		window.addButton(new Button("Rate!", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				writeImagesToFS();
				window.hide();
			}
		}));
		window.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		}));
		window.setButtonAlign(HorizontalAlignment.CENTER);
		window.show();
		WindowManager.get().bringToFront(window);
	}

	public int getView() {
		return viewType;
	}

	private void init() {
		selected = -1;

		viewType = DETAIL_VIEW;
		Button item = new Button();
		item.setText("Attach New Image");
		item.setIconStyle("icon-attachment");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				// Popup addPopup = new Popup( true );
				final Window addPopup = WindowUtils.newWindow("Attach Image", null, true, false);
				addPopup.add(new FileUploadWidget(UriBase.getInstance().getImageBase() + 
						"/images/" + taxon.getId()) {

					public void init() {
						setLayout(new FillLayout());
						setStyleName("gwt-background");
						setLayoutOnChange(true);
						setWidth(400);
						setHeight(200);
						setBorders(true);
					}

					protected void onClose() {
						addPopup.hide();
					}

					protected void onSuccess(SubmitCompleteEvent event) {
						super.onSuccess(event);
						update(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
					}

					protected boolean validate() {
						if (uploader.getFilename().trim().equalsIgnoreCase("")) {
							WindowUtils.errorAlert("Error", "Please select an image file to attach.");
							return false;
						} else if (!ManagedImage.isAcceptedFormat(uploader.getFilename())) {
							WindowUtils.errorAlert("Error", "You must choose a valid image file.");
							return false;
						}
						return true;
					}
				});
				addPopup.show();
				addPopup.center();
			}
		});
		toolbar.add(item);

		item = new Button();
		item.setText("Remove Image");
		item.setIconStyle("icon-delete-image");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (selected == -1) {
					WindowUtils.infoAlert("Info", "Must select image to remove.");
					return;
				}
				imageList.remove(selected);
				selectedList.remove(selected);

				writeImagesToFS();
			}
		});
		toolbar.add(item);

		item = new Button();
		item.setText("Set as Primary");
		item.setIconStyle("icon-favorite");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (selected == -1) {
					WindowUtils.errorAlert("Error", "Must select image to make primary.");
					return;
				}

				for (int i = 0; i < imageList.size(); i++) {
					imageList.get(i).setPrimary(i == selected);
				}
				writeImagesToFS();

			}
		});
		toolbar.add(item);

		item = new Button();
		item.setText("View Image");
		item.setIconStyle("icon-image");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (selected == -1) {
					WindowUtils.errorAlert("Info", "Must select image to view.");
					return;
				}
				Window s = WindowUtils.newWindow("Taxon Image Viewer", null, true, true);
				s.setLayout(new FillLayout());
				s.add(new ImagePopupPanel(imageList.get(selected)));
				/*s.add(ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel);
				ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel
						.update(((ManagedImage) imageList.get(selected)));*/
				s.setScrollMode(Scroll.AUTO);
				s.setHeight(700);
				s.setWidth(700);
				s.show();
			}
		});
		toolbar.add(item);

		item = new Button();
		item.setText("Edit Details");
		item.setIconStyle("icon-edit-image");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (selected == -1) {
					WindowUtils.errorAlert("Error", "Must select image to edit.");
					return;
				}
				
				final Window content = WindowUtils.newWindow("Edit Image Details", null, true, true);
				content.setLayout(new RowLayout(Orientation.VERTICAL));
				
				final TaxonImage current = imageList.get(selected);
				
				final TextBox description = new TextBox();
				if (current.getCaption() != null)
					description.setText(current.getCaption());
				description.setSize("300", "150");
				final TextBox credit = new TextBox();
				if (current.getCredit() != null)
					credit.setText(current.getCredit());
				final TextBox source = new TextBox();
				if (current.getSource() != null)
					source.setText(current.getSource());
				final CheckBox redlist = new CheckBox();
				if (current.getShowRedList())
					redlist.setValue(true);
				redlist.setText("Display with credit on IUCN Red List");
				final CheckBox SIS = new CheckBox();
				if (current.getShowSIS())
					SIS.setValue(true);
				SIS.setText("Display with credit on IUCN SIS");

				content.add(new HTML("Photo caption/description:"));
				content.add(description);
				content.add(new HTML("Photo credit:"));
				content.add(credit);
				content.add(new HTML("Photo source or URL:"));
				content.add(source);
				content.add(redlist);
				content.add(SIS);

				content.addButton(new Button("Save Details", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						current.setCaption(description.getText());
						current.setCredit(credit.getText());
						current.setSource(source.getText());
						current.setShowRedList((redlist.getValue()));
						current.setShowSIS((SIS.getValue()));
						writeImagesToFS();
						content.hide();
					}
				}));
				content.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						content.hide();
					}
				}));
				content.setButtonAlign(HorizontalAlignment.CENTER);

				content.setHeight(420);
				content.setWidth(420);
				content.show();
				content.center();
			}
		});
		toolbar.add(item);

		item = new Button();
		item.setText("Gallery View");
		item.setIconStyle("icon-image");

		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				setView(GALLERY_VIEW);
				update(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
			}
		});
		viewbar.add(item);

		item = new Button();
		item.setText("Detail View");
		item.setIconStyle("icon-image");
		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				setView(DETAIL_VIEW);
				update(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
			}
		});
		viewbar.add(item);

		add(toolbar, new RowData(1d, -1));
		add(viewbar, new RowData(1d, -1));
		add(imagesContainer, new RowData(1d, 1d));
	}

	private void selectImage(int selection) {
		for (HorizontalPanel curPanel : selectedList)
			curPanel.setBorderWidth(0);

		((HorizontalPanel) selectedList.get(selection)).setBorderWidth(4);
	}

	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

	public void setView(int view) {
		viewType = view;
		if (viewType == GALLERY_VIEW) {
			imagesContainer.setScrollMode(Scroll.NONE);
			// imagesGrid = new Grid(5, GALLERY_COLS);
		}
		if (viewType == DETAIL_VIEW) {
			imagesContainer.setScrollMode(Scroll.AUTO);
			// imagesGrid = new Grid(50, 1);
		}
	}

	public void update(final DrawsLazily.DoneDrawingCallback callback) {
		selected = -1;
		selectedList.clear();
		imageList.clear();
		imagesContainer.removeAll();

		if (viewType == DETAIL_VIEW)
			imagesContainer.setLayout(new TableLayout(2));
		else
			imagesContainer.setLayout(new TableLayout(10));

		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.get(UriBase.getInstance().getImageBase() + "/images/" + taxon.getId(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not load images for this taxon.");
			}

			public void onSuccess(String result) {
				final NativeNodeList imageNodes = ndoc.getDocumentElement().getElementsByTagName("image");

				if (imageNodes.getLength() == 0)
					imagesContainer.add(new HTML("No Images Available."));
				else {
					for (int i = 0; i < imageNodes.getLength(); i++) {
						TaxonImage image = TaxonImage.fromXML(imageNodes.elementAt(i));
						addImage(image);
					}
					if (selected != -1) {
						selectImage(selected);
					}
				}

				layout();
				
				callback.isDrawn();
			}
		});
	}

	protected void writeImagesToFS() {
		StringBuilder xml = new StringBuilder();
		xml.append("<images>");
		for (int i = 0; i < imageList.size(); i++) {
			xml.append(imageList.get(i).toXML());
		}
		xml.append("</images>");

		NativeDocument newDoc = SimpleSISClient.getHttpBasicNativeDocument();
		newDoc.put(UriBase.getInstance().getImageBase() + "/images/" + taxon.getId(), xml.toString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Could not save image details, please try again later.");
			}
			public void onSuccess(String result) {
				//TaxonomyCache.impl.setCurrentTaxon(taxon);
				ClientUIContainer.bodyContainer.refreshBody();
				//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(Integer.valueOf(groupingId));
				update(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
			}
		});
	}

}
