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

import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.debug.Debugger;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldType;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.api.views.FieldParser;
import org.iucn.sis.shared.api.views.components.DisplayData;
import org.iucn.sis.shared.api.views.components.TreeData;

import com.solertium.db.DBSessionFactory;
import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.AlphanumericComparator;


public class FieldsToWiki {
	
	private static final List<String> credits = Arrays.asList(CanonicalNames.credits);
	private static final List<String> deprecated = Arrays.asList(new String[] {
		"InPlaceEducation.xml", "InPlaceLandWaterProtection.xml", 
		"InPlaceResearch.xml", "InPlaceSpeciesManagement.xml",
		"RedListEvaluationDate.xml", "PVAFile.xml"
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
		
		DBSessionFactory.registerDataSource("sis_lookups", 
			"**URL**", 
			"org.postgresql.Driver", "**USER**", "**PASSWORD**"
		);
		
		String current = new File("here").getAbsolutePath();
		String root = current.substring(0, current.lastIndexOf(File.separatorChar, current.lastIndexOf(File.separatorChar)-1));
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
		
		FieldSchemaGenerator generator = new FieldSchemaGenerator();
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
			
			Field field;
			try {
				field = generator.getField(currentDisplayData.getCanonicalName());
			} catch (Exception e){
				continue;
			}
			
			Map<String, String> descriptions = 
				extractDescriptions(currentDisplayData, document);
			
			System.out.println(formatCanonicalName(currentDisplayData));
			System.out.println(formatHeaders());
			for (Map.Entry<String, String> entry : descriptions.entrySet()) {
				Field template = field.getPrimitiveField().isEmpty() ? 
						field.getField(field.getName() + "Subfield") : field;
				PrimitiveField<?> pf = template.getPrimitiveField(entry.getKey());
				String type = pf == null ? "N/A" : toFriendlyDescription(pf.getClass());
				System.out.println(formatRow(entry.getKey(), type, entry.getValue()));
			}
		}
		
		System.out.println("There are " + files.size() + " total fields available.");
	}
	
	private static String toFriendlyDescription(Class<?> pfType) {
		PrimitiveFieldType type = PrimitiveFieldType.get(pfType.getSimpleName());
		String text;
		switch (type) {
			case BOOLEAN_PRIMITIVE: text = "Boolean"; break;
			case BOOLEAN_RANGE_PRIMITIVE: text = "Boolean Range"; break;
			case BOOLEAN_UNKNOWN_PRIMITIVE: text = "Boolean Unknown"; break;
			case DATE_PRIMITIVE: text = "Date"; break;
			case FLOAT_PRIMITIVE: text = "Numeric"; break;
			case FOREIGN_KEY_LIST_PRIMITIVE: text = "Multiple Select"; break;
			case FOREIGN_KEY_PRIMITIVE: text = "Single Select"; break;
			case INTEGER_PRIMITIVE: text = "Integer"; break;
			case RANGE_PRIMITIVE: text = "Range"; break;
			case STRING_PRIMITIVE: text = "Short Text"; break;
			case TEXT_PRIMITIVE: text = "Long Text/Narrative"; break;
			default: text = "N/A";
		}
		return text;
	}
	
	private static String formatCanonicalName(DisplayData data) {
		return String.format("|\\3. \n*\"%s\":http://sis.iucnsis.org/apps/org.iucn.sis.server/application" +
				"/schema/org.iucn.sis.server.schemas.redlist/field/%s* %s |", 
				data.getCanonicalName(), data.getCanonicalName(), 
				data instanceof TreeData ? " (Classification Scheme)" : "");
	}
	
	private static String formatHeaders() {
		return "|_. Data Point |_. Description |_. Data Type |";
	}
	
	private static String formatRow(String name, String type, String description) {
		return String.format("|{width:150px;}. %s |{width:250px;}. %s |{width:100px;}. %s |", name, description, type);
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
			if (!CanonicalNames.RedListFacilitators.equals(canonicalName))
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
