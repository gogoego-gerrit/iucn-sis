package org.iucn.sis.server.authFiles.csv;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.iucn.sis.server.ServerUtils;

import com.solertium.util.SysDebugger;

public class AuthFileConverter {

	class CSVRow {
		public String id = "";
		public String description = "";
		public String codeable = "true";
		public String expanded = "false";
		public String title = "";
		public int depth = 1;
		public int family;
		public String parent;

		public ArrayList<CSVRow> children;

		public CSVRow(String[] rowData) {
			id = rowData[ID_INDEX];

			description = rowData[DESC_INDEX].trim();
			/* For FAO_Marine.csv, countries */// description = (rowData[1] +
			// " " + rowData[2]).trim();
			description = description.replaceAll("&", "and");
			String[] test = description.split(" ");
			if (test.length > 1) {
				if (test[0].equalsIgnoreCase(id)) {
					description = description.substring(id.length(), description.length());// test[1];
				}
			}

			/*
			 * For plant growth forms for (int i = 3; i < rowData.length; i++) {
			 * title += rowData[i]; if ((i+1) < rowData.length) title += ", "; }
			 * title = title.replaceAll("\"", "");
			 */
			try {
				codeable = rowData[CODEABLE_INDEX].equalsIgnoreCase("x") ? "true" : "false";
			} catch (Exception e) {
				codeable = "false";
			}

			/* for plant growth forms, countries */// codeable = "true";
			depth = id.split("\\.").length;

			try {
				family = Integer.parseInt(id.split("\\.")[0]);
			} catch (Exception e) {
				SysDebugger.getInstance().println("Error (" + id + "): " + e.getMessage());
				SysDebugger.getInstance().println(id.split("\\.").length);
				family = Integer.parseInt(id);
			}

			int size = depth - 1;
			String[] idSplit = id.split("\\.");
			parent = "";

			for (int i = 0; i < size; i++) {
				parent += idSplit[i];
				if ((i + 1) < size) {
					parent += ".";
				}
			}

			children = new ArrayList<CSVRow>();
		}

		public void addChild(CSVRow row) {
			children.add(row);
		}

		public String getParent() {
			return parent;
		}
	}

	class Tree {
		public String rowNum;
		public ArrayList<CSVRow> rows;

		public Tree(String rowNum) {
			this.rowNum = rowNum;
			rows = new ArrayList<CSVRow>();
		}

		public void addCSVRow(CSVRow row) {
			rows.add(row);
		}

		public ArrayList<CSVRow> getRows() {
			return rows;
		}
	}

	private static BufferedReader reader;
	private static ArrayList<Tree> trees;

	private static AuthFileConverter converter;
	private static final int startTab = 2;
	// These can get set in parse headers, or you can set them yourself
	private static int ID_INDEX = 0;

	private static int DESC_INDEX = 2;

	private static int CODEABLE_INDEX = 0;

	private static String xml = "";

	private static void doCSVtoXML(BufferedReader reader) {
		String line = "";
		try {
			String[] rowData = new String[parseHeaders(reader.readLine())];
			trees = new ArrayList<Tree>();
			trees.add(null);

			// Read in each line, convert
			while ((line = reader.readLine()) != null) {
				rowData = line.split(",");

				if (rowData[ID_INDEX].split("\\.").length == 1) {
					Tree tree = converter.new Tree(rowData[ID_INDEX]);
					trees.add(tree);
				}

				CSVRow current = converter.new CSVRow(rowData);
				if (current.family != 100)
					trees.get(current.family).addCSVRow(current);
			}

			writeXML();

			try {
				reader.close();
				if (ServerUtils.writeStringToFile("stresses.xml", xml)) {
					SysDebugger.getInstance().println("Contents written!");
				} else {
					SysDebugger.getInstance().println("Failed");
					SysDebugger.getInstance().println(xml);
				}
			} catch (Exception e) {
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static CSVRow fillParentWithChildren(Tree tree) {
		ArrayList<CSVRow> rows = tree.getRows();
		CSVRow parent = null;
		int maxDepth = 0;
		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).depth == 1)
				parent = rows.get(i);
			if (rows.get(i).depth > maxDepth)
				maxDepth = rows.get(i).depth;
		}

		for (int count = 2; count <= maxDepth; count++) {
			for (int i = 0; i < rows.size(); i++) {
				if (rows.get(i).depth == count) {
					int index = findParentIndex(rows.get(i).getParent(), tree);
					if (index != -1) {
						rows.get(index).addChild(rows.get(i));
					}
				}
			}
		}

		return parent;
	}

