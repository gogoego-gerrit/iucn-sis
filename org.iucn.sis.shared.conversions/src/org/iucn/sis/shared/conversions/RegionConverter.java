package org.iucn.sis.shared.conversions;

import java.io.File;
import java.io.IOException;

import org.gogoego.api.plugins.GoGoEgo;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.data.assessments.Region;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class RegionConverter {

	public static void generateRegions() throws PersistentException, IOException {
		File file = new File(GoGoEgo.getInitProperties().get("sis_old_vfs") + "/HEAD/regions/regions.xml");
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(FileListing.readFileAsString(file));
		NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName("region");
		org.iucn.sis.shared.api.models.Region newRegion = new org.iucn.sis.shared.api.models.Region(org.iucn.sis.shared.api.models.Region.GLOBAL_ID, "Global", "Global");
		SIS.get().getManager().getSession().save(newRegion);
		for (int i = 0; i < list.getLength(); i++) {
			Region oldRegion = new Region(list.elementAt(i));
			newRegion = new org.iucn.sis.shared.api.models.Region(getNewRegionID(Integer.valueOf(oldRegion.getId())), oldRegion.getRegionName(), oldRegion.getDescription());
			SIS.get().getManager().getSession().save(newRegion);
		}

	}
	
	public static Integer getNewRegionID(Integer oldID) {
		return oldID + 2;
	}
	
	public static org.iucn.sis.shared.api.models.Region getNewRegion(Integer oldID) {
		return SIS.get().getRegionIO().getRegion(getNewRegionID(oldID));
	}

}
