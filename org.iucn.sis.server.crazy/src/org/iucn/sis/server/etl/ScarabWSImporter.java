package org.iucn.sis.server.etl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.CSVTokenizer;
import com.solertium.util.NodeCollection;
import com.solertium.util.Replacer;

public class ScarabWSImporter {

	private static Map<String, String> footprints = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {
		File scarabDir = new File("/home/rob.heittman/workspace-projects/SISServer1_5/scarabs");
		System.out.println("Reading scarab taxonomy.");
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				new File(scarabDir, "taxonomy.xml"));
		traverse("", doc.getDocumentElement());
		String[] scarabFiles = new String[] { "ScarabsAfrotropical", "ScarabsAustralia", "ScarabsMadagascar",
				"ScarabsNearctic", "ScarabsNeotropical", "ScarabsOriental", "ScarabsPalaearctic" };
		int found = 0;
		int notFound = 0;
		int count = 100;
		for (String scarabFile : scarabFiles) {
			System.out.println(scarabFile);
			File csv = new File(scarabDir, scarabFile + ".csv");
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv), "UTF-8"));
			count++;
			File xml = new File(scarabDir, "ws/" + count + ".xml");
			FileWriter fw = new FileWriter(xml);
			fw.write("<workingSet creator=\"sacha.spector\" id=\"" + count + "\">\n");
			fw.write("<info>\n");
			fw.write("<name>Imp" + scarabFile + "</name>\n");
			fw.write("<date>2008-09-22</date>\n");
			fw.write("<mode>public</mode>\n");
			fw.write("<description>Imported List: " + scarabFile + "</description>\n");
			fw.write("<notes></notes>\n");
			fw.write("</info>\n");
			fw.write("<taxa>\n");
			String line = "";
			line = br.readLine(); // clear header line
			while (line != null) {
				line = br.readLine();
				if (line == null)
					break;
				CSVTokenizer tok = new CSVTokenizer(line);
				String t = tok.nextToken();
				t = Replacer.stripWhitespace(t);
				String s = "/ANIMALIA/ARTHROPODA/INSECTA/COLEOPTERA/SCARABAEIDAE/" + t.replace(' ', '/');
				String i = footprints.get(s);
				if (i == null) {
					notFound++;
					System.out.println("NOT FOUND: " + t);
				} else {
					fw.write("<species>" + i + "</species>\n");
					found++;
				}
			}
			fw.write("</taxa>\n");
			fw.write("<persons>\n");
			fw.write("</persons>\n");
			fw.write("</workingSet>");
			fw.close();
		}
		System.out.println("Taxa found: " + found);
		System.out.println("Taxa NOT found: " + notFound);
	}

	public static void traverse(String parentFootprint, Element e) {
		for (Node node : new NodeCollection(e.getChildNodes())) {
			if (node instanceof Element) {
				Element child = (Element) node;
				String nodeId = child.getNodeName().substring(4);
				String name = child.getAttribute("name");
				name = Replacer.replace(name, "ssp. ", "");
				String footprint = parentFootprint + "/" + name;
				footprints.put(footprint, nodeId);
				// System.out.println(footprint + ": "+nodeId);
				traverse(footprint, child);
			}
		}
	}

}