	private static int findParentIndex(String parent, Tree tree) {
		for (int i = 0; i < tree.getRows().size(); i++) {
			if (tree.getRows().get(i).id.equalsIgnoreCase(parent)) {
				return i;
			}
		}
		return -1;
	}

	private static String getTabs(int numTabs) {
		String tabs = "";
		for (int i = 0; i < numTabs; i++) {
			tabs += "\t";
		}
		return tabs;
	}

	public static void main(String args[]) {
		converter = new AuthFileConverter();

		reader = ServerUtils.getFileReaderForFile("hibernate/csv/stresses.csv");
		if (reader == null) {
			SysDebugger.getInstance().println("Reader is null");
			System.exit(1);
		}

		doCSVtoXML(reader);
	}

	private static int parseHeaders(String line) {
		String[] header = line.split(",");
		for (int i = 0; i < header.length; i++) {
			if (header[i].indexOf("Description") != -1) {
				DESC_INDEX = i;
			} else if (header[i].indexOf("odeable") != -1) {
				CODEABLE_INDEX = i;
			}
		}
		return line.split(",").length;
	}

	private static void write(String line) {
		xml += line + "\n";
	}

	private static void writeChild(CSVRow childRow, int numTabs) {
		write(getTabs(numTabs) + "<child" + " id=\"" + childRow.id + "\"" + " depth=\"" + childRow.depth + "\""
				+ (childRow.codeable.equalsIgnoreCase("false") ? " codeable=\"false\" " : "")
				+ (childRow.expanded.equalsIgnoreCase("false") ? " expanded=\"false\"" : "") + ">");
		write(getTabs(numTabs + 1)
				+ "<label"
				+ ((childRow.title != null && !childRow.title.equalsIgnoreCase("")) ? " title=\"" + childRow.title
						+ "\"" : "") + ">" + childRow.description + "</label>");
		// writeTreeStructures(childRow, numTabs+1);
		for (int i = 0; i < childRow.children.size(); i++) {
			writeChild(childRow.children.get(i), numTabs + 1);
		}
		write(getTabs(numTabs) + "</child>");
	}

