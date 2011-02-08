package org.iucn.sis.client.panels.bookmarks;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.client.api.caches.BookmarkCache;
import org.iucn.sis.client.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.Bookmark;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.StyledHTML;

public class BookmarkManager extends Window {
	
	public BookmarkManager() {
		super();
		setLayout(new FillLayout());
		setHeading("Bookmark Manager");
		setIconStyle("icon-bookmark");
		setSize(600, 400);
		
		draw();
	}

	private void draw() {
		final ListStore<BookmarkModelData> store = new ListStore<BookmarkModelData>();
		for (Bookmark bookmark : BookmarkCache.impl.list())
			store.add(new BookmarkModelData(bookmark));
		
		final EditorGrid<BookmarkModelData> grid = new EditorGrid<BookmarkModelData>(store, getColumnModel());
		grid.setAutoExpandColumn("name");
		grid.setClicksToEdit(ClicksToEdit.TWO);
		grid.addListener(Events.AfterEdit, new Listener<GridEvent<BookmarkModelData>>() {
			public void handleEvent(final GridEvent<BookmarkModelData> be) {
				String newName = (String)be.getValue();
				
				Bookmark model = be.getModel().getModel();
				model.setName(newName);
				
				BookmarkCache.impl.update(model, new GenericCallback<String>() {
					public void onSuccess(String result) {
						be.getGrid().getStore().commitChanges();
						Info.display("Success", "Changes saved.");
					}
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Could not save changes, please try again later.");
					}
				});
			}
		});
		
		add(grid);
	}
	
	private ColumnModel getColumnModel() {
		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
		
		final TextField<String> editor = new TextField<String>();
		editor.setAllowBlank(false);
		
		ColumnConfig name = new ColumnConfig("name", "Bookmark (double-click to edit)", 350);
		name.setEditor(new CellEditor(editor));
		
		list.add(name);
		
		ColumnConfig date = new ColumnConfig("date", "Date Added", 150);
		date.setDateTimeFormat(FormattedDate.SHORT.getDateTimeFormat());
		
		list.add(date);
		
		ColumnConfig remove = new ColumnConfig("remove", "", 25);
		remove.setAlignment(HorizontalAlignment.CENTER);
		remove.setRenderer(new GridCellRenderer<BookmarkModelData>() {
			@Override
			public Object render(final BookmarkModelData model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<BookmarkModelData> store,
					final Grid<BookmarkModelData> grid) {
				
				HTML html = new StyledHTML("[X]", "SIS_HyperlinkLookAlike");
				html.setTitle("Remove this bookmark.");
				html.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						WindowUtils.confirmAlert("Confirm", "Are you sure you want to delete this bookmark?", new WindowUtils.SimpleMessageBoxListener() {
							public void onYes() {
								BookmarkCache.impl.remove(model.getModel(), new GenericCallback<String>() {
									public void onSuccess(String result) {
										store.remove(model);
										store.commitChanges();
										grid.getView().refresh(false);
									}
									public void onFailure(Throwable caught) {
										WindowUtils.errorAlert("Could not remove, please try again later.");
									}
								});
							}
						});
					}
				});
				
				return html;
			}
		});
		list.add(remove);
		
		return new ColumnModel(list);
	}
	
	private static class BookmarkModelData extends BaseModelData {
		private static final long serialVersionUID = 1L;
		
		private final Bookmark model;
		
		public BookmarkModelData(Bookmark model) {
			super();
			this.model = model;
			
			set("name", model.getName());
			set("date", model.getDate());
		}
		
		public Bookmark getModel() {
			return model;
		}
	}
	
}
