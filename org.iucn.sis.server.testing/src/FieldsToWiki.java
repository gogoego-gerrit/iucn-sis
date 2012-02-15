import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.debug.Debugger;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.api.views.FieldParser;
import org.iucn.sis.shared.api.views.components.DisplayData;
import org.iucn.sis.shared.api.views.components.TreeData;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.AlphanumericComparator;


public class FieldsToWiki {
	
	private static final List<String> credits = Arrays.asList(CanonicalNames.credits);
	private static final List<String> deprecated = Arrays.asList(new String[] {
		"InPlaceEducation.xml", "InPlaceLandWaterProtection.xml", 
		"InPlaceResearch.xml", "InPlaceSpeciesManagement.xml"
	});
	
	public static void main(String[] args) throws Exception {
		Debug.setInstance(new Debugger() {
			public void println(String template, Object... args) {
			}
			public void println(Object obj) {
			}
			public void println(Throwable e) {
			}
		});
		
		String current = new File("here").getAbsolutePath();
		String root = current.substring(0, current.lastIndexOf('/', current.lastIndexOf('/')-1));
		String directory = "/org.iucn.sis.server.api/src/org/iucn/sis/server/api/fields/definitions";
		
		File folder = new File(root + directory);
		if (!folder.exists() || !folder.isDirectory())
			throw new RuntimeException("Not a folder.");
		
		List<File> files = Arrays.asList(folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith("_") && name.endsWith(".xml") && 
					!name.endsWith("Old.xml") && !deprecated.contains(name);
			}
		}));
		Collections.sort(files, new FileSorter());
		
		FieldParser parser = new FieldParser();
		for (File file : files) {
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			
			while ((line = reader.readLine()) != null)
				builder.append(line);
			
			NativeDocument document = new JavaNativeDocument();
			document.parse(builder.toString());
			
			DisplayData currentDisplayData = parser.parseFieldData(document);
			
			Map<String, String> descriptions = 
				extractDescriptions(currentDisplayData, document);
			
			System.out.println(formatCanonicalName(currentDisplayData));
			System.out.println(formatHeaders());
			for (Map.Entry<String, String> entry : descriptions.entrySet())
				System.out.println(formatRow(entry.getKey(), entry.getValue()));
		}
		
		System.out.println("There are " + files.size() + " total fields available.");
	}
	
	private static String formatCanonicalName(DisplayData data) {
		return String.format("|\\2. \n*\"%s\":http://sis.iucnsis.org/apps/org.iucn.sis.server/application" +
				"/schema/org.iucn.sis.server.schemas.redlist/field/%s* %s |", 
				data.getCanonicalName(), data.getCanonicalName(), 
				data instanceof TreeData ? " (Classification Scheme)" : "");
	}
	
	private static String formatHeaders() {
		return "|_. Data Point |_. Description |";
	}
	
	private static String formatRow(String name, String description) {
		return String.format("|{width:100px}. %s |{width:200px;}. %s |", name, description);
	}
	
	private static Map<String, String> extractDescriptions(DisplayData data, NativeDocument document) {
		String canonicalName = data.getCanonicalName();
		String defaultDescription = data.getDescription();
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (CanonicalNames.Threats.equals(canonicalName)) {
			map.put("ThreatsLookup", "Selected Coding Option");
			map.put("timing", "Timing");
			map.put("scope", "Scope");
			map.put("severity", "Severity");
			map.put("score", "Impact Score");
			//TODO: stresses... list.add("No. of Stresses");
		}
		else if (credits.contains(canonicalName)) {
			map.put("text", "Text-based account of names");
			map.put("value", "List of profile IDs.");
		}
		else if (CanonicalNames.UseTradeDetails.endsWith(canonicalName)) {
			map.put("purpose", "Purpose");
			map.put("source", "Source");
			map.put("formRemoved", "Form Removed");
			map.put("subsistence", "Subsistence");
			map.put("national", "National");
			map.put("international", "International");
			map.put("harvestLevel", "Harvest Level");
			map.put("units", "Units");
			map.put("possibleThreat", "Possible Threat");
			map.put("justification", "Notes and Justification");
		}
		else {
			if (data instanceof TreeData)
				map.put(canonicalName + "Lookup", "Selected Coding Option");
			NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("structure");
			for (int i = 0; i < nodes.getLength(); i++) {
				NativeElement node = nodes.elementAt(i);
				String id = node.getAttribute("id");
				String description = node.getAttribute("description");
				if (description == null || "".equals(description))
					description = defaultDescription;
				if (id != null && !"".equals(id)) {
					if ("qualifier".equals(id))
						map.put("qualifier", "Qualifier");
					else if ("justification".equals(id))
						map.put("justification", "Justification");
					else if ("note".equals(id))
						map.put("note", "Note");
					else
						map.put(id, description);
				}
			}
		}
		
		return map;
	}
	
	private static final class FileSorter implements Comparator<File> {
		
		private final AlphanumericComparator comparator = new AlphanumericComparator();
		
		@Override
		public int compare(File o1, File o2) {
			return comparator.compare(o1.getName(), o2.getName());
		}
		
	}

}