	private static void writeTreeStructures(CSVRow row, int numTabs) {
		/*
		 * FOR THREATS write(getTabs(numTabs) + "<treeStructures>");
		 * write(getTabs(numTabs+1) + "<structure>"); write(getTabs(numTabs+2) +
		 * "<threat>"); write(getTabs(numTabs+3) + "<timing>0</timing>");
		 * write(getTabs(numTabs+3) + "<scope>0</scope>");
		 * write(getTabs(numTabs+3) + "<severity>0</severity>");
		 * write(getTabs(numTabs+3) + "<impact>0</impact>");
		 * write(getTabs(numTabs+2) + "</threat>"); write(getTabs(numTabs+1) +
		 * "</structure>"); write(getTabs(numTabs) + "</treeStructures>");
		 */
		/* FOR STRESSES */
		write(getTabs(numTabs) + "<treeStructures>");
		write(getTabs(numTabs + 1) + "<structure description=\"Stress?\">");
		write(getTabs(numTabs + 2) + "<boolean>false</boolean>");
		write(getTabs(numTabs + 1) + "</structure>");
		write(getTabs(numTabs) + "</treeStructures>");
		/*
		 * FOR CONSERVATION ACTIONS write(getTabs(numTabs) +
		 * "<treeStructures>"); write(getTabs(numTabs+1) +
		 * "<structure description=\"In place?\">"); write(getTabs(numTabs+2) +
		 * "<boolean>false</boolean>"); write(getTabs(numTabs+1) +
		 * "</structure>"); write(getTabs(numTabs) + "</treeStructures>");
		 */
		/*
		 * FOR HABITATS write(getTabs(numTabs) + "<treeStructures>");
		 * write(getTabs(numTabs+1) + "<structure>"); write(getTabs(numTabs+2) +
		 * "<relatedStructure>");
		 * 
		 * write(getTabs(numTabs+3) + "<dominantStructures>");
		 * write(getTabs(numTabs+4) + "<structure>"); write(getTabs(numTabs+5) +
		 * "<singleSelect>"); write(getTabs(numTabs+6) +
		 * "<option>Suitable</option>"); write(getTabs(numTabs+6) +
		 * "<option>Marginal</option>"); write(getTabs(numTabs+6) +
		 * "<option>Possible</option>"); write(getTabs(numTabs+6) +
		 * "<selected>0</selected>"); write(getTabs(numTabs+5) +
		 * "</singleSelect>"); write(getTabs(numTabs+4) + "</structure>");
		 * write(getTabs(numTabs+3) + "</dominantStructures>");
		 * 
		 * write(getTabs(numTabs+3) + "<dependentStructures>");
		 * write(getTabs(numTabs+4) + "<structure>"); write(getTabs(numTabs+5) +
		 * "<singleSelect>"); write(getTabs(numTabs+6) +
		 * "<option>Yes</option>"); write(getTabs(numTabs+6) +
		 * "<option>No</option>"); write(getTabs(numTabs+6) +
		 * "<selected>0</selected>"); write(getTabs(numTabs+5) +
		 * "</singleSelect>"); write(getTabs(numTabs+4) + "</structure>");
		 * write(getTabs(numTabs+3) + "</dependentStructures>");
		 * 
		 * write(getTabs(numTabs+3) + "<rules>"); write(getTabs(numTabs+4) +
		 * "<selectRule>"); write(getTabs(numTabs+5) +
		 * "<activateOnIndex>0</activateOnIndex>"); write(getTabs(numTabs+5) +
		 * "<actions>"); write(getTabs(numTabs+6) + "<onTrue>show</onTrue>");
		 * write(getTabs(numTabs+6) + "<onFalse>hide</onFalse>");
		 * write(getTabs(numTabs+5) + "</actions>"); write(getTabs(numTabs+4) +
		 * "</selectRule>"); write(getTabs(numTabs+3) + "</rules>");
		 * 
		 * write(getTabs(numTabs+2) + "</relatedStructure>");
		 * write(getTabs(numTabs+1) + "</structure>"); write(getTabs(numTabs) +
		 * "</treeStructures>");
		 */
		/*
		 * FOR LARGE MARINE ECOSYSTEMS, COUNTRIES, ... write(getTabs(numTabs) +
		 * "<treeStructures>"); write(getTabs(numTabs+1) + "<structure>");
		 * write(getTabs(numTabs+2) + "<singleSelect>");
		 * write(getTabs(numTabs+3) + "<option>Year Round</option>");
		 * write(getTabs(numTabs+3) + "<option>Breeding Season Only</option>");
		 * write(getTabs(numTabs+3) +
		 * "<option>Non-breeding Season Only</option>");
		 * write(getTabs(numTabs+3) + "<selected></selected>");
		 * write(getTabs(numTabs+2) + "</singleSelect>");
		 * write(getTabs(numTabs+1) + "</structure>");
		 * 
		 * write(getTabs(numTabs+1) + "<structure>"); write(getTabs(numTabs+2) +
		 * "<boolean>false</boolean>"); write(getTabs(numTabs+1) +
		 * "</structure>");
		 * 
		 * write(getTabs(numTabs+1) + "<structure>"); write(getTabs(numTabs+2) +
		 * "<singleSelect>"); write(getTabs(numTabs+3) +
		 * "<option>Possibly Extinct</option>"); write(getTabs(numTabs+3) +
		 * "<option>Extinct</option>"); write(getTabs(numTabs+3) +
		 * "<option>Not extinct</option>"); write(getTabs(numTabs+3) +
		 * "<selected></selected>"); write(getTabs(numTabs+2) +
		 * "</singleSelect>"); write(getTabs(numTabs+1) + "</structure>");
		 * 
		 * write(getTabs(numTabs+1) + "<structure>"); write(getTabs(numTabs+2) +
		 * "<relatedStructure>");
		 * 
		 * write(getTabs(numTabs+3) + "<dominantStructures>");
		 * write(getTabs(numTabs+4) + "<structure>"); write(getTabs(numTabs+5) +
		 * "<boolean>false</boolean>"); write(getTabs(numTabs+4) +
		 * "</structure>"); write(getTabs(numTabs+3) + "</dominantStructures>");
		 * 
		 * write(getTabs(numTabs+3) + "<dependentStructures>");
		 * write(getTabs(numTabs+4) + "<structure>"); write(getTabs(numTabs+5) +
		 * "<relatedStructure>");
		 * 
		 * write(getTabs(numTabs+6) + "<dominantStructures>");
		 * write(getTabs(numTabs+7) + "<structure>"); write(getTabs(numTabs+8) +
		 * "<boolean>false</boolean>"); write(getTabs(numTabs+7) +
		 * "</structure>"); write(getTabs(numTabs+6) + "</dominantStructures>");
		 * 
		 * write(getTabs(numTabs+6) + "<dependentStructures>");
		 * write(getTabs(numTabs+7) + "<structure>"); write(getTabs(numTabs+8) +
		 * "<singleSelect>"); write(getTabs(numTabs+9) +
		 * "<option>Native</option>"); write(getTabs(numTabs+9) +
		 * "<option>Introduced</option>"); write(getTabs(numTabs+9) +
		 * "<option>Reintroduced</option>"); write(getTabs(numTabs+9) +
		 * "<option>Vagrant</option>"); write(getTabs(numTabs+9) +
		 * "<selected>0</selected>"); write(getTabs(numTabs+8) +
		 * "</singleSelect>"); write(getTabs(numTabs+7) + "</structure>");
		 * write(getTabs(numTabs+6) + "</dependentStructures>");
		 * 
		 * write(getTabs(numTabs+6) + "<rules>"); write(getTabs(numTabs+7) +
		 * "<booleanRule>"); write(getTabs(numTabs+8) +
		 * "<activateOnRule>false</activateOnRule>"); write(getTabs(numTabs+8) +
		 * "<actions>"); write(getTabs(numTabs+9) + "<onTrue>show</onTrue>");
		 * write(getTabs(numTabs+9) + "<onFalse>hide</onFalse>");
		 * write(getTabs(numTabs+8) + "</actions>"); write(getTabs(numTabs+7) +
		 * "</booleanRule>"); write(getTabs(numTabs+6) + "</rules>");
		 * 
		 * write(getTabs(numTabs+5) + "</relatedStructure>");
		 * write(getTabs(numTabs+4) + "</structure>"); write(getTabs(numTabs+3)
		 * + "</dependentStructures>");
		 * 
		 * write(getTabs(numTabs+3) + "<rules>"); write(getTabs(numTabs+4) +
		 * "<booleanRule>"); write(getTabs(numTabs+5) +
		 * "<activateOnRule>false</activateOnRule>"); write(getTabs(numTabs+5) +
		 * "<actions>"); write(getTabs(numTabs+6) + "<onTrue>show</onTrue>");
		 * write(getTabs(numTabs+6) + "<onFalse>hide</onFalse>");
		 * write(getTabs(numTabs+5) + "</actions>"); write(getTabs(numTabs+4) +
		 * "</booleanRule>"); write(getTabs(numTabs+3) + "</rules>");
		 * 
		 * write(getTabs(numTabs+2) + "</relatedStructure>");
		 * write(getTabs(numTabs+1) + "</structure>");
		 * 
		 * write(getTabs(numTabs) + "</treeStructures>");
		 */
		/*
		 * FOR RESEARCH NEEDED write(getTabs(numTabs) + "<treeStructures>");
		 * write(getTabs(numTabs+1) + "<structure description=\"Needed?\">");
		 * write(getTabs(numTabs+2) + "<boolean>false</boolean>");
		 * write(getTabs(numTabs+1) + "</structure>"); write(getTabs(numTabs) +
		 * "</treeStructures>");
		 */
	}

