package org.iucn.sis.client.panels.utils;

import java.util.Comparator;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.container.StateManager;
import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
import org.iucn.sis.client.api.utils.TaxonComparator;
import org.iucn.sis.client.api.utils.TaxonPagingLoader;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonHierarchy;
import org.iucn.sis.shared.api.models.TaxonLevel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.utils.ArrayUtils;
import com.solertium.util.events.SimpleListener;
import com.solertium.util.extjs.client.ViewerFilterTextBox;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;

/**
 * 
 * @author carl.scott@solertium.com
 *
 */
@SuppressWarnings("deprecation")
public class TaxonomyBrowserPanel extends LayoutContainer {
	
	protected String[] footprints;
	
	protected VerticalPanel footprintPanel;
	protected VerticalPanel optionsPanel;
	protected VerticalPanel summaryPanel;
	
	private final TaxonPagingLoader loader;
	private final ListStore<TaxonListElement> store;
	private final DataListBinder<TaxonListElement> binder;
	private final PagingToolBar pagingToolBar;
	private final ViewerFilterTextBox<TaxonListElement> textBox;
	private final StoreFilter<TaxonListElement> filter;
	private final DataList list;
	
	private int checkableLevel;
	
	private int cellWidth = 0;
	
	private boolean asCheckable = false;

	@SuppressWarnings("unchecked")
	public TaxonomyBrowserPanel() {
		super();
		
		list = new DataList();
		list.setScrollMode(Scroll.AUTOY);
		list.setSelectionMode(SelectionMode.SIMPLE);
		
		checkableLevel = TaxonLevel.ORDER;

		loader = new TaxonPagingLoader();

		store = new ListStore<TaxonListElement>(loader.getPagingLoader());

		binder = new DataListBinder<TaxonListElement>(list, store);
		binder.setDisplayProperty("name");
		binder.setStyleProvider(new ModelStringProvider<TaxonListElement>() {
			public String getStringValue(TaxonListElement model, String property) {
				return model.getStyleName();	
			}
		});
		
		textBox = new ViewerFilterTextBox<TaxonListElement>();
		textBox.bind(store);

		filter = new StoreFilter<TaxonListElement>() {
			public boolean select(Store store, TaxonListElement parent, TaxonListElement item, String property) {
				String txt = textBox.getText();
				if (txt != null && !txt.equals("")) {
					String elementString = (item).getNode().getName().toLowerCase();
					return elementString.toLowerCase().startsWith(txt.toLowerCase());
				}
				return true;
			}
		};

		store.setStoreSorter(new StoreSorter(TaxonComparator.getInstance()));
		store.addFilter(filter);

		pagingToolBar = new PagingToolBar(40);
		pagingToolBar.bind(loader.getPagingLoader());
	}
	
	public void setCheckableLevel(int checkableLevel) {
		this.checkableLevel = checkableLevel;
	}

