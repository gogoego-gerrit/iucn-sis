package org.iucn.sis.client.tabs.home;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.client.panels.utils.RefreshPortlet;
import org.iucn.sis.shared.api.models.VideoSource;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.StyledHTML;

public class VideoContentPortlet extends RefreshPortlet {
	
	public VideoContentPortlet() {
		super();
		setCollapsible(true);
		setAnimCollapse(false);
		setLayout(new FlowLayout());
		setLayoutOnChange(true);
		setHeading("Videos");
		setHeight(300);
		setScrollMode(Scroll.AUTO);
		
		refresh();
	}
	
	public void refresh() {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.get(UriBase.getInstance().getVideoBase() + "/sources/youtube", new GenericCallback<String>() {
			public void onSuccess(String result) {
				List<VideoSource> videos = new ArrayList<VideoSource>();
				
				NativeNodeList nodes = document.getDocumentElement().getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					NativeNode node = nodes.item(i);
					if ("video".equals(node.getNodeName()))
						videos.add(VideoSource.fromXML(node));
				}
				
				draw(videos);
			}
			public void onFailure(Throwable caught) {
				List<VideoSource> videos = new ArrayList<VideoSource>();
				
				draw(videos);
			}
		});
	}
	
	private void draw(final List<VideoSource> videos) {
		removeAll();
		
		if (videos.isEmpty()) {
			add(new Html("No videos to display."));
			return;
		}
		
		final FlexTable table = new FlexTable();
		table.setWidth("100%");
		table.setCellSpacing(8);
		final VerticalPanel featured = new VerticalPanel(); {
			featured.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			
			final VideoSource video = videos.get(0);
			final Image image = new Image();
			image.addStyleName("clickable");
			image.setUrl(UriBase.getInstance().getVideoBase() + video.getImage());
			image.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					showVideo(videos, video);
				}
			});
			
			final HTML html = new HTML(video.getTitle());
			html.addStyleName("clickable");
			html.addStyleName("page_home_videos_featured_caption");
			html.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					showVideo(videos, video);		
				}
			});
			
			featured.add(image);
			featured.add(html);
		}
		
		int row = 0;
		
		table.setWidget(row, 0, featured);
		table.getCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		
		row++;
		
		if (videos.size() > 1) {
			table.setWidget(row, 0, new StyledHTML("See More Videos", "page_home_videos_more_heading"));
			table.getCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		}
		
		row++;
		
		for (int i = 1; i < videos.size(); i++) {
			final VideoSource video = videos.get(i);
			
			HTML html = new HTML(video.getTitle());
			html.setWidth("100%");
			html.addStyleName("clickable");
			html.addStyleName("SIS_HyperlinkLookAlike");
			html.addStyleName("page_home_videos_more_caption");
			html.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					showVideo(videos, video);		
				}
			});
			
			table.setWidget(row, 0, html);
			
			table.getCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
			
			row++;
		}
		
		add(table);
	}
	
	private void showVideo(List<VideoSource> videos, VideoSource video) {
		final VideoWindow window = new VideoWindow(videos);
		window.setCurrent(video);
		window.show();
	}
	
	private static class VideoWindow extends BasicWindow {
		
		private final List<VideoSource> videos;
		private final ContentPanel player;
		private final HtmlContainer caption;
		private final Grid grid;
		
		public VideoWindow(List<VideoSource> videos) {
			super();
			this.videos = videos;
			this.player = new ContentPanel();
			this.caption = new HtmlContainer();
			this.grid = new Grid(videos.size(), 1);
			
			setSize(750, 500);
			setLayout(new FillLayout());
			
			drawGrid();
		}
		
		private void drawGrid() {
			grid.setCellSpacing(5);
			grid.setCellPadding(0);
			grid.setWidth("100%");
			
			int row = 0;
			for (VideoSource video : videos) {
				HTML html = new StyledHTML(video.getTitle(), "page_home_videos_viewer_row");
				html.addStyleName("clickable");
				html.setWidth("100%");
				
				grid.setWidget(row++, 0, html);
			}
			
			grid.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					Cell cell = grid.getCellForEvent(event);
					if (cell != null) {
						setCurrent(cell.getRowIndex());
					}
				}
			});
			
			final LayoutContainer listing = new LayoutContainer();
			listing.setScrollMode(Scroll.AUTO);
			listing.add(grid);
			
			player.setHeaderVisible(false);
			player.setBodyBorder(false);
			player.setBorders(false);
			
			caption.addStyleName("page_home_videos_viewer_caption");
			
			final LayoutContainer captionWrapper = new LayoutContainer();
			captionWrapper.setScrollMode(Scroll.AUTO);
			captionWrapper.add(caption);
			
			final LayoutContainer playerAndCaption = new LayoutContainer(new BorderLayout());
			playerAndCaption.add(player, new BorderLayoutData(LayoutRegion.CENTER));
			playerAndCaption.add(captionWrapper, new BorderLayoutData(LayoutRegion.SOUTH, 100, 100, 300));
			
			final LayoutContainer container = new LayoutContainer(new BorderLayout());
			container.add(listing, new BorderLayoutData(LayoutRegion.WEST, 150, 150, 150));
			container.add(playerAndCaption, new BorderLayoutData(LayoutRegion.CENTER));
			
			add(container);
		}
		
		public void setCurrent(VideoSource video) {
			setCurrent(videos.indexOf(video));
		}
		
		public void setCurrent(int index) {
			VideoSource video;
			try {
				video = videos.get(index);
			} catch (Exception e) {
				WindowUtils.errorAlert("Could not find this video.");
				return;
			}
			
			for (int i = 0; i < grid.getRowCount(); i++) {
				grid.getRowFormatter().removeStyleName(i, "page_home_videos_viewer_row_selected");
				if (i == index)
					grid.getRowFormatter().addStyleName(i, "page_home_videos_viewer_row_selected");
			}
			
			setHeading("Now Viewing - " + video.getTitle());
			player.setUrl(video.getUrl());
			caption.setHtml(video.getCaption());
		}
		
	}
	
}
