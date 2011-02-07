package org.iucn.sis.shared.conversions;

import java.io.File;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.RegionIO;
import org.iucn.sis.shared.helpers.Region;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;

public class RegionConverter extends GenericConverter<String> {
	
	@Override
	protected void run() throws Exception {
		File file = new File(data + "/HEAD/regions/regions.xml");
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(FileListing.readFileAsString(file));
		NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName("region");
		org.iucn.sis.shared.api.models.Region newRegion = new org.iucn.sis.shared.api.models.Region(org.iucn.sis.shared.api.models.Region.GLOBAL_ID, "Global", "Global");
		session.save(newRegion);
		for (int i = 0; i < list.getLength(); i++) {
			Region oldRegion = new Region(list.elementAt(i));
			newRegion = new org.iucn.sis.shared.api.models.Region(getNewRegionID(Integer.valueOf(oldRegion.getId())), oldRegion.getRegionName(), oldRegion.getDescription());
			session.save(newRegion);
		}

	}
	
	public static Integer getNewRegionID(Integer oldID) {
		return oldID + 2;
	}
	
	public static org.iucn.sis.shared.api.models.Region getNewRegion(Session session, Integer oldID) {
		RegionIO io = new RegionIO(session);
		return io.getRegion(getNewRegionID(oldID));
	}

}
