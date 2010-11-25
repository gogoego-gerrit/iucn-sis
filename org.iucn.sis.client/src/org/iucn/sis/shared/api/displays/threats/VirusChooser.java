package org.iucn.sis.shared.api.displays.threats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.iucn.sis.client.api.caches.VirusCache;
import org.iucn.sis.client.panels.viruses.VirusModelData;
import org.iucn.sis.shared.api.models.Virus;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.portable.PortableAlphanumericComparator;

public class VirusChooser extends LayoutContainer implements DrawsLazily {
	
	private final CheckBoxListView<VirusModelData> list;

	public VirusChooser() {
		super(new FillLayout());
		list = new CheckBoxListView<VirusModelData>();
	}
	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		VirusCache.impl.list(new ComplexListener<List<Virus>>() {
			public void handleEvent(List<Virus> eventData) {
				if (eventData.isEmpty()) {
					WindowUtils.infoAlert("No viruses found in the system to choose from.");
				}
				else {
					Collections.sort(eventData, new VirusComparator());
					
					final ListStore<VirusModelData> store = new ListStore<VirusModelData>();
					for (Virus virus : eventData)
						store.add(new VirusModelData(virus));
					
					list.setStore(store);
					
					add(list);
					
					callback.isDrawn();
				}
			}
		});
	}
	
	public List<Virus> getSelection() {
		final List<Virus> checked = new ArrayList<Virus>();
		for (VirusModelData model : list.getChecked())
			checked.add(model.getVirus());
		
		return checked;
	}
	
	private static class VirusComparator implements Comparator<Virus> {
		
		private static final long serialVersionUID = 1L;
		
		private final PortableAlphanumericComparator comparator;
		
		public VirusComparator() {
			this.comparator = new PortableAlphanumericComparator();
		}
		
		@Override
		public int compare(Virus o1, Virus o2) {
			return comparator.compare(o1.getName(), o2.getName());
		}
		
	}
	
}
