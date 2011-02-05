package org.iucn.sis.server.extensions.recentasms;

import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.RegionIO;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Region;

public class RecentAssessmentInfo extends RecentInfo<Assessment> {
	
	private final RegionIO regionIO;
	
	public RecentAssessmentInfo(Session session) {
		super(session);
		regionIO = new RegionIO(session);
	}
	
	@Override
	protected void parse(Assessment assessment) {
		if (assessment != null && assessment.getState() != Assessment.DELETED) {
			String region;
			if (assessment.isRegional()) {
				List<Integer> regions = assessment.getRegionIDs();
				if (regions.isEmpty())
					region = "(Unspecified Region)";
				else {
					Region r = regionIO.getRegion(regions.get(0));
					if (r == null)
						region = "(Invalid Region ID)";
					else if (regions.size() == 1)
						region = r.getName();
					else
						region = r.getName() + " + " + (regions.size() - 1) + " more...";
				}
				if (assessment.isEndemic())
					region += " -- Endemic";
			}
			else
				region = "Global";
			
			addField("id", assessment.getId() + "");
			addField("status", assessment.getAssessmentType().getName());
			addField("species", assessment.getSpeciesName());
			addField("region", region);
		}	
	}

}
