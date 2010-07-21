package org.iucn.sis.client.api.ui.models.region;

import org.iucn.sis.shared.api.models.Region;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.solertium.lwxml.shared.NativeElement;

public class RegionModel extends BaseModel {
		private static final long serialVersionUID = 4770673249783507045L;

		Region region;

		public RegionModel(NativeElement el) {
			
			set("name", region.getName());
			set("description", region.getDescription());
			set("id", region.getId());
		}

		public RegionModel(Region r) {
			region = r;
			set("name", region.getName());
			set("description", region.getDescription());
			set("id", region.getId()+"");
		}

		public Region getRegion() {
			return region;
		}

		public void sinkModelDataIntoRegion() {
			region.setName((String) get("name"));
			//region.setId((Integer) get("id"));
			region.setDescription((String) get("description"));
		}
	}