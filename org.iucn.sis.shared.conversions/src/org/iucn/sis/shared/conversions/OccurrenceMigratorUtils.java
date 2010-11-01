package org.iucn.sis.shared.conversions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.iucn.sis.shared.helpers.AssessmentData;
import org.iucn.sis.shared.helpers.CanonicalNames;


public class OccurrenceMigratorUtils {
	/**
	 * This will migrate occurrence data from the old format (included passage
	 * migrant as its own check box) to the new format, with three select boxes.
	 * 
	 * @param data
	 *            Assessment to work on
	 * @return true/false, if changes were made to the assessment
	 */
	public static boolean migrateOccurrenceData(AssessmentData data) {
		boolean writeback = false;

		if (operateOn(data, CanonicalNames.CountryOccurrence))
			writeback = true;

		if (writeback = operateOn(data, CanonicalNames.FAOOccurrence))
			writeback = true;

		if (writeback = operateOn(data, CanonicalNames.LargeMarineEcosystems))
			writeback = true;

		return writeback;
	}

	public static void modifyOccurrenceEntry(Entry<String, ArrayList<String>> curSelected) {
		curSelected.getValue().ensureCapacity(4);

		String presenceCode = curSelected.getValue().get(0);
		String passageMigrant = curSelected.getValue().get(1);
		String origin = curSelected.getValue().get(2);

		String seasonality = "";

		if (!presenceCode.equals("") && !presenceCode.equals("0")) {
			int pCode = Integer.valueOf(presenceCode);
			if (pCode <= 3) {
				curSelected.getValue().set(0, "1");

				if (pCode == 1)
					seasonality += "1,";
				else if (pCode == 2)
					seasonality += "2,";
				else if (pCode == 3)
					seasonality += "3,";
			} else if (pCode == 4)
				curSelected.getValue().set(0, "2");
			else if (pCode == 5)
				curSelected.getValue().set(0, "3");
			else if (pCode == 6)
				curSelected.getValue().set(0, "4");
		} else {
			curSelected.getValue().set(0, "0");
		}

		curSelected.getValue().set(1, "0");

		if (passageMigrant.equals("true"))
			seasonality += "4";

		if (!origin.equals("") && !origin.equals("0")) {
			int oCode = Integer.valueOf(origin);

			if (oCode == 1)
				curSelected.getValue().set(2, "1");
			else if (oCode == 2)
				curSelected.getValue().set(2, "3");
			else if (oCode == 3)
				curSelected.getValue().set(2, "2");
			else if (oCode == 4)
				curSelected.getValue().set(2, "5");
			else if (oCode == 5)
				curSelected.getValue().set(2, "6");
			else if (oCode == 9) // This shouldn't be in there, but somehow a
				// few are...
				curSelected.getValue().set(2, "6");
		} else
			curSelected.getValue().set(2, "0");

		if (seasonality.endsWith(","))
			seasonality = seasonality.substring(0, seasonality.length() - 1);

		curSelected.getValue().add(seasonality);
	}

	public static boolean operateOn(AssessmentData data, String canonicalName) {
		boolean writeback = false;

		if (data.getDataMap().containsKey(canonicalName)) {
			HashMap<String, ArrayList<String>> coo = (HashMap) data.getDataMap().get(canonicalName);

			if (coo.size() != 0)
				writeback = true;

			for (Entry<String, ArrayList<String>> curSelected : coo.entrySet()) {
				if (curSelected.getValue().size() == 4) { // This is already modded

					String presence = curSelected.getValue().get(0);
					String origin = curSelected.getValue().get(2);
					
					if( origin.equals("9") )
						curSelected.getValue().set(2, "6");
					
					if (presence.equals("4"))
						curSelected.getValue().set(0, "2");
					else if (presence.equals("5"))
						curSelected.getValue().set(0, "3");
					else if (presence.equals("6"))
						curSelected.getValue().set(0, "4");
					
					writeback = false;
					continue;
				} else {
					writeback = true;
					modifyOccurrenceEntry(curSelected);
				}
			}
		}
		return writeback;
	}
}