	private static void writeXML() {
		write(getTabs(startTab) + "<treeRoot>");
		ArrayList<CSVRow> roots = new ArrayList<CSVRow>();
		for (int i = 1; i < trees.size(); i++) {
			Tree currTree = trees.get(i);
			roots.add(fillParentWithChildren(currTree));
		}

		write(getTabs(startTab + 1) + "<defaultStructure>");
		writeTreeStructures(null, startTab + 2);
		write(getTabs(startTab + 1) + "</defaultStructure>");

		for (int i = 0; i < roots.size(); i++) {
			CSVRow current = roots.get(i);
			if (!(current == null)) {
				write(getTabs(startTab + 1) + "<root" + " id=\"" + current.id + "\"" + " depth=\"" + current.depth
						+ "\"" + (current.codeable.equalsIgnoreCase("false") ? " codeable=\"false\"" : "")
						+ (current.expanded.equalsIgnoreCase("false") ? " expanded=\"false\"" : "") + ">");
				write(getTabs(startTab + 2)
						+ "<label"
						+ ((current.title != null && !current.title.equalsIgnoreCase("")) ? " title=\"" + current.title
								+ "\"" : "") + ">" + current.description + "</label>");
				// writeTreeStructures(current, startTab+2);
				for (int j = 0; j < current.children.size(); j++) {
					writeChild(current.children.get(j), startTab + 2);
				}
				write(getTabs(startTab + 1) + "</root>");
			}
		}

		write(getTabs(startTab) + "</treeRoot>");

	}

}
