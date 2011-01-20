package org.iucn.sis.server.extensions.recentasms;

import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Region;

public class RecentAssessmentInfo extends RecentInfo<Assessment> {
	
	@Override
	protected void parse(Assessment assessment) {
		if (assessment != null && assessment.getState() != Assessment.DELETED) {
			String region;
			if (assessment.isRegional()) {
				List<Integer> regions = assessment.getRegionIDs();
				if (regions.isEmpty())
					region = "(Unspecified Region)";
				else {
					Region r = SIS.get().getRegionIO().getRegion(regions.get(0));
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