	protected void addViewButtonToFootprint() {
		footprintPanel.add(new Button("View", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				WindowUtils.showLoadingAlert("Loading...");
				TaxonomyCache.impl.fetchTaxon(Integer.valueOf(footprints[footprints.length - 1]), true, new GenericCallback<Taxon>() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Failed to fetch taxon, please try again later.");
					}
					public void onSuccess(Taxon result) {
						//updateNodeSummary();
						StateManager.impl.setTaxon(result);
					}
				});
			}
		}));
	}

	protected void display(final TaxonHierarchy hierarchy) {
		String footprint = hierarchy.getFootprint();

		if (footprint.indexOf("-") > 0)
			footprints = footprint.split("-");
		else if (!footprint.equals(""))
			footprints = new String[] { footprint };
		else
			footprints = new String[0];

		updateFootprintPanel(hierarchy.getTaxon());
		
		updateBody(hierarchy, footprint, 0, new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				resizeList(getOffsetWidth(), getOffsetHeight());
				
				onChangedTaxon(hierarchy.getTaxon());
				
				layout();
				
				hideLoadingScreen();
			}
		});
	}

	/**
	 * Does nothing by default, but is AFTER a user selects a new taxon, and the hierarchy browser
	 * has been updated to reflect the new taxon. 
	 */
	private void onChangedTaxon(final Taxon taxon) {
		//String id = footprints[footprints.length - 1];
		if (taxon == null)
			return;
		 
		try {
			fireEvent(Events.Change, new TaxonChangeEvent(TaxonomyBrowserPanel.this, taxon));
			/*TaxonomyCache.impl.fetchTaxon(Integer.valueOf(id), new GenericCallback<Taxon>() {
				public void onSuccess(Taxon result) {
					fireEvent(Events.Change, new TaxonChangeEvent(TaxonomyBrowserPanel.this, result));
				}
				public void onFailure(Throwable caught) {
				}
			});*/
		} catch (IndexOutOfBoundsException e) {
			//It's fine.
		} catch (NumberFormatException e) {
			//Also fine
		} catch (NullPointerException e) {
			//Also fine.
		}
	}
	
	public DataListBinder<TaxonListElement> getBinder() {
		return binder;
	}

	public DataList getList() {
		return list;
	}
	
	
	public ListStore<TaxonListElement> getStore() {
		return store;
	}
	
	/**
	 * Fetches the "children" of the last node in the path.
	 * 
	 * @param path
	 */
	protected void fetch(String path) {
		fetch(path, null);
	}
	
	protected void fetch(String path, final GenericCallback<Object> callback) {
		showLoadingScren();
		TaxonomyCache.impl.fetchPath(path, new GenericCallback<TaxonHierarchy>() {
			public void onFailure(Throwable caught) {
				hideLoadingScreen();
				WindowUtils.errorAlert("Failed to fetch taxa, please try again later.");
				if (callback != null)
					callback.onFailure(caught);
			}
			public void onSuccess(TaxonHierarchy result) {
				display(result);
				if (callback != null)
					callback.onSuccess(result);
			}
		});
	}

	protected void fetchWithID(String id) {
		showLoadingScren();
		TaxonomyCache.impl.fetchPathWithID(id, new GenericCallback<TaxonHierarchy>() {
			public void onFailure(Throwable caught) {
				hideLoadingScreen();
				WindowUtils.errorAlert("Failed to fetch taxa, please try again later.");
			}
			public void onSuccess(TaxonHierarchy result) {
				display(result);
			}
		});
	}

	private HorizontalPanel generateFootprintSection() {
		HTML spacer = new HTML("Kingdoms ->");
		spacer.setVisible(false);

		HorizontalPanel inner = new HorizontalPanel();
		inner.addStyleName("SIS_hasSmallerHTML");
		inner.setSize("auto", "auto");
		inner.setSpacing(2);
		inner.add(spacer);

		return inner;
	}

	protected HTML getClickableHTML(final String label, final String path) {
		final HTML ret = new HTML(label);
		ret.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				fetch(path);
			}
		});
		ret.addStyleName("SIS_HyperlinkLookAlike");
		ret.addStyleName("inline");

		/*if (!label.equalsIgnoreCase("Kingdoms")) {
			TaxonomyCache.impl.fetchTaxon(Integer.valueOf(label), false, new GenericCallback<Taxon>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Failed to fetch taxon, please try again later.");
				}
				public void onSuccess(Taxon result) {
					ret.setText(result.getName());
				}
			});
		}*/

		return ret;
	}
	
	public boolean isAsCheckable() {
		return asCheckable;
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		resizeList(width, height);
	}

	private void resizeList(int width, int height) {
		if (list != null && footprintPanel != null && width > 0 && height > 0) {
			int bodyWidth = width - 10;
			int bodyHeight = height - footprintPanel.getOffsetHeight();

			optionsPanel.setSize(bodyWidth + "px", bodyHeight + "px");

			// Set the size a bit smaller, to take into account the text box and
			// label
			list.setSize(bodyWidth, bodyHeight - 75 - 25);
		}
	}
	
	private void showLoadingScren() {
		if (isRendered())
			el().mask("Loading...");
	}
	
	private void hideLoadingScreen() {
		if (isRendered())
			el().unmask();
	}

	public void setAsCheckable(boolean asCheckable) {
		this.asCheckable = asCheckable;
	}

	public void update() {
		updateWithoutFetch();
		fetch("");
	}
	
	public void update(GenericCallback<Object> callback){
		updateWithoutFetch();
		fetch("", callback);
	}

	public void update(String id) {
		updateWithoutFetch();
		fetchWithID(id);
	}

	protected void updateBody(final TaxonHierarchy hierarchy, String footprint, int offset, final DrawsLazily.DoneDrawingCallback callback) {
		if (list.isCheckable())
			for (DataListItem curItem : list.getItems())
				curItem.setChecked(false);
		
		optionsPanel.clear();
		binder.removeAllListeners();
		
		loader.getFullList().clear();
		loader.getPagingLoader().setOffset(0);
		
		list.getChecked().clear();

		if (!hierarchy.hasChildren()) {
			optionsPanel.add(new HTML("No " + Taxon.getDisplayableLevel(footprint.split("-").length) + "."));
		
			callback.isDrawn();
		}
		else {
			if (footprint.equals(""))
				optionsPanel.add(new HTML("<b>Kingdom</b><br />"));
			else
				optionsPanel.add(new HTML("<b>" + Taxon.getDisplayableLevel(footprint.split("-").length)
						+ "</b><br />"));

			if (!footprint.equals("") && !footprint.endsWith("-"))
				footprint += "-";
			
			final String foot = footprint;
			
			list.setCheckable(asCheckable && footprints.length >= checkableLevel);
			
			final SimpleListener listener = new SimpleListener() {
				public void handleEvent() {
					binder.removeAllListeners();
					binder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
						public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
							if (se.getSelectedItem() != null)
								fetch(se.getSelectedItem().getFootprint());
						}
					});

					// binder.init();
					optionsPanel.add(textBox);
					optionsPanel.add(list);
					optionsPanel.add(pagingToolBar);
					optionsPanel.add(new Html("* denotes new taxon"));
					
					callback.isDrawn();
				}
			};
			
			if (!hierarchy.hasChildren())
				listener.handleEvent();
			else {
				loader.getFullList().clear();
				for (Taxon node : hierarchy.getChildren()) {
					if (node != null) {
						String path = foot + node.getId();
						TaxonListElement el = new TaxonListElement(node, path);

						loader.getFullList().add(el);
					}
				}

				//FIXME: this should come back from the server in the correct order...
				ArrayUtils.quicksort(loader.getFullList(), new Comparator<TaxonListElement>() {
					public int compare(TaxonListElement o1, TaxonListElement o2) {
						return ((String) o1.get("name")).compareTo((String) o2.get("name"));
					}
				});
				loader.getPagingLoader().load();
				
				listener.handleEvent();
				/*TaxonomyCache.impl.fetchList(hierarchy.getChildren(), new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Could not fetch taxa, please try again later.");
					}
					public void onSuccess(String arg0) {
						loader.getFullList().clear();
						for (Integer id : hierarchy.getChildren()) {
							Taxon node = TaxonomyCache.impl.getTaxon(id);
							if (node != null) {
								String path = foot + node.getId();
								TaxonListElement el = new TaxonListElement(node, path);

								loader.getFullList().add(el);
							}
						}

						ArrayUtils.quicksort(loader.getFullList(), new Comparator<TaxonListElement>() {
							public int compare(TaxonListElement o1, TaxonListElement o2) {
								return ((String) o1.get("name")).compareTo((String) o2.get("name"));
							}
						});
						loader.getPagingLoader().load();
						
						listener.handleEvent();
					}
				});*/
			}
		}
	}

	private void updateFootprintPanel(Taxon taxon) {
		if (footprintPanel == null)
			return;

		footprintPanel.clear();

		if (footprints.length == 0)
			footprintPanel.add(new HTML("Kingdoms"));
		else {
			cellWidth = getOffsetWidth();

			HorizontalPanel inner = generateFootprintSection();
			footprintPanel.add(inner);

			HTML curHTML;
			HTML arrow;

			inner.clear();
			inner.add(getClickableHTML("Kingdoms", ""));

			String[] displayNames = taxon.getFootprint();
			String path = "";
			for (int i = 0; i < footprints.length; i++) {
				path += footprints[i] + "-";
				arrow = new HTML("->");
				if (i < footprints.length - 1)
					curHTML = getClickableHTML(displayNames[i], path.substring(0, path.length() - 1));
				else
					curHTML = new HTML(taxon.getName());

				inner.add(arrow);
				if ((inner.getOffsetWidth() + arrow.getOffsetWidth() + 5) > cellWidth) {
					inner.remove(arrow);
					inner = generateFootprintSection();
					footprintPanel.add(inner);
				}
				inner.add(arrow);

				inner.add(curHTML);
				if (inner.getOffsetWidth() + curHTML.getOffsetWidth() + 5 > cellWidth) {
					inner.remove(curHTML);
					inner = generateFootprintSection();
					footprintPanel.add(inner);
				}
				inner.add(curHTML);
			}

			addViewButtonToFootprint();
		}
	}

	public void updateWithoutFetch() {
		removeAll();

		summaryPanel = new VerticalPanel();
		summaryPanel.addStyleName("padded");

		footprints = new String[0];
		footprintPanel = new VerticalPanel();
		footprintPanel.addStyleName("padded");
		HorizontalPanel inner = new HorizontalPanel();
		inner.addStyleName("SIS_hasSmallerHTML");
		footprintPanel.add(inner);
		footprintPanel.addStyleName("SIS_hasSmallerHTML");
		footprintPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		optionsPanel = new VerticalPanel();
		optionsPanel.setSpacing(3);

		add(footprintPanel);
		add(summaryPanel);
		add(optionsPanel);

		layout();
	}
	
	public static class TaxonChangeEvent extends BaseEvent {
		
		private final Taxon taxon;
		
		public TaxonChangeEvent(TaxonomyBrowserPanel source, Taxon newTaxon) {
			super(Events.Change);
			setSource(this);
			this.taxon = newTaxon;
		}
		
		public Taxon getTaxon() {
			return taxon;
		}
		
	}

}
